package uk.gov.hmcts.reform.civil.service.camunda;

import org.camunda.community.rest.client.model.VariableValueDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import uk.gov.hmcts.reform.civil.model.ExternalTaskDTO;

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

    @GetMapping("/external-task")
    List<ExternalTaskDTO> getExternalTasks(@RequestParam("processInstanceId") String processInstanceId);

    @PostMapping("/decision-definition/key/{decisionKey}/tenant-id/{tenantId}/evaluate")
    List<Map<String, Object>> evaluateDecision(
        @PathVariable("decisionKey") String decisionKey,
        @PathVariable("tenantId") String tenantId,
        @RequestBody Map<String, Object> requestBody
    );
}
