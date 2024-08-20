package uk.gov.hmcts.reform.civil.handler.callback.user.task.createClaimSpecCallbackHanderTask;

import com.fasterxml.jackson.databind.ObjectMapper;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.model.AirlineEpimsId;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.FlightDelayDetails;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.service.AirlineEpimsDataLoader;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import java.util.ArrayList;
import java.util.List;

public class GetAirlineListTask {

    private final FeatureToggleService featureToggleService;
    private final AirlineEpimsDataLoader airlineEpimsDataLoader;
    private final ObjectMapper objectMapper;

    public GetAirlineListTask(FeatureToggleService featureToggleService, AirlineEpimsDataLoader airlineEpimsDataLoader, ObjectMapper objectMapper) {
        this.featureToggleService = featureToggleService;
        this.airlineEpimsDataLoader = airlineEpimsDataLoader;
        this.objectMapper = objectMapper;
    }

    public CallbackResponse getAirlineList(CaseData caseData) {
        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = caseData.toBuilder();
        if (featureToggleService.isSdoR2Enabled()) {
            List<AirlineEpimsId> airlineEpimsIDList = new ArrayList<>(airlineEpimsDataLoader.getAirlineEpimsIDList());
            DynamicList airlineList = DynamicList
                .fromList(
                    airlineEpimsIDList.stream()
                        .map(AirlineEpimsId::getAirline).toList(),
                    Object::toString,
                    Object::toString,
                    null,
                    false
                );
            DynamicList dropdownAirlineList = DynamicList.builder()
                .listItems(airlineList.getListItems()).build();

            FlightDelayDetails flightDelayDetails = FlightDelayDetails.builder().airlineList(dropdownAirlineList).build();
            caseDataBuilder.flightDelayDetails(flightDelayDetails);
        }
        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataBuilder.build().toMap(objectMapper))
            .build();
    }
}
