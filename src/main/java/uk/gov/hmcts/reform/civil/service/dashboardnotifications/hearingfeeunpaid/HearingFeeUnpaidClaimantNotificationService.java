package uk.gov.hmcts.reform.civil.service.dashboardnotifications.hearingfeeunpaid;

import static java.util.Objects.isNull;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_HEARING_FEE_UNPAID_CLAIMANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_HEARING_FEE_UNPAID_TRIAL_READY_CLAIMANT;

import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardScenarioService;
import uk.gov.hmcts.reform.civil.service.sdo.SdoCaseClassificationService;
import uk.gov.hmcts.reform.dashboard.services.DashboardNotificationService;
import uk.gov.hmcts.reform.dashboard.services.DashboardScenariosService;
import uk.gov.hmcts.reform.dashboard.services.TaskListService;

import org.springframework.stereotype.Service;

@Service
public class HearingFeeUnpaidClaimantNotificationService extends DashboardScenarioService {

    private final DashboardNotificationService dashboardNotificationService;
    private final TaskListService taskListService;
    private final SdoCaseClassificationService sdoCaseClassificationService;

    public HearingFeeUnpaidClaimantNotificationService(DashboardScenariosService dashboardScenariosService,
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
        return isNull(caseData.getTrialReadyApplicant()) && sdoCaseClassificationService.isFastTrack(caseData)
            ? SCENARIO_AAA6_HEARING_FEE_UNPAID_CLAIMANT.getScenario()
            : SCENARIO_AAA6_HEARING_FEE_UNPAID_TRIAL_READY_CLAIMANT.getScenario();
    }

    @Override
    public boolean shouldRecordScenario(CaseData caseData) {
        return caseData.isApplicantNotRepresented();
    }

    @Override
    protected void beforeRecordScenario(CaseData caseData, String authToken) {
        dashboardNotificationService.deleteByReferenceAndCitizenRole(
            String.valueOf(caseData.getCcdCaseReference()),
            CLAIMANT_ROLE
        );
    }
}
