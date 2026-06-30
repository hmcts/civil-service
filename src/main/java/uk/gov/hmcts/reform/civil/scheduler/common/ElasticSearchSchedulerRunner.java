package uk.gov.hmcts.reform.civil.scheduler.common;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.search.common.ElasticSearchResult;

import java.util.function.ObjIntConsumer;
import java.util.function.Supplier;

@Component
@RequiredArgsConstructor
@Slf4j
public class ElasticSearchSchedulerRunner {

    private final ScheduledTaskRunner scheduledTaskRunner;
    private final FeatureToggleService featureToggleService;

    public void run(String schedulerName,
                    Supplier<ElasticSearchResult> searchResultSupplier,
                    ObjIntConsumer<CaseDetails> scheduledTask) {
        if (featureToggleService.isSpringSchedulerEnabled(schedulerName)) {
            log.info("Running {} scheduler", schedulerName);
            ElasticSearchResult searchResult = searchResultSupplier.get();
            int totalCases = searchResult == null ? 0 : searchResult.totalResults();
            scheduledTaskRunner.run(
                new ScheduledTaskEventConfiguration(schedulerName),
                searchResult,
                caseDetails -> scheduledTask.accept(caseDetails, totalCases)
            );
        }
    }
}
