package uk.gov.hmcts.reform.civil.service.flowstate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.config.FlowStateAllowedEventsConfig;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.stateflow.simplegrammar.SimpleStateFlowBuilder;

@SpringBootTest(classes = {
    JacksonAutoConfiguration.class,
    CaseDetailsConverter.class,
    SimpleStateFlowEngine.class,
    SimpleStateFlowBuilder.class,
    TransitionsTestConfiguration.class,
    FlowStateAllowedEventsConfig.class,
    FlowStateAllowedEventService.class
}, properties = "feature.allowed-event-orchestrator.enabled=false")
class AllowedEventProviderLegacyTest extends AbstractAllowedEventProviderTest {

    @Autowired
    private FlowStateAllowedEventsConfig flowStateAllowedEventsConfig;

    @Autowired
    private FlowStateAllowedEventService allowedEventsService;

    @MockBean
    private FeatureToggleService featureToggleService;

    @Override
    protected boolean isAllowed(uk.gov.hmcts.reform.civil.model.CaseData caseData, CaseEvent event) {
        return allowedEventsService.isAllowed(caseData, event);
    }
}
