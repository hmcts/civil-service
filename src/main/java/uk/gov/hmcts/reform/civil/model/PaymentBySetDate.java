package uk.gov.hmcts.reform.civil.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.LocalDate;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@Data
@NoArgsConstructor
@Accessors(chain = true)
public class PaymentBySetDate {

    @CCD(label = " ", hint = "For example, 12 11 2023", searchable = false)
    private LocalDate paymentSetDate;

    @JsonCreator
    public PaymentBySetDate(@JsonProperty("paymentSetDate") LocalDate paymentSetDate) {
        this.paymentSetDate = paymentSetDate;
    }
}
