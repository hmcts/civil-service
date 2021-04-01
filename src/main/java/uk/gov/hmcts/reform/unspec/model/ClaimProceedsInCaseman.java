package uk.gov.hmcts.reform.unspec.model;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.unspec.enums.ReasonForProceedingOnPaper;
import uk.gov.hmcts.reform.unspec.validation.groups.CasemanTransferDateGroup;

import java.time.LocalDate;
import javax.validation.constraints.PastOrPresent;

@Data
@Builder
public class ClaimProceedsInCaseman {

    @PastOrPresent(message = "The date entered cannot be in the future", groups = CasemanTransferDateGroup.class)
    private LocalDate date;
    private ReasonForProceedingOnPaper reason;
    private String other;
}
