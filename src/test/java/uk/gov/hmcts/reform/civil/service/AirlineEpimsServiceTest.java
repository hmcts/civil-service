package uk.gov.hmcts.reform.civil.service;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.civil.model.AirlineEpimsId;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = {AirlineEpimsService.class})
class AirlineEpimsServiceTest {

    @MockBean
    private AirlineEpimsDataLoader airlineEpimsDataLoader;

    @InjectMocks
    private AirlineEpimsService airlineEpimsService;

    @Test
    void getEpimsIdForAirline_shouldReturnCorrespondingEpimsIdForAirline() {
        setup();
        String result = airlineEpimsService.getEpimsIdForAirline("Gulf Air");

        assertThat(result).isEqualTo("36791");
    }

    @Test
    void getEpimsIdForAirline_givenInvalidAirline_shouldReturnNull() {
        setup();
        String result = airlineEpimsService.getEpimsIdForAirline("INVALID_AIRLINE");

        assertThat(result).isNull();
    }

    @Test
    void getEpimsIdForAirline_givenInvalidAirline_shouldThrowException() {
        assertThatExceptionOfType(IllegalStateException.class)
            .isThrownBy(() -> airlineEpimsService.getEpimsIdForAirline("NoLocationAirline"));
    }

    void setup () {
        List<AirlineEpimsId> airlineEpimsIDList = new ArrayList<>();
        airlineEpimsIDList.add(AirlineEpimsId.builder().airline("Gulf Air").epimsID("36791").build());
        airlineEpimsIDList.add(AirlineEpimsId.builder().airline("NoLocationAirline").build());

        when(airlineEpimsDataLoader.getAirlineEpimsIDList()).thenReturn(airlineEpimsIDList);
    }
}
