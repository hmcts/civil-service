package uk.gov.hmcts.reform.civil.model.dq;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DisclosureOfNonElectronicDocuments {

    private YesOrNo directionsForDisclosureProposed;
    private YesOrNo standardDirectionsRequired;
    private String bespokeDirections;
}
