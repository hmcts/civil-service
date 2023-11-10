package uk.gov.hmcts.reform.fees.client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.config.FeesConfiguration;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.fees.client.model.FeeLookupResponseDto;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fees.client.FeesClient.EVENT_HEARING;
import static uk.gov.hmcts.reform.fees.client.FeesClient.EVENT_ISSUE;
import static uk.gov.hmcts.reform.fees.client.FeesClient.FAST_TRACK_HEARING;
import static uk.gov.hmcts.reform.fees.client.FeesClient.HEARING_SMALL_CLAIMS;
import static uk.gov.hmcts.reform.fees.client.FeesClient.MONEY_CLAIM;

@ExtendWith(MockitoExtension.class)
class FeeClientTest {

    private static final String CHANNEL = "channel";

    private static final BigDecimal TEST_FEE_AMOUNT_POUNDS = new BigDecimal("1.00");
    @Mock
    private FeesApi feesApi;
    @Mock
    private FeesConfiguration feesConfiguration;
    @Mock
    private FeatureToggleService featureToggleService;
    private FeesClient feesClient;

    @BeforeEach
    void setUp() {
        feesClient = new FeesClient(
            feesApi,
            featureToggleService,
            "civil",
            "jurisdiction1",
            "jurisdiction2",
            "jurisdictionFastTrackClaim"
        );
    }

    @Test
    void shouldReturnFeeData_whenValidClaimValueWhenFeatureIsEnabled() {
        when(featureToggleService.isFeatureEnabled("fee-keywords-enable")).thenReturn(true);
        given(feesApi.lookupFee(any(), any(), any(), any(), any(), any(), any()))
            .willReturn(FeeLookupResponseDto.builder()
                            .feeAmount(TEST_FEE_AMOUNT_POUNDS)
                            .code("test_fee_code")
                            .version(1)
                            .build());

        FeeLookupResponseDto expectedFeeDtoFeeLookupResponseDto = FeeLookupResponseDto.builder()
            .feeAmount(TEST_FEE_AMOUNT_POUNDS)
            .code("test_fee_code")
            .version(1)
            .build();

        FeeLookupResponseDto feeLookupResponseDto = feesClient.lookupFee(CHANNEL, EVENT_ISSUE, new BigDecimal("50.00"));

        verify(feesApi).lookupFee(
            "civil",
            "jurisdiction1",
            "jurisdiction2",
            CHANNEL,
            EVENT_ISSUE,
            MONEY_CLAIM,
            new BigDecimal("50.00")
        );
        assertThat(feeLookupResponseDto).isEqualTo(expectedFeeDtoFeeLookupResponseDto);
    }

    @Test
    void shouldReturnFeeData_whenValidClaimValueWhenFeatureIsNotEnabled() {
        when(featureToggleService.isFeatureEnabled("fee-keywords-enable")).thenReturn(false);
        given(feesApi.lookupFeeWithoutKeyword(any(), any(), any(), any(), any(), any()))
            .willReturn(FeeLookupResponseDto.builder()
                            .feeAmount(TEST_FEE_AMOUNT_POUNDS)
                            .code("test_fee_code")
                            .version(1)
                            .build());

        FeeLookupResponseDto expectedFeeDtoFeeLookupResponseDto = FeeLookupResponseDto.builder()
            .feeAmount(TEST_FEE_AMOUNT_POUNDS)
            .code("test_fee_code")
            .version(1)
            .build();

        FeeLookupResponseDto feeLookupResponseDto = feesClient.lookupFee(CHANNEL, EVENT_ISSUE, new BigDecimal("50.00"));

        verify(feesApi).lookupFeeWithoutKeyword(
            "civil",
            "jurisdiction1",
            "jurisdiction2",
            CHANNEL,
            EVENT_ISSUE,
            new BigDecimal("50.00")
        );
        assertThat(feeLookupResponseDto).isEqualTo(expectedFeeDtoFeeLookupResponseDto);
    }

    @Test
    void shouldCallLookupFeeWhenFastTrackClaimWithHearingEventAndLipVLipFeatureEnabled() {
        when(featureToggleService.isFeatureEnabled("fee-keywords-enable")).thenReturn(true);
        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);

        given(feesApi.lookupFee(any(), any(), any(), any(), any(), any(), any()))
            .willReturn(FeeLookupResponseDto.builder()
                            .feeAmount(TEST_FEE_AMOUNT_POUNDS)
                            .code("test_fee_code")
                            .version(1)
                            .build());

        FeeLookupResponseDto feeLookupResponseDto = feesClient.lookupFee(
            CHANNEL,
            EVENT_HEARING,
            new BigDecimal("10001.00")
        );

        verify(feesApi).lookupFee(
            "civil",
            "jurisdiction1",
            "jurisdictionFastTrackClaim",
            CHANNEL,
            EVENT_HEARING,
            FAST_TRACK_HEARING,
            new BigDecimal("10001.00")
        );

    }

    @Test
    void shouldCallLookupFeeWithKeyWordHearingSmallClaimsWhenEventIsNotIssue() {
        when(featureToggleService.isFeatureEnabled("fee-keywords-enable")).thenReturn(true);
        given(feesApi.lookupFee(any(), any(), any(), any(), any(), any(), any()))
            .willReturn(FeeLookupResponseDto.builder()
                            .feeAmount(TEST_FEE_AMOUNT_POUNDS)
                            .code("test_fee_code")
                            .version(1)
                            .build());

        FeeLookupResponseDto feeLookupResponseDto = feesClient.lookupFee(CHANNEL, "EventOtherThanIssueOrHearing", new BigDecimal("50.00"));

        verify(feesApi).lookupFee(
            "civil",
            "jurisdiction1",
            "jurisdiction2",
            CHANNEL,
            "EventOtherThanIssueOrHearing",
            HEARING_SMALL_CLAIMS,
            new BigDecimal("50.00")
        );

    }
}
