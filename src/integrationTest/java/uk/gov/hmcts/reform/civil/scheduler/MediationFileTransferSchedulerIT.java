package uk.gov.hmcts.reform.civil.scheduler;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.scheduler.common.ListTaskResult;
import uk.gov.hmcts.reform.civil.scheduler.common.ScheduledTaskRunner;
import uk.gov.hmcts.reform.civil.scheduler.common.TaskResult;
import uk.gov.hmcts.reform.civil.scheduler.mediationfiletransfer.MediationFileTransferScheduledTask;
import uk.gov.hmcts.reform.civil.scheduler.mediationfiletransfer.MediationFileTransferScheduler;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.mediation.MediationFileTransferService;
import uk.gov.hmcts.reform.civil.service.search.MediationSearchService;

import java.util.List;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class MediationFileTransferSchedulerIT {

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

    @Test
    @SuppressWarnings("unchecked")
    void shouldExecuteMediationFileTransferScheduler() {
        CaseData csvCase = CaseData.builder().ccdCaseReference(1L).build();
        CaseData jsonCase = CaseData.builder().ccdCaseReference(2L).build();
        TaskResult<CaseData> csvResult = new ListTaskResult<>(List.of(csvCase), 1);
        TaskResult<CaseData> jsonResult = new ListTaskResult<>(List.of(jsonCase), 1);
        when(featureToggleService.isSpringSchedulerEnabled(SCHEDULER_NAME)).thenReturn(true);
        when(searchService.getInMediationCsv()).thenReturn(csvResult);
        when(searchService.getInMediationJson()).thenReturn(jsonResult);

        new MediationFileTransferScheduler(
            searchService,
            scheduledTaskRunner,
            task,
            featureToggleService,
            mediationFileTransferService
        ).runScheduledTask();

        ArgumentCaptor<Supplier<TaskResult<CaseData>>> supplierCaptor = ArgumentCaptor.forClass(Supplier.class);

        verify(scheduledTaskRunner, times(2)).run(eq(SCHEDULER_NAME), supplierCaptor.capture(), eq(task));

        TaskResult<CaseData> csvTaskResult = supplierCaptor.getAllValues().get(0).get();
        assertThat(csvTaskResult.itemStream()).containsExactly(csvCase);
        assertThat(csvTaskResult.totalResults()).isEqualTo(1);
        verify(mediationFileTransferService).sendCsv(List.of(csvCase));
        verify(searchService).getInMediationCsv();

        TaskResult<CaseData> jsonTaskResult = supplierCaptor.getAllValues().get(1).get();
        assertThat(jsonTaskResult.itemStream()).containsExactly(jsonCase);
        assertThat(jsonTaskResult.totalResults()).isEqualTo(1);
        verify(mediationFileTransferService).sendJson(List.of(jsonCase));
        verify(searchService).getInMediationJson();
    }
}
