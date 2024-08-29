package uk.gov.hmcts.reform.dashboard.services;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.dashboard.data.TaskList;
import uk.gov.hmcts.reform.dashboard.data.TaskStatus;
import uk.gov.hmcts.reform.dashboard.entities.TaskItemTemplateEntity;
import uk.gov.hmcts.reform.dashboard.entities.TaskListEntity;
import uk.gov.hmcts.reform.dashboard.repositories.TaskListRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
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
    void shouldBlockEditorTasks() {
        String caseId = "caseId";
        String role = "role";
        List<TaskListEntity> shouldModify = List.of(
            TaskListEntity.builder()
                .currentStatus(TaskStatus.IN_PROGRESS.getPlaceValue())
                .hintTextEn("dates and all")
                .hintTextCy("dates and all, in welsh")
                .taskNameEn("<a href=\"somewhere\">Link name</A >")
                .taskNameCy("<A  href=\"somewhere\">Link name Welsh</A>")
                .taskItemTemplate(TaskItemTemplateEntity.builder()
                                      .templateName("Task.Progress")
                                      .build())
                .build()
        );
        List<TaskListEntity> shouldNotModify = List.of(
            TaskListEntity.builder()
                .currentStatus(TaskStatus.AVAILABLE.getPlaceValue())
                .hintTextEn("dates and all")
                .hintTextCy("dates and all, in welsh")
                .taskItemTemplate(TaskItemTemplateEntity.builder()
                                      .templateName("Application.View")
                                      .build())
                .build()
        );

        when(taskListRepository.findByReferenceAndTaskItemTemplateRoleAndCurrentStatusIn(
            caseId, role,
            Set.of(TaskStatus.IN_PROGRESS.getPlaceValue(), TaskStatus.AVAILABLE.getPlaceValue(),
                   TaskStatus.OPTIONAL.getPlaceValue(), TaskStatus.ACTION_NEEDED.getPlaceValue()
            )
        )).thenReturn(Stream.concat(shouldModify.stream(), shouldNotModify.stream())
                          .collect(Collectors.toList()));

        List<TaskListEntity> updated = taskListService.blockTaskProgress(caseId, role);

        shouldModify.forEach(t ->
                                 Mockito.verify(taskListRepository).save(ArgumentMatchers.argThat(
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
                                 ))
        );
        Assertions.assertEquals(1, updated.size());

    }

}
