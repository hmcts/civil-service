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

    @JsonProperty("storedBreathingSpace")
    private List<Element<StoredBreathingSpace>> storedBreathingSpace;
}
