package uk.gov.hmcts.reform.unspec.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.unspec.model.common.Element;
import uk.gov.hmcts.reform.unspec.model.documents.Document;

import java.util.ArrayList;
import java.util.List;

import static java.util.Optional.ofNullable;

@Data
@Builder
public class ServedDocumentFiles {

    private List<Element<DocumentWithRegex>> other;
    private List<Element<Document>> medicalReports;
    private List<Element<DocumentWithRegex>> medicalReport;
    private List<Element<DocumentWithRegex>> scheduleOfLoss;
    private Document particularsOfClaimDocument;
    private List<Element<Document>> particularsOfClaimDocumentNew;
    private String particularsOfClaimText;
    private List<Element<DocumentWithRegex>> certificateOfSuitability;

    @JsonIgnore
    public List<String> getErrors() {
        List<String> errors = new ArrayList<>();
        if (ofNullable(particularsOfClaimDocumentNew).isPresent() && ofNullable(particularsOfClaimText).isPresent()) {
            errors.add("You need to either upload 1 Particulars of claim only or enter the Particulars "
                           + "of claim text in the field provided. You cannot do both.");
        }

        if (ofNullable(particularsOfClaimDocumentNew).isEmpty() && ofNullable(particularsOfClaimText).isEmpty()) {
            errors.add("You must add Particulars of claim details");
        }
        return errors;
    }

    @JsonIgnore
    public List<String> getErrorsBackwardsCompatible() {
        List<String> errors = new ArrayList<>();
        if (ofNullable(particularsOfClaimDocument).isPresent() && ofNullable(particularsOfClaimText).isPresent()) {
            errors.add("More than one Particulars of claim details added");
        }

        if (ofNullable(particularsOfClaimDocument).isEmpty() && ofNullable(particularsOfClaimText).isEmpty()) {
            errors.add("You must add Particulars of claim details");
        }
        return errors;
    }
}
