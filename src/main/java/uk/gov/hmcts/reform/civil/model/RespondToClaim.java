package uk.gov.hmcts.reform.civil.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.civil.validation.groups.PaymentDateGroup;

import java.math.BigDecimal;
import java.time.LocalDate;
import javax.validation.constraints.PastOrPresent;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RespondToClaim {

    /**
     * money amount in pence.
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal howMuchWasPaid;
    @PastOrPresent(message = "Date for when amount was paid must be today or in the past",
        groups = PaymentDateGroup.class)
    private LocalDate whenWasThisAmountPaid;
    private PaymentMethod howWasThisAmountPaid;
    private String howWasThisAmountPaidOther;

    @JsonIgnore
    public String getExplanationOnHowTheAmountWasPaid() {
        return getHowWasThisAmountPaid() == PaymentMethod.OTHER
            ? getHowWasThisAmountPaidOther()
            : getHowWasThisAmountPaid().getHumanFriendly();
    }
}
