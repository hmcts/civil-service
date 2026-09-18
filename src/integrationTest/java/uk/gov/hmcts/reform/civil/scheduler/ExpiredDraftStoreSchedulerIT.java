package uk.gov.hmcts.reform.civil.scheduler;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.civil.Application;
import uk.gov.hmcts.reform.civil.config.TestIdamConfiguration;
import uk.gov.hmcts.reform.civil.scheduler.common.ScheduledEventTracker;
import uk.gov.hmcts.reform.civil.scheduler.expiredraftstore.ExpiredDraftStoreScheduler;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.draftstore.entities.DraftStoreEntity;
import uk.gov.hmcts.reform.draftstore.repositories.DraftStoreRepository;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ActiveProfiles("integration-test")
@SpringBootTest(classes = {Application.class, TestIdamConfiguration.class}, properties = {
    "test.id=ExpiredDraftStoreSchedulerIT",
    "scheduler.lockAtLeastFor=PT0S"
})
@Execution(ExecutionMode.SAME_THREAD)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class ExpiredDraftStoreSchedulerIT {

    @Autowired
    private ExpiredDraftStoreScheduler scheduler;

    @Autowired
    private DraftStoreRepository draftStoreRepository;

    @MockitoBean
    private ScheduledEventTracker eventTracker;

    @MockitoBean
    private FeatureToggleService featureToggleService;

    @BeforeEach
    @AfterEach
    void clearDatabase() {
        draftStoreRepository.deleteAll();
    }

    @Test
    void shouldPurgeExpiredDraftsAndKeepFutureDrafts() {
        when(featureToggleService.isSpringSchedulerEnabled(ExpiredDraftStoreScheduler.SCHEDULER_NAME))
            .thenReturn(true);

        OffsetDateTime now = OffsetDateTime.now();

        DraftStoreEntity expiredDraft = new DraftStoreEntity(
            UUID.randomUUID(),
            "user-1",
            null,
            1,
            Map.of("data", "expired"),
            now.minusDays(3),
            now.minusDays(2),
            now.minusDays(1)
        );

        DraftStoreEntity futureDraft = new DraftStoreEntity(
            UUID.randomUUID(),
            "user-2",
            null,
            1,
            Map.of("data", "future"),
            now.minusDays(1),
            now,
            now.plusHours(1)
        );

        draftStoreRepository.save(expiredDraft);
        draftStoreRepository.save(futureDraft);

        scheduler.runScheduledTask();

        assertThat(draftStoreRepository.findById(expiredDraft.getId())).isEmpty();
        assertThat(draftStoreRepository.findById(futureDraft.getId())).isPresent();

        verify(eventTracker).jobStartedEvent(argThat(config ->
            ExpiredDraftStoreScheduler.SCHEDULER_NAME.equals(config.getSchedulerName())
        ));
        verify(eventTracker).jobCompletedBulkEvent(
            argThat(config -> ExpiredDraftStoreScheduler.SCHEDULER_NAME.equals(config.getSchedulerName())),
            eq(1)
        );
    }

    @Test
    void shouldNotDeleteWhenSchedulerIsDisabled() {
        when(featureToggleService.isSpringSchedulerEnabled(ExpiredDraftStoreScheduler.SCHEDULER_NAME))
            .thenReturn(false);

        OffsetDateTime now = OffsetDateTime.now();

        DraftStoreEntity expiredDraft = new DraftStoreEntity(
            UUID.randomUUID(),
            "user-4",
            null,
            1,
            Map.of("data", "expired"),
            now.minusDays(3),
            now.minusDays(2),
            now.minusDays(1)
        );

        draftStoreRepository.save(expiredDraft);

        scheduler.runScheduledTask();

        assertThat(draftStoreRepository.findById(expiredDraft.getId())).isPresent();
    }
}
