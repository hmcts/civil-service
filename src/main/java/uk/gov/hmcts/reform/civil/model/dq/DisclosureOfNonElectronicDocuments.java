package uk.gov.hmcts.reform.civil.model.dq;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;

@Data
@lombok.AllArgsConstructor
@lombok.NoArgsConstructor
@Builder
public class DisclosureOfNonElectronicDocuments {

    private YesOrNo directionsForDisclosureProposed;
    private YesOrNo standardDirectionsRequired;
    private String bespokeDirections;
}
