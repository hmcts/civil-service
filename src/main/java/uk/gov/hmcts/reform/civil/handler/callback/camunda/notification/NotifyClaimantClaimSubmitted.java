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
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;

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
    private final PinInPostConfiguration pipInPostConfiguration;
    private final NotificationsProperties notificationsProperties;
    private final Map<String, Callback> callBackMap = Map.of(
        callbackKey(ABOUT_TO_SUBMIT), this::notifyApplicantForClaimSubmitted
    );

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

        generateEmail(caseData);

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
            FRONTEND_URL, pipInPostConfiguration.getCuiFrontEndUrl()
        );
    }

    private void generateEmail(CaseData caseData) {
        if (Objects.nonNull(caseData.getApplicant1Email())) {
            notificationService.sendMail(
                caseData.getApplicant1Email(),
                Objects.isNull(caseData.getHelpWithFeesReferenceNumber())
                    ? notificationsProperties.getNotifyLiPClaimantClaimSubmittedAndPayClaimFeeTemplate()
                    : notificationsProperties.getNotifyLiPClaimantClaimSubmittedAndHelpWithFeeTemplate(),
                addProperties(caseData),
                String.format(REFERENCE_TEMPLATE, caseData.getLegacyCaseReference())
            );
        }
    }
}
