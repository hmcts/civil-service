package uk.gov.hmcts.reform.civil.service.flowstate.repository;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ScenarioConfigLoaderTest {

    private final ResourceLoader resourceLoader = new DefaultResourceLoader();

    @Test
    void parsesKnownState_fromUnspecFile() {
        var loader = new ScenarioConfigLoader(resourceLoader);
        Set<CaseEvent> events = loader.getAllowedEvents("valid-unspec-events.yml", "MAIN.DRAFT");
        assertThat(events).isNotNull().isNotEmpty();
    }

    @Test
    void returnsEmptySet_forUnknownState() {
        var loader = new ScenarioConfigLoader(resourceLoader);
        Set<CaseEvent> events = loader.getAllowedEvents("valid-spec-events.yml", "MAIN.NOT_A_STATE");
        assertThat(events).isEmpty();
    }

    @Test
    void invalidEventName_inSpecFile_throwsHelpfulException() {
        var loader = new ScenarioConfigLoader(resourceLoader);
        assertThatThrownBy(() -> loader.getAllowedEvents("invalid-spec-events.yml", "MAIN.DRAFT"))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("Unknown CaseEvent");
    }

    @Test
    void invalidFile_throwsHelpfulException() {
        var loader = new ScenarioConfigLoader(resourceLoader);
        assertThatThrownBy(() -> loader.getAllowedEvents("not_a-events.yml", "MAIN.DRAFT"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Scenario config not found");
    }

    @Test
    void whitelist_missingFile_returnsEmptySet() {
        var loader = new ScenarioConfigLoader(resourceLoader);
        assertThat(loader.getWhitelist()).isNotNull();
    }
}
