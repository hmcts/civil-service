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
import uk.gov.hmcts.reform.dashboard.entities.TaskListEntity;
import uk.gov.hmcts.reform.dashboard.repositories.TaskListRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.dashboard.utils.DashboardNotificationsTestUtils.getTaskListEntity;
import static uk.gov.hmcts.reform.dashboard.utils.DashboardNotificationsTestUtils.getTaskListEntityList;
import static uk.gov.hmcts.reform.dashboard.utils.DashboardNotificationsTestUtils.getTaskListList;

@ExtendWith(MockitoExtension.class)
class TaskListServiceTest {

    @Mock
    private TaskListRepository taskListRepository;

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
        TaskListEntity expected = taskListEntity.toBuilder().currentStatus(taskListEntity.getNextStatus()).build();
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
        List<TaskListEntity> tasks = new ArrayList<>();
        tasks.add(getTaskListEntity(UUID.randomUUID()).toBuilder()
                      .taskNameEn("<a href=\"somewhere\">Link name</A >")
                      .taskNameCy("<A  href=\"somewhere\">Link name Welsh</A>")
                      .currentStatus(TaskStatus.NOT_AVAILABLE_YET.getPlaceValue())
                      .build());
        tasks.add(getTaskListEntity(UUID.randomUUID()).toBuilder()
                      .currentStatus(TaskStatus.INACTIVE.getPlaceValue())
                      .taskNameEn("<a href=\"somewhere\">Link name</A >")
                      .taskNameCy("<A  href=\"somewhere\">Link name Welsh</A>").build());
        tasks.add(getTaskListEntity(UUID.randomUUID()).toBuilder()
                      .currentStatus(TaskStatus.AVAILABLE.getPlaceValue())
                      .taskNameEn("<a href=\"somewhere\">Link name</A >")
                      .taskNameCy("<A  href=\"somewhere\">Link name Welsh</A>").build());
        tasks.add(getTaskListEntity(UUID.randomUUID()).toBuilder()
                      .currentStatus(TaskStatus.OPTIONAL.getPlaceValue())
                      .taskNameEn("<a href=\"somewhere\">Link name</A >")
                      .taskNameCy("<A  href=\"somewhere\">Link name Welsh</A>").build());
        tasks.add(getTaskListEntity(UUID.randomUUID()).toBuilder()
                      .currentStatus(TaskStatus.ACTION_NEEDED.getPlaceValue())
                      .taskNameEn("<a href=\"somewhere\">Link name</A >")
                      .taskNameCy("<A  href=\"somewhere\">Link name Welsh</A>")
                      .build());
        tasks.add(getTaskListEntity(UUID.randomUUID()).toBuilder()
                      .currentStatus(TaskStatus.IN_PROGRESS.getPlaceValue())
                      .taskNameEn("<a href=\"somewhere\">Link name</A >")
                      .taskNameCy("<A  href=\"somewhere\">Link name Welsh</A>")
                      .build());
        tasks.add(getTaskListEntity(UUID.randomUUID()).toBuilder()
                      .currentStatus(TaskStatus.DONE.getPlaceValue())
                      .taskNameEn("<a href=\"somewhere\">Link name</A >")
                      .taskNameCy("<A  href=\"somewhere\">Link name Welsh</A>")
                      .build());

        when(taskListRepository.findByReferenceAndTaskItemTemplateRoleAndCurrentStatusNotIn(
            "123", "Claimant",
            List.of(TaskStatus.AVAILABLE.getPlaceValue(), TaskStatus.DONE.getPlaceValue())
        ))
            .thenReturn(tasks);

        //when
        taskListService.makeProgressAbleTasksInactiveForCaseIdentifierAndRole("123", "Claimant");

        //then
        verify(taskListRepository).findByReferenceAndTaskItemTemplateRoleAndCurrentStatusNotIn(
            "123", "Claimant",
            List.of(TaskStatus.AVAILABLE.getPlaceValue(), TaskStatus.DONE.getPlaceValue())
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

}
