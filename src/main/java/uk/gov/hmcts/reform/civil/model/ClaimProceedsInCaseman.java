package uk.gov.hmcts.reform.civil.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import uk.gov.hmcts.reform.civil.enums.ReasonForProceedingOnPaper;
import uk.gov.hmcts.reform.civil.validation.groups.CasemanTransferDateGroup;

import java.time.LocalDate;
import jakarta.validation.constraints.PastOrPresent;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class ClaimProceedsInCaseman {

    @CCD(
            label = "Date when claim was transferred to Caseman",
            hint = "The date entered can be today's or a previous date.",
            searchable = false
    )
    @PastOrPresent(message = "The date entered cannot be in the future", groups = CasemanTransferDateGroup.class)
    private LocalDate date;
    @CCD(label = "Reason for proceeding on paper", searchable = false)
    private ReasonForProceedingOnPaper reason;
    @CCD(
            label = "Other reason",
            showCondition = "reason = \"OTHER\"",
            searchable = false,
            typeOverride = FieldType.TextArea
    )
    private String other;

  // ==== ccd-definition-converter: synthesised definition-only fields (retrofit) ====
  @CCD(
          label = "You are taking the claim offline. The claim will now continue by post.",
          searchable = false,
          typeOverride = FieldType.Label
  )
  private String label;
  // ==== end synthesised definition-only fields ====
}