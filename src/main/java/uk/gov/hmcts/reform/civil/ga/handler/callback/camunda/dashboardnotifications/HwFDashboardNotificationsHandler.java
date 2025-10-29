package uk.gov.hmcts.reform.civil.ga.handler.callback.camunda.dashboardnotifications;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.ga.client.DashboardApiClient;
import uk.gov.hmcts.reform.civil.enums.FeeType;
import uk.gov.hmcts.reform.civil.ga.callback.GaDashboardCallbackHandler;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.ga.service.GaDashboardNotificationsParamsMapper;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CaseEvent.APPLICANT_LIP_HWF_DASHBOARD_NOTIFICATION;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.INVALID_HWF_REFERENCE_GA;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.MORE_INFORMATION_HWF_GA;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NO_REMISSION_HWF_GA;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.PARTIAL_REMISSION_HWF_GA;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_GENERAL_APPS_HWF_INVALID_REFERENCE_APPLICANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_GENERAL_APPS_HWF_MORE_INFORMATION_APPLICANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_GENERAL_APPS_HWF_PARTIAL_REMISSION_APPLICANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_GENERAL_APPS_HWF_REJECTED_APPLICANT;

@Service
public class HwFDashboardNotificationsHandler extends GaDashboardCallbackHandler {

    private static final List<CaseEvent> EVENTS = List.of(APPLICANT_LIP_HWF_DASHBOARD_NOTIFICATION);

    public HwFDashboardNotificationsHandler(DashboardApiClient dashboardApiClient,
                                            GaDashboardNotificationsParamsMapper mapper,
                                            FeatureToggleService featureToggleService) {
        super(dashboardApiClient, mapper, featureToggleService);
    }

    public final Map<CaseEvent, String> dashboardScenarios = Map.of(
        NO_REMISSION_HWF_GA, SCENARIO_AAA6_GENERAL_APPS_HWF_REJECTED_APPLICANT.getScenario(),
        MORE_INFORMATION_HWF_GA, SCENARIO_AAA6_GENERAL_APPS_HWF_MORE_INFORMATION_APPLICANT.getScenario(),
        PARTIAL_REMISSION_HWF_GA, SCENARIO_AAA6_GENERAL_APPS_HWF_PARTIAL_REMISSION_APPLICANT.getScenario(),
        INVALID_HWF_REFERENCE_GA, SCENARIO_AAA6_GENERAL_APPS_HWF_INVALID_REFERENCE_APPLICANT.getScenario()
    );

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    @Override
    public String getScenario(GeneralApplicationCaseData caseData) {
        if (FeeType.APPLICATION == caseData.getHwfFeeType()) {
            return dashboardScenarios.get(caseData.getGaHwfDetails().getHwfCaseEvent());
        } else if (FeeType.ADDITIONAL == caseData.getHwfFeeType()) {
            return dashboardScenarios.get(caseData.getAdditionalHwfDetails().getHwfCaseEvent());
        }
        return "";
    }
}
