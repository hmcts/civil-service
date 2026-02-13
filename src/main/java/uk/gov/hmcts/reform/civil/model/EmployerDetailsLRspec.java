package uk.gov.hmcts.reform.civil.model;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class EmployerDetailsLRspec {

    private String employerName;
    private String jobTitle;
}
