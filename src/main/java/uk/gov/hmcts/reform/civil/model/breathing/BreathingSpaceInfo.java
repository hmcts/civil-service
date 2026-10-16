package uk.gov.hmcts.reform.civil.model.breathing;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.common.Element;

import java.util.List;

@Data
@NoArgsConstructor
@Accessors(chain = true)
public class BreathingSpaceInfo {

    @JsonProperty("enterBreathing")
    private BreathingSpaceEnterInfo enter;

    @JsonProperty("liftBreathing")
    private BreathingSpaceLiftInfo lift;

    @JsonProperty("breathingSpaceActive")
    private YesOrNo active;

    // Yes -> once a lift is submitted with an end date after today. Set back to No -> when the lift happens.
    // The lift event in EXUI can only check Yes/No, so this is what keeps that event hidden until the date arrives.
    @JsonProperty("breathingSpaceLiftPending")
    private YesOrNo liftPending;

    @JsonProperty("storedBreathingSpace")
    private List<Element<StoredBreathingSpace>> storedBreathingSpace;

    @JsonProperty("breathingSpaceDefendant1Details")
    private BreathingSpaceSummaryDetails defendant1Details;

    @JsonProperty("breathingSpaceDefendant2Details")
    private BreathingSpaceSummaryDetails defendant2Details;
}
