package uk.gov.hmcts.reform.civil.handler.callback.user.task.createclaim;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.model.AirlineEpimsId;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.FlightDelayDetails;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.service.AirlineEpimsDataLoader;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import java.util.List;

@Component
public class GetAirlineListTask {

    private final FeatureToggleService featureToggleService;
    private final AirlineEpimsDataLoader airlineEpimsDataLoader;
    private final ObjectMapper objectMapper;

    @Autowired
    public GetAirlineListTask(FeatureToggleService featureToggleService,
                              AirlineEpimsDataLoader airlineEpimsDataLoader, ObjectMapper objectMapper) {
        this.featureToggleService = featureToggleService;
        this.airlineEpimsDataLoader = airlineEpimsDataLoader;
        this.objectMapper = objectMapper;
    }

    public CallbackResponse getAirlineList(CaseData caseData) {
        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = caseData.toBuilder();

        if (featureToggleService.isSdoR2Enabled()) {
            DynamicList airlineDropdownList = createAirlineDropdownList();
            FlightDelayDetails flightDelayDetails = buildFlightDelayDetails(airlineDropdownList);
            caseDataBuilder.flightDelayDetails(flightDelayDetails);
        }

        return buildCallbackResponse(caseDataBuilder);
    }

    private DynamicList createAirlineDropdownList() {
        List<AirlineEpimsId> airlineEpimsIDList = airlineEpimsDataLoader.getAirlineEpimsIDList();

        return DynamicList.fromList(
            airlineEpimsIDList.stream()
                .map(AirlineEpimsId::getAirline)
                .toList(),
            Object::toString,
            Object::toString,
            null,
            false
        );
    }

    private FlightDelayDetails buildFlightDelayDetails(DynamicList airlineDropdownList) {
        return FlightDelayDetails.builder()
            .airlineList(DynamicList.builder()
                             .listItems(airlineDropdownList.getListItems())
                             .build())
            .build();
    }

    private CallbackResponse buildCallbackResponse(CaseData.CaseDataBuilder<?, ?> caseDataBuilder) {
        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataBuilder.build().toMap(objectMapper))
            .build();
    }
}
