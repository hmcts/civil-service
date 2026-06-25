package uk.gov.hmcts.reform.civil.scheduler.hearingcvplink;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.scheduler.common.ScheduledTaskEventConfiguration;
import uk.gov.hmcts.reform.civil.scheduler.common.ScheduledTaskRunner;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.search.CaseHearingDateSearchService;
import uk.gov.hmcts.reform.civil.service.search.common.ElasticSearchResult;

import java.util.stream.Stream;
import java.util.function.Consumer;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HearingCvpLinkSchedulerTest {

    @Mock
    private CaseHearingDateSearchService searchService;

    @Mock
    private ScheduledTaskRunner scheduledTaskRunner;

    @Mock
    private HearingCvpLinkScheduledTask hearingCvpLinkScheduledTask;

    @Mock
    private FeatureToggleService featureToggleService;

    @Captor
    private ArgumentCaptor<Consumer<CaseDetails>> taskCaptor;

    @InjectMocks
    private HearingCvpLinkScheduler scheduler;

    @Test
    void shouldRunTaskRunnerWhenScheduledTaskRuns() {
        when(featureToggleService.isSpringSchedulerEnabled()).thenReturn(true);
        ScheduledTaskEventConfiguration expectedConfig = new ScheduledTaskEventConfiguration(scheduler.getName());
        CaseDetails caseDetails = CaseDetails.builder().id(123L).build();
        ElasticSearchResult elasticSearchResult = new ElasticSearchResult(Stream.of(caseDetails), 1);
        when(searchService.getElasticSearchResult()).thenReturn(elasticSearchResult);

        scheduler.runScheduledTask();

        verify(scheduledTaskRunner).run(
            eq(expectedConfig),
            eq(elasticSearchResult),
            taskCaptor.capture()
        );
        taskCaptor.getValue().accept(caseDetails);
        verify(hearingCvpLinkScheduledTask).accept(caseDetails, 1);
    }

    @Test
    void shouldNotRunTaskRunnerWhenSpringSchedulerFeatureToggleIsDisabled() {
        when(featureToggleService.isSpringSchedulerEnabled()).thenReturn(false);

        scheduler.runScheduledTask();

        verifyNoInteractions(searchService, scheduledTaskRunner, hearingCvpLinkScheduledTask);
    }
}
