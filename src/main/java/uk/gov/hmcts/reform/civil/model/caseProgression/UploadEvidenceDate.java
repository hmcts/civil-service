package uk.gov.hmcts.reform.civil.model.caseProgression;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class UploadEvidenceDate{

    private LocalDate witnessOption1UploadDate;
    private LocalDate witnessOption3UploadDate;
    private LocalDate expertOption1UploadDate;
    private LocalDate expertOption2UploadDate;
    private LocalDate expertOption3UploadDate;

}
