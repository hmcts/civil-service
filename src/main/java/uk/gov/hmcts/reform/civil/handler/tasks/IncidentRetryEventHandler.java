package uk.gov.hmcts.reform.civil.handler.tasks;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.community.rest.client.model.IncidentDto;
import org.camunda.community.rest.client.model.ProcessInstanceDto;
import org.camunda.community.rest.client.model.VariableValueDto;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.civil.model.ExternalTaskData;
import uk.gov.hmcts.reform.civil.service.camunda.CamundaRuntimeApi;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

@Slf4j
@Component
@RequiredArgsConstructor
public class IncidentRetryEventHandler extends BaseExternalTaskHandler {

    private final CamundaRuntimeApi camundaRuntimeApi;
    private final AuthTokenGenerator authTokenGenerator;

    private static final int MAX_THREADS = 50;
    private static final String CASE_ID_VARIABLE = "caseId";
    private static final String RETRIES_FIELD = "retries";
    private static final int RETRIES_COUNT = 1;
    private static final int PAGE_SIZE = 50;

    @Override
    public ExternalTaskData handleTask(ExternalTask externalTask) {
        String incidentStartTime = resolveStartTime(externalTask.getVariable("incidentStartTime"));
        String incidentEndTime = resolveEndTime(externalTask.getVariable("incidentEndTime"));
        String incidentMessageLike = externalTask.getVariable("incidentMessageLike");

        log.info("Incident retry process using date range {} → {}", incidentStartTime, incidentEndTime);

        String serviceAuthorization = authTokenGenerator.generate();
        AtomicInteger totalRetries = new AtomicInteger();
        AtomicInteger successRetries = new AtomicInteger();
        AtomicInteger failedRetries = new AtomicInteger();

        processAllIncidents(
            serviceAuthorization,
            incidentStartTime,
            incidentEndTime,
            incidentMessageLike,
            totalRetries,
            successRetries,
            failedRetries
        );

        log.info(
            "Incident retry completed. Total={}, Success={}, Failed={}",
            totalRetries.get(), successRetries.get(), failedRetries.get()
        );

        return ExternalTaskData.builder().build();
    }

    private void processAllIncidents(
        String serviceAuthorization,
        String incidentStartTime,
        String incidentEndTime,
        String incidentMessageLike,
        AtomicInteger totalRetries,
        AtomicInteger successRetries,
        AtomicInteger failedRetries
    ) {
        int firstResult = 0;
        List<ProcessInstanceDto> processInstancesBatch;

        do {
            processInstancesBatch = fetchProcessInstances(
                serviceAuthorization, incidentStartTime, incidentEndTime, incidentMessageLike, firstResult
            );

            if (processInstancesBatch.isEmpty()) {
                return;
            }

            List<String> processInstanceIds = processInstancesBatch.stream()
                .map(ProcessInstanceDto::getId)
                .toList();

            List<IncidentDto> incidents = getOpenIncidentsBatched(serviceAuthorization, processInstanceIds);

            if (!incidents.isEmpty()) {
                retryIncidents(incidents, serviceAuthorization, totalRetries, successRetries, failedRetries);
            }

            firstResult += PAGE_SIZE;
        } while (processInstancesBatch.size() == PAGE_SIZE);
    }

    private void retryIncidents(
        List<IncidentDto> incidents,
        String serviceAuthorization,
        AtomicInteger totalRetries,
        AtomicInteger successRetries,
        AtomicInteger failedRetries
    ) {
        log.info("Retrying {} incidents across process instances", incidents.size());
        int poolSize = Math.min(MAX_THREADS, incidents.size());
        ForkJoinPool customThreadPool = new ForkJoinPool(poolSize);

        try {
            customThreadPool.submit(() ->
                                        incidents.parallelStream().forEach(incident ->
                                                                               handleIncidentRetry(incident, serviceAuthorization, totalRetries, successRetries, failedRetries))
            ).get();
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            log.error("Incident retry execution was interrupted", ie);
        } catch (ExecutionException ee) {
            log.error("Error during parallel incident retries", ee.getCause());
        } finally {
            customThreadPool.shutdown();
        }
    }

