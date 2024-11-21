package uk.gov.hmcts.reform.civil.service.robotics.builders;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowState;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Configuration
public class EventBuilderConfig {

    @Bean
    public Map<FlowState.Main, EventBuilder> flowStateBuilderMap(List<EventBuilder> eventBuilders) {
        return eventBuilders.stream()
            .flatMap(builder -> builder.supportedFlowStates().stream()
                .map(flowState -> Map.entry(flowState, builder)))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
}
