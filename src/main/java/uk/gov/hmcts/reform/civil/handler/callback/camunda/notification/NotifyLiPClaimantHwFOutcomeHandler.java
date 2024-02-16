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

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;

@Service
@RequiredArgsConstructor
public class NotifyLiPClaimantHwFOutcomeHandler extends CallbackHandler implements NotificationData {

    private static final List<CaseEvent> EVENTS = List.of(CaseEvent.NOTIFY_LIP_CLAIMANT_HWF_OUTCOME);
    public static final String TASK_ID_Applicant1 = "NotifyApplicant1ClaimSubmitted";

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
        System.out.println("--------------------- NOTIFICATION HWF HANDLER -----------");
        System.out.println(callbackParams.getCaseData());

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
}
