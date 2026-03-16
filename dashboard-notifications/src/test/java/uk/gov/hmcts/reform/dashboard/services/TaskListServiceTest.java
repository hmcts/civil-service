package uk.gov.hmcts.reform.dashboard.services;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.dashboard.data.TaskList;
import uk.gov.hmcts.reform.dashboard.data.TaskStatus;
import uk.gov.hmcts.reform.dashboard.entities.TaskItemTemplateEntity;
import uk.gov.hmcts.reform.dashboard.entities.TaskListEntity;
import uk.gov.hmcts.reform.dashboard.repositories.TaskItemTemplateRepository;
import uk.gov.hmcts.reform.dashboard.repositories.TaskListRepository;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.dashboard.utils.DashboardNotificationsTestUtils.getTaskListEntity;
import static uk.gov.hmcts.reform.dashboard.utils.DashboardNotificationsTestUtils.getTaskListEntityList;
import static uk.gov.hmcts.reform.dashboard.utils.DashboardNotificationsTestUtils.getTaskListList;

@ExtendWith(MockitoExtension.class)
class TaskListServiceTest {

    @Mock
    private TaskListRepository taskListRepository;

    @Mock
    private TaskItemTemplateRepository taskItemTemplateRepository;

    @InjectMocks
    private TaskListService taskListService;

    @Test
    void shouldReturnEmpty_whenTaskListIsNotPresent() {

        //given
        when(taskListRepository.findByReferenceAndTaskItemTemplateRole(any(), any())).thenReturn(List.of());

        //when
        List<TaskList> actual = taskListService.getTaskList("123", "Claimant");

        //then
        verify(taskListRepository).findByReferenceAndTaskItemTemplateRole("123", "Claimant");
        assertThat(actual).isEqualTo(List.of());
    }

    @Test
    void shouldReturnTaskList_whenTaskListIsPresent() {

        //given
        when(taskListRepository.findByReferenceAndTaskItemTemplateRole(
            any(),
            any()
        )).thenReturn(getTaskListEntityList());

        //when
        List<TaskList> actual = taskListService.getTaskList("123", "Claimant");

        //then
        verify(taskListRepository).findByReferenceAndTaskItemTemplateRole("123", "Claimant");
        assertThat(actual).isEqualTo(getTaskListList());
    }

