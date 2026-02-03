package uk.gov.hmcts.reform.civil.model.genapplication;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@Accessors(chain = true)
public class GAUrgencyRequirement {

    private YesOrNo generalAppUrgency;
    private String reasonsForUrgency;
    private LocalDate urgentAppConsiderationDate;

    @JsonCreator
    GAUrgencyRequirement(@JsonProperty("generalAppUrgency") YesOrNo generalAppUrgency,
                         @JsonProperty("reasonsForUrgency") String reasonsForUrgency,
                         @JsonProperty("urgentAppConsiderationDate") LocalDate urgentAppConsiderationDate) {
        this.generalAppUrgency = generalAppUrgency;
        this.reasonsForUrgency = reasonsForUrgency;
        this.urgentAppConsiderationDate = urgentAppConsiderationDate;
    }
}
