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
public class UploadEvidenceExpert {

    private String expertOptionName;
    private String expertOptionExpertise;
    private String expertOptionOtherParty;
    private String expertDocumentQuestion;
    private String expertDocumentAnswer;
    private LocalDate expertOptionUploadDate;
    private Document expertDocument;
}
