package uk.gov.hmcts.reform.civil.service.dashboardnotifications.hearingfeeunpaid;

import static java.util.Objects.isNull;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_HEARING_FEE_UNPAID_DEFENDANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_HEARING_FEE_UNPAID_TRIAL_READY_DEFENDANT;

import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardScenarioService;
import uk.gov.hmcts.reform.civil.service.sdo.SdoCaseClassificationService;
import uk.gov.hmcts.reform.dashboard.services.DashboardNotificationService;
import uk.gov.hmcts.reform.dashboard.services.DashboardScenariosService;
import uk.gov.hmcts.reform.dashboard.services.TaskListService;

import org.springframework.stereotype.Service;

@Service
public class HearingFeeUnpaidDefendantNotificationService extends DashboardScenarioService {

    private final DashboardNotificationService dashboardNotificationService;
    private final TaskListService taskListService;
    private final SdoCaseClassificationService sdoCaseClassificationService;

    public HearingFeeUnpaidDefendantNotificationService(DashboardScenariosService dashboardScenariosService,
                                                        DashboardNotificationService dashboardNotificationService,
                                                        DashboardNotificationsParamsMapper mapper,
                                                        TaskListService taskListService,
                                                        SdoCaseClassificationService sdoCaseClassificationService) {
        super(dashboardScenariosService, mapper);
        this.dashboardNotificationService = dashboardNotificationService;
        this.taskListService = taskListService;
        this.sdoCaseClassificationService = sdoCaseClassificationService;
    }

    public void notifyHearingFeeUnpaid(CaseData caseData, String authToken) {
        recordScenario(caseData, authToken);
    }

    @Override
    public String getScenario(CaseData caseData) {
        return isNull(caseData.getTrialReadyRespondent1()) && sdoCaseClassificationService.isFastTrack(caseData)
            ? SCENARIO_AAA6_HEARING_FEE_UNPAID_DEFENDANT.getScenario()
            : SCENARIO_AAA6_HEARING_FEE_UNPAID_TRIAL_READY_DEFENDANT.getScenario();
    }

    @Override
    public boolean shouldRecordScenario(CaseData caseData) {
        return caseData.isRespondent1NotRepresented();
    }

    @Override
    protected void beforeRecordScenario(CaseData caseData, String authToken) {
        dashboardNotificationService.deleteByReferenceAndCitizenRole(
            String.valueOf(caseData.getCcdCaseReference()),
            DEFENDANT_ROLE
        );
    }
}
