package uk.gov.hmcts.reform.civil.notify.audit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

class RingBufferNotificationAuditServiceTest {

    private RingBufferNotificationAuditService service;

    @BeforeEach
    void setUp() {
        service = new RingBufferNotificationAuditService();
    }

    @Test
    void shouldRecordAndQueryEntryByReferenceSubstring() {
        service.record("template-1", "claimant@email.com", "received-template-001MC123", "notif-1");
        service.record("template-2", "defendant@email.com", "received-template-001MC999", "notif-2");

        List<NotificationAuditEntry> result = service.query("001MC123");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).templateId()).isEqualTo("template-1");
        assertThat(result.get(0).recipientEmail()).isEqualTo("claimant@email.com");
        assertThat(result.get(0).notificationId()).isEqualTo("notif-1");
        assertThat(result.get(0).sentAt()).isNotNull();
    }

    @Test
    void shouldMatchSubstringAtStartMiddleAndEndOfReference() {
        service.record("t", "a@email.com", "001MC123-prefix", "n1");
        service.record("t", "a@email.com", "prefix-001MC123-suffix", "n2");
        service.record("t", "a@email.com", "suffix-001MC123", "n3");
        service.record("t", "a@email.com", "001MC999", "n4");

        List<NotificationAuditEntry> result = service.query("001MC123");

        assertThat(result).extracting(NotificationAuditEntry::notificationId)
            .containsExactly("n1", "n2", "n3");
    }

    @Test
    void shouldBeCaseSensitiveOnSubstringMatch() {
        service.record("t", "a@email.com", "ref-001MC123", "n1");

        assertThat(service.query("001mc123")).isEmpty();
        assertThat(service.query("001MC123")).hasSize(1);
    }

    @Test
    void shouldReturnAllEntriesWhenReferenceFilterIsBlank() {
        service.record("t1", "a@email.com", "ref-1", "n1");
        service.record("t2", "b@email.com", "ref-2", "n2");

        assertThat(service.query(null)).hasSize(2);
        assertThat(service.query("")).hasSize(2);
        assertThat(service.query("   ")).hasSize(2);
    }

    @Test
    void shouldReturnEmptyListWhenNoEntriesMatch() {
        service.record("t1", "a@email.com", "ref-001MC111", "n1");

        assertThat(service.query("001MC999")).isEmpty();
    }

    @Test
    void shouldHoldExactlyMaxEntriesAtBoundary() {
        for (int i = 0; i < RingBufferNotificationAuditService.MAX_ENTRIES; i++) {
            service.record("t", "x@email.com", "ref-" + i, "n-" + i);
        }

        List<NotificationAuditEntry> all = service.query(null);
        assertThat(all).hasSize(RingBufferNotificationAuditService.MAX_ENTRIES);
        assertThat(all.get(0).reference()).isEqualTo("ref-0");
        assertThat(all.get(all.size() - 1).reference())
            .isEqualTo("ref-" + (RingBufferNotificationAuditService.MAX_ENTRIES - 1));
    }

    @Test
    void shouldEvictOldestEntriesWhenBufferIsFull() {
        for (int i = 0; i < RingBufferNotificationAuditService.MAX_ENTRIES + 5; i++) {
            service.record("t", "x@email.com", "ref-" + i, "n-" + i);
        }

        List<NotificationAuditEntry> all = service.query(null);
        assertThat(all).hasSize(RingBufferNotificationAuditService.MAX_ENTRIES);
        assertThat(all.get(0).reference()).isEqualTo("ref-5");
        assertThat(all.get(all.size() - 1).reference())
            .isEqualTo("ref-" + (RingBufferNotificationAuditService.MAX_ENTRIES + 4));
    }

    @Test
    void shouldClearAllEntries() {
        service.record("t1", "a@email.com", "ref-1", "n1");
        service.record("t2", "b@email.com", "ref-2", "n2");

        service.clear();

        assertThat(service.query(null)).isEmpty();
    }

    @Test
    void shouldHandleNullReferenceOnRecordWithoutThrowing() {
        service.record("t1", "a@email.com", null, "n1");

        assertThat(service.query("anything")).isEmpty();
        assertThat(service.query(null)).hasSize(1);
    }

    @Test
    void shouldHandleNullTemplateAndRecipientAndNotificationIdOnRecord() {
        service.record(null, null, "ref-1", null);

        List<NotificationAuditEntry> result = service.query("ref-1");
        assertThat(result).hasSize(1);
        assertThat(result.get(0).templateId()).isNull();
        assertThat(result.get(0).recipientEmail()).isNull();
        assertThat(result.get(0).notificationId()).isNull();
    }

    @Test
    void shouldNotThrowConcurrentModification_whenQueryAndRecordRunInParallel() throws Exception {
        int writers = 4;
        int recordsPerWriter = 500;
        ExecutorService pool = Executors.newFixedThreadPool(writers + 1);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(writers + 1);
        AtomicInteger queryFailures = new AtomicInteger();

        for (int w = 0; w < writers; w++) {
            int writerId = w;
            pool.submit(() -> {
                try {
                    start.await();
                    for (int i = 0; i < recordsPerWriter; i++) {
                        service.record("t", "x@email.com", "ref-" + writerId + "-" + i, "n-" + writerId + "-" + i);
                    }
                } catch (InterruptedException ignored) {
                    Thread.currentThread().interrupt();
                } finally {
                    done.countDown();
                }
            });
        }

        pool.submit(() -> {
            try {
                start.await();
                for (int i = 0; i < 200; i++) {
                    try {
                        service.query("ref-");
                    } catch (RuntimeException e) {
                        queryFailures.incrementAndGet();
                    }
                }
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            } finally {
                done.countDown();
            }
        });

        start.countDown();
        assertThat(done.await(10, TimeUnit.SECONDS)).isTrue();
        pool.shutdownNow();

        assertThat(queryFailures.get()).isZero();
        assertThat(service.query(null)).hasSize(writers * recordsPerWriter);
    }
}
