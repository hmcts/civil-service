package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.dashboard.services.DashboardScenariosService;

import java.util.List;

import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_GENERAL_APPLICATION_SUBMITTED_NONURGENT_RESPONDENT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_GENERAL_APPLICATION_SUBMITTED_URGENT_RESPONDENT;

@Slf4j
@Service
public class CreateRespondentDashboardNotificationForApplicationSubmittedHandler extends GaDashboardCallbackHandler {

    private static final List<CaseEvent> EVENTS = List.of(CaseEvent.CREATE_APPLICATION_SUBMITTED_DASHBOARD_NOTIFICATION_FOR_RESPONDENT);

    public CreateRespondentDashboardNotificationForApplicationSubmittedHandler(DashboardScenariosService dashboardScenariosService,
                                                                               DashboardNotificationsParamsMapper mapper,
                                                                               FeatureToggleService featureToggleService,
                                                                               ObjectMapper objectMapper) {
        super(dashboardScenariosService, mapper, featureToggleService, objectMapper);
    }

    @Override
    protected String getScenario(CaseData caseData) {
        if (isWithNoticeOrConsent(caseData)) {
            if (caseData.isUrgent()) {
                log.info("Case {} with notice or consent and is urgent", caseData.getCcdCaseReference());
                return SCENARIO_AAA6_GENERAL_APPLICATION_SUBMITTED_URGENT_RESPONDENT.getScenario();
            } else {
                log.info("Case {} with notice or consent and is not urgent", caseData.getCcdCaseReference());
                return SCENARIO_AAA6_GENERAL_APPLICATION_SUBMITTED_NONURGENT_RESPONDENT.getScenario();
            }
        }
        return "";
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private boolean isWithNoticeOrConsent(CaseData caseData) {
        return (YES.equals(caseData.getGeneralAppInformOtherParty().getIsWithNotice())
            || caseData.getGeneralAppConsentOrder() == YesOrNo.YES);
    }
}
