package uk.gov.hmcts.reform.civil.service.citizen.repaymentplan;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.citizenui.dto.RepaymentDecisionType;
import uk.gov.hmcts.reform.civil.model.repaymentplan.ClaimantProposedPlan;

@Service
@RequiredArgsConstructor
public class RepaymentPlanDecisionService {

    private final CaseDetailsConverter caseDetailsConverter;
    private final RepaymentPlanDecisionCalculator repaymentPlanDecisionCalculator;

    public RepaymentDecisionType getCalculatedDecision(final CaseDetails caseDetails, final ClaimantProposedPlan claimantProposedPlan) {
        CaseData caseData = caseDetailsConverter.toCaseData(caseDetails);
        return repaymentPlanDecisionCalculator.calculateRepaymentDecision(caseData, claimantProposedPlan);
    }
}
