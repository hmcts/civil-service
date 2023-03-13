package uk.gov.hmcts.reform.civil.controllers.cases;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.civil.model.HearingValuesRequest;
import uk.gov.hmcts.reform.civil.model.hearingvalues.ServiceHearingValuesModel;
import uk.gov.hmcts.reform.civil.service.hearings.HearingValuesService;

@Api
@Slf4j
@RestController
@AllArgsConstructor
public class HearingValuesController {

    private final HearingValuesService hearingValuesService;

    @PostMapping(path = {
        "/serviceHearingValues"
    })
    @ApiOperation("Builds and returns the hearing values for exui")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK"),
        @ApiResponse(code = 401, message = "Incorrect authorisation"),
        @ApiResponse(code = 400, message = "Bad Request")
    })
    public ResponseEntity<ServiceHearingValuesModel> getHearingValues(
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
        @NonNull @RequestBody HearingValuesRequest requestDetails) {

        var hearingValues = hearingValuesService.getValues(
            requestDetails.getCaseReference(), requestDetails.getHearingId());

        return new ResponseEntity<>(hearingValues, HttpStatus.OK);
    }
}

