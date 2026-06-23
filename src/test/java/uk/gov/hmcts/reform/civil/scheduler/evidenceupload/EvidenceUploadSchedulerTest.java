package uk.gov.hmcts.reform.civil.scheduler.evidenceupload;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.scheduler.common.ScheduledTaskEventConfiguration;
import uk.gov.hmcts.reform.civil.scheduler.common.ScheduledTaskRunner;
import uk.gov.hmcts.reform.civil.service.search.EvidenceUploadNotificationSearchService;
import uk.gov.hmcts.reform.civil.service.search.common.ElasticSearchResult;

import java.util.stream.Stream;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EvidenceUploadSchedulerTest {

    @Mock
    private EvidenceUploadNotificationSearchService searchService;

    @Mock
    private ScheduledTaskRunner scheduledTaskRunner;

    @Mock
    private EvidenceUploadSchedulerTask evidenceUploadSchedulerTask;

    @InjectMocks
    private EvidenceUploadScheduler scheduler;

    @Test
    void shouldRunTaskRunner_whenSchedulerIsEnabled() {
        ScheduledTaskEventConfiguration expectedConfig = new ScheduledTaskEventConfiguration(scheduler.getName());
        ElasticSearchResult elasticSearchResult = new ElasticSearchResult(Stream.empty(), 0);
        when(searchService.getElasticSearchResult()).thenReturn(elasticSearchResult);

        scheduler.runScheduledTask();

        verify(scheduledTaskRunner).run(
            expectedConfig,
            elasticSearchResult,
            evidenceUploadSchedulerTask
        );
    }
}
