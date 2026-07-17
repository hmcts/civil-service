package uk.gov.hmcts.reform.civil.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;

import java.util.ArrayList;
import java.util.List;

import static java.util.Optional.ofNullable;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class ServedDocumentFiles {

    @CCD(
            label = "Other documents",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "DocumentOrImageWithRegex"
    )
    private List<Element<DocumentWithRegex>> other;
    @CCD(
            label = "Medical reports",
            hint = "Medical reports can be scanned and uploaded as image files such as jpg or png.",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "DocumentOrImageWithRegex"
    )
    private List<Element<DocumentWithRegex>> medicalReport;
    @CCD(
            label = "Schedule of loss",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "DocumentOrImageWithRegex"
    )
    private List<Element<DocumentWithRegex>> scheduleOfLoss;
    @CCD(
            label = "Particulars of claim",
            regex = ".pdf,.txt,.doc,.dot,.docx,.rtf,.xls,.xlt,.xla,.xlsx,.xltx,.xlsb,.ppt,.pot,.pps,.ppa,.pptx,.potx,.ppsx",
            searchable = false,
            max = 1
    )
    private List<Element<Document>> particularsOfClaimDocument;
    @CCD(label = "Particulars of claim", searchable = false, typeOverride = FieldType.TextArea)
    private String particularsOfClaimText;
    @CCD(
            label = "Certificate of suitability",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "DocumentOrImageWithRegex"
    )
    private List<Element<DocumentWithRegex>> certificateOfSuitability;
    @CCD(
            label = "Claim timeline",
            regex = ".pdf,.txt,.doc,.dot,.docx,.rtf,.xls,.xlt,.xla,.xlsx,.xltx,.xlsb,.ppt,.pot,.pps,.ppa,.pptx,.potx,.ppsx",
            searchable = false,
            max = 1
    )
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

  // ==== ccd-definition-converter: synthesised definition-only fields (retrofit) ====
  @CCD(
          label = "Aside from medical reports, any documents you upload must be in the following machine readable format: doc, docx, dot, pdf, rtf, txt, xlt or xlsx. Pdf’s must be text based, not a scanned document.",
          searchable = false,
          typeOverride = FieldType.Label
  )
  private String documentsLabel;
  // ==== end synthesised definition-only fields ====
}
