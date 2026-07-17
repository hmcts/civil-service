package uk.gov.hmcts.reform.civil.model.sdo;

import jakarta.validation.constraints.Future;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.LocalDate;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class FastTrackWitnessOfFact {

    @CCD(label = " ", searchable = false, typeOverride = FieldType.TextArea)
    private String input1;
    @CCD(label = " ", searchable = false)
    private String input2;
    @CCD(label = " ", searchable = false)
    private String input3;
    @CCD(label = " ", searchable = false)
    private String input4;
    @CCD(label = " ", searchable = false)
    private String input5;
    @CCD(label = " ", searchable = false)
    private String input6;
    @CCD(label = " ", searchable = false)
    private String input7;
    @CCD(label = " ", searchable = false)
    private String input8;
    @CCD(label = " ", searchable = false)
    @Future(message = "The date entered must be in the future")
    private LocalDate date;
    @CCD(label = " ", searchable = false, typeOverride = FieldType.TextArea)
    private String input9;

  // ==== ccd-definition-converter: synthesised definition-only fields (retrofit) ====
  @CCD(label = " ", searchable = false, typeOverride = FieldType.Label)
  private String label;
  // ==== end synthesised definition-only fields ====
}
