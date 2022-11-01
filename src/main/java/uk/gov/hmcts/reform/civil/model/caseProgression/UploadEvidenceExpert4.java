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
public class UploadEvidenceExpert4 {

    private String expertOption4OtherName;
    private LocalDate expertOption4UploadDate;

}
