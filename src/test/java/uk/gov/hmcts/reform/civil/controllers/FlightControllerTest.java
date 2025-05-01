package uk.gov.hmcts.reform.civil.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.civil.controllers.airlines.FlightController;
import uk.gov.hmcts.reform.civil.model.AirlineEpimsId;
import uk.gov.hmcts.reform.civil.service.AirlineEpimsDataLoader;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FlightControllerTest {

    @Mock
    private AirlineEpimsDataLoader airlineEpimsDataLoader;
    FlightController flightController;

    @BeforeEach
    public void setUp() {
        flightController = new FlightController(airlineEpimsDataLoader);
    }

    @Test
    void shouldReturnAirlines() {
        // Given
        List<AirlineEpimsId> airlineEpimsIDs = new ArrayList<>();
        AirlineEpimsId airlineEpimsID = AirlineEpimsId.builder()
            .airline("test")
            .epimsID("123")
            .build();
        airlineEpimsIDs.add(airlineEpimsID);
        when(airlineEpimsDataLoader.getAirlineEpimsIDList()).thenReturn(airlineEpimsIDs);

        // When
        ResponseEntity<List<AirlineEpimsId>> response = flightController.getAirlines();

        // Then
        assertNotNull(response.getBody());
        List<AirlineEpimsId> responseAirlines = response.getBody();
        assertEquals(airlineEpimsID, responseAirlines.get(0));
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}
