package uk.gov.hmcts.reform.civil.model.caseprogression;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.documents.Document;

import java.time.LocalDate;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class UploadEvidenceExpert {

    private String expertOption1Name;
    private String expertOption1Expertise;
    private LocalDate expertOption1UploadDate;
    private Document expertOption1;

    private String expertOption2Name;
    private String expertOption2Expertise;
    private LocalDate expertOption2UploadDate;
    private Document expertOption2;

    private String expertOption3Name;
    private String expertOption3OtherName3;
    private String expertOption3Document;
    private LocalDate expertOption3UploadDate;
    private Document expertOption3;

    private String expertOption4Name;
    private String expertOption4OtherName4;
    private String expertOption4Document;
    private LocalDate expertOption4UploadDate;
    private Document expertOption4;

}
