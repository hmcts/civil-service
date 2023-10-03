package uk.gov.hmcts.reform.civil.service.citizen.repaymentplan;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.citizenui.dto.RepaymentPlanDecisionDto;

@Service
@RequiredArgsConstructor
public class RepaymentPlanDecisionCalculator {

    private IncomeCalculator incomeCalculator;

    public RepaymentPlanDecisionDto calculateRepaymentDecision(CaseData caseData) {

    }


}
