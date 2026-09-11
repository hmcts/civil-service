package uk.gov.hmcts.reform.civil.model.scanneddocument;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;

import java.net.URI;
import java.time.LocalDateTime;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ScannedDocument {

    private String id;
    private String fileName;
    private ScannedDocumentType documentType;
    private String controlNumber;
    private String subtype;
    private String formSubtype;
    private String submittedBy;
    private LocalDateTime scannedDate;
    private LocalDateTime deliveryDate;
    private String exceptionRecordReference;
    private URI documentManagementUrl;
    private URI documentManagementBinaryUrl;
    private Document url;
}
