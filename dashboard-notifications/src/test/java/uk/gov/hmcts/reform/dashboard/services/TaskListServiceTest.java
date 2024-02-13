package uk.gov.hmcts.reform.dashboard.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.dashboard.model.TaskList;
import uk.gov.hmcts.reform.dashboard.repositories.TaskListRepository;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
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
        when(taskListRepository.findByReferenceAndTaskItemTemplateRole(any(), any())).thenReturn(List.of());
        List<TaskList> actual = taskListService.getTaskList("123","Claimant"
        );

        assertThat(actual).isEqualTo(List.of());
    }

    @Test
    void shouldReturnTaskList_whenTaskListIsPresent() {
        when(taskListRepository.findByReferenceAndTaskItemTemplateRole(any(), any())).thenReturn(getTaskListEntityList());
        List<TaskList> actual = taskListService.getTaskList("123","Claimant"
        );


        assertThat(actual).isEqualTo(getTaskListList());
    }

}
