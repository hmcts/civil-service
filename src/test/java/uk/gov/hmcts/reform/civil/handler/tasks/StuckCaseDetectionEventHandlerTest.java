package uk.gov.hmcts.reform.civil.handler.tasks;

import org.camunda.bpm.client.task.ExternalTask;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.config.properties.EventProperties;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.CaseTaskTrackingService;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.civil.service.ExternalTaskCompletionService;
import uk.gov.hmcts.reform.civil.service.camunda.CamundaRuntimeApi;
import uk.gov.hmcts.reform.civil.service.search.CasesStuckCheckSearchService;

import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StuckCaseDetectionEventHandlerTest {

    @Mock
    private ExternalTaskCompletionService completionService;
    @Mock
    private EventProperties eventProperties;
    @Mock
    private CasesStuckCheckSearchService searchService;
    @Mock
    private CamundaRuntimeApi camundaRuntimeApi;
    @Mock
    private AuthTokenGenerator authTokenGenerator;
    @Mock
    private CaseTaskTrackingService trackingService;
    @Mock
    private CoreCaseDataService coreCaseDataService;
    @Mock
    private CaseDetailsConverter converter;
    @Mock
    private ExternalTask task;
    @InjectMocks
    private StuckCaseDetectionEventHandler handler;

    @Test
    void retryFailureShouldNotPreventIndependentDetection() {
        IncidentRetryEventHandler retryHandler = new IncidentRetryEventHandler(
            completionService, eventProperties, camundaRuntimeApi, authTokenGenerator, trackingService
        );
        when(authTokenGenerator.generate()).thenThrow(new RuntimeException("Retry failed"))
            .thenReturn("auth");
        assertThatThrownBy(() -> retryHandler.handleTask(task)).isInstanceOf(RuntimeException.class);
        when(searchService.getCases("7")).thenReturn(Set.of());

        handler.handleTask(task);

        verify(searchService).getCases("7");
        verify(camundaRuntimeApi, never()).queryProcessInstances(
            any(), anyInt(), anyInt(), anyString(), anyString(), anyMap()
        );
    }

    @Test
    void shouldSplitPopulationAndPreserveProcessIdentityWhenCamundaFails() {
        CaseDetails withProcess = CaseDetails.builder().id(1L).build();
        CaseDetails withoutProcess = CaseDetails.builder().id(2L).build();
        CaseDetails unknown = CaseDetails.builder().id(3L).build();
        when(searchService.getCases("7")).thenReturn(Set.of(withProcess, withoutProcess, unknown));
        when(authTokenGenerator.generate()).thenReturn("auth");
        when(coreCaseDataService.getCase(1L)).thenReturn(withProcess);
        when(coreCaseDataService.getCase(2L)).thenReturn(withoutProcess);
        when(coreCaseDataService.getCase(3L)).thenThrow(new RuntimeException("CCD unavailable"));
        when(converter.toCaseData(withProcess)).thenReturn(CaseData.builder()
            .businessProcess(new BusinessProcess().setProcessInstanceId("process-1")).build());
        when(converter.toCaseData(withoutProcess)).thenReturn(CaseData.builder().build());
        when(camundaRuntimeApi.getLatestOpenIncidentForProcessInstance(
            any(), anyBoolean(), eq("process-1"), anyString(), anyString(), anyInt()
        )).thenThrow(new RuntimeException("Camunda unavailable"));

        handler.handleTask(task);

        verify(trackingService).trackCaseTask(eq("1"), eq("stuckCaseDetection"), eq("StuckCaseDetected"),
            argThat(p -> "has-process-instance".equals(p.get("processInstanceStatus"))
                && "process-1".equals(p.get("processInstanceId"))));
        verify(trackingService).trackCaseTask(eq("2"), eq("stuckCaseDetection"), eq("StuckCaseDetected"),
            argThat(p -> "no-process-instance".equals(p.get("processInstanceStatus"))));
        verify(trackingService).trackCaseTask(eq("3"), eq("stuckCaseDetection"), eq("StuckCaseDetected"),
            argThat(p -> "unknown".equals(p.get("processInstanceStatus"))));
        verify(trackingService).trackCaseTask(eq("MULTIPLE"), eq("stuckCaseDetectionSummary"), eq("StuckCasesDetectionSummary"),
            argThat(p -> "3".equals(p.get("stuckCaseCount"))
                && "1".equals(p.get("withProcessInstanceCount"))
                && "1".equals(p.get("withoutProcessInstanceCount"))
                && "1".equals(p.get("unknownProcessInstanceCount"))));
    }

    @Test
    void shouldUseIndependentLookbackAndReportEmptyPopulation() {
        when(task.getVariable("stuckCasesFromPastDays")).thenReturn("8");
        when(searchService.getCases("8")).thenReturn(Set.of());
        handler.handleTask(task);
        verify(trackingService).trackCaseTask(eq("MULTIPLE"), eq("stuckCaseDetectionSummary"), eq("StuckCasesDetectionSummary"),
            argThat(p -> "0".equals(p.get("stuckCaseCount")) && "8".equals(p.get("stuckCasesFromPastDays"))));
    }
}
