package uk.gov.hmcts.reform.civil.handler.callback.camunda.cosc;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
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
import uk.gov.hmcts.reform.civil.model.citizenui.CertOfSC;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.genapplication.GeneralApplication;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentDetails;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CHECK_AND_MARK_PAID_IN_FULL;
import static uk.gov.hmcts.reform.civil.enums.dq.GeneralApplicationTypes.CONFIRM_CCJ_DEBT_PAID;

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
            LocalDate certSCPaymentDate = caseData.getGeneralApplications().stream()
                .filter(app -> app.getValue().getGeneralAppType().getTypes().contains(CONFIRM_CCJ_DEBT_PAID))
                .findFirst()
                .map(Element::getValue)
                .map(GeneralApplication::getCertOfSC)
                .map(CertOfSC::getDefendantFinalPaymentDate)
                .orElseThrow(() -> new IllegalArgumentException("Cosc was not found."));

            runtimeService.setVariable(caseData.getBusinessProcess().getProcessInstanceId(), SEND_DETAILS_CJES, true);
            caseData.setJoIsLiveJudgmentExists(YesOrNo.YES);
            caseData.setActiveJudgment(paidInFullJudgmentOnlineMapper.addUpdateActiveJudgment(
                caseData,
                certSCPaymentDate
            ));
            caseData.setJoRepaymentSummaryObject(JudgmentsOnlineHelper.calculateRepaymentBreakdownSummary(caseData.getActiveJudgment()));
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseData.toMap(objectMapper))
            .build();
    }
}
