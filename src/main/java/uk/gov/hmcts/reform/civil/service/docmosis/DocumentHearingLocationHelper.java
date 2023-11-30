package uk.gov.hmcts.reform.civil.service.docmosis;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.CaseLocationCivil;
import uk.gov.hmcts.reform.civil.referencedata.LocationRefDataService;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class DocumentHearingLocationHelper {

    private final LocationRefDataService locationRefDataService;

    public LocationRefData getHearingLocation(String valueFromForm, CaseData caseData, String authorisation) {
        if (StringUtils.isNotBlank(valueFromForm)) { // false
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

        LocationRefData locationRefData = Optional.ofNullable(caseData.getCaseManagementLocation())
            .map(CaseLocationCivil::getBaseLocation)
            .map(baseLocation -> {
                List<LocationRefData> sameLocation = locationRefDataService.getCourtLocationsByEpimmsIdAndCourtType(
                    authorisation,
                    baseLocation
                ).stream().filter(location -> StringUtils.equals(
                    location.getRegionId(),
                    caseData.getCaseManagementLocation().getRegion()
                )).collect(Collectors.toList());
                if (sameLocation.isEmpty()) {
                    return null;
                } else if (sameLocation.size() == 1) {
                    log.info("Claim " + caseData.getLegacyCaseReference()
                                 + " found one matching location: " + LocationRefDataService.getDisplayEntry(
                        sameLocation.get(0)));
                    return sameLocation.get(0);
                } else {
                    log.info("Claim " + caseData.getLegacyCaseReference() +
                                 "found " + sameLocation.size() + " locations with same epimmsId and region "
                                 + baseLocation + "/" + caseData.getCaseManagementLocation().getRegion()
                                 + ": " + sameLocation.stream().map(LocationRefDataService::getDisplayEntry)
                        .collect(Collectors.joining(", ")));
                    return sameLocation.get(0);
                }
            }).orElse(null);
        return locationRefData;
    }

}
