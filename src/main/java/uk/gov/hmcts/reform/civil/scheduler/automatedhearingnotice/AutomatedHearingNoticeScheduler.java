package uk.gov.hmcts.reform.civil.scheduler.automatedhearingnotice;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.config.SystemUpdateUserConfiguration;
import uk.gov.hmcts.reform.civil.scheduler.common.CivilScheduler;
import uk.gov.hmcts.reform.civil.scheduler.common.DefaultBackPressureConfiguration;
import uk.gov.hmcts.reform.civil.scheduler.common.ScheduledEventTracker;
import uk.gov.hmcts.reform.civil.scheduler.common.ScheduledTaskBackPressure;
import uk.gov.hmcts.reform.civil.scheduler.common.ScheduledTaskEventConfiguration;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.hmc.model.unnotifiedhearings.UnNotifiedHearingResponse;
import uk.gov.hmcts.reform.hmc.service.HearingsService;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(prefix = "scheduler.automated-hearing-notice", name = "enabled", havingValue = "true")
public class AutomatedHearingNoticeScheduler implements CivilScheduler {

    public static final String SCHEDULER_NAME = "AutomatedHearingNotice";

    private final UserService userService;
    private final SystemUpdateUserConfiguration userConfig;
    private final HearingsService hearingsService;
    private final AutomatedHearingNoticeScheduledTask scheduledTask;
    private final ScheduledEventTracker eventTracker;
    private final FeatureToggleService featureToggleService;
    private final DefaultBackPressureConfiguration defaultBackPressureConfiguration;

    @Value("${scheduler.automated-hearing-notice.serviceIds}")
    private List<String> serviceIds;

    @Value("${scheduler.circuitBreakerThreshold:5}")
    private int circuitBreakerThreshold;

    @Override
    public String getName() {
        return SCHEDULER_NAME;
    }

    @Scheduled(cron = "${scheduler.automated-hearing-notice.cronExpression}")
    @SchedulerLock(name = "AutomatedHearingNoticeScheduler_sendHearingNotices",
        lockAtMostFor = "${scheduler.lockAtMostFor}",
        lockAtLeastFor = "${scheduler.lockAtLeastFor}")
    @Override
    public void runScheduledTask() {
        if (!featureToggleService.isSpringSchedulerEnabled(SCHEDULER_NAME)) {
            return;
        }

        log.info("Running {} scheduler", SCHEDULER_NAME);
        ScheduledTaskEventConfiguration eventConfig = new ScheduledTaskEventConfiguration(SCHEDULER_NAME);
        List<UnnotifiedHearingsForService> unnotifiedHearings = getUnnotifiedHearings();
        int totalHearings = unnotifiedHearings.stream()
            .mapToInt(serviceHearings -> Math.toIntExact(serviceHearings.totalFound()))
            .sum();

        if (totalHearings == 0) {
            eventTracker.jobStartedEvent(eventConfig, 0);
            eventTracker.jobCompletedNoCasesEvent(eventConfig);
            log.info("{} scheduler completed with no unnotified hearings", SCHEDULER_NAME);
            return;
        }

        processUnnotifiedHearings(eventConfig, unnotifiedHearings, totalHearings);
    }

    private List<UnnotifiedHearingsForService> getUnnotifiedHearings() {
        String userToken = userService.getAccessToken(userConfig.getUserName(), userConfig.getPassword());
        List<UnnotifiedHearingsForService> hearingsForServices = new ArrayList<>();

        for (String serviceId : serviceIds) {
            UnNotifiedHearingResponse response = hearingsService.getUnNotifiedHearingResponses(
                userToken,
                serviceId,
                LocalDateTime.now(ZoneId.systemDefault()).minusDays(7),
                null
            );
            List<String> hearingIds = response.getHearingIds() == null ? List.of() : response.getHearingIds();
            hearingsForServices.add(new UnnotifiedHearingsForService(serviceId, hearingIds, response.getTotalFound()));
            log.info(
                "{} scheduler found {} dispatched unnotified hearing(s) for serviceId {} with ids {}",
                SCHEDULER_NAME,
                response.getTotalFound(),
                serviceId,
                hearingIds
            );
        }

        return hearingsForServices;
    }

    private void processUnnotifiedHearings(ScheduledTaskEventConfiguration eventConfig,
                                           List<UnnotifiedHearingsForService> unnotifiedHearings,
                                           int totalHearings) {
        eventTracker.jobStartedEvent(eventConfig, totalHearings);

        int succeededHearings = 0;
        int failedHearings = 0;
        int consecutiveFailures = 0;
        String abortReason = null;
        ScheduledTaskBackPressure backPressure = new ScheduledTaskBackPressure(
            defaultBackPressureConfiguration.getDefaultBackPressure()
        );

        for (UnnotifiedHearingsForService serviceHearings : unnotifiedHearings) {
            for (String hearingId : serviceHearings.hearingIds()) {
                applyBackPressure(backPressure);
                Instant startedAt = Instant.now();
                try {
                    scheduledTask.accept(hearingId);
                    backPressure.afterSuccess(Duration.between(startedAt, Instant.now()));
                    eventTracker.caseProcessedEvent(eventConfig, hearingId);
                    succeededHearings++;
                    consecutiveFailures = 0;
                } catch (Exception e) {
                    backPressure.afterFailure();
                    eventTracker.caseFailedEvent(eventConfig, hearingId, e);
                    failedHearings++;
                    consecutiveFailures++;
                    abortReason = e.getMessage();
                    log.error("Error processing hearing {}: {}", hearingId, e.getMessage(), e);

                    if (consecutiveFailures >= circuitBreakerThreshold) {
                        eventTracker.jobAbortedEvent(
                            eventConfig,
                            totalHearings,
                            succeededHearings,
                            failedHearings,
                            abortReason
                        );
                        return;
                    }
                }
            }
        }

        eventTracker.jobCompletedEvent(eventConfig, totalHearings, succeededHearings, failedHearings);
        log.info(
            "{} scheduler completed, totalHearings: {}, succeededHearings: {}, failedHearings: {}",
            SCHEDULER_NAME,
            totalHearings,
            succeededHearings,
            failedHearings
        );
    }

    private record UnnotifiedHearingsForService(String serviceId, List<String> hearingIds, long totalFound) {
    }

    private void applyBackPressure(ScheduledTaskBackPressure backPressure) {
        Duration delay = backPressure.currentDelay();
        if (delay.isZero()) {
            return;
        }

        try {
            log.debug("Applying scheduled task backpressure delay: {}", delay);
            Thread.sleep(delay.toMillis());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Scheduled task interrupted while applying backpressure", e);
        }
    }
}
