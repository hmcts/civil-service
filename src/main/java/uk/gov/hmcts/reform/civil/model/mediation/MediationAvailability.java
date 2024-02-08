package uk.gov.hmcts.reform.civil.model.mediation;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.UnavailableDate;
import uk.gov.hmcts.reform.civil.model.common.Element;

import java.util.List;

@Data
@Builder(toBuilder = true)
public class MediationAvailability {

    private YesOrNo isMediationUnavailablityExists;
    private List<Element<UnavailableDate>> unavailableDatesForMediation;

    @JsonCreator
    MediationAvailability(@JsonProperty("isMediationUnavailablityExists") YesOrNo isMediationUnavailablityExists,
                          @JsonProperty("unavailableDatesForMediation") List<Element<UnavailableDate>> unavailableDatesForMediation) {
        this.isMediationUnavailablityExists = isMediationUnavailablityExists;
        this.unavailableDatesForMediation = unavailableDatesForMediation;

    }

}
