package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.model.CaseData;
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
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_OTHER_PARTY_FOR_RAISED_QUERY;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_ONE;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_TWO_ONE_LEGAL_REP;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_TWO_TWO_LEGAL_REP;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.TWO_V_ONE;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.getMultiPartyScenario;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CASEMAN_REF;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_LEGAL_ORG_NAME_SPEC;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.PARTY_REFERENCES;
import static uk.gov.hmcts.reform.civil.utils.CaseQueriesUtil.getUserRoleForQuery;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.buildPartiesReferencesEmailSubject;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.getApplicantLegalOrganizationName;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.getLegalOrganizationNameForRespondent;
import static uk.gov.hmcts.reform.civil.utils.QueryNotificationUtils.isOtherPartyApplicant;

@Service
@RequiredArgsConstructor
public class NotifyOtherPartyQueryRaisedNotificationHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = Collections.singletonList(NOTIFY_OTHER_PARTY_FOR_RAISED_QUERY);

    public static final String TASK_ID = "NotifyOtherPartyQueryRaised";
    private static final String REFERENCE_TEMPLATE = "a-query-has-been-raised-notification-%s";

    private final NotificationService notificationService;
    private final NotificationsProperties notificationsProperties;
    private final OrganisationService organisationService;
    private final CoreCaseUserService coreCaseUserService;
    private final QueryManagementCamundaService runtimeService;

    @Override
    public String camundaActivityId(CallbackParams callbackParams) {
        return TASK_ID;
    }

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_SUBMIT), this::notifyOtherPartyQueryHasBeenRaised
        );
    }

    private CallbackResponse notifyOtherPartyQueryHasBeenRaised(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        String processInstanceId = caseData.getBusinessProcess().getProcessInstanceId();
        QueryManagementVariables processVariables = runtimeService.getProcessVariables(processInstanceId);
        String queryId = processVariables.getQueryId();
        List<String> roles = getUserRoleForQuery(caseData, coreCaseUserService, queryId);
        MultiPartyScenario multiPartyScenario = getMultiPartyScenario(caseData);

        System.out.println("MULTI PART SCENARIO IS " + multiPartyScenario);

        if (multiPartyScenario.equals(ONE_V_ONE) || multiPartyScenario.equals(TWO_V_ONE) || multiPartyScenario.equals(ONE_V_TWO_ONE_LEGAL_REP)) {
            // When 1v1, 2v1,  or 1v2 same solicitor, "other party" will either be applicant 1, or respondent 1
            String email = isOtherPartyApplicant(roles) ?
                caseData.getApplicantSolicitor1UserDetails().getEmail() :
                caseData.getRespondentSolicitor1EmailAddress();
            String legalOrgName = isOtherPartyApplicant(roles) ?
                getApplicantLegalOrganizationName(caseData, organisationService) :
                getLegalOrganizationNameForRespondent(caseData, true, organisationService);

            notificationService.sendMail(
                email,
                notificationsProperties.getNotifyOtherPartyQueryRaised(),
                addProperties(caseData, legalOrgName),
                String.format(REFERENCE_TEMPLATE, caseData.getLegacyCaseReference())
            );
        } else if (isOtherPartyApplicant(roles) && multiPartyScenario.equals(ONE_V_TWO_TWO_LEGAL_REP)) {
            // 1v2 different solicitor, and when "other party" is applicant 1.
            String applicantEmail = caseData.getApplicantSolicitor1UserDetails().getEmail();
            String legalOrgName = getApplicantLegalOrganizationName(caseData, organisationService);

            notificationService.sendMail(
                applicantEmail,
                notificationsProperties.getNotifyOtherPartyQueryRaised(),
                addProperties(caseData, legalOrgName),
                String.format(REFERENCE_TEMPLATE, caseData.getLegacyCaseReference())
            );
        } else if (!isOtherPartyApplicant(roles) && multiPartyScenario.equals(ONE_V_TWO_TWO_LEGAL_REP)) {
            // 1v2 different solicitor, and when "other party" is respondent 1 AND respondent 2.
            String emailRep1 = caseData.getRespondentSolicitor1EmailAddress();
            String emailRep2 = caseData.getRespondentSolicitor2EmailAddress();
            String legalOrgRep1Name = getLegalOrganizationNameForRespondent(caseData, true, organisationService);
            String legalOrgRep2Name = getLegalOrganizationNameForRespondent(caseData, false, organisationService);

            notificationService.sendMail(
                emailRep1,
                notificationsProperties.getNotifyOtherPartyQueryRaised(),
                addProperties(caseData, legalOrgRep1Name),
                String.format(REFERENCE_TEMPLATE, caseData.getLegacyCaseReference())
            );
            notificationService.sendMail(
                emailRep2,
                notificationsProperties.getNotifyOtherPartyQueryRaised(),
                addProperties(caseData, legalOrgRep2Name),
                String.format(REFERENCE_TEMPLATE, caseData.getLegacyCaseReference())
            );
        }

        return AboutToStartOrSubmitCallbackResponse.builder().build();
    }

    public Map<String, String> addProperties(CaseData caseData, String legalOrgName) {

        return new HashMap<>(Map.of(
            CLAIM_LEGAL_ORG_NAME_SPEC, legalOrgName,
            CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString(),
            PARTY_REFERENCES, buildPartiesReferencesEmailSubject(caseData),
            CASEMAN_REF, caseData.getLegacyCaseReference()
        ));
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

}
