package uk.gov.hmcts.reform.civil.model.caseprogression;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.civil.model.documents.Document;

import java.time.LocalDate;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class UploadEvidenceWitness {

    private String witnessOption1Name;
    private LocalDate witnessOption1UploadDate;
    private Document witnessOption1;

    private String witnessOption2Name;
    private Document witnessOption2;

    private String witnessOption3Name;
    private LocalDate witnessOption3UploadDate;
    private Document witnessOption3;

    private Document witnessOption4;

}
