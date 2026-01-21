package uk.gov.hmcts.reform.civil.model.citizenui;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import uk.gov.hmcts.reform.civil.documentmanagement.DocumentUploadException;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType;

import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.COURT_OFFICER_ORDER_TRANSLATED_DOCUMENT;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.DEFENCE_TRANSLATED_DOCUMENT;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.DECISION_MADE_ON_APPLICATIONS_TRANSLATED;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.DIRECTION_ORDER;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.DISMISSAL_ORDER;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.FINAL_ORDER_TRANSLATED_DOCUMENT;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.GENERAL_APPLICATION_DRAFT;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.GENERAL_ORDER;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.HEARING_NOTICE;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.HEARING_ORDER;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.JUDGES_DIRECTIONS_APPLICANT_TRANSLATED;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.JUDGES_DIRECTIONS_RESPONDENT_TRANSLATED;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.MANUAL_DETERMINATION_TRANSLATED_DOCUMENT;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.CLAIM_ISSUE_TRANSLATED_DOCUMENT;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.CLAIMANT_INTENTION_TRANSLATED_DOCUMENT;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.INTERLOC_JUDGMENT_TRANSLATED_DOCUMENT;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.NOTICE_OF_DISCONTINUANCE_DEFENDANT_TRANSLATED_DOCUMENT;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.ORDER_NOTICE_TRANSLATED_DOCUMENT;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.REQUEST_FOR_INFORMATION;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.REQUEST_MORE_INFORMATION_APPLICANT_TRANSLATED;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.REQUEST_MORE_INFORMATION_RESPONDENT_TRANSLATED;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.SDO_TRANSLATED_DOCUMENT;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.SETTLEMENT_AGREEMENT_TRANSLATED_DOCUMENT;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.TRANSLATED_HEARING_NOTICE;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.UPLOADED_DOCUMENT_APPLICANT;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.UPLOADED_DOCUMENT_RESPONDENT;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.WRITTEN_REPRESENTATION_APPLICANT_TRANSLATED;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.WRITTEN_REPRESENTATION_CONCURRENT;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.WRITTEN_REPRESENTATION_RESPONDENT_TRANSLATED;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.WRITTEN_REPRESENTATION_SEQUENTIAL;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
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

    @JsonIgnore
    public DocumentType getCorrespondingDocumentTypeGA(TranslatedDocumentType documentType) {
        switch (documentType) {
            case REQUEST_FOR_MORE_INFORMATION_ORDER:
                return REQUEST_FOR_INFORMATION;
            case REQUEST_MORE_INFORMATION_APPLICANT:
                return REQUEST_MORE_INFORMATION_APPLICANT_TRANSLATED;
            case REQUEST_MORE_INFORMATION_RESPONDENT:
                return REQUEST_MORE_INFORMATION_RESPONDENT_TRANSLATED;
            case GENERAL_ORDER:
            case APPROVE_OR_EDIT_ORDER:
                return GENERAL_ORDER;
            case JUDGES_DIRECTIONS_ORDER:
                return DIRECTION_ORDER;
            case JUDGES_DIRECTIONS_APPLICANT:
                return JUDGES_DIRECTIONS_APPLICANT_TRANSLATED;
            case JUDGES_DIRECTIONS_RESPONDENT:
                return JUDGES_DIRECTIONS_RESPONDENT_TRANSLATED;
            case HEARING_ORDER:
                return HEARING_ORDER;
            case HEARING_NOTICE:
                return HEARING_NOTICE;
            case DISMISSAL_ORDER:
                return DISMISSAL_ORDER;
            case WRITTEN_REPRESENTATIONS_ORDER_SEQUENTIAL:
                return WRITTEN_REPRESENTATION_SEQUENTIAL;
            case WRITTEN_REPRESENTATIONS_ORDER_CONCURRENT:
                return WRITTEN_REPRESENTATION_CONCURRENT;
            case WRITTEN_REPRESENTATIONS_APPLICANT:
                return WRITTEN_REPRESENTATION_APPLICANT_TRANSLATED;
            case WRITTEN_REPRESENTATIONS_RESPONDENT:
                return WRITTEN_REPRESENTATION_RESPONDENT_TRANSLATED;
            case UPLOADED_DOCUMENTS_APPLICANT:
                return UPLOADED_DOCUMENT_APPLICANT;
            case UPLOADED_DOCUMENTS_RESPONDENT:
                return UPLOADED_DOCUMENT_RESPONDENT;
            case APPLICATION_SUMMARY_DOCUMENT:
            case APPLICATION_SUMMARY_DOCUMENT_RESPONDED:
                return GENERAL_APPLICATION_DRAFT;
            default:
                throw new DocumentUploadException("No document file type found for Translated document");
        }
    }
}
