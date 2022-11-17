package uk.gov.hmcts.reform.civil.model.breathing;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BreathingSpaceInfo {

    private BreathingSpaceEnterInfo enter;
    private BreathingSpaceLiftInfo lift;
}
