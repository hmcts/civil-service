package uk.gov.hmcts.reform.civil.handler.tasks;

import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.event.HearingFeeUnpaidEvent;
import uk.gov.hmcts.reform.civil.event.HearingFeePaidEvent;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.civil.service.search.HearingFeeDueSearchService;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class HearingFeeDueHandlerTest {

    @Mock
    private ExternalTask mockTask;

    @Mock
    private ExternalTaskService externalTaskService;

    @Mock
    private HearingFeeDueSearchService searchService;

    @Mock
    private CaseDetailsConverter caseDetailsConverter;

    @Mock
    private CoreCaseDataService coreCaseDataService;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @InjectMocks
    private HearingFeeDueHandler handler;

    @BeforeEach
    void init() {
        when(mockTask.getTopicName()).thenReturn("test");
        when(mockTask.getWorkerId()).thenReturn("worker");

    }

    @Test
    void shouldEmitHearingFeePaidEvent_whenCasesFoundPaid() {
        long caseId = 1L;
        CaseData caseData = CaseDataBuilder.builder().atStateHearingFeeDuePaid().build();
        Map<String, Object> data = Map.of("data", caseData);
        List<CaseDetails> caseDetails = List.of(CaseDetails.builder().id(caseId).data(data).build());

        when(searchService.getCases()).thenReturn(caseDetails);
        when(coreCaseDataService.getCase(caseId)).thenReturn(caseDetails.get(0));
        when(caseDetailsConverter.toCaseData(caseDetails.get(0))).thenReturn(caseData);

        handler.execute(mockTask, externalTaskService);

        verify(applicationEventPublisher).publishEvent(new HearingFeePaidEvent(caseId));
        verify(externalTaskService).complete(mockTask);
    }

    @Test
    void shouldEmitHearingFeePaidEvent_whenCasesFoundUnpaid() {
        long caseId = 1L;
        CaseData caseData = CaseDataBuilder.builder().atStateHearingFeeDueUnpaid().build();
        Map<String, Object> data = Map.of("data", caseData);
        List<CaseDetails> caseDetails = List.of(CaseDetails.builder().id(caseId).data(data).build());

        when(searchService.getCases()).thenReturn(caseDetails);
        when(coreCaseDataService.getCase(caseId)).thenReturn(caseDetails.get(0));
        when(caseDetailsConverter.toCaseData(caseDetails.get(0))).thenReturn(caseData);

        handler.execute(mockTask, externalTaskService);

        verify(applicationEventPublisher).publishEvent(new HearingFeeUnpaidEvent(caseId));
        verify(externalTaskService).complete(mockTask);
    }

    @Test
    void shouldNotEmitTakeCaseOfflineEvent_WhenNoCasesFound() {
        when(searchService.getCases()).thenReturn(List.of());

        handler.execute(mockTask, externalTaskService);

        verifyNoInteractions(applicationEventPublisher);
    }

    @Test
    void shouldCallHandleFailureMethod_whenExceptionFromBusinessLogic() {
        String errorMessage = "there was an error";

        when(mockTask.getRetries()).thenReturn(null);
        when(searchService.getCases()).thenAnswer(invocation -> {
            throw new Exception(errorMessage);
        });

        handler.execute(mockTask, externalTaskService);

        verify(externalTaskService, never()).complete(mockTask);
        verify(externalTaskService).handleFailure(
            eq(mockTask),
            eq(errorMessage),
            anyString(),
            eq(2),
            eq(1000L)
        );
    }

    @Test
    void shouldNotCallHandleFailureMethod_whenExceptionOnCompleteCall() {
        handler.execute(mockTask, externalTaskService);

        verify(externalTaskService, never()).handleFailure(
            any(ExternalTask.class),
            anyString(),
            anyString(),
            anyInt(),
            anyLong()
        );
    }

    @Test
    void shouldHandleExceptionAndContinue_whenOneCaseErrors() {
        long caseId = 1L;
        long otherId = 2L;
        Map<String, Object> data = Map.of("data", "some data");
        List<CaseDetails> caseDetails = List.of(
            CaseDetails.builder().id(caseId).data(data).build(),
            CaseDetails.builder().id(otherId).data(data).build());

        when(searchService.getCases()).thenReturn(caseDetails);

        String errorMessage = "there was an error";

        handler.execute(mockTask, externalTaskService);

        verify(externalTaskService, never()).handleFailure(
            any(ExternalTask.class),
            anyString(),
            anyString(),
            anyInt(),
            anyLong()
        );
    }
}
