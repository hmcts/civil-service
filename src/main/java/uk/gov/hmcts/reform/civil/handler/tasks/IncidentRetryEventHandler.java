package uk.gov.hmcts.reform.civil.handler.tasks;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.client.task.ExternalTask;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.civil.config.properties.EventProperties;
import uk.gov.hmcts.reform.civil.model.ExternalTaskData;
import uk.gov.hmcts.reform.civil.model.camunda.CamundaIncident;
import uk.gov.hmcts.reform.civil.model.camunda.CamundaProcessInstance;
import uk.gov.hmcts.reform.civil.model.camunda.CamundaVariableValue;
import uk.gov.hmcts.reform.civil.service.CaseTaskTrackingService;
import uk.gov.hmcts.reform.civil.service.ExternalTaskCompletionService;
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
import java.util.regex.Pattern;

@Slf4j
@Component
public class IncidentRetryEventHandler extends BaseExternalTaskHandler {

    private static final class IncidentRetryContext {
        private final String serviceAuthorization;
        private final AtomicInteger totalRetries = new AtomicInteger();
        private final AtomicInteger successRetries = new AtomicInteger();
        private final AtomicInteger failedRetries = new AtomicInteger();
        private final Set<String> caseIds = ConcurrentHashMap.newKeySet();

        private IncidentRetryContext(String serviceAuthorization) {
            this.serviceAuthorization = serviceAuthorization;
        }
    }

    private final CamundaRuntimeApi camundaRuntimeApi;
    private final AuthTokenGenerator authTokenGenerator;
    private final CaseTaskTrackingService caseTaskTrackingService;

    public IncidentRetryEventHandler(
        ExternalTaskCompletionService externalTaskCompletionService,
        EventProperties eventProperties,
        CamundaRuntimeApi camundaRuntimeApi,
        AuthTokenGenerator authTokenGenerator,
        CaseTaskTrackingService caseTaskTrackingService
    ) {
        super(externalTaskCompletionService, eventProperties);
        this.camundaRuntimeApi = camundaRuntimeApi;
        this.authTokenGenerator = authTokenGenerator;
        this.caseTaskTrackingService = caseTaskTrackingService;
    }

    private static final int MAX_THREADS = 10;
    private static final String CASE_ID_VARIABLE = "caseId";
    private static final String STATE_ID_VARIABLE = "stateId";
    private static final String EVENT_ID_VARIABLE = "eventId";
    private static final int PAGE_SIZE = 50;
    private static final Pattern ALREADY_PROCESSED_PATTERN =
        Pattern.compile("already processed|already performed", Pattern.CASE_INSENSITIVE);
    private static final String ACTIVITY_ID = "activityId";
    private static final String INCIDENT_TIMESTAMP = "incidentTimestamp";
    private static final String UNKNOWN = "UNKNOWN";
    private static final String STUCK_CASE_EVENT_TYPE = "incidentRetry";
    private static final String STUCK_CASE_EVENT_NAME = "StuckCaseDetected";
    private static final DateTimeFormatter INCIDENT_FORMATTER =
        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
            .withZone(ZoneOffset.UTC);

    @Override
    public ExternalTaskData handleTask(ExternalTask externalTask) {
        String incidentStartTime = resolveStartTime(externalTask.getVariable("incidentStartTime"));
        String incidentEndTime = resolveEndTime(externalTask.getVariable("incidentEndTime"));
        String incidentMessageLike = externalTask.getVariable("incidentMessageLike");

        log.info("Incident retry process using date range {} → {}", incidentStartTime, incidentEndTime);

        IncidentRetryContext retryContext = new IncidentRetryContext(authTokenGenerator.generate());

        processAllIncidents(
            retryContext,
            incidentStartTime,
            incidentEndTime,
            incidentMessageLike
        );

        log.info(
            "Incident retry completed. Total={}, Success={}, Failed={}",
            retryContext.totalRetries.get(),
            retryContext.successRetries.get(),
            retryContext.failedRetries.get()
        );

        log.info(
            "All caseIds retried between incidents startTime={} and endTime={}: {}",
            incidentStartTime,
            incidentEndTime,
            retryContext.caseIds
        );

        return new ExternalTaskData();
    }

