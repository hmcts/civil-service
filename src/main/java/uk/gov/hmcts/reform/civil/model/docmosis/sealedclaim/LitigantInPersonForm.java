package uk.gov.hmcts.reform.civil.model.docmosis.sealedclaim;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import uk.gov.hmcts.reform.civil.model.common.MappableObject;

@Getter
@Builder
@AllArgsConstructor
@EqualsAndHashCode
public class LitigantInPersonForm implements MappableObject {

    @JsonProperty("courtseal")
    private final String courtSeal = "[userImage:courtseal.PNG]"; //NOSONAR
    private final String ccdCaseReference;
    private final String referenceNumber;

}
