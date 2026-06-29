package uk.gov.hmcts.reform.civil.scheduler;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.civil.Application;
import uk.gov.hmcts.reform.civil.config.TestIdamConfiguration;
import uk.gov.hmcts.reform.civil.scheduler.common.ScheduledEventTracker;
import uk.gov.hmcts.reform.civil.scheduler.common.ScheduledTaskEventConfiguration;
import uk.gov.hmcts.reform.civil.scheduler.mediationfiletransfer.MediationFileTransferResult;
import uk.gov.hmcts.reform.civil.scheduler.mediationfiletransfer.MediationFileTransferScheduledTask;
import uk.gov.hmcts.reform.civil.scheduler.mediationfiletransfer.MediationFileTransferScheduler;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ActiveProfiles("integration-test")
@SpringBootTest(classes = {Application.class, TestIdamConfiguration.class}, properties = {
    "test.id=MediationFileTransferSchedulerITest",
    "scheduler.bundleCreation.enabled=false",
    "scheduler.hearingCvpLink.enabled=false",
    "scheduler.pollingEventEmitter.enabled=false",
    "scheduler.automatedHearingNotice.enabled=false",
    "scheduler.mediationFileTransfer.enabled=true"
})
public class MediationFileTransferSchedulerITest {

    private static final String SCHEDULER_NAME = "GenerateCsvAndSendToMmt";

    @Autowired
    private MediationFileTransferScheduler scheduler;

    @MockBean
    private MediationFileTransferScheduledTask scheduledTask;

    @MockBean
    private ScheduledEventTracker eventTracker;

    @MockBean
    private FeatureToggleService featureToggleService;

    @Test
    void shouldExecuteMediationFileTransferScheduler() {
        when(featureToggleService.isSpringSchedulerEnabled(SCHEDULER_NAME)).thenReturn(true);
        when(scheduledTask.generateCsvAndTransfer(5)).thenReturn(
            new MediationFileTransferResult(List.of("1"), List.of("1"), List.of(), false, null)
        );
        when(scheduledTask.generateJsonAndTransfer(5)).thenReturn(
            new MediationFileTransferResult(List.of(), List.of(), List.of(), false, null)
        );

        scheduler.runScheduledTask();

        ScheduledTaskEventConfiguration eventConfiguration = new ScheduledTaskEventConfiguration(SCHEDULER_NAME);
        verify(scheduledTask).generateCsvAndTransfer(5);
        verify(scheduledTask).generateJsonAndTransfer(5);
        verify(eventTracker).jobStartedEvent(eventConfiguration, 1);
        verify(eventTracker).caseProcessedEvent(eventConfiguration, "1");
        verify(eventTracker).jobCompletedEvent(eventConfiguration, 1, 1, 0);
    }
}
