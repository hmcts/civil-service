package uk.gov.hmcts.reform.civil.service.hearings;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.civil.config.HearingFeeConfiguration;
import uk.gov.hmcts.reform.civil.exceptions.InternalServerErrorException;
import uk.gov.hmcts.reform.civil.model.Fee;
import uk.gov.hmcts.reform.fees.client.model.FeeLookupResponseDto;

import java.math.BigDecimal;
import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = {HearingFeesService.class, RestTemplate.class, HearingFeeConfiguration.class})
class HearingFeesServiceTest {

    private static final BigDecimal TEST_FEE_AMOUNT_POUNDS_14 = new BigDecimal("14.00");
    private static final BigDecimal TEST_FEE_AMOUNT_PENCE_14 = new BigDecimal("1400");

    @Captor
    private ArgumentCaptor<URI> queryCaptor;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private HearingFeeConfiguration feesConfiguration;

    @InjectMocks
    private HearingFeesService feesService;

    @BeforeEach
    void setUp() {
        when(feesConfiguration.getUrl()).thenReturn("dummy_url");
        when(feesConfiguration.getEndpoint()).thenReturn("/fees-register/fees/lookup");
        when(feesConfiguration.getService()).thenReturn("civil money claims");
        when(feesConfiguration.getChannel()).thenReturn("default");
        when(feesConfiguration.getHearingEvent()).thenReturn("hearing");
        when(feesConfiguration.getJurisdiction1()).thenReturn("civil");
        when(feesConfiguration.getJurisdiction2()).thenReturn("county court");
        when(feesConfiguration.getJurisdiction2Hearing()).thenReturn("civil");
        when(feesConfiguration.getFastTrackHrgKey()).thenReturn("FastTrackHrgKey");
        when(feesConfiguration.getMultiClaimKey()).thenReturn("MultiTrackHrg");
        when(feesConfiguration.getSmallClaimHrgKey()).thenReturn("HearingSmallClaims");
    }

    @Test
    void shouldReturnFeeData_whenSmallClaim() {
        // Given
        given(restTemplate.getForObject(queryCaptor.capture(), eq(FeeLookupResponseDto.class)))
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
        assertThat(queryCaptor.getValue().toString())
            .isEqualTo("dummy_url/fees-register/fees/lookup?channel=default&event=hearing&jurisdiction1"
                           + "=civil&jurisdiction2=county%20court&service=civil%20money%20claims"
                           + "&keyword=HearingSmallClaims&amount_or_volume=125");
    }

    @Test
    void shouldReturnFeeData_whenFastTrackClaim() {
        // Given
        given(restTemplate.getForObject(queryCaptor.capture(), eq(FeeLookupResponseDto.class)))
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
        assertThat(queryCaptor.getValue().toString())
            .isEqualTo("dummy_url/fees-register/fees/lookup?channel=default&event=hearing&jurisdiction1"
                           + "=civil&jurisdiction2=civil&service=civil%20money%20claims"
                           + "&keyword=FastTrackHrgKey&amount_or_volume=125");
    }

    @Test
    void shouldReturnFeeData_whenMultiClaim() {
        // Given
        given(restTemplate.getForObject(queryCaptor.capture(), eq(FeeLookupResponseDto.class)))
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
        assertThat(queryCaptor.getValue().toString())
            .isEqualTo("dummy_url/fees-register/fees/lookup?channel=default&event=hearing&jurisdiction1"
                           + "=civil&jurisdiction2=civil&service=civil%20money%20claims"
                           + "&keyword=MultiTrackHrg&amount_or_volume=125");
    }

    @Test
    void throwRuntimeException_whenFeeServiceThrowsException() {
        // Given
        given(restTemplate.getForObject(queryCaptor.capture(), eq(FeeLookupResponseDto.class)))
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
        given(restTemplate.getForObject(queryCaptor.capture(), eq(FeeLookupResponseDto.class)))
            .willReturn(null);

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
        given(restTemplate.getForObject(queryCaptor.capture(), eq(FeeLookupResponseDto.class)))
            .willReturn(FeeLookupResponseDto.builder()
                            .code("test_fee_code")
                            .version(1)
                            .build());

        BigDecimal claimAmount = new BigDecimal(125);
        // When
        Exception exception = assertThrows(
            InternalServerErrorException.class, () -> feesService.getFeeForHearingSmallClaims(claimAmount));
        // Then
        assertThat(exception.getMessage())
            .isEqualTo("No Fees returned by fee-service while creating hearing fee");
    }
}
