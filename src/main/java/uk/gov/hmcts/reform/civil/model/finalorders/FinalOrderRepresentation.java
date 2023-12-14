package uk.gov.hmcts.reform.civil.model.finalorders;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.civil.enums.finalorders.FinalOrderRepresentationList;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class FinalOrderRepresentation {

    private FinalOrderRepresentationList typeRepresentationList;
    private ClaimantAndDefendantHeard typeRepresentationComplex;
    private ClaimantAndDefendantHeard typeRepresentationOtherComplex;
}
