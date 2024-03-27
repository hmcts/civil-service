package uk.gov.hmcts.reform.civil.model.caseprogression;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Objects;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
public class UploadEvidenceDocumentType {

    private String witnessOptionName;
    private String typeOfDocument;
    private LocalDate documentIssuedDate;
    private Document documentUpload;
    private LocalDateTime createdDatetime;

    // Constructor to set createdDatetime only if it's not already set
    public UploadEvidenceDocumentType(String witnessOptionName, String typeOfDocument, LocalDate documentIssuedDate,
                                      Document documentUpload, LocalDateTime createdDatetime) {
        this.witnessOptionName = witnessOptionName;
        this.typeOfDocument = typeOfDocument;
        this.documentIssuedDate = documentIssuedDate;
        this.documentUpload = documentUpload;
        this.createdDatetime = Objects.requireNonNullElseGet(createdDatetime, () -> LocalDateTime.now(ZoneId.of("Europe/London")));
    }
}

