package uk.gov.hmcts.reform.civil.model.citizenui;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.citizenui.dto.ClaimantResponseOnCourtDecisionType;
import uk.gov.hmcts.reform.civil.model.citizenui.dto.RepaymentDecisionType;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClaimantLiPResponse {

    private DQExtraDetailsLip applicant1DQExtraDetails;
    private HearingSupportLip applicant1DQHearingSupportLip;
    private YesOrNo applicant1SignedSettlementAgreement;
    private ChooseHowToProceed applicant1ChoosesHowToProceed;
    private RepaymentDecisionType claimantCourtDecision;
    private ClaimantResponseOnCourtDecisionType claimantResponseOnCourtDecision;
    private String applicant1RejectedRepaymentReason;
    private LocalDate applicant1SuggestedImmediatePaymentDeadLine;
    private EvidenceConfirmDetails applicant1DQEvidenceConfirmDetails;

    @JsonIgnore
    public boolean hasApplicant1SignedSettlementAgreement() {
        return YesOrNo.YES.equals(applicant1SignedSettlementAgreement);
    }

    @JsonIgnore
    public boolean hasApplicant1RequestedCcj() {
        return ChooseHowToProceed.REQUEST_A_CCJ == applicant1ChoosesHowToProceed;
    }

    @JsonIgnore
    public boolean hasCourtDecisionInFavourOfClaimant() {
        return RepaymentDecisionType.IN_FAVOUR_OF_CLAIMANT == claimantCourtDecision;
    }

    @JsonIgnore
    public boolean hasCourtDecisionInFavourOfDefendant() {
        return RepaymentDecisionType.IN_FAVOUR_OF_DEFENDANT == claimantCourtDecision;
    }

    @JsonIgnore
    public boolean hasClaimantAcceptedCourtDecision() {
        return ClaimantResponseOnCourtDecisionType.ACCEPT_REPAYMENT_DATE == claimantResponseOnCourtDecision
            || ClaimantResponseOnCourtDecisionType.ACCEPT_REPAYMENT_PLAN == claimantResponseOnCourtDecision;
    }

    @JsonIgnore
    public boolean hasClaimantRejectedCourtDecision() {
        return ClaimantResponseOnCourtDecisionType.JUDGE_REPAYMENT_DATE == claimantResponseOnCourtDecision
            || ClaimantResponseOnCourtDecisionType.JUDGE_REPAYMENT_PLAN == claimantResponseOnCourtDecision;
    }

}
