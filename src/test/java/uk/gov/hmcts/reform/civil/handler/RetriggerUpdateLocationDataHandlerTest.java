package uk.gov.hmcts.reform.civil.handler;

import org.camunda.bpm.client.task.ExternalTask;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RetriggerUpdateLocationDataHandlerTest {

    @Mock
    private CoreCaseDataService coreCaseDataService;
    private RetriggerUpdateLocationDataHandler handler;

    @BeforeEach
    void setUp() {
        handler = new RetriggerUpdateLocationDataHandler(coreCaseDataService);
    }

    @Test
    void testHandleTask_RetriggerUpdateCaseData() {
        ExternalTask externalTask = mock(ExternalTask.class);
        when(externalTask.getVariable("caseIds")).thenReturn("1,2");
        when(externalTask.getVariable("reason")).thenReturn("court closed as a cml");
        when(externalTask.getVariable("ePimId")).thenReturn("123456");

        handler.handleTask(externalTask);

        verify(coreCaseDataService).triggerUpdateCaseMgmtLocation(
            1L,
            CaseEvent.TRANSFER_ONLINE_CASE,
            "123456",
            "court closed as a cml",
            "Updated case management location with epimId 123456",
            "Updated case management location with epimId 123456"
        );
        verify(coreCaseDataService).triggerUpdateCaseMgmtLocation(
            2L,
            CaseEvent.TRANSFER_ONLINE_CASE,
            "123456",
            "court closed as a cml",
            "Updated case management location with epimId 123456",
            "Updated case management location with epimId 123456"
        );
    }

    @Test
    void testHandleTask_RetriggerrUpdateLocationWithMissingCaseIds() {
        ExternalTask externalTask = mock(ExternalTask.class);
        when(externalTask.getVariable("caseIds")).thenReturn(null);

        assertThrows(IllegalArgumentException.class, () -> handler.handleTask(externalTask));
    }

    @Test
    void testHandleTask_RetriggerrUpdateLocationWithMissingEpimsId() {
        ExternalTask externalTask = mock(ExternalTask.class);
        when(externalTask.getVariable("caseIds")).thenReturn("1");
        when(externalTask.getVariable("ePimId")).thenReturn(null);

        assertThrows(IllegalArgumentException.class, () -> handler.handleTask(externalTask));
    }
}