    private void processAllIncidents(
        IncidentRetryContext retryContext,
        String incidentStartTime,
        String incidentEndTime,
        String incidentMessageLike
    ) {
        int firstResult = 0;
        List<CamundaProcessInstance> processInstancesBatch;

        do {
            log.info("Calling process instances for {}, {}, {}, {}",
                     incidentStartTime, incidentEndTime, incidentMessageLike, firstResult);
            processInstancesBatch = fetchProcessInstances(
                retryContext.serviceAuthorization, incidentStartTime, incidentEndTime, incidentMessageLike, firstResult
            );

            log.info("Extracted process instances for {}, {}, {}, {}",
                     incidentStartTime, incidentEndTime, incidentMessageLike, firstResult);

            if (processInstancesBatch.isEmpty()) {
                return;
            }

            List<String> processInstanceIds = processInstancesBatch.stream()
                .map(CamundaProcessInstance::getId)
                .toList();

            log.info("Calling incidents for {} process instances with firstResult {}", processInstanceIds.size(), firstResult);

            List<CamundaIncident> incidents = getLatestIncidentsForProcessInstances(
                retryContext.serviceAuthorization,
                processInstanceIds
            );

            log.info("Extracted {} incidents with firstResult {}", incidents.size(), firstResult);

            if (!incidents.isEmpty()) {
                retryIncidents(
                    incidents,
                    retryContext
                );
            }

            firstResult += PAGE_SIZE;
        } while (processInstancesBatch.size() == PAGE_SIZE);
    }

