package uk.gov.hmcts.reform.civil.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ResourceLoader;
import uk.gov.hmcts.reform.civil.service.flowstate.AllowedEventService;
import uk.gov.hmcts.reform.civil.service.flowstate.IStateFlowEngine;
import uk.gov.hmcts.reform.civil.service.flowstate.repository.AllowedEventRepository;
import uk.gov.hmcts.reform.civil.service.flowstate.repository.ScenarioConfigLoader;
import uk.gov.hmcts.reform.civil.service.flowstate.scenario.AllowedEventScenario;
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
    AllowedEventService allowedEventService(
        AllowedEventRepository repo,
        IStateFlowEngine stateFlowEngine,
        List<AllowedEventScenario> scenarios
    ) {
        return new AllowedEventService(repo, stateFlowEngine, scenarios);
    }

    @Bean
    AllowedEventScenario allowedEventSpecScenario(AllowedEventRepository repo) {
        return new SpecScenario(repo);
    }

    @Bean
    AllowedEventScenario allowedEventUnspecScenario(AllowedEventRepository repo) {
        return new UnspecScenario(repo);
    }

    // Additional scenarios to be added here (Offline,Lip,Multiparty,DJ, etc...)
    // For ordering of the List<AllowedEventsScenario> use @Order or bean names (if strict precedence required).
}
