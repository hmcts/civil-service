package uk.gov.hmcts.reform.civil.scheduler.fulladmitpayimmediatelynopayfromdef;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.scheduler.common.ScheduledTaskRunner;
import uk.gov.hmcts.reform.civil.service.search.FullAdmitPayImmediatelyNoPaymentFromDefendantSearchService;

import java.util.function.Supplier;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FullAdmitPayImmediatelyNoPaymentFromDefendantSchedulerTest {

    @Mock
    private FullAdmitPayImmediatelyNoPaymentFromDefendantSearchService searchService;

    @Mock
    private ScheduledTaskRunner<CaseDetails, Long> scheduledTaskRunner;

    @Mock
    private FullAdmitPayImmediatelyNoPaymentFromDefendantScheduledTask scheduledTask;

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
    @SuppressWarnings("unchecked")
    void shouldRunScheduledTask() {
        scheduler.runScheduledTask();

        verify(scheduledTaskRunner).run(
            eq(FullAdmitPayImmediatelyNoPaymentFromDefendantScheduler.SCHEDULER_NAME),
            any(Supplier.class),
            eq(scheduledTask)
        );
    }
}
