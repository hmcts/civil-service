package uk.gov.hmcts.reform.civil.controllers.cases;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SearchResult;
import uk.gov.hmcts.reform.civil.exceptions.CaseDataInvalidException;
import uk.gov.hmcts.reform.civil.model.bulkclaims.CaseworkerSubmitEventDTo;
import uk.gov.hmcts.reform.civil.model.citizenui.DashboardResponse;
import uk.gov.hmcts.reform.civil.model.citizenui.dto.EventDto;
import uk.gov.hmcts.reform.civil.model.citizenui.dto.RepaymentDecisionType;
import uk.gov.hmcts.reform.civil.model.repaymentplan.ClaimantProposedPlan;
import uk.gov.hmcts.reform.civil.model.search.Query;
import uk.gov.hmcts.reform.civil.ras.model.RoleAssignmentServiceResponse;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.civil.service.RoleAssignmentsService;
import uk.gov.hmcts.reform.civil.service.bulkclaims.CaseWorkerSearchCaseParams;
import uk.gov.hmcts.reform.civil.service.bulkclaims.CaseworkerCaseEventService;
import uk.gov.hmcts.reform.civil.service.bulkclaims.CaseworkerEventSubmissionParams;
import uk.gov.hmcts.reform.civil.service.citizen.events.CaseEventService;
import uk.gov.hmcts.reform.civil.service.citizen.events.EventSubmissionParams;
import uk.gov.hmcts.reform.civil.service.citizen.repaymentplan.RepaymentPlanDecisionService;
import uk.gov.hmcts.reform.civil.service.citizenui.DashboardClaimInfoService;
import uk.gov.hmcts.reform.civil.service.citizenui.responsedeadline.DeadlineExtensionCalculatorService;
import uk.gov.hmcts.reform.civil.service.search.CaseSdtRequestSearchService;
import uk.gov.hmcts.reform.civil.service.user.UserInformationService;
import uk.gov.hmcts.reform.civil.validation.PostcodeValidator;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyList;

