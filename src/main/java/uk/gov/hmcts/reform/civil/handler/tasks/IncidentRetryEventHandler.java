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
        String incidentStartTime = externalTask.getVariable("incidentStartTime");
        String incidentEndTime = externalTask.getVariable("incidentEndTime");

        DateTimeFormatter formatter = DateTimeFormatter.ISO_INSTANT;

        if (incidentStartTime == null || incidentStartTime.isBlank()) {
            incidentStartTime = formatter.format(Instant.now().minus(24, ChronoUnit.HOURS));
        }
        if (incidentEndTime == null || incidentEndTime.isBlank()) {
            incidentEndTime = formatter.format(Instant.now());
        }

        log.info("Incident retry process using date range {} → {}", incidentStartTime, incidentEndTime);

        String serviceAuthorization = authTokenGenerator.generate();
        int firstResult = 0;
        List<ProcessInstanceDto> processInstancesBatch;

        // Metrics counters
        AtomicInteger totalRetries = new AtomicInteger(0);
        AtomicInteger successRetries = new AtomicInteger(0);
        AtomicInteger failedRetries = new AtomicInteger(0);

        String incidentMessageLike = externalTask.getVariable("incidentMessageLike");
        do {
            processInstancesBatch = fetchProcessInstances(
                serviceAuthorization, incidentStartTime, incidentEndTime, incidentMessageLike,
                firstResult
            );

            if (processInstancesBatch.isEmpty()) {
                break;
            }

            List<String> processInstanceIds = processInstancesBatch.stream()
                .map(ProcessInstanceDto::getId)
                .toList();

            List<IncidentDto> incidents = getOpenIncidentsBatched(serviceAuthorization, processInstanceIds);

            if (!incidents.isEmpty()) {
                log.info(
                    "Retrying {} incidents across {} process instances",
                    incidents.size(),
                    processInstancesBatch.size()
                );

                int poolSize = Math.min(MAX_THREADS, incidents.size());
                ForkJoinPool customThreadPool = new ForkJoinPool(poolSize);

                try {
                    customThreadPool.submit(() ->
                                                incidents.parallelStream().forEach(incident -> {
                                                    totalRetries.incrementAndGet();
                                                    try {
                                                        boolean success = retryIncidentSafely(
                                                            incident,
                                                            serviceAuthorization
                                                        );
                                                        if (success) {
                                                            successRetries.incrementAndGet();
                                                            log.info("Successfully retried incident {}: {}",
                                                                     incident.getId(),
                                                                     incident.getProcessInstanceId());
                                                        } else {
                                                            failedRetries.incrementAndGet();
                                                        }
                                                    } catch (Exception e) {
                                                        failedRetries.incrementAndGet();
                                                        log.error(
                                                            "Unexpected error retrying incident {}: {}",
                                                            incident.getId(),
                                                            e.getMessage(),
                                                            e
                                                        );
                                                    }
                                                })
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

            firstResult += PAGE_SIZE;
        } while (processInstancesBatch.size() == PAGE_SIZE);

        log.info(
            "Incident retry completed. Total={}, Success={}, Failed={}",
            totalRetries.get(), successRetries.get(), failedRetries.get()
        );

        return ExternalTaskData.builder().build();
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
