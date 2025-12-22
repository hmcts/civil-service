package uk.gov.hmcts.reform.civil.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import uk.gov.hmcts.reform.civil.model.common.MappableObject;
import uk.gov.hmcts.reform.civil.validation.groups.PaymentDateGroup;

import java.time.LocalDate;
import jakarta.validation.constraints.FutureOrPresent;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
public class RespondToClaimAdmitPartLRspec implements MappableObject {

    @FutureOrPresent(message = "Date for when will the amount be paid must be today or in the future.",
        groups = PaymentDateGroup.class)
    private LocalDate whenWillThisAmountBePaid;

    @JsonCreator
    public RespondToClaimAdmitPartLRspec(@JsonProperty("whenWillThisAmountBePaid") LocalDate whenWillThisAmountBePaid) {
        this.whenWillThisAmountBePaid = whenWillThisAmountBePaid;
    }
}
