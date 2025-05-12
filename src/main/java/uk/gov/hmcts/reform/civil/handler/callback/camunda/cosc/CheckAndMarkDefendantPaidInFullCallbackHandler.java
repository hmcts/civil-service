package uk.gov.hmcts.reform.civil.handler.callback.camunda.cosc;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.RuntimeService;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.helpers.judgmentsonline.JudgmentPaidInFullOnlineMapper;
import uk.gov.hmcts.reform.civil.helpers.judgmentsonline.JudgmentsOnlineHelper;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentDetails;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentState;
import uk.gov.hmcts.reform.civil.utils.InterestCalculator;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CHECK_AND_MARK_PAID_IN_FULL;
import static uk.gov.hmcts.reform.civil.enums.cosc.CoscRPAStatus.CANCELLED;
import static uk.gov.hmcts.reform.civil.enums.cosc.CoscRPAStatus.SATISFIED;

@Slf4j
@Service
@RequiredArgsConstructor
public class CheckAndMarkDefendantPaidInFullCallbackHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = List.of(
        CHECK_AND_MARK_PAID_IN_FULL
    );
    private static final String TASK_ID = "CheckAndMarkDefendantPaidInFull";
    private static final String SEND_DETAILS_CJES = "sendDetailsToCJES";

    private final JudgmentPaidInFullOnlineMapper paidInFullJudgmentOnlineMapper;
    private final RuntimeService runtimeService;
    private final ObjectMapper objectMapper;
    private final InterestCalculator interestCalculator;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(callbackKey(ABOUT_TO_SUBMIT), this::checkAndMarkDefendantPaidInFull);
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    @Override
    public String camundaActivityId(CallbackParams callbackParams) {
        return TASK_ID;
    }

    private CallbackResponse checkAndMarkDefendantPaidInFull(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();

        LocalDate judgementPaidDate =
            ofNullable(caseData.getActiveJudgment())
                .map(JudgmentDetails::getFullyPaymentMadeDate)
                .orElse(null);

        if (nonNull(judgementPaidDate)) {
            runtimeService.setVariable(caseData.getBusinessProcess().getProcessInstanceId(), SEND_DETAILS_CJES, false);
        } else {
            runtimeService.setVariable(caseData.getBusinessProcess().getProcessInstanceId(), SEND_DETAILS_CJES, true);
            caseData.setJoIsLiveJudgmentExists(YesOrNo.YES);
            caseData.setActiveJudgment(paidInFullJudgmentOnlineMapper.addUpdateActiveJudgment(
                caseData,
                caseData.getCertOfSC().getDefendantFinalPaymentDate()
            ));
            BigDecimal interest = interestCalculator.calculateInterest(caseData);
            log.info("--- Checking judgment on CheckAndMarkDefendantPaidInFullCallbackHandler");
            caseData.setJoRepaymentSummaryObject(JudgmentsOnlineHelper.calculateRepaymentBreakdownSummary(caseData.getActiveJudgment(), interest));
            caseData.setJoDefendantMarkedPaidInFullIssueDate(LocalDateTime.now());
        }
        caseData.setJoCoscRpaStatus(JudgmentState.CANCELLED.equals(caseData.getActiveJudgment().getState()) ? CANCELLED : SATISFIED);

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseData.toMap(objectMapper))
            .build();
    }
}
