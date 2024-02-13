package uk.gov.hmcts.reform.dashboard.controllers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.dashboard.model.TaskList;
import uk.gov.hmcts.reform.dashboard.services.TaskListService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.dashboard.utils.DashboardNotificationsTestUtils.getTaskListList;

@ExtendWith(MockitoExtension.class)
class DashboardControllerTest {

    @Mock
    private TaskListService taskListService;

    @InjectMocks
    private DashboardController dashboardController;

    private static final String AUTHORISATION = "Bearer: aaa";

    @Test
    public void shouldReturnTaskListForCaseReferenceAndRole() {

        List<TaskList> taskList = getTaskListList();
        //given
        when(taskListService.getTaskList(any(),any()))
            .thenReturn(taskList);

        //when
        ResponseEntity<List<TaskList>> output = dashboardController.getTaskListByCaseIdentifierAndRole("123", "Claimant", AUTHORISATION);

        //then
        assertThat(output.getBody()).isEqualTo(taskList);
    }

}
