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
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.helpers.judgmentsonline.JudgmentsOnlineHelper;
import uk.gov.hmcts.reform.civil.helpers.judgmentsonline.RecordJudgmentOnlineMapper;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentRecordedReason;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.MID;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.RECORD_JUDGMENT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.RECORD_JUDGMENT_NOTIFICATION;

@Service
@RequiredArgsConstructor
public class RecordJudgmentCallbackHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = Collections.singletonList(RECORD_JUDGMENT);
    protected final ObjectMapper objectMapper;
    private final RecordJudgmentOnlineMapper recordJudgmentOnlineMapper;

    @Override
    protected Map<String, Callback> callbacks() {
        return new ImmutableMap.Builder<String, Callback>()
            .put(callbackKey(ABOUT_TO_START), this::clearAllFields)
            .put(callbackKey(MID, "validateDates"), this::validateDates)
            .put(callbackKey(ABOUT_TO_SUBMIT), this::saveJudgmentDetails)
            .put(callbackKey(SUBMITTED), this::buildConfirmation)
            .build();
    }

    private CallbackResponse validateDates(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = caseData.toBuilder();
        List<String> errors = JudgmentsOnlineHelper.validateMidCallbackData(caseData);
        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataBuilder.build().toMap(objectMapper))
            .errors(errors)
            .build();
    }

    private CallbackResponse clearAllFields(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        // If first time (IsLiveJudgmentExists = null) do not clear them
        if (caseData.getJoIsLiveJudgmentExists() != null) {
            caseData.setJoOrderMadeDate(null);
            caseData.setJoPaymentPlan(null);
            caseData.setJoInstalmentDetails(null);
            caseData.setJoJudgmentRecordReason(null);
            caseData.setJoAmountOrdered(null);
            caseData.setJoAmountCostOrdered(null);
            caseData.setJoIsRegisteredWithRTL(null);
            caseData.setJoIssuedDate(null);
            CaseData.CaseDataBuilder<?, ?> caseDataBuilder = caseData.toBuilder();
            return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataBuilder.build().toMap(objectMapper))
            .build();
        }
        return AboutToStartOrSubmitCallbackResponse.builder().build();
    }

    private CallbackResponse buildConfirmation(CallbackParams callbackParams) {
        return SubmittedCallbackResponse.builder()
            .confirmationHeader("# Judgment recorded")
            .confirmationBody("The judgment has been recorded")
            .build();
    }

    private CallbackResponse saveJudgmentDetails(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        caseData.setJoIsLiveJudgmentExists(YesOrNo.YES);
        caseData.setJoSetAsideOrderDate(null);
        caseData.setJoSetAsideApplicationDate(null);
        caseData.setJoSetAsideDefenceReceivedDate(null);
        caseData.setJoSetAsideOrderType(null);
        caseData.setJoSetAsideReason(null);
        caseData.setJoSetAsideJudgmentErrorText(null);
        caseData.setJoJudgmentPaidInFull(null);
        if (caseData.getJoIsRegisteredWithRTL() == YesOrNo.YES) {
            caseData.setJoIssuedDate(caseData.getJoOrderMadeDate());
        }
        caseData.setActiveJudgment(recordJudgmentOnlineMapper.addUpdateActiveJudgment(caseData));
        caseData.setJoRepaymentSummaryObject(JudgmentsOnlineHelper.calculateRepaymentBreakdownSummary(caseData.getActiveJudgment()));

        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = caseData.toBuilder();
        if (caseData.getJoJudgmentRecordReason() == JudgmentRecordedReason.DETERMINATION_OF_MEANS) {
            caseDataBuilder.businessProcess(BusinessProcess.ready(RECORD_JUDGMENT_NOTIFICATION));
        }
        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataBuilder.build().toMap(objectMapper))
            .build();
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

}
