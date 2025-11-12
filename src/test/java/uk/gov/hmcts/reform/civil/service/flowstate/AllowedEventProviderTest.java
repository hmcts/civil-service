package uk.gov.hmcts.reform.civil.service.flowstate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.config.AllowedEventsConfig;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.flowstate.repository.AllowedEventRepository;
import uk.gov.hmcts.reform.civil.stateflow.simplegrammar.SimpleStateFlowBuilder;

@SpringBootTest(classes = {
    JacksonAutoConfiguration.class,
    CaseDetailsConverter.class,
    SimpleStateFlowEngine.class,
    SimpleStateFlowBuilder.class,
    TransitionsTestConfiguration.class,
    AllowedEventsConfig.class,
    AllowedEventOrchestrator.class
}, properties = "feature.allowed-event-orchestrator.enabled=true")
class AllowedEventProviderTest extends AbstractAllowedEventProviderTest {

    @Autowired
    private AllowedEventRepository allowedEventRepository;

    @Autowired
    private AllowedEventOrchestrator allowedEventOrchestrator;

    @MockBean
    private FeatureToggleService featureToggleService;

    @Override
    protected boolean isAllowed(uk.gov.hmcts.reform.civil.model.CaseData caseData, CaseEvent event) {
        return allowedEventOrchestrator.isAllowed(caseData, event);
    }

}
