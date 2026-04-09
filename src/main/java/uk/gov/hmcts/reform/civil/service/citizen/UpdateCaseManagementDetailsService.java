package uk.gov.hmcts.reform.civil.service.citizen;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.helpers.LocationHelper;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.CaseManagementCategory;
import uk.gov.hmcts.reform.civil.model.CaseManagementCategoryElement;
import uk.gov.hmcts.reform.civil.model.FlightDelayDetails;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.CaseLocationCivil;
import uk.gov.hmcts.reform.civil.model.dq.RequestedCourt;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;
import uk.gov.hmcts.reform.civil.service.AirlineEpimsService;
import uk.gov.hmcts.reform.civil.service.referencedata.LocationReferenceDataService;
import uk.gov.hmcts.reform.civil.utils.CourtLocationUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.getMultiPartyScenario;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.element;

@Slf4j
@Service
@RequiredArgsConstructor
public class UpdateCaseManagementDetailsService {

    private final LocationHelper locationHelper;
    private final LocationReferenceDataService locationRefDataService;
    private final CourtLocationUtils courtLocationUtils;
    private final AirlineEpimsService airlineEpimsService;
    private static final String LIVERPOOL_SITE_NAME = "Liverpool Civil and Family Court";

    public void updateCaseManagementDetails(CaseData caseData, CallbackParams callbackParams) {
        final List<LocationRefData> availableLocations = fetchLocationData(callbackParams);

        updateApplicant1RequestedCourtDetails(caseData, availableLocations);

        if (caseData.getIsFlightDelayClaim() == YesOrNo.YES && caseData.isSmallClaim()) {
            updateFlightDelayCaseManagementLocation(caseData, availableLocations);
        } else {
            locationHelper.getCaseManagementLocation(caseData)
                .ifPresent(requestedCourt -> locationHelper.updateCaseManagementLocation(
                    caseData,
                    requestedCourt,
                    () -> locationRefDataService.getCourtLocationsForDefaultJudgments(callbackParams.getParams().get(
                        CallbackParams.Params.BEARER_TOKEN).toString())
                ));
        }

        caseData.setCaseNameHmctsInternal(caseParticipants(caseData).toString());

        CaseManagementCategoryElement civil =
            new CaseManagementCategoryElement().setCode("Civil").setLabel("Civil");
        List<Element<CaseManagementCategoryElement>> itemList = new ArrayList<>();
        itemList.add(element(civil));
        caseData.setCaseManagementCategory(
            new CaseManagementCategory().setValue(civil).setList_items(itemList));
    }

    private void updateFlightDelayCaseManagementLocation(
        CaseData caseData, List<LocationRefData> availableLocations) {
        Optional.ofNullable(caseData.getFlightDelayDetails())
            .map(FlightDelayDetails::getNameOfAirline)
            .map(airline -> mapToLocation(airline, availableLocations))
            .ifPresent(caseLocationCivil -> setCaseManagementLocation(caseData, caseLocationCivil, availableLocations));
    }

    public void setCaseManagementLocation(
        CaseData caseData, CaseLocationCivil caseLocationCivil, List<LocationRefData> availableLocations) {
        caseData.setCaseManagementLocation(caseLocationCivil);
        availableLocations.stream()
            .filter(locationRefData -> locationRefData.getRegionId().equals(caseLocationCivil.getRegion())
                && locationRefData.getEpimmsId().equals(caseLocationCivil.getBaseLocation()))
            .findFirst()
            .map(LocationRefData::getSiteName)
            .ifPresent(caseData::setLocationName);
    }

