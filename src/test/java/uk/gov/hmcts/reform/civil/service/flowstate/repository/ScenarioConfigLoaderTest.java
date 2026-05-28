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
    private static final String NO_ONGOING_BP_ALLOWED_EVENTS_FILE = "no-ongoing-bp-allowed-events.yml";

    @Test
    void parsesKnownState_fromUnspecFile() {
        var loader = new ScenarioConfigLoader(resourceLoader);
        Set<CaseEvent> events = loader.getFlowStateAllowedEvents("valid-unspec-events.yml", "MAIN.DRAFT");
        assertThat(events).isNotNull().isNotEmpty();
    }

    @Test
    void returnsEmptySet_forUnknownState() {
        var loader = new ScenarioConfigLoader(resourceLoader);
        Set<CaseEvent> events = loader.getFlowStateAllowedEvents("valid-spec-events.yml", "MAIN.NOT_A_STATE");
        assertThat(events).isEmpty();
    }

    @Test
    void invalidEventName_inSpecFile_throwsHelpfulException() {
        var loader = new ScenarioConfigLoader(resourceLoader);
        assertThatThrownBy(() -> loader.getFlowStateAllowedEvents("invalid-spec-events.yml", "MAIN.DRAFT"))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("Unknown CaseEvent");
    }

    @Test
    void invalidFile_throwsHelpfulException() {
        var loader = new ScenarioConfigLoader(resourceLoader);
        assertThatThrownBy(() -> loader.getFlowStateAllowedEvents("not_a-events.yml", "MAIN.DRAFT"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Scenario config not found");
    }

    @Test
    void whitelist_missingFile_returnsEmptySet() {
        var loader = new ScenarioConfigLoader(resourceLoader);
        assertThat(loader.getWhitelist()).isNotNull();
    }

    @Test
    void parsesNoOngoingBusinessProcessAllowedEvents() {
        var loader = new ScenarioConfigLoader(resourceLoader);

        Set<CaseEvent> events = loader.getNoOngoingBPAllowedEvents(NO_ONGOING_BP_ALLOWED_EVENTS_FILE);

        assertThat(events).contains(
            CaseEvent.ADD_OR_AMEND_CLAIM_DOCUMENTS,
            CaseEvent.CHANGE_SOLICITOR_EMAIL,
            CaseEvent.CREATE_CASE_FLAGS,
            CaseEvent.EVIDENCE_UPLOAD_JUDGE,
            CaseEvent.MANAGE_CASE_FLAGS,
            CaseEvent.MANAGE_DOCUMENTS,
            CaseEvent.ORDER_REVIEW_OBLIGATION_CHECK,
            CaseEvent.UPDATE_CASE_DATA,
            CaseEvent.REMOVE_DOCUMENT,
            CaseEvent.SERVICE_REQUEST_RECEIVED
        );
    }

    @Test
    void doesNotAllowEventsThatSetOrTriggerBusinessProcessWithoutDeferral() {
        var loader = new ScenarioConfigLoader(resourceLoader);

        Set<CaseEvent> events = loader.getNoOngoingBPAllowedEvents(NO_ONGOING_BP_ALLOWED_EVENTS_FILE);

        assertThat(events).doesNotContain(
            CaseEvent.ADD_CASE_NOTE,
            CaseEvent.AMEND_RESTITCH_BUNDLE,
            CaseEvent.CREATE_BUNDLE,
            CaseEvent.GENERATE_DIRECTIONS_ORDER,
            CaseEvent.INITIATE_GENERAL_APPLICATION,
            CaseEvent.MANAGE_CONTACT_INFORMATION,
            CaseEvent.queryManagementRaiseQuery,
            CaseEvent.queryManagementRespondQuery
        );
    }

    @Test
    void noOngoingBusinessProcessAllowedEvents_missingFile_returnsEmptySet() {
        var loader = new ScenarioConfigLoader(resourceLoader);

        Set<CaseEvent> events = loader.getNoOngoingBPAllowedEvents("missing-no-ongoing-bp-allowed-events.yml");

        assertThat(events).isEmpty();
    }

    @Test
    void invalidEventName_inNoOngoingBusinessProcessAllowedEventsFile_throwsHelpfulException() {
        var loader = new ScenarioConfigLoader(resourceLoader);

        assertThatThrownBy(() -> loader.getNoOngoingBPAllowedEvents("invalid-no-ongoing-bp-allowed-events.yml"))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("Failed to load event config")
            .hasRootCauseMessage("Unknown CaseEvent 'NOT_A_REAL_EVENT'");
    }
}
