package uk.gov.hmcts.reform.civil.service.docmosis;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.CaseLocationCivil;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;
import uk.gov.hmcts.reform.civil.service.referencedata.LocationReferenceDataService;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.civil.enums.CaseCategory.SPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.CaseCategory.UNSPEC_CLAIM;

@Component
@RequiredArgsConstructor
@Slf4j
public class DocumentHearingLocationHelper {

    @Value("${court-location.unspecified-claim.epimms-id}")
    public String ccmccEpimmId;
    @Value("${court-location.specified-claim.epimms-id}")
    public String cnbcEpimmId;
    private final LocationReferenceDataService locationRefDataService;

    public LocationRefData getHearingLocation(String valueFromForm, CaseData caseData, String authorisation) {
        if (StringUtils.isNotBlank(valueFromForm)) {
            Optional<LocationRefData> fromForm = locationRefDataService.getLocationMatchingLabel(
                valueFromForm,
                authorisation
            );
            if (fromForm.isPresent()) {
                log.info("Case location for " + caseData.getLegacyCaseReference()
                             + " determined from " + valueFromForm + " as " + fromForm.get().getSiteName());
                return fromForm.get();
            }
        }

        return Optional.ofNullable(caseData.getCaseManagementLocation())
            .map(CaseLocationCivil::getBaseLocation)
            .map(baseLocation -> {
                List<LocationRefData> sameLocation = locationRefDataService.getCourtLocationsByEpimmsIdAndCourtType(
                    authorisation,
                    baseLocation
                ).stream().filter(location -> StringUtils.equals(
                    location.getRegionId(),
                    caseData.getCaseManagementLocation().getRegion()
                )).toList();
                if (sameLocation.isEmpty()) {
                    return null;
                } else if (sameLocation.size() == 1) {
                    log.info("Claim " + caseData.getLegacyCaseReference()
                                 + " found one matching location: " + LocationReferenceDataService.getDisplayEntry(
                        sameLocation.get(0)));
                    return sameLocation.get(0);
                } else {
                    log.info("Claim " + caseData.getLegacyCaseReference() +
                                 "found " + sameLocation.size() + " locations with same epimmsId and region "
                                 + baseLocation + "/" + caseData.getCaseManagementLocation().getRegion()
                                 + ": " + sameLocation.stream().map(LocationReferenceDataService::getDisplayEntry)
                        .collect(Collectors.joining(", ")));
                    return sameLocation.get(0);
                }
            }).orElse(null);
    }

    public LocationRefData getCaseManagementLocationDetailsNro(CaseData caseData, LocationReferenceDataService locationRefDataService, String authorisation) {
        LocationRefData caseManagementLocationDetails = null;
        if (checkIfCcmccOrCnbc(caseData) && caseData.getCaseAccessCategory().equals(SPEC_CLAIM)) {
            caseManagementLocationDetails = locationRefDataService.getCnbcLocation(authorisation);
        }
        if (checkIfCcmccOrCnbc(caseData) && caseData.getCaseAccessCategory().equals(UNSPEC_CLAIM)) {
            caseManagementLocationDetails = locationRefDataService.getCcmccLocation(authorisation);
        }
        if (!checkIfCcmccOrCnbc(caseData)) {
            List<LocationRefData>  locationRefDataList = locationRefDataService.getHearingCourtLocations(authorisation);
            var foundLocations = locationRefDataList.stream()
                .filter(location -> location.getEpimmsId().equals(caseData.getCaseManagementLocation().getBaseLocation())).toList();
            if (!foundLocations.isEmpty()) {
                caseManagementLocationDetails = foundLocations.get(0);
            } else {
                throw new IllegalArgumentException("Base Court Location not found, in location data");
            }
        }
        return caseManagementLocationDetails;
    }

    public Boolean checkIfCcmccOrCnbc(CaseData caseData) {
        if (caseData.getCaseManagementLocation().getBaseLocation().equals(ccmccEpimmId)) {
            return true;
        } else {
            return caseData.getCaseManagementLocation().getBaseLocation().equals(cnbcEpimmId);
        }
    }

}