    private CaseLocationCivil mapToLocation(String airlineName, List<LocationRefData> availableLocations) {
        return Optional.ofNullable(airlineEpimsService.getEpimsIdForAirlineIgnoreCase(airlineName))
            .map(locationEpimmsId -> availableLocations.stream().filter(loc -> loc.getEpimmsId().equals(locationEpimmsId)).toList())
            .filter(matchedLocations -> !matchedLocations.isEmpty())
            .map(matchedLocations -> new CaseLocationCivil()
                .setRegion(matchedLocations.get(0).getRegionId())
                .setBaseLocation(matchedLocations.get(0).getEpimmsId()))
            .orElse(availableLocations.stream()
                        .filter(locationRefData -> LIVERPOOL_SITE_NAME.equals(locationRefData.getSiteName()))
                        .findFirst()
                        .map(locationRefData -> new CaseLocationCivil()
                            .setRegion(locationRefData.getRegionId())
                            .setBaseLocation(locationRefData.getEpimmsId()))
                        .orElse(null));
    }

    private void updateApplicant1RequestedCourtDetails(CaseData caseData, List<LocationRefData> availableLocations) {
        Optional.ofNullable(caseData.getApplicant1DQ())
            .ifPresent(dq -> Optional.ofNullable(dq.getApplicant1DQRequestedCourt())
                .ifPresent(requestedCourt -> caseData.setApplicant1DQ(
                    dq.copy().setApplicant1DQRequestedCourt(correctCaseLocation(requestedCourt, availableLocations)))));
    }

    public void updateRespondent1RequestedCourtDetails(CaseData caseData, List<LocationRefData> availableLocations) {
        if (caseData.isRespondent1LiP()) {
            Optional.ofNullable(caseData.getRespondent1DQ())
                .ifPresent(dq -> Optional.ofNullable(dq.getRespondent1DQRequestedCourt())
                    .ifPresent(requestedCourt -> caseData.setRespondent1DQ(
                        dq.copy().setRespondent1DQRequestedCourt(correctCaseLocation(requestedCourt, availableLocations)))));
        }
    }

    private RequestedCourt correctCaseLocation(RequestedCourt requestedCourt, List<LocationRefData> locations) {
        if (requestedCourt.getCaseLocation() == null || requestedCourt.getCaseLocation().getBaseLocation() == null) {
            return requestedCourt;
        }
        String locationLabel = requestedCourt.getCaseLocation().getBaseLocation();
        LocationRefData preferredLocation = locations.stream()
            .filter(locationRefData -> courtLocationUtils.checkLocation(locationRefData, locationLabel))
            .findFirst().orElseThrow(RuntimeException::new);
        return requestedCourt.copy()
            .setResponseCourtCode(preferredLocation.getCourtLocationCode())
            .setCaseLocation(LocationHelper.buildCaseLocation(preferredLocation));
    }

    public List<LocationRefData> fetchLocationData(CallbackParams callbackParams) {
        String authToken = callbackParams.getParams().get(BEARER_TOKEN).toString();
        return locationRefDataService.getCourtLocationsForDefaultJudgments(authToken);
    }

    private StringBuilder caseParticipants(CaseData caseData) {
        StringBuilder participantString = new StringBuilder();
        MultiPartyScenario multiPartyScenario = getMultiPartyScenario(caseData);
        if (multiPartyScenario.equals(MultiPartyScenario.ONE_V_TWO_ONE_LEGAL_REP)
            || multiPartyScenario.equals(MultiPartyScenario.ONE_V_TWO_TWO_LEGAL_REP)) {
            participantString.append(caseData.getApplicant1().getPartyName())
                .append(" v ").append(caseData.getRespondent1().getPartyName())
                .append(" and ").append(caseData.getRespondent2().getPartyName());

        } else if (multiPartyScenario.equals(MultiPartyScenario.TWO_V_ONE)) {
            participantString.append(caseData.getApplicant1().getPartyName())
                .append(" and ").append(caseData.getApplicant2().getPartyName())
                .append(" v ")
                .append(caseData.getRespondent1().getPartyName());

        } else {
            participantString.append(caseData.getApplicant1().getPartyName())
                .append(" v ")
                .append(caseData.getRespondent1().getPartyName());
        }
        return participantString;

    }
}
