package uk.gov.hmcts.reform.civil.documentmanagement.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
public class CaseDocument {

    private final Document documentLink;
    private final String documentName;
    private final DocumentType documentType;
    private final long documentSize;
    private final LocalDateTime createdDatetime;
    private final String createdBy;

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
