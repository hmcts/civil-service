package uk.gov.hmcts.reform.civil.scheduler.hearingcvplink;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.scheduler.common.CivilScheduler;
import uk.gov.hmcts.reform.civil.scheduler.common.ScheduledTaskEventConfiguration;
import uk.gov.hmcts.reform.civil.scheduler.common.ScheduledTaskRunner;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.search.CaseHearingDateSearchService;
import uk.gov.hmcts.reform.civil.service.search.common.ElasticSearchResult;

@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(prefix = "scheduler.hearingCvpLink", name = "enabled", havingValue = "true")
public class HearingCvpLinkScheduler implements CivilScheduler {

    private static final String SCHEDULER_NAME = "HearingCvpLink";

    private final CaseHearingDateSearchService searchService;
    private final ScheduledTaskRunner scheduledTaskRunner;
    private final HearingCvpLinkScheduledTask hearingCvpLinkScheduledTask;
    private final FeatureToggleService featureToggleService;

    @Override
    public String getName() {
        return SCHEDULER_NAME;
    }

    @Scheduled(cron = "${scheduler.hearingCvpLink.cronExpression}")
    @SchedulerLock(name = "HearingCvpLinkScheduler_sendCvpJoinLinks",
        lockAtMostFor = "${scheduler.lockAtMostFor}",
        lockAtLeastFor = "${scheduler.lockAtLeastFor}")
    @Override
    public void runScheduledTask() {
        if (featureToggleService.isSpringSchedulerEnabled(SCHEDULER_NAME)) {
            log.info("Running {} scheduler", SCHEDULER_NAME);
            ElasticSearchResult searchResult = searchService.getElasticSearchResult();
            int totalCases = searchResult == null ? 0 : searchResult.totalResults();
            scheduledTaskRunner.run(
                new ScheduledTaskEventConfiguration(SCHEDULER_NAME),
                searchResult,
                caseDetails -> hearingCvpLinkScheduledTask.accept(caseDetails, totalCases)
            );
        }
    }
}
