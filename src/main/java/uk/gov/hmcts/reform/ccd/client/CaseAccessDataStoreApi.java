package uk.gov.hmcts.reform.ccd.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import uk.gov.hmcts.reform.civil.model.AddCaseAssignedUserRolesRequest;
import uk.gov.hmcts.reform.civil.model.AddCaseAssignedUserRolesResponse;
import uk.gov.hmcts.reform.civil.model.CaseAssignedUserRolesRequest;
import uk.gov.hmcts.reform.civil.model.CaseAssignedUserRolesResource;

import java.util.List;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi.SERVICE_AUTHORIZATION;

@FeignClient(name = "ccd-access-data-store-api", url = "${core_case_data.api.url}",
    configuration = CoreCaseDataConfiguration.class)
public interface CaseAccessDataStoreApi {

    @PostMapping(
        value = "/case-users",
        consumes = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseBody
    AddCaseAssignedUserRolesResponse addCaseUserRoles(
        @RequestHeader(AUTHORIZATION) String authorisation,
        @RequestHeader(SERVICE_AUTHORIZATION) String serviceAuthorization,
        @RequestBody AddCaseAssignedUserRolesRequest caseRoleRequest
    );

    @GetMapping(
        value = "/case-users",
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseBody
    CaseAssignedUserRolesResource getUserRoles(
        @RequestHeader(AUTHORIZATION) String authorisation,
        @RequestHeader(SERVICE_AUTHORIZATION) String serviceAuthorization,
        @RequestParam("case_ids") List<String> caseIds
    );

    @DeleteMapping(
        value = "/case-users",
        consumes = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseBody
    AddCaseAssignedUserRolesResponse removeCaseUserRoles(
        @RequestHeader(AUTHORIZATION) String authorisation,
        @RequestHeader(SERVICE_AUTHORIZATION) String serviceAuthorization,
        @RequestBody CaseAssignedUserRolesRequest caseRoleRequest
    );
}
