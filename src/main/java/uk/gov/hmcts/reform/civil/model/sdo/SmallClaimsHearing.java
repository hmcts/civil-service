package uk.gov.hmcts.reform.civil.model.sdo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.civil.enums.sdo.SmallClaimsTimeEstimate;

import java.math.BigDecimal;
import java.time.LocalDate;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class SmallClaimsHearing {

    @CCD(label = " ", searchable = false, typeOverride = FieldType.TextArea)
    private String input1;
    @CCD(label = "The time estimate is", searchable = false)
    private SmallClaimsTimeEstimate time;
    @CCD(label = "Hour(s)", showCondition = "time = \"OTHER\"", searchable = false)
    private BigDecimal otherHours;
    @CCD(label = "Minute(s)", showCondition = "time = \"OTHER\"", searchable = false, max = 59)
    private BigDecimal otherMinutes;
    @CCD(label = " ", searchable = false, typeOverride = FieldType.TextArea)
    private String input2;
    @CCD(label = " ", searchable = false)
    private LocalDate dateFrom;
    @CCD(label = " ", searchable = false)
    private LocalDate dateTo;

  // ==== ccd-definition-converter: synthesised definition-only fields (retrofit) ====
  @CCD(label = " ", searchable = false, typeOverride = FieldType.Label)
  private String label;
  @CCD(label = "Date to", searchable = false, typeOverride = FieldType.Label)
  private String dateToLabel;
  @CCD(label = "Date from", searchable = false, typeOverride = FieldType.Label)
  private String dateFromLabel;
  // ==== end synthesised definition-only fields ====
}