@Tag(name = "Cases Controller")
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
    private final DashboardClaimInfoService dashboardClaimInfoService;
    private final CaseEventService caseEventService;
    private final CaseSdtRequestSearchService caseSdtRequestSearchService;
    private final CaseworkerCaseEventService caseworkerCaseEventService;
    private final DeadlineExtensionCalculatorService deadlineExtensionCalculatorService;
    private final PostcodeValidator postcodeValidator;
    private final UserInformationService userInformationService;
    private final RepaymentPlanDecisionService repaymentPlanDecisionService;

    @GetMapping(path = {
        "/{caseId}",
    })
    @Operation(summary = "get case by id from CCD")
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
    @Operation(summary = "get list of the cases from CCD")
    public ResponseEntity<SearchResult> getCaseList(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
                                                    @RequestBody String searchString) {

        log.info("Received getCaseList");

        Query query = new Query(QueryBuilders
                                    .wrapperQuery(searchString), emptyList(), 0);
        SearchResult claims = coreCaseDataService.searchCases(query, authorization);

        return new ResponseEntity<>(claims, HttpStatus.OK);
    }

    @GetMapping(path = "/actors/{actorId}")
    @Operation(summary = "Gets credentials for actorId from RAS")
    public ResponseEntity<RoleAssignmentServiceResponse> getRoleAssignmentsByActorId(
        @PathVariable("actorId") String actorId,
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization
    ) {

        log.info("Received ActorId: {}", actorId);
        var roleAssignmentResponse = roleAssignmentsService.getRoleAssignments(actorId, authorization);
        return new ResponseEntity<>(roleAssignmentResponse, HttpStatus.OK);
    }

    @GetMapping(path = "/claimant/{submitterId}")
    @Operation(summary = "Gets basic claim information for claimant")
    public ResponseEntity<DashboardResponse> getClaimsForClaimant(
        @PathVariable("submitterId") String submitterId,
        @RequestParam(value = "page", defaultValue = "1") int currentPage,
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization
    ) {
        DashboardResponse claimantClaims = dashboardClaimInfoService.getDashboardClaimantResponse(
            authorization,
            submitterId,
            currentPage
        );
        return new ResponseEntity<>(claimantClaims, HttpStatus.OK);
    }

    @GetMapping(path = "/defendant/{submitterId}")
    @Operation(summary = "Gets basic claim information for defendant")
    public ResponseEntity<DashboardResponse> getClaimsForDefendant(
        @PathVariable("submitterId") String submitterId,
        @RequestParam(value = "page", defaultValue = "1") int currentPage,
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization
    ) {
        DashboardResponse defendantClaims = dashboardClaimInfoService.getDashboardDefendantResponse(
            authorization,
            submitterId,
            currentPage
        );
        return new ResponseEntity<>(defendantClaims, HttpStatus.OK);
    }

    @PostMapping(path = "/{caseId}/citizen/{submitterId}/event")
    @Operation(summary = "Submits event")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "401", description = "Not Authorized")})
    public ResponseEntity<CaseDetails> submitEvent(
        @PathVariable("submitterId") String submitterId,
        @PathVariable("caseId") String caseId,
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
        @RequestBody EventDto eventDto
    ) {
        EventSubmissionParams params = EventSubmissionParams
            .builder()
            .authorisation(authorization)
            .caseId(caseId)
            .userId(submitterId)
            .event(eventDto.getEvent())
            .updates(eventDto.getCaseDataUpdate())
            .build();
        log.info(eventDto.getCaseDataUpdate().toString());
        CaseDetails caseDetails = caseEventService.submitEvent(params);
        return new ResponseEntity<>(caseDetails, HttpStatus.OK);
    }

    @PostMapping(path = "/response/deadline")
    @Operation(summary = "Calculates extended response deadline")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "401", description = "Not Authorized")})
    public ResponseEntity<LocalDate> calculateNewResponseDeadline(@RequestBody LocalDate extendedDeadline) {
        LocalDate calculatedDeadline = deadlineExtensionCalculatorService.calculateExtendedDeadline(extendedDeadline);
        return new ResponseEntity<>(calculatedDeadline, HttpStatus.OK);
    }

    @GetMapping(path = "/response/agreeddeadline/{caseId}")
    @Operation(summary = "Return the agreed deadline date")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "401", description = "Not Authorized")})
    public ResponseEntity<LocalDate> getAgreedDeadlineResponseDate(@PathVariable("caseId") Long caseId,
                                                           @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) {
        LocalDate deadlineAgreedDate = coreCaseDataService.getAgreedDeadlineResponseDate(caseId, authorization);
        return new ResponseEntity<>(deadlineAgreedDate, HttpStatus.OK);
    }

    @PostMapping(path = "/caseworkers/create-case/{userId}")
    @Operation(summary = "Submits event for new case, for caseworker")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Created"),
        @ApiResponse(responseCode = "401", description = "Not Authorized")})
    public ResponseEntity<CaseDetails> caseworkerSubmitEvent(
        @PathVariable("userId") String userId,
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
        @RequestBody CaseworkerSubmitEventDTo submitEventDto
    ) {
        try {
            CaseworkerEventSubmissionParams params = CaseworkerEventSubmissionParams
                .builder()
                .authorisation(authorization)
                .userId(userId)
                .event(submitEventDto.getEvent())
                .updates(submitEventDto.getData())
                .build();
            log.info("Updated case data:  " + submitEventDto.getData().toString());
            CaseDetails caseDetails = caseworkerCaseEventService.submitEventForNewClaimCaseWorker(params);
            return new ResponseEntity<>(caseDetails, HttpStatus.CREATED);
        } catch (Exception ex) {
            log.error("Case  creation unsuccessful:  " + ex.getMessage());
            throw new CaseDataInvalidException();
        }
    }

    @GetMapping(path = "/caseworker/searchCaseForSDT/{userId}")
    @Operation(summary = "SQL Search for a case, for caseworker")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "List of cases for the given search criteria")})
    public Boolean caseworkerSearchCase(
        @PathVariable("userId") String userId,
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
        @RequestParam(name = "sdtRequestId") String searchParam
    ) {
        CaseWorkerSearchCaseParams params = CaseWorkerSearchCaseParams.builder()
            .authorisation(authorization)
            .userId(userId)
            .searchCriteria(Map.of("case.sdtRequestIdFromSdt", searchParam)).build();
        List<CaseDetails> caseDetails = caseSdtRequestSearchService.searchCaseForSdtRequest(params);

        if (caseDetails.size() < 1 && caseDetails.isEmpty()) {
            return true;
        }
        return false;
    }

    @GetMapping(path = "/caseworker/validatePin")
    @Operation(summary = "Validate address - PostCode")
    public List<String> validatePostCode(
        @RequestParam(name = "postCode") String postCode
    ) {
        List<String> errors =  postcodeValidator.validate(postCode);
        return errors;
    }

    @GetMapping(path = "/{caseId}/userCaseRoles")
    @Operation(summary = "Get user Roles for a case")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "400", description = "Bad request for caseId"),
        @ApiResponse(responseCode = "401", description = "Not Authorized"),
        @ApiResponse(responseCode = "404", description = "User not found on case")})
    public ResponseEntity<List<String>> getUserInfo(
        @PathVariable("caseId") String caseId,
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) {
        return ResponseEntity.ok(userInformationService.getUserCaseRoles(caseId, authorization));
    }

    @PostMapping(path = "/{caseId}/courtDecision")
    @Operation(summary = "Calculates decision on proposed claimant repayment")
    public RepaymentDecisionType calculateDecisionOnClaimantProposedRepayment(
        @PathVariable("caseId") Long caseId,
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation,
        @RequestBody ClaimantProposedPlan claimantProposedPlan) {
        var caseDetails = coreCaseDataService.getCase(caseId, authorisation);
        return repaymentPlanDecisionService.getCalculatedDecision(caseDetails, claimantProposedPlan);
    }

}
