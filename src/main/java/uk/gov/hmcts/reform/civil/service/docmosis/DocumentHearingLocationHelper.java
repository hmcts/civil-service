package uk.gov.hmcts.reform.civil.service.docmosis;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.CaseLocationCivil;
import uk.gov.hmcts.reform.civil.referencedata.LocationRefDataService;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class DocumentHearingLocationHelper {

    private final LocationRefDataService locationRefDataService;

    public LocationRefData getHearingLocation(String valueFromForm, CaseData caseData, String authorisation) {
        if (StringUtils.isNotBlank(valueFromForm)) {
            Optional<LocationRefData> fromForm = locationRefDataService.getLocationMatchingLabel(
                valueFromForm,
                authorisation
            );
            if (fromForm.isPresent()) {
                return fromForm.get();
            }
        }

        return Optional.ofNullable(caseData.getCaseManagementLocation())
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
    }

}
