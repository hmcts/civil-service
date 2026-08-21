package uk.gov.hmcts.reform.civil.model.finalorders;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import uk.gov.hmcts.reform.civil.enums.finalorders.FinalOrderRepresentationList;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@Accessors(chain = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FinalOrderRepresentation {

    @CCD(label = " ", searchable = false)
    private FinalOrderRepresentationList typeRepresentationList;
    @CCD(label = " ", showCondition = "typeRepresentationList=\"CLAIMANT_AND_DEFENDANT\"", searchable = false)
    private ClaimantAndDefendantHeard typeRepresentationComplex;
    @CCD(label = " ", showCondition = "typeRepresentationList=\"OTHER_REPRESENTATION\"", searchable = false)
    private ClaimantAndDefendantHeard typeRepresentationOtherComplex;
}
