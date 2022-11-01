package uk.gov.hmcts.reform.civil.controllers.locations;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
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
import uk.gov.hmcts.reform.civil.service.referencedata.LocationRefDataService;
import uk.gov.hmcts.reform.civil.utils.CourtLocationUtils;

import java.util.List;

@Api
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
    @ApiOperation("Gets court locations")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK"),
        @ApiResponse(code = 401, message = "Not Authorized")})
    public ResponseEntity<List<DynamicListElement>> getCourtLocations(
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) {
        return new ResponseEntity<>(courtLocationUtils.getLocationsFromList(
            locationRefDataService.getCourtLocationsForDefaultJudgments(authorization)).getListItems(), HttpStatus.OK);
    }
}
