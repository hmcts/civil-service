package uk.gov.hmcts.reform.civil.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.model.AirlineEpimsId;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class AirlineEpimsServiceTest {

    @Mock
    private AirlineEpimsDataLoader airlineEpimsDataLoader;

    private AirlineEpimsService airlineEpimsService;

    @BeforeEach
    void setup() {
        List<AirlineEpimsId> airlineEpimsIDList = new ArrayList<>();
        airlineEpimsIDList.add(AirlineEpimsId.builder().airline("Gulf Air").epimsID("36791").build());
        airlineEpimsIDList.add(AirlineEpimsId.builder().airline("NoLocationAirline").build());

        given(airlineEpimsDataLoader.getAirlineEpimsIDList())
            .willReturn(airlineEpimsIDList);

        airlineEpimsService = new AirlineEpimsService(airlineEpimsDataLoader);

    }

    @Test
    void getEpimsIdForAirline_shouldReturnCorrespondingEpimsIdForAirline() {
        String result = airlineEpimsService.getEpimsIdForAirline("Gulf Air");

        assertThat(result).isEqualTo("36791");
    }

    @Test
    void getEpimsIdForAirline_givenInvalidAirline_shouldThrowException() {
        assertThatExceptionOfType(IllegalStateException.class)
            .isThrownBy(() -> airlineEpimsService.getEpimsIdForAirline("INVALID_AIRLINE"));
    }

    @Test
    void getEpimsIdForAirline_givenNoLocationAirline_shouldThrowException() {
        assertThatExceptionOfType(IllegalStateException.class)
            .isThrownBy(() -> airlineEpimsService.getEpimsIdForAirline("NoLocationAirline"));
    }

    @Test
    void getEpimsIdForAirlineIgnoreCase_shouldReturnCorrespondingEpimsIdForAirline() {
        String result = airlineEpimsService.getEpimsIdForAirlineIgnoreCase("gulf air");

        assertThat(result).isEqualTo("36791");
    }

    @Test
    void getEpimsIdForAirlineIgnoreCase_givenInvalidAirline_shouldThrowException() {
        String result = airlineEpimsService.getEpimsIdForAirlineIgnoreCase("INVALID_AIRLINE");

        assertThat(result).isNull();
    }

    @Test
    void getEpimsIdForAirlineIgnoreCase_givenNoLocationAirline_shouldThrowException() {
        String result = airlineEpimsService.getEpimsIdForAirlineIgnoreCase("NoLocationAirline");

        assertThat(result).isNull();
    }
}
