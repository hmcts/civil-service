package uk.gov.hmcts.reform.civil.service.camunda;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.model.CaseData;

import uk.gov.hmcts.reform.civil.model.dmnWaCourtTaskLocation.DmnListingLocations;
import uk.gov.hmcts.reform.civil.model.dmnWaCourtTaskLocation.TaskManagementLocationTypes;
import uk.gov.hmcts.reform.civil.model.dmnWaCourtTaskLocation.TaskManagementLocationsModel;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;
import uk.gov.hmcts.reform.civil.service.referencedata.LocationReferenceDataService;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.enums.CaseCategory.SPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.CaseCategory.UNSPEC_CLAIM;

@RequiredArgsConstructor
@Service
@Slf4j
public class UpdateWaCourtLocationsService {

    private final CamundaRuntimeClient camundaRuntimeClient;
    private final ObjectMapper objectMapper;
    private final LocationReferenceDataService locationRefDataService;

    public void updateCourtListingWALocations(String courtId, CaseData caseData, String authorisation, CaseData.CaseDataBuilder<?, ?> caseDataBuilder) {

        Map<String, Object> evaluatedCourtMap = camundaRuntimeClient.getEvaluatedDmnCourtLocations(courtId, getClaimTrack(caseData));
        DmnListingLocations dmnListingLocations = objectMapper.convertValue(evaluatedCourtMap, DmnListingLocations.class);
        List<LocationRefData> locationRefDataList = locationRefDataService.getHearingCourtLocations(authorisation);

        try {
            LocationRefData cmcListing = courtLocationDetails(locationRefDataList,
                                                              dmnListingLocations.getCmcListingLocation().getValue(), "Cmc Listing");
            LocationRefData ccmcListing = courtLocationDetails(locationRefDataList,
                                                               dmnListingLocations.getCcmcListingLocation().getValue(), "Ccmc Listing");
            LocationRefData preTrialListing = courtLocationDetails(locationRefDataList,
                                                                   dmnListingLocations.getPtrListingLocation().getValue(), "Pre trial Listing");
            LocationRefData trialListing = courtLocationDetails(locationRefDataList,
                                                                dmnListingLocations.getTrialListingLocation().getValue(), "Trial Listing");

            caseDataBuilder.taskManagementLocations(TaskManagementLocationTypes.builder()
                                                        .cmcListingLocation(TaskManagementLocationsModel.builder()
                                                                                .region(cmcListing.getRegionId())
                                                                                .regionName(cmcListing.getRegion())
                                                                                .location(cmcListing.getEpimmsId())
                                                                                .locationName(cmcListing.getVenueName())
                                                                                .build())
                                                        .ccmcListingLocation(TaskManagementLocationsModel.builder()
                                                                                 .region(ccmcListing.getRegionId())
                                                                                 .regionName(ccmcListing.getRegion())
                                                                                 .location(ccmcListing.getEpimmsId())
                                                                                 .locationName(ccmcListing.getVenueName())
                                                                                 .build())
                                                        .ptrListingLocation(TaskManagementLocationsModel.builder()
                                                                                .region(preTrialListing.getRegionId())
                                                                                .regionName(preTrialListing.getRegion())
                                                                                .location(preTrialListing.getEpimmsId())
                                                                                .locationName(preTrialListing.getVenueName())
                                                                                .build())
                                                        .trialListingLocation(TaskManagementLocationsModel.builder()
                                                                                  .region(trialListing.getRegionId())
                                                                                  .regionName(trialListing.getRegion())
                                                                                  .location(trialListing.getEpimmsId())
                                                                                  .locationName(trialListing.getVenueName())
                                                                                  .build())
                                                        .build());

        } catch (NullPointerException e) {
            log.info("Court epimmId missing");
        }
    }

    private LocationRefData courtLocationDetails(List<LocationRefData> locationRefDataList, String court, String courtType) {
        LocationRefData courtTypeLocationDetails;
        var foundLocations = locationRefDataList.stream()
            .filter(location -> location.getEpimmsId().equals(court)).toList();
        if (!foundLocations.isEmpty()) {
            courtTypeLocationDetails = foundLocations.get(0);
        } else {
            throw new IllegalArgumentException("Court Location not found, in location data for court type " + courtType);
        }
        return courtTypeLocationDetails;
    }

    private String getClaimTrack(CaseData caseData) {
        if (caseData.getCaseAccessCategory().equals(UNSPEC_CLAIM)) {
            return caseData.getAllocatedTrack().name();
        } else if (caseData.getCaseAccessCategory().equals(SPEC_CLAIM)) {
            return caseData.getResponseClaimTrack();
        }
        return null;
    }
}
