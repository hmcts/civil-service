package uk.gov.hmcts.reform.unspec.model;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.unspec.validation.groups.ClaimWithdrawalDateGroup;

import java.time.LocalDate;
import javax.validation.constraints.PastOrPresent;

@Data
@Builder
public class CloseClaim {

    @PastOrPresent(message = "The date must not be in the future", groups = ClaimWithdrawalDateGroup.class)
    private LocalDate date;
    private String reason;
}