    private void retryIncidents(
        List<CamundaIncident> incidents,
        IncidentRetryContext retryContext
    ) {
        log.info("Retrying {} incidents across process instances", incidents.size());
        int poolSize = Math.min(MAX_THREADS, incidents.size());
        ForkJoinPool customThreadPool = new ForkJoinPool(poolSize);

        try {
            customThreadPool.submit(() ->
                                        incidents.parallelStream().forEach(incident ->
                                                                               handleIncidentRetry(incident, retryContext))
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

    private void handleIncidentRetry(CamundaIncident incident, IncidentRetryContext retryContext) {
        retryContext.totalRetries.incrementAndGet();
        try {
            log.info("HandleIncidentRetry: calling retryIncidentSafely {}", incident.getId());
            if (retryIncidentSafely(
                incident,
                retryContext
            )) {
                retryContext.successRetries.incrementAndGet();
                log.info("Successfully retried incident {}: {}", incident.getId(), incident.getProcessInstanceId());
            } else {
                retryContext.failedRetries.incrementAndGet();
            }
        } catch (Exception e) {
            retryContext.failedRetries.incrementAndGet();
            log.error("Unexpected error retrying incident {}: {}", incident.getId(), e.getMessage(), e);
        }
    }

    private String resolveStartTime(String incidentStartTime) {
        return (incidentStartTime == null || incidentStartTime.isBlank())
            ? INCIDENT_FORMATTER.format(Instant.now().minus(24, ChronoUnit.HOURS))
            : incidentStartTime;
    }

    private String resolveEndTime(String incidentEndTime) {
        return (incidentEndTime == null || incidentEndTime.isBlank())
            ? INCIDENT_FORMATTER.format(Instant.now())
            : incidentEndTime;
    }

    private List<CamundaProcessInstance> fetchProcessInstances(String serviceAuthorization,
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

    private boolean retryIncidentSafely(CamundaIncident incident, IncidentRetryContext retryContext) {
        String processInstanceId = incident.getProcessInstanceId();
        Map<String, CamundaVariableValue> processVariables = fetchProcessVariables(processInstanceId, retryContext.serviceAuthorization);
        String incidentCaseId = resolveVariable(processVariables, CASE_ID_VARIABLE);
        String stateId = resolveVariable(processVariables, STATE_ID_VARIABLE);
        String lastEventId = resolveVariable(processVariables, EVENT_ID_VARIABLE);

        try {
            String failedActivityId = incident.getActivityId();
            retryContext.caseIds.add(incidentCaseId);

            String jobId = incident.getConfiguration();

            log.info(
                "Retrying incident {} for processInstanceId={} (jobId={}, caseId={}, activityId={})",
                incident.getId(), processInstanceId, jobId, incidentCaseId, failedActivityId
            );

            boolean alreadyProcessed = incident.getIncidentMessage() != null
                && ALREADY_PROCESSED_PATTERN.matcher(incident.getIncidentMessage()).find();
            boolean retrySucceeded;
            if (alreadyProcessed) {
                retrySucceeded = completeAlreadyProcessedIncident(
                    incident.getProcessInstanceId(),
                    retryContext.serviceAuthorization,
                    failedActivityId
                );
            } else {
                retrySucceeded = retryProcessInstance(processInstanceId, retryContext.serviceAuthorization, failedActivityId);
            }

            if (!retrySucceeded) {
                trackStuckCaseEvent(incident, incidentCaseId, stateId, lastEventId, "retry_failed", null);
                return false;
            }

            if (hasOpenIncident(processInstanceId, incident.getId(), retryContext.serviceAuthorization)) {
                trackStuckCaseEvent(incident, incidentCaseId, stateId, lastEventId, "retry_validation_failed", null);
                return false;
            }

            log.info(
                "Retries reset for job {} (processInstanceId={}, caseId={})",
                jobId,
                processInstanceId,
                incidentCaseId
            );
            return true;
        } catch (Exception e) {
            trackStuckCaseEvent(incident, incidentCaseId, stateId, lastEventId, "retry_failed_exception", e.getMessage());
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

    private boolean hasOpenIncident(String processInstanceId, String originalIncidentId, String serviceAuthorization) {
        try {
            List<CamundaIncident> incidents = camundaRuntimeApi.getLatestOpenIncidentForProcessInstance(
                serviceAuthorization,
                true,
                processInstanceId,
                INCIDENT_TIMESTAMP,
                "desc",
                1
            );
            if (incidents.isEmpty()) {
                return false;
            }

            CamundaIncident latestIncident = incidents.get(0);
            log.info(
                "Open incident {} still exists for processInstanceId={} after retry (originalIncidentId={})",
                latestIncident.getId(),
                processInstanceId,
                originalIncidentId
            );
            return true;
        } catch (Exception e) {
            log.warn("Could not verify incident clearance for processInstanceId={}", processInstanceId, e);
            return true;
        }
    }

    private boolean completeAlreadyProcessedIncident(String processInstanceId, String serviceAuthorization, String failedActivityId) {
        try {
            log.info("Completing incident for processInstance {} (already processed)",
                      processInstanceId);

            Map<String, Object> modificationRequest = new HashMap<>();
            modificationRequest.put("skipCustomListeners", true);
            modificationRequest.put("skipIoMappings", true);

            List<Map<String, Object>> instructions = new ArrayList<>();
            Map<String, Object> startAfterInstruction = new HashMap<>();
            startAfterInstruction.put("type", "startAfterActivity");
            startAfterInstruction.put(ACTIVITY_ID, failedActivityId);
            instructions.add(startAfterInstruction);

            modificationRequest.put("instructions", instructions);

            camundaRuntimeApi.modifyProcessInstance(
                serviceAuthorization,
                processInstanceId,
                modificationRequest
            );

            log.info("Successfully completed activity {} for process instance {}", failedActivityId, processInstanceId);
            return true;

        } catch (Exception e) {
            log.error("Failed to complete already processed activity {} for processInstance {}: {}",
                      failedActivityId, processInstanceId, e.getMessage(), e);
            return false;
        }
    }

    private boolean retryProcessInstance(String processInstanceId, String serviceAuthorization, String failedActivityId) {
        Map<String, Object> modificationRequest = new HashMap<>();
        modificationRequest.put("skipCustomListeners", true);
        modificationRequest.put("skipIoMappings", false);

        List<Map<String, Object>> instructions = new ArrayList<>();
        Map<String, Object> cancelInstruction = new HashMap<>();
        cancelInstruction.put("type", "cancel");
        cancelInstruction.put(ACTIVITY_ID, failedActivityId);
        instructions.add(cancelInstruction);

        Map<String, Object> startBeforeInstruction = new HashMap<>();
        startBeforeInstruction.put("type", "startBeforeActivity");
        startBeforeInstruction.put(ACTIVITY_ID, failedActivityId);
        instructions.add(startBeforeInstruction);

        modificationRequest.put("instructions", instructions);

        try {
            camundaRuntimeApi.modifyProcessInstance(
                serviceAuthorization,
                processInstanceId,
                modificationRequest
            );
            log.info("Process instance {} successfully modified to retry activity {}", processInstanceId, failedActivityId);
            return true;
        } catch (Exception e) {
            log.error("Failed to retry activity {} for processInstanceId={}: {}", failedActivityId, processInstanceId, e.getMessage(), e);
            return false;
        }
    }

    private Map<String, CamundaVariableValue> fetchProcessVariables(String processInstanceId, String serviceAuthorization) {
        try {
            return camundaRuntimeApi.getProcessVariables(
                processInstanceId,
                serviceAuthorization
            );
        } catch (Exception e) {
            log.warn("Could not fetch process variables for processInstanceId={}", processInstanceId, e);
        }
        return Collections.emptyMap();
    }

    private String resolveVariable(Map<String, CamundaVariableValue> variables, String variableName) {
        if (variables.containsKey(variableName) && variables.get(variableName).getValue() != null) {
            return String.valueOf(variables.get(variableName).getValue());
        }
        return UNKNOWN;
    }

    private void trackStuckCaseEvent(CamundaIncident incident,
                                     String caseId,
                                     String stateId,
                                     String lastEventId,
                                     String retryStatus,
                                     String retryFailureReason) {
        Map<String, String> additionalProperties = new HashMap<>();
        additionalProperties.put("processInstanceId", defaultIfBlank(incident.getProcessInstanceId()));
        additionalProperties.put("incidentId", defaultIfBlank(incident.getId()));
        additionalProperties.put("incidentMessage", defaultIfBlank(incident.getIncidentMessage()));
        additionalProperties.put(STATE_ID_VARIABLE, defaultIfBlank(stateId));
        additionalProperties.put("lastEventId", defaultIfBlank(lastEventId));
        additionalProperties.put("failedActivityId", defaultIfBlank(incident.getActivityId()));
        additionalProperties.put("retryStatus", retryStatus);
        additionalProperties.put("retryExhausted", Boolean.TRUE.toString());
        additionalProperties.put("jobId", defaultIfBlank(incident.getConfiguration()));
        if (StringUtils.isNotBlank(retryFailureReason)) {
            additionalProperties.put("retryFailureReason", retryFailureReason);
        }

        caseTaskTrackingService.trackCaseTask(
            defaultIfBlank(caseId),
            STUCK_CASE_EVENT_TYPE,
            STUCK_CASE_EVENT_NAME,
            additionalProperties
        );
    }

    private String defaultIfBlank(String value) {
        return StringUtils.isBlank(value) ? UNKNOWN : value;
    }

    private List<CamundaIncident> getLatestIncidentsForProcessInstances(
        String serviceAuthorization,
        List<String> processInstanceIds
    ) {
        return processInstanceIds.stream()
            .map(processInstanceId -> {
                try {
                    // Call the API for a single process instance
                    List<CamundaIncident> incidents = camundaRuntimeApi.getLatestOpenIncidentForProcessInstance(
                        serviceAuthorization,
                        true,                   // open incidents only
                        processInstanceId,       // single process instance
                        INCIDENT_TIMESTAMP,      // use default sortBy
                        "desc",                  // use default sortOrder
                        1                        // use default maxResults
                    );
                    CamundaIncident incidentDto = incidents.isEmpty() ? null : incidents.get(0);
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
