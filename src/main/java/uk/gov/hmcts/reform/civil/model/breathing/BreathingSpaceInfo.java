package uk.gov.hmcts.reform.civil.model.breathing;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BreathingSpaceInfo {

    private final BreathingSpaceEnterInfo enter;
    private final BreathingSpaceLiftInfo lift;
}
