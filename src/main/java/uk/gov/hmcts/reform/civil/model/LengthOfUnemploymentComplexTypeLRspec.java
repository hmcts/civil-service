package uk.gov.hmcts.reform.civil.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LengthOfUnemploymentComplexTypeLRspec {

    private String numberOfYearsInUnemployment;
    private String numberOfMonthsInUnemployment;
}
