package uk.gov.hmcts.reform.civil.controllers.testingsupport;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import uk.gov.hmcts.reform.civil.model.camunda.CamundaActivityInstance;
import uk.gov.hmcts.reform.civil.model.camunda.CamundaHistoricProcessInstance;
import uk.gov.hmcts.reform.civil.model.camunda.CamundaIncident;
import uk.gov.hmcts.reform.civil.model.camunda.CamundaProcessInstance;

import java.util.List;
import java.util.Map;

/**
 * Camunda REST endpoints used only by {@code /testing-support}, replacing the Holunda generated API
 * clients ({@code ExternalTaskApiClient}, {@code HistoryApiClient}, {@code IncidentApiClient},
 * {@code ProcessDefinitionApiClient}, {@code ProcessInstanceApiClient}) so the build does not depend
 * on the Holunda REST client, which is not compatible with Spring Boot 4.
 *
 * <p>Kept separate from {@link uk.gov.hmcts.reform.civil.service.camunda.CamundaRuntimeApi} rather
 * than bolted onto it. These endpoints exist to support functional tests, not production
 * behaviour, and mixing them would make the production surface look larger than it is.</p>
 *
 * <p>Request bodies are {@code Map} rather than typed DTOs, following
 * {@code CamundaRuntimeApi.queryProcessInstances}. The query shapes here are small and written in
 * one place, so a DTO per body would add types without adding safety.</p>
 */
@FeignClient(name = "camunda-testing-support-api", url = "${feign.client.config.processInstance.url}")
public interface CamundaTestingSupportApi {

    /**
     * Activity instance tree for a process instance, used to find the incident on a stuck process.
     */
    @GetMapping("/process-instance/{processInstanceId}/activity-instances")
    CamundaActivityInstance getActivityInstanceTree(
        @PathVariable("processInstanceId") String processInstanceId
    );

    @GetMapping("/incident/{incidentId}")
    CamundaIncident getIncident(
        @PathVariable("incidentId") String incidentId
    );

    /**
     * Error details for a failed external task. The engine returns the stack trace as plain text,
     * not JSON, so this is a {@code String}.
     */
    @GetMapping("/external-task/{externalTaskId}/errorDetails")
    String getExternalTaskErrorDetails(
        @PathVariable("externalTaskId") String externalTaskId
    );

    /**
     * Starts a process by definition key within the civil tenant. The body takes the Camunda start
     * shape, {@code {"variables": {"name": {"value": ..., "type": ...}}}}.
     */
    @PostMapping("/process-definition/key/{definitionKey}/tenant-id/{tenantId}/start")
    CamundaProcessInstance startProcessInstanceByKeyAndTenantId(
        @PathVariable("definitionKey") String definitionKey,
        @PathVariable("tenantId") String tenantId,
        @RequestBody Map<String, Object> startRequest
    );

    /**
     * Historic process instance query. Returns {@link CamundaHistoricProcessInstance} rather than
     * {@code CamundaProcessInstance} because the functional suites assert on {@code state}, which
     * the runtime representation does not carry.
     */
    @PostMapping("/history/process-instance")
    List<CamundaHistoricProcessInstance> queryHistoricProcessInstances(
        @RequestParam(value = "firstResult", required = false) Integer firstResult,
        @RequestParam(value = "maxResults", required = false) Integer maxResults,
        @RequestBody Map<String, Object> query
    );
}
