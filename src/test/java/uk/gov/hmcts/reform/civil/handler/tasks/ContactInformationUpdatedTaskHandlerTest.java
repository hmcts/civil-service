package uk.gov.hmcts.reform.civil.handler.tasks;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.camunda.bpm.client.exception.ValueMapperException;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.Event;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.civil.enums.BusinessProcessStatus;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.ContactDetailsUpdatedEvent;
import uk.gov.hmcts.reform.civil.sampledata.CaseDetailsBuilder;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CONTACT_INFORMATION_UPDATED;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;

@ExtendWith(MockitoExtension.class)
class ContactInformationUpdatedTaskHandlerTest {

    private static final String CASE_ID = "1";

    @Mock
    private ExternalTask mockTask;

    @Mock
    private ExternalTaskService externalTaskService;

    @Mock
    private CoreCaseDataService coreCaseDataService;

    @Captor
    ArgumentCaptor<CaseDataContent> caseDataContentArgumentCaptor;

    private ContactInformationUpdatedTaskHandler caseEventTaskHandler;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper();
        CaseDetailsConverter caseDetailsConverter = new CaseDetailsConverter(objectMapper);
        caseEventTaskHandler = new ContactInformationUpdatedTaskHandler(coreCaseDataService, caseDetailsConverter,
                                                                        objectMapper);
    }

    @BeforeEach
    void init() {
        Map<String, Object> variables = Map.of(
            "caseId", CASE_ID,
            "caseEvent", CONTACT_INFORMATION_UPDATED.name()
        );

        when(mockTask.getAllVariables()).thenReturn(variables);
        when(mockTask.getTopicName()).thenReturn("test");
    }

    @Test
    void shouldHaveCorrectEventDetails_whenContactDetailsUpdatedEventIsProvided() {
        CaseData caseData = CaseData.builder()
            .contactDetailsUpdatedEvent(
                ContactDetailsUpdatedEvent.builder()
                    .description("Best description")
                    .summary("Even better summary")
                    .submittedByCaseworker(YES).build())
            .businessProcess(
                BusinessProcess.builder()
                    .status(BusinessProcessStatus.READY)
                    .processInstanceId("process-id").build())
            .build();

        CaseDetails caseDetails = CaseDetailsBuilder.builder().data(caseData).build();
        when(coreCaseDataService.startUpdate(CASE_ID, CONTACT_INFORMATION_UPDATED))
            .thenReturn(StartEventResponse.builder().caseDetails(caseDetails)
                            .eventId(CONTACT_INFORMATION_UPDATED.name()).build());
        when(coreCaseDataService.submitUpdate(eq(CASE_ID), caseDataContentArgumentCaptor.capture()))
            .thenReturn(caseData);

        caseEventTaskHandler.execute(mockTask, externalTaskService);

        verify(coreCaseDataService, times(1)).submitUpdate(eq(CASE_ID), caseDataContentArgumentCaptor.capture());

        CaseDataContent caseDataContent = caseDataContentArgumentCaptor.getValue();
        Event actual = caseDataContent.getEvent();
        Event expected = Event.builder()
            .id(CONTACT_INFORMATION_UPDATED.name())
            .description("Best description")
            .summary("Even better summary")
            .build();

        assertEquals(expected, actual);
    }

    @Test
    void shouldHandleFailureWithExpectedErrorMessage_whenCaseEventIsNotProvided() {
        Map<String, Object> variables = Map.of(
            "caseId", CASE_ID
        );

        when(mockTask.getAllVariables()).thenReturn(variables);

        caseEventTaskHandler.execute(mockTask, externalTaskService);

        verify(externalTaskService, times(1))
            .handleFailure(eq(mockTask), eq("The case event was not provided"),
                           anyString(), anyInt(), anyLong()
            );
    }

    @Test
    void shouldHandleFailureWithExpectedErrorMessage_whenMapperConversionFailed() {
        when(mockTask.getAllVariables())
            .thenThrow(new ValueMapperException("Mapper conversion failed due to incompatible types"));

        caseEventTaskHandler.execute(mockTask, externalTaskService);

        verify(externalTaskService, times(1))
            .handleFailure(eq(mockTask), eq("Mapper conversion failed due to incompatible types"),
                           anyString(), anyInt(), anyLong()
            );
    }

    @Test
    void shouldHandleFailureWithExpectedErrorMessage_whenCaseIdIsNotProvided() {
        Map<String, Object> variables = Map.of(
            "caseEvent", CONTACT_INFORMATION_UPDATED.name()
        );

        when(mockTask.getAllVariables()).thenReturn(variables);

        caseEventTaskHandler.execute(mockTask, externalTaskService);

        verify(externalTaskService, times(1))
            .handleFailure(eq(mockTask), eq("The caseId was not provided"),
                           anyString(), anyInt(), anyLong()
            );
    }

    @Test
    void shouldHandleFailureWithExpectedErrorMessage_whenContactDetailsUpdatedEventIsNotProvided() {
        CaseData caseData = CaseData.builder()
            .businessProcess(
                BusinessProcess.builder()
                    .status(BusinessProcessStatus.READY)
                    .processInstanceId("process-id").build())
            .build();

        CaseDetails caseDetails = CaseDetailsBuilder.builder().data(caseData).build();
        when(coreCaseDataService.startUpdate(CASE_ID, CONTACT_INFORMATION_UPDATED))
            .thenReturn(StartEventResponse.builder().caseDetails(caseDetails)
                            .eventId(CONTACT_INFORMATION_UPDATED.name()).build());

        caseEventTaskHandler.execute(mockTask, externalTaskService);

        verify(externalTaskService, times(1))
            .handleFailure(eq(mockTask), eq("The contactDetailsUpdatedEvent was not provided"),
                           anyString(), anyInt(), anyLong()
            );
    }
}
