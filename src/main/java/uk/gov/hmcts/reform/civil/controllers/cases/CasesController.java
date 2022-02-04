package uk.gov.hmcts.reform.civil.controllers.cases;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.ccd.client.model.SearchResult;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.search.Query;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.civil.service.RoleAssignmentsService;
import uk.gov.hmcts.reform.ras.model.RasResponse;
import uk.gov.hmcts.reform.ras.model.RoleAssignmentResponse;

import static java.util.Collections.emptyList;
@Api
@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping(
    path = "/cases",
    produces = MediaType.APPLICATION_JSON_VALUE
)
public class CasesController {

    private final CoreCaseDataService coreCaseDataService;
    private final CaseDetailsConverter caseDetailsConverter;
    private final RoleAssignmentsService roleAssignmentsService;

    @GetMapping(path = {
        "/{cid}",
    })
    @ApiOperation("get case by id from CCD")
   public ResponseEntity<CaseData> getClaimById(
        @PathVariable("cid") Long claimId
    ) {
        log.info(
            "Received CaseId: {}",
            claimId
        );

        var caseDataResponse = caseDetailsConverter.toCaseData(coreCaseDataService.getCase(claimId).getData());
        log.info(
            "CaseDataResponse : {}",
            caseDataResponse
        );
        return new ResponseEntity<>(caseDataResponse, HttpStatus.OK);
    }

    @GetMapping(path = "/")
    @ApiOperation("Handles all callbacks from CCD")
    public ResponseEntity<SearchResult> getList(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization){

        log.info("Received callback from CCD getting claim list");

        Query query = new Query(QueryBuilders.matchQuery("case_type_id", "CIVIL"), emptyList(), 0);
        SearchResult claims = coreCaseDataService.searchCases(query);

        return new ResponseEntity<>(claims, HttpStatus.OK);
    }

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
