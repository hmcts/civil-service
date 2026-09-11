package uk.gov.hmcts.reform.civil.scheduler.expiredraftstore;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.scheduler.common.ScheduledEventTracker;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.draftstore.repositories.DraftStoreRepository;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExpiredDraftStoreSchedulerTest {

    @Mock
    private FeatureToggleService featureToggleService;

    @Mock
    private DraftStoreRepository draftStoreRepository;

    @Mock
    private ScheduledEventTracker eventTracker;

    @InjectMocks
    private ExpiredDraftStoreScheduler scheduler;

    @Test
    void shouldReturnSchedulerName() {
        assertThat(scheduler.getName()).isEqualTo(ExpiredDraftStoreScheduler.SCHEDULER_NAME);
    }

    @Test
    void shouldNotDeleteWhenSpringSchedulerIsDisabled() {
        when(featureToggleService.isSpringSchedulerEnabled(ExpiredDraftStoreScheduler.SCHEDULER_NAME))
            .thenReturn(false);

        scheduler.runScheduledTask();

        verifyNoInteractions(draftStoreRepository);
    }

    @Test
    void shouldDeleteExpiredDraftsWhenEnabled() {
        when(featureToggleService.isSpringSchedulerEnabled(ExpiredDraftStoreScheduler.SCHEDULER_NAME))
            .thenReturn(true);
        when(draftStoreRepository.deleteByExpiresAtBefore(any(OffsetDateTime.class))).thenReturn(3L);

        scheduler.runScheduledTask();

        verify(draftStoreRepository).deleteByExpiresAtBefore(any(OffsetDateTime.class));
    }

    @Test
    void shouldNoOpWhenNothingIsExpired() {
        when(featureToggleService.isSpringSchedulerEnabled(ExpiredDraftStoreScheduler.SCHEDULER_NAME))
            .thenReturn(true);
        when(draftStoreRepository.deleteByExpiresAtBefore(any(OffsetDateTime.class))).thenReturn(0L);

        assertThatCode(() -> scheduler.runScheduledTask()).doesNotThrowAnyException();

        verify(draftStoreRepository).deleteByExpiresAtBefore(any(OffsetDateTime.class));
    }

    @Test
    void shouldTrackAbortedEventWhenRepositoryThrows() {
        when(featureToggleService.isSpringSchedulerEnabled(ExpiredDraftStoreScheduler.SCHEDULER_NAME))
            .thenReturn(true);

        String errorMessage = "Database connection error during purge";
        doThrow(new RuntimeException(errorMessage))
            .when(draftStoreRepository).deleteByExpiresAtBefore(any(OffsetDateTime.class));

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
