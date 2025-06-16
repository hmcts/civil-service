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
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.DECISION_MADE_ON_APPLICATIONS_TRANSLATED;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.FINAL_ORDER_TRANSLATED_DOCUMENT;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.MANUAL_DETERMINATION_TRANSLATED_DOCUMENT;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.CLAIM_ISSUE_TRANSLATED_DOCUMENT;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.CLAIMANT_INTENTION_TRANSLATED_DOCUMENT;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.INTERLOC_JUDGMENT_TRANSLATED_DOCUMENT;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.NOTICE_OF_DISCONTINUANCE_DEFENDANT_TRANSLATED_DOCUMENT;
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
        switch (documentType) {
            case DEFENDANT_RESPONSE : return DEFENCE_TRANSLATED_DOCUMENT;
            case CLAIM_ISSUE : return CLAIM_ISSUE_TRANSLATED_DOCUMENT;
            case CLAIMANT_INTENTION : return CLAIMANT_INTENTION_TRANSLATED_DOCUMENT;
            case ORDER_NOTICE : return ORDER_NOTICE_TRANSLATED_DOCUMENT;
            case STANDARD_DIRECTION_ORDER: return SDO_TRANSLATED_DOCUMENT;
            case INTERLOCUTORY_JUDGMENT: return INTERLOC_JUDGMENT_TRANSLATED_DOCUMENT;
            case MANUAL_DETERMINATION: return MANUAL_DETERMINATION_TRANSLATED_DOCUMENT;
            case DECISION_MADE_ON_APPLICATIONS: return DECISION_MADE_ON_APPLICATIONS_TRANSLATED;
            case FINAL_ORDER: return FINAL_ORDER_TRANSLATED_DOCUMENT;
            case NOTICE_OF_DISCONTINUANCE_DEFENDANT:
                return NOTICE_OF_DISCONTINUANCE_DEFENDANT_TRANSLATED_DOCUMENT;
            case SETTLEMENT_AGREEMENT: return SETTLEMENT_AGREEMENT_TRANSLATED_DOCUMENT;
            case COURT_OFFICER_ORDER: return COURT_OFFICER_ORDER_TRANSLATED_DOCUMENT;
            case HEARING_NOTICE: return TRANSLATED_HEARING_NOTICE;
            default: throw new DocumentUploadException("No document file type found for Translated document");
        }
    }
}
