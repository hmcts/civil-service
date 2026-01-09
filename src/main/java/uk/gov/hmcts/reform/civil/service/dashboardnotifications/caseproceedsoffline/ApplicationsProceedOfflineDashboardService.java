package uk.gov.hmcts.reform.civil.service.dashboardnotifications.caseproceedsoffline;

import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.genapplication.GeneralApplication;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.dashboard.data.ScenarioRequestParams;
import uk.gov.hmcts.reform.dashboard.services.DashboardNotificationService;
import uk.gov.hmcts.reform.dashboard.services.DashboardScenariosService;

import java.util.List;

abstract class ApplicationsProceedOfflineDashboardService {

    private final DashboardScenariosService dashboardScenariosService;
    private final DashboardNotificationService dashboardNotificationService;
    private final DashboardNotificationsParamsMapper mapper;
    private final FeatureToggleService featureToggleService;

    private static final List<String> NON_LIVE_STATES = List.of(
        "Application Closed",
        "Order Made",
        "Application Dismissed"
    );

    protected static final String CLAIMANT_LABEL = "Claimant";
    protected static final String DEFENDANT_LABEL = "Defendant";

    ApplicationsProceedOfflineDashboardService(DashboardScenariosService dashboardScenariosService,
                                               DashboardNotificationService dashboardNotificationService,
                                               DashboardNotificationsParamsMapper mapper,
                                               FeatureToggleService featureToggleService) {
        this.dashboardScenariosService = dashboardScenariosService;
        this.dashboardNotificationService = dashboardNotificationService;
        this.mapper = mapper;
        this.featureToggleService = featureToggleService;
    }

    public void notify(CaseData caseData, String authToken) {
        if (!featureToggleService.isLipVLipEnabled()) {
            return;
        }
        if (!CaseState.PROCEEDS_IN_HERITAGE_SYSTEM.equals(caseData.getCcdState())) {
            return;
        }
        if (!isLip(caseData)) {
            return;
        }

        dashboardNotificationService.deleteByReferenceAndCitizenRole(
            caseData.getCcdCaseReference().toString(),
            partyLabel()
        );

        ScenarioRequestParams params = ScenarioRequestParams.builder()
            .params(mapper.mapCaseDataToParams(caseData))
            .build();

        dashboardScenariosService.recordScenarios(
            authToken,
            inactiveScenarioId(),
            caseData.getCcdCaseReference().toString(),
            params
        );

        if (hasLiveApplications(caseData)) {
            dashboardScenariosService.recordScenarios(
                authToken,
                activeScenarioId(),
                caseData.getCcdCaseReference().toString(),
                params
            );
        }
    }

    private boolean hasLiveApplications(CaseData caseData) {
        List<Element<GeneralApplication>> applications = caseData.getGeneralApplications();
        if (applications == null || applications.isEmpty()) {
            return false;
        }
        List<String> states = partyApplicationStates(caseData);
        if (states == null || states.isEmpty()) {
            return false;
        }
        return states.stream().anyMatch(state -> !NON_LIVE_STATES.contains(state));
    }

    protected abstract boolean isLip(CaseData caseData);

    protected abstract String inactiveScenarioId();

    protected abstract String activeScenarioId();

    protected abstract String partyLabel();

    protected abstract List<String> partyApplicationStates(CaseData caseData);
}
