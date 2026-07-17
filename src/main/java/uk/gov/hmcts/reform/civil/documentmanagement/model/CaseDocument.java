package uk.gov.hmcts.reform.civil.documentmanagement.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.experimental.Accessors;
import lombok.NoArgsConstructor;
import lombok.Data;
import uk.gov.hmcts.reform.civil.enums.CaseRole;

import java.time.LocalDateTime;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Accessors(chain = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CaseDocument {

    @CCD(label = "Document URL", searchable = false)
    private Document documentLink;
    @CCD(label = "Name", searchable = false)
    private String documentName;
    @CCD(label = "Type", searchable = false, typeOverride = FieldType.FixedList, typeParameterOverride = "DocumentType")
    private DocumentType documentType;
    @CCD(label = "Document size", searchable = false)
    private long documentSize;
    @CCD(label = "Uploaded on", searchable = false)
    private LocalDateTime createdDatetime;
    @CCD(label = "Uploaded by", searchable = false)
    private String createdBy;
    @CCD(
            label = "Owned by",
            showCondition = "createdDatetime = \"DO_NOT_SHOW\"",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "CaseRoleType"
    )
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
