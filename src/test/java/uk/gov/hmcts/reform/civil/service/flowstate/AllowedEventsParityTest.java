package uk.gov.hmcts.reform.civil.service.flowstate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.hmcts.reform.civil.config.FlowStateAllowedEventsConfig;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.stateflow.simplegrammar.SimpleStateFlowBuilder;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.testsupport.mockito.MockitoBean;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = {
    JacksonAutoConfiguration.class,
    CaseDetailsConverter.class,
    SimpleStateFlowEngine.class,
    SimpleStateFlowBuilder.class,
    TransitionsTestConfiguration.class,
    FlowStateAllowedEventsConfig.class,
    FlowStateAllowedEventService.class
})
class AllowedEventsParityTest {

    @SuppressWarnings("unused")
    @MockitoBean
    private FeatureToggleService toggleService;

    @Autowired
    private FlowStateAllowedEventsConfig flowStateAllowedEventsConfig;

    @SuppressWarnings("unchecked")
    private Map<String, List<CaseEvent>> readStaticMap(String fieldName) throws Exception {
        Field f = FlowStateAllowedEventsConfig.class.getDeclaredField(fieldName);
        f.setAccessible(true);
        return (Map<String, List<CaseEvent>>) f.get(flowStateAllowedEventsConfig);
    }

    private static List<CaseEvent> toList(CaseEvent[] array) {
        return Arrays.asList(array);
    }

    @Test
    void unspecMap_has_full_parity_with_provider_matrix() throws Exception {
        Map<String, List<CaseEvent>> prod = readStaticMap("allowedEvents");

        FlowStateAllowedEventServiceTest.GetAllowedCaseEventForFlowStateArguments provider =
            new FlowStateAllowedEventServiceTest.GetAllowedCaseEventForFlowStateArguments();

        Map<String, List<CaseEvent>> expected = provider.provideArguments(null)
            .collect(Collectors.toMap(
                a -> {
                    Object[] arr = a.get();
                    FlowState.Main state = (FlowState.Main) arr[0];
                    return state.fullName();
                },
                a -> {
                    Object[] arr = a.get();
                    CaseEvent[] events = (CaseEvent[]) arr[1];
                    return toList(events);
                }
            ));

        assertThat(expected.keySet())
            .as("Provider states must match production UNSPEC map states")
            .containsExactlyInAnyOrderElementsOf(prod.keySet());

        for (String state : prod.keySet()) {
            assertThat(expected.get(state))
                .as("Expected matrix contains duplicate events for state %s", state)
                .doesNotHaveDuplicates();
            assertThat(prod.get(state))
                .as("Production UNSPEC map contains duplicate events for state %s", state)
                .doesNotHaveDuplicates();
            assertThat(expected.get(state))
                .as("Events mismatch for state %s", state)
                .containsExactlyInAnyOrderElementsOf(prod.get(state));
        }
    }

    @Test
    void specMap_has_full_parity_with_provider_matrix() throws Exception {
        Map<String, List<CaseEvent>> prod = readStaticMap("allowedEventsSpec");

        FlowStateAllowedEventServiceTest.GetAllowedCaseEventForFlowStateArgumentsSpec provider =
            new FlowStateAllowedEventServiceTest.GetAllowedCaseEventForFlowStateArgumentsSpec();

        Map<String, List<CaseEvent>> expected = provider.provideArguments(null)
            .collect(Collectors.toMap(
                a -> {
                    Object[] arr = a.get();
                    FlowState.Main state = (FlowState.Main) arr[0];
                    return state.fullName();
                },
                a -> {
                    Object[] arr = a.get();
                    CaseEvent[] events = (CaseEvent[]) arr[1];
                    return toList(events);
                }
            ));

        assertThat(expected.keySet())
            .as("Provider states must match production SPEC map states")
            .containsExactlyInAnyOrderElementsOf(prod.keySet());

        for (String state : prod.keySet()) {
            assertThat(expected.get(state))
                .as("Expected SPEC matrix contains duplicate events for state %s", state)
                .doesNotHaveDuplicates();
            assertThat(prod.get(state))
                .as("Production SPEC map contains duplicate events for state %s", state)
                .doesNotHaveDuplicates();
            assertThat(expected.get(state))
                .as("SPEC events mismatch for state %s", state)
                .containsExactlyInAnyOrderElementsOf(prod.get(state));
        }
    }
}
