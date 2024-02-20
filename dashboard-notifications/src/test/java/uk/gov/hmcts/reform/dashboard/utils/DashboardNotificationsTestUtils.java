package uk.gov.hmcts.reform.dashboard.utils;

import uk.gov.hmcts.reform.dashboard.data.Notification;
import uk.gov.hmcts.reform.dashboard.data.TaskList;
import uk.gov.hmcts.reform.dashboard.data.TaskStatus;
import uk.gov.hmcts.reform.dashboard.entities.DashboardNotificationsEntity;
import uk.gov.hmcts.reform.dashboard.entities.TaskItemTemplateEntity;
import uk.gov.hmcts.reform.dashboard.entities.TaskListEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DashboardNotificationsTestUtils {

    private static final UUID uuid = UUID.randomUUID();

    private DashboardNotificationsTestUtils() {
        //utility class
    }

    public static List<TaskList> getTaskListList() {

        List<TaskList> taskListList = new ArrayList<>();
        taskListList.add(TaskList.builder().id(uuid).taskNameCy("HearingCY")
                             .taskNameEn("HearingEN").taskOrder(1).categoryCy("CategoryCy")
                             .categoryEn("CategoryEn")
                             .role("Defendant")
                             .currentStatusEn(TaskStatus.NOT_AVAILABLE_YET.getName())
                             .currentStatusCy(TaskStatus.NOT_AVAILABLE_YET.getWelshName())
                             .nextStatusEn(TaskStatus.IN_PROGRESS.getName())
                             .nextStatusCy(TaskStatus.IN_PROGRESS.getWelshName())
                             .hintTextCy("HintCy").hintTextEn("HintEn").reference("123").build());
        return taskListList;
    }

    public static List<TaskListEntity> getTaskListEntityList() {

        List<TaskListEntity> taskListEntityList = new ArrayList<>();
        taskListEntityList.add(TaskListEntity.builder()
                                   .id(uuid).taskNameCy("HearingCY").taskNameEn("HearingEN")
                                   .currentStatus(1).nextStatus(6).hintTextCy("HintCy")
                                   .hintTextEn("HintEn").reference("123")
                                   .taskItemTemplate(TaskItemTemplateEntity.builder()
                                                         .id(Long.valueOf(123)).taskNameCy("TaskNameCy")
                                                         .taskNameEn("TaskNameEn")
                                                         .taskOrder(1).hintTextCy("HintCY")
                                                         .hintTextEn("HintEn").role("Defendant")
                                                         .categoryCy("CategoryCy").categoryEn("CategoryEn")
                                                         .build())
                                   .build());
        return taskListEntityList;
    }

    public static TaskListEntity getTaskListEntity() {

        return TaskListEntity.builder()
            .id(uuid).taskNameCy("HearingCY").taskNameEn("HearingEN")
            .currentStatus(0).nextStatus(1).hintTextCy("HintCy")
            .hintTextEn("HintEn").reference("123")
            .taskItemTemplate(TaskItemTemplateEntity.builder()
                                  .id(Long.valueOf(123)).taskNameCy("TaskNameCy")
                                  .taskNameEn("TaskNameEn")
                                  .name("hearing")
                                  .taskOrder(1).hintTextCy("HintCY")
                                  .hintTextEn("HintEn").role("Defendant")
                                  .categoryCy("CategoryCy").categoryEn("CategoryEn")
                                  .build()).build();
    }

    public static List<DashboardNotificationsEntity> getNotificationEntityList() {
        return List.of(getNotification(uuid));
    }

    public static DashboardNotificationsEntity getNotification(UUID notificationId) {
        return DashboardNotificationsEntity.builder().id(notificationId)
            .reference("ccd-case-reference")
            .descriptionEn("desc").descriptionCy("descCy").build();
    }

    public static List<Notification> getNotificationList() {
        List<Notification> notificationList = new ArrayList<>();
        notificationList.add(Notification.builder().id(uuid).descriptionEn("desc").descriptionCy("descCy").build());
        return notificationList;
    }
}
