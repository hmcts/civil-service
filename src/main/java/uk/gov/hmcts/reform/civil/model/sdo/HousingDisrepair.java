package uk.gov.hmcts.reform.civil.model.sdo;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class HousingDisrepair {

    @CCD(label = " ", searchable = false, typeOverride = FieldType.TextArea)
    private String clauseA;
    @CCD(label = " ", searchable = false, typeOverride = FieldType.TextArea)
    private String clauseB;
    @CCD(label = " ", searchable = false)
    private LocalDate firstReportDateBy;
    @CCD(label = " ", searchable = false, typeOverride = FieldType.TextArea)
    private String clauseCBeforeDate;
    @CCD(label = " ", searchable = false)
    private LocalDate jointStatementDateBy;
    @CCD(label = " ", searchable = false, typeOverride = FieldType.TextArea)
    private String clauseCAfterDate;
    @CCD(label = " ", searchable = false, typeOverride = FieldType.TextArea)
    private String clauseD;
    @CCD(label = " ", searchable = false, typeOverride = FieldType.TextArea)
    private String clauseE;

    @CCD(ignore = true)
    private String input1;
    @CCD(ignore = true)
    private String input2;
    @CCD(ignore = true)
    private String input3;
    @CCD(ignore = true)
    private LocalDate date1;
    @CCD(ignore = true)
    private String input4;
    @CCD(ignore = true)
    private LocalDate date2;

  // ==== ccd-definition-converter: synthesised definition-only fields (retrofit) ====
  @CCD(label = "Expert evidence is directed as follows:", searchable = false, typeOverride = FieldType.Label)
  private String introLine;
  // ==== end synthesised definition-only fields ====
}
