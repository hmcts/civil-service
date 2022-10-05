package uk.gov.hmcts.reform.civil.model.genapplication;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.Setter;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;

import java.time.LocalDate;

@Setter
@Data
@Builder(toBuilder = true)
public class GAUrgencyRequirement {

    private final YesOrNo generalAppUrgency;
    private final String reasonsForUrgency;
    private final LocalDate urgentAppConsiderationDate;

    @JsonCreator
    GAUrgencyRequirement(@JsonProperty("generalAppUrgency") YesOrNo generalAppUrgency,
                         @JsonProperty("reasonsForUrgency") String reasonsForUrgency,
                         @JsonProperty("urgentAppConsiderationDate") LocalDate urgentAppConsiderationDate) {
        this.generalAppUrgency = generalAppUrgency;
        this.reasonsForUrgency = reasonsForUrgency;
        this.urgentAppConsiderationDate = urgentAppConsiderationDate;
    }
}
