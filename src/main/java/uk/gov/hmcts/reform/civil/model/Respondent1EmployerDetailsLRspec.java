package uk.gov.hmcts.reform.civil.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Data;
import uk.gov.hmcts.reform.civil.model.common.Element;

import java.util.List;

@Data
public class Respondent1EmployerDetailsLRspec {

    private List<Element<EmployerDetailsLRspec>> employerDetails;

    @JsonCreator
    public Respondent1EmployerDetailsLRspec(List<Element<EmployerDetailsLRspec>> employerDetails) {
        this.employerDetails = employerDetails;
    }

}
