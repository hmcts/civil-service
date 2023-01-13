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
public class UploadEvidenceDocumentType {

    private String typeOfDocument;
    private LocalDate documentIssuedDate;
    private Document documentUpload;
    private String expertOptionName;
    private String expertOptionName2;
    private String expertOptionName3;
    private String expertOptionExpertise;
    private String expertOptionExpertises;
    private String expertOptionOtherParty;
    private String expertDocumentQuestion;
    private String expertDocumentAnswer;
    private LocalDate expertOptionUploadDate;
    private String witnessOptionName;
    private LocalDate witnessOptionUploadDate;

}

