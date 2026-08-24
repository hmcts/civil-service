package uk.gov.hmcts.reform.civil.model.breathing;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;

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

    @JsonProperty("breathingSpaceLifted")
    private YesOrNo lifted;
}
