package uk.gov.hmcts.reform.ras.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import uk.gov.hmcts.reform.ras.model.RoleAssignmentResponse;

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
    RoleAssignmentResponse getRoleAssignments(
        @RequestHeader(AUTHORIZATION) String authorization,
        @RequestHeader(SERVICE_AUTHORIZATION) String serviceAuthorization,
        @PathVariable(ACTOR_ID) String actorId);

}
