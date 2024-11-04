package uk.gov.hmcts.reform.civil.service.robotics.builders;

import uk.gov.hmcts.reform.civil.service.robotics.dto.EventHistoryDTO;

public interface EventBuilder {

    void buildEvent(EventHistoryDTO eventHistoryDTO);
}
