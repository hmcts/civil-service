package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.config.PinInPostConfiguration;
import uk.gov.hmcts.reform.civil.config.EmailTemplateFooterConfiguration;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.getPartyNameBasedOnType;

@Service
@RequiredArgsConstructor
public class NotifyClaimantClaimSubmitted extends CallbackHandler implements NotificationData {

    private static final List<CaseEvent> EVENTS = List.of(CaseEvent.NOTIFY_APPLICANT1_CLAIM_SUBMITTED);
    public static final String TASK_ID_Applicant1 = "NotifyApplicant1ClaimSubmitted";
    private static final String REFERENCE_TEMPLATE = "claim-submitted-notification-%s";
    private final NotificationService notificationService;
    private final FeatureToggleService toggleService;
    private final PinInPostConfiguration pipInPostConfiguration;
    private final NotificationsProperties notificationsProperties;
    private final Map<String, Callback> callBackMap = Map.of(
        callbackKey(ABOUT_TO_SUBMIT), this::notifyApplicantForClaimSubmitted
    );
    private final EmailTemplateFooterConfiguration emailTemplateFooterConfiguration;

    @Override
    protected Map<String, Callback> callbacks() {
        return callBackMap;
    }

    @Override
    public String camundaActivityId(CallbackParams callbackParams) {
        return TASK_ID_Applicant1;
    }

    private CallbackResponse notifyApplicantForClaimSubmitted(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();

        if (caseData.isLipvLipOneVOne()) {
            generateEmail(caseData);
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .build();
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    @Override
    public Map<String, String> addProperties(CaseData caseData) {
        return Map.of(
            CLAIMANT_NAME, getPartyNameBasedOnType(caseData.getApplicant1()),
            DEFENDANT_NAME, getPartyNameBasedOnType(caseData.getRespondent1()),
            FRONTEND_URL, pipInPostConfiguration.getCuiFrontEndUrl(),
            PHONE_AND_OPENING_TIMES, emailTemplateFooterConfiguration.getPhone() + "\n" + emailTemplateFooterConfiguration.getOpeningTime(),
            SMART_SURVEY_URL, emailTemplateFooterConfiguration.getSmartSurveyUrl()
        );
    }

    private String addTemplate(CaseData caseData) {
        boolean isWithHearingFee = caseData.getHelpWithFeesReferenceNumber() != null;
        if (caseData.isClaimantBilingual() && isWithHearingFee) {
            return notificationsProperties.getNotifyLiPClaimantClaimSubmittedAndHelpWithFeeBilingualTemplate();
        }
        if (caseData.isClaimantBilingual()) {
            return notificationsProperties.getNotifyLiPClaimantClaimSubmittedAndPayClaimFeeBilingualTemplate();
        }
        if (isWithHearingFee) {
            return notificationsProperties.getNotifyLiPClaimantClaimSubmittedAndHelpWithFeeTemplate();
        }
        return notificationsProperties.getNotifyLiPClaimantClaimSubmittedAndPayClaimFeeTemplate();

    }

    private void generateEmail(CaseData caseData) {
        if (Objects.nonNull(caseData.getApplicant1Email())) {
            notificationService.sendMail(
                caseData.getApplicant1Email(),
                addTemplate(caseData),
                addProperties(caseData),
                String.format(REFERENCE_TEMPLATE, caseData.getLegacyCaseReference())
            );
        }
    }
}
