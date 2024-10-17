package uk.gov.hmcts.reform.civil.handler.tasks;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.civil.enums.BusinessProcessStatus;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.ContactDetailsUpdatedEvent;
import uk.gov.hmcts.reform.civil.sampledata.CaseDetailsBuilder;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CHECK_PAID_IN_FULL_SCHED_DEADLINE;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CONTACT_INFORMATION_UPDATED;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;

@ExtendWith(MockitoExtension.class)
public class CoscApplicationAfterPaymentTaskHandlerTest {

    private static final String CIVIL_CASE_ID = "1594901956117591";
    private static final String GENERAL_APP_CASE_ID = "1234";

    @Mock
    private ExternalTask mockExternalTask;

    @Mock
    private ExternalTaskService externalTaskService;

    @Mock
    private CoreCaseDataService coreCaseDataService;

    @Mock
    private CaseDetailsConverter caseDetailsConverter;

    private CoscApplicationAfterPaymentTaskHandler handler;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper().registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
        handler = new CoscApplicationAfterPaymentTaskHandler(coreCaseDataService, objectMapper);
    }

    @Test
    void testStartTheEvent() {
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

        when(mockExternalTask.getTopicName()).thenReturn("test");
        when(mockExternalTask.getAllVariables())
            .thenReturn(Map.of(
                "caseId", GENERAL_APP_CASE_ID,
                "caseEvent", CHECK_PAID_IN_FULL_SCHED_DEADLINE,
                "generalAppParentCaseLink", CIVIL_CASE_ID
            ));

        when(coreCaseDataService.startUpdate(CIVIL_CASE_ID, CHECK_PAID_IN_FULL_SCHED_DEADLINE))
            .thenReturn(StartEventResponse.builder().caseDetails(caseDetails)
                            .eventId(CONTACT_INFORMATION_UPDATED.name()).build());
        when(coreCaseDataService.submitUpdate(eq(CIVIL_CASE_ID), any(CaseDataContent.class)))
            .thenReturn(caseData);

        handler.execute(mockExternalTask, externalTaskService);

        verify(coreCaseDataService).startUpdate(CIVIL_CASE_ID, CHECK_PAID_IN_FULL_SCHED_DEADLINE);
        verify(coreCaseDataService).submitUpdate(eq(CIVIL_CASE_ID), any(CaseDataContent.class));
        verify(externalTaskService).complete(mockExternalTask, null);
    }

}
