package uk.gov.hmcts.reform.civil.model.citizenui;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;

import java.time.LocalDateTime;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "StaffDocument", generate = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class ManageDocument {

    @CCD(
            label = "Document URL",
            regex = ".pdf,.txt,.doc,.dot,.docx,.rtf,.xls,.xlt,.xla,.xlsx,.xltx,.xlsb,.ppt,.pot,.pps,.ppa,.pptx,.potx,.ppsx,.jpg,.jpeg,.bmp,.tif,.tiff,.png",
            searchable = false
    )
    private Document documentLink;
    @CCD(label = "Document Name", searchable = false)
    private String documentName;
    @CCD(
            label = "Document Type",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "StaffDocumentType"
    )
    private ManageDocumentType documentType;
    @CCD(label = "What type of document is it?", showCondition = "documentType=\"OTHER\"", searchable = false)
    private String documentTypeOther;
    @CCD(label = "Uploaded on", searchable = false)
    private LocalDateTime createdDatetime = LocalDateTime.now();

  // ==== ccd-definition-converter: synthesised definition-only fields (retrofit) ====
  @CCD(label = "Document size", searchable = false)
  private Integer documentSize;
  // ==== end synthesised definition-only fields ====
}