    @Test
    void shouldReturnTaskListEntity_whenTaskListEntityIsUpdated() {
        //given
        UUID taskItemIdentifier = UUID.randomUUID();
        TaskListEntity taskListEntity = getTaskListEntity(taskItemIdentifier);
        when(taskListRepository.findById(taskItemIdentifier)).thenReturn(Optional.of(taskListEntity));
        TaskListEntity expected = copyTaskListEntity(taskListEntity);
        expected.setCurrentStatus(taskListEntity.getNextStatus());
        when(taskListRepository.save(expected)).thenReturn(expected);

        //when
        TaskListEntity actual = taskListService.updateTaskListItem(taskItemIdentifier);

        //then
        verify(taskListRepository).findById(taskItemIdentifier);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void shouldReturnEmptyTaskListEntity_whenTaskListEntityIsNotUpdated() {

        //given
        UUID taskItemIdentifier = UUID.randomUUID();
        when(taskListRepository.findById(taskItemIdentifier)).thenReturn(Optional.empty());

        //when
        assertThatThrownBy(() -> taskListService.updateTaskListItem(taskItemIdentifier))
            .isInstanceOf(IllegalArgumentException.class)
            .hasNoCause()
            .hasMessage("Invalid task item identifier " + taskItemIdentifier);

        //then
        verify(taskListRepository).findById(taskItemIdentifier);
    }

    @Test
    void shouldThrowExceptionWhenExceptionInGetTaskList() {

        //given
        when(taskListRepository.findByReferenceAndTaskItemTemplateRole(any(), any()))
            .thenThrow(new RuntimeException());

        //then
        assertThrows(
            RuntimeException.class,
            () -> {
                taskListRepository.findByReferenceAndTaskItemTemplateRole(any(), any());
            }
        );

    }

    @Test
    void shouldMakeProgressAbleTaskListInactive_whenTaskListIsPresent() {

        //given
        List<TaskListEntity> tasks = getTaskListEntitiesWithAnchorNames();

        when(taskListRepository.findByReferenceAndTaskItemTemplateRoleAndCurrentStatusNotIn(
            "123", "Claimant",
            List.of(
                TaskStatus.AVAILABLE.getPlaceValue(),
                TaskStatus.DONE.getPlaceValue(),
                TaskStatus.NOT_AVAILABLE_YET.getPlaceValue()
            )
        ))
            .thenReturn(tasks);

        //when
        taskListService.makeProgressAbleTasksInactiveForCaseIdentifierAndRole("123", "Claimant");

        //then
        verify(taskListRepository).findByReferenceAndTaskItemTemplateRoleAndCurrentStatusNotIn(
            "123", "Claimant",
            List.of(
                TaskStatus.AVAILABLE.getPlaceValue(),
                TaskStatus.DONE.getPlaceValue(),
                TaskStatus.NOT_AVAILABLE_YET.getPlaceValue()
            )
        );

        verify(taskListRepository, atLeast(4)).save(ArgumentMatchers.argThat(
            a -> {
                Assertions.assertEquals(
                    a.getCurrentStatus(),
                    TaskStatus.INACTIVE.getPlaceValue()
                );
                Assertions.assertEquals(
                    a.getNextStatus(),
                    TaskStatus.INACTIVE.getPlaceValue()
                );
                Assertions.assertTrue(StringUtils.isBlank(a.getHintTextCy()));
                Assertions.assertTrue(StringUtils.isBlank(a.getHintTextEn()));
                Assertions.assertEquals("Link name", a.getTaskNameEn());
                Assertions.assertEquals("Link name Welsh", a.getTaskNameCy());
                return true;
            }
        ));
    }

    @Test
    void shouldMakeProgressAbleTaskListInactiveExcludingTemplate_whenTaskListIsPresent() {

        //given
        List<TaskListEntity> tasks = getTaskListEntitiesWithAnchorNames();

        when(taskListRepository.findByReferenceAndTaskItemTemplateRoleAndCurrentStatusNotInAndTaskItemTemplateTemplateNameNot(
            "123", "Claimant",
            List.of(
                TaskStatus.AVAILABLE.getPlaceValue(),
                TaskStatus.DONE.getPlaceValue(),
                TaskStatus.NOT_AVAILABLE_YET.getPlaceValue()
            ),
            "Template"
        ))
            .thenReturn(tasks);

        //when
        taskListService.makeProgressAbleTasksInactiveForCaseIdentifierAndRoleExcludingTemplate(
            "123",
            "Claimant",
            "Template"
        );

        //then
        verify(taskListRepository).findByReferenceAndTaskItemTemplateRoleAndCurrentStatusNotInAndTaskItemTemplateTemplateNameNot(
            "123", "Claimant",
            List.of(
                TaskStatus.AVAILABLE.getPlaceValue(),
                TaskStatus.DONE.getPlaceValue(),
                TaskStatus.NOT_AVAILABLE_YET.getPlaceValue()
            ),
            "Template"
        );

        verify(taskListRepository, atLeast(4)).save(ArgumentMatchers.argThat(
            a -> {
                Assertions.assertEquals(
                    a.getCurrentStatus(),
                    TaskStatus.INACTIVE.getPlaceValue()
                );
                Assertions.assertEquals(
                    a.getNextStatus(),
                    TaskStatus.INACTIVE.getPlaceValue()
                );
                Assertions.assertTrue(StringUtils.isBlank(a.getHintTextCy()));
                Assertions.assertTrue(StringUtils.isBlank(a.getHintTextEn()));
                Assertions.assertEquals("Link name", a.getTaskNameEn());
                Assertions.assertEquals("Link name Welsh", a.getTaskNameCy());
                return true;
            }
        ));
    }

    @Test
    void shouldMakeProgressAbleTaskListInactive_Except_Ga_whenTaskListIsPresent() {

        //given
        List<TaskListEntity> tasks = getTaskListEntitiesWithAnchorNames();

        when(taskListRepository.findByReferenceAndTaskItemTemplateRoleAndCurrentStatusNotInAndTaskItemTemplate_IdNotIn(
            "123", "Claimant",
            List.of(
                TaskStatus.AVAILABLE.getPlaceValue(), TaskStatus.DONE.getPlaceValue(),
                TaskStatus.NOT_AVAILABLE_YET.getPlaceValue()
            ),
            List.of(Long.valueOf(123))
        ))
            .thenReturn(tasks);

        TaskItemTemplateEntity category = new TaskItemTemplateEntity();
        category.setId(Long.valueOf(123));
        category.setTaskNameCy("TaskNameCy");
        category.setTaskNameEn("TaskNameEn");
        category.setScenarioName("Scenario.hearing");
        category.setTemplateName("Hearing.view");
        category.setTaskOrder(1);
        category.setHintTextCy("HintCY");
        category.setHintTextEn("HintEn");
        category.setRole("Claimant");
        category.setCategoryCy("CategoryCy");
        category.setCategoryEn("CategoryEn");
        List<TaskItemTemplateEntity> categories = List.of(category);

        when(taskItemTemplateRepository.findByCategoryEnAndRole(
            "CategoryEn", "Claimant"))
            .thenReturn(categories);

        //when
        taskListService.makeProgressAbleTasksInactiveForCaseIdentifierAndRoleExcludingCategory(
            "123",
            "Claimant",
            "CategoryEn"
        );

        //then
        verify(taskItemTemplateRepository).findByCategoryEnAndRole(
            "CategoryEn", "Claimant");
        verify(taskListRepository).findByReferenceAndTaskItemTemplateRoleAndCurrentStatusNotInAndTaskItemTemplate_IdNotIn(
            "123", "Claimant",
            List.of(
                TaskStatus.AVAILABLE.getPlaceValue(), TaskStatus.DONE.getPlaceValue(),
                TaskStatus.NOT_AVAILABLE_YET.getPlaceValue()
            ),
            List.of(Long.valueOf(123))
        );

        verify(taskListRepository, atLeast(5)).save(ArgumentMatchers.argThat(
            a -> {
                Assertions.assertEquals(
                    a.getCurrentStatus(),
                    TaskStatus.INACTIVE.getPlaceValue()
                );
                Assertions.assertEquals(
                    a.getNextStatus(),
                    TaskStatus.INACTIVE.getPlaceValue()
                );
                Assertions.assertTrue(StringUtils.isBlank(a.getHintTextCy()));
                Assertions.assertTrue(StringUtils.isBlank(a.getHintTextEn()));
                Assertions.assertEquals("Link name", a.getTaskNameEn());
                Assertions.assertEquals("Link name Welsh", a.getTaskNameCy());
                return true;
            }
        ));
    }

    @Test
    public void shouldDeleteWhenThereWereDuplicateEntriesInTheRepository() {
        TaskListEntity task = getTaskListEntityWithAnchorNames(UUID.randomUUID(), TaskStatus.AVAILABLE.getPlaceValue());
        task.setCreatedAt(OffsetDateTime.MAX);

        TaskListEntity task2 = getTaskListEntityWithAnchorNames(UUID.randomUUID(), TaskStatus.NOT_AVAILABLE_YET.getPlaceValue());
        task2.setCreatedAt(OffsetDateTime.MIN.plusDays(99L));

        TaskListEntity task3 = getTaskListEntityWithAnchorNames(UUID.randomUUID(), TaskStatus.INACTIVE.getPlaceValue());
        task3.setCreatedAt(OffsetDateTime.MIN.plusDays(20L));

        List<TaskListEntity> tasks = new ArrayList<>();
        tasks.add(task);
        tasks.add(task2);
        tasks.add(task3);

        when(taskListRepository
                 .findByReferenceAndTaskItemTemplateRoleAndTaskItemTemplateTemplateName(
                     any(),
                     any(),
                     any()
                 )).thenReturn(tasks);

        taskListService.saveOrUpdate(task);

        verify(taskListRepository).deleteById(task2.getId());
        verify(taskListRepository).deleteById(task3.getId());
        verify(taskListRepository).save(task);
    }

    @Test
    void shouldMakeProgressAbleTaskListActiveExcludingTemplate() {

        //given
        List<TaskListEntity> tasks = getTaskListEntitiesWithAnchorNames();

        when(taskListRepository.findByReferenceAndTaskItemTemplateRoleAndTaskItemTemplateTemplateName(
            any(),
            any(),
            any()
        ))
            .thenReturn(tasks);

        //when
        taskListService.makeViewDocumentTaskAvailable("123");

        //then
        verify(taskListRepository, times(2)).findByReferenceAndTaskItemTemplateRoleAndTaskItemTemplateTemplateName(
            any(),
            any(),
            any()
        );
    }

    private List<TaskListEntity> getTaskListEntitiesWithAnchorNames() {
        List<TaskListEntity> tasks = new ArrayList<>();
        tasks.add(getTaskListEntityWithAnchorNames(UUID.randomUUID(), TaskStatus.NOT_AVAILABLE_YET.getPlaceValue()));
        tasks.add(getTaskListEntityWithAnchorNames(UUID.randomUUID(), TaskStatus.INACTIVE.getPlaceValue()));
        tasks.add(getTaskListEntityWithAnchorNames(UUID.randomUUID(), TaskStatus.AVAILABLE.getPlaceValue()));
        tasks.add(getTaskListEntityWithAnchorNames(UUID.randomUUID(), TaskStatus.OPTIONAL.getPlaceValue()));
        tasks.add(getTaskListEntityWithAnchorNames(UUID.randomUUID(), TaskStatus.ACTION_NEEDED.getPlaceValue()));
        tasks.add(getTaskListEntityWithAnchorNames(UUID.randomUUID(), TaskStatus.IN_PROGRESS.getPlaceValue()));
        tasks.add(getTaskListEntityWithAnchorNames(UUID.randomUUID(), TaskStatus.DONE.getPlaceValue()));
        return tasks;
    }

    private TaskListEntity getTaskListEntityWithAnchorNames(UUID id, int currentStatus) {
        TaskListEntity task = getTaskListEntity(id);
        task.setCurrentStatus(currentStatus);
        task.setTaskNameEn("<a href=\"somewhere\">Link name</A >");
        task.setTaskNameCy("<A  href=\"somewhere\">Link name Welsh</A>");
        return task;
    }

    private TaskListEntity copyTaskListEntity(TaskListEntity source) {
        TaskListEntity copy = new TaskListEntity();
        copy.setId(source.getId());
        copy.setTaskItemTemplate(source.getTaskItemTemplate());
        copy.setReference(source.getReference());
        copy.setCurrentStatus(source.getCurrentStatus());
        copy.setNextStatus(source.getNextStatus());
        copy.setTaskNameEn(source.getTaskNameEn());
        copy.setHintTextEn(source.getHintTextEn());
        copy.setTaskNameCy(source.getTaskNameCy());
        copy.setHintTextCy(source.getHintTextCy());
        copy.setCreatedAt(source.getCreatedAt());
        copy.setUpdatedAt(source.getUpdatedAt());
        copy.setUpdatedBy(source.getUpdatedBy());
        copy.setMessageParams(source.getMessageParams());
        return copy;
    }

}
