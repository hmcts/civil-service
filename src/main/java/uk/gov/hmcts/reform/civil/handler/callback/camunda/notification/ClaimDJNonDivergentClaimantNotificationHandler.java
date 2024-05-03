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
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_DJ_NON_DIVERGENT_SPEC_CLAIMANT;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.getApplicantEmail;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.getApplicantLegalOrganizationName;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.getDefendantNameBasedOnCaseType;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.getAllPartyNames;

@Service
@RequiredArgsConstructor
public class ClaimDJNonDivergentClaimantNotificationHandler extends CallbackHandler
    implements NotificationData {

    private static final List<CaseEvent> EVENTS = List.of(NOTIFY_DJ_NON_DIVERGENT_SPEC_CLAIMANT);
    public static final String TASK_ID = "NotifyDJNonDivergentClaimant";

    private static final String REFERENCE_TEMPLATE = "dj-non-divergent-applicant-notification-%s";
    private static final String REFERENCE_TEMPLATE_LIP = "dj-non-divergent-applicant-notification-lip-%s";

    private final NotificationService notificationService;
    private final NotificationsProperties notificationsProperties;
    private final OrganisationService organisationService;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_SUBMIT),
            this::notifyClaimDefaultJudgementToClaimant
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

    private CallbackResponse notifyClaimDefaultJudgementToClaimant(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        boolean isApplicantLip = caseData.isApplicantLiP();
        String recipientEmail = getApplicantEmail(caseData, isApplicantLip);
        if (recipientEmail != null) {
            notificationService.sendMail(
                recipientEmail,
                getTemplate(isApplicantLip),
                getEmailProperties(caseData),
                getReferenceTemplate(caseData, isApplicantLip)
            );
        }

        return AboutToStartOrSubmitCallbackResponse.builder().build();
    }

    @Override
    public Map<String, String> addProperties(CaseData caseData) {
        return Map.of(
            CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference(),
            LEGAL_ORG_NAME, getApplicantLegalOrganizationName(caseData, organisationService),
            DEFENDANT_NAME_INTERIM,  getDefendantNameBasedOnCaseType(caseData)
        );
    }

    private String getTemplate(boolean isApplicantLip) {
        return isApplicantLip ? notificationsProperties.getNotifyUpdateTemplate()
            : notificationsProperties.getNotifyDJNonDivergentSpecClaimantTemplate();
    }

    private String getReferenceTemplate(CaseData caseData, boolean isApplicantLip) {
        return isApplicantLip ? String.format(REFERENCE_TEMPLATE_LIP, caseData.getLegacyCaseReference())
            : String.format(REFERENCE_TEMPLATE, caseData.getLegacyCaseReference());
    }

    private Map<String, String> getEmailProperties(CaseData caseData) {
        return caseData.isApplicantLiP() ? addPropertiesLip(caseData)
            : addProperties(caseData);
    }

    private Map<String, String> addPropertiesLip(CaseData caseData) {
        return Map.of(
            CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference(),
            CLAIMANT_V_DEFENDANT, getAllPartyNames(caseData),
            PARTY_NAME, caseData.getApplicant1().getPartyName()
        );
    }
}
