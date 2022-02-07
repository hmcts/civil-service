package uk.gov.hmcts.reform.civil.controllers.cases;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.gov.hmcts.reform.civil.service.RoleAssignmentsService;
import uk.gov.hmcts.reform.ras.model.RasResponse;
@Api
@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping(
    path = "/cases",
    produces = MediaType.APPLICATION_JSON_VALUE
)
public class CasesController {

    private final RoleAssignmentsService roleAssignmentsService;

    @GetMapping(path = "/actors/{actorId}")
    @ApiOperation("Gets credentials for actorId from RAS")
    public ResponseEntity<RasResponse> getCredentials(@PathVariable("actorId") String actorId,
                                                      @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization)
    {

        log.info("Received ActorId: {}",actorId);
        var roleAssignmentResponse = roleAssignmentsService.getRoleAssignments(actorId, authorization);
        return new ResponseEntity<>(roleAssignmentResponse, HttpStatus.OK);
    }
}
