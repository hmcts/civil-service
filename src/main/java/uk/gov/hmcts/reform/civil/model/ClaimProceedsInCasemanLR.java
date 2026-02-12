package uk.gov.hmcts.reform.civil.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import uk.gov.hmcts.reform.civil.enums.ReasonForProceedingOnPaper;
import uk.gov.hmcts.reform.civil.validation.groups.CasemanTransferDateGroup;

import jakarta.validation.constraints.PastOrPresent;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@Accessors(chain = true)
public class ClaimProceedsInCasemanLR {

    @PastOrPresent(message = "The date entered cannot be in the future", groups = CasemanTransferDateGroup.class)
    private LocalDate date;
    private ReasonForProceedingOnPaper reason;
    private String other;
}