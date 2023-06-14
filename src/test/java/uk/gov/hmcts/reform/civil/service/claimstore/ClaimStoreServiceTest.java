package uk.gov.hmcts.reform.civil.service.claimstore;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.civil.model.citizenui.DashboardClaimInfo;
import uk.gov.hmcts.reform.civil.model.citizenui.DashboardClaimStatusFactory;
import uk.gov.hmcts.reform.cmc.client.ClaimStoreApi;
import uk.gov.hmcts.reform.cmc.model.ClaimData;
import uk.gov.hmcts.reform.cmc.model.CmcClaim;
import uk.gov.hmcts.reform.cmc.model.CmcParty;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(SpringExtension.class)
public class ClaimStoreServiceTest {

    @Mock
    private ClaimStoreApi claimStoreApi;

    @Mock
    private DashboardClaimStatusFactory dashboardClaimStatusFactory;

    @InjectMocks
    private ClaimStoreService claimStoreService;

    private static final String CLAIMANT_NAME = "Mr John Wick";
    private static final String DEFENDANT_NAME = "Mr James Bond";
    private static final String REFERENCE_NUMBER = "256MC007";
    private static final BigDecimal TOTAL_AMOUNT = new BigDecimal("1000");
    private static final LocalDate RESPONSE_DEADLINE = LocalDate.of(2021, 1, 1);
    private static final LocalDate CREATE_DATE = LocalDate.of(2023, 1, 22);
    private static final LocalDateTime CREATE_DATETIME = CREATE_DATE.atTime(0, 0);
    private static final LocalDateTime intentionToProceedDeadline = LocalDateTime.now().minusDays(1);
    private static final List<DashboardClaimInfo> EXPECTED_CLAIM_RESULT = Arrays.asList(DashboardClaimInfo.builder()
                                                                                            .claimNumber(
                                                                                                REFERENCE_NUMBER)
                                                                                            .claimAmount(TOTAL_AMOUNT)
                                                                                            .claimantName(CLAIMANT_NAME)
                                                                                            .defendantName(
                                                                                                DEFENDANT_NAME)
                                                                                            .responseDeadline(
                                                                                                RESPONSE_DEADLINE)
                                                                                            .ocmc(true)
                                                                                            .createdDate(CREATE_DATETIME)
                                                                                            .intentionToProceedDeadline(
                                                                                                intentionToProceedDeadline)
                                                                                            .build(),
                                                                                        DashboardClaimInfo.builder()
                                                                                            .claimNumber(
                                                                                                REFERENCE_NUMBER)
                                                                                            .claimAmount(TOTAL_AMOUNT)
                                                                                            .claimantName(CLAIMANT_NAME)
                                                                                            .defendantName(
                                                                                                DEFENDANT_NAME)
                                                                                            .responseDeadline(
                                                                                                RESPONSE_DEADLINE)
                                                                                            .ocmc(true)
                                                                                            .createdDate(CREATE_DATETIME)
                                                                                            .build()
    );

    @BeforeEach
    void setUp() {
        List<CmcClaim> cmcClaims = Arrays.asList(CmcClaim.builder()
                                                     .claimData(ClaimData.builder()
                                                                    .defendants(Collections.singletonList(CmcParty.builder()
                                                                                                              .name(
                                                                                                                  DEFENDANT_NAME)
                                                                                                              .build()))
                                                                    .claimants(Collections.singletonList(CmcParty.builder()
                                                                                                             .name(
                                                                                                                 CLAIMANT_NAME)
                                                                                                             .build()))
                                                                    .build())
                                                     .referenceNumber(REFERENCE_NUMBER)
                                                     .responseDeadline(RESPONSE_DEADLINE)
                                                     .totalAmountTillToday(TOTAL_AMOUNT)
                                                     .createdAt(CREATE_DATETIME)
                                                     .intentionToProceedDeadline(intentionToProceedDeadline)
                                                     .build(), CmcClaim.builder()
                                                     .claimData(ClaimData.builder()
                                                                    .defendants(Collections.singletonList(CmcParty.builder()
                                                                                                              .name(
                                                                                                                  DEFENDANT_NAME)
                                                                                                              .build()))
                                                                    .claimants(Collections.singletonList(CmcParty.builder()
                                                                                                             .name(
                                                                                                                 CLAIMANT_NAME)
                                                                                                             .build()))
                                                                    .build())
                                                     .referenceNumber(REFERENCE_NUMBER)
                                                     .responseDeadline(RESPONSE_DEADLINE)
                                                     .totalAmountTillToday(TOTAL_AMOUNT)
                                                     .createdAt(CREATE_DATETIME)
                                                     .build());
        given(claimStoreApi.getClaimsForClaimant(any(), any())).willReturn(cmcClaims);
        given(claimStoreApi.getClaimsForDefendant(any(), any())).willReturn(cmcClaims);
    }

    @Test
    void shouldReturnClaimsForClaimantsSuccessfully() {
        List<DashboardClaimInfo> resultClaims = claimStoreService.getClaimsForClaimant("23746486", "1234");
        verify(claimStoreApi).getClaimsForClaimant("23746486", "1234");
        assertThat(resultClaims.size()).isEqualTo(2);
        assertThat(resultClaims.get(0)).isEqualTo(EXPECTED_CLAIM_RESULT.get(0));
        assertThat(resultClaims.get(1)).isEqualTo(EXPECTED_CLAIM_RESULT.get(1));
    }

    @Test
    void shouldReturnClaimsForDefendantSuccessfully() {
        List<DashboardClaimInfo> resultClaims = claimStoreService.getClaimsForDefendant("23746486", "1234");
        verify(claimStoreApi).getClaimsForDefendant("23746486", "1234");
        assertThat(resultClaims.size()).isEqualTo(2);
        assertThat(resultClaims.get(0)).isEqualTo(EXPECTED_CLAIM_RESULT.get(0));
        assertThat(resultClaims.get(1)).isEqualTo(EXPECTED_CLAIM_RESULT.get(1));
    }
}
