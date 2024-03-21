package uk.gov.hmcts.reform.civil.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.civil.model.AirlineEpimsId;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.BDDMockito.given;

@SpringBootTest(classes = {AirlineEpimsService.class})
class AirlineEpimsServiceTest {

    @MockBean
    private AirlineEpimsDataLoader airlineEpimsDataLoader;

    private AirlineEpimsService airlineEpimsService;

    @BeforeEach
    void setup() {
        List<AirlineEpimsId> airlineEpimsIDList = new ArrayList<>();
        airlineEpimsIDList.add(AirlineEpimsId.builder().airline("Gulf Air").epimsID("36791").build());
        airlineEpimsIDList.add(AirlineEpimsId.builder().airline("NoLocationAirline").build());

        given(airlineEpimsDataLoader.getAirlineEpimsIDList())
            .willReturn(airlineEpimsIDList);
    }

    @Test
    void getEpimsIdForAirline_shouldReturnCorrespondingEpimsIdForAirline() {
        // Given
        airlineEpimsService = new AirlineEpimsService(airlineEpimsDataLoader);

        // When
        String result = airlineEpimsService.getEpimsIdForAirline("Gulf Air");

        // Then
        assertThat(result).isEqualTo("36791");
    }

    @Test
    void getEpimsIdForAirline_givenInvalidAirline_shouldThrowException() {
        // Given
        airlineEpimsService = new AirlineEpimsService(airlineEpimsDataLoader);

        // Then
        assertThatExceptionOfType(IllegalStateException.class)
            .isThrownBy(() -> airlineEpimsService.getEpimsIdForAirline("INVALID_AIRLINE"));
    }

    @Test
    void getEpimsIdForAirline_givenNoLocationAirline_shouldThrowException() {
        // Given
        airlineEpimsService = new AirlineEpimsService(airlineEpimsDataLoader);

        // Then
        assertThatExceptionOfType(IllegalStateException.class)
            .isThrownBy(() -> airlineEpimsService.getEpimsIdForAirline("NoLocationAirline"));
    }
}
