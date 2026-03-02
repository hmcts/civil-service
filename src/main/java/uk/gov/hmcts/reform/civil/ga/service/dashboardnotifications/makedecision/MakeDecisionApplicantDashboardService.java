package uk.gov.hmcts.reform.civil.ga.service.dashboardnotifications.makedecision;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.ga.client.DashboardApiClient;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.ga.service.GaDashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.civil.ga.service.JudicialDecisionHelper;
import uk.gov.hmcts.reform.civil.ga.service.dashboardnotifications.GaDashboardScenarioService;
import uk.gov.hmcts.reform.civil.ga.service.dashboardnotifications.makedecision.scenario.DecisionContext;
import uk.gov.hmcts.reform.civil.ga.service.dashboardnotifications.makedecision.scenario.DecisionRule;
import uk.gov.hmcts.reform.civil.ga.service.dashboardnotifications.makedecision.scenario.HearingScheduledRule;
import uk.gov.hmcts.reform.civil.ga.service.dashboardnotifications.makedecision.scenario.OrderMadeRule;
import uk.gov.hmcts.reform.civil.ga.service.dashboardnotifications.makedecision.scenario.RequestMoreInfoApplicantRule;
import uk.gov.hmcts.reform.civil.ga.service.dashboardnotifications.makedecision.scenario.WrittenRepresentationsRule;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_GENERAL_APPLICATION_HEARING_SCHEDULED_APPLICANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_GENERAL_APPLICATION_ORDER_MADE_APPLICANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_GENERAL_APPLICATION_WRITTEN_REPRESENTATION_REQUIRED_APPLICANT;

@Service
public class MakeDecisionApplicantDashboardService extends GaDashboardScenarioService {

    private final List<DecisionRule> rules;

    public MakeDecisionApplicantDashboardService(DashboardApiClient dashboardApiClient,
                                                 GaDashboardNotificationsParamsMapper mapper,
                                                 JudicialDecisionHelper judicialDecisionHelper) {
        super(dashboardApiClient, mapper);
        this.rules = List.of(
            new RequestMoreInfoApplicantRule(judicialDecisionHelper),
            new HearingScheduledRule(SCENARIO_AAA6_GENERAL_APPLICATION_HEARING_SCHEDULED_APPLICANT),
            new WrittenRepresentationsRule(SCENARIO_AAA6_GENERAL_APPLICATION_WRITTEN_REPRESENTATION_REQUIRED_APPLICANT),
            new OrderMadeRule(SCENARIO_AAA6_GENERAL_APPLICATION_ORDER_MADE_APPLICANT)
        );
    }

    public void notifyMakeDecision(GeneralApplicationCaseData caseData, String authToken) {
        recordScenario(caseData, authToken);
    }

    @Override
    protected boolean shouldRecordScenario(GeneralApplicationCaseData caseData) {
        return Objects.nonNull(caseData.getIsGaApplicantLip())
            && caseData.getIsGaApplicantLip().equals(YES);
    }

    @Override
    protected String getScenario(GeneralApplicationCaseData caseData) {
        DecisionContext context = DecisionContext.from(caseData);
        return rules.stream()
            .map(rule -> rule.evaluate(caseData, context))
            .flatMap(Optional::stream)
            .findFirst()
            .orElse("");
    }
}
