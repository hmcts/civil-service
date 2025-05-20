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
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.service.CoreCaseUserService;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.civil.service.querymanagement.QueryManagementCamundaService;
import uk.gov.hmcts.reform.civil.service.querymanagement.QueryManagementVariables;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_OTHER_PARTY_QUERY_HAS_RESPONSE;

import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CASEMAN_REF;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_LEGAL_ORG_NAME_SPEC;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.PARTY_REFERENCES;
import static uk.gov.hmcts.reform.civil.utils.CaseQueriesUtil.getQueryById;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.buildPartiesReferencesEmailSubject;
import static uk.gov.hmcts.reform.civil.utils.QueryNotificationUtils.EMAIL;
import static uk.gov.hmcts.reform.civil.utils.QueryNotificationUtils.LEGAL_ORG;
import static uk.gov.hmcts.reform.civil.utils.QueryNotificationUtils.getOtherPartyEmailDetails;

@Service
@RequiredArgsConstructor
public class NotifyOtherPartyQueryHasResponseNotificationHandler extends CallbackHandler  {

    private static final List<CaseEvent> EVENTS = Collections.singletonList(NOTIFY_OTHER_PARTY_QUERY_HAS_RESPONSE);

    public static final String TASK_ID = "NotifyOtherPartyQueryHasResponse";
    private static final String REFERENCE_TEMPLATE = "other-party-response-to-query-notification-%s";

    private final NotificationService notificationService;
    private final QueryManagementCamundaService runtimeService;
    private final OrganisationService organisationService;
    private final NotificationsProperties notificationsProperties;
    private final CoreCaseUserService coreCaseUserService;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_SUBMIT), this::notifyOtherPartyForResponseToQuery
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

    private CallbackResponse notifyOtherPartyForResponseToQuery(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        // to avoid null pointer exception for lip bypassing the notification
        if (caseData.isLipCase()) {
            return AboutToStartOrSubmitCallbackResponse.builder().build();
        }
        String processInstanceId = caseData.getBusinessProcess().getProcessInstanceId();
        QueryManagementVariables processVariables = runtimeService.getProcessVariables(processInstanceId);
        String responseQueryId = processVariables.getQueryId();
        CaseMessage responseQuery = getQueryById(caseData, responseQueryId);
        String parentQueryId = responseQuery.getParentId();

        List<Map<String, String>> emailDetailsList = getOtherPartyEmailDetails(caseData, organisationService, coreCaseUserService, parentQueryId);

        emailDetailsList.forEach(otherPartyEmailDetails -> {
            notificationService.sendMail(
                otherPartyEmailDetails.get(EMAIL),
                notificationsProperties.getNotifyOtherPartyQueryResponseReceived(),
                addProperties(caseData, otherPartyEmailDetails.get(LEGAL_ORG)),
                String.format(REFERENCE_TEMPLATE, caseData.getLegacyCaseReference())
            );
        });

        return AboutToStartOrSubmitCallbackResponse.builder().build();
    }

    public Map<String, String> addProperties(CaseData caseData, String legalOrgName) {
        return new HashMap<>(Map.of(
            CLAIM_LEGAL_ORG_NAME_SPEC, legalOrgName,
            CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString(),
            PARTY_REFERENCES, buildPartiesReferencesEmailSubject(caseData),
            CASEMAN_REF, caseData.getLegacyCaseReference()));
    }
}
