package uk.gov.hmcts.reform.civil.scheduler.mediationfiletransfer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.scheduler.common.ScheduledTaskEventConfiguration;
import uk.gov.hmcts.reform.civil.scheduler.common.ScheduledTaskRunner;
import uk.gov.hmcts.reform.civil.scheduler.common.TaskResult;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.search.MediationSearchService;

import static org.mockito.ArgumentMatchers.any;
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
    void shouldRunCsvAndJsonMediationFileTransferTasks() {
        when(featureToggleService.isSpringSchedulerEnabled(SCHEDULER_NAME)).thenReturn(true);
        when(searchService.getInMediationCsv()).thenReturn(csvResult);
        when(searchService.getInMediationJson()).thenReturn(jsonResult);

        scheduler.runScheduledTask();

        verify(scheduledTaskRunner).run(any(ScheduledTaskEventConfiguration.class), eq(csvResult), eq(task));
        verify(scheduledTaskRunner).run(any(ScheduledTaskEventConfiguration.class), eq(jsonResult), eq(task));
    }

    @Test
    void shouldNotRunWhenSpringSchedulerFeatureToggleIsDisabled() {
        when(featureToggleService.isSpringSchedulerEnabled(SCHEDULER_NAME)).thenReturn(false);

        scheduler.runScheduledTask();

        verifyNoInteractions(scheduledTaskRunner, searchService, task);
    }
}
