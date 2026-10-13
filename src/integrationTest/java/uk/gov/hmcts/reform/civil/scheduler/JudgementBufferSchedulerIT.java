package uk.gov.hmcts.reform.civil.scheduler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SearchResult;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.civil.Application;
import uk.gov.hmcts.reform.civil.config.TestIdamConfiguration;
import uk.gov.hmcts.reform.civil.sampledata.CaseDetailsBuilder;
import uk.gov.hmcts.reform.civil.scheduler.judgementbuffer.JudgementBufferScheduler;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.TelemetryService;
import uk.gov.hmcts.test.config.CoreCaseDataApiMockHelperConfiguration;
import uk.gov.hmcts.test.helper.CoreCaseDataApiMockHelper;

import java.util.List;
import java.util.Map;

import static java.util.stream.IntStream.rangeClosed;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.DEFAULT_JUDGEMENT_GRANTED_SPEC;

@ActiveProfiles("integration-test")
@SpringBootTest(classes = {Application.class, TestIdamConfiguration.class, CoreCaseDataApiMockHelperConfiguration.class}, properties = {
    "test.id=JudgementBufferSchedulerIT",
    "search.judgement-buffer.pageSize=50",
    "scheduler.lockAtLeastFor=PT0S"
})
public class JudgementBufferSchedulerIT {

    private static final String SCHEDULER_NAME = JudgementBufferScheduler.SCHEDULER_NAME;

    @Autowired
    private JudgementBufferScheduler scheduler;

    @MockitoBean
    private TelemetryService telemetryService;

    @MockitoBean
    private FeatureToggleService featureToggleService;

    @Autowired
    private CoreCaseDataApiMockHelper coreCaseDataApiMockHelper;

    @BeforeEach
    void setUp() {
        reset(telemetryService, featureToggleService);
        coreCaseDataApiMockHelper.resetMocks();
        when(featureToggleService.isJudgmentBufferEnabled()).thenReturn(true);
        when(featureToggleService.isSpringSchedulerEnabled(JudgementBufferScheduler.SCHEDULER_NAME)).thenReturn(true);
        coreCaseDataApiMockHelper.setupIdamClient();
    }

    @Test
    void shouldExecuteJudgementBufferSchedulerWithPagination() {
        // Given
        // Create 50 cases for the first page to test pagination
        List<CaseDetails> page1Cases = createCaseDetailsBatch(50);
        CaseDetails case51 = CaseDetailsBuilder.builder().id(51L).data(Map.of()).build();

        SearchResult page1 = SearchResult.builder().total(51).cases(page1Cases).build();
        SearchResult page2 = SearchResult.builder().total(51).cases(List.of(case51)).build();

        coreCaseDataApiMockHelper.mockElasticSearchResultPaginated(page1, page2);

        StartEventResponse startEventResponse = StartEventResponse.builder()
            .eventId("eventId")
            .caseDetails(CaseDetails.builder().id(1L).data(Map.of()).build())
            .build();
        // Mock start and submit events for all 51 cases
        coreCaseDataApiMockHelper.mockStartEventAnyCase(startEventResponse, DEFAULT_JUDGEMENT_GRANTED_SPEC.name());
        coreCaseDataApiMockHelper.mockSubmitEventAnyCase(CaseDetailsBuilder.builder().id(1L).data(Map.of()).build());

        // When
        scheduler.runScheduledTask();

        // Then
        verify(telemetryService).trackEvent(eq("SchedulerJobStarted"), argThat(props -> SCHEDULER_NAME.equals(props.get("schedulerName"))));
        verify(telemetryService).trackEvent(eq("SchedulerJobCompleted"), argThat(props -> SCHEDULER_NAME.equals(props.get("schedulerName"))));
        coreCaseDataApiMockHelper.verifySubmitEvent(51);
    }

    private List<CaseDetails> createCaseDetailsBatch(int size) {
        return rangeClosed(1, size)
            .mapToObj(i -> CaseDetailsBuilder.builder().id((long) i).data(Map.of()).build())
            .toList();
    }
}
