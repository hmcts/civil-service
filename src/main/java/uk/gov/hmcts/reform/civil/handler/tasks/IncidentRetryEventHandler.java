package uk.gov.hmcts.reform.civil.handler.tasks;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.community.rest.client.model.IncidentDto;
import org.camunda.community.rest.client.model.ProcessInstanceDto;
import org.camunda.community.rest.client.model.VariableValueDto;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.civil.model.ExternalTaskData;
import uk.gov.hmcts.reform.civil.service.camunda.CamundaRuntimeApi;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Component
@RequiredArgsConstructor
public class IncidentRetryEventHandler extends BaseExternalTaskHandler {

    private final CamundaRuntimeApi camundaRuntimeApi;
    private final AuthTokenGenerator authTokenGenerator;

    private static final int MAX_THREADS = 10;
    private static final String CASE_ID_VARIABLE = "caseId";
    private static final int PAGE_SIZE = 50;
    private static final DateTimeFormatter INCIDENT_FORMATTER =
        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
            .withZone(ZoneOffset.UTC);

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
        Set<String> caseIds = ConcurrentHashMap.newKeySet();

        processAllIncidents(
            serviceAuthorization,
            incidentStartTime,
            incidentEndTime,
            incidentMessageLike,
            totalRetries,
            successRetries,
            failedRetries,
            caseIds
        );

        log.info(
            "Incident retry completed. Total={}, Success={}, Failed={}",
            totalRetries.get(), successRetries.get(), failedRetries.get()
        );

        log.info(
            "All caseIds retried between incidents startTime={} and endTime={}: {}",
            incidentStartTime,
            incidentEndTime,
            caseIds
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
        AtomicInteger failedRetries,
        Set<String> caseIds
    ) {
        int firstResult = 0;
        List<ProcessInstanceDto> processInstancesBatch;

        do {
            log.info("Calling process instances for {}, {}, {}, {}",
                     incidentStartTime, incidentEndTime, incidentMessageLike, firstResult);
            processInstancesBatch = fetchProcessInstances(
                serviceAuthorization, incidentStartTime, incidentEndTime, incidentMessageLike, firstResult
            );

            log.info("Extracted process instances for {}, {}, {}, {}",
                     incidentStartTime, incidentEndTime, incidentMessageLike, firstResult);

            if (processInstancesBatch.isEmpty()) {
                return;
            }

            List<String> processInstanceIds = processInstancesBatch.stream()
                .map(ProcessInstanceDto::getId)
                .toList();

            log.info("Calling incidents for {} process instances with firstResult {}", processInstanceIds.size(), firstResult);

            List<IncidentDto> incidents = getLatestIncidentsForProcessInstances(serviceAuthorization, processInstanceIds);

            log.info("Extracted {} incidents with firstResult {}", incidents.size(), firstResult);

            if (!incidents.isEmpty()) {
                retryIncidents(incidents, serviceAuthorization, totalRetries, successRetries, failedRetries, caseIds);
            }

            firstResult += PAGE_SIZE;
        } while (processInstancesBatch.size() == PAGE_SIZE);
    }

