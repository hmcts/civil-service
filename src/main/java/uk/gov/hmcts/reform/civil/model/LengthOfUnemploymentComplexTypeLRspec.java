package uk.gov.hmcts.reform.civil.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LengthOfUnemploymentComplexTypeLRspec {

    private final String numberOfYearsInUnemployment;
    private final String numberOfMonthsInUnemployment;
}
