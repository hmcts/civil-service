package uk.gov.hmcts.reform.civil.model.documents;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;

import java.time.LocalDateTime;
import java.time.ZoneId;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class DocumentAndNote {

    @CCD(label = "Name of document", searchable = false)
    private String documentName;
    @CCD(
            label = "Document",
            regex = ".pdf,.txt,.doc,.dot,.docx,.rtf,.xls,.xlt,.xla,.xlsx,.xltx,.xlsb,.ppt,.pot,.pps,.ppa,.pptx,.potx,.ppsx,.jpg,.jpeg,.bmp,.tif,.tiff,.png",
            searchable = false
    )
    private Document document;
    @CCD(label = "Add a note about this document", searchable = false, typeOverride = FieldType.TextArea)
    private String documentNote;
    @CCD(label = "Date added", showCondition = "label = \"DO_NOT_SHOW\"", searchable = false)
    private LocalDateTime createdDateTime = LocalDateTime.now(ZoneId.of("Europe/London"));
    @CCD(label = "Created by", showCondition = "label = \"DO_NOT_SHOW\"", searchable = false)
    private String createdBy;
    @CCD(
            label = "Note",
            showCondition = "label = \"DO_NOT_SHOW\"",
            searchable = false,
            typeOverride = FieldType.TextArea
    )
    private String documentNoteForTab;

  // ==== ccd-definition-converter: synthesised definition-only fields (retrofit) ====
  @CCD(label = "Case note", searchable = false, typeOverride = FieldType.Label)
  private String label;
  // ==== end synthesised definition-only fields ====
}
