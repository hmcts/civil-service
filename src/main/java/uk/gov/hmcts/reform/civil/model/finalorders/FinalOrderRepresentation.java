package uk.gov.hmcts.reform.civil.model.finalorders;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import uk.gov.hmcts.reform.civil.enums.finalorders.FinalOrderRepresentationList;

@Accessors(chain = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FinalOrderRepresentation {

    private FinalOrderRepresentationList typeRepresentationList;
    private ClaimantAndDefendantHeard typeRepresentationComplex;
    private ClaimantAndDefendantHeard typeRepresentationOtherComplex;
}
