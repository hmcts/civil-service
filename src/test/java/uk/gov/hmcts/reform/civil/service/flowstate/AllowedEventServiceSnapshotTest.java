package uk.gov.hmcts.reform.civil.service.flowstate;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.yaml.snakeyaml.Yaml;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.config.AllowedEventsConfig;
import uk.gov.hmcts.reform.civil.enums.CaseCategory;
import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.flowstate.wrapper.CaseDataDirector;
import uk.gov.hmcts.reform.civil.stateflow.simplegrammar.SimpleStateFlowBuilder;
import uk.gov.hmcts.reform.civil.testsupport.mockito.MockitoBean;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.of;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_ONE;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_TWO_ONE_LEGAL_REP;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_TWO_TWO_LEGAL_REP;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.TWO_V_ONE;

@SpringBootTest(classes = {
    JacksonAutoConfiguration.class,
    CaseDetailsConverter.class,
    SimpleStateFlowEngine.class,
    SimpleStateFlowBuilder.class,
    TransitionsTestConfiguration.class,
    AllowedEventsConfig.class,
    AllowedEventService.class
})
@SuppressWarnings("unused")
class AllowedEventServiceSnapshotTest {

    private static final String TEST_CASES_FILE = "allowed-test-cases.yml";

    @Autowired
    private AllowedEventService allowedEventService;

    @MockitoBean
    private FeatureToggleService featureToggleService;
    @MockitoBean
    private CaseDetailsConverter caseDetailsConverter;

    private static List<CaseEvent> whitelistEvents;

    @BeforeAll
    static void setup() {
        whitelistEvents = List.of(
            CaseEvent.ADD_CASE_NOTE,
            CaseEvent.CASE_PROCEEDS_IN_CASEMAN,
            CaseEvent.DISPATCH_BUSINESS_PROCESS,
            CaseEvent.MANAGE_CONTACT_INFORMATION,
            CaseEvent.migrateCase,
            CaseEvent.NOTIFY_HEARING_PARTIES,
            CaseEvent.queryManagementRaiseQuery,
            CaseEvent.queryManagementRespondQuery,
            CaseEvent.REMOVE_DOCUMENT,
            CaseEvent.SEND_AND_REPLY,
            CaseEvent.UPDATE_NEXT_HEARING_DETAILS,
            CaseEvent.UpdateNextHearingInfo
        );
    }

    static Stream<Arguments> whitelist() {
        return whitelistEvents.stream().map(Arguments::of);
    }

    @SuppressWarnings("unchecked")
    static Stream<Arguments> flowStateCaseEventSnapshot() {
        Yaml yaml = new Yaml();
        InputStream is = AllowedEventServiceSnapshotTest.class.getResourceAsStream("/config/" + TEST_CASES_FILE);

        if (is == null) {
            throw new IllegalStateException("Test resource '" + TEST_CASES_FILE + "' not found in classpath.");
        }

        List<Map<String, Object>> stateEntries = yaml.load(is);

        return stateEntries.stream()
            .flatMap(stateEntry -> {
                FlowState.Main state = FlowState.Main.valueOf((String) stateEntry.get("state"));

                Object eventsObj = stateEntry.get("events");
                List<Map<String, Object>> events;

                if (eventsObj instanceof List) {
                    events = ((List<?>) eventsObj).stream()
                        .filter(Map.class::isInstance)
                        .map(item -> (Map<String, Object>) item)
                        .toList();
                } else {
                    events = List.of();
                }

                return events.stream().map(eventEntry -> {
                    CaseEvent event = CaseEvent.valueOf((String) eventEntry.get("event"));

                    List<MultiPartyScenario> unspec = (eventEntry.get("unspec") instanceof List)
                        ? ((List<?>) eventEntry.get("unspec")).stream()
                        .map(item -> MultiPartyScenario.valueOf((String) item))
                        .toList()
                        : List.of();

                    List<MultiPartyScenario> spec = (eventEntry.get("spec") instanceof List)
                        ? ((List<?>) eventEntry.get("spec")).stream()
                        .map(item -> MultiPartyScenario.valueOf((String) item))
                        .toList()
                        : List.of();

                    return of(state, event, unspec, spec);
                });
            });
    }

