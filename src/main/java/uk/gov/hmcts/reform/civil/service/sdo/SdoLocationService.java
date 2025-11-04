package uk.gov.hmcts.reform.civil.service.sdo;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.helpers.LocationHelper;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.model.dq.RequestedCourt;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;
import uk.gov.hmcts.reform.civil.service.camunda.UpdateWaCourtLocationsService;
import uk.gov.hmcts.reform.civil.service.referencedata.LocationReferenceDataService;
import uk.gov.hmcts.reform.civil.utils.DynamicListUtils;

import java.util.List;
import java.util.Optional;

import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.model.common.DynamicListElement.dynamicElementFromCode;

@Service
@RequiredArgsConstructor
public class SdoLocationService {

    private final LocationReferenceDataService locationReferenceDataService;
    private final LocationHelper locationHelper;
    private final Optional<UpdateWaCourtLocationsService> updateWaCourtLocationsService;

    public DynamicList buildLocationList(CallbackParams callbackParams, RequestedCourt preferredCourt) {
        return buildLocationList(callbackParams, preferredCourt, false);
    }

    public DynamicList buildLocationList(CallbackParams callbackParams, RequestedCourt preferredCourt, boolean includeAllCourts) {
        String authToken = callbackParams.getParams().get(BEARER_TOKEN).toString();
        List<LocationRefData> locations = locationReferenceDataService.getCourtLocationsForDefaultJudgments(authToken);
        Optional<LocationRefData> matchingLocation = Optional.ofNullable(preferredCourt)
            .flatMap(requestedCourt -> locationHelper.getMatching(locations, preferredCourt));

        if (includeAllCourts) {
            matchingLocation = Optional.empty();
        }

        return matchingLocation
            .map(location -> DynamicList.fromList(
                locations,
                this::getLocationEpimms,
                LocationReferenceDataService::getDisplayEntry,
                location,
                true))
            .orElseGet(() -> DynamicList.fromList(
                locations,
                this::getLocationEpimms,
                LocationReferenceDataService::getDisplayEntry,
                null,
                true
            ));
    }

    public DynamicList trimListItems(DynamicList list) {
        return DynamicListUtils.trimToSelectedValue(list);
    }

    public void updateWaLocationsIfRequired(CaseData caseData, CaseData.CaseDataBuilder<?, ?> builder, String authToken) {
        updateWaCourtLocationsService.ifPresent(service -> service.updateCourtListingWALocations(authToken, builder));
    }

    public DynamicList buildAlternativeCourtLocations(List<LocationRefData> locations) {
        List<DynamicListElement> options = locations.stream()
            .map(location -> dynamicElementFromCode(location.getEpimmsId(), LocationReferenceDataService.getDisplayEntry(location)))
            .toList();
        return DynamicList.fromDynamicListElementList(options);
    }

    private String getLocationEpimms(LocationRefData location) {
        return location.getEpimmsId();
    }
}
