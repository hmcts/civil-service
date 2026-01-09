package uk.gov.hmcts.reform.civil.service.dashboardnotifications.caseproceedsoffline;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.genapplication.GADetailsRespondentSol;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.dashboard.services.DashboardNotificationService;
import uk.gov.hmcts.reform.dashboard.services.DashboardScenariosService;

import java.util.List;

@Service
public class ApplicationsProceedOfflineDefendantDashboardService extends ApplicationsProceedOfflineDashboardService {

    public ApplicationsProceedOfflineDefendantDashboardService(DashboardScenariosService dashboardScenariosService,
                                                               DashboardNotificationService dashboardNotificationService,
                                                               DashboardNotificationsParamsMapper mapper,
                                                               FeatureToggleService featureToggleService) {
        super(dashboardScenariosService, dashboardNotificationService, mapper, featureToggleService);
    }

    @Override
    protected boolean isLip(CaseData caseData) {
        return caseData.isRespondent1LiP();
    }

    @Override
    protected String inactiveScenarioId() {
        return DashboardScenarios.SCENARIO_AAA6_GENERAL_APPLICATION_INITIATE_APPLICATION_INACTIVE_DEFENDANT.getScenario();
    }

    @Override
    protected String activeScenarioId() {
        return DashboardScenarios.SCENARIO_AAA6_UPDATE_CASE_PROCEED_IN_CASE_MAN_DEFENDANT.getScenario();
    }

    @Override
    protected String partyLabel() {
        return DEFENDANT_LABEL;
    }

    @Override
    protected List<String> partyApplicationStates(CaseData caseData) {
        List<Element<GADetailsRespondentSol>> details = caseData.getRespondentSolGaAppDetails();
        if (details == null) {
            return List.of();
        }
        return details.stream()
            .map(Element::getValue)
            .map(GADetailsRespondentSol::getCaseState)
            .toList();
    }
}
