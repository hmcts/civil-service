package uk.gov.hmcts.reform.civil.model.citizenui;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.civil.documentmanagement.DocumentUploadException;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType;

import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.CLAIM_ISSUE_TRANSLATED_DOCUMENT;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.DEFENCE_TRANSLATED_DOCUMENT;
@Slf4j
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TranslatedDocument {

    private Document file;
    private TranslatedDocumentType documentType;

    @JsonIgnore
    public DocumentType getCorrespondingDocumentType(TranslatedDocumentType documentType) {
        log.info("-------------------getCorrespondingDocumentType-------------------------");
        log.info(documentType.toString());
        log.info(documentType.name().toString());
        switch (documentType) {
            case DEFENDANT_RESPONSE : {
                log.info("DEFENDANT_RESPONSE ---------");
                return DEFENCE_TRANSLATED_DOCUMENT;
            }
            case CLAIM_ISSUE : {
                log.info("CLAIM_ISSUE ---------");
                return CLAIM_ISSUE_TRANSLATED_DOCUMENT;
            }
            default: throw new DocumentUploadException("No document file type found for Translated document");
        }
    }
}
