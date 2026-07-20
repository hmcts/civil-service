package uk.gov.hmcts.reform.civil.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;
import uk.gov.hmcts.reform.civil.service.referencedata.LocationReferenceDataService;

import java.util.List;

import static uk.gov.hmcts.reform.civil.enums.CaseCategory.SPEC_CLAIM;

@Slf4j
@Component
public class LocationRefDataUtil {

    private final LocationReferenceDataService locationRefDataService;

    @Autowired
    public LocationRefDataUtil(LocationReferenceDataService locationRefDataService) {

        this.locationRefDataService = locationRefDataService;
    }

    public String getPreferredCourtData(CaseData caseData, String authToken, boolean isCourtCodeRequired) {
        if (SPEC_CLAIM.equals(caseData.getCaseAccessCategory())) {
            return "";
        }
        final String caseServiceId = CaseServiceUtil.getCaseServiceId(caseData.getCaseAccessCategory());
        if (caseData.getCourtLocation().getCaseLocation() == null) {
            return caseData.getCourtLocation().getApplicantPreferredCourt();
        } else {
            List<LocationRefData> courtLocations =
                locationRefDataService.getCourtLocationsByEpimmsIdAndCourtType(
                    authToken,
                    caseData.getCourtLocation().getCaseLocation().getBaseLocation(),
                    caseServiceId
                );
            if (!courtLocations.isEmpty()) {
                return courtLocations.stream()
                    .findFirst()
                    .map(locationRefData -> isCourtCodeRequired
                        ? locationRefData.getCourtLocationCode() : locationRefData.getCourtName())
                    .orElse("");
            } else {
                log.info("Court location not found");
                return "";
            }
        }
    }
}
