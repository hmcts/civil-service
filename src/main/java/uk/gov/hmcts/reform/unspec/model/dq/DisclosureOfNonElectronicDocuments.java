package uk.gov.hmcts.reform.unspec.model.dq;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.unspec.enums.YesOrNo;

@Data
@Builder
public class DisclosureOfNonElectronicDocuments {

    private final YesOrNo directionsForDisclosureProposed;
    private final YesOrNo standardDirectionsRequired;
    private final String bespokeDirections;
}
