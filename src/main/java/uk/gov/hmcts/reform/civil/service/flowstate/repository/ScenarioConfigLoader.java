package uk.gov.hmcts.reform.civil.service.flowstate.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.Assert;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@RequiredArgsConstructor
@SuppressWarnings("java:S3077")
public class ScenarioConfigLoader implements AllowedEventRepository {

    private static final String WHITELIST_FILE = "allowed-whitelist-events.yml";

    private final ResourceLoader resourceLoader;
    private final ObjectMapper yaml = new ObjectMapper(new YAMLFactory());

    private final Map<String, Map<String, Set<CaseEvent>>> cache = new ConcurrentHashMap<>();
    private final Map<String, Set<CaseEvent>> eventSetCache = new ConcurrentHashMap<>();

    private volatile Set<CaseEvent> whitelist;

    @Override
    public Set<CaseEvent> getWhitelist() {
        Set<CaseEvent> result = whitelist;
        if (result == null) {
            synchronized (this) {
                if (whitelist == null) {
                    whitelist = loadWhitelist();
                }
                result = whitelist;
            }
        }
        return result;
    }

    @Override
    public Set<CaseEvent> getFlowStateAllowedEvents(String scenarioFile, String stateFullName) {
        Map<String, Set<CaseEvent>> eventsByState = cache.computeIfAbsent(scenarioFile, this::loadScenario);
        return eventsByState.getOrDefault(stateFullName, Set.of());
    }

    @Override
    public Set<CaseEvent> getNoOngoingBPAllowedEvents(String fileName) {
        return eventSetCache.computeIfAbsent(fileName, this::loadEventSet);
    }

    private Map<String, Set<CaseEvent>> loadScenario(String scenarioFile) {
        String location = "classpath:config/" + scenarioFile;
        try {
            Resource resource = resourceLoader.getResource(location);
            Assert.isTrue(resource.exists(), () -> "Scenario config not found: " + location);

            try (InputStream is = resource.getInputStream()) {
                Map<String, List<String>> raw = yaml.readValue(
                    is, new TypeReference<>() {
                    }
                );
                Map<String, Set<CaseEvent>> mapped = new HashMap<>();
                raw.forEach((state, events) -> mapped.put(state, toCaseEvents(events)));
                return Map.copyOf(mapped);
            }
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to load scenario config: " + location, ex);
        }
    }

    private Set<CaseEvent> loadWhitelist() {
        String location = configLocation(WHITELIST_FILE);
        try {
            Resource resource = resourceLoader.getResource(location);
            if (!resource.exists()) {
                return Set.of();
            }
            return loadEventSet(WHITELIST_FILE);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to load whitelist: " + location, ex);
        }
    }

    private Set<CaseEvent> loadEventSet(String fileName) {
        String location = configLocation(fileName);
        try {
            Resource resource = resourceLoader.getResource(location);
            if (!resource.exists()) {
                return Set.of();
            }
            try (InputStream is = resource.getInputStream()) {
                List<String> raw = yaml.readValue(
                    is, new TypeReference<>() {
                    }
                );
                return toCaseEvents(raw);
            }
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to load event config: " + location, ex);
        }
    }

    private String configLocation(String fileName) {
        return "classpath:config/" + fileName;
    }

    private Set<CaseEvent> toCaseEvents(List<String> raw) {
        Set<CaseEvent> set = new LinkedHashSet<>();
        for (String v : Optional.ofNullable(raw).orElseGet(List::of)) {
            try {
                set.add(CaseEvent.valueOf(v));
            } catch (IllegalArgumentException iae) {
                throw new IllegalStateException("Unknown CaseEvent '" + v + "'");
            }
        }
        return Set.copyOf(set);
    }

}
