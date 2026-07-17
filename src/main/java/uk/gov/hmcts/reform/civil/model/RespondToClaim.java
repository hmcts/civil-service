package uk.gov.hmcts.reform.civil.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import uk.gov.hmcts.reform.civil.validation.groups.PaymentDateGroup;

import java.math.BigDecimal;
import java.time.LocalDate;
import jakarta.validation.constraints.PastOrPresent;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class RespondToClaim {

    /**
     * money amount in pence.
     */
    @CCD(label = "How much was paid?", searchable = false, typeOverride = FieldType.MoneyGBP)
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal howMuchWasPaid;
    @CCD(label = "When was this amount paid?", hint = "For example, 12 11 2007", searchable = false)
    @PastOrPresent(message = "Date for when amount was paid must be today or in the past",
        groups = PaymentDateGroup.class)
    private LocalDate whenWasThisAmountPaid;
    @CCD(label = "How was this amount paid?", searchable = false)
    private PaymentMethod howWasThisAmountPaid;
    @CCD(label = "Tell us how", searchable = false)
    private String howWasThisAmountPaidOther;

    @JsonIgnore
    public String getExplanationOnHowTheAmountWasPaid() {
        return getHowWasThisAmountPaid() == PaymentMethod.OTHER
            ? getHowWasThisAmountPaidOther()
            : getHowWasThisAmountPaid().getHumanFriendly();
    }

  // ==== ccd-definition-converter: synthesised definition-only fields (retrofit) ====
  @CCD(
          label = "The total amount claimed is £${totalClaimAmount}. This includes the claim fee and any interest. \n",
          searchable = false,
          typeOverride = FieldType.Label
  )
  private String howMuchWasPaidLabel;
  @CCD(
          label = "The total amount claimed is £${totalClaimAmountPlusInterestAdmitPartString}.\n\nThis amount includes any interest claimed but not the claim fee and any fixed costs claimed.\n",
          searchable = false,
          typeOverride = FieldType.Label
  )
  private String howMuchWasPaidWithInterestLabel;
  // ==== end synthesised definition-only fields ====
}
