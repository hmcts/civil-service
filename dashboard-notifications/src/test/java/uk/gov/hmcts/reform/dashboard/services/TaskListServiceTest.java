package uk.gov.hmcts.reform.dashboard.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.dashboard.data.TaskList;
import uk.gov.hmcts.reform.dashboard.entities.TaskListEntity;
import uk.gov.hmcts.reform.dashboard.repositories.TaskListRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.dashboard.utils.DashboardNotificationsTestUtils.*;

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
        when(taskListRepository.findByReferenceAndTaskItemTemplateRoleAndTaskItemTemplateName(
            any(),
            any(),
            any()
        )).thenReturn(Optional.of(getTaskListEntity()));

        //when
        TaskListEntity actual = taskListService.updateTaskList("hearing", "123","Claimant");

        //then
        verify(taskListRepository).findByReferenceAndTaskItemTemplateRoleAndTaskItemTemplateName("hearing","123","Claimant");
        assertThat(actual).isEqualTo(getTaskListList());
    }
}