    private void handleIncidentRetry(
        IncidentDto incident,
        String serviceAuthorization,
        AtomicInteger totalRetries,
        AtomicInteger successRetries,
        AtomicInteger failedRetries
    ) {
        totalRetries.incrementAndGet();
        try {
            if (retryIncidentSafely(incident, serviceAuthorization)) {
                successRetries.incrementAndGet();
                log.info("Successfully retried incident {}: {}", incident.getId(), incident.getProcessInstanceId());
            } else {
                failedRetries.incrementAndGet();
            }
        } catch (Exception e) {
            failedRetries.incrementAndGet();
            log.error("Unexpected error retrying incident {}: {}", incident.getId(), e.getMessage(), e);
        }
    }

    private String resolveStartTime(String incidentStartTime) {
        return (incidentStartTime == null || incidentStartTime.isBlank())
            ? DateTimeFormatter.ISO_INSTANT.format(Instant.now().minus(24, ChronoUnit.HOURS))
            : incidentStartTime;
    }

    private String resolveEndTime(String incidentEndTime) {
        return (incidentEndTime == null || incidentEndTime.isBlank())
            ? DateTimeFormatter.ISO_INSTANT.format(Instant.now())
            : incidentEndTime;
    }

    private List<ProcessInstanceDto> fetchProcessInstances(String serviceAuthorization,
                                                           String incidentStartTime,
                                                           String incidentEndTime,
                                                           String incidentMessageLike,
                                                           int firstResult) {
        try {
            return camundaRuntimeApi.getUnfinishedProcessInstancesWithIncidents(
                serviceAuthorization,
                true,       // unfinished
                true,       // withIncident
                incidentMessageLike,
                incidentStartTime,
                incidentEndTime,
                firstResult,
                IncidentRetryEventHandler.PAGE_SIZE,
                "startTime",
                "desc"
            );
        } catch (Exception e) {
            log.error("Error fetching unfinished process instances", e);
            return Collections.emptyList();
        }
    }

    private boolean retryIncidentSafely(IncidentDto incident, String serviceAuthorization) {
        try {
            String jobId = incident.getConfiguration();
            String processInstanceId = incident.getProcessInstanceId();
            String incidentCaseId = fetchCaseId(processInstanceId, serviceAuthorization);

            log.info(
                "Retrying incident {} for processInstanceId={} (jobId={}, caseId={}, activityId={})",
                incident.getId(), processInstanceId, jobId, incidentCaseId, incident.getActivityId()
            );

            camundaRuntimeApi.setJobRetries(serviceAuthorization, jobId, Map.of(RETRIES_FIELD, RETRIES_COUNT));

            log.info(
                "Retries reset for job {} (processInstanceId={}, caseId={})",
                jobId,
                processInstanceId,
                incidentCaseId
            );
            return true;

        } catch (Exception e) {
            log.error(
                "Error retrying incident {} (processInstanceId={}): {}",
                incident.getId(),
                incident.getProcessInstanceId(),
                e.getMessage(),
                e
            );
            return false;
        }
    }

    private String fetchCaseId(String processInstanceId, String serviceAuthorization) {
        try {
            Map<String, VariableValueDto> variables = camundaRuntimeApi.getProcessVariables(
                processInstanceId,
                serviceAuthorization
            );
            if (variables.containsKey(CASE_ID_VARIABLE) && variables.get(CASE_ID_VARIABLE).getValue() != null) {
                return String.valueOf(variables.get(CASE_ID_VARIABLE).getValue());
            }
        } catch (Exception e) {
            log.warn("Could not fetch caseId for processInstanceId={}", processInstanceId, e);
        }
        return "UNKNOWN";
    }

    private List<IncidentDto> getOpenIncidentsBatched(String serviceAuthorization, List<String> processInstanceIds) {
        int batchSize = 10; // safe for URL length

        return IntStream.range(0, (processInstanceIds.size() + batchSize - 1) / batchSize)
            .mapToObj(batchIndex -> {
                int start = batchIndex * batchSize;
                int end = Math.min(start + batchSize, processInstanceIds.size());
                List<String> batch = processInstanceIds.subList(start, end);
                String idsParam = String.join(",", batch);
                return camundaRuntimeApi.getOpenIncidents(serviceAuthorization, true, idsParam);
            })
            .flatMap(List::stream)
            .toList();
    }
}
