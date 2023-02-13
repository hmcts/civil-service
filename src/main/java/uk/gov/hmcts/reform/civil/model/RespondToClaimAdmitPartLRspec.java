package uk.gov.hmcts.reform.civil.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.civil.model.common.MappableObject;
import uk.gov.hmcts.reform.civil.validation.groups.PaymentDateGroup;

import java.time.LocalDate;
import javax.validation.constraints.FutureOrPresent;

@Data
@Builder(toBuilder = true)
public class RespondToClaimAdmitPartLRspec implements MappableObject {

    @FutureOrPresent(message = "Date for when will the amount be paid must be today or in the future.",
        groups = PaymentDateGroup.class)
    private final LocalDate whenWillThisAmountBePaid;
    private final String formattedWhenWillThisAmountBePaid;

    @JsonCreator
    public RespondToClaimAdmitPartLRspec(@JsonProperty("whenWillThisAmountBePaid") LocalDate whenWillThisAmountBePaid,
                                         @JsonProperty("formattedWhenWillThisAmountBePaid") String formattedWhenWillThisAmountBePaid) {
        this.whenWillThisAmountBePaid = whenWillThisAmountBePaid;
        this.formattedWhenWillThisAmountBePaid = formattedWhenWillThisAmountBePaid;
    }
}
