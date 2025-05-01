package uk.gov.hmcts.reform.dashboard.services;

import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.dashboard.data.ScenarioRequestParams;
import uk.gov.hmcts.reform.dashboard.entities.DashboardNotificationsEntity;
import uk.gov.hmcts.reform.dashboard.entities.NotificationTemplateEntity;
import uk.gov.hmcts.reform.dashboard.entities.ScenarioEntity;
import uk.gov.hmcts.reform.dashboard.entities.TaskItemTemplateEntity;
import uk.gov.hmcts.reform.dashboard.entities.TaskListEntity;
import uk.gov.hmcts.reform.dashboard.repositories.NotificationTemplateRepository;
import uk.gov.hmcts.reform.dashboard.repositories.ScenarioRepository;
import uk.gov.hmcts.reform.dashboard.repositories.TaskItemTemplateRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
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
    private NotificationTemplateRepository notificationTemplateRepository;
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
            notificationTemplateRepository,
            dashboardNotificationService,
            taskListService,
            taskItemTemplateRepository
        );

        when(scenarioRepository.findByName(SCENARIO_ISSUE_CLAIM_START))
            .thenReturn(Optional.of(ScenarioEntity.builder()
                                        .id(1L)
                                        .name(SCENARIO_ISSUE_CLAIM_START)
                                        .notificationsToCreate(
                                            Map.of(
                                                NOTIFICATION_ISSUE_CLAIM_START,
                                                new String[]{"url, status, helpText, animal, target"}
                                            ))
                                        .notificationsToDelete(new String[]{NOTIFICATION_DRAFT_CLAIM_START})
                                        .build()));

        when(notificationTemplateRepository.findByName(NOTIFICATION_ISSUE_CLAIM_START))
            .thenReturn(Optional.of(NotificationTemplateEntity.builder()
                                        .name(NOTIFICATION_ISSUE_CLAIM_START)
                                        .role("claimant")
                                        .titleEn("The ${animal} jumped over the ${target}.")
                                        .descriptionEn("The ${animal} jumped over the ${target}.")
                                        .titleCy("The ${animal} jumped over the ${target}.")
                                        .descriptionCy("The ${animal} jumped over the ${target}.")
                                        .deadlineParam("deadlineParam")
                                        .id(2L)
                                        .build()));

        when(notificationTemplateRepository.findByName(NOTIFICATION_DRAFT_CLAIM_START))
            .thenReturn(Optional.of(NotificationTemplateEntity.builder()
                                        .name(NOTIFICATION_DRAFT_CLAIM_START)
                                        .role("claimant")
                                        .titleEn("The ${animal} jumped over the ${target}.")
                                        .descriptionEn("The ${animal} jumped over the ${target}.")
                                        .titleCy("The ${animal} jumped over the ${target}.")
                                        .descriptionCy("The ${animal} jumped over the ${target}.")
                                        .id(1L)
                                        .build()));

        when(taskItemTemplateRepository.findByScenarioName(SCENARIO_ISSUE_CLAIM_START))
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

        when(dashboardNotificationService.deleteByNameAndReferenceAndCitizenRole(
            NOTIFICATION_DRAFT_CLAIM_START, "ccd-case-id", "claimant"))
            .thenReturn(1);
    }

    @Test
    void shouldRecordScenario() {
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
                LocalDateTime.now()

            )))
        );

        verify(scenarioRepository).findByName(SCENARIO_ISSUE_CLAIM_START);
        verify(notificationTemplateRepository).findByName(NOTIFICATION_ISSUE_CLAIM_START);
        verify(taskItemTemplateRepository).findByScenarioName(SCENARIO_ISSUE_CLAIM_START);
        verify(notificationTemplateRepository).findByName(NOTIFICATION_DRAFT_CLAIM_START);
        verify(dashboardNotificationService).saveOrUpdate(any(DashboardNotificationsEntity.class));
        verify(taskListService).saveOrUpdate(any(TaskListEntity.class));
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
        verify(notificationTemplateRepository).findByName(NOTIFICATION_ISSUE_CLAIM_START);
        verify(taskItemTemplateRepository).findByScenarioName(SCENARIO_ISSUE_CLAIM_START);
        verify(notificationTemplateRepository).findByName(NOTIFICATION_DRAFT_CLAIM_START);
        verify(dashboardNotificationService).saveOrUpdate(any(DashboardNotificationsEntity.class));
        verify(taskListService).saveOrUpdate(any(TaskListEntity.class));
        verify(dashboardNotificationService).deleteByNameAndReferenceAndCitizenRole(
            NOTIFICATION_DRAFT_CLAIM_START,
            "ccd-case-id",
            "claimant"
        );
    }

}
