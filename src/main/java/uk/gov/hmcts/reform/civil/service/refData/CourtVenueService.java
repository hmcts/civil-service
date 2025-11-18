package uk.gov.hmcts.reform.civil.service.refData;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.client.LocationReferenceDataApiClient;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CourtVenueService {

    private final LocationReferenceDataApiClient locationRefDataApiClient;

    private static final String CIVIL_COURT_TYPE_ID = "10";

    public CourtVenueService(LocationReferenceDataApiClient locationRefDataApiClient) {
        this.locationRefDataApiClient = locationRefDataApiClient;
    }

    @Cacheable(value = "courtVenueCache", key = "'allLocations'")
    public List<LocationRefData> getAllCivilCourts(
        String serviceAuth,
        String auth
    ) {
        return locationRefDataApiClient.getAllCivilCourtVenues(serviceAuth, auth, CIVIL_COURT_TYPE_ID);
    }

    public List<LocationRefData> getByEpimmsId(String serviceAuth, String auth, String epimmsId) {
        log.info("Fetching courts by epimms id: {}", epimmsId);
        return getAllCivilCourts(serviceAuth, auth).stream()
            .filter(c -> epimmsId.equals(c.getEpimmsId()))
            .collect(Collectors.toList());
    }

    public LocationRefData getByCourtName(String serviceAuth, String auth, String courtName) {
        log.info("Fetching court by name: {}", courtName);
        return getAllCivilCourts(serviceAuth, auth).stream()
            .filter(c -> courtName.equalsIgnoreCase(c.getCourtName()))
            .findFirst()
            .orElse(null);
    }

    public List<LocationRefData> getByRegion(String serviceAuth, String auth, String region) {
        log.info("Fetching courts by region: {}", region);
        return getAllCivilCourts(serviceAuth, auth).stream()
            .filter(c -> region.equalsIgnoreCase(c.getRegion()))
            .collect(Collectors.toList());
    }

    public List<LocationRefData> getByRegionId(String serviceAuth, String auth, String regionId) {
        log.info("Fetching courts by region ID: {}", regionId);
        return getAllCivilCourts(serviceAuth, auth).stream()
            .filter(c -> regionId.equals(c.getRegionId()))
            .collect(Collectors.toList());
    }

    public List<LocationRefData> getByLocationType(String serviceAuth, String auth, String locationType) {
        log.info("Fetching courts by location type: {}", locationType);
        return getAllCivilCourts(serviceAuth, auth).stream()
            .filter(c -> locationType.equalsIgnoreCase(c.getLocationType()))
            .collect(Collectors.toList());
    }

    public List<LocationRefData> getCourtByWelshSiteName(String serviceAuth, String auth, String welshSiteName) {
        log.info("Fetching courts by welsh site type: {}", welshSiteName);
        return getAllCivilCourts(serviceAuth, auth).stream()
            .filter(c -> welshSiteName.equalsIgnoreCase(c.getWelshSiteName()))
            .collect(Collectors.toList());
    }
}
