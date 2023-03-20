package uk.gov.hmcts.reform.civil.model.caseprogression;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class UploadEvidenceWitness {

    private String witnessOptionName;
    private LocalDate witnessOptionUploadDate;
    private Document witnessOptionDocument;
    @Builder.Default
    private LocalDateTime createdDatetime = LocalDateTime.now();
}
