package uk.gov.hmcts.reform.civil.scheduler.common;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.search.common.ElasticSearchResult;

import java.util.function.Supplier;

@Component
@RequiredArgsConstructor
@Slf4j
public class ElasticSearchSchedulerRunner {

    private final ScheduledTaskRunner scheduledTaskRunner;
    private final FeatureToggleService featureToggleService;

    public void run(String schedulerName,
                    Supplier<ElasticSearchResult> searchResultSupplier,
                    ScheduledTask scheduledTask) {
        if (featureToggleService.isSpringSchedulerEnabled(schedulerName)) {
            log.info("Running {} scheduler", schedulerName);
            ElasticSearchResult searchResult = searchResultSupplier.get();
            scheduledTaskRunner.run(
                new ScheduledTaskEventConfiguration(schedulerName),
                searchResult,
                scheduledTask
            );
        }
    }
}
