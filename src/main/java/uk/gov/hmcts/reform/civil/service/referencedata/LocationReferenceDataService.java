package uk.gov.hmcts.reform.civil.service.referencedata;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.civil.client.LocationReferenceDataApiClient;
import uk.gov.hmcts.reform.civil.referencedata.LocationRefDataException;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static org.apache.logging.log4j.util.Strings.concat;
import static org.apache.logging.log4j.util.Strings.isNotBlank;

@Slf4j
@Service
@RequiredArgsConstructor
public class LocationReferenceDataService {

    public static final String CIVIL_NATIONAL_BUSINESS_CENTRE = "Civil National Business Centre";
    public static final String COUNTY_COURT_MONEY_CLAIMS_CENTRE = "County Court Money Claims Centre";
    private final LocationReferenceDataApiClient locationReferenceDataApiClient;
    private final AuthTokenGenerator authTokenGenerator;
    private static final String CIVIL_COURT_TYPE_ID = "10";
    private static final String IS_HEARING_LOCATION = "Y";
    private static final String IS_CASE_MANAGEMENT_LOCATION = "Y";
    private static final String LOCATION_TYPE = "Court";
    private static final String COURT_STATUS = "Open";
    private static final String LOCATION_REFERENCE_DATA_LOOKUP_FAILED = "Location Reference Data Lookup Failed - {}";

    public LocationRefData getCnbcLocation(String authToken) {
        try {
            List<LocationRefData> cnbcLocations =
                locationReferenceDataApiClient.getCourtVenueByName(
                    authTokenGenerator.generate(),
                    authToken,
                    CIVIL_NATIONAL_BUSINESS_CENTRE
                );
            if (cnbcLocations == null || cnbcLocations.isEmpty()) {
                log.warn("Location Reference Data Lookup did not return any CNBC location");
                return LocationRefData.builder().build();
            } else {
                if (cnbcLocations.size() > 1) {
                    log.warn("Location Reference Data Lookup returned more than one CNBC location");
                }
                return cnbcLocations.get(0);
            }
        } catch (Exception e) {
            log.error(LOCATION_REFERENCE_DATA_LOOKUP_FAILED, e.getMessage(), e);
        }
        return LocationRefData.builder().build();
    }

    public LocationRefData getCcmccLocation(String authToken) {
        try {
            List<LocationRefData> ccmccLocations =
                locationReferenceDataApiClient.getCourtVenueByName(
                    authTokenGenerator.generate(),
                    authToken,
                    COUNTY_COURT_MONEY_CLAIMS_CENTRE
                );
            if (ccmccLocations == null || ccmccLocations.isEmpty()) {
                log.warn("Location Reference Data Lookup did not return any CCMCC location");
                return LocationRefData.builder().build();
            } else {
                if (ccmccLocations.size() > 1) {
                    log.warn("Location Reference Data Lookup returned more than one CCMCC location");
                }
                return ccmccLocations.get(0);
            }
        } catch (Exception e) {
            log.error(LOCATION_REFERENCE_DATA_LOOKUP_FAILED, e.getMessage(), e);
        }
        return LocationRefData.builder().build();
    }

    public List<LocationRefData> getCourtLocationsForDefaultJudgments(String authToken) {
        try {
            return
                locationReferenceDataApiClient.getCourtVenue(
                    authTokenGenerator.generate(),
                    authToken,
                    IS_HEARING_LOCATION,
                    IS_CASE_MANAGEMENT_LOCATION,
                    CIVIL_COURT_TYPE_ID,
                    LOCATION_TYPE
                );

        } catch (Exception e) {
            log.error(LOCATION_REFERENCE_DATA_LOOKUP_FAILED, e.getMessage(), e);
        }
        return new ArrayList<>();
    }

    public List<LocationRefData> getCourtLocationsForGeneralApplication(String authToken) {
        try {
            List<LocationRefData> responseEntity =
                locationReferenceDataApiClient.getCourtVenue(
                    authTokenGenerator.generate(),
                    authToken,
                    IS_HEARING_LOCATION,
                    IS_CASE_MANAGEMENT_LOCATION,
                    CIVIL_COURT_TYPE_ID,
                    LOCATION_TYPE
                );
            return onlyEnglandAndWalesLocations(responseEntity)
                .stream().sorted(Comparator.comparing(LocationRefData::getSiteName)).toList();
        } catch (Exception e) {
            log.error(LOCATION_REFERENCE_DATA_LOOKUP_FAILED, e.getMessage(), e);
        }
        return new ArrayList<>();
    }

