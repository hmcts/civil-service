package uk.gov.hmcts.reform.civil.model;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.civil.validation.groups.PaymentDateGroup;

import java.time.LocalDate;
import javax.validation.constraints.FutureOrPresent;


@Data
@Builder
public class RespondToClaimAdmitPartLRspec {

    @FutureOrPresent(message = "Date for when will the amount be paid must be today or in the future.",
        groups = PaymentDateGroup.class)
    private final LocalDate whenWillThisAmountBePaid;
}
