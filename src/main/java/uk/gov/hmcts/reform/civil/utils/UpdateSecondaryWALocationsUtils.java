package uk.gov.hmcts.reform.civil.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.model.CaseData;

import uk.gov.hmcts.reform.civil.model.caseprogression.SecondaryListingLocations;
import uk.gov.hmcts.reform.civil.model.finalorders.SecondaryWaCaseLocationCivil;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;
import uk.gov.hmcts.reform.civil.service.camunda.CamundaRuntimeClient;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentHearingLocationHelper;
import uk.gov.hmcts.reform.civil.service.referencedata.LocationReferenceDataService;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Service
public class UpdateSecondaryWALocationsUtils {

    private final CamundaRuntimeClient camundaRuntimeClient;
    private final ObjectMapper objectMapper;
    private final DocumentHearingLocationHelper documentHearingLocationHelper;
    private final LocationReferenceDataService locationRefDataService;

    public void updateSecondaryWALocations(String courtId, CaseData caseData,  String authorisation, CaseData.CaseDataBuilder<?, ?> caseDataBuilder) {

        Map<String, Object> secondaryCourtMap = camundaRuntimeClient.getEvaluatedDmnCourtLocations(courtId);


        ObjectMapper mapper = new ObjectMapper();

        SecondaryListingLocations secondaryLocations = mapper.convertValue(secondaryCourtMap, SecondaryListingLocations.class);
        System.out.println(secondaryLocations.getCmcListingLocation().getValue());

        List<LocationRefData> locationRefDataList = locationRefDataService.getHearingCourtLocations(authorisation);


        LocationRefData CMCListing = courtLocationDetails(locationRefDataList, secondaryLocations.getCmcListingLocation().getValue(), "CMC Listing");
        LocationRefData CCMCListing = courtLocationDetails(locationRefDataList, secondaryLocations.getCcmcListingLocation().getValue(), "CCMC Listing");
        LocationRefData preTrialListing = courtLocationDetails(locationRefDataList, secondaryLocations.getPtrListingLocation().getValue(), "Pre trial Listing");
        LocationRefData trialListing = courtLocationDetails(locationRefDataList, secondaryLocations.getTrialListingLocation().getValue(), "Trial Listing");

        caseDataBuilder.cmcListingLocation(SecondaryWaCaseLocationCivil.builder()
                                               .region(CMCListing.getRegionId())
                                               .regionName(CMCListing.getRegion())
                                               .location(CMCListing.getEpimmsId())
                                               .locationName(CMCListing.getVenueName())
                                               .build());
        caseDataBuilder.ccmcListingLocation(SecondaryWaCaseLocationCivil.builder()
                                                .region(CCMCListing.getRegionId())
                                                .regionName(CCMCListing.getRegion())
                                                .location(CCMCListing.getEpimmsId())
                                                .locationName(CCMCListing.getVenueName())
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
