package uk.gov.hmcts.reform.civil.service.camunda;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.model.CaseData;

import uk.gov.hmcts.reform.civil.model.dmnacourttasklocation.DmnListingLocations;
import uk.gov.hmcts.reform.civil.model.dmnacourttasklocation.TaskManagementLocationTypes;
import uk.gov.hmcts.reform.civil.model.dmnacourttasklocation.TaskManagementLocationsModel;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;
import uk.gov.hmcts.reform.civil.service.referencedata.LocationReferenceDataService;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.enums.CaseCategory.SPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.CaseCategory.UNSPEC_CLAIM;

@RequiredArgsConstructor
@Service
@Slf4j
@ConditionalOnProperty(value = "court_location_dmn.enabled", havingValue = "true")
public class UpdateWaCourtLocationsService {

    private final CamundaRuntimeClient camundaRuntimeClient;
    private final ObjectMapper objectMapper;
    private final LocationReferenceDataService locationRefDataService;
    @Value("${court-location.specified-claim.epimms-id}") private String cnbcEpimmId;

    public void updateCourtListingWALocations(String authorisation, CaseData.CaseDataBuilder<?, ?> caseDataBuilder) {
        CaseData caseData = caseDataBuilder.build();

        String claimTrack = getClaimTrack(caseData);
        if ("FAST_CLAIM".equals(claimTrack) || "SMALL_CLAIM".equals(claimTrack)) {
            // when track is small or fast do not evaluate DMN, and also if claim was changed to small or fast
            // remove any previously evaluated and populate locations from taskManagementLocations
            caseDataBuilder.taskManagementLocations(null);
            return;
        }

        Map<String, Object> evaluatedCourtMap = camundaRuntimeClient
            .getEvaluatedDmnCourtLocations(caseDataBuilder.build().getCaseManagementLocation().getBaseLocation(), claimTrack);
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
                                                                                .locationName(cmcListing.getSiteName())
                                                                                .build())
                                                        .ccmcListingLocation(TaskManagementLocationsModel.builder()
                                                                                 .region(ccmcListing.getRegionId())
                                                                                 .regionName(ccmcListing.getRegion())
                                                                                 .location(ccmcListing.getEpimmsId())
                                                                                 .locationName(ccmcListing.getSiteName())
                                                                                 .build())
                                                        .ptrListingLocation(TaskManagementLocationsModel.builder()
                                                                                .region(preTrialListing.getRegionId())
                                                                                .regionName(preTrialListing.getRegion())
                                                                                .location(preTrialListing.getEpimmsId())
                                                                                .locationName(preTrialListing.getSiteName())
                                                                                .build())
                                                        .trialListingLocation(TaskManagementLocationsModel.builder()
                                                                                  .region(trialListing.getRegionId())
                                                                                  .regionName(trialListing.getRegion())
                                                                                  .location(trialListing.getEpimmsId())
                                                                                  .locationName(trialListing.getSiteName())
                                                                                  .build())
                                                        .build());

        } catch (NullPointerException e) {
            log.info("Court epimmId missing");
        }
    }

    private LocationRefData courtLocationDetails(List<LocationRefData> locationRefDataList, String court, String courtType) {

        // CNBC will not be returned by ref data call, so populate details manually
        if (court.equals(cnbcEpimmId)) {
            LocationRefData cnbcDetails = LocationRefData.builder()
                .region("Midlands")
                .regionId("2")
                .epimmsId(cnbcEpimmId)
                .siteName("Civil National Business Centre").build();
            return cnbcDetails;
        }

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
