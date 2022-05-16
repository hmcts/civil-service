package uk.gov.hmcts.reform.civil.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.civil.config.GeneralAppFeesConfiguration;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Fee;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.GeneralApplicationDetailsBuilder;
import uk.gov.hmcts.reform.fees.client.model.FeeLookupResponseDto;

import java.math.BigDecimal;
import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = {GeneralAppFeesService.class, RestTemplate.class, GeneralAppFeesConfiguration.class})
class GeneralAppFeesServiceTest {

    private static final BigDecimal TEST_FEE_AMOUNT_POUNDS_108 = new BigDecimal("108.00");
    private static final BigDecimal TEST_FEE_AMOUNT_PENCE_108 = new BigDecimal("10800");
    private static final BigDecimal TEST_FEE_AMOUNT_POUNDS_275 = new BigDecimal("275.00");
    private static final BigDecimal TEST_FEE_AMOUNT_PENCE_275 = new BigDecimal("27500");

    @Captor
    private ArgumentCaptor<URI> queryCaptor;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private GeneralAppFeesConfiguration feesConfiguration;

    @InjectMocks
    private GeneralAppFeesService feesService;

    @BeforeEach
    void setUp() {
        when(feesConfiguration.getUrl()).thenReturn("dummy_url");
        when(feesConfiguration.getEndpoint()).thenReturn("/fees-register/fees/lookup");
        when(feesConfiguration.getService()).thenReturn("general");
        when(feesConfiguration.getChannel()).thenReturn("default");
        when(feesConfiguration.getEndpoint()).thenReturn("general application");
        when(feesConfiguration.getJurisdiction1()).thenReturn("civil");
        when(feesConfiguration.getJurisdiction2()).thenReturn("civil");
        when(feesConfiguration.getWithNoticeKeyword()).thenReturn("GAOnNotice");
        when(feesConfiguration.getConsentedOrWithoutNoticeKeyword()).thenReturn("GeneralAppWithoutNotice");
    }

    @Test
    void shouldReturnFeeData_whenConsentedApplicationIsBeingMade() {
        when(restTemplate.getForObject(queryCaptor.capture(), eq(FeeLookupResponseDto.class)))
                .thenReturn(FeeLookupResponseDto.builder()
                        .feeAmount(TEST_FEE_AMOUNT_POUNDS_108)
                        .code("test_fee_code")
                        .version(1)
                        .build());

        CaseData caseData = GeneralApplicationDetailsBuilder.builder().getTestCaseDataForApplicationFee(
                CaseDataBuilder.builder().build(), true, false);

        Fee expectedFeeDto = Fee.builder()
                .calculatedAmountInPence(TEST_FEE_AMOUNT_PENCE_108)
                .code("test_fee_code")
                .version("1")
                .build();

        Fee feeDto = feesService.getFeeForGA(caseData);

        assertThat(feeDto).isEqualTo(expectedFeeDto);
        verify(feesConfiguration, times(1)).getConsentedOrWithoutNoticeKeyword();
        verify(feesConfiguration, never()).getWithNoticeKeyword();
        assertThat(queryCaptor.getValue().toString())
                .isEqualTo("dummy_urlgeneral%20application?channel=default&event&jurisdiction1=civil&"
                        + "jurisdiction2=civil&service=general&keyword=GeneralAppWithoutNotice");
    }

    @Test
    void shouldReturnFeeData_whenUnonsentedWithoutNoticeApplicationIsBeingMade() {
        when(restTemplate.getForObject(queryCaptor.capture(), eq(FeeLookupResponseDto.class)))
                .thenReturn(FeeLookupResponseDto.builder()
                        .feeAmount(TEST_FEE_AMOUNT_POUNDS_108)
                        .code("test_fee_code")
                        .version(1)
                        .build());

        CaseData caseData = GeneralApplicationDetailsBuilder.builder().getTestCaseDataForApplicationFee(
                CaseDataBuilder.builder().build(), false, false);

        Fee expectedFeeDto = Fee.builder()
                .calculatedAmountInPence(TEST_FEE_AMOUNT_PENCE_108)
                .code("test_fee_code")
                .version("1")
                .build();

        Fee feeDto = feesService.getFeeForGA(caseData);

        assertThat(feeDto).isEqualTo(expectedFeeDto);
        verify(feesConfiguration, times(1)).getConsentedOrWithoutNoticeKeyword();
        verify(feesConfiguration, never()).getWithNoticeKeyword();
        assertThat(queryCaptor.getValue().toString())
                .isEqualTo("dummy_urlgeneral%20application?channel=default&event&jurisdiction1=civil&"
                        + "jurisdiction2=civil&service=general&keyword=GeneralAppWithoutNotice");
    }

