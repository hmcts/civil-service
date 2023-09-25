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
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentStatusType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;
import static java.util.Objects.nonNull;
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
    private static final String ERROR_MESSAGE_DATE_MUST_BE_IN_PAST = "Date must be in past";

    @Override
    protected Map<String, Callback> callbacks() {
        return new ImmutableMap.Builder<String, Callback>()
            .put(callbackKey(ABOUT_TO_START), this::resetPageData)
            .put(callbackKey(MID, "validate-payment-date"), this::validatePaymentDate)
            .put(callbackKey(ABOUT_TO_SUBMIT), this::saveJudgmentPaidInFullDetails)
            .put(callbackKey(SUBMITTED), this::buildConfirmation)
            .build();
    }

    private CallbackResponse resetPageData(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        caseData.setJoJudgmentPaidInFull(null);
        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = caseData.toBuilder();
        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataBuilder.build().toMap(objectMapper))
            .build();
    }

    private CallbackResponse buildConfirmation(CallbackParams callbackParams) {
        return SubmittedCallbackResponse.builder()
            .confirmationHeader(getHeader())
            .confirmationBody(getBody())
            .build();
    }

    private String getHeader() {
        return format("# Judgment marked as paid in full");
    }

    private String getBody() {
        return format("# Judgment marked as paid in full");
    }

    private CallbackResponse saveJudgmentPaidInFullDetails(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        List<String> errors = new ArrayList<>();

        if (nonNull(caseData.getJoOrderMadeDate())
            && nonNull(caseData.getJoJudgmentPaidInFull().getDateOfFullPaymentMade())) {
            boolean paidAfter30Days = JudgmentsOnlineHelper.checkIfDateDifferenceIsGreaterThan30Days(caseData.getJoOrderMadeDate(),
                                                                              caseData.getJoJudgmentPaidInFull().getDateOfFullPaymentMade());
            if (paidAfter30Days) {
                caseData.getJoJudgmentStatusDetails().setJudgmentStatusTypes(JudgmentStatusType.SATISFIED);
                if (caseData.getJoIsRegisteredWithRTL() == YesOrNo.YES) {
                    caseData.getJoJudgmentStatusDetails().setJoRtlState(JudgmentsOnlineHelper.getRTLStatusBasedOnJudgementStatus(JudgmentStatusType.SATISFIED));
                }
            } else {
                caseData.getJoJudgmentStatusDetails().setJudgmentStatusTypes(JudgmentStatusType.CANCELLED);
                if (caseData.getJoIsRegisteredWithRTL() == YesOrNo.YES) {
                    caseData.getJoJudgmentStatusDetails().setJoRtlState(JudgmentsOnlineHelper.getRTLStatusBasedOnJudgementStatus(JudgmentStatusType.CANCELLED));
                }
            }
            caseData.getJoJudgmentStatusDetails().setLastUpdatedDate(LocalDateTime.now());
            caseData.setJoIsLiveJudgmentExists(YesOrNo.NO);
        }

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
            errors.add(String.format(ERROR_MESSAGE_DATE_MUST_BE_IN_PAST, ERROR_MESSAGE_DATE_MUST_BE_IN_PAST));
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
