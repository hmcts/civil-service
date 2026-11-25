package uk.gov.hmcts.reform.civil.service.flowstate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.stateflow.StateFlow;
import uk.gov.hmcts.reform.civil.stateflow.simplegrammar.SimpleStateFlowBuilder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SimpleStateFlowEngineBuilderTest {

    @Test
    @SuppressWarnings("unchecked")
    void evaluateSpecBuildsANewStateFlow() {
        SimpleStateFlowBuilder builder = mock(SimpleStateFlowBuilder.class);
        StateFlow stateFlow = mock(StateFlow.class);
        when(builder.build(FlowState.Main.SPEC_DRAFT)).thenReturn(stateFlow);
        when(stateFlow.evaluate(any())).thenReturn(stateFlow);
        ObjectProvider<SimpleStateFlowBuilder> stateFlowBuilders = mock(ObjectProvider.class);
        when(stateFlowBuilders.getObject()).thenReturn(builder);

        SimpleStateFlowEngine engine = new SimpleStateFlowEngine(
            mock(CaseDetailsConverter.class),
            mock(FeatureToggleService.class),
            stateFlowBuilders
        );

        assertThat(engine.evaluateSpec(CaseData.builder().build())).isSameAs(stateFlow);
        verify(stateFlowBuilders).getObject();
    }
}
