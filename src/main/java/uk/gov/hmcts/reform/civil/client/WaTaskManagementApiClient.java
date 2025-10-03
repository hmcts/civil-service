package uk.gov.hmcts.reform.civil.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import uk.gov.hmcts.reform.civil.model.taskmanagement.GetTasksResponse;
import uk.gov.hmcts.reform.civil.model.taskmanagement.SearchTaskRequest;
import jakarta.validation.Valid;

@FeignClient(name = "wa", url = "${task-management.api.url}")
public interface WaTaskManagementApiClient {

    @PostMapping(value = "/task", consumes = "application/json")
    GetTasksResponse searchWithCriteria(
        @RequestHeader("ServiceAuthorization") String serviceAuthorisation,
        @RequestHeader("Authorization") String authorization,
        @Valid @RequestBody SearchTaskRequest searchTaskRequest
    );

    @PostMapping(value = "/task/{task-id}/claim", consumes = "application/json")
    void claimTask(
        @RequestHeader("ServiceAuthorization") String serviceAuthorisation,
        @RequestHeader("Authorization") String authorization,
        @PathVariable("task-id") String taskId
    );
}
