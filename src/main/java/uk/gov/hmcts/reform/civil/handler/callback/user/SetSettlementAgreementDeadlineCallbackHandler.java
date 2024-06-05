package uk.gov.hmcts.reform.civil.handler.callback.user;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.SET_SETTLEMENT_AGREEMENT_DEADLINE;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;

import java.util.Collections;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.DeadlinesCalculator;

@Service
@RequiredArgsConstructor
public class SetSettlementAgreementDeadlineCallbackHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = Collections.singletonList(SET_SETTLEMENT_AGREEMENT_DEADLINE);
    private final DeadlinesCalculator deadlinesCalculator;
    private final ObjectMapper objectMapper;

    @Override
    protected Map<String, Callback> callbacks() {
        return new ImmutableMap.Builder<String, Callback>()
            .put(callbackKey(ABOUT_TO_SUBMIT), this::setSettlementAgreementDeadline)
            .build();
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse setSettlementAgreementDeadline(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        LocalDateTime currentDateTime = LocalDateTime.now();
        CaseData.CaseDataBuilder<?, ?> builder = caseData.toBuilder();
        if (caseData.isClaimantBilingual()) {
            builder.respondent1RespondToSettlementAgreementDeadline(getRespondToSettlementAgreementDeadline(caseData, currentDateTime));
        }
        CaseData updatedData = builder.build();
        AboutToStartOrSubmitCallbackResponse.AboutToStartOrSubmitCallbackResponseBuilder response =
            AboutToStartOrSubmitCallbackResponse.builder()
                .data(updatedData.toMap(objectMapper));

        return response.build();
    }

    private LocalDateTime getRespondToSettlementAgreementDeadline(CaseData caseData, LocalDateTime responseDate) {
        if (caseData.hasApplicant1SignedSettlementAgreement()) {
            return caseData.isCourtDecisionInClaimantFavourImmediateRePayment()
                    ? deadlinesCalculator.getRespondentToImmediateSettlementAgreement(responseDate)
                    : deadlinesCalculator.getRespondToSettlementAgreementDeadline(responseDate);
        }
        return null;
    }
}
