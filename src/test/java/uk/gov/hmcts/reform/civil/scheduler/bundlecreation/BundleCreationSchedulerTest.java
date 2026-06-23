package uk.gov.hmcts.reform.civil.scheduler.bundlecreation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.scheduler.common.ScheduledTaskEventConfiguration;
import uk.gov.hmcts.reform.civil.scheduler.common.ScheduledTaskRunner;
import uk.gov.hmcts.reform.civil.service.search.BundleCreationTriggerService;
import uk.gov.hmcts.reform.civil.service.search.common.ElasticSearchResult;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BundleCreationSchedulerTest {

    @Mock
    private BundleCreationTriggerService searchService;
    @Mock
    private ScheduledTaskRunner scheduledTaskRunner;
    @Mock
    private BundleCreationScheduledTask bundleCreationScheduledTask;
    @InjectMocks
    private BundleCreationScheduler scheduler;

    @Test
    void shouldRunBundleCreationTask() {
        ElasticSearchResult searchResult = new ElasticSearchResult(Stream.empty(), 0);
        when(searchService.getElasticSearchResult()).thenReturn(searchResult);

        scheduler.runScheduledTask();

        assertThat(scheduler.getName()).isEqualTo("BundleCreation");
        verify(scheduledTaskRunner).run(
            new ScheduledTaskEventConfiguration("BundleCreation"),
            searchResult,
            bundleCreationScheduledTask
        );
    }
}
