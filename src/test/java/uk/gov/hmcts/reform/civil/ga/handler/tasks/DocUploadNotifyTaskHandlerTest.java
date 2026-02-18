package uk.gov.hmcts.reform.civil.ga.handler.tasks;

import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.ga.service.GaCoreCaseDataService;
import uk.gov.hmcts.reform.civil.ga.service.search.GaEvidenceUploadNotificationSearchService;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.sampledata.GeneralApplicationCaseDataBuilder;

import java.util.Map;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.GA_EVIDENCE_UPLOAD_CHECK;

@ExtendWith(MockitoExtension.class)
public class DocUploadNotifyTaskHandlerTest {

    @Mock
    private ExternalTask externalTask;

    @Mock
    private ExternalTaskService externalTaskService;

    @Mock
    private GaEvidenceUploadNotificationSearchService searchService;

    @Mock
    private CaseDetailsConverter caseDetailsConverter;

    @Mock
    private GaCoreCaseDataService coreCaseDataService;

    @InjectMocks
    private DocUploadNotifyTaskHandler handler;

    @Test
    void shouldNotSendMessageAndTriggerGaEvent_whenZeroCasesFound() {
        when(searchService.getApplications()).thenReturn(Set.of());

        handler.execute(externalTask, externalTaskService);

        verify(searchService).getApplications();
        verifyNoInteractions(coreCaseDataService);
        verify(externalTaskService).complete(any(), any());
    }

    @Test
    void shouldEmitBusinessProcessEvent_whenCasesFound() {
        long caseId = 1L;
        when(searchService.getApplications())
            .thenReturn(Set.of(CaseDetails.builder().build()));

        when(caseDetailsConverter.toGeneralApplicationCaseData(any()))
            .thenReturn(GeneralApplicationCaseDataBuilder.builder().ccdCaseReference(caseId).build());

        handler.execute(externalTask, externalTaskService);

        verify(searchService).getApplications();
        verify(coreCaseDataService).triggerGaEvent(
            1L, GA_EVIDENCE_UPLOAD_CHECK,
            Map.of()
        );
        verify(externalTaskService).complete(any(), any());
    }
}
