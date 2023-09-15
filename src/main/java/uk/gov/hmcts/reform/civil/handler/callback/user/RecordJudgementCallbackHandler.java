package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.helpers.judgmentsonline.JudgmentsOnlineHelper;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentStatusDetails;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentStatusType;
import uk.gov.hmcts.reform.civil.model.judgmentonline.RePaymentPlanSelection;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.MID;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.RECORD_JUDGMENT;

@Service
@RequiredArgsConstructor
public class RecordJudgementCallbackHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = Collections.singletonList(RECORD_JUDGMENT);
    protected final ObjectMapper objectMapper;

    @Override
    protected Map<String, Callback> callbacks() {
        return new ImmutableMap.Builder<String, Callback>()
            .put(callbackKey(ABOUT_TO_START), this::clearAllFields)
            .put(callbackKey(MID, "validate"), this::validateFields)
            .put(callbackKey(ABOUT_TO_SUBMIT), this::saveJudgementDetails)
            .put(callbackKey(SUBMITTED), this::buildConfirmation)
            .build();
    }

    private CallbackResponse clearAllFields(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        caseData.setJoOrderMadeDate(null);
        caseData.setJoJudgementStatusDetails(null);
        caseData.setJoRePaymentPlanSelection(null);
        caseData.setJoJudgementPaymentDetails(null);
        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = caseData.toBuilder();
        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataBuilder.build().toMap(objectMapper))
            .build();
    }

    private CallbackResponse buildConfirmation(CallbackParams callbackParams) {
        return SubmittedCallbackResponse.builder()
            .confirmationHeader("# Judgment recorded")
            .confirmationBody("The judgment has been recorded")
            .build();
    }

    private CallbackResponse saveJudgementDetails(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        JudgmentStatusDetails judgmentStatusDetails = JudgmentStatusDetails.builder()
            .judgmentStatusTypes(JudgmentStatusType.ISSUED)
            .lastUpdatedDate(LocalDateTime.now()).build();
        if (caseData.isJoIsRegisteredWithRTL()) {
            judgmentStatusDetails.setJoRtlState(JudgmentsOnlineHelper.getRTLStatusBasedOnJudgementStatus(JudgmentStatusType.ISSUED));
        }
        caseData.setJoJudgementStatusDetails(judgmentStatusDetails);
        caseData.setJoIsLiveJudgementExists(true);
        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = caseData.toBuilder();
        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataBuilder.build().toMap(objectMapper))
            .build();
    }

    private CallbackResponse validateFields(CallbackParams callbackParams) {
        List<String> errorList = new ArrayList<>();
        String errorMessage = JudgmentsOnlineHelper.validateIfFutureDates(callbackParams.getCaseData().getJoOrderMadeDate());
        if (errorMessage != null) {
            errorList.add(errorMessage);
        }
        if (callbackParams.getCaseData().getJoRePaymentPlanSelection().equals(RePaymentPlanSelection.PAY_IMMEDIATELY)) {
            String errorMsg =
                JudgmentsOnlineHelper.validateIfFutureDates(callbackParams.getCaseData().getJoJudgementPaymentDetails().getFirstInstallmentDate());
            if (errorMsg != null) {
                errorList.add(errorMsg);
            }
        }
        return AboutToStartOrSubmitCallbackResponse.builder()
            .errors(errorList)
            .build();
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }
}
