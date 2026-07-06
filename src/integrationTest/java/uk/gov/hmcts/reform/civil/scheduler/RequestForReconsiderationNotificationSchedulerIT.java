package uk.gov.hmcts.reform.civil.scheduler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SearchResult;
import uk.gov.hmcts.reform.civil.Application;
import uk.gov.hmcts.reform.civil.config.TestIdamConfiguration;
import uk.gov.hmcts.reform.civil.sampledata.CaseDetailsBuilder;
import uk.gov.hmcts.reform.civil.scheduler.requestforreconsideration.RequestForReconsiderationNotificationScheduledTask;
import uk.gov.hmcts.reform.civil.scheduler.requestforreconsideration.RequestForReconsiderationNotificationScheduler;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.TelemetryService;
import uk.gov.hmcts.test.config.CoreCaseDataApiMockHelperConfiguration;
import uk.gov.hmcts.test.helper.CoreCaseDataApiMockHelper;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ActiveProfiles("integration-test")
@SpringBootTest(classes = {Application.class, TestIdamConfiguration.class, CoreCaseDataApiMockHelperConfiguration.class}, properties = {
    "test.id=RequestForReconsiderationNotificationSchedulerIT",
    "scheduler.request-for-reconsideration-notification.enabled=true"
})
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
public class RequestForReconsiderationNotificationSchedulerIT {

    private static final Long CASE_ID = 123L;
    private static final String SCHEDULER_NAME = "REQUEST_FOR_RECONSIDERATION_NOTIFICATION_CHECK";

    @Autowired
    private RequestForReconsiderationNotificationScheduler scheduler;

    @MockBean
    private TelemetryService telemetryService;

    @MockBean
    private FeatureToggleService featureToggleService;

    @MockBean
    private RequestForReconsiderationNotificationScheduledTask requestForReconsiderationNotificationScheduledTask;

    @Autowired
    private CoreCaseDataApiMockHelper coreCaseDataApiMockHelper;

    @BeforeEach
    void setUp() {
        coreCaseDataApiMockHelper.setupIdamClient();
        when(featureToggleService.isSpringSchedulerEnabled(SCHEDULER_NAME)).thenReturn(true);
        when(requestForReconsiderationNotificationScheduledTask.maxCasesPerRun()).thenReturn(Long.MAX_VALUE);
        when(requestForReconsiderationNotificationScheduledTask.getItemId(any(CaseDetails.class))).thenAnswer(invocation ->
            invocation.<CaseDetails>getArgument(0).getId());
    }

    @Test
    void shouldExecuteRequestForReconsiderationNotificationScheduler() {
        CaseDetails searchCase = CaseDetailsBuilder.builder().id(CASE_ID).build();
        SearchResult searchResult = SearchResult.builder()
            .total(1)
            .cases(List.of(searchCase))
            .build();

        coreCaseDataApiMockHelper.mockElasticSearchResult(searchResult);

        scheduler.runScheduledTask();

        verify(requestForReconsiderationNotificationScheduledTask).accept(searchCase);
        verify(telemetryService).trackEvent(eq("REQUEST_FOR_RECONSIDERATION_NOTIFICATION_CHECKJobStarted"), anyMap());
        verify(telemetryService).trackEvent(eq("REQUEST_FOR_RECONSIDERATION_NOTIFICATION_CHECKCaseProcessed"), anyMap());
        verify(telemetryService).trackEvent(eq("REQUEST_FOR_RECONSIDERATION_NOTIFICATION_CHECKJobCompleted"), anyMap());
    }
}
