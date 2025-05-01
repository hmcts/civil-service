package uk.gov.hmcts.reform.civil.service.hearings;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClientException;
import uk.gov.hmcts.reform.civil.client.FeesApiClient;
import uk.gov.hmcts.reform.civil.config.HearingFeeConfiguration;
import uk.gov.hmcts.reform.civil.exceptions.InternalServerErrorException;
import uk.gov.hmcts.reform.civil.model.Fee;
import uk.gov.hmcts.reform.civil.model.FeeLookupResponseDto;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HearingFeesServiceTest {

    private static final BigDecimal TEST_FEE_AMOUNT_POUNDS_14 = new BigDecimal("14.00");
    private static final BigDecimal TEST_FEE_AMOUNT_PENCE_14 = new BigDecimal("1400");

    @Captor
    private ArgumentCaptor<String> keywordCaptor;

    @Mock
    private FeesApiClient feesApiClient;

    @Mock
    private HearingFeeConfiguration feesConfiguration;

    @InjectMocks
    private HearingFeesService feesService;

    @Test
    void shouldReturnFeeData_whenSmallClaim() {
        when(feesConfiguration.getService()).thenReturn("civil money claims");
        when(feesConfiguration.getChannel()).thenReturn("default");
        when(feesConfiguration.getHearingEvent()).thenReturn("hearing");
        when(feesConfiguration.getJurisdiction1()).thenReturn("civil");
        when(feesConfiguration.getJurisdiction2()).thenReturn("county court");
        when(feesConfiguration.getSmallClaimHrgKey()).thenReturn("HearingSmallClaims");

        given(feesApiClient.lookupFeeWithAmount(
            anyString(),
            anyString(),
            anyString(),
            anyString(),
            anyString(),
            keywordCaptor.capture(),
            any(BigDecimal.class)
        ))
            .willReturn(FeeLookupResponseDto.builder()
                            .feeAmount(TEST_FEE_AMOUNT_POUNDS_14)
                            .code("test_fee_code")
                            .version(2)
                            .build());

        BigDecimal claimAmount = new BigDecimal(125);

        Fee expectedFeeDto = Fee.builder()
            .calculatedAmountInPence(TEST_FEE_AMOUNT_PENCE_14)
            .code("test_fee_code")
            .version("2")
            .build();
        // When
        Fee feeDto = feesService.getFeeForHearingSmallClaims(claimAmount);
        // Then
        assertThat(feeDto).isEqualTo(expectedFeeDto);
        assertThat(keywordCaptor.getValue())
            .isEqualTo("HearingSmallClaims");
    }

    @Test
    void shouldReturnFeeData_whenFastTrackClaim() {
        when(feesConfiguration.getService()).thenReturn("civil money claims");
        when(feesConfiguration.getChannel()).thenReturn("default");
        when(feesConfiguration.getHearingEvent()).thenReturn("hearing");
        when(feesConfiguration.getJurisdiction1()).thenReturn("civil");
        when(feesConfiguration.getJurisdiction2Hearing()).thenReturn("civil");
        when(feesConfiguration.getFastTrackHrgKey()).thenReturn("FastTrackHrgKey");
        // Given
        given(feesApiClient.lookupFeeWithAmount(
            anyString(),
            anyString(),
            anyString(),
            anyString(),
            anyString(),
            keywordCaptor.capture(),
            any(BigDecimal.class)
        ))
            .willReturn(FeeLookupResponseDto.builder()
                            .feeAmount(TEST_FEE_AMOUNT_POUNDS_14)
                            .code("test_fee_code")
                            .version(2)
                            .build());

        BigDecimal claimAmount = new BigDecimal(125);

        Fee expectedFeeDto = Fee.builder()
            .calculatedAmountInPence(TEST_FEE_AMOUNT_PENCE_14)
            .code("test_fee_code")
            .version("2")
            .build();
        // When
        Fee feeDto = feesService.getFeeForHearingFastTrackClaims(claimAmount);
        // Then
        assertThat(feeDto).isEqualTo(expectedFeeDto);
        assertThat(keywordCaptor.getValue())
            .isEqualTo("FastTrackHrgKey");
    }

    @Test
    void shouldReturnFeeData_whenMultiClaim() {
        // Given
        when(feesConfiguration.getService()).thenReturn("civil money claims");
        when(feesConfiguration.getChannel()).thenReturn("default");
        when(feesConfiguration.getHearingEvent()).thenReturn("hearing");
        when(feesConfiguration.getJurisdiction1()).thenReturn("civil");
        when(feesConfiguration.getJurisdiction2Hearing()).thenReturn("civil");
        when(feesConfiguration.getMultiClaimKey()).thenReturn("MultiTrackHrg");
        given(feesApiClient.lookupFeeWithAmount(
            anyString(),
            anyString(),
            anyString(),
            anyString(),
            anyString(),
            keywordCaptor.capture(),
            any(BigDecimal.class)
        ))
            .willReturn(FeeLookupResponseDto.builder()
                            .feeAmount(TEST_FEE_AMOUNT_POUNDS_14)
                            .code("test_fee_code")
                            .version(2)
                            .build());

        BigDecimal claimAmount = new BigDecimal(125);

        Fee expectedFeeDto = Fee.builder()
            .calculatedAmountInPence(TEST_FEE_AMOUNT_PENCE_14)
            .code("test_fee_code")
            .version("2")
            .build();
        // When
        Fee feeDto = feesService.getFeeForHearingMultiClaims(claimAmount);
        // Then
        assertThat(feeDto).isEqualTo(expectedFeeDto);
        assertThat(keywordCaptor.getValue())
            .isEqualTo("MultiTrackHrg");
    }

    @Test
    void throwRuntimeException_whenFeeServiceThrowsException() {
        when(feesConfiguration.getService()).thenReturn("civil money claims");
        when(feesConfiguration.getChannel()).thenReturn("default");
        when(feesConfiguration.getHearingEvent()).thenReturn("hearing");
        when(feesConfiguration.getJurisdiction1()).thenReturn("civil");
        when(feesConfiguration.getJurisdiction2()).thenReturn("county court");
        when(feesConfiguration.getSmallClaimHrgKey()).thenReturn("HearingSmallClaims");
        // Given
        given(feesApiClient.lookupFeeWithAmount(
            anyString(),
            anyString(),
            anyString(),
            anyString(),
            anyString(),
            anyString(),
            any(BigDecimal.class)
        ))
            .willThrow(new RestClientException("No Fees returned by fee-service while creating hearing fee"));

        BigDecimal claimAmount = new BigDecimal(125);
        // When
        Exception exception = assertThrows(
            RestClientException.class, () -> feesService.getFeeForHearingSmallClaims(claimAmount));
        // Then
        assertThat(exception).isInstanceOf(RestClientException.class);
    }

    @Test
    void throwRuntimeException_whenNoFeeIsReturnedByFeeService() {
        // Given
        when(feesConfiguration.getService()).thenReturn("civil money claims");
        when(feesConfiguration.getChannel()).thenReturn("default");
        when(feesConfiguration.getHearingEvent()).thenReturn("hearing");
        when(feesConfiguration.getJurisdiction1()).thenReturn("civil");
        when(feesConfiguration.getJurisdiction2()).thenReturn("county court");
        when(feesConfiguration.getSmallClaimHrgKey()).thenReturn("HearingSmallClaims");

        BigDecimal claimAmount = new BigDecimal(125);

        Exception exception = assertThrows(
            InternalServerErrorException.class, () -> feesService.getFeeForHearingSmallClaims(claimAmount));
        // Then
        assertThat(exception.getMessage())
            .isEqualTo("No Fees returned by fee-service while creating hearing fee");
    }

    @Test
    void throwRuntimeException_whenNoFeeAmountIsReturnedByFeeService() {
        // Given
        when(feesConfiguration.getService()).thenReturn("civil money claims");
        when(feesConfiguration.getChannel()).thenReturn("default");
        when(feesConfiguration.getHearingEvent()).thenReturn("hearing");
        when(feesConfiguration.getJurisdiction1()).thenReturn("civil");
        when(feesConfiguration.getJurisdiction2()).thenReturn("county court");
        when(feesConfiguration.getSmallClaimHrgKey()).thenReturn("HearingSmallClaims");

        BigDecimal claimAmount = new BigDecimal(125);
        // When
        Exception exception = assertThrows(
            InternalServerErrorException.class, () -> feesService.getFeeForHearingSmallClaims(claimAmount));
        // Then
        assertThat(exception.getMessage())
            .isEqualTo("No Fees returned by fee-service while creating hearing fee");
    }
}