    @Test
    void shouldReturnFeeData_whenUnconsentedNotifiedApplicationIsBeingMade() {
        when(restTemplate.getForObject(queryCaptor.capture(), eq(FeeLookupResponseDto.class)))
                .thenReturn(FeeLookupResponseDto.builder()
                        .feeAmount(TEST_FEE_AMOUNT_POUNDS_275)
                        .code("test_fee_code")
                        .version(1)
                        .build());

        CaseData caseData = GeneralApplicationDetailsBuilder.builder().getTestCaseDataForApplicationFee(
                CaseDataBuilder.builder().build(), false, true);

        Fee expectedFeeDto = Fee.builder()
                .calculatedAmountInPence(TEST_FEE_AMOUNT_PENCE_275)
                .code("test_fee_code")
                .version("1")
                .build();

        Fee feeDto = feesService.getFeeForGA(caseData);

        assertThat(feeDto).isEqualTo(expectedFeeDto);
        verify(feesConfiguration, times(1)).getWithNoticeKeyword();
        verify(feesConfiguration, never()).getConsentedOrWithoutNoticeKeyword();
        assertThat(queryCaptor.getValue().toString())
                .isEqualTo("dummy_urlgeneral%20application?channel=default&event&jurisdiction1=civil&"
                        + "jurisdiction2=civil&service=general&keyword=GAOnNotice");
    }

    @Test
    void throwRuntimeException_whenFeeServiceThrowsException() {
        when(restTemplate.getForObject(queryCaptor.capture(), eq(FeeLookupResponseDto.class)))
                .thenThrow(new RuntimeException("Some Exception"));

        CaseData caseData = GeneralApplicationDetailsBuilder.builder().getTestCaseDataForApplicationFee(
                CaseDataBuilder.builder().build(), false, true);

        Exception exception = assertThrows(RuntimeException.class, () -> feesService.getFeeForGA(caseData));

        assertThat(exception.getMessage()).isEqualTo("java.lang.RuntimeException: Some Exception");
    }

    @Test
    void throwRuntimeException_whenNoFeeIsReturnedByFeeService() {
        when(restTemplate.getForObject(queryCaptor.capture(), eq(FeeLookupResponseDto.class)))
                .thenReturn(null);

        CaseData caseData = GeneralApplicationDetailsBuilder.builder().getTestCaseDataForApplicationFee(
                CaseDataBuilder.builder().build(), false, true);

        Exception exception = assertThrows(RuntimeException.class, () -> feesService.getFeeForGA(caseData));

        assertThat(exception.getMessage())
                .isEqualTo("No Fees returned by fee-service while creating General Application");
    }

    @Test
    void throwRuntimeException_whenNoFeeAmountIsReturnedByFeeService() {
        when(restTemplate.getForObject(queryCaptor.capture(), eq(FeeLookupResponseDto.class)))
                .thenReturn(FeeLookupResponseDto.builder()
                        .code("test_fee_code")
                        .version(1)
                        .build());

        CaseData caseData = GeneralApplicationDetailsBuilder.builder().getTestCaseDataForApplicationFee(
                CaseDataBuilder.builder().build(), false, true);

        Exception exception = assertThrows(RuntimeException.class, () -> feesService.getFeeForGA(caseData));

        assertThat(exception.getMessage())
                .isEqualTo("No Fees returned by fee-service while creating General Application");
    }
}
