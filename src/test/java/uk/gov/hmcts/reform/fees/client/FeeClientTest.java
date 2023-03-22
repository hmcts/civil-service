package uk.gov.hmcts.reform.fees.client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.civil.config.FeesConfiguration;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.fees.client.model.FeeLookupResponseDto;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class FeeClientTest {

    private static final String CHANNEL = "channel";
    private static final String EVENT = "event";
    private static final String HEARING_EVENT = "hearingEvent";
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
        given(feesConfiguration.getChannel()).willReturn(CHANNEL);
        given(feesConfiguration.getEvent()).willReturn(EVENT);
        given(feesConfiguration.getHearingEvent()).willReturn(HEARING_EVENT);
        feesClient = new FeesClient(feesApi, featureToggleService, "civil", "jurisdiction1", "jurisdiction2");
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

        FeeLookupResponseDto feeLookupResponseDto = feesClient.lookupFee(CHANNEL, EVENT, new BigDecimal("50.00"));

        verify(feesApi).lookupFee(
            "civil",
            "jurisdiction1",
            "jurisdiction2",
            CHANNEL,
            EVENT,
            "HearingSmallClaims",
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

        FeeLookupResponseDto feeLookupResponseDto = feesClient.lookupFee(CHANNEL, EVENT, new BigDecimal("50.00"));

        verify(feesApi).lookupFeeWithoutKeyword(
            "civil",
            "jurisdiction1",
            "jurisdiction2",
            CHANNEL,
            EVENT,
            new BigDecimal("50.00")
        );
        assertThat(feeLookupResponseDto).isEqualTo(expectedFeeDtoFeeLookupResponseDto);
    }

}
