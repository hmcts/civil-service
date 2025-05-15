package uk.gov.hmcts.reform.civil.model.citizenui;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.civil.documentmanagement.DocumentUploadException;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType;

import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.DEFENCE_TRANSLATED_DOCUMENT;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.CLAIM_ISSUE_TRANSLATED_DOCUMENT;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.CLAIMANT_INTENTION_TRANSLATED_DOCUMENT;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.INTERLOC_JUDGMENT_TRANSLATED_DOCUMENT;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.ORDER_NOTICE_TRANSLATED_DOCUMENT;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.SDO_TRANSLATED_DOCUMENT;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TranslatedDocument {

    private Document file;
    private TranslatedDocumentType documentType;

    @JsonIgnore
    public DocumentType getCorrespondingDocumentType(TranslatedDocumentType documentType) {
        switch (documentType) {
            case DEFENDANT_RESPONSE : return DEFENCE_TRANSLATED_DOCUMENT;
            case CLAIM_ISSUE : return CLAIM_ISSUE_TRANSLATED_DOCUMENT;
            case CLAIMANT_INTENTION : return CLAIMANT_INTENTION_TRANSLATED_DOCUMENT;
            case ORDER_NOTICE : return ORDER_NOTICE_TRANSLATED_DOCUMENT;
            case STANDARD_DIRECTION_ORDER: return SDO_TRANSLATED_DOCUMENT;
            case INTERLOCUTORY_JUDGMENT: return INTERLOC_JUDGMENT_TRANSLATED_DOCUMENT;
            default: throw new DocumentUploadException("No document file type found for Translated document");
        }
    }
}
