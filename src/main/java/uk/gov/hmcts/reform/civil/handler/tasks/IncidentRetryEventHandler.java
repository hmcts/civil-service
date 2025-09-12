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

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

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

    @Override
    public ExternalTaskData handleTask(ExternalTask externalTask) {
        String serviceAuthorization = authTokenGenerator.generate();
        String incidentStartTime = externalTask.getVariable("incidentStartTime");
        String incidentEndTime = externalTask.getVariable("incidentEndTime");
        String caseIds = externalTask.getVariable("caseIds");

        log.info(
            "Incident retry process {} using date range {} → {}",
            caseIds != null ? "for caseIds=" + caseIds : "for all cases",
            incidentStartTime, incidentEndTime
        );

        List<String> caseIdList = (caseIds == null || caseIds.isBlank())
            ? Collections.emptyList()
            : Stream.of(caseIds.split(","))
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .toList(); // Java 16+ unmodifiable

        List<ProcessInstanceDto> processInstances = fetchProcessInstances(serviceAuthorization, caseIdList, incidentStartTime, incidentEndTime);

        if (processInstances.isEmpty()) {
            log.info("No process instances found for retry.");
            return ExternalTaskData.builder().build();
        }

        List<String> processInstanceIds = processInstances.stream()
            .map(ProcessInstanceDto::getId)
            .toList(); // Java 16+ unmodifiable

        List<IncidentDto> incidents = camundaRuntimeApi.getOpenIncidents(serviceAuthorization, true, processInstanceIds);

        if (incidents.isEmpty()) {
            log.info("No open incidents found for these process instances.");
            return ExternalTaskData.builder().build();
        }

        log.info("Retrying {} incidents across {} process instances", incidents.size(), processInstances.size());

        // Metrics counters
        AtomicInteger totalRetries = new AtomicInteger(0);
        AtomicInteger successRetries = new AtomicInteger(0);
        AtomicInteger failedRetries = new AtomicInteger(0);

        int poolSize = Math.min(MAX_THREADS, incidents.size());
        ForkJoinPool customThreadPool = new ForkJoinPool(poolSize);

        try {
            customThreadPool.submit(() ->
                                        incidents.parallelStream().forEach(incident -> {
                                            totalRetries.incrementAndGet();
                                            try {
                                                boolean success = retryIncidentSafely(incident, serviceAuthorization);
                                                if (success) {
                                                    successRetries.incrementAndGet();
                                                } else {
                                                    failedRetries.incrementAndGet();
                                                }
                                            } catch (Exception e) {
                                                failedRetries.incrementAndGet();
                                                log.error("Unexpected error retrying incident {}: {}", incident.getId(), e.getMessage(), e);
                                            }
                                        })
            ).get();
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt(); // restore the interrupted status
            log.error("Incident retry execution was interrupted", ie);
        } catch (ExecutionException ee) {
            log.error("Error during parallel incident retries", ee.getCause());
        } finally {
            customThreadPool.shutdown();
        }

        log.info(
            "Incident retry completed. Total={}, Success={}, Failed={}",
            totalRetries.get(), successRetries.get(), failedRetries.get()
        );

        return ExternalTaskData.builder().build();
    }

    private List<ProcessInstanceDto> fetchProcessInstances(String serviceAuthorization, List<String> caseIdList,
                                                           String incidentStartTime, String incidentEndTime) {
        if (!caseIdList.isEmpty()) {
            return caseIdList.stream()
                .flatMap(id -> camundaRuntimeApi.getProcessInstancesByCaseId(
                    serviceAuthorization,
                    CASE_ID_VARIABLE + "_eq_" + id,
                    true,
                    true
                ).stream())
                .toList(); // Java 16+ unmodifiable
        } else {
            return camundaRuntimeApi.getUnfinishedProcessInstancesWithIncidents(
                serviceAuthorization, true, true, incidentStartTime, incidentEndTime
            );
        }
    }

    /**
     * Retry an incident safely and return success/failure for metrics.
     */
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

            log.info("Retries reset for job {} (processInstanceId={}, caseId={})", jobId, processInstanceId, incidentCaseId);
            return true;

        } catch (Exception e) {
            log.error("Error retrying incident {} (processInstanceId={}): {}", incident.getId(), incident.getProcessInstanceId(), e.getMessage(), e);
            return false;
        }
    }

    private String fetchCaseId(String processInstanceId, String serviceAuthorization) {
        try {
            Map<String, VariableValueDto> variables = camundaRuntimeApi.getProcessVariables(processInstanceId, serviceAuthorization);
            if (variables.containsKey(CASE_ID_VARIABLE) && variables.get(CASE_ID_VARIABLE).getValue() != null) {
                return String.valueOf(variables.get(CASE_ID_VARIABLE).getValue());
            }
        } catch (Exception e) {
            log.warn("Could not fetch caseId for processInstanceId={}", processInstanceId, e);
        }
        return "UNKNOWN";
    }
}
