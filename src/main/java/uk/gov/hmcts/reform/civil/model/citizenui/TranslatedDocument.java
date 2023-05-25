package uk.gov.hmcts.reform.civil.model.citizenui;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.civil.documentmanagement.DocumentUploadException;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType;

import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.DEFENDANT_DEFENCE;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TranslatedDocument {

    private Document file;
    private TranslatedDocumentType documentType;

    @JsonIgnore
    public DocumentType getCorrespondingDocumentType() {
        switch (documentType) {
            case DEFENDANT_RESPONSE : return DEFENDANT_DEFENCE;
            default: throw new DocumentUploadException("No document file type found for Translated document");
        }
    }
}
