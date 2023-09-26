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
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentStatusDetails;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentStatusType;
import uk.gov.hmcts.reform.civil.model.judgmentonline.PaymentPlanSelection;

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
public class RecordJudgmentCallbackHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = Collections.singletonList(RECORD_JUDGMENT);
    protected final ObjectMapper objectMapper;
    private static final String ERROR_MESSAGE_DATE_PAID_BY_MUST_BE_IN_FUTURE = "Date the judgment will be paid by must be in the future";
    private static final String ERROR_MESSAGE_DATE_FIRST_INSTALMENT_MUST_BE_IN_FUTURE = "Date of first instalment must be in the future";
    private static final String ERROR_MESSAGE_DATE_ORDER_MUST_BE_IN_PAST = "Date judge made the order must be in the past";

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
        var caseData = callbackParams.getCaseData();
        List<String> errors = new ArrayList<>();
        boolean isOrderMadeFutureDate =
            JudgmentsOnlineHelper.validateIfFutureDate(callbackParams.getCaseData().getJoOrderMadeDate());
        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = caseData.toBuilder();
        if (isOrderMadeFutureDate) {
            errors.add(ERROR_MESSAGE_DATE_ORDER_MUST_BE_IN_PAST);
        }
        if (callbackParams.getCaseData().getJoPaymentPlanSelection().equals(PaymentPlanSelection.PAY_BY_DATE)) {
            boolean isFutureDate =
                JudgmentsOnlineHelper.validateIfFutureDate(callbackParams.getCaseData().getJoPaymentToBeMadeByDate());
            if (!isFutureDate) {
                errors.add(ERROR_MESSAGE_DATE_PAID_BY_MUST_BE_IN_FUTURE);
            }
        } else if (callbackParams.getCaseData().getJoPaymentPlanSelection().equals(PaymentPlanSelection.PAY_IN_INSTALMENTS)) {
            boolean isFutureDate =
                JudgmentsOnlineHelper.validateIfFutureDate(callbackParams.getCaseData().getJoJudgmentInstalmentDetails().getFirstInstalmentDate());
            if (!isFutureDate) {
                errors.add(ERROR_MESSAGE_DATE_FIRST_INSTALMENT_MUST_BE_IN_FUTURE);
            }
        }
        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataBuilder.build().toMap(objectMapper))
            .errors(errors)
            .build();
    }

    private CallbackResponse clearAllFields(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        caseData.setJoOrderMadeDate(null);
        caseData.setJoJudgmentStatusDetails(null);
        caseData.setJoPaymentPlanSelection(null);
        caseData.setJoJudgmentInstalmentDetails(null);
        caseData.setJoJudgmentRecordReason(null);
        caseData.setJoAmountOrdered(null);
        caseData.setJoAmountCostOrdered(null);
        caseData.setJoIsRegisteredWithRTL(null);
        caseData.setJoSetAsideDate(null);
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

    private CallbackResponse saveJudgmentDetails(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        JudgmentStatusDetails judgmentStatusDetails = JudgmentStatusDetails.builder()
            .judgmentStatusTypes(JudgmentStatusType.ISSUED)
            .lastUpdatedDate(LocalDateTime.now()).build();
        if (caseData.getJoIsRegisteredWithRTL() == YesOrNo.YES) {
            judgmentStatusDetails.setJoRtlState(JudgmentsOnlineHelper.getRTLStatusBasedOnJudgementStatus(JudgmentStatusType.ISSUED));
        }
        caseData.setJoJudgmentStatusDetails(judgmentStatusDetails);
        caseData.setJoIsLiveJudgmentExists(YesOrNo.YES);
        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = caseData.toBuilder();
        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataBuilder.build().toMap(objectMapper))
            .build();
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }
}
