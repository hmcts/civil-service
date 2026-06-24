package uk.gov.hmcts.reform.civil.scheduler.bundlecreation;

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
import uk.gov.hmcts.reform.civil.service.search.BundleCreationTriggerService;
import uk.gov.hmcts.reform.civil.service.search.common.ElasticSearchResult;

import java.util.stream.Stream;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
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
    @Captor
    private ArgumentCaptor<Consumer<CaseDetails>> taskCaptor;
    @InjectMocks
    private BundleCreationScheduler scheduler;

    @Test
    void shouldRunBundleCreationTask() {
        CaseDetails caseDetails = CaseDetails.builder().id(123L).build();
        ElasticSearchResult searchResult = new ElasticSearchResult(Stream.of(caseDetails), 1);
        when(searchService.getElasticSearchResult()).thenReturn(searchResult);

        scheduler.runScheduledTask();

        assertThat(scheduler.getName()).isEqualTo("BundleCreation");
        verify(scheduledTaskRunner).run(
            eq(new ScheduledTaskEventConfiguration("BundleCreation")),
            eq(searchResult),
            taskCaptor.capture()
        );
        taskCaptor.getValue().accept(caseDetails);
        verify(bundleCreationScheduledTask).accept(caseDetails, 1);
    }
}
