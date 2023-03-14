package uk.gov.hmcts.reform.civil.model.caseprogression;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.civil.model.documents.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class UploadEvidenceDocumentType {

    private String typeOfDocument;
    private LocalDate documentIssuedDate;
    private Document documentUpload;
    @Builder.Default
    private LocalDateTime createdDatetime = LocalDateTime.now();
}

