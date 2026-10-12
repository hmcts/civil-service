package uk.gov.hmcts.reform.civil.scheduler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import uk.gov.hmcts.reform.civil.Application;
import uk.gov.hmcts.reform.civil.config.TestIdamConfiguration;
import uk.gov.hmcts.reform.civil.scheduler.casedismissed.CaseDismissedScheduler;
import uk.gov.hmcts.reform.civil.scheduler.common.SetTaskResult;
import uk.gov.hmcts.reform.civil.scheduler.hearingfee.HearingFeeScheduler;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.TelemetryService;
import uk.gov.hmcts.reform.civil.service.search.CaseDismissedSearchService;
import uk.gov.hmcts.reform.civil.service.search.common.ElasticSearchResult;
import uk.gov.hmcts.reform.civil.service.search.hearingfee.HearingFeeDuePaginatedSearchService;
import uk.gov.hmcts.test.config.CoreCaseDataApiMockHelperConfiguration;
import uk.gov.hmcts.test.helper.CoreCaseDataApiMockHelper;

import java.time.Instant;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

/**
 * Runs two real schedulers ({@link HearingFeeScheduler} and {@link CaseDismissedScheduler}) through the
 * application's {@link ThreadPoolTaskScheduler} with the same wiring that a {@code @Scheduled} trigger uses
 * (ShedLock proxy, {@code ScheduledTaskRunner}, interceptors).
 *
 * <p>Both search services are mocked and wait at a shared {@link CyclicBarrier}. The barrier can only be
 * passed if both schedulers are inside their search at the same moment, so the test fails if the pool
 * reverts to a single thread, or if the schedulers share state that serialises them.</p>
 */
@ActiveProfiles("integration-test")
@SpringBootTest(classes = {Application.class, TestIdamConfiguration.class, CoreCaseDataApiMockHelperConfiguration.class},
    properties = {
        "test.id=RealSchedulersConcurrentExecutionIT",
        "scheduler.lockAtLeastFor=PT0S",
        "spring.task.scheduling.pool.size=2"
    })
class RealSchedulersConcurrentExecutionIT {

    private static final String THREAD_NAME_PREFIX = "civil-scheduler-";
    private static final int CONCURRENT_SCHEDULERS = 2;
    private static final int TIMEOUT_SECONDS = 20;

    @Autowired
    private ThreadPoolTaskScheduler taskScheduler;
    @Autowired
    private HearingFeeScheduler hearingFeeScheduler;
    @Autowired
    private CaseDismissedScheduler caseDismissedScheduler;
    @Autowired
    private CoreCaseDataApiMockHelper coreCaseDataApiMockHelper;

    @MockitoBean
    private HearingFeeDuePaginatedSearchService hearingFeeSearchService;
    @MockitoBean
    private CaseDismissedSearchService caseDismissedSearchService;
    @MockitoBean
    private FeatureToggleService featureToggleService;
    @MockitoBean
    private TelemetryService telemetryService;

    private final CyclicBarrier searchBarrier = new CyclicBarrier(CONCURRENT_SCHEDULERS);
    private final Set<String> searchThreadNames = ConcurrentHashMap.newKeySet();
    private final AtomicInteger searchesPastBarrier = new AtomicInteger();
    private final CountDownLatch schedulersFinished = new CountDownLatch(CONCURRENT_SCHEDULERS);

    @BeforeEach
    void setUp() {
        reset(featureToggleService, telemetryService);
        coreCaseDataApiMockHelper.resetMocks();
        coreCaseDataApiMockHelper.setupIdamClient();
        when(featureToggleService.isSpringSchedulerEnabled(any())).thenReturn(true);

        when(hearingFeeSearchService.getElasticSearchResult()).thenAnswer(invocation -> {
            awaitOtherScheduler();
            return new ElasticSearchResult(Stream.empty(), 0);
        });
        when(caseDismissedSearchService.getElasticSearchResult()).thenAnswer(invocation -> {
            awaitOtherScheduler();
            return new SetTaskResult<>(Set.of());
        });
    }

    @Test
    void shouldRunDifferentSchedulersConcurrentlyOnSchedulerPoolThreads() throws InterruptedException {
        // When both schedulers are triggered at the same instant
        taskScheduler.schedule(() -> runAndSignal(hearingFeeScheduler::runScheduledTask), Instant.now());
        taskScheduler.schedule(() -> runAndSignal(caseDismissedScheduler::runScheduledTask), Instant.now());

        // Then both searches were in progress at the same time, which is only possible with parallel threads
        boolean bothFinished = schedulersFinished.await(TIMEOUT_SECONDS, TimeUnit.SECONDS);

        assertThat(bothFinished)
            .as("both schedulers should have run together and passed the barrier")
            .isTrue();
        assertThat(searchesPastBarrier.get()).isEqualTo(CONCURRENT_SCHEDULERS);
        assertThat(searchThreadNames)
            .hasSize(CONCURRENT_SCHEDULERS)
            .allMatch(name -> name.startsWith(THREAD_NAME_PREFIX));
    }

    private void runAndSignal(Runnable scheduler) {
        try {
            scheduler.run();
        } finally {
            schedulersFinished.countDown();
        }
    }

    private void awaitOtherScheduler() throws Exception {
        searchThreadNames.add(Thread.currentThread().getName());
        searchBarrier.await(TIMEOUT_SECONDS / 2, TimeUnit.SECONDS);
        searchesPastBarrier.incrementAndGet();
    }
}
