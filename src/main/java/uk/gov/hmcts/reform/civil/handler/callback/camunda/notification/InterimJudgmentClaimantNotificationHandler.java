package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.config.properties.notification.NotificationsProperties;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.NotificationService;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_INTERIM_JUDGMENT_CLAIMANT;

@Service
@RequiredArgsConstructor
public class InterimJudgmentClaimantNotificationHandler extends CallbackHandler implements NotificationData {

    private final NotificationService notificationService;
    private final NotificationsProperties notificationsProperties;
    private final ObjectMapper objectMapper;
    private static final String CLAIM_NUMBER = "Claim number";
    private static final String CLAIMANT_NAME = "Claimant Name";
    private static final String CLAIMANT2_NAME = "Claimant2 Name";
    private static final String DEFENDANT_NAME = "Defendant Name";
    private static final String DEFENDANT2_NAME = "Defendant2 Name";
    private static final List<CaseEvent> EVENTS = List.of(NOTIFY_INTERIM_JUDGMENT_CLAIMANT);
    private static final String REFERENCE_TEMPLATE_APPROVAL = "interim-judgment-approval-notification-%s";
    private static final String REFERENCE_TEMPLATE_REQUEST = "interim-judgment-requested-notification-%s";
    private static final String TASK_ID= "NotifyInterimJudgmentClaimant";

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_SUBMIT), this::notifyAllPartiesInterimJudgmentApproved
        );
    }

    @Override
    public String camundaActivityId(CallbackParams callbackParams) {
        return TASK_ID;
    }

    private CallbackResponse notifyAllPartiesInterimJudgmentApproved(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        if(caseData.getAddApplicant2().equals(YesOrNo.YES)){
            notificationService.sendMail(
                caseData.getApplicantSolicitor1UserDetails().getEmail(),
                notificationsProperties.getInterimJudgmentApprovalClaimant(),
                addProperties2Claimants(caseData),
                String.format(REFERENCE_TEMPLATE_APPROVAL, caseData.getLegacyCaseReference())
            );
        }else if(caseData.getAddRespondent2().equals(YesOrNo.YES)){
            notificationService.sendMail(
                caseData.getApplicantSolicitor1UserDetails().getEmail(),
                notificationsProperties.getInterimJudgmentRequestedClaimant(),
                addProperties2Defendants(caseData),
                String.format(REFERENCE_TEMPLATE_REQUEST
                    , caseData.getLegacyCaseReference())
            );
        }else{
            notificationService.sendMail(
                caseData.getApplicantSolicitor1UserDetails().getEmail(),
                notificationsProperties.getInterimJudgmentApprovalClaimant(),
                addProperties(caseData),
                String.format(REFERENCE_TEMPLATE_APPROVAL, caseData.getLegacyCaseReference()));
        }
        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseData.toMap(objectMapper))
            .build();
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    @Override
    public Map<String, String> addProperties(CaseData caseData) {
        return Map.of(
            CLAIMANT_NAME, caseData.getApplicant1().getPartyName(),
            CLAIM_NUMBER, caseData.getLegacyCaseReference(),
            DEFENDANT_NAME, caseData.getRespondent1DetailsForClaimDetailsTab().getPartyName()
        );
    }

    public Map<String, String> addProperties2Claimants(CaseData caseData) {
        return Map.of(
            CLAIMANT_NAME, caseData.getApplicant1().getPartyName(),
            CLAIMANT2_NAME, caseData.getApplicant2().getPartyName(),
            CLAIM_NUMBER, caseData.getLegacyCaseReference(),
            DEFENDANT_NAME, caseData.getRespondent1DetailsForClaimDetailsTab().getPartyName()
        );
    }

    public Map<String, String> addProperties2Defendants(CaseData caseData) {
        return Map.of(
            CLAIMANT_NAME, caseData.getApplicant1().getPartyName(),
            CLAIM_NUMBER, caseData.getLegacyCaseReference(),
            DEFENDANT_NAME, caseData.getRespondent1DetailsForClaimDetailsTab().getPartyName(),
            DEFENDANT2_NAME, caseData.getRespondent2DetailsForClaimDetailsTab().getPartyName()
        );
    }
}
