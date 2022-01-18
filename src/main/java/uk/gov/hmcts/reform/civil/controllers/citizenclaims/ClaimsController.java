package uk.gov.hmcts.reform.civil.controllers.citizenclaims;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;

@Api
@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping(path = "/claims",
    produces = MediaType.APPLICATION_JSON_VALUE,
    consumes = MediaType.APPLICATION_JSON_VALUE
    )

public class ClaimsController {

    private final CoreCaseDataService coreCaseDataService;
    private final CaseDetailsConverter caseDetailsConverter;

    @GetMapping(path = "/{claimId}")
    @ApiOperation("Handles claim by claimId from CCD")
    public ResponseEntity<CaseData> getClaim( @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation, @PathVariable("claimId") long claimId){

        log.info("Received claimId" + claimId + "from CCD");
        CaseData caseData = caseDetailsConverter.toCaseData(coreCaseDataService.getCase(claimId));

        return new ResponseEntity<>(caseData, HttpStatus.OK);
    }
    @GetMapping(path = "/list")
    @ApiOperation("Handles all callbacks from CCD")
    public ResponseEntity<CaseData> getList( @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation){

        log.info("Received callback from CCD");
        //modify the service to get all claims instead of 1.
        long id=1641908933090158L;
        CaseData caseData = caseDetailsConverter.toCaseData(coreCaseDataService.getCase(id));

        return new ResponseEntity<>(caseData, HttpStatus.OK);
    }
}
