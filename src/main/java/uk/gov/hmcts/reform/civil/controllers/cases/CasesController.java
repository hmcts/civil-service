package uk.gov.hmcts.reform.civil.controllers.cases;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.SearchResult;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.citizenui.DashboardClaimInfo;
import uk.gov.hmcts.reform.civil.model.search.Query;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.civil.service.RoleAssignmentsService;
import uk.gov.hmcts.reform.civil.service.citizenui.CaseEventService;
import uk.gov.hmcts.reform.civil.service.claimstore.ClaimStoreService;
import uk.gov.hmcts.reform.ras.model.RoleAssignmentServiceResponse;

import java.util.List;

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

    private final RoleAssignmentsService roleAssignmentsService;
    private final CoreCaseDataService coreCaseDataService;
    private final CaseDetailsConverter caseDetailsConverter;
    private final ClaimStoreService claimStoreService;
    private final CaseEventService caseEventService;

    @GetMapping(path = {
        "/{caseId}",
    })
    @ApiOperation("get case by id from CCD")
    public ResponseEntity<CaseData> getCaseId(
        @PathVariable("caseId") Long caseId,
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation
    ) {
        log.info(
            "Received CaseId: {}",
            caseId
        );

        var caseDataResponse = caseDetailsConverter
            .toCaseData(coreCaseDataService.getCase(caseId, authorisation).getData());

        return new ResponseEntity<>(caseDataResponse, HttpStatus.OK);
    }

    @PostMapping(path = "/")
    @ApiOperation("get list of the cases from CCD")
    public ResponseEntity<SearchResult> getCaseList(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
                                                @RequestBody String searchString) {

        log.info("Received getCaseList");

        Query query = new Query(QueryBuilders
                                    .wrapperQuery(searchString), emptyList(), 0);
        SearchResult claims = coreCaseDataService.searchCases(query, authorization);

        return new ResponseEntity<>(claims, HttpStatus.OK);
    }

    @GetMapping(path = "/actors/{actorId}")
    @ApiOperation("Gets credentials for actorId from RAS")
    public ResponseEntity<RoleAssignmentServiceResponse>
        getRoleAssignmentsByActorId(@PathVariable("actorId") String actorId,
                                @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) {

        log.info("Received ActorId: {}", actorId);
        var roleAssignmentResponse = roleAssignmentsService.getRoleAssignments(actorId, authorization);
        return new ResponseEntity<>(roleAssignmentResponse, HttpStatus.OK);
    }

    @GetMapping(path = "/claimant/{submitterId}")
    @ApiOperation("Gets basic claim information for claimant")
    public ResponseEntity<List<DashboardClaimInfo>>
        getClaimsForClaimant(@PathVariable("submitterId") String submitterId,
                            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) {
        List<DashboardClaimInfo> ocmcClaims = claimStoreService.getClaimsForClaimant(authorization, submitterId);
        return new ResponseEntity<>(ocmcClaims, HttpStatus.OK);
    }

    @GetMapping(path = "/defendant/{submitterId}")
    @ApiOperation("Gets basic claim information for defendant")
    public ResponseEntity<List<DashboardClaimInfo>>
        getClaimsForDefendant(@PathVariable("submitterId") String submitterId,
                              @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) {
        List<DashboardClaimInfo> ocmcClaims = claimStoreService.getClaimsForDefendant(authorization, submitterId);
        return new ResponseEntity<>(ocmcClaims, HttpStatus.OK);
    }

    @GetMapping(path = "/defendant/{submitterId}/response/{caseId}/event-token")
    @ApiOperation("Gets event token for defendant submit response event")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK"),
        @ApiResponse(code = 401, message = "Not Authorized")})
    public ResponseEntity<String>
        getSubmitResponseToken(@PathVariable("submitterId") String submitterId,
                           @PathVariable("caseId") String caseId,
                           @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) {
        String eventToken = caseEventService.getDefendantResponseSpecEventToken(authorization, submitterId, caseId);
        return new ResponseEntity<>(eventToken, HttpStatus.OK);
    }
}
