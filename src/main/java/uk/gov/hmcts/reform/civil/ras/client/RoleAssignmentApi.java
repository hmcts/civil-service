package uk.gov.hmcts.reform.civil.ras.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;


@FeignClient(name = "ras-api", url = "${role-assignment-service.api.url}")
public interface RoleAssignmentApi {

    String SERVICE_AUTHORIZATION = "ServiceAuthorization";

    @GetMapping(
        value = "/am/role-assignments/actors/{actorId}",
        produces = MediaType.APPLICATION_JSON_VALUE

    )
    String getRoleAssignments(
        //@RequestHeader(HttpHeaders.ACCEPT) String acceptType,
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
        @RequestHeader(SERVICE_AUTHORIZATION) String serviceAuthorization,
        @PathVariable("actorId") String actorId);

}
