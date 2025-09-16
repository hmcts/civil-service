package uk.gov.hmcts.reform.civil.service.camunda;

import org.camunda.community.rest.client.model.IncidentDto;
import org.camunda.community.rest.client.model.ProcessInstanceDto;
import org.camunda.community.rest.client.model.VariableValueDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@FeignClient(name = "camunda-rest-engine-api", url = "${feign.client.config.processInstance.url}")
public interface CamundaRuntimeApi {

    @GetMapping("process-instance/{processInstanceId}/variables")
    HashMap<String, VariableValueDto> getProcessVariables(
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
    List<ProcessInstanceDto> getProcessInstancesByCaseId(
        @RequestHeader("ServiceAuthorization") String serviceAuthorization,
        @RequestParam(value = "variables", required = false) String variables,  // e.g. "caseId_eq_12345"
        @RequestParam("unfinished") boolean unfinished,
        @RequestParam(value = "withIncident", required = false) Boolean withIncident
    );

    @GetMapping("/process-instance")
    List<ProcessInstanceDto> getUnfinishedProcessInstancesWithIncidents(
        @RequestHeader("ServiceAuthorization") String serviceAuthorization,
        @RequestParam("unfinished") boolean unfinished,
        @RequestParam("withIncident") boolean withIncident,
        @RequestParam("startedAfter") String startedAfter,   // e.g. 2025-09-10T12:00:00Z
        @RequestParam("startedBefore") String startedBefore  // e.g. 2025-09-10T23:59:59Z
    );

    @GetMapping("/incident")
    List<IncidentDto> getOpenIncidents(
        @RequestHeader("ServiceAuthorization") String serviceAuthorization,
        @RequestParam("open") boolean open,
        @RequestParam("processInstanceIdIn") String processInstanceIdsCsv
    );

    @PutMapping("/job/{jobId}/retries")
    void setJobRetries(
        @RequestHeader("ServiceAuthorization") String serviceAuthorization,
        @PathVariable("jobId") String jobId,
        @RequestBody Map<String, Object> body
    );
}
