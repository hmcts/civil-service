package uk.gov.hmcts.reform.civil.controllers.cases;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.service.pininpost.DefendantPinToPostLRspecService;
import uk.gov.hmcts.reform.civil.service.search.CaseLegacyReferenceSearchService;

@Api
@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping(
    path = "/assignment",
    produces = MediaType.APPLICATION_JSON_VALUE
)
public class CaseAssignmentController {

    private final CaseLegacyReferenceSearchService caseByLegacyReferenceSearchService;
    private final DefendantPinToPostLRspecService defendantPinToPostLRspecService;

    @PostMapping(path = {
        "/reference/{caseReference}"
    })
    @ApiOperation("Validates case reference and pin")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK"),
        @ApiResponse(code = 401, message = "Not Authorized"),
        @ApiResponse(code = 400, message = "Bad Request")})
    public ResponseEntity<CaseDetails> validateCaseAndPin(
        @PathVariable("caseReference") String caseReference, @RequestBody String pin) {
        log.info("case reference {}", caseReference);
        CaseDetails caseDetails = caseByLegacyReferenceSearchService.getCaseDataByLegacyReference(caseReference);
        log.info("validating Pin {}", pin);
        defendantPinToPostLRspecService.validatePin(caseDetails, pin);
        return new ResponseEntity<>(caseDetails, HttpStatus.OK);
    }
}
