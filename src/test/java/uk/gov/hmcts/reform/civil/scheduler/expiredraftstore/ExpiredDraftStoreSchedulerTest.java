package uk.gov.hmcts.reform.civil.scheduler.expiredraftstore;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import uk.gov.hmcts.reform.civil.scheduler.common.ScheduledEventTracker;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExpiredDraftStoreSchedulerTest {

    @Mock
    private FeatureToggleService featureToggleService;

    @Mock
    private ExpiredDraftStoreTask expiredDraftStoreTask;

    @Mock
    private ScheduledEventTracker eventTracker;

    private ExpiredDraftStoreScheduler scheduler;

    @BeforeEach
    void setUp() {
        scheduler = new ExpiredDraftStoreScheduler(
            featureToggleService,
            expiredDraftStoreTask,
            eventTracker,
            500
        );
    }

    @Test
    void shouldReturnSchedulerName() {
        assertThat(scheduler.getName()).isEqualTo(ExpiredDraftStoreScheduler.SCHEDULER_NAME);
    }

    @Test
    void shouldNotDeleteWhenSpringSchedulerIsDisabled() {
        when(featureToggleService.isSpringSchedulerEnabled(ExpiredDraftStoreScheduler.SCHEDULER_NAME))
            .thenReturn(false);

        scheduler.runScheduledTask();

        verifyNoInteractions(expiredDraftStoreTask);
    }

    @Test
    void shouldDeleteExpiredDraftsInSingleBatchWhenEnabled() {
        when(featureToggleService.isSpringSchedulerEnabled(ExpiredDraftStoreScheduler.SCHEDULER_NAME))
            .thenReturn(true);
        when(expiredDraftStoreTask.deleteExpiredBatch(any(OffsetDateTime.class), any(Pageable.class)))
            .thenReturn(2);

        scheduler.runScheduledTask();

        verify(expiredDraftStoreTask).deleteExpiredBatch(any(OffsetDateTime.class), any(Pageable.class));
        verify(eventTracker).jobCompletedBulkEvent(
            argThat(config -> ExpiredDraftStoreScheduler.SCHEDULER_NAME.equals(config.getSchedulerName())),
            eq(2)
        );
    }

    @Test
    void shouldLoopAndPurgeAcrossMultipleBatches() {
        scheduler.setBatchSize(2);

        when(featureToggleService.isSpringSchedulerEnabled(ExpiredDraftStoreScheduler.SCHEDULER_NAME))
            .thenReturn(true);

        when(expiredDraftStoreTask.deleteExpiredBatch(any(OffsetDateTime.class), any(Pageable.class)))
            .thenReturn(2)
            .thenReturn(1);

        scheduler.runScheduledTask();

        verify(expiredDraftStoreTask, times(2)).deleteExpiredBatch(any(OffsetDateTime.class), any(Pageable.class));
        verify(eventTracker).jobCompletedBulkEvent(
            argThat(config -> ExpiredDraftStoreScheduler.SCHEDULER_NAME.equals(config.getSchedulerName())),
            eq(3)
        );
    }

    @Test
    void shouldNoOpWhenNothingIsExpired() {
        when(featureToggleService.isSpringSchedulerEnabled(ExpiredDraftStoreScheduler.SCHEDULER_NAME))
            .thenReturn(true);
        when(expiredDraftStoreTask.deleteExpiredBatch(any(OffsetDateTime.class), any(Pageable.class)))
            .thenReturn(0);

        assertThatCode(() -> scheduler.runScheduledTask()).doesNotThrowAnyException();

        verify(expiredDraftStoreTask).deleteExpiredBatch(any(OffsetDateTime.class), any(Pageable.class));
        verify(eventTracker).jobCompletedBulkEvent(
            argThat(config -> ExpiredDraftStoreScheduler.SCHEDULER_NAME.equals(config.getSchedulerName())),
            eq(0)
        );
    }

    @Test
    void shouldTrackAbortedEventWhenTaskThrows() {
        when(featureToggleService.isSpringSchedulerEnabled(ExpiredDraftStoreScheduler.SCHEDULER_NAME))
            .thenReturn(true);

        String errorMessage = "Database connection error during purge";
        doThrow(new RuntimeException(errorMessage))
            .when(expiredDraftStoreTask).deleteExpiredBatch(any(OffsetDateTime.class), any(Pageable.class));

        assertThatThrownBy(() -> scheduler.runScheduledTask())
            .isInstanceOf(RuntimeException.class)
            .hasMessage(errorMessage);

        verify(eventTracker).jobStartedEvent(argThat(config ->
                                                         ExpiredDraftStoreScheduler.SCHEDULER_NAME.equals(config.getSchedulerName())
        ));
        verify(eventTracker).jobAbortedEvent(
            argThat(config -> ExpiredDraftStoreScheduler.SCHEDULER_NAME.equals(config.getSchedulerName())),
            eq(errorMessage)
        );
    }
}
