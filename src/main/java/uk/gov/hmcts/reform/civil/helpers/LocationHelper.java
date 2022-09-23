package uk.gov.hmcts.reform.civil.helpers;

import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.enums.AllocatedTrack;
import uk.gov.hmcts.reform.civil.enums.SuperClaimType;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.sdo.OrderType;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.CaseLocation;
import uk.gov.hmcts.reform.civil.model.dq.Applicant1DQ;
import uk.gov.hmcts.reform.civil.model.dq.RequestedCourt;
import uk.gov.hmcts.reform.civil.model.referencedata.response.LocationRefData;
import uk.gov.hmcts.reform.civil.service.referencedata.LocationRefDataService;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.enums.SuperClaimType.SPEC_CLAIM;

public class LocationHelper {

    public void setLocationAndCaseManagementLocation(CallbackParams callbackParams,
                                                     CaseData caseData,
                                                     CaseData.CaseDataBuilder<?, ?> updatedData,
                                                     LocationRefDataService locationRefDataService) {
        List<LocationRefData> locations = locationRefDataService.getCourtLocationsForDefaultJudgments(
            callbackParams.getParams().get(BEARER_TOKEN).toString()
        );

        String locationLabel;
        if (caseData.getSuperClaimType() == SuperClaimType.SPEC_CLAIM) {
            RequestedCourt courtRequest = Optional.ofNullable(caseData.getApplicant1DQ())
                .map(Applicant1DQ::getRequestedCourt).orElse(null);
            if (courtRequest != null && courtRequest.getRequestHearingAtSpecificCourt() == YesOrNo.YES) {
                locationLabel = courtRequest.getResponseCourtCode();
            } else {
                return; // getLocationsFromList(locations);
            }
        } else {
            // assume unspec
            locationLabel = caseData.getCourtLocation().getApplicantPreferredCourt();
        }

        var preferredLocation = preferredLocation(locationLabel, locations);

        setCaseManagementLocationData(caseData, updatedData, preferredLocation, locations);
    }

    public Optional<LocationRefData> preferredLocation(String locationLabel, List<LocationRefData> locations) {
        return locations
            .stream()
            .filter(locationRefData -> checkLocation(
                locationRefData,
                locationLabel
            )).findFirst();
    }

    public List<String> getLocationsFromList(final List<LocationRefData> locations) {
        return locations.stream()
            .map(location -> location.getSiteName()
                + " - " + location.getCourtAddress()
                + " - " + location.getPostcode())
            .collect(Collectors.toList());
    }

    public Boolean checkLocation(final LocationRefData location,
                                 String locationTempLabel) {
        return location.getEpimmsId().equals(locationTempLabel);
    }

    public void setCaseManagementLocationData(CaseData caseData,
                                              CaseData.CaseDataBuilder<?, ?> updatedData,
                                              Optional<LocationRefData> preferredLocation,
                                              List<LocationRefData> locations) {
        if (caseData.getOrderType() == OrderType.DECIDE_DAMAGES && preferredLocation.isPresent()) {
            updatedData.caseManagementLocation(
                CaseLocation.builder()
                    .region(preferredLocation.get().getRegionId())
                    .baseLocation(preferredLocation.get().getEpimmsId()).build()
            ).locationName(preferredLocation.get().getSiteName());
        } else if (SPEC_CLAIM.equals(caseData.getSuperClaimType())
            || (caseData.getAllocatedTrack() == AllocatedTrack.MULTI_CLAIM
            && SPEC_CLAIM.equals(caseData.getSuperClaimType()))) {
            if (caseData.getApplicant1().getType() == Party.Type.INDIVIDUAL
                || caseData.getApplicant1().getType() == Party.Type.SOLE_TRADER) {
                updatedData.caseManagementLocation(
                    CaseLocation.builder()
                        .region(caseData.getApplicant1DQ().getApplicant1DQRequestedCourt()
                                    .getCaseLocation().getRegion())
                        .baseLocation(caseData.getApplicant1DQ().getApplicant1DQRequestedCourt().getCaseLocation()
                                          .getBaseLocation()).build()
                );
            } else if (caseData.getApplicant1().getType() == Party.Type.COMPANY
                || caseData.getApplicant1().getType() == Party.Type.ORGANISATION) {
                updatedData.caseManagementLocation(
                    CaseLocation.builder()
                        .region(caseData.getRespondent1DQ().getRespondent1DQRequestedCourt()
                                    .getCaseLocation().getRegion())
                        .baseLocation(caseData.getRespondent1DQ().getRespondent1DQRequestedCourt().getCaseLocation()
                                          .getBaseLocation()).build()
                );
            }
        } else if (preferredLocation.isPresent()) {
            updatedData.caseManagementLocation(
                CaseLocation.builder()
                    .region(preferredLocation.get().getRegionId())
                    .baseLocation(preferredLocation.get().getEpimmsId()).build()
            ).locationName(preferredLocation.get().getSiteName());
        }

        if (preferredLocation.isPresent()) {
            locations.remove(preferredLocation.get());
            locations.add(0, preferredLocation.get());
        }
    }
}
