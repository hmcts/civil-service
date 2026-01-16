package uk.gov.hmcts.reform.civil.documentmanagement.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.experimental.Accessors;
import lombok.NoArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.civil.enums.CaseRole;

import java.time.LocalDateTime;

@Accessors(chain = true)
@Data
@Builder(toBuilder = true)
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
        return CaseDocument.builder()
            .documentLink(document)
            .documentName(document.documentFileName)
            .documentType(documentType)
            .createdDatetime(LocalDateTime.now())
            .build();
    }

    @JsonIgnore
    public static CaseDocument toCaseDocumentGA(Document document, DocumentType documentType, String translator) {
        return CaseDocument.builder()
            .documentLink(document)
            .documentName(document.documentFileName)
            .documentType(setOnlyCCDDocumentTypes(documentType))
            .createdDatetime(LocalDateTime.now())
            .createdBy(translator)
            .build();
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
