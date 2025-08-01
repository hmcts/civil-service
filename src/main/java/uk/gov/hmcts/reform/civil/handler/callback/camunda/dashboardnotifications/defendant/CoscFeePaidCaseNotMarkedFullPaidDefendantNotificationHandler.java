package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.defendant;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.callback.DashboardCallbackHandler;
import uk.gov.hmcts.reform.civil.enums.dq.GeneralApplicationTypes;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.dashboard.services.DashboardNotificationService;
import uk.gov.hmcts.reform.dashboard.services.DashboardScenariosService;

import java.util.Collections;
import java.util.List;

import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_PROOF_OF_DEBT_PAYMENT_APPLICATION_DEFENDANT;

@Service
public class CoscFeePaidCaseNotMarkedFullPaidDefendantNotificationHandler extends DashboardCallbackHandler {

    private static final List<CaseEvent> EVENTS = Collections.singletonList(CaseEvent.CREATE_DASHBOARD_NOTIFICATION_COSC_NOT_PAID_FULL_DEFENDANT);
    private static final String TASK_ID = "GenerateDashboardNotificationCoSCProcessedDefendant";
    private final DashboardNotificationService dashboardNotificationService;

    public CoscFeePaidCaseNotMarkedFullPaidDefendantNotificationHandler(DashboardScenariosService dashboardScenariosService,
                                                                        DashboardNotificationsParamsMapper mapper,
                                                                        DashboardNotificationService dashboardNotificationService,
                                                                        FeatureToggleService featureToggleService) {
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
    protected String getScenario(CaseData caseData) {
        return SCENARIO_AAA6_PROOF_OF_DEBT_PAYMENT_APPLICATION_DEFENDANT.getScenario();
    }

    @Override
    public boolean shouldRecordScenario(CaseData caseData) {
        return caseData.isRespondent1NotRepresented();
    }

    @Override
    protected void beforeRecordScenario(CaseData caseData, String authToken) {
        if (caseData.getGeneralApplications() != null && !caseData.getGeneralApplications().isEmpty()) {
            caseData.getGeneralApplications().stream()
                .filter(application ->
                            application.getValue().getGeneralAppType().getTypes().contains(GeneralApplicationTypes.CONFIRM_CCJ_DEBT_PAID))
                .findFirst()
                .ifPresent(coscApplication -> dashboardNotificationService.deleteByReferenceAndCitizenRole(
                               coscApplication.getValue().getCaseLink().getCaseReference(),
                               "APPLICANT"
                ));
        }
    }
}
