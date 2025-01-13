package uk.gov.hmcts.reform.civil.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseEventDetail;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.genapplication.CaseLocationCivil;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;
import uk.gov.hmcts.reform.civil.service.referencedata.LocationReferenceDataService;

import java.util.List;

import static uk.gov.hmcts.reform.civil.callback.CaseEvent.TRANSFER_ONLINE_CASE;

@Service
@RequiredArgsConstructor
@Slf4j
public class LocationService {

    private final LocationReferenceDataService locationRefDataService;
    private final CoreCaseEventDataService coreCaseEventDataService;

    public Pair<CaseLocationCivil, Boolean> getWorkAllocationLocation(CaseData caseData, String authToken) {
        if (hasSDOBeenMade(caseData.getCcdState())) {
            return Pair.of(assignCaseManagementLocationToMainCaseLocation(caseData, authToken), false);
        } else {
            return getWorkAllocationLocationBeforeSdo(caseData, authToken);
        }
    }

    private Pair<CaseLocationCivil, Boolean> getWorkAllocationLocationBeforeSdo(CaseData caseData, String authToken) {
        List<CaseEventDetail> caseEventDetails = coreCaseEventDataService.getEventsForCase(caseData.getCcdCaseReference().toString());
        List<String> currentEvents = caseEventDetails.stream().map(CaseEventDetail::getId).toList();
        CaseLocationCivil courtLocation;
        if (currentEvents.contains(TRANSFER_ONLINE_CASE.name())) {
            courtLocation = assignCaseManagementLocationToMainCaseLocation(caseData, authToken);
            return Pair.of(courtLocation, true);
        } else {
            LocationRefData cnbcLocation = locationRefDataService.getCnbcLocation(authToken);
            courtLocation = CaseLocationCivil.builder()
                .region(cnbcLocation.getRegionId())
                .baseLocation(cnbcLocation.getEpimmsId())
                .siteName(cnbcLocation.getSiteName())
                .address(cnbcLocation.getCourtAddress())
                .postcode(cnbcLocation.getPostcode())
                .build();
        }
        return Pair.of(courtLocation, true);
    }

    private boolean hasSDOBeenMade(CaseState state) {
        return !CaseState.statesBeforeSDO.contains(state);
    }

    public LocationRefData getWorkAllocationLocationDetails(String baseLocation, String authToken) {
        List<LocationRefData> locationDetails = locationRefDataService.getCourtLocationsByEpimmsId(authToken, baseLocation);
        if (locationDetails != null && !locationDetails.isEmpty()) {
            return locationDetails.get(0);
        } else {
            return LocationRefData.builder().build();
        }
    }

    private CaseLocationCivil assignCaseManagementLocationToMainCaseLocation(CaseData caseData, String authToken) {
        LocationRefData caseManagementLocationDetails;
        List<LocationRefData>  locationRefDataList = locationRefDataService.getHearingCourtLocations(authToken);
        var foundLocations = locationRefDataList.stream()
            .filter(location -> location.getEpimmsId().equals(caseData.getCaseManagementLocation().getBaseLocation())).toList();
        if (!foundLocations.isEmpty()) {
            caseManagementLocationDetails = foundLocations.get(0);
        } else {
            throw new IllegalArgumentException("Base Court Location for General applications not found, in location data");
        }
        CaseLocationCivil courtLocation;
        courtLocation = CaseLocationCivil.builder()
            .region(caseManagementLocationDetails.getRegionId())
            .baseLocation(caseManagementLocationDetails.getEpimmsId())
            .siteName(caseManagementLocationDetails.getSiteName())
            .address(caseManagementLocationDetails.getCourtAddress())
            .postcode(caseManagementLocationDetails.getPostcode())
            .build();
        return courtLocation;
    }
}
