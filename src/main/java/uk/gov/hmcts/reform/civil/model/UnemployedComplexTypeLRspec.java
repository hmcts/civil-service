package uk.gov.hmcts.reform.civil.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Data
@NoArgsConstructor(force = true)
@AllArgsConstructor
public class UnemployedComplexTypeLRspec {

    @CCD(
            label = "Is your client unemployed or retired?",
            searchable = false,
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "UnemployedRadioFixedListLRspec"
    )
    private String unemployedComplexTypeRequired;
    @CCD(label = " ", showCondition = "unemployedComplexTypeRequired = \"UNEMPLOYED\"", searchable = false)
    private LengthOfUnemploymentComplexTypeLRspec lengthOfUnemployment;
    @CCD(label = "Provide details", showCondition = "unemployedComplexTypeRequired = \"OTHER\"", searchable = false)
    private String otherUnemployment;
}
