package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import uk.gov.hmcts.reform.civil.service.DeadlinesCalculator;
import uk.gov.hmcts.reform.civil.service.JudgementService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.MID;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.REQUEST_JUDGEMENT_ADMISSION_SPEC;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE_TIME_AT;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.formatLocalDateTime;

@Service
@RequiredArgsConstructor
public class RequestJudgementByAdmissionForSpecCuiCallbackHandler extends CallbackHandler {

    private static final String NOT_VALID_DJ_BY_ADMISSION = "The Claim is not eligible for Request Judgment By Admission until %s.";
    private static final List<CaseEvent> EVENTS = Collections.singletonList(REQUEST_JUDGEMENT_ADMISSION_SPEC);
    private final ObjectMapper objectMapper;
    private final JudgementService judgementService;

    @Override
    protected Map<String, Callback> callbacks() {
        return new ImmutableMap.Builder<String, Callback>()
            .put(callbackKey(ABOUT_TO_START), this::validateDefaultJudgementEligibility)
            .put(callbackKey(MID, "set-up-ccj-amount-summary"), this::buildJudgmentAmountSummaryDetails)
            .put(callbackKey(MID, "validate-amount-paid"), this::validateAmountPaid)
            .put(callbackKey(ABOUT_TO_SUBMIT), this::emptySubmittedCallbackResponse)
            .put(callbackKey(SUBMITTED), this::emptySubmittedCallbackResponse)
            .build();
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse validateDefaultJudgementEligibility(CallbackParams callbackParams) {

        var caseData = callbackParams.getCaseData();
        CaseData.CaseDataBuilder caseDataBuilder = caseData.toBuilder();
        ArrayList<String> errors = new ArrayList<>();
        if (caseData.isNotPermitForRequestJudgementByAdmission()) {
            String formattedDeadline = formatLocalDateTime(
                caseData.getRespondent1ResponseDate().toLocalDate().plusDays(5)
                    .atTime(DeadlinesCalculator.END_OF_BUSINESS_DAY), DATE_TIME_AT);
            errors.add(format(NOT_VALID_DJ_BY_ADMISSION, formattedDeadline));
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .errors(errors)
            .data(errors.size() == 0
                      ? caseDataBuilder.build().toMap(objectMapper) : null)
            .build();
    }

    private CallbackResponse buildJudgmentAmountSummaryDetails(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        CaseData.CaseDataBuilder<?, ?> updatedCaseData = caseData.toBuilder();
        updatedCaseData.ccjPaymentDetails(judgementService.buildJudgmentAmountSummaryDetails(caseData));

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(updatedCaseData.build().toMap(objectMapper))
            .build();
    }

    private CallbackResponse validateAmountPaid(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        List<String> errors = judgementService.validateAmountPaid(caseData);
        if (errors.size() > 0) {
            return AboutToStartOrSubmitCallbackResponse.builder()
                .errors(errors)
                .build();
        }
        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseData.toMap(objectMapper))
            .build();
    }

}
