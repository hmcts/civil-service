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
import uk.gov.hmcts.reform.civil.handler.callback.user.task.createclaim.GetAirlineListTask;
import uk.gov.hmcts.reform.civil.model.AirlineEpimsId;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.AirlineEpimsDataLoader;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;

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
        List<AirlineEpimsId> airlineEpimsIDList = new ArrayList<>();
        airlineEpimsIDList.add(AirlineEpimsId.builder().airline("BA/Cityflyer").epimsID("111000").build());
        airlineEpimsIDList.add(AirlineEpimsId.builder().airline("OTHER").epimsID("111111").build());

        given(airlineEpimsDataLoader.getAirlineEpimsIDList())
            .willReturn(airlineEpimsIDList);

        CaseData caseData = CaseData.builder().build();

        when(featureToggleService.isSdoR2Enabled()).thenReturn(true);
        var response = (AboutToStartOrSubmitCallbackResponse) getAirlineListTask.getAirlineList(caseData);

        assertThat(response.getData()).extracting("flightDelayDetails").extracting("airlineList")
            .extracting("list_items").asList().extracting("label")
            .contains("BA/Cityflyer");

        assertThat(response.getData()).extracting("flightDelayDetails").extracting("airlineList")
            .extracting("list_items").asList().extracting("label")
            .contains("OTHER");
    }
}
