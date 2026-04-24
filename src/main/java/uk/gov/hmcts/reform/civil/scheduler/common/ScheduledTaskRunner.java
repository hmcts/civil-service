package uk.gov.hmcts.reform.civil.scheduler.common;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.service.TelemetryService;
import uk.gov.hmcts.reform.civil.service.search.ElasticSearchService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@RequiredArgsConstructor
public class ScheduledTaskRunner {

    private final ErrorCategorizer errorCategorizer;
    private final TelemetryService telemetryService;

    @Value("${scheduler.circuitBreakerThreshold:5}")
    private int circuitBreakerThreshold;

    public void run(ScheduledTaskEventConfiguration eventConfig,
                    ElasticSearchService searchService,
                    ScheduledTask scheduledTask) {

        List<Long> failedCases = new ArrayList<>();
        int consecutiveFailures = 0;
        boolean abortedEarly = false;
        String abortReason = "";

        Set<CaseDetails> cases = searchService.getCases();
        jobStartedEvent(eventConfig, cases.size());

        for (CaseDetails caseDetails : cases) {
            try {
                scheduledTask.accept(caseDetails);
                caseProcessedEvent(eventConfig, caseDetails.getId());
                consecutiveFailures = 0;
            } catch (Exception e) {
                failedCases.add(caseDetails.getId());
                caseFailedEvent(eventConfig, caseDetails, e);
                consecutiveFailures++;

                if (consecutiveFailures >= circuitBreakerThreshold) {
                    abortedEarly = true;
                    abortReason = e.getMessage();
                    break;
                }
            }
        }

        if (abortedEarly) {
            jobAbortedEvent(eventConfig, cases, failedCases, abortReason);
        }
        jobCompletedEvent(eventConfig, cases, failedCases, abortedEarly);
    }

    private void jobStartedEvent(ScheduledTaskEventConfiguration eventConfig, int casesSize) {
        telemetryService.trackEvent(
            eventConfig.getJobStartedEvent(),
            Map.of(
                "schedulerName", eventConfig.getSchedulerName(),
                "totalCases", String.valueOf(casesSize)
            )
        );
    }

    private void caseProcessedEvent(ScheduledTaskEventConfiguration eventConfig, Long caseId) {
        telemetryService.trackEvent(
            eventConfig.getCaseProcessedEvent(),
            Map.of(
                "schedulerName", eventConfig.getSchedulerName(),
                "caseId", String.valueOf(caseId),
                "status", "SUCCESS"
            )
        );
    }

    private void caseFailedEvent(ScheduledTaskEventConfiguration eventConfig, CaseDetails caseDetails, Exception e) {
        telemetryService.trackEvent(
            eventConfig.getCaseFailedEvent(), Map.of(
                "schedulerName", eventConfig.getSchedulerName(),
                "caseId", String.valueOf(caseDetails.getId()),
                "status", "FAILURE",
                "error", e.getMessage(),
                "errorCategory", errorCategorizer.categorizeError(e)
            )
        );
    }

    private void jobCompletedEvent(ScheduledTaskEventConfiguration eventConfig,
                                   Set<CaseDetails> cases,
                                   List<Long> failedCases,
                                   boolean abortedEarly) {
        telemetryService.trackEvent(
            eventConfig.getJobCompletedEvent(),
            Map.of(
                "schedulerName", eventConfig.getSchedulerName(),
                "totalCases", String.valueOf(cases.size()),
                "succeededCases", String.valueOf(cases.size() - failedCases.size()),
                "failedCases", String.valueOf(failedCases.size()),
                "abortedEarly", String.valueOf(abortedEarly)
            )
        );
    }

    private void jobAbortedEvent(ScheduledTaskEventConfiguration eventConfig,
                                 Set<CaseDetails> cases,
                                 List<Long> failedCases,
                                 String reason) {
        telemetryService.trackEvent(
            eventConfig.getJobAbortedEvent(),
            Map.of(
                "schedulerName", eventConfig.getSchedulerName(),
                "totalCases", String.valueOf(cases.size()),
                "succeededCases", String.valueOf(cases.size() - failedCases.size()),
                "failedCases", String.valueOf(failedCases.size()),
                "abortReason", reason != null ? reason : "Unknown"
            )
        );
    }
}
