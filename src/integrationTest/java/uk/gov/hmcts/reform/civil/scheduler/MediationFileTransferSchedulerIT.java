package uk.gov.hmcts.reform.civil.scheduler;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.scheduler.common.ScheduledTaskRunner;
import uk.gov.hmcts.reform.civil.scheduler.common.TaskResult;
import uk.gov.hmcts.reform.civil.scheduler.mediationfiletransfer.MediationFileTransferScheduledTask;
import uk.gov.hmcts.reform.civil.scheduler.mediationfiletransfer.MediationFileTransferScheduler;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.search.MediationSearchService;

import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
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

    @Test
    @SuppressWarnings("unchecked")
    void shouldExecuteMediationFileTransferScheduler() {
        when(featureToggleService.isSpringSchedulerEnabled(SCHEDULER_NAME)).thenReturn(true);
        TaskResult<CaseData> csvResult = mock(TaskResult.class);
        TaskResult<CaseData> jsonResult = mock(TaskResult.class);
        when(searchService.getInMediationCsv()).thenReturn(csvResult);
        when(searchService.getInMediationJson()).thenReturn(jsonResult);

        new MediationFileTransferScheduler(
            searchService,
            scheduledTaskRunner,
            task,
            featureToggleService
        ).runScheduledTask();

        ArgumentCaptor<Supplier<? extends TaskResult<CaseData>>> csvSupplierCaptor =
            ArgumentCaptor.forClass(Supplier.class);
        ArgumentCaptor<Supplier<? extends TaskResult<CaseData>>> jsonSupplierCaptor =
            ArgumentCaptor.forClass(Supplier.class);

        verify(scheduledTaskRunner).run(eq(SCHEDULER_NAME + "_CSV"), csvSupplierCaptor.capture(), eq(task));
        verify(scheduledTaskRunner).run(eq(SCHEDULER_NAME + "_JSON"), jsonSupplierCaptor.capture(), eq(task));

        assertThat(csvSupplierCaptor.getValue().get()).isSameAs(csvResult);
        assertThat(jsonSupplierCaptor.getValue().get()).isSameAs(jsonResult);
        verify(searchService).getInMediationCsv();
        verify(searchService).getInMediationJson();
    }
}
