package uk.gov.hmcts.reform.civil.controllers.airlines;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.civil.model.AirlineEpimsId;
import uk.gov.hmcts.reform.civil.service.AirlineEpimsDataLoader;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
@AllArgsConstructor
public class FlightController {

    private final AirlineEpimsDataLoader airlineEpimsDataLoader;

    @GetMapping(path = {"/airlines"}, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get airlines")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "401", description = "Not Authorized"),
        @ApiResponse(responseCode = "400", description = "Bad Request")})
    public ResponseEntity<List<AirlineEpimsId>> getAirlines() {
        log.info("Get airlines");
        List<AirlineEpimsId> airlineEpimsIDList = new ArrayList<>(airlineEpimsDataLoader.getAirlineEpimsIDList());

        return new ResponseEntity<>(airlineEpimsIDList, HttpStatus.OK);
    }

}
