package uk.gov.hmcts.reform.civil.model.breathing;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BreathingSpaceInfo {

    @JsonProperty("enterBreathing")
    private BreathingSpaceEnterInfo enter;

    @JsonProperty("liftBreathing")
    private BreathingSpaceLiftInfo lift;
}
