package uk.gov.hmcts.reform.civil.scheduler;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.AopTestUtils;
import uk.gov.hmcts.reform.civil.Application;
import uk.gov.hmcts.reform.civil.config.TestIdamConfiguration;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.scheduler.common.ScheduledTaskRunner;
import uk.gov.hmcts.reform.civil.scheduler.common.TaskResult;
import uk.gov.hmcts.reform.civil.scheduler.mediationfiletransfer.MediationFileTransferScheduledTask;
import uk.gov.hmcts.reform.civil.scheduler.mediationfiletransfer.MediationFileTransferScheduler;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.search.MediationSearchService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ActiveProfiles("integration-test")
@SpringBootTest(classes = {Application.class, TestIdamConfiguration.class}, properties = {
    "test.id=MediationFileTransferSchedulerIT",
    "scheduler.bundle-creation.enabled=false",
    "scheduler.hearing-cvp-link.enabled=false",
    "scheduler.polling-event-emitter.enabled=false",
    "scheduler.automated-hearing-notice.enabled=false",
    "scheduler.mediation-file-transfer.enabled=true"
})
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
public class MediationFileTransferSchedulerIT {

    private static final String SCHEDULER_NAME = "GenerateCsvAndSendToMmt";

    @Autowired
    private MediationFileTransferScheduler scheduler;

    @MockBean
    private MediationSearchService searchService;

    @MockBean
    private ScheduledTaskRunner<CaseData, Long> scheduledTaskRunner;

    @MockBean
    private MediationFileTransferScheduledTask task;

    @MockBean
    private FeatureToggleService featureToggleService;

    @Test
    @SuppressWarnings("unchecked")
    void shouldExecuteMediationFileTransferScheduler() throws Exception {
        when(featureToggleService.isSpringSchedulerEnabled(SCHEDULER_NAME)).thenReturn(true);
        TaskResult<CaseData> csvResult = mock(TaskResult.class);
        TaskResult<CaseData> jsonResult = mock(TaskResult.class);
        when(searchService.getInMediationCsv()).thenReturn(csvResult);
        when(searchService.getInMediationJson()).thenReturn(jsonResult);

        AopTestUtils.<MediationFileTransferScheduler>getTargetObject(scheduler).runScheduledTask();

        verify(scheduledTaskRunner, atLeastOnce()).run(eq(SCHEDULER_NAME + "_CSV"), any(), eq(task));
        verify(scheduledTaskRunner, atLeastOnce()).run(eq(SCHEDULER_NAME + "_JSON"), any(), eq(task));
        verify(searchService).getInMediationCsv();
        verify(searchService).getInMediationJson();
    }
}
