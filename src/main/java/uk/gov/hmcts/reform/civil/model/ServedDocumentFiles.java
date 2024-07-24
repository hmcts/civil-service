package uk.gov.hmcts.reform.civil.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;

import java.util.ArrayList;
import java.util.List;

import static java.util.Optional.ofNullable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServedDocumentFiles {

    private List<Element<DocumentWithRegex>> other;
    private List<Element<DocumentWithRegex>> medicalReport;
    private List<Element<DocumentWithRegex>> scheduleOfLoss;
    private List<Element<Document>> particularsOfClaimDocument;
    private String particularsOfClaimText;
    private List<Element<DocumentWithRegex>> certificateOfSuitability;
    private List<Element<Document>> timelineEventUpload;

    private static final String BOTH_PARTICULARS_OF_CLAIM_ERROR = "You need to either upload 1 Particulars of claim "
        + "only or enter the Particulars of claim text in the field provided. You cannot do both.";
    private static final String EMPTY_ERROR = "You must add Particulars of claim details";

    @JsonIgnore
    public List<String> getErrors() {
        List<String> errors = getErrorsAddOrAmendDocuments();

        if (ofNullable(particularsOfClaimDocument).isEmpty() && ofNullable(particularsOfClaimText).isEmpty()) {
            errors.add(EMPTY_ERROR);
        }
        return errors;
    }

    @JsonIgnore
    public List<String> getErrorsAddOrAmendDocuments() {
        List<String> errors = new ArrayList<>();
        if (ofNullable(particularsOfClaimDocument).isPresent() && ofNullable(particularsOfClaimText).isPresent()) {
            errors.add(BOTH_PARTICULARS_OF_CLAIM_ERROR);
        }
        return errors;
    }
}
