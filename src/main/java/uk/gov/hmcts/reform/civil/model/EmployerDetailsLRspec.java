package uk.gov.hmcts.reform.civil.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EmployerDetailsLRspec {

    private final String employerName;
    private final String jobTitle;
}
