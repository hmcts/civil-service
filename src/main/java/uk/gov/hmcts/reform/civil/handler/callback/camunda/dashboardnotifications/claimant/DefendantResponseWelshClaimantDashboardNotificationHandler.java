package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.claimant;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.callback.DashboardCallbackHandler;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.dashboard.services.DashboardNotificationService;
import uk.gov.hmcts.reform.dashboard.services.DashboardScenariosService;

import java.time.LocalDateTime;
import java.util.List;

import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_CLAIMANT_DASHBOARD_NOTIFICATION_FOR_DEFENDANT_RESPONSE_WELSH;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_DEFENDANT_RESPONSE_BILINGUAL_CLAIMANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_DEFENDANT_RESPONSE_BILINGUAL_WELSH_ENABLED_CLAIMANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_ENGLISH_DEFENDANT_RESPONSE_BILINGUAL_CLAIMANT;

@Service
public class DefendantResponseWelshClaimantDashboardNotificationHandler extends DashboardCallbackHandler {

    private static final List<CaseEvent> EVENTS =
        List.of(CREATE_CLAIMANT_DASHBOARD_NOTIFICATION_FOR_DEFENDANT_RESPONSE_WELSH);
    public static final String TASK_ID = "GenerateClaimantDashboardNotificationDefendantResponseWelsh";
    public static final String DJ_NOTIFICATION = "Notice.AAA6.DefResponse.ResponseTimeElapsed.Claimant";

    private final DashboardNotificationService dashboardNotificationService;

    public DefendantResponseWelshClaimantDashboardNotificationHandler(DashboardScenariosService dashboardScenariosService,
                                                                      DashboardNotificationsParamsMapper mapper,
                                                                      FeatureToggleService featureToggleService,
                                                                      DashboardNotificationService dashboardNotificationService) {
        super(dashboardScenariosService, mapper, featureToggleService);
        this.dashboardNotificationService = dashboardNotificationService;
    }

    @Override
    public String camundaActivityId(CallbackParams callbackParams) {
        return TASK_ID;
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    @Override
    public String getScenario(CaseData caseData) {
        if (caseData.isRespondentResponseBilingual()) {
                return SCENARIO_AAA6_DEFENDANT_RESPONSE_BILINGUAL_WELSH_ENABLED_CLAIMANT.getScenario();
        } else {
            return SCENARIO_AAA6_ENGLISH_DEFENDANT_RESPONSE_BILINGUAL_CLAIMANT.getScenario();
        }
    }

    @Override
    protected boolean shouldRecordScenario(CaseData caseData) {
        return caseData.isApplicantLiP();
    }

    @Override
    protected void beforeRecordScenario(CaseData caseData, String authToken) {
        if (caseData.getRespondent1ResponseDeadline().isBefore(LocalDateTime.now())) {
            dashboardNotificationService.deleteByNameAndReferenceAndCitizenRole(DJ_NOTIFICATION, caseData.getCcdCaseReference().toString(), "CLAIMANT");
        }
    }
}
