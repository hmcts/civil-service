package uk.gov.hmcts.reform.civil.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
public class PaymentBySetDate {

    private LocalDate paymentSetDate;

    @JsonCreator
    public PaymentBySetDate(@JsonProperty("paymentSetDate") LocalDate paymentSetDate) {
        this.paymentSetDate = paymentSetDate;
    }
}
