package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.callback.DashboardCallbackHandler;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.dashboard.services.DashboardScenariosService;

import java.util.List;

import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_GENERAL_APPLICATION_ORDER_MADE_RESPONDENT;

@Service
public class CreateDashboardNotificationWhenFinalOrderMadeRespondentHandler extends DashboardCallbackHandler {

    private static final List<CaseEvent> EVENTS = List.of(CaseEvent.CREATE_RESPONDENT_DASHBOARD_NOTIFICATION_ORDER_MADE);

    public CreateDashboardNotificationWhenFinalOrderMadeRespondentHandler(DashboardScenariosService dashboardScenariosService,
                                                                          DashboardNotificationsParamsMapper mapper,
                                                                          FeatureToggleService featureToggleService) {
        super(dashboardScenariosService, mapper, featureToggleService);
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    @Override
    public String getScenario(CaseData caseData) {
        if (isWithNoticeOrConsent(caseData)) {
            return SCENARIO_AAA6_GENERAL_APPLICATION_ORDER_MADE_RESPONDENT.getScenario();
        }
        return "";
    }

    private boolean isWithNoticeOrConsent(CaseData caseData) {
        return YES.equals(caseData.getGeneralAppInformOtherParty().getIsWithNotice())
            || caseData.getGeneralAppConsentOrder() == YES;
    }
}
