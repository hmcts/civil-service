package uk.gov.hmcts.reform.dashboard.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.dashboard.data.ScenarioRequestParams;
import uk.gov.hmcts.reform.dashboard.entities.NotificationEntity;
import uk.gov.hmcts.reform.dashboard.entities.NotificationTemplateEntity;
import uk.gov.hmcts.reform.dashboard.entities.TaskItemTemplateEntity;
import uk.gov.hmcts.reform.dashboard.entities.TaskListEntity;
import uk.gov.hmcts.reform.dashboard.repositories.NotificationRepository;
import uk.gov.hmcts.reform.dashboard.repositories.NotificationTemplateRepository;
import uk.gov.hmcts.reform.dashboard.repositories.TaskItemTemplateRepository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class DashboardScenariosServiceTest {

    public static final String NOTIFICATION_ISSUE_CLAIM_START = "notification.issue.claim.start";
    public static final String NOTIFICATION_DRAFT_CLAIM_START = "notification.draft.claim.start";
    public static final String TASK_ITEM_HEARING_FEE_PAYMENT = "taskItem.hearing.fee.payment";
    private DashboardScenariosService dashboardScenariosService;
    @Mock
    private NotificationTemplateRepository notificationTemplateRepository;
    @Mock
    private NotificationRepository notificationRepository;
    @Mock
    private TaskListService taskListService;
    @Mock
    private TaskItemTemplateRepository taskItemTemplateRepository;

    @BeforeEach
    void setup() {
        dashboardScenariosService = new DashboardScenariosService(
            notificationTemplateRepository,
            notificationRepository,
            taskListService,
            taskItemTemplateRepository
        );

        when(notificationTemplateRepository.findByName(NOTIFICATION_ISSUE_CLAIM_START))
            .thenReturn(Optional.of(NotificationTemplateEntity.builder()
                                        .role("claimant")
                                        .titleEn("The ${animal} jumped over the ${target}.")
                                        .descriptionEn("The ${animal} jumped over the ${target}.")
                                        .titleCy("The ${animal} jumped over the ${target}.")
                                        .descriptionCy("The ${animal} jumped over the ${target}.")
                                        .notificationsToBeDeleted(new String[]{NOTIFICATION_DRAFT_CLAIM_START})
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

        when(taskItemTemplateRepository.findByNameAndRole(NOTIFICATION_ISSUE_CLAIM_START, "claimant"))
            .thenReturn(List.of(TaskItemTemplateEntity.builder()
                                    .taskStatusSequence(new int[]{1, 2})
                                    .name(TASK_ITEM_HEARING_FEE_PAYMENT)
                                    .categoryEn("Hearing")
                                    .categoryCy("Hearing")
                                    .hintTextEn("Must use ${url} to make payment for status ${status}")
                                    .hintTextCy("Must use ${url} to make payment for status ${status}")
                                    .taskNameEn("Pay hearing fee")
                                    .taskNameCy("Pay hearing fee")
                                    .build()));

        when(notificationTemplateRepository.findByName(NOTIFICATION_DRAFT_CLAIM_START))
            .thenReturn(Optional.of(NotificationTemplateEntity.builder().build()));
    }

    @Test
    void shouldRecordScenario() {
        dashboardScenariosService.recordScenarios(
            "Auth-token",
            NOTIFICATION_ISSUE_CLAIM_START,
            "ccd-case-id",
            new ScenarioRequestParams(Map.of(
                "url",
                "http://testUrl",
                "status",
                "InProgress",
                "helpText",
                "Should be helpful!",
                "animal",
                "Tiger",
                "target",
                "Safari"
            ))
        );

        verify(notificationTemplateRepository).findByName(NOTIFICATION_ISSUE_CLAIM_START);
        verify(taskItemTemplateRepository).findByNameAndRole(NOTIFICATION_ISSUE_CLAIM_START, "claimant");
        verify(notificationTemplateRepository).findByName(NOTIFICATION_DRAFT_CLAIM_START);
        verify(notificationRepository).save(any(NotificationEntity.class));
        verify(taskListService).saveOrUpdate(any(TaskListEntity.class), eq(TASK_ITEM_HEARING_FEE_PAYMENT));
//        verify(notificationRepository).deleteByNameAndReferenceAndCitizenRole(
//            NOTIFICATION_DRAFT_CLAIM_START,
//            "ccd-case-id",
//            "claimant"
//        );
    }

}
