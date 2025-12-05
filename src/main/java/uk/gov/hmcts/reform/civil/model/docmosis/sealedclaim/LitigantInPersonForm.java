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
    @Builder.Default
    private String courtSeal = "[userImage:courtseal.PNG]"; //NOSONAR
    private String ccdCaseReference;
    private String referenceNumber;

}
