package uk.gov.hmcts.reform.civil.model.finalorders;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import uk.gov.hmcts.reform.civil.enums.finalorders.AppealList;
import uk.gov.hmcts.reform.civil.enums.finalorders.ApplicationAppealList;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Accessors(chain = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FinalOrderAppeal {

    @CCD(label = " ", searchable = false)
    private AppealList list;
    @CCD(label = "Please specify", showCondition = "list = \"OTHER\"", searchable = false)
    private String otherText;
    @CCD(label = " ", searchable = false)
    private ApplicationAppealList applicationList;
    @CCD(label = " ", showCondition = "applicationList = \"GRANTED\"", searchable = false)
    private AppealGrantedRefused appealGrantedDropdown;
    @CCD(label = " ", showCondition = "applicationList = \"REFUSED\"", searchable = false)
    private AppealGrantedRefused appealRefusedDropdown;

  // ==== ccd-definition-converter: synthesised definition-only fields (retrofit) ====
  @CCD(label = "### The", searchable = false, typeOverride = FieldType.Label)
  private String label;
  @CCD(label = "### application for permission to appeal is", searchable = false, typeOverride = FieldType.Label)
  private String applicationLabel;
  // ==== end synthesised definition-only fields ====
}
