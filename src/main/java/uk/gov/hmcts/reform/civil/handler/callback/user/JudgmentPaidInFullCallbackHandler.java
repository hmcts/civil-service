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
import uk.gov.hmcts.reform.civil.helpers.judgmentsonline.JudgmentPaidInFullOnlineMapper;
import uk.gov.hmcts.reform.civil.helpers.judgmentsonline.JudgmentsOnlineHelper;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.MID;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.JUDGMENT_PAID_IN_FULL;

@Service
@RequiredArgsConstructor
public class JudgmentPaidInFullCallbackHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = Collections.singletonList(JUDGMENT_PAID_IN_FULL);
    protected final ObjectMapper objectMapper;
    private final JudgmentPaidInFullOnlineMapper paidInFullJudgmentOnlineMapper;
    private static final String ERROR_MESSAGE_DATE_MUST_BE_IN_PAST = "Date must be in past";

    @Override
    protected Map<String, Callback> callbacks() {
        return new ImmutableMap.Builder<String, Callback>()
            .put(callbackKey(ABOUT_TO_START), this::emptyCallbackResponse)
            .put(callbackKey(MID, "validate-payment-date"), this::validatePaymentDate)
            .put(callbackKey(ABOUT_TO_SUBMIT), this::saveJudgmentPaidInFullDetails)
            .put(callbackKey(SUBMITTED), this::buildConfirmation)
            .build();
    }

    private CallbackResponse buildConfirmation(CallbackParams callbackParams) {
        return SubmittedCallbackResponse.builder()
            .confirmationHeader(getHeader())
            .confirmationBody(getBody())
            .build();
    }

    private static final String JUDGMENT_MARKED_AS_PAID_IN_FULL_HEADER = "# Judgment marked as paid in full";

    private String getHeader() {
        return JUDGMENT_MARKED_AS_PAID_IN_FULL_HEADER;
    }

    private static final String JUDGMENT_MARKED_AS_PAID_IN_FULL_BODY = "# The judgment has been marked as paid in full";

    private String getBody() {
        return JUDGMENT_MARKED_AS_PAID_IN_FULL_BODY;
    }

    private CallbackResponse saveJudgmentPaidInFullDetails(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        caseData.setJoIsLiveJudgmentExists(YesOrNo.NO);
        paidInFullJudgmentOnlineMapper.moveToHistoricJudgment(caseData);
        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = caseData.toBuilder();
        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataBuilder.build().toMap(objectMapper))
            .build();
    }

    private CallbackResponse validatePaymentDate(CallbackParams callbackParams) {
        List<String> errors = new ArrayList<>();
        CaseData caseData = callbackParams.getCaseData();
        LocalDate dateOfPaymentMade = caseData.getJoJudgmentPaidInFull().getDateOfFullPaymentMade();

        if (JudgmentsOnlineHelper.validateIfFutureDate(dateOfPaymentMade)) {
            errors.add(ERROR_MESSAGE_DATE_MUST_BE_IN_PAST);
        }
        return AboutToStartOrSubmitCallbackResponse.builder()
            .errors(errors)
            .build();
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }
}
