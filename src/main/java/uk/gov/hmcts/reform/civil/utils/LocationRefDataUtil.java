package uk.gov.hmcts.reform.civil.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.CourtLocation;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.CaseLocationCivil;
import uk.gov.hmcts.reform.civil.referencedata.LocationRefDataService;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;

import java.util.List;
import java.util.Optional;

import static uk.gov.hmcts.reform.civil.enums.CaseCategory.SPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.service.robotics.utils.RoboticsDataUtil.CIVIL_COURT_TYPE_ID;

@Slf4j
@Component
public class LocationRefDataUtil {

    private final LocationRefDataService locationRefDataService;

    @Autowired
    public LocationRefDataUtil(LocationRefDataService locationRefDataService) {

        this.locationRefDataService = locationRefDataService;
    }

    public String getPreferredCourtData(CaseData caseData, String authToken, boolean isCourtCodeRequired) {
        if (SPEC_CLAIM.equals(caseData.getCaseAccessCategory())) {
            return "";
        }
        if (caseData.getCourtLocation().getCaseLocation() == null) {
            return caseData.getCourtLocation().getApplicantPreferredCourt();
        } else {
            List<LocationRefData> courtLocations = locationRefDataService.getCourtLocationsByEpimmsIdAndCourtType(
                authToken, caseData.getCourtLocation().getCaseLocation().getBaseLocation());
            if (!courtLocations.isEmpty()) {
                return courtLocations.stream()
                    .filter(id -> id.getCourtTypeId().equals(CIVIL_COURT_TYPE_ID))
                    .findFirst()
                    .map(locationRefData -> isCourtCodeRequired
                        ? locationRefData.getCourtLocationCode() : locationRefData.getCourtName())
                    .orElse("");
            } else {
                log.info("Case {}, court location not found for base location {}",
                         caseData.getLegacyCaseReference(),
                         Optional.ofNullable(caseData.getCourtLocation())
                             .map(CourtLocation::getCaseLocation)
                             .map(CaseLocationCivil::getBaseLocation)
                             .orElse("null"));
                return "";
            }
        }
    }
}
