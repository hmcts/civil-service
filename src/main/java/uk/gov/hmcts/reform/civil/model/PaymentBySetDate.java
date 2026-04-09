package uk.gov.hmcts.reform.civil.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@Accessors(chain = true)
public class PaymentBySetDate {

    private LocalDate paymentSetDate;

    @JsonCreator
    public PaymentBySetDate(@JsonProperty("paymentSetDate") LocalDate paymentSetDate) {
        this.paymentSetDate = paymentSetDate;
    }
}
