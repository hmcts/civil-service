package uk.gov.hmcts.reform.civil.ga.handler.callback.camunda.dashboardnotifications;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.ga.client.DashboardApiClient;
import uk.gov.hmcts.reform.civil.ga.callback.GaDashboardCallbackHandler;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.ga.service.GaDashboardNotificationsParamsMapper;

import java.util.List;

import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_GENERAL_APPLICATION_ORDER_MADE_APPLICANT;

@Service
public class CreateDashboardNotificationWhenFinalOrderMadeApplicantHandler extends GaDashboardCallbackHandler {

    private static final List<CaseEvent> EVENTS = List.of(CaseEvent.CREATE_APPLICANT_DASHBOARD_NOTIFICATION_ORDER_MADE);

    public CreateDashboardNotificationWhenFinalOrderMadeApplicantHandler(DashboardApiClient dashboardApiClient,
                                                                         GaDashboardNotificationsParamsMapper mapper,
                                                                         FeatureToggleService featureToggleService) {
        super(dashboardApiClient, mapper, featureToggleService);
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    @Override
    public String getScenario(GeneralApplicationCaseData caseData) {
        return SCENARIO_AAA6_GENERAL_APPLICATION_ORDER_MADE_APPLICANT.getScenario();
    }
}
