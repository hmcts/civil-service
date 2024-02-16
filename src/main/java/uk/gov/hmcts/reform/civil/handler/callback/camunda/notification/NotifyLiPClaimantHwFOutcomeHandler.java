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
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;

@Service
@RequiredArgsConstructor
public class NotifyLiPClaimantHwFOutcomeHandler extends CallbackHandler implements NotificationData {

    private static final List<CaseEvent> EVENTS = List.of(CaseEvent.NOTIFY_LIP_CLAIMANT_HWF_OUTCOME);
    public static final String TASK_ID = "NotifyClaimantHwFOutcome";
    private static final String REFERENCE_TEMPLATE = "hwf-outcome-notification-%s";
    private final NotificationService notificationService;
    private final NotificationsProperties notificationsProperties;
    private final FeatureToggleService toggleService;

    private final Map<String, Callback> callBackMap = Map.of(
        callbackKey(ABOUT_TO_SUBMIT), this::notifyApplicantForClaimSubmitted
    );

    @Override
    protected Map<String, Callback> callbacks() {
        return callBackMap;
    }

    @Override
    public String camundaActivityId(CallbackParams callbackParams) {
        return TASK_ID;
    }

    private CallbackResponse notifyApplicantForClaimSubmitted(CallbackParams callbackParams) {
       CaseData caseData = callbackParams.getCaseData();

        if (caseData.isLipvLipOneVOne() && toggleService.isLipVLipEnabled()) {
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
        return null;
    }

    private String addTemplate(CaseData caseData) {
        CaseEvent hwfEvent = null;
        if(caseData.isHWFTypeHearing()){
            hwfEvent = caseData.getHearingHwfDetails().getHwfCaseEvent();
        }
        if(caseData.isHWFTypeClaimIssued()){
            hwfEvent = caseData.getClaimIssuedHwfDetails().getHwfCaseEvent();
        }
        switch(hwfEvent){
            case NO_REMISSION_HWF:
                return notificationsProperties.getNotifyApplicantForHwFNoRemission();
            default:
                return null;
        }

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
