package uk.gov.hmcts.reform.civil.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.service.flowstate.AllowedEventsOrchestrator;
import uk.gov.hmcts.reform.civil.service.flowstate.IStateFlowEngine;
import uk.gov.hmcts.reform.civil.service.flowstate.scenario.AllowedEventsScenario;
import uk.gov.hmcts.reform.civil.service.flowstate.scenario.OneVOneSpecScenario;

import java.util.List;

@Configuration
public class AllowedEventsConfig {

    @Bean
    @ConditionalOnProperty(prefix = "feature.allowed-events-orchestrator", name = "enabled", havingValue = "true")
    AllowedEventsOrchestrator allowedEventsOrchestrator(
        FlowStateAllowedEventsConfig config,
        IStateFlowEngine stateFlowEngine,
        CaseDetailsConverter caseDetailsConverter,
        List<AllowedEventsScenario> scenarios
    ) {
        return new AllowedEventsOrchestrator(config, stateFlowEngine, caseDetailsConverter, scenarios);
    }

    @Bean
    @ConditionalOnProperty(prefix = "feature.allowed-events-orchestrator", name = "enabled", havingValue = "true")
    AllowedEventsScenario oneVOneSpecScenario(FlowStateAllowedEventsConfig config) {
        return new OneVOneSpecScenario(config);
    }

    // Additional scenarios to be added here
    // For ordering of the List<AllowedEventsScenario> use @Order or bean names (if strict precedence required).
}
