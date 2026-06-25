package uk.gov.hmcts.reform.civil.scheduler.pollingeventemitter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.scheduler.common.ScheduledTaskEventConfiguration;
import uk.gov.hmcts.reform.civil.scheduler.common.ScheduledTaskRunner;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.search.CaseReadyBusinessProcessSearchService;
import uk.gov.hmcts.reform.civil.service.search.common.ElasticSearchResult;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PollingEventEmitterSchedulerTest {

    private static final long CASE_ID = 123L;

    @Mock
    private CaseReadyBusinessProcessSearchService searchService;

    @Mock
    private ScheduledTaskRunner scheduledTaskRunner;

    @Mock
    private PollingEventEmitterScheduledTask pollingEventEmitterScheduledTask;

    @Mock
    private FeatureToggleService featureToggleService;

    private PollingEventEmitterScheduler scheduler;

    @BeforeEach
    void setUp() {
        scheduler = new PollingEventEmitterScheduler(
            searchService,
            scheduledTaskRunner,
            pollingEventEmitterScheduledTask,
            featureToggleService
        );
        ReflectionTestUtils.setField(scheduler, "multiCasesExecutionDelayInSeconds", 30L);
    }

    @Test
    void shouldRunPollingEventEmitterScheduler() {
        when(featureToggleService.isSpringSchedulerEnabled()).thenReturn(true);
        CaseDetails caseDetails = CaseDetails.builder().id(CASE_ID).build();
        ElasticSearchResult searchResult = new ElasticSearchResult(List.of(caseDetails).stream(), 1);
        when(searchService.getElasticSearchResult()).thenReturn(searchResult);
        doAnswer(invocation -> {
            Consumer<CaseDetails> task = invocation.getArgument(2);
            task.accept(caseDetails);
            return null;
        }).when(scheduledTaskRunner).run(any(), eq(searchResult), any());

        scheduler.runScheduledTask();

        verify(scheduledTaskRunner).run(
            eq(new ScheduledTaskEventConfiguration("PollingEventEmitter")),
            eq(searchResult),
            any()
        );
        verify(pollingEventEmitterScheduledTask).accept(caseDetails, 1L, 30000L);
    }

    @Test
    void shouldLimitCasesToFitWithinFiftyMinuteProcessingWindow() {
        when(featureToggleService.isSpringSchedulerEnabled()).thenReturn(true);
        ReflectionTestUtils.setField(scheduler, "multiCasesExecutionDelayInSeconds", 30L);
        List<CaseDetails> cases = IntStream.rangeClosed(1, 101)
            .mapToObj(caseId -> CaseDetails.builder().id((long) caseId).build())
            .toList();
        when(searchService.getElasticSearchResult()).thenReturn(new ElasticSearchResult(cases.stream(), cases.size()));

        scheduler.runScheduledTask();

        ArgumentCaptor<ElasticSearchResult> searchResultCaptor = ArgumentCaptor.forClass(ElasticSearchResult.class);
        verify(scheduledTaskRunner).run(
            eq(new ScheduledTaskEventConfiguration("PollingEventEmitter")),
            searchResultCaptor.capture(),
            any()
        );

        ElasticSearchResult limitedSearchResult = searchResultCaptor.getValue();
        assertThat(limitedSearchResult.totalResults()).isEqualTo(100);
        assertThat(limitedSearchResult.caseDetailsStream()).hasSize(100);
    }

    @Test
    void shouldTreatConfiguredDelayBelowOneSecondAsOneSecond() {
        when(featureToggleService.isSpringSchedulerEnabled()).thenReturn(true);
        ReflectionTestUtils.setField(scheduler, "multiCasesExecutionDelayInSeconds", 0L);
        List<CaseDetails> cases = IntStream.rangeClosed(1, 3001)
            .mapToObj(caseId -> CaseDetails.builder().id((long) caseId).build())
            .toList();
        when(searchService.getElasticSearchResult()).thenReturn(new ElasticSearchResult(cases.stream(), cases.size()));

        scheduler.runScheduledTask();

        ArgumentCaptor<ElasticSearchResult> searchResultCaptor = ArgumentCaptor.forClass(ElasticSearchResult.class);
        verify(scheduledTaskRunner).run(
            eq(new ScheduledTaskEventConfiguration("PollingEventEmitter")),
            searchResultCaptor.capture(),
            any()
        );

        ElasticSearchResult limitedSearchResult = searchResultCaptor.getValue();
        assertThat(limitedSearchResult.totalResults()).isEqualTo(3000);
        assertThat(limitedSearchResult.caseDetailsStream()).hasSize(3000);
    }

    @Test
    void shouldPassNullSearchResultToRunner() {
        when(featureToggleService.isSpringSchedulerEnabled()).thenReturn(true);
        when(searchService.getElasticSearchResult()).thenReturn(null);

        scheduler.runScheduledTask();

        verify(scheduledTaskRunner).run(
            eq(new ScheduledTaskEventConfiguration("PollingEventEmitter")),
            eq(null),
            any()
        );
        verify(pollingEventEmitterScheduledTask, never()).accept(any(), anyLong(), anyLong());
    }

    @Test
    void shouldNotRunPollingEventEmitterSchedulerWhenSpringSchedulerFeatureToggleIsDisabled() {
        when(featureToggleService.isSpringSchedulerEnabled()).thenReturn(false);

        scheduler.runScheduledTask();

        verifyNoInteractions(searchService, scheduledTaskRunner, pollingEventEmitterScheduledTask);
    }
}
