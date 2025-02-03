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
import uk.gov.hmcts.reform.civil.utils.NotificationUtils;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_APPLICANT_FOR_RECORD_JUDGMENT;
import static java.util.Objects.nonNull;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.getAllPartyNames;

@Service
@RequiredArgsConstructor
public class RecordJudgmentDeterminationMeansApplicantNotificationHandler extends CallbackHandler
    implements NotificationData {

    private static final List<CaseEvent> EVENTS = List.of(NOTIFY_APPLICANT_FOR_RECORD_JUDGMENT);
    public static final String TASK_ID = "RecordJudgmentApplicantSolicitor1";
    private static final String REFERENCE_TEMPLATE = "record-judgment-determination-means-notification-%s";

    private final NotificationService notificationService;
    private final NotificationsProperties notificationsProperties;
    private final OrganisationService organisationService;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_SUBMIT), this::notifyApplicantSolicitorForRecordJudgmentDeterminationOfMeans
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

    private CallbackResponse notifyApplicantSolicitorForRecordJudgmentDeterminationOfMeans(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        boolean isApplicantLip = caseData.isApplicantNotRepresented();
        String emailTemplateID;
        if (isApplicantLip) {
            if (caseData.isClaimantBilingual()) {
                emailTemplateID = notificationsProperties.getNotifyLipUpdateTemplateBilingual();
            } else {
                emailTemplateID = notificationsProperties.getNotifyLipUpdateTemplate();
            }
        } else {
            emailTemplateID = notificationsProperties.getNotifyLrRecordJudgmentDeterminationMeansTemplate();
        }

        String recipient = isApplicantLip ? caseData.getApplicant1Email() :
            caseData.getApplicantSolicitor1UserDetails().getEmail();

        if (nonNull(recipient)) {
            notificationService.sendMail(
                recipient,
                emailTemplateID,
                isApplicantLip ? addPropertiesLip(caseData) : addProperties(caseData),
                String.format(REFERENCE_TEMPLATE, caseData.getLegacyCaseReference())
            );
        }

        return AboutToStartOrSubmitCallbackResponse.builder().build();
    }

    @Override
    public Map<String, String> addProperties(CaseData caseData) {
        return Map.of(
            CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference(),
            LEGAL_ORG_NAME, NotificationUtils.getApplicantLegalOrganizationName(caseData, organisationService),
            DEFENDANT_NAME, NotificationUtils.getDefendantNameBasedOnCaseType(caseData)
        );
    }

    private Map<String, String> addPropertiesLip(CaseData caseData) {
        return Map.of(
            CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference(),
            PARTY_NAME, caseData.getApplicant1().getPartyName(),
            CLAIMANT_V_DEFENDANT, getAllPartyNames(caseData)
        );
    }
}
