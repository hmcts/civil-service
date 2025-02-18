package uk.gov.hmcts.reform.civil.handler.tasks;

import org.camunda.bpm.client.exception.NotFoundException;
import org.camunda.bpm.client.exception.RestException;
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
import uk.gov.hmcts.reform.civil.event.DismissClaimEvent;
import uk.gov.hmcts.reform.civil.exceptions.CompleteTaskException;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.civil.service.search.CaseDismissedSearchService;

import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;

@ExtendWith(SpringExtension.class)
class ClaimDismissedHandlerTest {

    @Mock
    private ExternalTask mockTask;

    @Mock
    private ExternalTaskService externalTaskService;

    @Mock
    private CaseDismissedSearchService searchService;

    @Mock
    private CoreCaseDataService coreCaseDataService;

    @Mock
    private CaseDetailsConverter caseDetailsConverter;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @InjectMocks
    private ClaimDismissedHandler handler;

    @BeforeEach
    void init() {
        when(mockTask.getTopicName()).thenReturn("test");
        when(mockTask.getWorkerId()).thenReturn("worker");
    }

    @Test
    void shouldEmitMoveCaseToStuckOutEvent_whenCasesFound() {
        long caseId = 1L;
        Map<String, Object> data = Map.of("data", "some data",
                                          "ccdCaseReference", caseId);
        Set<CaseDetails> caseDetails = Set.of(CaseDetails.builder().id(caseId).data(data).build());

        when(searchService.getCases()).thenReturn(caseDetails);
        CaseData caseData = CaseDataBuilder.builder()
            .atStateClaimDismissed()
            .respondent1ResponseDate(null)
            .respondent2ResponseDate(null)
            .takenOfflineByStaffDate(null)
            .build();

        when(coreCaseDataService.getCase(anyLong())).thenReturn(CaseDetails.builder().data(data).build());
        when(caseDetailsConverter.toCaseData(any(CaseDetails.class))).thenReturn(caseData);

        handler.execute(mockTask, externalTaskService);

        verify(applicationEventPublisher).publishEvent(new DismissClaimEvent(caseId));
        verify(externalTaskService).complete(mockTask, null);
    }

    @Test
    void shouldEmitMoveCaseToStuckOutEvent_whenCasesFoundButNotEligibleForDismiss() {
        long caseId = 1L;
        Map<String, Object> data = Map.of("data", "some data",
                                          "ccdCaseReference", caseId);
        Set<CaseDetails> caseDetails = Set.of(CaseDetails.builder().id(caseId).data(data).build());

        when(searchService.getCases()).thenReturn(caseDetails);
        CaseData caseData = CaseDataBuilder.builder()
            .atStateClaimDismissed()
            .respondent1ResponseDate(null)
            .respondent2ResponseDate(null)
            .takenOfflineByStaffDate(null)
            .setRequestDJDamagesFlagForWA(YES)
            .build();

        when(coreCaseDataService.getCase(anyLong())).thenReturn(CaseDetails.builder().data(data).build());
        when(caseDetailsConverter.toCaseData(any(CaseDetails.class))).thenReturn(caseData);

        handler.execute(mockTask, externalTaskService);

        verifyNoInteractions(applicationEventPublisher);
        verify(externalTaskService).complete(mockTask, null);
    }

    @Test
    void shouldNotEmitMoveCaseToStuckOutEvent_WhenNoCasesFound() {
        when(searchService.getCases()).thenReturn(Set.of());

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
            eq(300000L)
        );
    }

    @Test
    void shouldNotCallHandleFailureMethod_whenExceptionOnCompleteCall() {
        String errorMessage = "there was an error";

        doThrow(new NotFoundException(errorMessage, new RestException("", "", 404)))
            .when(externalTaskService).complete(mockTask, null);

        assertThrows(CompleteTaskException.class,
                     () -> handler.execute(mockTask, externalTaskService));

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
        Map<String, Object> data = Map.of("data", "some data",
                                          "ccdCaseReference", caseId);
        Set<CaseDetails> caseDetails = Set.of(
            CaseDetails.builder().id(caseId).data(data).build(),
            CaseDetails.builder().id(otherId).data(data).build());

        when(searchService.getCases()).thenReturn(caseDetails);

        CaseData caseData = CaseDataBuilder.builder()
            .atStateClaimDismissed()
            .respondent1ResponseDate(null)
            .respondent2ResponseDate(null)
            .takenOfflineByStaffDate(null)
            .build();

        when(coreCaseDataService.getCase(anyLong())).thenReturn(CaseDetails.builder().data(data).build());
        when(caseDetailsConverter.toCaseData(any(CaseDetails.class))).thenReturn(caseData);

        String errorMessage = "there was an error";

        doThrow(new NullPointerException(errorMessage))
            .when(applicationEventPublisher).publishEvent(new DismissClaimEvent(caseId));

        handler.execute(mockTask, externalTaskService);

        verify(externalTaskService, never()).handleFailure(
            any(ExternalTask.class),
            anyString(),
            anyString(),
            anyInt(),
            anyLong()
        );

        verify(applicationEventPublisher, times(2)).publishEvent(any(DismissClaimEvent.class));
        verify(applicationEventPublisher).publishEvent(new DismissClaimEvent(caseId));
        verify(applicationEventPublisher).publishEvent(new DismissClaimEvent(otherId));
    }
}
