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
            .thenReturn(Optional.of(ScenarioEntity.builder()
                                        .id(1L)
                                        .name(SCENARIO_ISSUE_CLAIM_START)
                                        .notificationsToCreate(
                                            Map.of(
                                                NOTIFICATION_ISSUE_CLAIM_START,
                                                new String[]{"url", "status", "helpText", "animal", "target", "deadlineParam"}
                                            ))
                                        .notificationsToDelete(new String[]{NOTIFICATION_DRAFT_CLAIM_START})
                                        .build()));

        lenient().when(notificationTemplateCatalog.findByName(NOTIFICATION_ISSUE_CLAIM_START))
            .thenReturn(Optional.of(NotificationTemplateDefinition.builder()
                                        .name(NOTIFICATION_ISSUE_CLAIM_START)
                                        .role("claimant")
                                        .titleEn("The ${animal} jumped over the ${target}.")
                                        .descriptionEn("The ${animal} jumped over the ${target}.")
                                        .titleCy("The ${animal} jumped over the ${target}.")
                                        .descriptionCy("The ${animal} jumped over the ${target}.")
                                        .deadlineParam("deadlineParam")
                                        .build()));

        lenient().when(notificationTemplateCatalog.findByName(NOTIFICATION_DRAFT_CLAIM_START))
            .thenReturn(Optional.of(NotificationTemplateDefinition.builder()
                                        .name(NOTIFICATION_DRAFT_CLAIM_START)
                                        .role("claimant")
                                        .titleEn("The ${animal} jumped over the ${target}.")
                                        .descriptionEn("The ${animal} jumped over the ${target}.")
                                        .titleCy("The ${animal} jumped over the ${target}.")
                                        .descriptionCy("The ${animal} jumped over the ${target}.")
                                        .build()));

        lenient().when(taskItemTemplateRepository.findByScenarioName(SCENARIO_ISSUE_CLAIM_START))
            .thenReturn(List.of(TaskItemTemplateEntity.builder()
                                    .taskStatusSequence(new int[]{1, 2})
                                    .scenarioName(SCENARIO_ISSUE_CLAIM_START)
                                    .templateName("Hearing.View")
                                    .categoryEn("Hearing")
                                    .categoryCy("Hearing")
                                    .hintTextEn("Must use ${url} to make payment for status ${status}")
                                    .hintTextCy("Must use ${url} to make payment for status ${status}")
                                    .taskNameEn("Pay hearing fee")
                                    .taskNameCy("Pay hearing fee")
                                    .build()));

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
        ScenarioEntity scenario = ScenarioEntity.builder()
            .name(scenarioName)
            .notificationsToCreate(Map.of("template.to.skip", new String[]{"animal"}))
            .notificationsToDelete(new String[0])
            .build();
        when(scenarioRepository.findByName(scenarioName)).thenReturn(Optional.of(scenario));
        when(notificationTemplateCatalog.findByName("template.to.skip"))
            .thenReturn(Optional.of(NotificationTemplateDefinition.builder()
                                        .name("template.to.skip")
                                        .markedForDeletion(true)
                                        .build()));
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
        ScenarioEntity scenario = ScenarioEntity.builder()
            .name(scenarioName)
            .notificationsToCreate(Map.of())
            .notificationsToDelete(new String[]{"template.missing"})
            .build();
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

}
