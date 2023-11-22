package uk.gov.hmcts.reform.civil.model.docmosis;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.MappableObject;
import uk.gov.hmcts.reform.civil.service.citizen.repaymentplan.RepaymentPlanDecisionCalculator;
import uk.gov.hmcts.reform.civil.service.docmosis.utils.ClaimantResponseUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class InterlocutoryJudgementDocMapper implements MappableObject {

    private final RepaymentPlanDecisionCalculator repaymentPlanDecisionCalculator;
    public InterlocutoryJudgementDoc toInterlocutoryJudgementDoc(CaseData caseData) {
       return InterlocutoryJudgementDoc.builder()
            .claimNumber(caseData.getLegacyCaseReference())
            .claimantResponseSubmitDate(LocalDate.now())
            .claimantResponseSubmitTime(LocalDateTime.now())
            .disposableIncome(repaymentPlanDecisionCalculator.calculateDisposableIncome(caseData))
            .claimantResponseToDefendantAdmission(ClaimantResponseUtils.getClaimantResponseToDefendantAdmission(caseData))
            .courtDecisionRepaymentBy("Immediately")
            .build();

        private final String claimantResponseToDefendantAdmission;
        private final String claimantRequestRepaymentBy;
        private final String claimantRequestRepaymentLastDateBy;

        private final double disposableIncome;
        private final String courtDecisionRepaymentBy;
        private final String courtDecisionRepaymentLastDateBy;
    }

    private String getClaimantResponseToDefendantAdmission(CaseData caseData) {
        if(caseData.getRespondent1ClaimResponseTypeForSpec() == RespondentResponseTypeSpec.FULL_ADMISSION) {

        }else if(caseData.getRespondent1ClaimResponseTypeForSpec() == RespondentResponseTypeSpec.PART_ADMISSION) {

        }
    }

}
