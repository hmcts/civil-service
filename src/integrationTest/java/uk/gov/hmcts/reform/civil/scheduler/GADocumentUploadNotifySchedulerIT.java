package uk.gov.hmcts.reform.civil.scheduler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.Application;
import uk.gov.hmcts.reform.civil.config.TestIdamConfiguration;
import uk.gov.hmcts.reform.civil.ga.service.search.GaEvidenceUploadNotificationSearchService;
import uk.gov.hmcts.reform.civil.sampledata.CaseDetailsBuilder;
import uk.gov.hmcts.reform.civil.scheduler.gadocumentuploadnotify.GADocumentUploadNotifyScheduledTask;
import uk.gov.hmcts.reform.civil.scheduler.gadocumentuploadnotify.GADocumentUploadNotifyScheduler;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.TelemetryService;

import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ActiveProfiles("integration-test")
@SpringBootTest(classes = {Application.class, TestIdamConfiguration.class}, properties = {
    "test.id=GADocumentUploadNotifySchedulerIT",
    "scheduler.ga-document-upload-notify.enabled=true",
    "scheduler.lockAtLeastFor=PT0S"
})
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
public class GADocumentUploadNotifySchedulerIT {

    private static final Long CASE_ID = 123L;
    private static final String SCHEDULER_NAME = "GADocUploadNotifyScheduler";

    @Autowired
    private GADocumentUploadNotifyScheduler scheduler;

    @MockBean
    private TelemetryService telemetryService;

    @MockBean
    private FeatureToggleService featureToggleService;

    @MockBean
    private GaEvidenceUploadNotificationSearchService searchService;

    @MockBean
    private GADocumentUploadNotifyScheduledTask gaDocumentUploadNotifyScheduledTask;

    @BeforeEach
    void setUp() {
        reset(telemetryService, featureToggleService, searchService, gaDocumentUploadNotifyScheduledTask);
        when(featureToggleService.isSpringSchedulerEnabled(SCHEDULER_NAME)).thenReturn(true);
        when(gaDocumentUploadNotifyScheduledTask.maxCasesPerRun()).thenReturn(Long.MAX_VALUE);
        when(gaDocumentUploadNotifyScheduledTask.getItemId(any(CaseDetails.class))).thenAnswer(invocation ->
            invocation.<CaseDetails>getArgument(0).getId());
    }

    @Test
    void shouldExecuteGADocumentUploadNotifyScheduler() {
        CaseDetails searchCase = CaseDetailsBuilder.builder().id(CASE_ID).build();
        when(searchService.getApplications()).thenReturn(Set.of(searchCase));

        scheduler.runScheduledTask();

        verify(gaDocumentUploadNotifyScheduledTask).accept(searchCase);
        verify(telemetryService).trackEvent(eq("GADocUploadNotifySchedulerJobStarted"), anyMap());
        verify(telemetryService).trackEvent(eq("GADocUploadNotifySchedulerCaseProcessed"), anyMap());
        verify(telemetryService).trackEvent(eq("GADocUploadNotifySchedulerJobCompleted"), anyMap());
    }
}
