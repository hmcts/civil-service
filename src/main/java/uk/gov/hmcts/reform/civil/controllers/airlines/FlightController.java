package uk.gov.hmcts.reform.civil.controllers.airlines;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
<<<<<<< Updated upstream
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
=======
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
>>>>>>> Stashed changes
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.civil.model.AirlineEpimsId;
import uk.gov.hmcts.reform.civil.service.AirlineEpimsDataLoader;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
@RequestMapping(
    path = "/airlines",
    produces = MediaType.APPLICATION_JSON_VALUE
)
public class FlightController {

    private final AirlineEpimsDataLoader airlineEpimsDataLoader;

    @Autowired
    public FlightController(AirlineEpimsDataLoader airlineEpimsDataLoader) {
        this.airlineEpimsDataLoader = airlineEpimsDataLoader;
    }

    @GetMapping(path = {""})
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
