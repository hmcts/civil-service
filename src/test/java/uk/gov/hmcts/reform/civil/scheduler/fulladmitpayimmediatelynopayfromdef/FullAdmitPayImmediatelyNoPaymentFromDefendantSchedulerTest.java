package uk.gov.hmcts.reform.civil.scheduler.fulladmitpayimmediatelynopayfromdef;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.scheduler.common.ScheduledTaskConfiguration;
import uk.gov.hmcts.reform.civil.scheduler.common.ScheduledTaskRunner;
import uk.gov.hmcts.reform.civil.service.search.fulladmitpayimmediatelynopayfromdef.FullAdmitPayImmediatelyNoPaymentFromDefendantPaginatedSearchService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FullAdmitPayImmediatelyNoPaymentFromDefendantSchedulerTest {

    @Mock
    private FullAdmitPayImmediatelyNoPaymentFromDefendantPaginatedSearchService searchService;

    @Mock
    private ScheduledTaskRunner<CaseDetails, Long> scheduledTaskRunner;

    @Mock
    private FullAdmitPayImmediatelyNoPaymentFromDefendantScheduledTask scheduledTask;

    @Captor
    private ArgumentCaptor<ScheduledTaskConfiguration<CaseDetails, Long>> configCaptor;

    @InjectMocks
    private FullAdmitPayImmediatelyNoPaymentFromDefendantScheduler scheduler;

    @BeforeEach
    void setUp() {
        scheduler = new FullAdmitPayImmediatelyNoPaymentFromDefendantScheduler(
            searchService,
            scheduledTaskRunner,
            scheduledTask
        );
    }

    @Test
    void shouldReturnSchedulerName() {
        assertThat(scheduler.getName())
            .isEqualTo(FullAdmitPayImmediatelyNoPaymentFromDefendantScheduler.SCHEDULER_NAME);
    }

    @Test
    void shouldRunScheduledTask() {
        scheduler.runScheduledTask();

        verify(scheduledTaskRunner).run(configCaptor.capture());

        ScheduledTaskConfiguration<CaseDetails, Long> config = configCaptor.getValue();
        assertThat(config.getSchedulerName()).isEqualTo(scheduler.getName());
        assertThat(config.getSearchResultSupplier().get()).isEqualTo(searchService.getElasticSearchResult());
        assertThat(config.getScheduledTask()).isEqualTo(scheduledTask);
        assertThat(config.isUseDefaultInterceptors()).isFalse();
    }
}
