package uk.gov.hmcts.reform.civil.scheduler.mediationfiletransfer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.scheduler.common.ScheduledTaskRunner;
import uk.gov.hmcts.reform.civil.scheduler.common.TaskResult;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.search.MediationSearchService;

import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
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
    private TaskResult<CaseData> csvResult;
    @Mock
    private TaskResult<CaseData> jsonResult;

    private MediationFileTransferScheduler scheduler;

    @BeforeEach
    void setUp() {
        scheduler = new MediationFileTransferScheduler(searchService, scheduledTaskRunner, task, featureToggleService);
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldRunCsvAndJsonMediationFileTransferTasks() {
        when(featureToggleService.isSpringSchedulerEnabled(SCHEDULER_NAME)).thenReturn(true);
        when(searchService.getInMediationCsv()).thenReturn(csvResult);
        when(searchService.getInMediationJson()).thenReturn(jsonResult);

        scheduler.runScheduledTask();

        ArgumentCaptor<Supplier<TaskResult<CaseData>>> supplierCaptor = ArgumentCaptor.forClass(Supplier.class);

        verify(scheduledTaskRunner).run(eq(SCHEDULER_NAME + "_CSV"), supplierCaptor.capture(), eq(task));
        assertThat(supplierCaptor.getValue().get()).isEqualTo(csvResult);

        verify(scheduledTaskRunner).run(eq(SCHEDULER_NAME + "_JSON"), supplierCaptor.capture(), eq(task));
        assertThat(supplierCaptor.getValue().get()).isEqualTo(jsonResult);
    }

    @Test
    void shouldNotRunWhenSpringSchedulerFeatureToggleIsDisabled() {
        when(featureToggleService.isSpringSchedulerEnabled(SCHEDULER_NAME)).thenReturn(false);

        scheduler.runScheduledTask();

        verifyNoInteractions(scheduledTaskRunner, searchService, task);
    }
}
