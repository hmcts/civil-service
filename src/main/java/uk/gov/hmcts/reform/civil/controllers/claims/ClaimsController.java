package uk.gov.hmcts.reform.civil.controllers.claims;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.callback.CallbackHandlerFactory;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;

@Api
@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping(
    path = "/claims",
    produces = MediaType.APPLICATION_JSON_VALUE,
    consumes = MediaType.APPLICATION_JSON_VALUE
)
public class ClaimsController {

    private final CoreCaseDataService coreCaseDataService;
    private final CaseDetailsConverter caseDetailsConverter;
    @GetMapping(path = {
        "/{claimsId}",
    })
    @ApiOperation("Handles all callbacks from CCD")

    public String getClaims(
        @PathVariable("claimsId") Long claimId,
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation
        ) {
        log.info(
            "Received ClaimId: {}",
            claimId
        );

        var response = caseDetailsConverter.toCaseData(coreCaseDataService.getCase(claimId));
        log.info("ClaimId: {}", response.toString());
        return response.toString();
    }
}
