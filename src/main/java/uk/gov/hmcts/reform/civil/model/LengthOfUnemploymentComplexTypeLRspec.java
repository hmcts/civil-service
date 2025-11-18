package uk.gov.hmcts.reform.civil.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LengthOfUnemploymentComplexTypeLRspec {

    private String numberOfYearsInUnemployment;
    private String numberOfMonthsInUnemployment;
}
