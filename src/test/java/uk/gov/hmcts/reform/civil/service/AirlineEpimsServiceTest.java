package uk.gov.hmcts.reform.civil.service;

import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.hmcts.reform.civil.model.AirlineEpimsId;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = {AirlineEpimsService.class})
public class AirlineEpimsServiceTest {

    @Mock
    private AirlineEpimsDataLoader airlineEpimsDataLoader;

    @InjectMocks
    private AirlineEpimsService airlineEpimsService;

    @Test
    public void getEpimsIdForAirline_shouldReturnCorrespondingEpimsIdForAirline() {
        setupAirlineEpimsList();

        String result = airlineEpimsService.getEpimsIdForAirline("GULF_AIR");

        assertThat(result).isEqualTo("36791");
    }

    @Test
    public void getEpimsIdForAirline_givenInvalidAirline_shouldThrowException() {
        setupAirlineEpimsList();

        assertThatExceptionOfType(IllegalStateException.class)
            .isThrownBy(() -> airlineEpimsService.getEpimsIdForAirline("INVALID_AIRLINE"));
    }

    private void setupAirlineEpimsList() {
        List<AirlineEpimsId> airlineEpimsIDList = new ArrayList<>();
        airlineEpimsIDList.add(AirlineEpimsId.builder().airline("GULF_AIR").epimsID("36791").build());

        when(airlineEpimsDataLoader.getAirlineEpimsIDList()).thenReturn(airlineEpimsIDList);
    }
}
