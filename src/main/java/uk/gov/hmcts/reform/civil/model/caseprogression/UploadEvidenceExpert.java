package uk.gov.hmcts.reform.civil.model.caseprogression;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Objects;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
public class UploadEvidenceExpert {

    private String expertOptionName;
    private String expertOptionExpertise;
    private String expertOptionExpertises;
    private String expertOptionOtherParty;
    private String expertDocumentQuestion;
    private String expertDocumentAnswer;
    private LocalDate expertOptionUploadDate;
    private Document expertDocument;
    private LocalDateTime createdDatetime;
    // Constructor to set createdDatetime only if it's not already set
    public UploadEvidenceExpert(String expertOptionName,  String expertOptionExpertise, String expertOptionExpertises,
                                String expertOptionOtherParty, String expertDocumentQuestion, String expertDocumentAnswer,
                                LocalDate expertOptionUploadDate, Document expertDocument, LocalDateTime createdDatetime) {
        this.expertOptionName = expertOptionName;
        this.expertOptionExpertise = expertOptionExpertise;
        this.expertOptionExpertises = expertOptionExpertises;
        this.expertOptionOtherParty = expertOptionOtherParty;
        this.expertDocumentQuestion = expertDocumentQuestion;
        this.expertDocumentAnswer = expertDocumentAnswer;
        this.expertOptionUploadDate = expertOptionUploadDate;
        this.expertDocument = expertDocument;
        this.createdDatetime = Objects.requireNonNullElseGet(createdDatetime, () -> LocalDateTime.now(ZoneId.of("Europe/London")));
    }
}
