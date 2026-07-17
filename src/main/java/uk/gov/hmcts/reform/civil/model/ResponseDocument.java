package uk.gov.hmcts.reform.civil.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Data;
import lombok.NoArgsConstructor;

import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@Data
@NoArgsConstructor
public class ResponseDocument {

    @CCD(
            label = "Defendant's defence",
            hint = "The defendant's defence must be in a document in the following format: pdf, txt, doc, dot, docx, rtf, xls, xlsx, jpg, jpeg or png with a maximum size of 10MB. It must also contain the following statement of truth: \"I believe the facts set out above to be true. I understand that proceedings for contempt of court may be brought against anyone who makes, or causes to be made, a false statement in a document verified by a statement of truth without an honest belief in its truth.\"",
            regex = ".pdf,.txt,.doc,.dot,.docx,.rtf,.xls,.xlt,.xla,.xlsx,.xltx,.xlsb,.ppt,.pot,.pps,.ppa,.pptx,.potx,.ppsx,.jpg,.jpeg,.bmp,.tif,.tiff,.png",
            searchable = false
    )
    private Document file;

    @JsonCreator
    public ResponseDocument(Document file) {
        this.file = file;
    }
}
