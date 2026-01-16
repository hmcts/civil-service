package uk.gov.hmcts.reform.civil.service.dashboardnotifications.evidenceuploaded;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardScenarioService;
import uk.gov.hmcts.reform.dashboard.services.DashboardNotificationService;
import uk.gov.hmcts.reform.dashboard.services.DashboardScenariosService;
import uk.gov.hmcts.reform.dashboard.services.TaskListService;

import static java.util.Objects.nonNull;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CP_HEARING_DOCUMENTS_NOT_UPLOADED_CLAIMANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CP_HEARING_DOCUMENTS_UPLOADED_CLAIMANT;

@Service
public class EvidenceUploadedClaimantDashboardService extends DashboardScenarioService {

    private final DashboardNotificationService dashboardNotificationService;
    private final TaskListService taskListService;

    public EvidenceUploadedClaimantDashboardService(DashboardScenariosService dashboardScenariosService,
                                                    DashboardNotificationService dashboardNotificationService,
                                                    DashboardNotificationsParamsMapper mapper,
                                                    TaskListService taskListService) {
        super(dashboardScenariosService, mapper);
        this.dashboardNotificationService = dashboardNotificationService;
        this.taskListService = taskListService;
    }

    public void notifyEvidenceUploaded(CaseData caseData, String authToken) {
        recordScenario(caseData, authToken);
    }

    @Override
    protected String getScenario(CaseData caseData) {
        return nonNull(caseData.getCaseDocumentUploadDate()) ? SCENARIO_AAA6_CP_HEARING_DOCUMENTS_UPLOADED_CLAIMANT.getScenario() :
            SCENARIO_AAA6_CP_HEARING_DOCUMENTS_NOT_UPLOADED_CLAIMANT.getScenario();
    }

    @Override
    public boolean shouldRecordScenario(CaseData caseData) {
        return caseData.isApplicantNotRepresented();
    }

    @Override
    protected void beforeRecordScenario(CaseData caseData, String authToken) {
        final String caseId = String.valueOf(caseData.getCcdCaseReference());
        dashboardNotificationService.deleteByReferenceAndCitizenRole(
            caseId,
            CLAIMANT_ROLE
        );

        taskListService.makeProgressAbleTasksInactiveForCaseIdentifierAndRole(
            caseId,
            CLAIMANT_ROLE
        );
    }
}
