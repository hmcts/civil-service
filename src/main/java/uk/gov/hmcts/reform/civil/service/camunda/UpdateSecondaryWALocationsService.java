package uk.gov.hmcts.reform.civil.service.camunda;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.model.CaseData;

import uk.gov.hmcts.reform.civil.model.caseprogression.SecondaryListingLocations;
import uk.gov.hmcts.reform.civil.model.caseprogression.WorkAllocationTaskLocationModel;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;
import uk.gov.hmcts.reform.civil.service.referencedata.LocationReferenceDataService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.enums.CaseCategory.SPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.CaseCategory.UNSPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.element;

@RequiredArgsConstructor
@Service
@Slf4j
public class UpdateSecondaryWALocationsService {

    private final CamundaRuntimeClient camundaRuntimeClient;
    private final ObjectMapper objectMapper;
    private final LocationReferenceDataService locationRefDataService;
    private static final String CASE_CONFERENCE_LOCATION = "Case conference location";
    private static final String COST_CONFERENCE_LOCATION = "Cost conference location";
    private static final String PRE_TRIAL_REVIEW_LOCATION = "pre trial review location";
    private static final String TRIAL_LOCATION = "trial location";

    public void updateSecondaryWALocations(String courtId, CaseData caseData, String authorisation, CaseData.CaseDataBuilder<?, ?> caseDataBuilder) {

        Map<String, Object> secondaryCourtMap = camundaRuntimeClient.getEvaluatedDmnCourtLocations(courtId, getClaimTrack(caseData));
        SecondaryListingLocations secondaryLocations = objectMapper.convertValue(secondaryCourtMap, SecondaryListingLocations.class);
        List<LocationRefData> locationRefDataList = locationRefDataService.getHearingCourtLocations(authorisation);
        log.info("hearing court list size {} with entries {}", locationRefDataList.size(), locationRefDataList);

        try {
            LocationRefData cmcListing = courtLocationDetails(locationRefDataList, secondaryLocations.getCmcListingLocation().getValue(), "Cmc Listing");
            LocationRefData ccmcListing = courtLocationDetails(locationRefDataList, secondaryLocations.getCcmcListingLocation().getValue(), "Ccmc Listing");
            LocationRefData preTrialListing = courtLocationDetails(locationRefDataList, secondaryLocations.getPtrListingLocation().getValue(), "Pre trial Listing");
            LocationRefData trialListing = courtLocationDetails(locationRefDataList, secondaryLocations.getTrialListingLocation().getValue(), "Trial Listing");


            List<Element<WorkAllocationTaskLocationModel>> waTaskLocationList = new ArrayList<>();

            waTaskLocationList.add(element(WorkAllocationTaskLocationModel.builder()
                                               .type(CASE_CONFERENCE_LOCATION)
                                               .region(cmcListing.getRegionId())
                                               .regionName(cmcListing.getRegion())
                                               .location(cmcListing.getEpimmsId())
                                               .locationName(cmcListing.getExternalShortName())
                                               .build()));

            waTaskLocationList.add(element(WorkAllocationTaskLocationModel.builder()
                                               .type(COST_CONFERENCE_LOCATION)
                                               .region(ccmcListing.getRegionId())
                                               .regionName(ccmcListing.getRegion())
                                               .location(ccmcListing.getEpimmsId())
                                               .locationName(ccmcListing.getExternalShortName())
                                               .build()));

            waTaskLocationList.add(element(WorkAllocationTaskLocationModel.builder()
                                               .type(PRE_TRIAL_REVIEW_LOCATION)
                                               .region(preTrialListing.getRegionId())
                                               .regionName(preTrialListing.getRegion())
                                               .location(preTrialListing.getEpimmsId())
                                               .locationName(preTrialListing.getExternalShortName())
                                               .build()));

            waTaskLocationList.add(element(WorkAllocationTaskLocationModel.builder()
                                               .type(TRIAL_LOCATION)
                                               .region(trialListing.getRegionId())
                                               .regionName(trialListing.getRegion())
                                               .location(trialListing.getEpimmsId())
                                               .locationName(trialListing.getExternalShortName())
                                               .build()));

            caseDataBuilder.taskManagementLocations(waTaskLocationList);

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
