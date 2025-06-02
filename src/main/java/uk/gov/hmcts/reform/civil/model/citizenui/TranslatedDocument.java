package uk.gov.hmcts.reform.civil.model.citizenui;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.civil.documentmanagement.DocumentUploadException;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType;

import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.COURT_OFFICER_ORDER_TRANSLATED_DOCUMENT;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.DEFENCE_TRANSLATED_DOCUMENT;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.FINAL_ORDER_TRANSLATED_DOCUMENT;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.MANUAL_DETERMINATION_TRANSLATED_DOCUMENT;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.CLAIM_ISSUE_TRANSLATED_DOCUMENT;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.CLAIMANT_INTENTION_TRANSLATED_DOCUMENT;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.INTERLOC_JUDGMENT_TRANSLATED_DOCUMENT;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.ORDER_NOTICE_TRANSLATED_DOCUMENT;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.SDO_TRANSLATED_DOCUMENT;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.SETTLEMENT_AGREEMENT_TRANSLATED_DOCUMENT;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.TRANSLATED_HEARING_NOTICE;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TranslatedDocument {

    private Document file;
    private TranslatedDocumentType documentType;

    @JsonIgnore
    public DocumentType getCorrespondingDocumentType(TranslatedDocumentType documentType) {
        return switch (documentType) {
            case DEFENDANT_RESPONSE -> DEFENCE_TRANSLATED_DOCUMENT;
            case CLAIM_ISSUE -> CLAIM_ISSUE_TRANSLATED_DOCUMENT;
            case CLAIMANT_INTENTION -> CLAIMANT_INTENTION_TRANSLATED_DOCUMENT;
            case ORDER_NOTICE -> ORDER_NOTICE_TRANSLATED_DOCUMENT;
            case STANDARD_DIRECTION_ORDER -> SDO_TRANSLATED_DOCUMENT;
            case INTERLOCUTORY_JUDGMENT -> INTERLOC_JUDGMENT_TRANSLATED_DOCUMENT;
            case MANUAL_DETERMINATION -> MANUAL_DETERMINATION_TRANSLATED_DOCUMENT;
            case FINAL_ORDER -> FINAL_ORDER_TRANSLATED_DOCUMENT;
            case COURT_OFFICER_ORDER -> COURT_OFFICER_ORDER_TRANSLATED_DOCUMENT;
            case SETTLEMENT_AGREEMENT -> SETTLEMENT_AGREEMENT_TRANSLATED_DOCUMENT;
            case HEARING_NOTICE -> TRANSLATED_HEARING_NOTICE;
            default -> throw new DocumentUploadException("No document file type found for Translated document");
        };
    }
}
