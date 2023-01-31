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
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.citizenui.dto.PinDto;
import uk.gov.hmcts.reform.civil.service.docmosis.pip.PiPLetterGenerator;
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
    private final PiPLetterGenerator piPLetterGenerator;

    @PostMapping(path = {
        "/reference/{caseReference}"
    })
    @ApiOperation("Validates case reference and pin")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK"),
        @ApiResponse(code = 401, message = "Not Authorized"),
        @ApiResponse(code = 400, message = "Bad Request")})
    public ResponseEntity<CaseDetails> validateCaseAndPin(
        @PathVariable("caseReference") String caseReference, @RequestBody PinDto pin) {
        log.info("case reference {}", caseReference);
        CaseDetails caseDetails = caseByLegacyReferenceSearchService.getCaseDataByLegacyReference(caseReference);
        defendantPinToPostLRspecService.validatePin(caseDetails, pin.getPin());
        return new ResponseEntity<>(caseDetails, HttpStatus.OK);
    }

    @PostMapping(value = "/pipLetter", produces = MediaType.APPLICATION_PDF_VALUE)
    public @ResponseBody
    byte[] downloadLetter(@RequestBody CaseData caseData) {
        return piPLetterGenerator.downloadLetter(caseData);
    }
}
