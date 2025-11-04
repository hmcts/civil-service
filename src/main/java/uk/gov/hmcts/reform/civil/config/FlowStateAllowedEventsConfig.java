package uk.gov.hmcts.reform.civil.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Data
@ConfigurationProperties(prefix = "flow-state")
public class FlowStateAllowedEventsConfig {

    private List<CaseEvent> eventWhitelist;
    private Map<String, List<CaseEvent>> allowedEvents;
    private Map<String, List<CaseEvent>> allowedEventsSpec;

    public boolean isWhitelistEvent(CaseEvent event) {
        return Optional.ofNullable(eventWhitelist)
            .map(list -> list.contains(event))
            .orElse(false);
    }

    public List<CaseEvent> getAllowedEvents(String stateFullName) {
        // stateFullName should be the full state name, e.g. MAIN.DRAFT
        return Optional.ofNullable(allowedEvents)
            .map(map -> map.getOrDefault(stateFullName, List.of()))
            .orElse(List.of());
    }

    public List<String> getAllowedStates(CaseEvent caseEvent) {
        return Optional.ofNullable(allowedEvents)
            .map(map -> map.entrySet().stream()
                .filter(entry -> entry.getValue().contains(caseEvent))
                .map(Map.Entry::getKey)
                .toList())
            .orElse(List.of());
    }

    public List<CaseEvent> getAllowedEventsSpec(String stateFullName) {
        // stateFullName should be the full state name, e.g. MAIN.DRAFT
        return Optional.ofNullable(allowedEventsSpec)
            .map(map -> map.getOrDefault(stateFullName, List.of()))
            .orElse(List.of());
    }

    public List<String> getAllowedStatesSpec(CaseEvent caseEvent) {
        return Optional.ofNullable(allowedEventsSpec)
            .map(map -> map.entrySet().stream()
                .filter(entry -> entry.getValue().contains(caseEvent))
                .map(Map.Entry::getKey)
                .toList())
            .orElse(List.of());
    }
}
