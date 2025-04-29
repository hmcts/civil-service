package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.querymanagement.CaseMessage;
import uk.gov.hmcts.reform.civil.model.querymanagement.CaseQueriesCollection;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.notify.NotificationsSignatureConfiguration;
import uk.gov.hmcts.reform.civil.service.CoreCaseUserService;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.civil.service.querymanagement.QueryManagementCamundaService;
import uk.gov.hmcts.reform.civil.service.querymanagement.QueryManagementVariables;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_RESPONSE_TO_QUERY;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.formatLocalDate;
import static uk.gov.hmcts.reform.civil.utils.CaseQueriesUtil.getQueryById;
import static uk.gov.hmcts.reform.civil.utils.CaseQueriesUtil.getUserRoleForQuery;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.unwrapElements;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.addCommonFooterSignature;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.addSpecAndUnspecContact;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.buildPartiesReferencesEmailSubject;
import static uk.gov.hmcts.reform.civil.utils.QueryNotificationUtils.getEmail;
import static uk.gov.hmcts.reform.civil.utils.QueryNotificationUtils.getProperties;
import static uk.gov.hmcts.reform.civil.utils.UserRoleUtils.isApplicantSolicitor;
import static uk.gov.hmcts.reform.civil.utils.UserRoleUtils.isRespondentSolicitorOne;
import static uk.gov.hmcts.reform.civil.utils.UserRoleUtils.isRespondentSolicitorTwo;

@Service
@RequiredArgsConstructor
public class QueryResponseSolicitorNotificationHandler extends CallbackHandler implements NotificationData {

    private static final List<CaseEvent> EVENTS = Collections.singletonList(NOTIFY_RESPONSE_TO_QUERY);

    public static final String TASK_ID = "QueryResponseNotify";
    private static final String REFERENCE_TEMPLATE = "response-to-query-notification-%s";
    private static final String QUERY_NOT_FOUND = "Matching parent query not found.";

    private final NotificationService notificationService;
    private final QueryManagementCamundaService runtimeService;
    private final OrganisationService organisationService;
    private final NotificationsProperties notificationsProperties;
    private final CoreCaseUserService coreCaseUserService;
    private final NotificationsSignatureConfiguration configuration;
    private final FeatureToggleService featureToggleService;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_SUBMIT), this::notifyPartyForResponseToQuery
        );
    }

    @Override
    public String camundaActivityId(CallbackParams callbackParams) {
        return TASK_ID;
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse notifyPartyForResponseToQuery(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        String processInstanceId = caseData.getBusinessProcess().getProcessInstanceId();
        QueryManagementVariables processVariables = runtimeService.getProcessVariables(processInstanceId);
        String responseQueryId = processVariables.getQueryId();
        CaseMessage responseQuery = getQueryById(caseData, responseQueryId);
        String parentQueryId = responseQuery.getParentId();
        CaseMessage parentQuery = getQueryById(caseData, parentQueryId);

        List<String> roles = getUserRoleForQuery(caseData, coreCaseUserService, parentQueryId);
        String email = getEmail(caseData, roles);
        Map<String, String> properties = getProperties(caseData, roles, addProperties(caseData),
                                                       organisationService);
        LocalDate queryDate = getOriginalQueryCreatedDate(caseData, responseQuery, roles, parentQuery);
        properties.put(QUERY_DATE, formatLocalDate(queryDate, DATE));

        notificationService.sendMail(
            email,
            notificationsProperties.getQueryResponseReceived(),
            properties,
            String.format(REFERENCE_TEMPLATE, caseData.getLegacyCaseReference())
        );

        return AboutToStartOrSubmitCallbackResponse.builder().build();
    }

    private LocalDate getOriginalQueryCreatedDate(CaseData caseData, CaseMessage responseQuery, List<String> roles, CaseMessage parentQuery) {
        if (isApplicantSolicitor(roles)) {
            return getLastRelatedQueryRaisedBySolicitorDate(caseData.getQmApplicantSolicitorQueries(),
                                                            parentQuery, responseQuery);
        } else if (isRespondentSolicitorOne(roles)) {
            return getLastRelatedQueryRaisedBySolicitorDate(caseData.getQmRespondentSolicitor1Queries(),
                                                            parentQuery, responseQuery);
        } else if (isRespondentSolicitorTwo(roles)) {
            return getLastRelatedQueryRaisedBySolicitorDate(caseData.getQmRespondentSolicitor2Queries(),
                                                            parentQuery, responseQuery);
        }
        return null;
    }

    private LocalDate getLastRelatedQueryRaisedBySolicitorDate(CaseQueriesCollection solicitorQueries,
                                                               CaseMessage parentQuery, CaseMessage responseQuery) {
        List<CaseMessage> caseMessages = unwrapElements(solicitorQueries.getCaseMessages());
        // check if there was a follow up query
        List<CaseMessage> queriesByUserWithMatchingParentId = caseMessages.stream()
            .filter(m -> responseQuery.getParentId().equals(m.getParentId())
            && m.getCreatedBy().equals(parentQuery.getCreatedBy())
            && m.getCreatedOn().isBefore(responseQuery.getCreatedOn())).toList();
        CaseMessage latestQuery;
        if (queriesByUserWithMatchingParentId.size() > 0) {
            // if there was a follow up query
            latestQuery = queriesByUserWithMatchingParentId.stream().max(Comparator.comparing(CaseMessage::getCreatedOn))
                .orElse(null);
        } else {
            // no follow up queries
            latestQuery = parentQuery;
        }
        if (latestQuery != null) {
            return latestQuery.getCreatedOn().toLocalDate();
        } else {
            throw new IllegalArgumentException(QUERY_NOT_FOUND);
        }
    }

    @Override
    public Map<String, String> addProperties(CaseData caseData) {
        HashMap<String, String> properties = new HashMap<>(Map.of(
            CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString(),
            PARTY_REFERENCES, buildPartiesReferencesEmailSubject(caseData),
            CASEMAN_REF, caseData.getLegacyCaseReference()));
        addCommonFooterSignature(properties, configuration);
        addSpecAndUnspecContact(caseData, properties, configuration,
                                featureToggleService.isQueryManagementLRsEnabled());
        return properties;
    }
}
