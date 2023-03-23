package uk.gov.hmcts.reform.cmc.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

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
    void shouldReturnPayedByDateWhenItExists() {
        //Given
        LocalDate now = LocalDate.now();
        CmcClaim cmcClaim = CmcClaim.builder()
            .response(Response.builder().paymentIntention(PaymentIntention.builder()
                                                              .paymentDate(now).build())
                          .build()).build();
        //When
        LocalDate paymentDate = cmcClaim.getBySpecifiedDate();
        //Then
        assertThat(paymentDate).isEqualTo(now);
    }

    @Test
    void shouldReturnNullWhenNoResponseExists() {
        //Given
        CmcClaim cmcClaim = CmcClaim.builder().build();
        //When
        LocalDate paymentDate = cmcClaim.getBySpecifiedDate();
        //Then
        assertThat(paymentDate).isNull();
    }

    @Test
    void shouldReturnNullWhenNoPaymentIntentionExists() {
        //Given
        CmcClaim cmcClaim = CmcClaim.builder()
            .response(Response.builder().build()).build();
        //When
        LocalDate paymentDate = cmcClaim.getBySpecifiedDate();
        //Then
        assertThat(paymentDate).isNull();
    }
}
