package uk.gov.hmcts.reform.civil.handler.tasks;

import org.camunda.bpm.client.exception.ValueMapperException;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.Event;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.civil.enums.BusinessProcessStatus;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.ContactDetailsUpdatedEvent;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
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

@SpringBootTest(classes = {
    ContactInformationUpdatedTaskHandler.class,
    JacksonAutoConfiguration.class,
    CaseDetailsConverter.class
})
@ExtendWith(SpringExtension.class)
class ContactInformationUpdatedTaskHandlerTest {

    private static final String CASE_ID = "1";

    @Mock
    private ExternalTask mockTask;

    @Mock
    private ExternalTaskService externalTaskService;

    @MockBean
    private CoreCaseDataService coreCaseDataService;

    @MockBean
    private FeatureToggleService featureToggleService;

    @Captor
    ArgumentCaptor<CaseDataContent> caseDataContentArgumentCaptor;

    @Autowired
    private ContactInformationUpdatedTaskHandler caseEventTaskHandler;

    @BeforeEach
    void init() {
        Map<String, Object> variables = Map.of(
            "caseId", CASE_ID,
            "caseEvent", CONTACT_INFORMATION_UPDATED.name()
        );

        when(mockTask.getAllVariables()).thenReturn(variables);
        when(mockTask.getTopicName()).thenReturn("test");
        when(mockTask.getWorkerId()).thenReturn("worker");
        when(mockTask.getActivityId()).thenReturn("activityId");
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
