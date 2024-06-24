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

import static java.util.Objects.nonNull;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_CLAIMANT_JUDGMENT_VARIED_DETERMINATION_OF_MEANS;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.getApplicantLegalOrganizationName;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.getDefendantNameBasedOnCaseType;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.getAllPartyNames;

@Service
@RequiredArgsConstructor
public class NotifyClaimantJudgmentVariedDeterminationOfMeansNotificationHandler extends CallbackHandler
    implements NotificationData {

    private static final List<CaseEvent> EVENTS = List.of(NOTIFY_CLAIMANT_JUDGMENT_VARIED_DETERMINATION_OF_MEANS);
    public static final String TASK_ID = "NotifyClaimantJudgmentVariedDeterminationOfMeans";
    private static final String REFERENCE_TEMPLATE =
        "claimant-judgment-varied-determination-of-means-%s";

    private final NotificationService notificationService;
    private final NotificationsProperties notificationsProperties;
    private final OrganisationService organisationService;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_SUBMIT),
            this::notifyClaimantJudgmentVariedDeterminationOfMeans
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

    private CallbackResponse notifyClaimantJudgmentVariedDeterminationOfMeans(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        String recipient = caseData.isApplicantLiP() ? caseData.getApplicant1Email() :
            caseData.getApplicantSolicitor1UserDetails().getEmail();

        if (nonNull(recipient)) {
            notificationService.sendMail(
                recipient,
                caseData.isApplicantLiP() ? getLIPTemplate(caseData) : getTemplate(),
                caseData.isApplicantLiP() ? addLIPProperties(caseData) : addProperties(caseData),
                getReferenceTemplate(caseData)
            );
        }

        return AboutToStartOrSubmitCallbackResponse.builder().build();
    }

    @Override
    public Map<String, String> addProperties(CaseData caseData) {
        return Map.of(
                CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference(),
                LEGAL_ORG_NAME, getApplicantLegalOrganizationName(caseData, organisationService),
                DEFENDANT_NAME,  getDefendantNameBasedOnCaseType(caseData)
            );
    }

    public Map<String, String> addLIPProperties(CaseData caseData) {
        return Map.of(
            CLAIMANT_V_DEFENDANT, getAllPartyNames(caseData),
            CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference(),
            PARTY_NAME, caseData.getApplicant1().getPartyName()
        );
    }

    private String getTemplate() {
        return notificationsProperties.getNotifyClaimantJudgmentVariedDeterminationOfMeansTemplate();
    }

    private String getLIPTemplate(CaseData caseData) {
        if (caseData.isClaimantBilingual()) {
            return notificationsProperties.getNotifyLipUpdateTemplateBilingual();
        } else {
            return notificationsProperties.getNotifyLipUpdateTemplate();
        }
    }

    private String getReferenceTemplate(CaseData caseData) {
        return String.format(REFERENCE_TEMPLATE, caseData.getLegacyCaseReference());
    }

}
