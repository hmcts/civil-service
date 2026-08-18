package uk.gov.hmcts.reform.civil.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;

import java.time.LocalDateTime;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class ScannedDocument {

    private String fileName;
    private String controlNumber;
    private String subtype;
    private String formSubtype;
    private ScannedDocumentType type;
    private String submittedBy;
    private LocalDateTime scannedDate;
    private LocalDateTime deliveryDate;
    private Document url;
    private String exceptionRecordReference;
}
