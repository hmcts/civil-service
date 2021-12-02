package uk.gov.hmcts.reform.civil.model.dq;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class EmploymentTypeComplexTypeDQLRspec {

    private final List<String> employmentTypeComplexTypeDQLRspec;

    @JsonCreator
    public EmploymentTypeComplexTypeDQLRspec(List<String> employmentTypeComplexTypeDQLRspec) {
        this.employmentTypeComplexTypeDQLRspec = employmentTypeComplexTypeDQLRspec;
    }
}
