package uk.gov.hmcts.reform.dashboard.services;

import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.dashboard.data.ScenarioRequestParams;
import uk.gov.hmcts.reform.dashboard.entities.DashboardNotificationsEntity;
import uk.gov.hmcts.reform.dashboard.entities.ScenarioEntity;
import uk.gov.hmcts.reform.dashboard.entities.TaskItemTemplateEntity;
import uk.gov.hmcts.reform.dashboard.entities.TaskListEntity;
import uk.gov.hmcts.reform.dashboard.repositories.ScenarioRepository;
import uk.gov.hmcts.reform.dashboard.repositories.TaskItemTemplateRepository;
import uk.gov.hmcts.reform.dashboard.templates.NotificationTemplateCatalog;
import uk.gov.hmcts.reform.dashboard.templates.NotificationTemplateDefinition;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardScenariosServiceTest {

    public static final String NOTIFICATION_ISSUE_CLAIM_START = "notification.issue.claim.start";
    public static final String SCENARIO_ISSUE_CLAIM_START = "scenario.issue.claim.start";
    public static final String NOTIFICATION_DRAFT_CLAIM_START = "notification.draft.claim.start";
    private DashboardScenariosService dashboardScenariosService;
    @Mock
    private ScenarioRepository scenarioRepository;
    @Mock
    private NotificationTemplateCatalog notificationTemplateCatalog;
    @Mock
    private DashboardNotificationService dashboardNotificationService;
    @Mock
    private TaskListService taskListService;
    @Mock
    private TaskItemTemplateRepository taskItemTemplateRepository;

    @BeforeEach
    void setup() {
        dashboardScenariosService = new DashboardScenariosService(
            scenarioRepository,
            notificationTemplateCatalog,
            dashboardNotificationService,
            taskListService,
            taskItemTemplateRepository
        );

        lenient().when(scenarioRepository.findByName(SCENARIO_ISSUE_CLAIM_START))
            .thenReturn(Optional.of(issueClaimStartScenario()));

        lenient().when(notificationTemplateCatalog.findByName(NOTIFICATION_ISSUE_CLAIM_START))
            .thenReturn(Optional.of(issueClaimTemplate()));

        lenient().when(notificationTemplateCatalog.findByName(NOTIFICATION_DRAFT_CLAIM_START))
            .thenReturn(Optional.of(draftClaimTemplate()));

        lenient().when(taskItemTemplateRepository.findByScenarioName(SCENARIO_ISSUE_CLAIM_START))
            .thenReturn(List.of(issueClaimTaskTemplate()));

        lenient().when(dashboardNotificationService.deleteByNameAndReferenceAndCitizenRole(
            NOTIFICATION_DRAFT_CLAIM_START, "ccd-case-id", "claimant"))
            .thenReturn(1);
    }

    @Test
    void shouldRecordScenario() {
        LocalDateTime deadline = LocalDateTime.of(2025, 1, 2, 10, 15);
        dashboardScenariosService.recordScenarios(
            "Auth-token",
            SCENARIO_ISSUE_CLAIM_START,
            "ccd-case-id",
            new ScenarioRequestParams(new HashMap<>(Map.of(
                "url",
                "http://testUrl",
                "status",
                "InProgress",
                "helpText",
                "Should be helpful!",
                "animal",
                "Tiger",
                "target",
                "Safari",
                "deadlineParam",
                deadline

            )))
        );

        verify(scenarioRepository).findByName(SCENARIO_ISSUE_CLAIM_START);
        verify(notificationTemplateCatalog).findByName(NOTIFICATION_ISSUE_CLAIM_START);
        verify(taskItemTemplateRepository).findByScenarioName(SCENARIO_ISSUE_CLAIM_START);
        verify(notificationTemplateCatalog).findByName(NOTIFICATION_DRAFT_CLAIM_START);
        ArgumentCaptor<DashboardNotificationsEntity> notificationCaptor = ArgumentCaptor.forClass(DashboardNotificationsEntity.class);
        verify(dashboardNotificationService).saveOrUpdate(notificationCaptor.capture());
        DashboardNotificationsEntity savedNotification = notificationCaptor.getValue();
        assertThat(savedNotification.getTitleEn()).isEqualTo("The Tiger jumped over the Safari.");
        assertThat(savedNotification.getDescriptionEn()).isEqualTo("The Tiger jumped over the Safari.");
        assertThat(savedNotification.getDeadline()).isEqualTo(deadline);
        assertThat(savedNotification.getParams()).containsEntry("helpText", "Should be helpful!");

        ArgumentCaptor<TaskListEntity> taskCaptor = ArgumentCaptor.forClass(TaskListEntity.class);
        verify(taskListService).saveOrUpdate(taskCaptor.capture());
        TaskListEntity savedTask = taskCaptor.getValue();
        assertThat(savedTask.getTaskNameEn()).isEqualTo("Pay hearing fee");
        assertThat(savedTask.getHintTextEn()).contains("http://testUrl");
        verify(dashboardNotificationService).deleteByNameAndReferenceAndCitizenRole(
            NOTIFICATION_DRAFT_CLAIM_START,
            "ccd-case-id",
            "claimant"
        );
    }

    @Test
    void shouldContinueIfCannotParseDeadline() {
        dashboardScenariosService.recordScenarios(
            "Auth-token",
            SCENARIO_ISSUE_CLAIM_START,
            "ccd-case-id",
            new ScenarioRequestParams(new HashMap<>(Map.of(
                "url",
                "http://testUrl",
                "status",
                "InProgress",
                "helpText",
                "Should be helpful!",
                "animal",
                "Tiger",
                "target",
                "Safari",
                "deadlineParam",
                "NotALocalDateTime"
            )))
        );

        verify(scenarioRepository).findByName(SCENARIO_ISSUE_CLAIM_START);
        verify(notificationTemplateCatalog).findByName(NOTIFICATION_ISSUE_CLAIM_START);
        verify(taskItemTemplateRepository).findByScenarioName(SCENARIO_ISSUE_CLAIM_START);
        verify(notificationTemplateCatalog).findByName(NOTIFICATION_DRAFT_CLAIM_START);
        ArgumentCaptor<DashboardNotificationsEntity> notificationCaptor = ArgumentCaptor.forClass(DashboardNotificationsEntity.class);
        verify(dashboardNotificationService).saveOrUpdate(notificationCaptor.capture());
        assertThat(notificationCaptor.getValue().getDeadline()).isNull();
        verify(taskListService).saveOrUpdate(any(TaskListEntity.class));
        verify(dashboardNotificationService).deleteByNameAndReferenceAndCitizenRole(
            NOTIFICATION_DRAFT_CLAIM_START,
            "ccd-case-id",
            "claimant"
        );
    }

    @Test
    void shouldSkipTemplatesMarkedForDeletion() {
        String scenarioName = "scenario.marked.for.deletion";
        ScenarioEntity scenario = new ScenarioEntity();
        scenario.setName(scenarioName);
        scenario.setNotificationsToCreate(Map.of("template.to.skip", new String[]{"animal"}));
        scenario.setNotificationsToDelete(new String[0]);
        when(scenarioRepository.findByName(scenarioName)).thenReturn(Optional.of(scenario));
        NotificationTemplateDefinition templateToSkip = new NotificationTemplateDefinition();
        templateToSkip.setName("template.to.skip");
        templateToSkip.setMarkedForDeletion(true);
        when(notificationTemplateCatalog.findByName("template.to.skip"))
            .thenReturn(Optional.of(templateToSkip));
        when(taskItemTemplateRepository.findByScenarioName(scenarioName)).thenReturn(List.of());

        dashboardScenariosService.recordScenarios(
            "token",
            scenarioName,
            "case-id",
            new ScenarioRequestParams(new HashMap<>(Map.of("animal", "Tiger")))
        );

        verify(notificationTemplateCatalog).findByName("template.to.skip");
        verifyNoInteractions(dashboardNotificationService);
        verify(taskListService, never()).saveOrUpdate(any());
    }

    @Test
    void shouldDeleteNotificationsWithoutRoleWhenTemplateMissing() {
        String scenarioName = "scenario.delete.missing";
        ScenarioEntity scenario = new ScenarioEntity();
        scenario.setName(scenarioName);
        scenario.setNotificationsToCreate(Map.of());
        scenario.setNotificationsToDelete(new String[]{"template.missing"});
        when(scenarioRepository.findByName(scenarioName)).thenReturn(Optional.of(scenario));
        when(notificationTemplateCatalog.findByName("template.missing")).thenReturn(Optional.empty());
        when(taskItemTemplateRepository.findByScenarioName(scenarioName)).thenReturn(List.of());

        dashboardScenariosService.recordScenarios(
            "token",
            scenarioName,
            "case-id",
            new ScenarioRequestParams(new HashMap<>())
        );

        verify(notificationTemplateCatalog).findByName("template.missing");
        verify(dashboardNotificationService).deleteByNameAndReference("template.missing", "case-id");
    }

    @Test
    void shouldReconfigureExistingNotifications() {
        // Arrange
        final String caseId = "ccd-case-id";
        final String roleType = "CLAIMANT";
        final HashMap<String, Object> params = new HashMap<>();
        params.put("animal", "Tiger");
        params.put("target", "Safari");

        DashboardNotificationsEntity existingNotification1 = new DashboardNotificationsEntity();
        existingNotification1.setName(NOTIFICATION_ISSUE_CLAIM_START);
        existingNotification1.setReference(caseId);

        DashboardNotificationsEntity existingNotification2 = new DashboardNotificationsEntity();
        existingNotification2.setName("nonexistent.template");
        existingNotification2.setReference(caseId);

        when(dashboardNotificationService.getDashboardNotifications(caseId, roleType))
            .thenReturn(List.of(existingNotification1, existingNotification2));

        NotificationTemplateDefinition template = new NotificationTemplateDefinition(
            NOTIFICATION_ISSUE_CLAIM_START,
            roleType.toLowerCase(),
            "The ${animal} jumped over the ${target}.",
            "The ${animal} jumped over the ${target}.",
            "The ${animal} jumped over the ${target}.",
            "The ${animal} jumped over the ${target}.",
            null,
            null,
            false
        );

        when(notificationTemplateCatalog.findByName(NOTIFICATION_ISSUE_CLAIM_START))
            .thenReturn(Optional.of(template));
        when(notificationTemplateCatalog.findByName("nonexistent.template"))
            .thenReturn(Optional.empty());

        final ScenarioRequestParams scenarioRequestParams = new ScenarioRequestParams(params);

        dashboardScenariosService.reconfigureCaseDashboardNotifications(caseId, scenarioRequestParams, roleType);

        verify(dashboardNotificationService).saveOrUpdate(
            org.mockito.ArgumentMatchers.argThat(updated ->
                                                     updated.getTitleEn().equals("The Tiger jumped over the Safari.")
                                                         && updated.getDescriptionEn().equals("The Tiger jumped over the Safari.")
                                                         && updated.getReference().equals(caseId)
            )
        );

        verify(dashboardNotificationService, never())
            .saveOrUpdate(org.mockito.ArgumentMatchers.argThat(n ->
                                                                   n.getName().equals("nonexistent.template")
            ));
    }

    private ScenarioEntity issueClaimStartScenario() {
        ScenarioEntity scenario = new ScenarioEntity();
        scenario.setId(1L);
        scenario.setName(SCENARIO_ISSUE_CLAIM_START);
        scenario.setNotificationsToCreate(
            Map.of(
                NOTIFICATION_ISSUE_CLAIM_START,
                new String[]{"url", "status", "helpText", "animal", "target", "deadlineParam"}
            )
        );
        scenario.setNotificationsToDelete(new String[]{NOTIFICATION_DRAFT_CLAIM_START});
        return scenario;
    }

    private NotificationTemplateDefinition issueClaimTemplate() {
        NotificationTemplateDefinition template = new NotificationTemplateDefinition();
        template.setName(NOTIFICATION_ISSUE_CLAIM_START);
        template.setRole("claimant");
        template.setTitleEn("The ${animal} jumped over the ${target}.");
        template.setDescriptionEn("The ${animal} jumped over the ${target}.");
        template.setTitleCy("The ${animal} jumped over the ${target}.");
        template.setDescriptionCy("The ${animal} jumped over the ${target}.");
        template.setDeadlineParam("deadlineParam");
        return template;
    }

    private NotificationTemplateDefinition draftClaimTemplate() {
        NotificationTemplateDefinition template = new NotificationTemplateDefinition();
        template.setName(NOTIFICATION_DRAFT_CLAIM_START);
        template.setRole("claimant");
        template.setTitleEn("The ${animal} jumped over the ${target}.");
        template.setDescriptionEn("The ${animal} jumped over the ${target}.");
        template.setTitleCy("The ${animal} jumped over the ${target}.");
        template.setDescriptionCy("The ${animal} jumped over the ${target}.");
        return template;
    }

    private TaskItemTemplateEntity issueClaimTaskTemplate() {
        TaskItemTemplateEntity template = new TaskItemTemplateEntity();
        template.setTaskStatusSequence(new int[]{1, 2});
        template.setScenarioName(SCENARIO_ISSUE_CLAIM_START);
        template.setTemplateName("Hearing.View");
        template.setCategoryEn("Hearing");
        template.setCategoryCy("Hearing");
        template.setHintTextEn("Must use ${url} to make payment for status ${status}");
        template.setHintTextCy("Must use ${url} to make payment for status ${status}");
        template.setTaskNameEn("Pay hearing fee");
        template.setTaskNameCy("Pay hearing fee");
        return template;
    }
}
