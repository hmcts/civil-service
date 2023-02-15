package uk.gov.hmcts.reform.cmc.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.minidev.json.annotate.JsonIgnore;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PaymentIntention {

    private PaymentOption paymentOption;
    private LocalDate paymentDate;

    @JsonIgnore
    public boolean isPayImmediately() {
        return paymentOption == PaymentOption.IMMEDIATELY;
    }

    @JsonIgnore
    public boolean isPayByDate() {
        return paymentOption == PaymentOption.BY_SPECIFIED_DATE;
    }

    @JsonIgnore
    public boolean isPayByInstallments() {
        return paymentOption == PaymentOption.INSTALMENTS;
    }

}
