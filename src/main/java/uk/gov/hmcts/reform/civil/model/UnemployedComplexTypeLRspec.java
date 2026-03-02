package uk.gov.hmcts.reform.civil.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor(force = true)
@AllArgsConstructor
public class UnemployedComplexTypeLRspec {

    private String unemployedComplexTypeRequired;
    private LengthOfUnemploymentComplexTypeLRspec lengthOfUnemployment;
    private String otherUnemployment;
}
