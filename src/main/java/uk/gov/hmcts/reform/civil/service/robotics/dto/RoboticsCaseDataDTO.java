package uk.gov.hmcts.reform.civil.service.robotics.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.civil.model.robotics.EventHistory;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class RoboticsCaseDataDTO {

    byte [] jsonRawData;
    private EventHistory events;
}
