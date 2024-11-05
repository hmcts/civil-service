package uk.gov.hmcts.reform.civil.service.camunda;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.model.CaseData;

import uk.gov.hmcts.reform.civil.model.caseprogression.SecondaryListingLocations;
import uk.gov.hmcts.reform.civil.model.finalorders.SecondaryWaCaseLocationCivil;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;
import uk.gov.hmcts.reform.civil.service.referencedata.LocationReferenceDataService;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Service
@Slf4j
public class UpdateSecondaryWALocationsService {

    private final CamundaRuntimeClient camundaRuntimeClient;
    private final ObjectMapper objectMapper;
    private final LocationReferenceDataService locationRefDataService;

    public void updateSecondaryWALocations(String courtId, String authorisation, CaseData.CaseDataBuilder<?, ?> caseDataBuilder) {
        Map<String, Object> secondaryCourtMap = camundaRuntimeClient.getEvaluatedDmnCourtLocations(courtId);

        SecondaryListingLocations secondaryLocations = objectMapper.convertValue(secondaryCourtMap, SecondaryListingLocations.class);
        List<LocationRefData> locationRefDataList = locationRefDataService.getHearingCourtLocations(authorisation);
        log.info("hearing court list size {} with entries {}", locationRefDataList.size(), locationRefDataList);

        try {
            LocationRefData cmcListing = courtLocationDetails(locationRefDataList, secondaryLocations.getCmcListingLocation().getValue(), "Cmc Listing");
            LocationRefData ccmcListing = courtLocationDetails(locationRefDataList, secondaryLocations.getCcmcListingLocation().getValue(), "Ccmc Listing");
            LocationRefData preTrialListing = courtLocationDetails(locationRefDataList, secondaryLocations.getPtrListingLocation().getValue(), "Pre trial Listing");
            LocationRefData trialListing = courtLocationDetails(locationRefDataList, secondaryLocations.getTrialListingLocation().getValue(), "Trial Listing");

            caseDataBuilder.cmcListingLocation(SecondaryWaCaseLocationCivil.builder()
                                                   .region(cmcListing.getRegionId())
                                                   .regionName(cmcListing.getRegion())
                                                   .location(cmcListing.getEpimmsId())
                                                   .locationName(cmcListing.getVenueName())
                                                   .build());
            caseDataBuilder.ccmcListingLocation(SecondaryWaCaseLocationCivil.builder()
                                                    .region(ccmcListing.getRegionId())
                                                    .regionName(ccmcListing.getRegion())
                                                    .location(ccmcListing.getEpimmsId())
                                                    .locationName(ccmcListing.getVenueName())
                                                    .build());
            caseDataBuilder.preTrialListingLocation(SecondaryWaCaseLocationCivil.builder()
                                                        .region(preTrialListing.getRegionId())
                                                        .regionName(preTrialListing.getRegion())
                                                        .location(preTrialListing.getEpimmsId())
                                                        .locationName(preTrialListing.getVenueName())
                                                        .build());
            caseDataBuilder.trialListingLocation(SecondaryWaCaseLocationCivil.builder()
                                                     .region(trialListing.getRegionId())
                                                     .regionName(trialListing.getRegion())
                                                     .location(trialListing.getEpimmsId())
                                                     .locationName(trialListing.getVenueName())
                                                     .build());
        } catch (NullPointerException e) {
            log.info("Secondary Court epimmId missing");
        }

    }

    private LocationRefData courtLocationDetails(List<LocationRefData> locationRefDataList, String court, String courtType) {
        LocationRefData courtTypeLocationDetails;
        var foundLocations = locationRefDataList.stream()
            .filter(location -> location.getEpimmsId().equals(court)).toList();
        if (!foundLocations.isEmpty()) {
            courtTypeLocationDetails = foundLocations.get(0);
        } else {
            throw new IllegalArgumentException("Base Court Location not found, in location data for court type " + courtType);
        }
        return courtTypeLocationDetails;
    }
}
