package uk.gov.hmcts.reform.civil.service.camunda;

import org.camunda.community.rest.client.model.VariableValueDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.HashMap;

@FeignClient(name = "camunda-rest-engine-api", url = "${feign.client.config.processInstance.url}")
public interface CamundaRuntimeApi {

    @GetMapping("process-instance/{processInstanceId}/variables")
    HashMap<String, VariableValueDto> getProcessVariables(
        @PathVariable("processInstanceId") String processInstanceId,
        @RequestHeader("ServiceAuthorization") String serviceAuthorization
    );
}
