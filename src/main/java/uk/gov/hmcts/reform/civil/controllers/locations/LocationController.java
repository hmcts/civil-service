package uk.gov.hmcts.reform.civil.controllers.locations;

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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.referencedata.LocationRefDataService;
import uk.gov.hmcts.reform.civil.utils.CourtLocationUtils;

import java.util.List;

@Tag(name = "Location Controller")
@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping(
    path = "/locations",
    produces = MediaType.APPLICATION_JSON_VALUE
)
public class LocationController {

    private final LocationRefDataService locationRefDataService;

    private final CourtLocationUtils courtLocationUtils;

    @GetMapping(path = "/courtLocations")
    @Operation(summary = "Gets court locations")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "401", description = "Not Authorized")})
    public ResponseEntity<List<DynamicListElement>> getCourtLocations(
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) {
        return new ResponseEntity<>(courtLocationUtils.getLocationsFromList(
            locationRefDataService.getCourtLocationsForDefaultJudgments(authorization)).getListItems(), HttpStatus.OK);
    }
}
