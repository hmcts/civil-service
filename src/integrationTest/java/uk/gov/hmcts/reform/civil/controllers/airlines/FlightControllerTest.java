package uk.gov.hmcts.reform.civil.controllers.airlines;

import com.opencsv.CSVReader;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.civil.controllers.BaseIntegrationTest;
import uk.gov.hmcts.reform.civil.model.AirlineEpimsId;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.service.AirlineEpimsDataLoader;
import uk.gov.hmcts.reform.civil.utils.CourtLocationUtils;

import javax.annotation.PostConstruct;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class FlightControllerTest extends BaseIntegrationTest {

    @MockBean
    private AirlineEpimsDataLoader airlineEpimsDataLoader;

    @Test
    @SneakyThrows
    void shouldReturnAirlineList() {
        List<AirlineEpimsId> airlineEpimsIDList = new ArrayList<>();

        AirlineEpimsId airlineEpimsID = AirlineEpimsId.builder()
            .airline("test")
            .epimsID("123")
            .build();
        airlineEpimsIDList.add(airlineEpimsID);

        when(airlineEpimsDataLoader.getAirlineEpimsIDList()).thenReturn(airlineEpimsIDList);

        doGet(BEARER_TOKEN, "/airlines")
            .andExpect(content().json(toJson(airlineEpimsIDList)))
            .andExpect(status().isOk());
    }
}
