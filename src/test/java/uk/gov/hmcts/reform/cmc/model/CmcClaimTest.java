package uk.gov.hmcts.reform.cmc.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.model.citizenui.DashboardClaimStatus;
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
}
