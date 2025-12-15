package uk.gov.hmcts.reform.civil.service.camunda;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.model.CaseData;

import uk.gov.hmcts.reform.civil.model.dmnacourttasklocation.DmnListingLocations;
import uk.gov.hmcts.reform.civil.model.dmnacourttasklocation.TaskManagementLocationTab;
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
    @Value("${court-location.unspecified-claim.epimms-id}") private String ccmccEpimmId;

    public void updateCourtListingWALocations(String authorisation, CaseData caseData) {
        List<LocationRefData> locationRefDataList = locationRefDataService.getHearingCourtLocations(authorisation);

        String claimTrack = getClaimTrack(caseData);
        if ("FAST_CLAIM".equals(claimTrack) || "SMALL_CLAIM".equals(claimTrack)) {
            // when track is small or fast do not evaluate DMN, and also if claim was changed to small or fast
            // remove any previously evaluated and populate locations from taskManagementLocations
            LocationRefData caseManagementLocationName = courtLocationDetails(locationRefDataList,
                                                                              caseData.getCaseManagementLocation().getBaseLocation(),
                                                                              "CML location");
            caseData.setCaseManagementLocationTab(new TaskManagementLocationTab()
                                                          .setCaseManagementLocation(caseManagementLocationName.getSiteName()));
            caseData.setTaskManagementLocations(null);
            return;
        }

        Map<String, Object> evaluatedCourtMap = camundaRuntimeClient
            .getEvaluatedDmnCourtLocations(caseData.getCaseManagementLocation().getBaseLocation(), claimTrack);
        DmnListingLocations dmnListingLocations = objectMapper.convertValue(evaluatedCourtMap, DmnListingLocations.class);

        try {
            LocationRefData cmcListing = courtLocationDetails(locationRefDataList,
                                                              dmnListingLocations.getCmcListingLocation().getValue(), "Cmc Listing");
            LocationRefData ccmcListing = courtLocationDetails(locationRefDataList,
                                                               dmnListingLocations.getCcmcListingLocation().getValue(), "Ccmc Listing");
            LocationRefData preTrialListing = courtLocationDetails(locationRefDataList,
                                                                   dmnListingLocations.getPtrListingLocation().getValue(), "Pre trial Listing");
            LocationRefData trialListing = courtLocationDetails(locationRefDataList,
                                                                dmnListingLocations.getTrialListingLocation().getValue(), "Trial Listing");

            caseData.setTaskManagementLocations(new TaskManagementLocationTypes()
                                                        .setCmcListingLocation(new TaskManagementLocationsModel()
                                                                                .setRegion(cmcListing.getRegionId())
                                                                                .setRegionName(cmcListing.getRegion())
                                                                                .setLocation(cmcListing.getEpimmsId())
                                                                                .setLocationName(cmcListing.getSiteName()))
                                                        .setCcmcListingLocation(new TaskManagementLocationsModel()
                                                                                 .setRegion(ccmcListing.getRegionId())
                                                                                 .setRegionName(ccmcListing.getRegion())
                                                                                 .setLocation(ccmcListing.getEpimmsId())
                                                                                 .setLocationName(ccmcListing.getSiteName()))
                                                        .setPtrListingLocation(new TaskManagementLocationsModel()
                                                                                .setRegion(preTrialListing.getRegionId())
                                                                                .setRegionName(preTrialListing.getRegion())
                                                                                .setLocation(preTrialListing.getEpimmsId())
                                                                                .setLocationName(preTrialListing.getSiteName()))
                                                        .setTrialListingLocation(new TaskManagementLocationsModel()
                                                                                  .setRegion(trialListing.getRegionId())
                                                                                  .setRegionName(trialListing.getRegion())
                                                                                  .setLocation(trialListing.getEpimmsId())
                                                                                  .setLocationName(trialListing.getSiteName())));

            populateSummaryTab(caseData, locationRefDataList);

        } catch (NullPointerException e) {
            log.info("Court epimmId missing");
        }
    }

    private void populateSummaryTab(CaseData caseData, List<LocationRefData> locationRefDataList) {

        TaskManagementLocationTab tabContent = new TaskManagementLocationTab()
            .setCmcListingLocation(caseData.getTaskManagementLocations().getCmcListingLocation().getLocationName())
            .setPtrListingLocation(caseData.getTaskManagementLocations().getPtrListingLocation().getLocationName())
            .setTrialListingLocation(caseData.getTaskManagementLocations().getTrialListingLocation().getLocationName());

        String claimTrack = getClaimTrack(caseData);
        if ("MULTI_CLAIM".equals(claimTrack)) {
            tabContent.setCcmcListingLocation(caseData.getTaskManagementLocations().getCcmcListingLocation().getLocationName());
        }

        LocationRefData caseManagementLocationName = courtLocationDetails(locationRefDataList,
                                                                          caseData.getCaseManagementLocation().getBaseLocation(),
                                                                          "CML location");

        caseData.setCaseManagementLocationTab(new TaskManagementLocationTab()
                                                      .setCaseManagementLocation(caseManagementLocationName.getSiteName()));

        caseData.setTaskManagementLocationsTab(tabContent);
    }

    private LocationRefData courtLocationDetails(List<LocationRefData> locationRefDataList, String court, String courtType) {

        // CNBC will not be returned by ref data call, so populate details manually
        if (cnbcEpimmId.equals(court) || ccmccEpimmId.equals(court)) {
            return LocationRefData.builder()
                .region("Midlands")
                .regionId("2")
                .epimmsId(cnbcEpimmId)
                .siteName("Civil National Business Centre").build();
        }

        LocationRefData courtTypeLocationDetails;
        var foundLocations = locationRefDataList.stream()
            .filter(location -> location.getEpimmsId().equals(court)).toList();
        if (!foundLocations.isEmpty()) {
            courtTypeLocationDetails = foundLocations.get(0);
        } else {
            throw new IllegalArgumentException(
                "Court Location not found, in location data for court type %s epimms_id %s".formatted(courtType, court));
        }
        return courtTypeLocationDetails;
    }

    private String getClaimTrack(CaseData caseData) {
        if (UNSPEC_CLAIM.equals(caseData.getCaseAccessCategory())) {
            return caseData.getAllocatedTrack().name();
        } else if (SPEC_CLAIM.equals(caseData.getCaseAccessCategory())) {
            return caseData.getResponseClaimTrack();
        }
        return null;
    }
}
