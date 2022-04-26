package uk.gov.hmcts.reform.civil.model.LRSpec;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.Address;
import uk.gov.hmcts.reform.civil.model.common.MappableObject;

@Data
@Builder(toBuilder = true)
public class LRSpecCaseDataExtension  implements MappableObject {

    private final String generalAppDeadlineNotification;
    private final Address specAoSRespondent2CorrespondenceAddressdetails;
    private final YesOrNo specAoSRespondent2CorrespondenceAddressRequired;

    @JsonCreator
    LRSpecCaseDataExtension(@JsonProperty("generalAppDeadlineNotification") String generalAppDeadlineNotification,
                            @JsonProperty("specAoSRespondent2CorrespondenceAddressdetails") Address specAoSRespondent2CorrespondenceAddressdetails,
                            @JsonProperty("specAoSRespondent2CorrespondenceAddressRequired") YesOrNo specAoSRespondent2CorrespondenceAddressRequired) {
        this.generalAppDeadlineNotification = generalAppDeadlineNotification;
        this.specAoSRespondent2CorrespondenceAddressdetails = specAoSRespondent2CorrespondenceAddressdetails;
        this.specAoSRespondent2CorrespondenceAddressRequired = specAoSRespondent2CorrespondenceAddressRequired;
    }
}
