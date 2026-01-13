package uk.gov.hmcts.reform.civil.service.dashboardnotifications.evidenceuploaded;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.dashboard.data.ScenarioRequestParams;
import uk.gov.hmcts.reform.dashboard.services.DashboardNotificationService;
import uk.gov.hmcts.reform.dashboard.services.DashboardScenariosService;
import uk.gov.hmcts.reform.dashboard.services.TaskListService;

import java.time.LocalDateTime;
import java.util.HashMap;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CP_HEARING_DOCUMENTS_NOT_UPLOADED_DEFENDANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CP_HEARING_DOCUMENTS_UPLOADED_DEFENDANT;

@ExtendWith(MockitoExtension.class)
class EvidenceUploadedDefendantDashboardServiceTest {

    private static final String AUTH_TOKEN = "BEARER";

    @Mock
    private DashboardScenariosService dashboardScenariosService;
    @Mock
    private DashboardNotificationService dashboardNotificationService;
    @Mock
    private TaskListService taskListService;
    @Mock
    private DashboardNotificationsParamsMapper mapper;

    @InjectMocks
    private EvidenceUploadedDefendantDashboardService service;

    @BeforeEach
    void setUp() {
        when(mapper.mapCaseDataToParams(any())).thenReturn(new HashMap<>());
    }

    @Test
    void shouldNotifyDefendantWhenEvidenceUploadedWithDocumentDate() {
        CaseData caseData = CaseDataBuilder.builder().build().toBuilder()
            .respondent1Represented(YesOrNo.NO)
            .caseDocumentUploadDateRes(LocalDateTime.now())
            .ccdCaseReference(1234L)
            .build();

        service.notifyCaseEvidenceUploaded(caseData, AUTH_TOKEN);

        verify(dashboardNotificationService).deleteByReferenceAndCitizenRole("1234", "DEFENDANT");
        verify(taskListService).makeProgressAbleTasksInactiveForCaseIdentifierAndRole("1234", "DEFENDANT");
        verify(dashboardScenariosService).recordScenarios(
            AUTH_TOKEN,
            SCENARIO_AAA6_CP_HEARING_DOCUMENTS_UPLOADED_DEFENDANT.getScenario(),
            "1234",
            ScenarioRequestParams.builder().params(new HashMap<>()).build()
        );
    }

    @Test
    void shouldNotifyDefendantWhenEvidenceNotUploadedWithoutDocumentDate() {
        CaseData caseData = CaseDataBuilder.builder().build().toBuilder()
            .respondent1Represented(YesOrNo.NO)
            .caseDocumentUploadDateRes(null)
            .ccdCaseReference(5678L)
            .build();

        service.notifyCaseEvidenceUploaded(caseData, AUTH_TOKEN);

        verify(dashboardNotificationService).deleteByReferenceAndCitizenRole("5678", "DEFENDANT");
        verify(taskListService).makeProgressAbleTasksInactiveForCaseIdentifierAndRole("5678", "DEFENDANT");
        verify(dashboardScenariosService).recordScenarios(
            AUTH_TOKEN,
            SCENARIO_AAA6_CP_HEARING_DOCUMENTS_NOT_UPLOADED_DEFENDANT.getScenario(),
            "5678",
            ScenarioRequestParams.builder().params(new HashMap<>()).build()
        );
    }

    @Test
    void shouldUseUploadedScenarioWhenDocumentDatePresent() {
        CaseData caseData = CaseDataBuilder.builder().build().toBuilder()
            .respondent1Represented(YesOrNo.NO)
            .caseDocumentUploadDateRes(LocalDateTime.of(2024, 1, 15, 10, 30))
            .ccdCaseReference(9012L)
            .build();

        service.notifyCaseEvidenceUploaded(caseData, AUTH_TOKEN);

        verify(dashboardScenariosService).recordScenarios(
            AUTH_TOKEN,
            SCENARIO_AAA6_CP_HEARING_DOCUMENTS_UPLOADED_DEFENDANT.getScenario(),
            "9012",
            ScenarioRequestParams.builder().params(new HashMap<>()).build()
        );
    }
}
