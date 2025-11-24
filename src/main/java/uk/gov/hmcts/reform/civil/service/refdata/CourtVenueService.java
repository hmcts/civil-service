package uk.gov.hmcts.reform.civil.service.refdata;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.model.referencedata.LocationRefData;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CourtVenueService {

    private final RdClientService rdClientService;

    private static final String IS_HEARING_LOCATION = "Y";
    private static final String IS_CASE_MANAGEMENT_LOCATION = "Y";
    private static final String COURT_STATUS = "Open";

    public CourtVenueService(RdClientService rdClientService) {
        this.rdClientService = rdClientService;
    }

    /**
     * Helper to filter cached court data.
     */
    private List<LocationRefData> filterCachedCourts(String serviceAuth, String auth, Predicate<LocationRefData> predicate) {
        return rdClientService.fetchAllCivilCourts(serviceAuth, auth).stream()
            .filter(predicate)
            .collect(Collectors.toList());
    }

    public List<LocationRefData> getCourtByEpimmsId(String serviceAuth, String auth, String epimmsId) {
        log.info("Fetching courts by epimms id: {}", epimmsId);
        return filterCachedCourts(serviceAuth, auth, c -> epimmsId.equals(c.getEpimmsId()));
    }

    public List<LocationRefData> getCMLCourtByEpimmsId(String serviceAuth, String auth, String epimmsId) {
        log.info("Fetching CML courts by epimms id: {}", epimmsId);
        return filterCachedCourts(serviceAuth, auth, c ->
            epimmsId.equals(c.getEpimmsId())
                && IS_CASE_MANAGEMENT_LOCATION.equals(c.getIsCaseManagementLocation())
        );
    }

    public List<LocationRefData> getCourtVenueByName(String serviceAuth, String auth, String courtName) {
        log.info("Fetching court by name: {}", courtName);
        return filterCachedCourts(serviceAuth, auth, c -> courtName.equalsIgnoreCase(c.getCourtName()));
    }

    public List<LocationRefData> getByRegion(String serviceAuth, String auth, String region) {
        log.info("Fetching courts by region: {}", region);
        return filterCachedCourts(serviceAuth, auth, c -> region.equalsIgnoreCase(c.getRegion()));
    }

    public List<LocationRefData> getByRegionId(String serviceAuth, String auth, String regionId) {
        log.info("Fetching courts by region ID: {}", regionId);
        return filterCachedCourts(serviceAuth, auth, c -> regionId.equals(c.getRegionId()));
    }

    public List<LocationRefData> getByLocationType(String serviceAuth, String auth, String locationType) {
        log.info("Fetching courts by location type: {}", locationType);
        return filterCachedCourts(serviceAuth, auth, c -> locationType.equalsIgnoreCase(c.getLocationType()));
    }

    public List<LocationRefData> getCourtVenueByLocationCode(String serviceAuth, String auth, String threeDigitCode) {
        log.info("Fetching courts by court location three digit code: {}", threeDigitCode);
        return filterCachedCourts(serviceAuth, auth, c ->
            threeDigitCode.equalsIgnoreCase(c.getCourtLocationCode())
                && COURT_STATUS.equalsIgnoreCase(c.getCourtStatus())
                && IS_CASE_MANAGEMENT_LOCATION.equalsIgnoreCase(c.getIsCaseManagementLocation())
        );
    }

    public List<LocationRefData> getCourtByWelshSiteName(String serviceAuth, String auth, String welshSiteName) {
        log.info("Fetching courts by welsh site name: {}", welshSiteName);
        return filterCachedCourts(serviceAuth, auth, c -> welshSiteName.equalsIgnoreCase(c.getWelshSiteName()));
    }

    public List<LocationRefData> getHearingLocationCourts(String serviceAuth, String auth) {
        return filterCachedCourts(serviceAuth, auth, c -> IS_HEARING_LOCATION.equals(c.getIsHearingLocation()));
    }

    public List<LocationRefData> getCMLAndHLCourts(String serviceAuth, String auth) {
        return filterCachedCourts(serviceAuth, auth, this::isCMLAndHL);
    }

    private boolean isCMLAndHL(LocationRefData court) {
        return IS_CASE_MANAGEMENT_LOCATION.equalsIgnoreCase(court.getIsCaseManagementLocation())
            && IS_HEARING_LOCATION.equalsIgnoreCase(court.getIsHearingLocation());
    }
}
