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
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.prd.model.Organisation;
import uk.gov.hmcts.reform.civil.service.CoreCaseUserService;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.civil.service.querymanagement.QueryManagementCamundaService;
import uk.gov.hmcts.reform.civil.service.querymanagement.QueryManagementVariables;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_RAISED_QUERY;
import static uk.gov.hmcts.reform.civil.utils.CaseQueriesUtil.getUserRoleForQuery;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.buildPartiesReferencesEmailSubject;
import static uk.gov.hmcts.reform.civil.utils.UserRoleUtils.isApplicantSolicitor;
import static uk.gov.hmcts.reform.civil.utils.UserRoleUtils.isRespondentSolicitorOne;
import static uk.gov.hmcts.reform.civil.utils.UserRoleUtils.isRespondentSolicitorTwo;

@Service
@RequiredArgsConstructor
public class RaiseQuerySolicitorNotificationHandler extends CallbackHandler implements NotificationData {

    private static final List<CaseEvent> EVENTS = Collections.singletonList(NOTIFY_RAISED_QUERY);

    public static final String TASK_ID = "QueryRaisedNotify";
    private static final String REFERENCE_TEMPLATE = "query-raised-notification-%s";
    private static final String UNSUPPORTED_ROLE_ERROR = "Unsupported case role for query management.";

    private final NotificationService notificationService;
    private final NotificationsProperties notificationsProperties;
    private final OrganisationService organisationService;
    private final CoreCaseUserService coreCaseUserService;
    private final QueryManagementCamundaService runtimeService;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_SUBMIT), this::notifyPartyForQueryRaised,
            callbackKey(SUBMITTED), this::emptySubmittedCallbackResponse
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

    private CallbackResponse notifyPartyForQueryRaised(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        String processInstanceId = caseData.getBusinessProcess().getProcessInstanceId();
        QueryManagementVariables processVariables = runtimeService.getProcessVariables(processInstanceId);
        String queryId = processVariables.getQueryId();

        List<String> roles = getUserRoleForQuery(caseData, coreCaseUserService, queryId);
        String email = getEmail(caseData, roles);
        Map<String, String> properties = getProperties(caseData, roles);

        notificationService.sendMail(
            email,
            notificationsProperties.getQueryRaised(),
            properties,
            String.format(REFERENCE_TEMPLATE, caseData.getLegacyCaseReference())
        );

        return AboutToStartOrSubmitCallbackResponse.builder().build();
    }

    @Override
    public Map<String, String> addProperties(CaseData caseData) {

        return Map.of(
            CLAIM_LEGAL_ORG_NAME_SPEC, getApplicantLegalOrganizationName(caseData.getApplicant1OrganisationPolicy()
                .getOrganisation().getOrganisationID(), caseData),
            CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString(),
            PARTY_REFERENCES, buildPartiesReferencesEmailSubject(caseData),
            CASEMAN_REF, caseData.getLegacyCaseReference()
        );
    }

    public Map<String, String> addPropertiesForRespondent(CaseData caseData) {

        return Map.of(
            CLAIM_LEGAL_ORG_NAME_SPEC, getApplicantLegalOrganizationName(
                caseData.getRespondent1OrganisationPolicy().getOrganisation().getOrganisationID(), caseData),
            CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString(),
            PARTY_REFERENCES, buildPartiesReferencesEmailSubject(caseData),
            CASEMAN_REF, caseData.getLegacyCaseReference()
        );
    }

    public Map<String, String> addPropertiesForRespondent2(CaseData caseData) {

        return Map.of(
            CLAIM_LEGAL_ORG_NAME_SPEC, getApplicantLegalOrganizationName(
                caseData.getRespondent2OrganisationPolicy().getOrganisation().getOrganisationID(), caseData),
            CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString(),
            PARTY_REFERENCES, buildPartiesReferencesEmailSubject(caseData),
            CASEMAN_REF, caseData.getLegacyCaseReference()
        );
    }

    public String getApplicantLegalOrganizationName(String id, CaseData caseData) {

        Optional<Organisation> organisation = organisationService.findOrganisationById(id);
        return organisation.isPresent() ? organisation.get().getName() :
            caseData.getApplicantSolicitor1ClaimStatementOfTruth().getName();
    }

    private String getEmail(CaseData caseData, List<String> roles) {
        if (isApplicantSolicitor(roles)) {
            return caseData.getApplicantSolicitor1UserDetails().getEmail();
        } else if (isRespondentSolicitorOne(roles)) {
            return caseData.getRespondentSolicitor1EmailAddress();
        } else if (isRespondentSolicitorTwo(roles)) {
            return caseData.getRespondentSolicitor2EmailAddress();
        } else {
            throw new IllegalArgumentException(UNSUPPORTED_ROLE_ERROR);
        }
    }

    private Map<String, String> getProperties(CaseData caseData, List<String> roles) {
        if (isApplicantSolicitor(roles)) {
            return addProperties(caseData);
        } else if (isRespondentSolicitorOne(roles)) {
            return addPropertiesForRespondent(caseData);
        } else if (isRespondentSolicitorTwo(roles)) {
            return addPropertiesForRespondent2(caseData);
        } else {
            throw new IllegalArgumentException(UNSUPPORTED_ROLE_ERROR);
        }
    }
}
