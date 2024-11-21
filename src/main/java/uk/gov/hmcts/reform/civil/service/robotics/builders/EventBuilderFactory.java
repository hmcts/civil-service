package uk.gov.hmcts.reform.civil.service.robotics.builders;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowState;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class EventBuilderFactory {

    private final Map<FlowState.Main, EventBuilder> flowStateBuilderMap;

    public EventBuilder getBuilder(FlowState.Main scenario) {
        return flowStateBuilderMap.get(scenario);
    }
}
