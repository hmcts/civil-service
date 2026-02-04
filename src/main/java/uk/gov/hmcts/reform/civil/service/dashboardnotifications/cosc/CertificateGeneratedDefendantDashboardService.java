package uk.gov.hmcts.reform.civil.service.dashboardnotifications.cosc;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardScenarioService;
import uk.gov.hmcts.reform.civil.utils.CoscHandlerUtility;
import uk.gov.hmcts.reform.dashboard.services.DashboardNotificationService;
import uk.gov.hmcts.reform.dashboard.services.DashboardScenariosService;

import java.util.Map;

import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_GENERAL_APPLICATION_AVAILABLE_DEFENDANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_PROOF_OF_DEBT_PAYMENT_APPLICATION_PROCESSED_DEFENDANT;

@Service
public class CertificateGeneratedDefendantDashboardService extends DashboardScenarioService {

    private final FeatureToggleService featureToggleService;
    private final DashboardNotificationService dashboardNotificationService;
    private final CoscDashboardHelper coscDashboardHelper;

    protected CertificateGeneratedDefendantDashboardService(DashboardScenariosService dashboardScenariosService,
                                                            DashboardNotificationsParamsMapper mapper,
                                                            FeatureToggleService featureToggleService,
                                                            DashboardNotificationService dashboardNotificationService,
                                                            CoscDashboardHelper coscDashboardHelper) {
        super(dashboardScenariosService, mapper);
        this.featureToggleService = featureToggleService;
        this.dashboardNotificationService = dashboardNotificationService;
        this.coscDashboardHelper = coscDashboardHelper;
    }

    public void notifyCertificateGenerated(CaseData caseData, String authToken) {
        recordScenario(caseData, authToken);
    }

    @Override
    protected String getScenario(CaseData caseData) {
        return SCENARIO_AAA6_PROOF_OF_DEBT_PAYMENT_APPLICATION_PROCESSED_DEFENDANT.getScenario();
    }

    @Override
    protected boolean shouldRecordScenario(CaseData caseData) {
        return featureToggleService.isLipVLipEnabled()
            && coscDashboardHelper.isMarkedPaidInFull(caseData)
            && caseData.isRespondent1NotRepresented();
    }

    @Override
    protected Map<String, Boolean> getScenarios(CaseData caseData) {
        if (shouldRecordScenario(caseData)) {
            return Map.of(
                SCENARIO_AAA6_GENERAL_APPLICATION_AVAILABLE_DEFENDANT.getScenario(),
                true
            );
        }
        return Map.of();
    }

    @Override
    protected void beforeRecordScenario(CaseData caseData, String authToken) {
        CoscHandlerUtility.addBeforeRecordScenario(caseData, dashboardNotificationService);
    }
}
