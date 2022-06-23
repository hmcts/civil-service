package uk.gov.hmcts.reform.cmc.model;


import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

public class CmcClaimTest {

    private final String PARTY_NAME = "Mr John Clark";

    @Test
    void shouldReturnClaimantNameWhenClaimantExists(){
        CmcClaim cmcClaim = CmcClaim.builder()
            .claimData(ClaimData.builder().claimants(Arrays.asList(CmcParty.builder().name(PARTY_NAME).build())).build())
            .build();
        String claimantName = cmcClaim.getClaimantName();
        assert(claimantName).equals(PARTY_NAME);
    }

    @Test
    void shouldReturnEmptyStringWhenClaimantDoesNotExist(){
        CmcClaim cmcClaim = CmcClaim.builder()
            .claimData(ClaimData.builder().claimants(Collections.emptyList()).build())
            .build();
        String claimantName = cmcClaim.getClaimantName();
        assert(claimantName).equals("");
    }

    @Test
    void shouldReturnDefendantNameWhenDefendantExists(){
        CmcClaim cmcClaim = CmcClaim.builder()
            .claimData(ClaimData.builder().defendants(Arrays.asList(CmcParty.builder().name(PARTY_NAME).build())).build())
            .build();
        String defendantName = cmcClaim.getDefendantName();
        assert(defendantName).equals(PARTY_NAME);
    }

    @Test
    void shouldReturnEmptyStringWhenDefendantDoesNotExist(){
        CmcClaim cmcClaim = CmcClaim.builder()
            .claimData(ClaimData.builder().defendants(Collections.emptyList()).build())
            .build();
        String defendantName = cmcClaim.getDefendantName();
        assert(defendantName).equals("");
    }
}
