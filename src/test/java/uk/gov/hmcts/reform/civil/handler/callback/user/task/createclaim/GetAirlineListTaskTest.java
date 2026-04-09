package uk.gov.hmcts.reform.civil.handler.callback.user.task.createclaim;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.AirlineEpimsId;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.AirlineEpimsDataLoader;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class GetAirlineListTaskTest extends BaseCallbackHandlerTest {

    @Mock
    private FeatureToggleService featureToggleService;

    @Mock
    private AirlineEpimsDataLoader airlineEpimsDataLoader;

    @InjectMocks
    private GetAirlineListTask getAirlineListTask;

    @BeforeEach
    public void setUp() {
        getAirlineListTask = new GetAirlineListTask(featureToggleService, airlineEpimsDataLoader, new ObjectMapper());
    }

    @Test
    void shouldGetAirlineList_whenRequired() {
        AirlineEpimsId airlineEpimsId = new AirlineEpimsId();
        airlineEpimsId.setAirline("BA/Cityflyer");
        airlineEpimsId.setEpimsID("111000");
        AirlineEpimsId airlineEpimsId1 = new AirlineEpimsId();
        airlineEpimsId1.setAirline("OTHER");
        airlineEpimsId1.setEpimsID("111111");
        List<AirlineEpimsId> airlineEpimsIDList = new ArrayList<>();
        airlineEpimsIDList.add(airlineEpimsId);
        airlineEpimsIDList.add(airlineEpimsId1);

        given(airlineEpimsDataLoader.getAirlineEpimsIDList())
            .willReturn(airlineEpimsIDList);

        CaseData caseData = CaseDataBuilder.builder().build();

        var response = (AboutToStartOrSubmitCallbackResponse) getAirlineListTask.getAirlineList(caseData);

        assertThat(response.getData()).extracting("flightDelayDetails").extracting("airlineList")
            .extracting("list_items").asList().extracting("label")
            .contains("BA/Cityflyer");

        assertThat(response.getData()).extracting("flightDelayDetails").extracting("airlineList")
            .extracting("list_items").asList().extracting("label")
            .contains("OTHER");
    }
}
