package uk.gov.hmcts.reform.civil.scheduler.mediationfiletransfer;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.scheduler.common.ListTaskResult;
import uk.gov.hmcts.reform.civil.scheduler.common.ScheduledTaskRunner;
import uk.gov.hmcts.reform.civil.scheduler.common.TaskResult;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.mediation.MediationFileTransferService;
import uk.gov.hmcts.reform.civil.service.search.MediationSearchService;

import java.util.List;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MediationFileTransferSchedulerTest {

    private static final String SCHEDULER_NAME = "GenerateCsvAndSendToMmt";

    @Mock
    private MediationSearchService searchService;
    @Mock
    private ScheduledTaskRunner<CaseData, Long> scheduledTaskRunner;
    @Mock
    private MediationFileTransferScheduledTask task;
    @Mock
    private FeatureToggleService featureToggleService;
    @Mock
    private MediationFileTransferService mediationFileTransferService;

    @InjectMocks
    private MediationFileTransferScheduler scheduler;

    @Test
    @SuppressWarnings("unchecked")
    void shouldRunCsvAndJsonMediationFileTransferTasks() {
        CaseData csvFailedCase = CaseData.builder().ccdCaseReference(1L).build();
        CaseData csvSuccessfulCase = CaseData.builder().ccdCaseReference(2L).build();
        CaseData jsonFailedCase = CaseData.builder().ccdCaseReference(3L).build();
        CaseData jsonSuccessfulCase = CaseData.builder().ccdCaseReference(4L).build();
        List<CaseData> csvCases = List.of(csvFailedCase, csvSuccessfulCase);
        List<CaseData> jsonCases = List.of(jsonFailedCase, jsonSuccessfulCase);
        TaskResult<CaseData> csvResult = new ListTaskResult<>(csvCases, 2);
        TaskResult<CaseData> jsonResult = new ListTaskResult<>(jsonCases, 2);
        when(featureToggleService.isSpringSchedulerEnabled(SCHEDULER_NAME)).thenReturn(true);
        when(searchService.getInMediationCsv()).thenReturn(csvResult);
        when(searchService.getInMediationJson()).thenReturn(jsonResult);
        when(mediationFileTransferService.sendCsv(csvCases)).thenReturn(List.of(csvSuccessfulCase));
        when(mediationFileTransferService.sendJson(jsonCases)).thenReturn(List.of(jsonSuccessfulCase));

        scheduler.runScheduledTask();

        ArgumentCaptor<Supplier<TaskResult<CaseData>>> supplierCaptor = ArgumentCaptor.forClass(Supplier.class);

        verify(scheduledTaskRunner, times(2)).run(eq(SCHEDULER_NAME), supplierCaptor.capture(), eq(task));

        TaskResult<CaseData> csvTaskResult = supplierCaptor.getAllValues().get(0).get();
        assertThat(csvTaskResult.itemStream()).containsExactly(csvSuccessfulCase);
        assertThat(csvTaskResult.totalResults()).isEqualTo(2);
        verify(mediationFileTransferService).sendCsv(csvCases);

        TaskResult<CaseData> jsonTaskResult = supplierCaptor.getAllValues().get(1).get();
        assertThat(jsonTaskResult.itemStream()).containsExactly(jsonSuccessfulCase);
        assertThat(jsonTaskResult.totalResults()).isEqualTo(2);
        verify(mediationFileTransferService).sendJson(jsonCases);
    }

    @Test
    void shouldNotRunWhenSpringSchedulerFeatureToggleIsDisabled() {
        when(featureToggleService.isSpringSchedulerEnabled(SCHEDULER_NAME)).thenReturn(false);

        scheduler.runScheduledTask();

        verifyNoInteractions(scheduledTaskRunner, searchService, task);
        verifyNoInteractions(mediationFileTransferService);
    }
}
