package uk.gov.hmcts.reform.civil.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.FeignClientProperties;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import uk.gov.hmcts.reform.civil.model.wa.GetTasksResponse;
import uk.gov.hmcts.reform.civil.model.wa.SearchTaskRequest;
import uk.gov.hmcts.reform.civil.model.wa.Task;

import javax.validation.Valid;

@FeignClient(name = "wa", url = "${wa.task-management.api.url}", configuration =
    FeignClientProperties.FeignClientConfiguration.class)
public interface WaTaskManagementApiClient {

    @PostMapping(value = "/task", consumes = "application/json")
    ResponseEntity<GetTasksResponse<Task>> searchWithCriteria(
        @RequestHeader("Authorization") String authorization,
        @Valid @RequestBody SearchTaskRequest searchTaskRequest
    );
}
