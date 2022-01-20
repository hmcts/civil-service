package uk.gov.hmcts.reform.civil.controllers.claims;

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
import static java.util.Collections.emptyList;
@Api
@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping(
    path = "/claims",
    produces = MediaType.APPLICATION_JSON_VALUE
)
public class ClaimsController {

    private final CoreCaseDataService coreCaseDataService;
    private final CaseDetailsConverter caseDetailsConverter;

    @GetMapping(path = {
        "/{claimId}",
    })
    @ApiOperation("get claim by id from CCD")

    public ResponseEntity<CaseData> getClaimById(
        @PathVariable("claimId") Long claimId
    ) {
        log.info(
            "Received ClaimId: {}",
            claimId
        );

        var caseDataResponse = caseDetailsConverter.toCaseData(coreCaseDataService.getCase(claimId));
        log.info(
            "CaseDataResponse : {}",
            caseDataResponse
        );
        return new ResponseEntity<>(caseDataResponse, HttpStatus.OK);
    }
    @GetMapping(path = "/list")
    @ApiOperation("Handles all callbacks from CCD")
    public ResponseEntity<SearchResult> getList(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization){

        log.info("Received callback from CCD");

        Query query = new Query(QueryBuilders.matchQuery("case_type_id", "CIVIL"), emptyList(), 0);
        SearchResult claims = coreCaseDataService.searchCases(query);

        return new ResponseEntity<>(claims, HttpStatus.OK);
    }
}
