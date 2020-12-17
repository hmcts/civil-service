package uk.gov.hmcts.reform.unspec.model;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.payments.client.models.FeeDto;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static org.assertj.core.api.Assertions.assertThat;

class FeeTest {

    @Test
    void shouldConvertCalculateAmountInPenceToPounds_whenFeeIsConvertedToFeeDto() {
        Fee fee = Fee.builder()
            .calculatedAmountInPence(BigDecimal.valueOf(100))
            .version("1")
            .code("1")
            .build();

        assertThat(fee.toFeeDto()).isEqualTo(
            FeeDto.builder()
                .calculatedAmount(BigDecimal.valueOf(1.0).setScale(2, RoundingMode.CEILING))
                .version("1")
                .code("1")
                .build());
    }

}
