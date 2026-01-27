package uk.gov.hmcts.reform.civil.model.docmosis.sealedclaim;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import uk.gov.hmcts.reform.civil.model.common.MappableObject;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class LitigantInPersonForm implements MappableObject {

    @JsonProperty("courtseal")
    private String courtSeal = "[userImage:courtseal.PNG]"; //NOSONAR
    private String ccdCaseReference;
    private String referenceNumber;

}
