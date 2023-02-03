package uk.gov.hmcts.reform.cmc.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.enums.ClaimState;
import uk.gov.hmcts.reform.civil.model.citizenui.DefendantResponseStatus;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseType;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CountyCourtJudgment;
import uk.gov.hmcts.reform.civil.model.PaymentDetails;
import uk.gov.hmcts.reform.civil.model.dq.Respondent1DQ;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;

public class CmcClaimTest {

    private static final String NAME = "Mr John Clark";

    @Test
    void shouldReturnClaimantNameWhenClaimantExists() {
        CmcClaim cmcClaim = CmcClaim.builder()
            .claimData(ClaimData.builder().claimants(Arrays.asList(CmcParty.builder()
                                                                       .name(NAME)
                                                                       .build()))
                           .build())
            .build();
        String claimantName = cmcClaim.getClaimantName();
        assert (claimantName).equals(NAME);
    }

    @Test
    void shouldReturnEmptyStringWhenClaimantDoesNotExist() {
        CmcClaim cmcClaim = CmcClaim.builder()
            .claimData(ClaimData.builder().claimants(Collections.emptyList()).build())
            .build();
        String claimantName = cmcClaim.getClaimantName();
        assert (claimantName).equals("");
    }

    @Test
    void shouldReturnDefendantNameWhenDefendantExists() {
        CmcClaim cmcClaim = CmcClaim.builder()
            .claimData(ClaimData.builder().defendants(Arrays.asList(CmcParty.builder().name(NAME)
                                                                        .build()))
                           .build())
            .build();
        String defendantName = cmcClaim.getDefendantName();
        assert (defendantName).equals(NAME);
    }

    @Test
    void shouldReturnEmptyStringWhenDefendantDoesNotExist() {
        CmcClaim cmcClaim = CmcClaim.builder()
            .claimData(ClaimData.builder().defendants(Collections.emptyList()).build())
            .build();
        String defendantName = cmcClaim.getDefendantName();
        assert (defendantName).equals("");
    }

    @Test
    public void defendantResponseStatus_noResponse() {
        CmcClaim cmcClaim = CmcClaim.builder()
            .response(null)
            .build();

        DefendantResponseStatus status = cmcClaim.getDefendantResponseStatus();

        Assertions.assertEquals(status, DefendantResponseStatus.NO_RESPONSE);
    }

    @Test
    public void defendantResponseStatus_eligibleForCCJ() {
        CountyCourtJudgment ccj = new CountyCourtJudgment();
        ccj.paymentDetails = new PaymentDetails();
        CmcClaim cmcClaim = CmcClaim
            .builder()
            .ccj(ccj)
            .build();

        DefendantResponseStatus status = cmcClaim.getDefendantResponseStatus();

        Assertions.assertEquals(status, DefendantResponseStatus.ELIGIBLE_FOR_CCJ);
    }

    @Test
    public void defendantResponseStatus_claimantAcceptedStatesPaid() {
        Response response = new Response();
        response.responseType = RespondentResponseType.PART_ADMISSION;
        response.paymentDeclaration = new PaymentDeclaration();
        CmcClaim cmcClaim = CmcClaim.builder()
            .claimantResponse(ClaimantResponse.ACCEPTATION)
            .response(response)
            .build();

        DefendantResponseStatus status = cmcClaim.getDefendantResponseStatus();

        Assertions.assertEquals(status, DefendantResponseStatus.CLAIMANT_ACCEPTED_STATES_PAID);
    }

    @Test
    public void defendantResponseStatus_redeterminationByJudge() {
        Respondent1DQ respondent1DQ = new Respondent1DQ();
        respondent1DQ.setResponseClaimCourtLocationRequired(YesOrNo.YES);
        CmcClaim cmcClaim = CmcClaim.builder()
            .claimantResponse(ClaimantResponse.ACCEPTATION)
            .countyCourtJudgmentRequestedAt(LocalDateTime.of(2022, 3, 22, 10, 30))
            .build();

        DefendantResponseStatus status = cmcClaim.getDefendantResponseStatus();

        Assertions.assertEquals(status, DefendantResponseStatus.REDETERMINATION_BY_JUDGE);
    }

    @Test
    public void defendantResponseStatus_paidInFullCCJCanceled() {
        CmcClaim cmcClaim = CmcClaim.builder()
            .moneyReceivedOn(LocalDate.of(2022, 1, 22))
            .countyCourtJudgmentRequestedAt(LocalDateTime.of(2022, 3, 20, 10, 30))
            .build();

        DefendantResponseStatus status = cmcClaim.getDefendantResponseStatus();

        Assertions.assertEquals(status, DefendantResponseStatus.PAID_IN_FULL_CCJ_CANCELLED);
    }

    @Test
    public void defendantResponseStatus_paidInFullCCJSatisfied() {
        CmcClaim cmcClaim = CmcClaim.builder()
            .moneyReceivedOn(LocalDate.of(2022, 4, 22))
            .countyCourtJudgmentRequestedAt(LocalDateTime.of(2022, 3, 20, 10, 30))
            .build();

        DefendantResponseStatus status = cmcClaim.getDefendantResponseStatus();

        Assertions.assertEquals(status, DefendantResponseStatus.PAID_IN_FULL_CCJ_SATISFIED);
    }

    @Test
    public void defendantResponseStatus_eligibleForCCJAfterFullAdmitPayImmediatelyPastDeadline() {
        CmcClaim caseData = CmcClaim.builder()
            .admissionPayImmediatelyPastPaymentDate(LocalDate.of(2022, 02, 22))
            .claimantResponse(null)
            .build();

        DefendantResponseStatus status = caseData.getDefendantResponseStatus();

        Assertions.assertEquals(status, DefendantResponseStatus.ELIGIBLE_FOR_CCJ_AFTER_FULL_ADMIT_PAY_IMMEDIATELY_PAST_DEADLINE);
    }

    @Test
    public void defendantResponseStatus_moreTimeRequested() {
        CmcClaim caseData = CmcClaim.builder()
            .moreTimeRequested(true)
            .build();

        DefendantResponseStatus status = caseData.getDefendantResponseStatus();

        Assertions.assertEquals(status, DefendantResponseStatus.MORE_TIME_REQUESTED);
    }

    @Test
    public void defendantResponseStatus_transferred() {
        CmcClaim caseData = CmcClaim.builder()
            .state(ClaimState.TRANSFERRED)
            .build();

        DefendantResponseStatus status = caseData.getDefendantResponseStatus();

        Assertions.assertEquals(status, DefendantResponseStatus.TRANSFERRED);
    }
}
