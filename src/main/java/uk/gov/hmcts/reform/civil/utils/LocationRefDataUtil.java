package uk.gov.hmcts.reform.civil.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.referencedata.LocationRefDataService;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.reform.civil.enums.CaseCategory.SPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.service.robotics.utils.RoboticsDataUtil.CIVIL_COURT_TYPE_ID;

@Slf4j
@Component
public class LocationRefDataUtil {

    private final LocationRefDataService locationRefDataService;
    /*
    *  Below map holds the key = Closed court in ref data
    *  and Value holds the live court against closed court
    * */
    public static final Map<String, String> liveEpimmsIdMap = Map.of("336348", "214320",
                 "403751", "227860",
                 "425094", "223503",
                 "487294", "369145",
                 "498443", "366796",
                 "634542", "102050",
                 "711798", "198592",
                 "771467", "369145");

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
                authToken, getLiveCourtByEpimmsId(caseData.getCourtLocation().getCaseLocation().getBaseLocation()));
            if (!courtLocations.isEmpty()) {
                return courtLocations.stream()
                    .filter(id -> id.getCourtTypeId().equals(CIVIL_COURT_TYPE_ID))
                    .findFirst()
                    .map(locationRefData -> isCourtCodeRequired
                        ? locationRefData.getCourtLocationCode() : locationRefData.getCourtName())
                    .orElse("");
            } else {
                log.info("Court location not found for caseId {}", caseData.getCcdCaseReference());
                return "";
            }
        }
    }

    public static String getLiveCourtByEpimmsId(String epimmsId) {
        return epimmsId != null ? Optional.ofNullable(liveEpimmsIdMap.get(epimmsId)).orElse(epimmsId) : epimmsId;
    }
}
