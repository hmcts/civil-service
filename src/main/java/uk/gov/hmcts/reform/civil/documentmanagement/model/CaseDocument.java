package uk.gov.hmcts.reform.civil.documentmanagement.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.civil.enums.CaseRole;

import java.time.LocalDateTime;

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
}
