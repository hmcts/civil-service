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
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ActiveProfiles("integration-test")
@SpringBootTest(classes = {Application.class, TestIdamConfiguration.class, CoreCaseDataApiMockHelperConfiguration.class}, properties = {
    "test.id=JudgementBufferSchedulerIT",
    "scheduler.judgement-buffer.enabled=true",
    "search.judgement-buffer.pageSize=50"
})
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
public class JudgementBufferSchedulerIT {

    @Autowired
    private JudgementBufferScheduler scheduler;

    @MockBean
    private TelemetryService telemetryService;

    @MockBean
    private FeatureToggleService featureToggleService;

    @Autowired
    private CoreCaseDataApiMockHelper coreCaseDataApiMockHelper;

    @BeforeEach
    void setUp() {
        when(featureToggleService.isJudgmentBufferEnabled()).thenReturn(true);
        when(featureToggleService.isSpringSchedulerEnabled("JudgementBuffer")).thenReturn(true);
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
        coreCaseDataApiMockHelper.mockStartEventAnyCase(startEventResponse);
        coreCaseDataApiMockHelper.mockSubmitEventAnyCase(CaseDetailsBuilder.builder().id(1L).data(Map.of()).build());

        // When
        scheduler.runScheduledTask();

        // Then
        verify(telemetryService).trackEvent(eq("JudgementBufferJobStarted"), anyMap());
        verify(telemetryService).trackEvent(eq("JudgementBufferJobCompleted"), anyMap());
        coreCaseDataApiMockHelper.verifySubmitEvent(51);
    }

    private List<CaseDetails> createCaseDetailsBatch(int size) {
        return rangeClosed(1, size)
            .mapToObj(i -> CaseDetailsBuilder.builder().id((long) i).data(Map.of()).build())
            .toList();
    }
}
