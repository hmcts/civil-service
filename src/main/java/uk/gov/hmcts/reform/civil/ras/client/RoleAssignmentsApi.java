package uk.gov.hmcts.reform.civil.ras.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import uk.gov.hmcts.reform.civil.ras.model.QueryRequest;
import uk.gov.hmcts.reform.civil.ras.model.RoleAssignmentRequest;
import uk.gov.hmcts.reform.civil.ras.model.RoleAssignmentServiceResponse;
import uk.gov.hmcts.reform.civil.ras.model.UpdateRoleAssignmentResponse;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@FeignClient(name = "am-role-assignment-service-api", url = "${role-assignment-service.api.url}")
public interface RoleAssignmentsApi {

    String SERVICE_AUTHORIZATION = "ServiceAuthorization";
    String ACTOR_ID = "actorId";

    @GetMapping(
        value = "/am/role-assignments/actors/{actorId}",
        consumes = APPLICATION_JSON_VALUE,
        headers = CONTENT_TYPE + "=" + APPLICATION_JSON_VALUE
    )
    RoleAssignmentServiceResponse getRoleAssignments(
        @RequestHeader(AUTHORIZATION) String authorization,
        @RequestHeader(SERVICE_AUTHORIZATION) String serviceAuthorization,
        @PathVariable(ACTOR_ID) String actorId);

    @PostMapping(
        value = "/am/role-assignments/query",
        consumes = APPLICATION_JSON_VALUE,
        headers = CONTENT_TYPE + "=" + APPLICATION_JSON_VALUE
    )
    RoleAssignmentServiceResponse getRoleAssignments(
        @RequestHeader(AUTHORIZATION) String authorization,
        @RequestHeader(SERVICE_AUTHORIZATION) String serviceAuthorization,
        @RequestHeader(value = "x-correlation-id",
            required = false) String correlationId,
        @RequestHeader(value = "pageNumber", required = false) Integer pageNumber,
        @RequestHeader(value = "size", required = false) Integer size,
        @RequestHeader(value = "sort", required = false) String sort,
        @RequestHeader(value = "direction", required = false) String direction,
        @RequestBody(required = true) QueryRequest queryRequest,
        @RequestParam(value = "includeLabels", defaultValue = "false") Boolean includeLabels);

    @PostMapping(
        value = "/am/role-assignments",
        consumes = APPLICATION_JSON_VALUE,
        headers = CONTENT_TYPE + "=" + APPLICATION_JSON_VALUE
    )
    UpdateRoleAssignmentResponse createRoleAssignment(
        @RequestHeader(AUTHORIZATION) String authorisation,
        @RequestHeader(SERVICE_AUTHORIZATION) String serviceAuthorization,
        @RequestBody RoleAssignmentRequest request
    );

}