    @ParameterizedTest(name = "Whitelist {0}")
    @MethodSource("whitelist")
    void flowStateCaseEvents_whitelist(CaseEvent event) {
        CaseDetails caseDetails = mock(CaseDetails.class);
        assertThat(isAllowed(caseDetails, event))
            .as("Scenario whitelist: %s", event)
            .isTrue();
    }

    @ParameterizedTest(name = "FlowState CaseEvent 1v1 UnSpec {0} {1}")
    @MethodSource("flowStateCaseEventSnapshot")
    void flowStateCaseEvents_1v1_Unspec(FlowState.Main state, CaseEvent event,
                                        List<MultiPartyScenario> unspec, List<MultiPartyScenario> spec) {
        MultiPartyScenario party = ONE_V_ONE;
        CaseCategory category = CaseCategory.UNSPEC_CLAIM;
        var expected = unspec.contains(party);

        CaseDetails caseDetails = mock(CaseDetails.class);
        CaseData caseData = buildCaseData(party, category, state);
        when(caseDetailsConverter.toCaseData(caseDetails)).thenReturn(caseData);

        assertThat(isAllowed(caseDetails, event))
            .as("Scenario: %s %s %s %s", party, category, state, event)
            .isEqualTo(expected);
    }

    @ParameterizedTest(name = "FlowState CaseEvent 1v1 Spec {0} {1}")
    @MethodSource("flowStateCaseEventSnapshot")
    void flowStateCaseEvents_1v1_Spec(FlowState.Main state, CaseEvent event,
                                      List<MultiPartyScenario> unspec, List<MultiPartyScenario> spec) {
        MultiPartyScenario party = ONE_V_ONE;
        CaseCategory category = CaseCategory.SPEC_CLAIM;
        var expected = spec.contains(party);

        CaseDetails caseDetails = mock(CaseDetails.class);
        CaseData caseData = buildCaseData(party, category, state);
        when(caseDetailsConverter.toCaseData(caseDetails)).thenReturn(caseData);

        assertThat(isAllowed(caseDetails, event))
            .as("Scenario: %s %s %s %s", party, category, state, event)
            .isEqualTo(expected);
    }

    @ParameterizedTest(name = "FlowState CaseEvent 1v2 1LR UnSpec {0} {1}")
    @MethodSource("flowStateCaseEventSnapshot")
    void flowStateCaseEvents_1v2_1LR_Unspec(FlowState.Main state, CaseEvent event,
                                            List<MultiPartyScenario> unspec, List<MultiPartyScenario> spec) {
        MultiPartyScenario party = ONE_V_TWO_ONE_LEGAL_REP;
        CaseCategory category = CaseCategory.UNSPEC_CLAIM;
        var expected = unspec.contains(party);

        CaseDetails caseDetails = mock(CaseDetails.class);
        CaseData caseData = buildCaseData(party, category, state);
        when(caseDetailsConverter.toCaseData(caseDetails)).thenReturn(caseData);

        assertThat(isAllowed(caseDetails, event))
            .as("Scenario: %s %s %s %s", party, category, state, event)
            .isEqualTo(expected);
    }

    @ParameterizedTest(name = "FlowState CaseEvent 1v2 1LR Spec {0} {1}")
    @MethodSource("flowStateCaseEventSnapshot")
    void flowStateCaseEvents_1v2_1LR_Spec(FlowState.Main state, CaseEvent event,
                                          List<MultiPartyScenario> unspec, List<MultiPartyScenario> spec) {
        MultiPartyScenario party = ONE_V_TWO_ONE_LEGAL_REP;
        CaseCategory category = CaseCategory.SPEC_CLAIM;
        var expected = spec.contains(party);

        CaseDetails caseDetails = mock(CaseDetails.class);
        CaseData caseData = buildCaseData(party, category, state);
        when(caseDetailsConverter.toCaseData(caseDetails)).thenReturn(caseData);

        assertThat(isAllowed(caseDetails, event))
            .as("Scenario: %s %s %s %s", party, category, state, event)
            .isEqualTo(expected);
    }

