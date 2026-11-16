package uk.gov.hmcts.reform.civil.service.camunda;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import uk.gov.hmcts.reform.civil.model.camunda.CamundaIncident;
import uk.gov.hmcts.reform.civil.model.camunda.CamundaMessageCorrelation;
import uk.gov.hmcts.reform.civil.model.camunda.CamundaProcessInstance;
import uk.gov.hmcts.reform.civil.model.camunda.CamundaVariableValue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@FeignClient(name = "camunda-rest-engine-api", url = "${feign.client.config.processInstance.url}")
public interface CamundaRuntimeApi {

    @GetMapping("process-instance/{processInstanceId}/variables")
    HashMap<String, CamundaVariableValue> getProcessVariables(
        @PathVariable("processInstanceId") String processInstanceId,
        @RequestHeader("ServiceAuthorization") String serviceAuthorization
    );

    @PostMapping("/decision-definition/key/{decisionKey}/tenant-id/{tenantId}/evaluate")
    List<Map<String, Object>> evaluateDecision(
        @PathVariable("decisionKey") String decisionKey,
        @PathVariable("tenantId") String tenantId,
        @RequestBody Map<String, Object> requestBody
    );

    @GetMapping("/process-instance")
    List<CamundaProcessInstance> getUnfinishedProcessInstancesWithIncidents(
        @RequestHeader("ServiceAuthorization") String serviceAuthorization,
        @RequestParam("unfinished") boolean unfinished,
        @RequestParam("withIncident") boolean withIncident,
        @RequestParam("startedAfter") String startedAfter,   // e.g. 2025-09-10T12:00:00Z
        @RequestParam("startedBefore") String startedBefore, // e.g. 2025-09-10T23:59:59Z
        @RequestParam(value = "firstResult", required = false) Integer firstResult,  // pagination offset
        @RequestParam(value = "maxResults", defaultValue = "50") Integer maxResults,    // pagination limit
        @RequestParam(value = "sortBy", required = false) String sortBy,             // e.g. "startTime"
        @RequestParam(value = "sortOrder", required = false) String sortOrder,        // "asc" or "desc"
        @RequestParam(value = "incidentStatus", defaultValue = "open") String incidentStatus
    );

    @PostMapping("/history/process-instance")
    List<CamundaProcessInstance> queryProcessInstances(
        @RequestHeader("ServiceAuthorization") String serviceAuthorization,
        @RequestParam(value = "firstResult", required = false) Integer firstResult,
        @RequestParam(value = "maxResults", required = false) Integer maxResults,
        @RequestParam(value = "sortBy", required = false) String sortBy,
        @RequestParam(value = "sortOrder", required = false) String sortOrder,
        @RequestBody Map<String, Object> filters
    );

    @GetMapping("/history/external-task-log/{incidentId}/error-details")
    Map<String, Object> fetchErrorDetails(
        @PathVariable("incidentId") String incidentId,
        @RequestHeader("ServiceAuthorization") String serviceAuthorization
    );

    @GetMapping("/incident")
    List<CamundaIncident> getLatestOpenIncidentForProcessInstance(
        @RequestHeader("ServiceAuthorization") String serviceAuthorization,
        @RequestParam("open") boolean open,
        @RequestParam("processInstanceId") String processInstanceId, // single process instance ID
        @RequestParam(value = "sortBy", defaultValue = "incidentTimestamp") String sortBy,
        @RequestParam(value = "sortOrder", defaultValue = "desc") String sortOrder,
        @RequestParam(value = "maxResults", defaultValue = "1") int maxResults
    );

    @PutMapping("/job/{jobId}/retries")
    void setJobRetries(
        @RequestHeader("ServiceAuthorization") String serviceAuthorization,
        @PathVariable("jobId") String jobId,
        @RequestBody Map<String, Object> body
    );

    @PostMapping("/process-instance/{processInstanceId}/modification")
    void modifyProcessInstance(
        @RequestHeader("ServiceAuthorization") String serviceAuthorization,
        @PathVariable("processInstanceId") String processInstanceId,
        @RequestBody Map<String, Object> modificationRequest
    );

    /**
     * REST equivalent of {@code runtimeService.setVariables(processInstanceId, variables)}.
     *
     * <p>The body takes the Camunda "modify variables" shape, i.e.
     * {@code {"modifications": {"name": {"value": ..., "type": ...}}}}.</p>
     */
    @PostMapping("/process-instance/{processInstanceId}/variables")
    void setProcessVariables(
        @RequestHeader("ServiceAuthorization") String serviceAuthorization,
        @PathVariable("processInstanceId") String processInstanceId,
        @RequestBody Map<String, Object> modifications
    );

    /**
     * REST equivalent of {@code runtimeService.setVariable(processInstanceId, name, value)}.
     */
    @PutMapping("/process-instance/{processInstanceId}/variables/{variableName}")
    void setProcessVariable(
        @RequestHeader("ServiceAuthorization") String serviceAuthorization,
        @PathVariable("processInstanceId") String processInstanceId,
        @PathVariable("variableName") String variableName,
        @RequestBody CamundaVariableValue variableValue
    );

    /**
     * REST equivalent of {@code runtimeService.createMessageCorrelation(...).correlateStartMessage()}.
     */
    @PostMapping("/message")
    void correlateMessage(
        @RequestHeader("ServiceAuthorization") String serviceAuthorization,
        @RequestBody CamundaMessageCorrelation correlation
    );
}
