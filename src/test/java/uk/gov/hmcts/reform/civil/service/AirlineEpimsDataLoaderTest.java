package uk.gov.hmcts.reform.civil.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class AirlineEpimsDataLoaderTest {

    private AirlineEpimsDataLoader airlineEpimsDataLoader;

    @BeforeEach
    public void setUp() {
        airlineEpimsDataLoader = new AirlineEpimsDataLoader();
        airlineEpimsDataLoader.init();
    }

    @Test
    void shouldGetAnAirlineEpimsList() {
        String airline = airlineEpimsDataLoader.getAirlineEpimsIDList().get(0).getAirline();
        String epimsID = airlineEpimsDataLoader.getAirlineEpimsIDList().get(0).getEpimsID();
        assertEquals("Aegean", airline);
        assertEquals("298828", epimsID);
    }
}
