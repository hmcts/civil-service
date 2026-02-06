package uk.gov.hmcts.reform.civil.service.dashboardnotifications.caseproceedsoffline;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.genapplication.GeneralApplicationsDetails;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.dashboard.services.DashboardNotificationService;
import uk.gov.hmcts.reform.dashboard.services.DashboardScenariosService;

import java.util.List;

@Service
public class ApplicationsProceedOfflineClaimantDashboardService extends ApplicationsProceedOfflineDashboardService {

    public ApplicationsProceedOfflineClaimantDashboardService(DashboardScenariosService dashboardScenariosService,
                                                              DashboardNotificationService dashboardNotificationService,
                                                              DashboardNotificationsParamsMapper mapper,
                                                              FeatureToggleService featureToggleService) {
        super(dashboardScenariosService, dashboardNotificationService, mapper, featureToggleService);
    }

    @Override
    protected boolean isLip(CaseData caseData) {
        return caseData.isApplicantLiP();
    }

    @Override
    protected String inactiveScenarioId() {
        return DashboardScenarios.SCENARIO_AAA6_GENERAL_APPLICATION_INITIATE_APPLICATION_INACTIVE_CLAIMANT.getScenario();
    }

    @Override
    protected String activeScenarioId() {
        return DashboardScenarios.SCENARIO_AAA6_CASE_PROCEED_IN_CASE_MAN_CLAIMANT.getScenario();
    }

    @Override
    protected String partyLabel() {
        return CLAIMANT_LABEL;
    }

    @Override
    protected List<String> partyApplicationStates(CaseData caseData) {
        List<Element<GeneralApplicationsDetails>> details = caseData.getClaimantGaAppDetails();
        if (details == null) {
            return List.of();
        }
        return details.stream()
            .map(Element::getValue)
            .map(GeneralApplicationsDetails::getCaseState)
            .toList();
    }
}
