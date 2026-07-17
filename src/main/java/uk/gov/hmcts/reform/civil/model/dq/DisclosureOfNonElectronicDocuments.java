package uk.gov.hmcts.reform.civil.model.dq;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class DisclosureOfNonElectronicDocuments {

    @CCD(
            label = "Do you want to propose directions for disclosure?",
            searchable = false,
            typeOverride = FieldType.YesOrNo
    )
    private YesOrNo directionsForDisclosureProposed;
    @CCD(
            label = "Do you want standard disclosure?",
            showCondition = "directionsForDisclosureProposed = \"Yes\"",
            searchable = false,
            typeOverride = FieldType.YesOrNo
    )
    private YesOrNo standardDirectionsRequired;
    @CCD(
            label = "What directions are proposed for disclosure?",
            showCondition = "directionsForDisclosureProposed = \"Yes\" AND standardDirectionsRequired = \"No\"",
            searchable = false
    )
    private String bespokeDirections;
}
