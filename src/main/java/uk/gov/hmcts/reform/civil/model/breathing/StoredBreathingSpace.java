package uk.gov.hmcts.reform.civil.model.breathing;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@NoArgsConstructor
@Accessors(chain = true)
public class StoredBreathingSpace {

    private String defendantLabel;

    private Integer defendantNumber;

    private BreathingSpaceEnterInfo enter;

    private BreathingSpaceLiftInfo lift;
}