    private void retryIncidents(
        List<IncidentDto> incidents,
        String serviceAuthorization,
        AtomicInteger totalRetries,
        AtomicInteger successRetries,
        AtomicInteger failedRetries,
        Set<String> caseIds

    ) {
        log.info("Retrying {} incidents across process instances", incidents.size());
        int poolSize = Math.min(MAX_THREADS, incidents.size());
        ForkJoinPool customThreadPool = new ForkJoinPool(poolSize);

        try {
            customThreadPool.submit(() ->
                                        incidents.parallelStream().forEach(incident ->
                                                                               handleIncidentRetry(incident,
                                                                                                   serviceAuthorization,
                                                                                                   totalRetries,
                                                                                                   successRetries,
                                                                                                   failedRetries,
                                                                                                   caseIds))
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
        AtomicInteger failedRetries,
        Set<String> caseIds
    ) {
        totalRetries.incrementAndGet();
        try {
            log.info("HandleIncidentRetry: calling retryIncidentSafely {}", incident.getId());
            if (retryIncidentSafely(incident, serviceAuthorization, caseIds)) {
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
            ? INCIDENT_FORMATTER.format(Instant.now().minus(23, ChronoUnit.HOURS))
            : incidentStartTime;
    }

    private String resolveEndTime(String incidentEndTime) {
        return (incidentEndTime == null || incidentEndTime.isBlank())
            ? INCIDENT_FORMATTER.format(Instant.now())
            : incidentEndTime;
    }

    private List<ProcessInstanceDto> fetchProcessInstances(String serviceAuthorization,
                                                           String incidentStartTime,
                                                           String incidentEndTime,
                                                           String incidentMessageLike,
                                                           int firstResult) {
        try {
            Map<String, Object> filters = new HashMap<>();
            filters.put("withIncidents", true);
            filters.put("unfinished", true);
            filters.put("executedActivityAfter", incidentStartTime);
            filters.put("executedActivityBefore", incidentEndTime); //"executedActivityBefore": "2025-09-17T16:20:05.471+0000",
            filters.put("tenantIdIn", List.of("civil"));
            if (StringUtils.isNotEmpty(incidentMessageLike)) {
                filters.put("incidentMessageLike", incidentMessageLike);
            }
            return camundaRuntimeApi.queryProcessInstances(
                serviceAuthorization,
                firstResult,
                IncidentRetryEventHandler.PAGE_SIZE,
                "instanceId",
                "desc",
                filters
            );
        } catch (Exception e) {
            log.error("Error fetching unfinished process instances", e);
            return Collections.emptyList();
        }
    }

    private boolean retryIncidentSafely(IncidentDto incident, String serviceAuthorization, Set<String> caseIds) {
        try {
            String processInstanceId = incident.getProcessInstanceId();
            String failedActivityId = incident.getActivityId();
            String incidentCaseId = fetchCaseId(processInstanceId, serviceAuthorization);
            caseIds.add(incidentCaseId);

            String jobId = incident.getConfiguration();

            log.info(
                "Retrying incident {} for processInstanceId={} (jobId={}, caseId={}, activityId={})",
                incident.getId(), processInstanceId, jobId, incidentCaseId, failedActivityId
            );

            retryProcessInstance(processInstanceId, serviceAuthorization, failedActivityId);

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

    private void retryProcessInstance(String processInstanceId, String serviceAuthorization, String failedActivityId) {
        Map<String, Object> modificationRequest = new HashMap<>();
        modificationRequest.put("skipCustomListeners", true);
        modificationRequest.put("skipIoMappings", false);

        List<Map<String, Object>> instructions = new ArrayList<>();
        Map<String, Object> startBeforeInstruction = new HashMap<>();
        startBeforeInstruction.put("type", "startBeforeActivity");
        startBeforeInstruction.put("activityId", failedActivityId);
        instructions.add(startBeforeInstruction);

        modificationRequest.put("instructions", instructions);

        try {
            camundaRuntimeApi.modifyProcessInstance(
                serviceAuthorization,
                processInstanceId,
                modificationRequest
            );
            log.info("Process instance {} successfully modified to retry activity {}", processInstanceId, failedActivityId);
        } catch (Exception e) {
            log.error("Failed to retry activity {} for processInstanceId={}: {}", failedActivityId, processInstanceId, e.getMessage(), e);
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

    private List<IncidentDto> getLatestIncidentsForProcessInstances(
        String serviceAuthorization,
        List<String> processInstanceIds
    ) {
        return processInstanceIds.stream()
            .map(processInstanceId -> {
                try {
                    // Call the API for a single process instance
                    List<IncidentDto> incidents = camundaRuntimeApi.getLatestOpenIncidentForProcessInstance(
                        serviceAuthorization,
                        true,                   // open incidents only
                        processInstanceId,       // single process instance
                        "incidentTimestamp",     // use default sortBy
                        "desc",                  // use default sortOrder
                        1                        // use default maxResults
                    );
                    IncidentDto incidentDto = incidents.isEmpty() ? null : incidents.get(0);
                    if (incidentDto != null) {
                        log.info("Fetched incident {} for process instance {}", incidentDto.getId(), processInstanceId);
                    } else {
                        log.info("No incidents found for process instance {}", processInstanceId);
                    }
                    return incidentDto;
                } catch (Exception e) {
                    log.error("Error fetching latest incident for process instance {}", processInstanceId, e);
                    return null;
                }
            })
            .filter(Objects::nonNull)
            .toList();
    }
}
