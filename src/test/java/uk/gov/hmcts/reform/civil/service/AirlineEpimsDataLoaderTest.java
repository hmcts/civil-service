package uk.gov.hmcts.reform.civil.service;

import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.Assert.assertEquals;

@SpringBootTest(classes = {AirlineEpimsDataLoader.class})
public class AirlineEpimsDataLoaderTest {

    private AirlineEpimsDataLoader airlineEpimsDataLoader;

    @Before
    public void setUp() {
        airlineEpimsDataLoader = new AirlineEpimsDataLoader();
        airlineEpimsDataLoader.init();
    }

    @Test
    public void shouldGetAnAirlineEpimsList() {
        String airline = airlineEpimsDataLoader.getAirlineEpimsIDList().get(1).getAirline();
        String epimsID = airlineEpimsDataLoader.getAirlineEpimsIDList().get(1).getEpimsID();
        assertEquals("airline", airline);
        assertEquals("111000", epimsID);
    }
}
