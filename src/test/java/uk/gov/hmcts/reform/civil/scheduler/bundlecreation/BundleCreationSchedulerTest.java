package uk.gov.hmcts.reform.civil.scheduler.bundlecreation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.scheduler.common.ElasticSearchSchedulerRunner;
import uk.gov.hmcts.reform.civil.service.search.BundleCreationTriggerService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class BundleCreationSchedulerTest {

    private static final String SCHEDULER_NAME = "BundleCreation";

    @Mock
    private BundleCreationTriggerService searchService;
    @Mock
    private BundleCreationScheduledTask bundleCreationScheduledTask;
    @Mock
    private ElasticSearchSchedulerRunner elasticSearchSchedulerRunner;
    @InjectMocks
    private BundleCreationScheduler scheduler;

    @Test
    void shouldRunBundleCreationTask() {
        scheduler.runScheduledTask();

        assertThat(scheduler.getName()).isEqualTo("BundleCreation");
        verify(elasticSearchSchedulerRunner).run(
            eq(SCHEDULER_NAME),
            any(),
            any()
        );
    }
}
