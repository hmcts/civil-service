package uk.gov.hmcts.reform.civil.model.caseprogression;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.civil.model.documents.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class UploadEvidenceExpert {

    private String expertOptionName;
    private String expertOptionExpertise;
    private String expertOptionExpertises;
    private String expertOptionOtherParty;
    private String expertDocumentQuestion;
    private String expertDocumentAnswer;
    private LocalDate expertOptionUploadDate;
    private Document expertDocument;
    @Builder.Default
    private LocalDateTime createdDatetime = LocalDateTime.now();
}
