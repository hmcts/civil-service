package uk.gov.hmcts.reform.civil.documentmanagement.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.experimental.Accessors;
import lombok.NoArgsConstructor;
import lombok.Data;
import uk.gov.hmcts.reform.civil.enums.CaseRole;

import java.time.LocalDateTime;

@Accessors(chain = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CaseDocument {

    private Document documentLink;
    private String documentName;
    private DocumentType documentType;
    private long documentSize;
    private LocalDateTime createdDatetime;
    private String createdBy;
    private CaseRole ownedBy;

    @JsonIgnore
    public static CaseDocument toCaseDocument(Document document, DocumentType documentType) {
        return new CaseDocument()
            .setDocumentLink(document)
            .setDocumentName(document.documentFileName)
            .setDocumentType(documentType)
            .setCreatedDatetime(LocalDateTime.now());
    }

    @JsonIgnore
    public static CaseDocument toCaseDocumentGA(Document document, DocumentType documentType, String translator) {
        return new CaseDocument()
            .setDocumentLink(document)
            .setDocumentName(document.documentFileName)
            .setDocumentType(setOnlyCCDDocumentTypes(documentType))
            .setCreatedDatetime(LocalDateTime.now())
            .setCreatedBy(translator);
    }

    public static DocumentType setOnlyCCDDocumentTypes(DocumentType documentType) {
        switch (documentType) {
            case JUDGES_DIRECTIONS_RESPONDENT_TRANSLATED:
            case JUDGES_DIRECTIONS_APPLICANT_TRANSLATED:
            case UPLOADED_DOCUMENT_RESPONDENT:
            case UPLOADED_DOCUMENT_APPLICANT:
                return null;
            default:
                return documentType;
        }
    }
}
