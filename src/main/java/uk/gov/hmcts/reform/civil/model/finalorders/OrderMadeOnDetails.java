package uk.gov.hmcts.reform.civil.model.finalorders;

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
public class OrderMadeOnDetails {

    @CCD(label = " ", searchable = false, typeOverride = FieldType.TextArea)
    private String ownInitiativeText;
    @CCD(label = " ", hint = "For example, 16 4 2021", searchable = false)
    private LocalDate ownInitiativeDate;

  // ==== ccd-definition-converter: synthesised definition-only fields (retrofit) ====
  @CCD(label = "### Order on court's own initiative", hint = " ", searchable = false, typeOverride = FieldType.Label)
  private String ownInitiativeLabel;
  @CCD(label = "### Please enter date", hint = " ", searchable = false, typeOverride = FieldType.Label)
  private String ownInitiativeDateLabel;
  // ==== end synthesised definition-only fields ====
}
