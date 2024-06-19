package uk.gov.hmcts.reform.civil.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.civil.config.FeesConfiguration;
import uk.gov.hmcts.reform.civil.model.ClaimValue;
import uk.gov.hmcts.reform.civil.model.Fee;
import uk.gov.hmcts.reform.fees.client.FeesClient;
import uk.gov.hmcts.reform.fees.client.model.Fee2Dto;
import uk.gov.hmcts.reform.fees.client.model.FeeLookupResponseDto;
import uk.gov.hmcts.reform.fees.client.model.FeeVersionDto;
import uk.gov.hmcts.reform.fees.client.model.FlatAmountDto;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(SpringExtension.class)
class FeesServiceTest {

    private static final String CHANNEL = "channel";
    private static final String EVENT = "event";
    private static final String HEARING_EVENT = "hearingEvent";
    private static final BigDecimal TEST_FEE_AMOUNT_POUNDS = new BigDecimal("1.00");
    private static final BigDecimal TEST_FEE_AMOUNT_PENCE = new BigDecimal("100");
    private static final BigDecimal MIN_RANGE = new BigDecimal("0.01");
    private static final BigDecimal MAX_RANGE = new BigDecimal("300");

    @Mock
    private FeesClient feesClient;

    @Mock
    private FeesConfiguration feesConfiguration;

    @InjectMocks
    private FeesService feesService;

    @BeforeEach
    void setUp() {
        given(feesClient.lookupFee(any(), any(), any()))
            .willReturn(FeeLookupResponseDto.builder()
                            .feeAmount(TEST_FEE_AMOUNT_POUNDS)
                            .code("test_fee_code")
                            .version(1)
                            .build());
        given(feesClient.findRangeGroup(any(), any())).willReturn(buildFeeRangeResponse());
        given(feesConfiguration.getChannel()).willReturn(CHANNEL);
        given(feesConfiguration.getEvent()).willReturn(EVENT);
        given(feesConfiguration.getHearingEvent()).willReturn(HEARING_EVENT);
    }

    @Test
    void shouldReturnFeeData_whenValidClaimValue() {
        var claimValue = ClaimValue.builder()
            .statementOfValueInPennies(BigDecimal.valueOf(5000))
            .build();

        Fee expectedFeeDto = Fee.builder()
            .calculatedAmountInPence(TEST_FEE_AMOUNT_PENCE)
            .code("test_fee_code")
            .version("1")
            .build();

        Fee feeDto = feesService.getFeeDataByClaimValue(claimValue);

        verify(feesClient).lookupFee(CHANNEL, EVENT, new BigDecimal("50.00"));
        assertThat(feeDto).isEqualTo(expectedFeeDto);
    }

    @Test
    void shouldReturnFeeDataByTotalClaimAmount() {

        Fee expectedFeeDto = Fee.builder()
            .calculatedAmountInPence(TEST_FEE_AMOUNT_PENCE)
            .code("test_fee_code")
            .version("1")
            .build();

        Fee feeDto = feesService.getFeeDataByTotalClaimAmount(new BigDecimal("7000.00"));

        verify(feesClient).lookupFee(CHANNEL, EVENT, new BigDecimal("7000.00"));
        assertThat(feeDto).isEqualTo(expectedFeeDto);
    }

    @Test
    void shouldReturnHearingFeeData() {
        Fee expectedFeeDto = Fee.builder()
            .calculatedAmountInPence(TEST_FEE_AMOUNT_PENCE)
            .code("test_fee_code")
            .version("1")
            .build();

        Fee feeDto = feesService.getHearingFeeDataByTotalClaimAmount(new BigDecimal("7000.00"));

        verify(feesClient).lookupFee(CHANNEL, HEARING_EVENT, new BigDecimal("7000.00"));
        assertThat(feeDto).isEqualTo(expectedFeeDto);
    }

    @Test
    void shouldReturnFeeRangeSuccessfully() {
        Fee2Dto[] expectedResult = buildFeeRangeResponse();
        Fee2Dto[] feeRange = feesService.getFeeRange().toArray(new Fee2Dto[0]);
        verify(feesClient).findRangeGroup(CHANNEL, EVENT);
        assertThat(feeRange).isEqualTo(expectedResult);
    }

    private Fee2Dto[] buildFeeRangeResponse() {
        return new Fee2Dto[]{Fee2Dto
            .builder()
            .minRange(MIN_RANGE)
            .maxRange(MAX_RANGE)
            .currentVersion(FeeVersionDto
                                .builder()
                                .flatAmount(FlatAmountDto
                                                .builder()
                                                .amount(TEST_FEE_AMOUNT_POUNDS)
                                                .build())
                                .build())
            .build()};
    }
}
