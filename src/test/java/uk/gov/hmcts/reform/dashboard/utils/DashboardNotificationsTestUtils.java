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

    private static final UUID TASK_ITEM_IDENTIFIER = UUID.randomUUID();

    private DashboardNotificationsTestUtils() {
        //utility class
    }

    public static List<TaskList> getTaskListList() {
        TaskList taskList = new TaskList();
        taskList.setId(TASK_ITEM_IDENTIFIER);
        taskList.setTaskNameCy("HearingCY");
        taskList.setTaskNameEn("HearingEN");
        taskList.setTaskOrder(1);
        taskList.setCategoryCy("CategoryCy");
        taskList.setCategoryEn("CategoryEn");
        taskList.setRole("Defendant");
        taskList.setCurrentStatusEn(TaskStatus.NOT_AVAILABLE_YET.getName());
        taskList.setCurrentStatusCy(TaskStatus.NOT_AVAILABLE_YET.getWelshName());
        taskList.setNextStatusEn(TaskStatus.IN_PROGRESS.getName());
        taskList.setNextStatusCy(TaskStatus.IN_PROGRESS.getWelshName());
        taskList.setHintTextCy("HintCy");
        taskList.setHintTextEn("HintEn");
        taskList.setReference("123");
        return List.of(taskList);
    }

    public static List<TaskListEntity> getTaskListEntityList() {

        List<TaskListEntity> taskListEntityList = new ArrayList<>();
        taskListEntityList.add(getTaskListEntity(TASK_ITEM_IDENTIFIER));
        return taskListEntityList;
    }

    public static TaskListEntity getTaskListEntity(UUID taskItemIdentifier) {
        TaskItemTemplateEntity taskItemTemplate = new TaskItemTemplateEntity();
        taskItemTemplate.setId(Long.valueOf(123));
        taskItemTemplate.setTaskNameCy("TaskNameCy");
        taskItemTemplate.setTaskNameEn("TaskNameEn");
        taskItemTemplate.setScenarioName("Scenario.hearing");
        taskItemTemplate.setTemplateName("Hearing.view");
        taskItemTemplate.setTaskOrder(1);
        taskItemTemplate.setHintTextCy("HintCY");
        taskItemTemplate.setHintTextEn("HintEn");
        taskItemTemplate.setRole("Defendant");
        taskItemTemplate.setCategoryCy("CategoryCy");
        taskItemTemplate.setCategoryEn("CategoryEn");

        TaskListEntity taskListEntity = new TaskListEntity();
        taskListEntity.setId(taskItemIdentifier);
        taskListEntity.setTaskNameCy("HearingCY");
        taskListEntity.setTaskNameEn("HearingEN");
        taskListEntity.setCurrentStatus(1);
        taskListEntity.setNextStatus(6);
        taskListEntity.setHintTextCy("HintCy");
        taskListEntity.setHintTextEn("HintEn");
        taskListEntity.setReference("123");
        taskListEntity.setTaskItemTemplate(taskItemTemplate);
        return taskListEntity;
    }

    public static List<DashboardNotificationsEntity> getNotificationEntityList() {
        return List.of(getNotification(TASK_ITEM_IDENTIFIER));
    }

    public static DashboardNotificationsEntity getNotification(UUID notificationId) {
        DashboardNotificationsEntity notification = new DashboardNotificationsEntity();
        notification.setId(notificationId);
        notification.setReference("ccd-case-reference");
        notification.setDescriptionEn("desc");
        notification.setDescriptionCy("descCy");
        notification.setName("template.name");
        notification.setCitizenRole("CLAIMANT");
        return notification;
    }

    public static List<Notification> getNotificationList() {
        Notification notification = new Notification();
        notification.setId(TASK_ITEM_IDENTIFIER);
        notification.setDescriptionEn("desc");
        notification.setDescriptionCy("descCy");
        return List.of(notification);
    }
}
