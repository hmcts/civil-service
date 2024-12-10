package uk.gov.hmcts.reform.civil.service.robotics.builders;

import uk.gov.hmcts.reform.civil.service.flowstate.FlowState;
import uk.gov.hmcts.reform.civil.service.robotics.dto.EventHistoryDTO;

import java.util.Set;

public interface EventBuilder {

    Set<FlowState.Main> supportedFlowStates();

    void buildEvent(EventHistoryDTO eventHistoryDTO);
}
