package uk.gov.hmcts.reform.civil.service.refdata;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.client.LocationReferenceDataApiClient;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CourtVenueService {

    private final LocationReferenceDataApiClient locationRefDataApiClient;

    private static final String CIVIL_COURT_TYPE_ID = "10";
    private static final String IS_HEARING_LOCATION = "Y";
    private static final String IS_CASE_MANAGEMENT_LOCATION = "Y";
    private static final String LOCATION_TYPE = "Court";
    private static final String COURT_STATUS = "Open";

    public CourtVenueService(LocationReferenceDataApiClient locationRefDataApiClient) {
        this.locationRefDataApiClient = locationRefDataApiClient;
    }

    /**
     * Public cached method that fetches all civil courts from the API.
     * All filtering will use this cached data.
     */
    @Cacheable(value = "courtVenueCache", key = "'allLocations'")
    public List<LocationRefData> fetchAllCivilCourts(String serviceAuth, String auth) {
        log.info("[CourtVenueService] Cache MISS → calling Location Reference Data API to fetch all courts");
        return locationRefDataApiClient.getAllCivilCourtVenues(serviceAuth, auth, CIVIL_COURT_TYPE_ID, LOCATION_TYPE);
    }

    /**
     * Helper to filter cached court data.
     */
    private List<LocationRefData> filterCachedCourts(String serviceAuth, String auth, Predicate<LocationRefData> predicate) {
        return fetchAllCivilCourts(serviceAuth, auth).stream()
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
