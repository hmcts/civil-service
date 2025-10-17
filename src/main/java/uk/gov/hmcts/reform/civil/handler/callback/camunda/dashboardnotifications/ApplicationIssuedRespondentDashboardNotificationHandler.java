package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.GeneralAppFeesService;
import uk.gov.hmcts.reform.dashboard.services.DashboardScenariosService;

import java.util.List;

import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_GENERAL_APPLICATION_SUBMITTED_NONURGENT_RESPONDENT;

@Service
public class ApplicationIssuedRespondentDashboardNotificationHandler extends GaDashboardCallbackHandler {

    private static final List<CaseEvent> EVENTS = List.of(CaseEvent.CREATE_DASHBOARD_NOTIFICATION_FOR_GA_RESPONDENT);
    private final GeneralAppFeesService generalAppFeesService;

    public ApplicationIssuedRespondentDashboardNotificationHandler(DashboardScenariosService dashboardScenariosService,
                                                                   DashboardNotificationsParamsMapper mapper,
                                                                   FeatureToggleService featureToggleService,
                                                                   GeneralAppFeesService generalAppFeesService,
                                                                   ObjectMapper objectMapper) {
        super(dashboardScenariosService, mapper, featureToggleService, objectMapper);
        this.generalAppFeesService = generalAppFeesService;
    }

    @Override
    protected String getScenario(CaseData caseData) {
        if (generalAppFeesService.isFreeApplication(caseData)) {
            return SCENARIO_AAA6_GENERAL_APPLICATION_SUBMITTED_NONURGENT_RESPONDENT.getScenario();
        }
        return "";
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }
}
