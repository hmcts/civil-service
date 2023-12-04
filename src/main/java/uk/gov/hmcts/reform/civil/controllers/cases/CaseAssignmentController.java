package uk.gov.hmcts.reform.civil.controllers.cases;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.enums.CaseRole;
import uk.gov.hmcts.reform.civil.model.citizenui.dto.PinDto;
import uk.gov.hmcts.reform.civil.service.AssignCaseService;
import uk.gov.hmcts.reform.civil.service.citizen.defendant.LipDefendantCaseAssignmentService;
import uk.gov.hmcts.reform.civil.service.pininpost.DefendantPinToPostLRspecService;
import uk.gov.hmcts.reform.civil.service.search.CaseLegacyReferenceSearchService;

import java.util.Optional;

@Tag(name = "Case Assignment Controller")
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
    private final AssignCaseService assignCaseService;
    private final LipDefendantCaseAssignmentService lipDefendantCaseAssignmentService;
    private static final int OCMC_PIN_LENGTH = 8;

    @PostMapping(path = {
        "/reference/{caseReference}"
    })
    @Operation(summary = "Validates case reference and pin")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "401", description = "Not Authorized"),
        @ApiResponse(responseCode = "400", description = "Bad Request")})
    public ResponseEntity<CaseDetails> validateCaseAndPin(
        @PathVariable("caseReference") String caseReference, @RequestBody PinDto pin) {
        log.info("case reference {}", caseReference);
        CaseDetails caseDetails = null;
        if (pin.getPin().length() != OCMC_PIN_LENGTH) {
            caseDetails = caseByLegacyReferenceSearchService.getCaseDataByLegacyReference(caseReference);
        }
        defendantPinToPostLRspecService.validatePin(caseDetails, pin.getPin());
        return new ResponseEntity<>(caseDetails, HttpStatus.OK);
    }

    @PostMapping(path = "/case/{caseId}/{caseRole}")
    @Operation(summary = "Assigns case to defendant")
    public void assignCaseToDefendant(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation,
                                      @PathVariable("caseId") String caseId,
                                      @PathVariable("caseRole") Optional<CaseRole> caseRole) {
        log.info("assigning case with id: {}", caseId);
        assignCaseService.assignCase(authorisation, caseId, caseRole);
        lipDefendantCaseAssignmentService.addLipDefendantToCaseDefendantUserDetails(authorisation, caseId);
    }

}