    @ParameterizedTest(name = "FlowState CaseEvent 1v2 2LR UnSpec {0} {1}")
    @MethodSource("flowStateCaseEventSnapshot")
    void flowStateCaseEvents_1v2_2LR_Unspec(FlowState.Main state, CaseEvent event,
                                            List<MultiPartyScenario> unspec, List<MultiPartyScenario> spec) {
        MultiPartyScenario party = ONE_V_TWO_TWO_LEGAL_REP;
        CaseCategory category = CaseCategory.UNSPEC_CLAIM;
        var expected = unspec.contains(party);

        CaseDetails caseDetails = mock(CaseDetails.class);
        CaseData caseData = buildCaseData(party, category, state);
        when(caseDetailsConverter.toCaseData(caseDetails)).thenReturn(caseData);

        assertThat(isAllowed(caseDetails, event))
            .as("Scenario: %s %s %s %s", party, category, state, event)
            .isEqualTo(expected);
    }

    @ParameterizedTest(name = "FlowState CaseEvent 1v2 2LR Spec {0} {1}")
    @MethodSource("flowStateCaseEventSnapshot")
    void flowStateCaseEvents_1v1_2LR_Spec(FlowState.Main state, CaseEvent event,
                                          List<MultiPartyScenario> unspec, List<MultiPartyScenario> spec) {
        MultiPartyScenario party = ONE_V_TWO_TWO_LEGAL_REP;
        CaseCategory category = CaseCategory.SPEC_CLAIM;
        var expected = spec.contains(party);

        CaseDetails caseDetails = mock(CaseDetails.class);
        CaseData caseData = buildCaseData(party, category, state);
        when(caseDetailsConverter.toCaseData(caseDetails)).thenReturn(caseData);

        assertThat(isAllowed(caseDetails, event))
            .as("Scenario: %s %s %s %s", party, category, state, event)
            .isEqualTo(expected);
    }

    @ParameterizedTest(name = "FlowState CaseEvent 1v2 2LR UnSpec {0} {1}")
    @MethodSource("flowStateCaseEventSnapshot")
    void flowStateCaseEvents_2v1_Unspec(FlowState.Main state, CaseEvent event,
                                        List<MultiPartyScenario> unspec, List<MultiPartyScenario> spec) {
        MultiPartyScenario party = TWO_V_ONE;
        CaseCategory category = CaseCategory.UNSPEC_CLAIM;
        var expected = unspec.contains(party);

        CaseDetails caseDetails = mock(CaseDetails.class);
        CaseData caseData = buildCaseData(party, category, state);
        when(caseDetailsConverter.toCaseData(caseDetails)).thenReturn(caseData);

        assertThat(isAllowed(caseDetails, event))
            .as("Scenario: %s %s %s %s", party, category, state, event)
            .isEqualTo(expected);
    }

    @ParameterizedTest(name = "FlowState CaseEvent 1v2 2LR Spec {0} {1}")
    @MethodSource("flowStateCaseEventSnapshot")
    void flowStateCaseEvents_2v1_Spec(FlowState.Main state, CaseEvent event,
                                      List<MultiPartyScenario> unspec, List<MultiPartyScenario> spec) {
        MultiPartyScenario party = TWO_V_ONE;
        CaseCategory category = CaseCategory.SPEC_CLAIM;
        var expected = spec.contains(party);

        CaseDetails caseDetails = mock(CaseDetails.class);
        CaseData caseData = buildCaseData(party, category, state);
        when(caseDetailsConverter.toCaseData(caseDetails)).thenReturn(caseData);

        assertThat(isAllowed(caseDetails, event))
            .as("Scenario: %s %s %s %s", party, category, state, event)
            .isEqualTo(expected);
    }

    private boolean isAllowed(CaseDetails caseDetails, CaseEvent event) {
        return allowedEventService.isAllowed(caseDetails, event);
    }

    private CaseData buildCaseData(MultiPartyScenario party, CaseCategory category, FlowState.Main state) {
        CaseDataDirector caseDataDirector = new CaseDataDirector();
        caseDataDirector.party(party);
        caseDataDirector.category(category);
        return caseDataDirector.buildCaseData(state);
    }
}