    public List<LocationRefData> getCourtLocationsByEpimmsId(String authToken, String epimmsId) {
        return getCourtLocationsByEpimmsIdAndCourtType(authToken, epimmsId);
    }

    public List<LocationRefData> getCourtLocationsByEpimmsIdAndCourtType(String authToken, String epimmsId) {
        try {
            return locationReferenceDataApiClient.getCourtVenueByEpimmsIdAndType(
                authTokenGenerator.generate(),
                authToken, epimmsId, CIVIL_COURT_TYPE_ID
                );
        } catch (Exception e) {
            log.error(LOCATION_REFERENCE_DATA_LOOKUP_FAILED, e.getMessage(), e);
        }
        return new ArrayList<>();
    }

    /**
     * Returns the list of locations that can then be added in dynamic list on the
     * Judge Assisted order screen, SDO and Hearing Schedule Venue list.
     *
     * @param authToken BEARER_TOKEN from CallbackParams
     * @return List of Hearing court Locations / Venues
     */
    public List<LocationRefData> getHearingCourtLocations(String authToken) {
        try {
            return locationReferenceDataApiClient.getHearingVenue(
                authTokenGenerator.generate(),
                authToken, IS_HEARING_LOCATION, CIVIL_COURT_TYPE_ID, LOCATION_TYPE
                );
        } catch (Exception e) {
            log.error(LOCATION_REFERENCE_DATA_LOOKUP_FAILED, e.getMessage(), e);
        }
        return new ArrayList<>();
    }

    private List<LocationRefData> onlyEnglandAndWalesLocations(List<LocationRefData> locationRefData) {
        return locationRefData == null
            ? new ArrayList<>()
            : locationRefData.stream().filter(location -> !"Scotland".equals(location.getRegion()))
            .toList();
    }

    public Optional<LocationRefData> getLocationMatchingLabel(String label, String bearerToken) {
        if (StringUtils.isBlank(label)) {
            return Optional.empty();
        }

        List<LocationRefData> locations = getHearingCourtLocations(bearerToken);
        return locations.stream().filter(loc -> LocationReferenceDataService.getDisplayEntry(loc)
                .equals(label))
            .findFirst();
    }

    /**
     * Label is siteName - courtAddress - postCode.
     *
     * @param location a location
     * @return string to serve as label
     */
    public static String getDisplayEntry(LocationRefData location) {
        return concat(
            concat(concat(location.getSiteName(), " - "), concat(location.getCourtAddress(), " - ")),
            location.getPostcode()
        );
    }

    /**
     * Label is siteName - courtAddress - postCode.
     *
     * @param location a location
     * @return string to serve as label
     */
    public static String getDisplayEntryWelsh(LocationRefData location) {
        return concat(
            concat(concat(
                isNotBlank(location.getWelshSiteName()) ? location.getWelshSiteName() : location.getSiteName(),
                          " - "), concat(location.getCourtAddress(), " - ")),
            location.getPostcode()
        );
    }

    public LocationRefData getCourtLocation(String authToken, String threeDigitCode) {
        try {
            List<LocationRefData> responseEntity =
                locationReferenceDataApiClient.getCourtVenueByLocationCode(
                    authTokenGenerator.generate(),
                    authToken, IS_CASE_MANAGEMENT_LOCATION, CIVIL_COURT_TYPE_ID, threeDigitCode, COURT_STATUS
                );
            List<LocationRefData> locations = responseEntity;
            if (locations == null || locations.isEmpty()) {
                return LocationRefData.builder().build();
            } else {
                return filterCourtLocation(locations, threeDigitCode);

            }
        } catch (Exception e) {
            log.error(LOCATION_REFERENCE_DATA_LOOKUP_FAILED, e.getMessage(), e);
            throw e;
        }

    }

    private LocationRefData filterCourtLocation(List<LocationRefData> locations, String courtCode) {
        List<LocationRefData> filteredLocations = locations.stream().filter(location -> location.getCourtLocationCode()
                .equals(courtCode)).toList();
        if (filteredLocations.isEmpty()) {
            log.warn("No court Location Found for three digit court code : {}", courtCode);
            throw new LocationRefDataException("No court Location Found for three digit court code : " + courtCode);
        } else if (filteredLocations.size() > 1) {
            log.warn("More than one court location found : {}", courtCode);
            throw new LocationRefDataException("More than one court location found : " + courtCode);
        }
        return filteredLocations.get(0);
    }
}
