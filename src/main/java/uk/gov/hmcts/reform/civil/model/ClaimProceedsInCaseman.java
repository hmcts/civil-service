package uk.gov.hmcts.reform.civil.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.civil.enums.ReasonForProceedingOnPaper;
import uk.gov.hmcts.reform.civil.validation.groups.CasemanTransferDateGroup;

import java.time.LocalDate;
import jakarta.validation.constraints.PastOrPresent;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ClaimProceedsInCaseman {

    @PastOrPresent(message = "The date entered cannot be in the future", groups = CasemanTransferDateGroup.class)
    private LocalDate date;
    private ReasonForProceedingOnPaper reason;
    private String other;
}
