package uk.gov.hmcts.reform.civil.service.docmosis;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.CaseLocationCivil;
import uk.gov.hmcts.reform.civil.referencedata.LocationRefDataService;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;

import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class DocumentHearingLocationHelper {

    private final LocationRefDataService locationRefDataService;

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

        LocationRefData locationRefData = Optional.ofNullable(caseData.getCaseManagementLocation())
            .map(CaseLocationCivil::getBaseLocation)
            .map(baseLocation -> locationRefDataService.getCourtLocationsByEpimmsId(
                authorisation,
                baseLocation
            )).flatMap(list -> list.stream()
                .filter(location -> StringUtils.equals(
                    location.getRegionId(),
                    caseData.getCaseManagementLocation().getRegion()
                ))
                .findFirst())
            .orElse(null);
        if (locationRefData == null) {
            if (caseData.getCaseManagementLocation() == null) {
                log.info("Case management location is empty for " + caseData.getLegacyCaseReference());
            } else {
                log.info("Case management location for " + caseData.getLegacyCaseReference()
                             + " couldn't be found in court service");
            }
        } else {
            log.info("Case location for " + caseData.getLegacyCaseReference()
                         + " found in CaseManagementLocation, is " + locationRefData.getSiteName());
        }
        return locationRefData;
    }

}
