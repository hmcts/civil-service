package uk.gov.hmcts.reform.civil.controllers.cases;

import feign.QueryMap;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
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
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SearchResult;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.citizenui.DashboardClaimInfo;
import uk.gov.hmcts.reform.civil.model.citizenui.dto.EventDto;
import uk.gov.hmcts.reform.civil.model.search.Query;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.civil.service.RoleAssignmentsService;
import uk.gov.hmcts.reform.civil.service.citizen.events.CaseEventService;
import uk.gov.hmcts.reform.civil.service.citizen.events.EventSubmissionParams;
import uk.gov.hmcts.reform.civil.service.citizenui.DashboardClaimInfoService;
import uk.gov.hmcts.reform.civil.service.citizenui.responsedeadline.DeadlineExtensionCalculatorService;
import uk.gov.hmcts.reform.civil.ras.model.RoleAssignmentServiceResponse;

import javax.ws.rs.QueryParam;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

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
    private final DashboardClaimInfoService dashboardClaimInfoService;
    private final CaseEventService caseEventService;
    private final DeadlineExtensionCalculatorService deadlineExtensionCalculatorService;

    @GetMapping(path = {
        "/{caseId}",
    })
    @ApiOperation("get case by id from CCD")
    public ResponseEntity<CaseDetails> getCaseId(
        @PathVariable("caseId") Long caseId,
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation
    ) {
        log.info(
            "Received CaseId: {}",
            caseId
        );

        var caseDetailsResponse = coreCaseDataService.getCase(caseId, authorisation);
        log.info("Returning case details: {}", caseDetailsResponse);

        return new ResponseEntity<>(caseDetailsResponse, HttpStatus.OK);
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
        List<DashboardClaimInfo> ocmcClaims = dashboardClaimInfoService.getClaimsForClaimant(
            authorization,
            submitterId
        );
        return new ResponseEntity<>(ocmcClaims, HttpStatus.OK);
    }

    @GetMapping(path = "/defendant/{submitterId}?page={pageNumber}")
    @ApiOperation("Gets basic claim information for defendant")
    public ResponseEntity<List<DashboardClaimInfo>>
        getClaimsForDefendant(@PathVariable("submitterId") String submitterId,
                          @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) {
        List<DashboardClaimInfo> defendantClaims = dashboardClaimInfoService.getClaimsForDefendant(
            authorization,
            submitterId
        );
        return new ResponseEntity<>(defendantClaims, HttpStatus.OK);
    }

    @GetMapping(path = "/defendant/{submitterId}?page={pageNumber}")
    @ApiOperation("Gets basic claim information for defendant with pagination")
    public ResponseEntity<Map<String, Object>>
    getClaimsForDefendantWithPagination(@PathVariable("submitterId") String submitterId, @QueryParam("pageNumber") String pageNumber,
                          @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) {
        Map<String, Object> defendantClaimsWithPagination = dashboardClaimInfoService.getClaimsForDefendantWithPagination(
            authorization,
            submitterId,
            pageNumber
        );
        return new ResponseEntity<>(defendantClaimsWithPagination, HttpStatus.OK);
    }

    @PostMapping(path = "/{caseId}/citizen/{submitterId}/event")
    @ApiOperation("Submits event")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK"),
        @ApiResponse(code = 401, message = "Not Authorized")})
    public ResponseEntity<CaseData>
        submitEvent(@PathVariable("submitterId") String submitterId,
                    @PathVariable("caseId") String caseId,
                    @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
                    @RequestBody EventDto eventDto) {
        EventSubmissionParams params = EventSubmissionParams
            .builder()
            .authorisation(authorization)
            .caseId(caseId)
            .userId(submitterId)
            .event(eventDto.getEvent())
            .updates(eventDto.getCaseDataUpdate())
            .build();
        log.info(eventDto.getCaseDataUpdate().toString());
        CaseData caseData = caseDetailsConverter
            .toCaseData(caseEventService.submitEvent(params));
        return new ResponseEntity<>(caseData, HttpStatus.OK);
    }

    @PostMapping(path = "/response/deadline")
    @ApiOperation("Calculates extended response deadline")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "Authorization", value = "Authorization token",
            required = true, dataType = "string", paramType = "header")})
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK"),
        @ApiResponse(code = 401, message = "Not Authorized")})
    public ResponseEntity<LocalDate> calculateNewResponseDeadline(@RequestBody LocalDate extendedDeadline) {
        LocalDate calculatedDeadline = deadlineExtensionCalculatorService.calculateExtendedDeadline(extendedDeadline);
        return new ResponseEntity<>(calculatedDeadline, HttpStatus.OK);
    }

}
