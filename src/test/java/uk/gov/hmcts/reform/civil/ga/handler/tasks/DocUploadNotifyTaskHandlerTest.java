package uk.gov.hmcts.reform.civil.ga.handler.tasks;

import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
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

@SpringBootTest(classes = {
    JacksonAutoConfiguration.class,
    CaseDetailsConverter.class,
    DocUploadNotifyTaskHandler.class})
public class DocUploadNotifyTaskHandlerTest {

    @MockitoBean
    private ExternalTask externalTask;

    @MockitoBean
    private ExternalTaskService externalTaskService;

    @MockitoBean
    private GaEvidenceUploadNotificationSearchService searchService;

    @MockitoBean
    private CaseDetailsConverter caseDetailsConverter;

    @MockitoBean
    private GaCoreCaseDataService coreCaseDataService;

    @Autowired
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
        verify(coreCaseDataService).triggerGaEvent(1L, GA_EVIDENCE_UPLOAD_CHECK,
                Map.of());
        verify(externalTaskService).complete(any(), any());
    }
}
