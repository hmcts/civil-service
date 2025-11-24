package uk.gov.hmcts.reform.civil.service.sdo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.helpers.LocationHelper;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.model.dq.RequestedCourt;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;
import uk.gov.hmcts.reform.civil.service.camunda.UpdateWaCourtLocationsService;
import uk.gov.hmcts.reform.civil.service.referencedata.LocationReferenceDataService;
import uk.gov.hmcts.reform.civil.utils.DynamicListUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static uk.gov.hmcts.reform.civil.model.common.DynamicListElement.dynamicElementFromCode;

@Service
@RequiredArgsConstructor
@Slf4j
public class SdoLocationService {

    private final LocationReferenceDataService locationReferenceDataService;
    private final LocationHelper locationHelper;
    private final Optional<UpdateWaCourtLocationsService> updateWaCourtLocationsService;

    public List<LocationRefData> fetchHearingLocations(String authToken) {
        return locationReferenceDataService.getHearingCourtLocations(authToken);
    }

    public List<LocationRefData> fetchDefaultJudgmentLocations(String authToken) {
        return locationReferenceDataService.getCourtLocationsForDefaultJudgments(authToken);
    }

    public DynamicList buildLocationList(RequestedCourt preferredCourt,
                                         boolean includeAllCourts,
                                         List<LocationRefData> locations) {
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

    public DynamicList buildCourtLocationForSdoR2(RequestedCourt preferredCourt,
                                                  List<LocationRefData> locations) {
        Optional<LocationRefData> matchingLocation = Optional.ofNullable(preferredCourt)
            .flatMap(requestedCourt -> locationHelper.getMatching(locations, preferredCourt));

        List<DynamicListElement> options = new ArrayList<>();
        DynamicListElement selected = matchingLocation
            .map(location -> dynamicElementFromCode(
                location.getEpimmsId(),
                LocationReferenceDataService.getDisplayEntry(location)
            ))
            .orElse(null);

        if (selected != null) {
            options.add(selected);
        }

        options.add(dynamicElementFromCode("OTHER_LOCATION", "Other location"));

        return DynamicList.builder()
            .listItems(options)
            .value(selected != null ? selected : DynamicListElement.EMPTY)
            .build();
    }

    public DynamicList trimListItems(DynamicList list) {
        return DynamicListUtils.trimToSelectedValue(list);
    }

    public void updateWaLocationsIfRequired(CaseData caseData,
                                            CaseData.CaseDataBuilder<?, ?> builder,
                                            String authToken) {
        log.info("Updating WA court locations if required for caseId {}", caseData.getCcdCaseReference());
        updateWaCourtLocationsService.ifPresent(service -> service.updateCourtListingWALocations(authToken, caseData));
    }

    public void clearWaLocationMetadata(CaseData.CaseDataBuilder<?, ?> builder) {
        log.info("Clearing WA location metadata");
        builder.taskManagementLocations(null)
            .taskManagementLocationsTab(null)
            .caseManagementLocationTab(null);
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
