package uk.gov.hmcts.reform.civil.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ResourceLoader;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.service.flowstate.AllowedEventOrchestrator;
import uk.gov.hmcts.reform.civil.service.flowstate.IStateFlowEngine;
import uk.gov.hmcts.reform.civil.service.flowstate.repository.AllowedEventRepository;
import uk.gov.hmcts.reform.civil.service.flowstate.repository.ScenarioConfigLoader;
import uk.gov.hmcts.reform.civil.service.flowstate.scenario.AllowedEventsScenario;
import uk.gov.hmcts.reform.civil.service.flowstate.scenario.SpecScenario;
import uk.gov.hmcts.reform.civil.service.flowstate.scenario.UnspecScenario;

import java.util.List;

@Configuration
public class AllowedEventsConfig {

    @Bean
    AllowedEventRepository allowedEventRepository(ResourceLoader resourceLoader) {
        return new ScenarioConfigLoader(resourceLoader);
    }

    @Bean
    @ConditionalOnProperty(prefix = "feature.allowed-event-orchestrator", name = "enabled", havingValue = "true")
    AllowedEventOrchestrator allowedEventsOrchestrator(
        AllowedEventRepository repo,
        IStateFlowEngine stateFlowEngine,
        CaseDetailsConverter caseDetailsConverter,
        List<AllowedEventsScenario> scenarios
    ) {
        return new AllowedEventOrchestrator(repo, stateFlowEngine, caseDetailsConverter, scenarios);
    }

    @Bean
    @ConditionalOnProperty(prefix = "feature.allowed-event-orchestrator", name = "enabled", havingValue = "true")
    AllowedEventsScenario specScenario(AllowedEventRepository repo) {
        return new SpecScenario(repo);
    }

    @Bean
    @ConditionalOnProperty(prefix = "feature.allowed-event-orchestrator", name = "enabled", havingValue = "true")
    AllowedEventsScenario unspecScenario(AllowedEventRepository repo) {
        return new UnspecScenario(repo);
    }

    // Additional scenarios to be added here
    // For ordering of the List<AllowedEventsScenario> use @Order or bean names (if strict precedence required).
}
