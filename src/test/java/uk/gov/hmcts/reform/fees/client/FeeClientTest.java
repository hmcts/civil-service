package uk.gov.hmcts.reform.fees.client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.client.FeesApiClient;
import uk.gov.hmcts.reform.civil.config.FeesConfiguration;
import uk.gov.hmcts.reform.civil.config.OtherRemedyFeesConfiguration;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.FeesClientService;
import uk.gov.hmcts.reform.civil.model.FeeLookupResponseDto;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.service.FeesClientService.EVENT_HEARING;
import static uk.gov.hmcts.reform.civil.service.FeesClientService.EVENT_ISSUE;
import static uk.gov.hmcts.reform.civil.service.FeesClientService.FAST_TRACK_HEARING;
import static uk.gov.hmcts.reform.civil.service.FeesClientService.HEARING_SMALL_CLAIMS;
import static uk.gov.hmcts.reform.civil.service.FeesClientService.MONEY_CLAIM;

@ExtendWith(MockitoExtension.class)
class FeeClientTest {

    private static final String CHANNEL = "channel";

    private static final BigDecimal TEST_FEE_AMOUNT_POUNDS = new BigDecimal("1.00");
    @Mock
    private FeesApiClient feesApiClient;
    @Mock
    private FeesConfiguration feesConfiguration;
    @Mock
    private OtherRemedyFeesConfiguration otherRemedyFeesConfiguration;
    @Mock
    private FeatureToggleService featureToggleService;
    private FeesClientService feesClient;

    @BeforeEach
    void setUp() {
        feesClient = new FeesClientService(
                feesApiClient,
            featureToggleService,
            "civil",
            "jurisdiction1",
            "jurisdiction2",
            "jurisdictionFastTrackClaim"
        );
    }

    @Test
    void shouldThrowException_whenLookupFeeWithKeywordReturnsNull() {
        when(featureToggleService.isFeatureEnabled("fee-keywords-enable")).thenReturn(true);

        given(feesApiClient.lookupFeeWithAmount(any(), any(), any(), any(), any(), any(), any()))
            .willReturn(null);

        assertThatThrownBy(() ->
                               feesClient.lookupFee(CHANNEL, EVENT_ISSUE, new BigDecimal("50.00"))
        )
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("Fee lookup returned null response");
    }

    @Test
    void shouldReturnFeeData_whenValidClaimValueWhenFeatureIsEnabled() {
        when(featureToggleService.isFeatureEnabled("fee-keywords-enable")).thenReturn(true);
        given(feesApiClient.lookupFeeWithAmount(any(), any(), any(), any(), any(), any(), any()))
            .willReturn(new FeeLookupResponseDto()
                            .setFeeAmount(TEST_FEE_AMOUNT_POUNDS)
                            .setCode("test_fee_code")
                            .setVersion(1));

        FeeLookupResponseDto expectedFeeDtoFeeLookupResponseDto = new FeeLookupResponseDto()
            .setFeeAmount(TEST_FEE_AMOUNT_POUNDS)
            .setCode("test_fee_code")
            .setVersion(1);

        FeeLookupResponseDto feeLookupResponseDto = feesClient.lookupFee(CHANNEL, EVENT_ISSUE, new BigDecimal("50.00"));

        verify(feesApiClient).lookupFeeWithAmount(
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
        given(feesApiClient.lookupFeeWithoutKeyword(any(), any(), any(), any(), any(), any()))
            .willReturn(new FeeLookupResponseDto()
                            .setFeeAmount(TEST_FEE_AMOUNT_POUNDS)
                            .setCode("test_fee_code")
                            .setVersion(1));

        FeeLookupResponseDto expectedFeeDtoFeeLookupResponseDto = new FeeLookupResponseDto()
            .setFeeAmount(TEST_FEE_AMOUNT_POUNDS)
            .setCode("test_fee_code")
            .setVersion(1);

        FeeLookupResponseDto feeLookupResponseDto = feesClient.lookupFee(CHANNEL, EVENT_ISSUE, new BigDecimal("50.00"));

        verify(feesApiClient).lookupFeeWithoutKeyword(
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
    void shouldReturnFeeDataForOtherRemedy_whenValidClaimValue() {
        FeeLookupResponseDto expectedFeeDto = new FeeLookupResponseDto(
            "test_fee_code", null, TEST_FEE_AMOUNT_POUNDS, 1
        );
        given(feesApiClient.lookupFeeWithAmount(any(), any(), any(), any(), any(), any(), any()))
            .willReturn(expectedFeeDto);
        given(otherRemedyFeesConfiguration.getService()).willReturn("civil");
        given(otherRemedyFeesConfiguration.getJurisdiction1()).willReturn("jurisdiction1");
        given(otherRemedyFeesConfiguration.getJurisdiction2()).willReturn("jurisdiction2");
        given(otherRemedyFeesConfiguration.getChannel()).willReturn(CHANNEL);
        given(otherRemedyFeesConfiguration.getEvent()).willReturn(EVENT_ISSUE);
        given(otherRemedyFeesConfiguration.getAnyOtherRemedyKeyword()).willReturn("OtherRemedyKeyword");

        FeeLookupResponseDto feeLookupResponseDto = feesClient.lookupOtherRemedyFees(otherRemedyFeesConfiguration, new BigDecimal("50.00"));

        verify(feesApiClient, times(1)).lookupFeeWithAmount(
            "civil",
            "jurisdiction1",
            "jurisdiction2",
            CHANNEL,
            EVENT_ISSUE,
            "OtherRemedyKeyword",
            new BigDecimal("50.00")
        );
        assertThat(feeLookupResponseDto).isEqualTo(expectedFeeDto);
    }

    @Test
    void shouldCallLookupFeeWhenFastTrackClaimWithHearingEventAndLipVLipFeatureEnabled() {
        when(featureToggleService.isFeatureEnabled("fee-keywords-enable")).thenReturn(true);
        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);

        given(feesApiClient.lookupFeeWithAmount(any(), any(), any(), any(), any(), any(), any()))
            .willReturn(new FeeLookupResponseDto()
                            .setFeeAmount(TEST_FEE_AMOUNT_POUNDS)
                            .setCode("test_fee_code")
                            .setVersion(1));

        FeeLookupResponseDto feeLookupResponseDto = feesClient.lookupFee(
            CHANNEL,
            EVENT_HEARING,
            new BigDecimal("10001.00")
        );

        verify(feesApiClient).lookupFeeWithAmount(
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
        given(feesApiClient.lookupFeeWithAmount(any(), any(), any(), any(), any(), any(), any()))
            .willReturn(new FeeLookupResponseDto()
                            .setFeeAmount(TEST_FEE_AMOUNT_POUNDS)
                            .setCode("test_fee_code")
                            .setVersion(1));

        FeeLookupResponseDto feeLookupResponseDto = feesClient.lookupFee(CHANNEL, "EventOtherThanIssueOrHearing", new BigDecimal("50.00"));

        verify(feesApiClient).lookupFeeWithAmount(
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
