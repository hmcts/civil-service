package uk.gov.hmcts.reform.civil.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseEventDetail;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.genapplication.CaseLocationCivil;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;
import uk.gov.hmcts.reform.civil.service.referencedata.LocationReferenceDataService;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static uk.gov.hmcts.reform.civil.callback.CaseEvent.TRANSFER_ONLINE_CASE;
import static uk.gov.hmcts.reform.civil.enums.CaseState.AWAITING_APPLICANT_INTENTION;
import static uk.gov.hmcts.reform.civil.enums.CaseState.AWAITING_CASE_DETAILS_NOTIFICATION;
import static uk.gov.hmcts.reform.civil.enums.CaseState.AWAITING_RESPONDENT_ACKNOWLEDGEMENT;
import static uk.gov.hmcts.reform.civil.enums.CaseState.CASE_DISCONTINUED;
import static uk.gov.hmcts.reform.civil.enums.CaseState.CASE_ISSUED;
import static uk.gov.hmcts.reform.civil.enums.CaseState.CASE_SETTLED;
import static uk.gov.hmcts.reform.civil.enums.CaseState.PENDING_CASE_ISSUED;

@Service
@RequiredArgsConstructor
@Slf4j
public class LocationService {

    private final LocationReferenceDataService locationRefDataService;
    private final CoreCaseEventDataService coreCaseEventDataService;
    private final FeatureToggleService featureToggleService;

    public static final Set<CaseState> statesBeforeSDO = EnumSet.of(PENDING_CASE_ISSUED, CASE_ISSUED,
                                                                    AWAITING_CASE_DETAILS_NOTIFICATION,
                                                                    AWAITING_RESPONDENT_ACKNOWLEDGEMENT,
                                                                    AWAITING_APPLICANT_INTENTION);

    public static final Set<CaseState> settleDiscontinueStates = EnumSet.of(CASE_SETTLED,
                                                                            CASE_DISCONTINUED);

    public Pair<CaseLocationCivil, Boolean> getWorkAllocationLocation(CaseData caseData, String authToken) {
        if (hasSDOBeenMade(caseData)) {
            log.info("WorkAllocation Location If for caseId {}", caseData.getCcdCaseReference());
            return Pair.of(assignCaseManagementLocationToMainCaseLocation(caseData, authToken), false);
        } else {
            log.info("WorkAllocation Location Else for caseId {}", caseData.getCcdCaseReference());
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

    private boolean hasSDOBeenMade(CaseData caseData) {
        log.info("hasSDOBeenMade If for caseId {}", caseData.getCcdCaseReference());
        return (!statesBeforeSDO.contains(caseData.getCcdState())
            && !settleDiscontinueStates.contains(caseData.getCcdState()))
            || (!statesBeforeSDO.contains(caseData.getPreviousCCDState())
            && settleDiscontinueStates.contains(caseData.getCcdState()));

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
        String epimmsId = Optional.ofNullable(caseData)
            .map(CaseData::getCaseManagementLocation)
            .map(uk.gov.hmcts.reform.civil.model.defaultjudgment.CaseLocationCivil::getBaseLocation)
            .orElse(null);

        String caseRef = (caseData != null) ? String.valueOf(caseData.getCcdCaseReference()) : "unknown";
        if (StringUtils.isEmpty(epimmsId)) {
            throw new IllegalArgumentException(String.format(
                "Base Court Location for main applications not found, in case data for caseId %s", caseRef
            ));
        }

        String region = Optional.of(caseData)
            .map(CaseData::getCaseManagementLocation)
            .map(uk.gov.hmcts.reform.civil.model.defaultjudgment.CaseLocationCivil::getRegion)
            .orElse("unknown");

        log.info("Case managementLocation region {} and base Location {} caseId {}", region, epimmsId, caseRef);

        List<LocationRefData> locationRefDataList =
            locationRefDataService.getCourtLocationsByEpimmsIdWithCML(authToken, epimmsId);

        log.info("CML court locations found : {} for caseId {}",
                 locationRefDataList, caseData.getCcdCaseReference());

        if (CollectionUtils.isEmpty(locationRefDataList)) {
            throw new IllegalArgumentException(String.format(
                "Base Court Location for General applications not found, in location data for caseId %s",
                caseData.getCcdCaseReference()
            ));
        }

        LocationRefData caseManagementLocationDetails = locationRefDataList.get(0);

        return CaseLocationCivil.builder()
            .region(caseManagementLocationDetails.getRegionId())
            .baseLocation(caseManagementLocationDetails.getEpimmsId())
            .siteName(caseManagementLocationDetails.getSiteName())
            .address(caseManagementLocationDetails.getCourtAddress())
            .postcode(caseManagementLocationDetails.getPostcode())
            .build();
    }
}
