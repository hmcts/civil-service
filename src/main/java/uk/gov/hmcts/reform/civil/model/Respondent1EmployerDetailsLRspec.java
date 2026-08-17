package uk.gov.hmcts.reform.civil.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Data;
import uk.gov.hmcts.reform.civil.model.common.Element;

import java.util.List;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "AdmitPartEmployer", generate = true)
@Data
public class Respondent1EmployerDetailsLRspec {

    @CCD(label = " ", searchable = false, typeOverride = FieldType.Collection, typeParameterOverride = "EmployerDetail")
    private List<Element<EmployerDetailsLRspec>> employerDetails;

    @JsonCreator
    public Respondent1EmployerDetailsLRspec(List<Element<EmployerDetailsLRspec>> employerDetails) {
        this.employerDetails = employerDetails;
    }

  // ==== ccd-definition-converter: synthesised definition-only fields (retrofit) ====
  @CCD(
          label = "## Name of employer (if employed by someone else) \n",
          searchable = false,
          typeOverride = FieldType.Label
  )
  private String employerDetailsLabel;
  // ==== end synthesised definition-only fields ====
}
