package uk.gov.hmcts.reform.civil.service.robotics.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import uk.gov.hmcts.reform.civil.model.robotics.EventHistory;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Accessors(chain = true)
public class RoboticsCaseDataDTO {

    byte [] jsonRawData;
    private EventHistory events;
}
