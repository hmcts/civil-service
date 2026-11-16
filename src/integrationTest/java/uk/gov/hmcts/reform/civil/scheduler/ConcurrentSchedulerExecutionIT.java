package uk.gov.hmcts.reform.civil.scheduler;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.task.TaskSchedulingAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.util.Set;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies that the {@code spring.task.scheduling.*} properties in application.yaml give us a pool of
 * threads so that independent {@code @Scheduled} jobs triggered at the same time run concurrently.
 *
 * <p>Only the Spring scheduling auto-configuration is loaded (no database, CCD or Camunda), so the real
 * application.yaml properties are exercised without the cost of the full application context.</p>
 */
@SpringBootTest(
    classes = ConcurrentSchedulerExecutionIT.TestSchedulingConfiguration.class,
    properties = "spring.task.scheduling.pool.size=3"
)
@ImportAutoConfiguration(TaskSchedulingAutoConfiguration.class)
class ConcurrentSchedulerExecutionIT {

    private static final String THREAD_NAME_PREFIX = "civil-scheduler-";
    private static final int CONCURRENT_JOBS = 3;
    private static final int TIMEOUT_SECONDS = 10;

    @Autowired
    private ThreadPoolTaskScheduler taskScheduler;

    @Autowired
    private ConcurrentJobs jobs;

    @Test
    void shouldConfigureTaskSchedulerFromProperties() {
        assertThat(taskScheduler.getThreadNamePrefix()).isEqualTo(THREAD_NAME_PREFIX);
        assertThat(taskScheduler.getScheduledThreadPoolExecutor().getCorePoolSize()).isEqualTo(CONCURRENT_JOBS);
    }

    @Test
    void shouldRunScheduledJobsConcurrentlyOnSchedulerPoolThreads() throws InterruptedException {
        // All jobs start at once and each waits at the barrier for the others.
        // With a single scheduler thread the barrier would time out and no job would complete.
        boolean allJobsFinished = jobs.allJobsFinished.await(TIMEOUT_SECONDS, TimeUnit.SECONDS);

        assertThat(allJobsFinished)
            .as("all jobs should have reached the barrier at the same time")
            .isTrue();
        assertThat(jobs.barrierFailures.get()).isZero();
        assertThat(jobs.threadNames)
            .hasSize(CONCURRENT_JOBS)
            .allMatch(name -> name.startsWith(THREAD_NAME_PREFIX));
    }

    @Configuration
    @EnableScheduling
    static class TestSchedulingConfiguration {

        @Bean
        ConcurrentJobs concurrentJobs() {
            return new ConcurrentJobs();
        }
    }

    static class ConcurrentJobs {

        private final CyclicBarrier barrier = new CyclicBarrier(CONCURRENT_JOBS);
        private final CountDownLatch allJobsFinished = new CountDownLatch(CONCURRENT_JOBS);
        private final Set<String> threadNames = ConcurrentHashMap.newKeySet();
        private final AtomicInteger barrierFailures = new AtomicInteger();

        @Scheduled(initialDelay = 0, fixedDelay = Long.MAX_VALUE)
        void jobOne() {
            run();
        }

        @Scheduled(initialDelay = 0, fixedDelay = Long.MAX_VALUE)
        void jobTwo() {
            run();
        }

        @Scheduled(initialDelay = 0, fixedDelay = Long.MAX_VALUE)
        void jobThree() {
            run();
        }

        private void run() {
            threadNames.add(Thread.currentThread().getName());
            try {
                barrier.await(TIMEOUT_SECONDS / 2, TimeUnit.SECONDS);
                allJobsFinished.countDown();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                barrierFailures.incrementAndGet();
            } catch (BrokenBarrierException | TimeoutException e) {
                barrierFailures.incrementAndGet();
            }
        }
    }
}
