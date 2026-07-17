package uk.gov.hmcts.reform.civil.model.sdo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SdoR2SmallClaimsUploadDoc {

    @CCD(label = " ", searchable = false, typeOverride = FieldType.TextArea)
    private String sdoUploadOfDocumentsTxt;

  // ==== ccd-definition-converter: synthesised definition-only fields (retrofit) ====
  @CCD(label = "### Upload of documents to be relied upon", searchable = false, typeOverride = FieldType.Label)
  private String sdoUploadOfDocumentsLbl;
  // ==== end synthesised definition-only fields ====
}
