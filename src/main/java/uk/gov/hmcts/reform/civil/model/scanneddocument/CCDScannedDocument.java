package uk.gov.hmcts.reform.civil.model.scanneddocument;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
@JsonIgnoreProperties(ignoreUnknown = true)
public class CCDScannedDocument {

    private String fileName;
    private String controlNumber;
    private String subtype;
    private String formSubtype;
    private CCDScannedDocumentType type;
    private String submittedBy;
    private LocalDateTime scannedDate;
    private LocalDateTime deliveryDate;
    private Document url;
    private String exceptionRecordReference;
}
