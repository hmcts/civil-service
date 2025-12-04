package uk.gov.hmcts.reform.civil.ga.handler.callback.camunda.dashboardnotifications;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.ga.client.DashboardApiClient;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.ga.callback.GaDashboardCallbackHandler;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.ga.service.GaDashboardNotificationsParamsMapper;

import java.util.List;

import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_GENERAL_APPLICATION_SUBMITTED_NONURGENT_RESPONDENT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_GENERAL_APPLICATION_SUBMITTED_URGENT_RESPONDENT;

@Slf4j
@Service
public class CreateRespondentDashboardNotificationForApplicationSubmittedHandler extends GaDashboardCallbackHandler {

    private static final List<CaseEvent> EVENTS = List.of(CaseEvent.CREATE_APPLICATION_SUBMITTED_DASHBOARD_NOTIFICATION_FOR_RESPONDENT);

    public CreateRespondentDashboardNotificationForApplicationSubmittedHandler(DashboardApiClient dashboardApiClient,
                                                                               GaDashboardNotificationsParamsMapper mapper,
                                                                               FeatureToggleService featureToggleService) {
        super(dashboardApiClient, mapper, featureToggleService);
    }

    @Override
    protected String getScenario(GeneralApplicationCaseData caseData) {
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

    private boolean isWithNoticeOrConsent(GeneralApplicationCaseData caseData) {
        return (YES.equals(caseData.getGeneralAppInformOtherParty().getIsWithNotice())
            || caseData.getGeneralAppConsentOrder() == YesOrNo.YES);
    }
}
