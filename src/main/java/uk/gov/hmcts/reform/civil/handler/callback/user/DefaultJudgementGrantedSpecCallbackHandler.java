package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.helpers.judgmentsonline.JudgmentsOnlineHelper;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentDetails;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentRTLStatus;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentState;
import uk.gov.hmcts.reform.civil.utils.InterestCalculator;
import uk.gov.hmcts.reform.civil.utils.MonetaryConversions;

import java.math.BigInteger;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static java.lang.String.format;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.DEFAULT_JUDGEMENT_GRANTED_SPEC;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.DEFAULT_JUDGEMENT_NON_DIVERGENT_SPEC;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;

@Service
@RequiredArgsConstructor
@Slf4j
public class DefaultJudgementGrantedSpecCallbackHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = List.of(DEFAULT_JUDGEMENT_GRANTED_SPEC);

    private final ObjectMapper objectMapper;

    private final InterestCalculator interestCalculator;

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    @Override
    protected Map<String, Callback> callbacks() {
        return new ImmutableMap.Builder<String, Callback>()
            .put(callbackKey(ABOUT_TO_SUBMIT), this::handleAboutToSubmit)
            .put(callbackKey(SUBMITTED), this::emptySubmittedCallbackResponse)
            .build();
    }

    private CallbackResponse handleAboutToSubmit(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        List<String> errors = checkCaseEligibility(caseData);

        if (!errors.isEmpty()) {
            return AboutToStartOrSubmitCallbackResponse.builder()
                .errors(errors)
                .build();
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(updateCaseData(caseData).toMap(objectMapper))
            .state(CaseState.All_FINAL_ORDERS_ISSUED.name())
            .build();
    }

    private List<String> checkCaseEligibility(CaseData caseData) {
        List<String> errors = new ArrayList<>();

        if (!Objects.equals(caseData.getCcdState(), CaseState.JUDGMENT_REQUESTED)) {
            errors.add(format(
                "Event DEFAULT_JUDGEMENT_GRANTED_SPEC: Cannot grant default judgment for case in state %s for caseId %d",
                caseData.getCcdState(),
                caseData.getCcdCaseReference()
            ));
        }

        if (caseData.getActiveJudgment() == null) {
            errors.add(format(
                "Event DEFAULT_JUDGEMENT_GRANTED_SPEC: Active judgment is null for caseId %d",
                caseData.getCcdCaseReference()
            ));
        }

        if (!errors.isEmpty()) {
            errors.forEach(log::error);
        }

        return errors;
    }

    private CaseData updateCaseData(CaseData caseData) {
        BigInteger orderAmount = MonetaryConversions.poundsToPennies(JudgmentsOnlineHelper.getDebtAmount(caseData, interestCalculator));
        BigInteger costs = MonetaryConversions.poundsToPennies(JudgmentsOnlineHelper.getFixedCostsOfJudgmentForDJ(caseData));
        BigInteger claimFee = MonetaryConversions.poundsToPennies(JudgmentsOnlineHelper.getClaimFeeOfJudgmentForDJ(caseData));
        JudgmentDetails activeJudgment = caseData.getActiveJudgment();
        activeJudgment.setState(JudgmentState.ISSUED)
            .setIssueDate(LocalDate.now())
            .setRtlState(JudgmentRTLStatus.ISSUED.getRtlState())
            .setIsRegisterWithRTL(YES)
            .setOrderedAmount(orderAmount.toString())
            .setClaimFeeAmount(claimFee.toString())
            .setCosts(costs.toString())
            .setTotalAmount(orderAmount.add(costs).add(claimFee).toString());

        caseData.setJoIsLiveJudgmentExists(YES);
        caseData.setJoState(JudgmentState.ISSUED);
        String repaymentSummaryObject = JudgmentsOnlineHelper.calculateRepaymentBreakdownSummaryWithoutClaimInterest(
            activeJudgment,
            true
        );
        caseData.setJoRepaymentSummaryObject(repaymentSummaryObject);
        caseData.setRepaymentSummaryObject(repaymentSummaryObject);
        caseData.setTotalInterest(interestCalculator.calculateInterest(caseData));
        caseData.setBusinessProcess(BusinessProcess.ready(DEFAULT_JUDGEMENT_NON_DIVERGENT_SPEC));

        return caseData;
    }
}
