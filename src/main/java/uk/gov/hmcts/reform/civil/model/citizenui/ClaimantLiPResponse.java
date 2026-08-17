package uk.gov.hmcts.reform.civil.model.citizenui;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.citizenui.dto.ClaimantResponseOnCourtDecisionType;
import uk.gov.hmcts.reform.civil.model.citizenui.dto.RepaymentDecisionType;

import java.time.LocalDate;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class ClaimantLiPResponse {

    @CCD(label = " ", searchable = false)
    private DQExtraDetailsLip applicant1DQExtraDetails;
    @CCD(label = " ", searchable = false)
    private HearingSupportLip applicant1DQHearingSupportLip;
    @CCD(label = " ", searchable = false, typeOverride = FieldType.YesOrNo)
    private YesOrNo applicant1SignedSettlementAgreement;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "ChooseHowToProceed"
    )
    private ChooseHowToProceed applicant1ChoosesHowToProceed;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "RepaymentDecisionType"
    )
    private RepaymentDecisionType claimantCourtDecision;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "ClaimantResponseOnCourtDecisionType"
    )
    private ClaimantResponseOnCourtDecisionType claimantResponseOnCourtDecision;
    @CCD(label = " ")
    private String applicant1RejectedRepaymentReason;
    @CCD(label = " ", searchable = false)
    private LocalDate applicant1SuggestedImmediatePaymentDeadLine;
    @CCD(label = " ", searchable = false)
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
