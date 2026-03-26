package uk.gov.hmcts.reform.civil.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import uk.gov.hmcts.reform.civil.validation.groups.ClaimWithdrawalDateGroup;

import java.time.LocalDate;
import jakarta.validation.constraints.PastOrPresent;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class CloseClaim {

    @PastOrPresent(message = "The date must not be in the future", groups = ClaimWithdrawalDateGroup.class)
    private LocalDate date;
    private String reason;
}