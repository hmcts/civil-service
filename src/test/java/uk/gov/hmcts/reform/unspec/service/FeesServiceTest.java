package uk.gov.hmcts.reform.unspec.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fees.client.FeesClient;
import uk.gov.hmcts.reform.fees.client.model.FeeLookupResponseDto;
import uk.gov.hmcts.reform.unspec.config.FeesConfiguration;
import uk.gov.hmcts.reform.unspec.model.ClaimValue;
import uk.gov.hmcts.reform.unspec.model.Fee;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(SpringExtension.class)
class FeesServiceTest {

    private static final String CHANNEL = "channel";
    private static final String EVENT = "event";
    private static final BigDecimal TEST_FEE_AMOUNT_POUNDS = new BigDecimal("1.00");
    private static final BigDecimal TEST_FEE_AMOUNT_PENCE = new BigDecimal("100");

    @Mock
    private FeesClient feesClient;

    @Mock
    private FeesConfiguration feesConfiguration;

    @InjectMocks
    private FeesService feesService;

    @BeforeEach
    void setUp() {
        given(feesClient.lookupFee(any(), any(), eq(new BigDecimal("50.00"))))
            .willReturn(FeeLookupResponseDto.builder()
                            .feeAmount(TEST_FEE_AMOUNT_POUNDS)
                            .code("test_fee_code")
                            .version(1)
                            .build());
        given(feesConfiguration.getChannel()).willReturn(CHANNEL);
        given(feesConfiguration.getEvent()).willReturn(EVENT);
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
}
