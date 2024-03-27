package uk.gov.hmcts.reform.civil.model.caseprogression;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
public class UploadEvidenceWitness {

    private String witnessOptionName;
    private LocalDate witnessOptionUploadDate;
    private Document witnessOptionDocument;
    private LocalDateTime createdDatetime;

    // Constructor to set createdDatetime only if it's not already set
    public UploadEvidenceWitness(String witnessOptionName, LocalDate witnessOptionUploadDate, Document witnessOptionDocument,
                                 LocalDateTime createdDatetime) {
        this.witnessOptionName = witnessOptionName;
        this.witnessOptionUploadDate = witnessOptionUploadDate;
        this.witnessOptionDocument = witnessOptionDocument;
        if (createdDatetime != null) this.createdDatetime = createdDatetime;
        else this.createdDatetime = LocalDateTime.now(ZoneId.of("Europe/London"));
    }
}
