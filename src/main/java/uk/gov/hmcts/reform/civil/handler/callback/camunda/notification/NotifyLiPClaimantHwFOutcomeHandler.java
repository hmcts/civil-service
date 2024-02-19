package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import com.google.common.collect.ImmutableMap;
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
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NO_REMISSION_HWF;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.getPartyNameBasedOnType;

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
        callbackKey(ABOUT_TO_SUBMIT), this::notifyApplicantForHwFOutcome
    );
    private final Map<CaseEvent, String> templateMap = ImmutableMap.of(
        NO_REMISSION_HWF, notificationsProperties.getNotifyApplicantForHwfNoRemission()
    );

    @Override
    protected Map<String, Callback> callbacks() {
        return callBackMap;
    }

    @Override
    public String camundaActivityId(CallbackParams callbackParams) {
        return TASK_ID;
    }

    private CallbackResponse notifyApplicantForHwFOutcome(CallbackParams callbackParams) {
       CaseData caseData = callbackParams.getCaseData();

        if (caseData.isLipvLipOneVOne() && toggleService.isLipVLipEnabled()) {
            sendEmail(caseData);
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
        switch(caseData.getHwFEvent()){
            case NO_REMISSION_HWF:
                return getNoRemissionProperties(caseData);
            default:
                return null;
        }
    }

    private Map<String, String> getNoRemissionProperties(CaseData caseData){
        return Map.of(
            CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference(),
            CLAIMANT_NAME, getPartyNameBasedOnType(caseData.getApplicant1()),
            REASONS, getHwFNoRemissionReason(caseData),
            TYPE_OF_FEE, caseData.getHwfFeeType().getLabel(),
            HWF_REFERENCE_NUMBER, caseData.getHwFReferenceNumber().toString(),
            AMOUNT, caseData.getHwFFeeAmount().toString()
        );
    }

    private void sendEmail(CaseData caseData) {
        if (Objects.nonNull(caseData.getApplicant1Email())) {
            notificationService.sendMail(
                caseData.getApplicant1Email(),
                templateMap.get(caseData.getHwFEvent()),
                addProperties(caseData),
                String.format(REFERENCE_TEMPLATE, caseData.getLegacyCaseReference())
            );
        }
    }

    private String getHwFNoRemissionReason(CaseData caseData) {
        if(caseData.isHWFTypeHearing()){
            return caseData.getHearingHwfDetails().getNoRemissionDetailsSummary().getLabel();
        }
        if(caseData.isHWFTypeClaimIssued()){
            return caseData.getClaimIssuedHwfDetails().getNoRemissionDetailsSummary().getLabel();
        }
        return "";
    }
}
