package uk.gov.hmcts.reform.civil.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.referencedata.response.LocationRefData;
import uk.gov.hmcts.reform.civil.service.referencedata.LocationRefDataService;

import java.util.List;

import static uk.gov.hmcts.reform.civil.service.robotics.utils.RoboticsDataUtil.CIVIL_COURT_TYPE_ID;
import static uk.gov.hmcts.reform.civil.utils.CaseCategoryUtils.isSpecCaseCategory;

@Slf4j
@Component
public class LocationRefDataUtil {

    private final LocationRefDataService locationRefDataService;

    @Autowired
    public LocationRefDataUtil(LocationRefDataService locationRefDataService) {

        this.locationRefDataService = locationRefDataService;
    }

    public String getPreferredCourtCode(CaseData caseData, String authToken) {
        if (isSpecCaseCategory(caseData, caseData.getCaseAccessCategory() != null)) {
            return "";
        }
        if (caseData.getCourtLocation().getCaseLocation() == null) {
            return caseData.getCourtLocation().getApplicantPreferredCourt();
        } else {
            List<LocationRefData> courtLocations = locationRefDataService.getCourtLocationsByEpimmsId(
                authToken, caseData.getCourtLocation().getCaseLocation().getBaseLocation());
            if (!courtLocations.isEmpty()) {
                return courtLocations.stream()
                    .filter(id -> id.getCourtTypeId().equals(CIVIL_COURT_TYPE_ID))
                    .findFirst()
                    .map(LocationRefData::getCourtLocationCode)
                    .orElse("");
            } else {
                log.info("Court location not found");
                return "";
            }
        }
    }
}
