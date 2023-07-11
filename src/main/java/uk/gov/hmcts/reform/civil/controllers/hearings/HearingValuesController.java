package uk.gov.hmcts.reform.civil.controllers.hearings;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@Tag(name = "Hearing Values Controller")
@Slf4j
@RestController
@AllArgsConstructor
public class HearingValuesController {

    private final HearingValuesService hearingValuesService;

    @PostMapping(path = {
        "/serviceHearingValues"
    })
    @Operation(summary = "Builds and returns the hearing values for exui")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "401", description = "Incorrect authorisation"),
        @ApiResponse(responseCode = "400", description = "Bad Request")
    })
    public ResponseEntity<ServiceHearingValuesModel> getHearingValues(
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
        @NonNull @RequestBody HearingValuesRequest requestDetails) throws Exception {

        var hearingValues = hearingValuesService.getValues(
            requestDetails.getCaseReference(), requestDetails.getHearingId(), authorization);

        return new ResponseEntity<>(hearingValues, HttpStatus.OK);
    }
}

