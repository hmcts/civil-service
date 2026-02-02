package uk.gov.hmcts.reform.civil.model.caseprogression;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class UploadEvidenceExpert {

    private String expertOptionName;
    private String expertOptionExpertise;
    private String expertOptionExpertises;
    private String expertOptionOtherParty;
    private String expertDocumentQuestion;
    private String expertDocumentAnswer;
    private LocalDate expertOptionUploadDate;
    private Document expertDocument;
    private LocalDateTime createdDatetime = LocalDateTime.now(ZoneId.of("Europe/London"));
}
