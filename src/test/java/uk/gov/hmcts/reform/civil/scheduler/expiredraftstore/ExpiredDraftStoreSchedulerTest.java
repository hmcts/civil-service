package uk.gov.hmcts.reform.civil.scheduler.expiredraftstore;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import uk.gov.hmcts.reform.civil.scheduler.common.ListTaskResult;
import uk.gov.hmcts.reform.civil.scheduler.common.ScheduledTaskConfiguration;
import uk.gov.hmcts.reform.civil.scheduler.common.ScheduledTaskRunner;
import uk.gov.hmcts.reform.civil.scheduler.common.TaskResult;
import uk.gov.hmcts.reform.draftstore.repositories.DraftStoreRepository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExpiredDraftStoreSchedulerTest {

    @Mock
    private DraftStoreRepository draftStoreRepository;

    @Mock
    private ExpiredDraftStoreTask expiredDraftStoreTask;

    @Mock
    private ScheduledTaskRunner<List<UUID>, String> scheduledTaskRunner;

    private ExpiredDraftStoreScheduler scheduler;

    @BeforeEach
    void setUp() {
        scheduler = new ExpiredDraftStoreScheduler(
            draftStoreRepository,
            expiredDraftStoreTask,
            scheduledTaskRunner,
            500,
            20
        );
    }

    @Test
    void shouldReturnSchedulerName() {
        assertThat(scheduler.getName()).isEqualTo(ExpiredDraftStoreScheduler.SCHEDULER_NAME);
    }

    @Test
    void shouldExecuteScheduledTaskRunnerWithConfiguration() {
        scheduler.runScheduledTask();

        @SuppressWarnings("unchecked")
        ArgumentCaptor<ScheduledTaskConfiguration<List<UUID>, String>> captor =
            ArgumentCaptor.forClass(ScheduledTaskConfiguration.class);

        verify(scheduledTaskRunner).run(captor.capture());

        ScheduledTaskConfiguration<List<UUID>, String> config = captor.getValue();
        assertThat(config.getSchedulerName()).isEqualTo(ExpiredDraftStoreScheduler.SCHEDULER_NAME);
        assertThat(config.getScheduledTask()).isEqualTo(expiredDraftStoreTask);
    }

    @Test
    void shouldFetchExpiredBatchesCorrectly() {
        List<UUID> batch1 = List.of(UUID.randomUUID(), UUID.randomUUID());
        when(draftStoreRepository.findExpiredIds(any(OffsetDateTime.class), any(Pageable.class)))
            .thenReturn(batch1)
            .thenReturn(List.of());

        scheduler.runScheduledTask();

        @SuppressWarnings("unchecked")
        ArgumentCaptor<ScheduledTaskConfiguration<List<UUID>, String>> captor =
            ArgumentCaptor.forClass(ScheduledTaskConfiguration.class);

        verify(scheduledTaskRunner).run(captor.capture());

        TaskResult<List<UUID>> result = captor.getValue().getSearchResultSupplier().get();

        ListTaskResult<List<UUID>> listResult = (ListTaskResult<List<UUID>>) result;

        assertThat(listResult.items()).hasSize(1);
        assertThat(listResult.items().getFirst()).containsExactlyElementsOf(batch1);
    }
}
