package uk.gov.hmcts.reform.civil.model;

import lombok.Data;
import lombok.experimental.Accessors;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "EmployerDetail", generate = true)
@Data
@Accessors(chain = true)
public class EmployerDetailsLRspec {

    @CCD(label = "Employer's name", searchable = false)
    private String employerName;
    @CCD(label = "Job title", searchable = false)
    private String jobTitle;
}
