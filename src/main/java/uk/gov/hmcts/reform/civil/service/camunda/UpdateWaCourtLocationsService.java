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
import static uk.gov.hmcts.reform.civil.utils.CaseServiceUtil.getCaseServiceId;

@RequiredArgsConstructor
@Service
@Slf4j
@ConditionalOnProperty(value = "court_location_dmn.enabled", havingValue = "true")
public class UpdateWaCourtLocationsService {

    private final CamundaRuntimeClient camundaRuntimeClient;
    private final ObjectMapper objectMapper;
    private final LocationReferenceDataService locationRefDataService;
    @Value("${court-location.specified-claim.epimms-id}")
    private String cnbcEpimmId;
    @Value("${court-location.unspecified-claim.epimms-id}")
    private String ccmccEpimmId;

    public void updateCourtListingWALocations(String authorisation, CaseData caseData) {
        List<LocationRefData> locationRefDataList = locationRefDataService.getHearingCourtLocations(
            authorisation,
            getCaseServiceId(caseData.getCaseAccessCategory())
        );

        String claimTrack = getClaimTrack(caseData);
        if (claimTrack == null) {
            // For SPEC cases this is expected before the respondent responds — responseClaimTrack
            // is set by DEFENDANT_RESPONSE_SPEC/CUI. WA listing locations will be evaluated when
            // the respondent/claimant responds or at SDO time. For UNSPEC this is unusual since
            // allocatedTrack is set at CREATE_CLAIM.
            // We still refresh the case management location summary tab so the transferred court is
            // shown even before the track is known (e.g. TRANSFER_ONLINE_CASE before defendant response).
            log.info("Claim track not yet set for case {} (caseAccessCategory={}); "
                     + "skipping WA listing location update — will be evaluated on later event",
                     caseData.getCcdCaseReference(), caseData.getCaseAccessCategory());
            // Best-effort refresh of the summary tab so the transferred court is shown; never fail the
            // event if the location is absent or not in reference data (preserves the safe early return).
            if (caseData.getCaseManagementLocation() != null
                && caseData.getCaseManagementLocation().getBaseLocation() != null) {
                try {
                    updateCaseManagementLocationTab(caseData, locationRefDataList);
                } catch (IllegalArgumentException e) {
                    log.info("Could not refresh case management location tab for case {}; "
                             + "leaving it unchanged. Reason: {}",
                             caseData.getCcdCaseReference(), e.getMessage());
                }
            }
            return;
        }
        if ("FAST_CLAIM".equals(claimTrack) || "SMALL_CLAIM".equals(claimTrack)) {
            // when track is small or fast do not evaluate DMN, and also if claim was changed to small or fast
            // remove any previously evaluated and populate locations from taskManagementLocations
            updateCaseManagementLocationTab(caseData, locationRefDataList);
            caseData.setTaskManagementLocations(null);
            return;
        }

        Map<String, Object> evaluatedCourtMap = camundaRuntimeClient
            .getEvaluatedDmnCourtLocations(caseData.getCaseManagementLocation().getBaseLocation(), claimTrack);
        DmnListingLocations dmnListingLocations = objectMapper.convertValue(
            evaluatedCourtMap,
            DmnListingLocations.class
        );

        try {
            LocationRefData cmcListing = courtLocationDetails(
                locationRefDataList,
                dmnListingLocations.getCmcListingLocation().getValue(), "Cmc Listing"
            );
            LocationRefData ccmcListing = courtLocationDetails(
                locationRefDataList,
                dmnListingLocations.getCcmcListingLocation().getValue(), "Ccmc Listing"
            );
            LocationRefData preTrialListing = courtLocationDetails(
                locationRefDataList,
                dmnListingLocations.getPtrListingLocation().getValue(), "Pre trial Listing"
            );
            LocationRefData trialListing = courtLocationDetails(
                locationRefDataList,
                dmnListingLocations.getTrialListingLocation().getValue(), "Trial Listing"
            );

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
                                                                                 .setLocationName(trialListing.getSiteName())
                                                    ));

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

        updateCaseManagementLocationTab(caseData, locationRefDataList);

        caseData.setTaskManagementLocationsTab(tabContent);
    }

    /**
     * Refreshes the case management location summary tab from the current case management location.
     * Kept independent of the listing-location (WA/DMN) evaluation so the tab reflects the latest
     * court even when the claim track is not yet known and listing locations cannot be evaluated.
     */
    private void updateCaseManagementLocationTab(CaseData caseData, List<LocationRefData> locationRefDataList) {
        LocationRefData caseManagementLocationName = courtLocationDetails(
            locationRefDataList,
            caseData.getCaseManagementLocation().getBaseLocation(),
            "CML location"
        );
        caseData.setCaseManagementLocationTab(new TaskManagementLocationTab()
                                                  .setCaseManagementLocation(caseManagementLocationName.getSiteName()));
    }

    private LocationRefData courtLocationDetails(List<LocationRefData> locationRefDataList, String court, String courtType) {

        // CNBC will not be returned by ref data call, so populate details manually
        if (cnbcEpimmId.equals(court) || ccmccEpimmId.equals(court)) {
            return new LocationRefData()
                .setRegion("Midlands")
                .setRegionId("2")
                .setEpimmsId(cnbcEpimmId)
                .setSiteName("Civil National Business Centre");
        }

        LocationRefData courtTypeLocationDetails;
        var foundLocations = locationRefDataList.stream()
            .filter(location -> location.getEpimmsId().equals(court)).toList();
        if (!foundLocations.isEmpty()) {
            courtTypeLocationDetails = foundLocations.get(0);
        } else {
            throw new IllegalArgumentException(
                "Court Location not found, in location data for court type %s epimms_id %s".formatted(
                    courtType,
                    court
                ));
        }
        return courtTypeLocationDetails;
    }

    private String getClaimTrack(CaseData caseData) {
        if (UNSPEC_CLAIM.equals(caseData.getCaseAccessCategory())) {
            return (null != caseData.getAllocatedTrack()) ? caseData.getAllocatedTrack().name() : null;
        } else if (SPEC_CLAIM.equals(caseData.getCaseAccessCategory())) {
            return caseData.getResponseClaimTrack();
        }
        return null;
    }
}
