package uk.gov.hmcts.reform.civil.controllers.testingsupport;

import org.camunda.bpm.engine.rest.dto.runtime.ActivityInstanceDto;
import org.camunda.bpm.engine.rest.dto.runtime.IncidentDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "camunda-rest-engine-api", url = "${feign.client.config.remoteRuntimeService.url}")
public interface CamundaRestEngineApi {

    @GetMapping("process-instance/{processInstanceId}/activity-instances")
    ActivityInstanceDto getActivityInstanceByProcessInstanceId(
        @PathVariable("processInstanceId") String processInstanceId,
        @RequestHeader("ServiceAuthorization") String serviceAuthorization
    );

    @GetMapping("incident/{incidentId}")
    IncidentDto getIncidentById(
        @PathVariable("incidentId") String incidentId,
        @RequestHeader("ServiceAuthorization") String serviceAuthorization
    );

    @GetMapping("external-task/{externalTaskId}/errorDetails")
    String getErrorDetailsByExternalTaskId(
        @PathVariable("externalTaskId") String externalTaskId,
        @RequestHeader("ServiceAuthorization") String serviceAuthorization
    );

}
