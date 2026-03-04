package uk.gov.hmcts.reform.civil.ga.service.dashboardnotifications.makedecision.scenario;

import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;

import java.util.Optional;

public interface DecisionRule {

    Optional<String> evaluate(GeneralApplicationCaseData caseData, DecisionContext context);
}
