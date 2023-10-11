package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.helpers.LocationHelper;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.transferonlinecase.TocNewCourtLocation;
import uk.gov.hmcts.reform.civil.referencedata.LocationRefDataService;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;
import uk.gov.hmcts.reform.civil.utils.CourtLocationUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static java.util.Objects.nonNull;
import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.MID;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.TRANSFER_ONLINE_CASE;
import static uk.gov.hmcts.reform.civil.model.common.DynamicList.fromList;

@Service
@RequiredArgsConstructor
public class TransferOnlineCaseCallbackHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = Collections.singletonList(TRANSFER_ONLINE_CASE);
    protected final ObjectMapper objectMapper;
    private final LocationRefDataService locationRefDataService;
    private final CourtLocationUtils courtLocationUtils;
    private static final String ERROR_SELECT_DIFF_LOCATION = "Select a different hearing court location to transfer!";

    @Override
    protected Map<String, Callback> callbacks() {
        return new ImmutableMap.Builder<String, Callback>()
            .put(callbackKey(ABOUT_TO_START), this::locationList)
            .put(callbackKey(MID, "validate-court-location"), this::validateCourtLocation)
            .put(callbackKey(ABOUT_TO_SUBMIT), this::saveTransferOnlineCase)
            .put(callbackKey(SUBMITTED), this::buildConfirmation)
            .build();
    }

    private CallbackResponse validateCourtLocation(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = caseData.toBuilder();
        List<String> errors = new ArrayList<>();

        if (ifSameCourtSelected(callbackParams)) {
            errors.add(ERROR_SELECT_DIFF_LOCATION);
        }
        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataBuilder.build().toMap(objectMapper))
            .errors(errors)
            .build();
    }

    private CallbackResponse buildConfirmation(CallbackParams callbackParams) {
        return SubmittedCallbackResponse.builder()
            .confirmationHeader(getHeader())
            .confirmationBody(getBody())
            .build();
    }

    private CallbackResponse locationList(CallbackParams callbackParams) {
        var caseData = callbackParams.getCaseData();
        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = caseData.toBuilder();
        List<LocationRefData> locations = fetchLocationData(callbackParams);
        caseDataBuilder.tocNewCourtLocation(TocNewCourtLocation.builder().responseCourtLocationList(getLocationsFromList(locations)).build());

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataBuilder.build().toMap(objectMapper))
            .build();
    }

    private DynamicList getLocationsFromList(final List<LocationRefData> locations) {
        return fromList(locations.stream().map(location -> new StringBuilder().append(location.getSiteName())
                .append(" - ").append(location.getCourtAddress())
                .append(" - ").append(location.getPostcode()).toString())
                            .collect(Collectors.toList()));
    }

    private String getHeader() {
        return format("# Case transferred to new location");
    }

    private String getBody() {
        return format("# Case transferred to new location");
    }

    private CallbackResponse saveTransferOnlineCase(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = caseData.toBuilder();
        LocationRefData newCourtLocation = courtLocationUtils.findPreferredLocationData(
            fetchLocationData(callbackParams),
            callbackParams.getCaseData().getTocNewCourtLocation().getResponseCourtLocationList());
        if (nonNull(newCourtLocation)) {
            caseDataBuilder.caseManagementLocation(LocationHelper.buildCaseLocation(newCourtLocation));
            caseDataBuilder.locationName(newCourtLocation.getSiteName());
        }
        DynamicList locationList = caseData.getTocNewCourtLocation().getResponseCourtLocationList();
        locationList.setListItems(null);
        caseDataBuilder.tocNewCourtLocation(TocNewCourtLocation.builder()
                                                .responseCourtLocationList(locationList)
                                                .reasonForTransfer(caseData.getTocNewCourtLocation().getReasonForTransfer()).build());
        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataBuilder.build().toMap(objectMapper))
            .build();
    }

    private boolean ifSameCourtSelected(CallbackParams callbackParams) {
        LocationRefData newCourtLocation = courtLocationUtils.findPreferredLocationData(
            fetchLocationData(callbackParams),
            callbackParams.getCaseData().getTocNewCourtLocation().getResponseCourtLocationList());
        LocationRefData caseManagementLocation =
            getLocationRefData(callbackParams);
        if (caseManagementLocation != null && newCourtLocation.getCourtLocationCode().equals(caseManagementLocation.getCourtLocationCode())) {
            return true;
        }
        return false;
    }

    private LocationRefData getLocationRefData(CallbackParams callbackParams) {
        List<LocationRefData> locations = fetchLocationData(callbackParams);
        var matchedLocations =  locations.stream().filter(loc -> loc.getEpimmsId().equals(callbackParams.getCaseData().getCaseManagementLocation().getBaseLocation())).toList();
        return matchedLocations.size() > 0 ? matchedLocations.get(0) : null;
    }

    private List<LocationRefData> fetchLocationData(CallbackParams callbackParams) {
        String authToken = callbackParams.getParams().get(BEARER_TOKEN).toString();
        return locationRefDataService.getCourtLocationsForDefaultJudgments(authToken);
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }
}
