package uk.gov.hmcts.reform.civil.service.refdata;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;

import java.util.List;
import java.util.function.Predicate;

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
    private List<LocationRefData> filterCachedCourtsByServiceId(String serviceAuth,
                                                                String auth,
                                                                Predicate<LocationRefData> predicate,
                                                                String serviceId) {
        return rdClientService.fetchAllCivilCourtsByServiceId(serviceAuth, auth, serviceId).stream()
            .filter(predicate)
            .toList();
    }

    public List<LocationRefData> getCourtByEpimmsId(String serviceAuth, String auth, String epimmsId, String serviceId) {
        log.info("Fetching courts by epims id: {} and serviceId {}", epimmsId, serviceId);
        if (epimmsId == null) {
            return List.of();
        }
        return filterCachedCourtsByServiceId(serviceAuth, auth, c -> epimmsId.equals(c.getEpimmsId()), serviceId);
    }

    public List<LocationRefData> getCMLCourtByEpimmsId(String serviceAuth, String auth, String epimmsId, String serviceId) {
        log.info("Fetching CML courts by epimms id: {} and serviceId {}", epimmsId, serviceId);
        if (epimmsId == null) {
            return List.of();
        }
        return filterCachedCourtsByServiceId(serviceAuth, auth, c ->
            epimmsId.equals(c.getEpimmsId())
                && IS_CASE_MANAGEMENT_LOCATION.equals(c.getIsCaseManagementLocation()), serviceId
        );
    }

    public List<LocationRefData> getCourtVenueByName(String serviceAuth, String auth, String courtName, String serviceId) {
        log.info("Fetching court by name: {} and serviceId {}", courtName, serviceId);
        return filterCachedCourtsByServiceId(serviceAuth, auth, c -> courtName.equalsIgnoreCase(c.getCourtName()), serviceId);
    }

    public List<LocationRefData> getByRegion(String serviceAuth, String auth, String region, String serviceId) {
        log.info("Fetching courts by region: {} and serviceId {}", region, serviceId);
        return filterCachedCourtsByServiceId(serviceAuth, auth, c -> region.equalsIgnoreCase(c.getRegion()), serviceId);
    }

    public List<LocationRefData> getByRegionId(String serviceAuth, String auth, String regionId, String serviceId) {
        log.info("Fetching courts by region ID: {} and serviceId {}", regionId, serviceId);
        return filterCachedCourtsByServiceId(serviceAuth, auth, c -> regionId.equals(c.getRegionId()), serviceId);
    }

    public List<LocationRefData> getByLocationType(String serviceAuth, String auth, String locationType, String serviceId) {
        log.info("Fetching courts by location type: {} and serviceId {}", locationType, serviceId);
        return filterCachedCourtsByServiceId(serviceAuth, auth, c -> locationType.equalsIgnoreCase(c.getLocationType()), serviceId);
    }

    public List<LocationRefData> getCourtVenueByLocationCode(String serviceAuth,
                                                                         String auth,
                                                                         String threeDigitCode,
                                                                         String serviceId) {
        log.info("Fetching courts by court location three digit code: {} and serviceId {}", threeDigitCode, serviceId);
        return filterCachedCourtsByServiceId(serviceAuth, auth, c ->
            threeDigitCode.equalsIgnoreCase(c.getCourtLocationCode())
                && COURT_STATUS.equalsIgnoreCase(c.getCourtStatus())
                && IS_CASE_MANAGEMENT_LOCATION.equalsIgnoreCase(c.getIsCaseManagementLocation()), serviceId
        );
    }

    public List<LocationRefData> getCourtByWelshSiteName(String serviceAuth, String auth, String welshSiteName, String serviceId) {
        log.info("Fetching courts by welsh site name: {} and serviceId {}", welshSiteName, serviceId);
        return filterCachedCourtsByServiceId(serviceAuth, auth, c -> welshSiteName.equalsIgnoreCase(c.getWelshSiteName()), serviceId);
    }

    public List<LocationRefData> getHearingLocationCourts(String serviceAuth, String auth, String serviceId) {
        return filterCachedCourtsByServiceId(serviceAuth, auth, c -> IS_HEARING_LOCATION.equals(c.getIsHearingLocation()), serviceId);
    }

    public List<LocationRefData> getCMLAndHLCourts(String serviceAuth, String auth, String serviceId) {
        return filterCachedCourtsByServiceId(serviceAuth, auth, this::isCMLAndHL, serviceId);
    }

    private boolean isCMLAndHL(LocationRefData court) {
        return IS_CASE_MANAGEMENT_LOCATION.equalsIgnoreCase(court.getIsCaseManagementLocation())
            && IS_HEARING_LOCATION.equalsIgnoreCase(court.getIsHearingLocation());
    }
}
