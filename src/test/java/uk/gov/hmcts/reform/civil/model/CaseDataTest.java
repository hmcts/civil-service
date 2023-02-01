package uk.gov.hmcts.reform.civil.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.enums.*;
import uk.gov.hmcts.reform.civil.model.dq.Respondent1DQ;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class CaseDataTest {

    @Test
    public void applicant1Proceed_when1v1() {
        CaseData caseData = CaseData.builder()
            .applicant1ProceedWithClaim(YesOrNo.YES)
            .build();
        Assertions.assertEquals(YesOrNo.YES, caseData.getApplicant1ProceedsWithClaimSpec());
    }

    @Test
    public void applicant1Proceed_when2v1() {
        CaseData caseData = CaseData.builder()
            .applicant1ProceedWithClaimSpec2v1(YesOrNo.YES)
            .build();
        Assertions.assertEquals(YesOrNo.YES, caseData.getApplicant1ProceedsWithClaimSpec());
    }

    @Test
    public void defendantResponseStatus_noResponse() {
        CaseData caseData = CaseData.builder()
            .respondent1ClaimResponseType(null)
            .build();

        DefendantResponseStatus status = caseData.getDefendantResponseStatus();

        Assertions.assertEquals(status, DefendantResponseStatus.NO_RESPONSE);
    }

    @Test
    public void defendantResponseStatus_eligibleForCCJ() {
        CaseData caseData = CaseData
            .builder()
            .defenceAdmitPartPaymentTimeRouteRequired(RespondentResponsePartAdmissionPaymentTimeLRspec.BY_SET_DATE).build();

        DefendantResponseStatus status = caseData.getDefendantResponseStatus();

        Assertions.assertEquals(status, DefendantResponseStatus.ELIGIBLE_FOR_CCJ);
    }

    @Test
    public void defendantResponseStatus_claimantAcceptedStatesPaid() {
        Respondent1DQ respondent1DQ = new Respondent1DQ();
        respondent1DQ.setResponseClaimCourtLocationRequired(YesOrNo.YES);
        CaseData caseData = CaseData.builder()
            .respondent1ClaimResponsePaymentAdmissionForSpec(RespondentResponseTypeSpecPaidStatus.PAID_FULL_OR_MORE_THAN_CLAIMED_AMOUNT)
            .respondent1DQ(respondent1DQ)
            .build();

        DefendantResponseStatus status = caseData.getDefendantResponseStatus();

        Assertions.assertEquals(status, DefendantResponseStatus.CLAIMANT_ACCEPTED_STATES_PAID);
    }

    @Test
    public void defendantResponseStatus_redeterminationByJudge() {
        Respondent1DQ respondent1DQ = new Respondent1DQ();
        respondent1DQ.setResponseClaimCourtLocationRequired(YesOrNo.YES);
        CaseData caseData = CaseData.builder()
            .applicant1ProceedWithClaim(YesOrNo.YES)
            .respondent1DQ(respondent1DQ)
            .build();

        DefendantResponseStatus status = caseData.getDefendantResponseStatus();

        Assertions.assertEquals(status, DefendantResponseStatus.REDETERMINATION_BY_JUDGE);
    }

    @Test
    public void defendantResponseStatus_paidInFullCCJCanceled() {
        Respondent1DQ respondent1DQ = new Respondent1DQ();
        respondent1DQ.setResponseClaimCourtLocationRequired(YesOrNo.YES);
        RespondToClaim respondToClaim = RespondToClaim
            .builder()
            .whenWasThisAmountPaid(LocalDate.of(2022, 10, 22))
            .build();
        CaseData caseData = CaseData.builder()
            .respondToClaim(respondToClaim)
            .respondent1DQ(respondent1DQ)
            .hearingDate(LocalDate.of(2022, 11, 22))
            .build();

        DefendantResponseStatus status = caseData.getDefendantResponseStatus();

        Assertions.assertEquals(status, DefendantResponseStatus.PAID_IN_FULL_CCJ_CANCELLED);
    }

    @Test
    public void defendantResponseStatus_paidInFullCCJSatisfied() {
        Respondent1DQ respondent1DQ = new Respondent1DQ();
        respondent1DQ.setResponseClaimCourtLocationRequired(YesOrNo.YES);
        RespondToClaim respondToClaim = RespondToClaim
            .builder()
            .whenWasThisAmountPaid(LocalDate.of(2022, 10, 22))
            .build();
        CaseData caseData = CaseData.builder()
            .respondToClaim(respondToClaim)
            .respondent1DQ(respondent1DQ)
            .hearingDate(LocalDate.of(2022, 9, 22))
            .build();

        DefendantResponseStatus status = caseData.getDefendantResponseStatus();

        Assertions.assertEquals(status, DefendantResponseStatus.PAID_IN_FULL_CCJ_SATISFIED);
    }

    @Test
    public void defendantResponseStatus_eligibleForCCJAfterFullAdmitPayImmediatelyPastDeadline() {
        RespondToClaim respondToClaim = RespondToClaim
            .builder()
            .whenWasThisAmountPaid(LocalDate.of(2022, 10, 22))
            .build();
        CaseData caseData = CaseData.builder()
            .respondToClaim(respondToClaim)
            .respondent1ClaimResponseType(RespondentResponseType.FULL_ADMISSION)
            .defenceAdmitPartPaymentTimeRouteRequired(RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY)
            .build();

        DefendantResponseStatus status = caseData.getDefendantResponseStatus();

        Assertions.assertEquals(status, DefendantResponseStatus.ELIGIBLE_FOR_CCJ_AFTER_FULL_ADMIT_PAY_IMMEDIATELY_PAST_DEADLINE);
    }

    @Test
    public void defendantResponseStatus_moreTimeRequested() {
        CaseData caseData = CaseData.builder()
            .respondent1TimeExtensionDate(LocalDateTime.of(2023, 11, 20, 11, 11))
            .build();

        DefendantResponseStatus status = caseData.getDefendantResponseStatus();

        Assertions.assertEquals(status, DefendantResponseStatus.MORE_TIME_REQUESTED);
    }

    @Test
    public void defendantResponseStatus_transferred() {
        CaseData caseData = CaseData.builder()
            .ccdState(CaseState.JUDICIAL_REFERRAL)
            .build();

        DefendantResponseStatus status = caseData.getDefendantResponseStatus();

        Assertions.assertEquals(status, DefendantResponseStatus.TRANSFERRED);
    }
}
