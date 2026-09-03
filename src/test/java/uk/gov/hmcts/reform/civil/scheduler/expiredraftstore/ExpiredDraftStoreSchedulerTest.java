package uk.gov.hmcts.reform.civil.scheduler.expiredraftstore;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.draftstore.repositories.DraftStoreRepository;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExpiredDraftStoreSchedulerTest {

    @Mock
    private FeatureToggleService featureToggleService;

    @Mock
    private DraftStoreRepository draftStoreRepository;

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
}
