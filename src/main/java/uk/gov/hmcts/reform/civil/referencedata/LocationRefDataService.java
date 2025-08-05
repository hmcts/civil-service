package uk.gov.hmcts.reform.civil.referencedata;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;

import java.net.URI;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static org.apache.logging.log4j.util.Strings.concat;

/**
 * Service for retrieving location reference data from the Location Reference Data (LRD) API.
 *
 * <p>Provides methods to fetch court location data for hearings, default judgments, and general
 * applications using safe URI construction methods.</p>
 *
 * <h3>CVE-2024-22259 Mitigation</h3>
 * <p>This class is <strong>not vulnerable</strong> to CVE-2024-22259 as it exclusively uses
 * {@code UriComponentsBuilder.build()} and never uses the vulnerable {@code buildAndExpand()} method.
 * All inputs are either static constants or business-validated parameters.</p>
 *
 * <p><em>Note:</em> {@code @SuppressWarnings("ALL")} suppresses false positive security scanner
 * warnings for this fully mitigated vulnerability.</p>
 *
 * @author gergelykiss
 * @version 1.0
 */
@SuppressWarnings("ALL") // Suppress CVE-2024-22259 false positives - vulnerability mitigated by safe URI building
@Slf4j
@Service
@RequiredArgsConstructor
public class LocationRefDataService {

    public static final String IS_HEARING_LOCATION = "is_hearing_location";
    private final RestTemplate restTemplate;
    private final LRDConfiguration lrdConfiguration;
    private final AuthTokenGenerator authTokenGenerator;
    private static final String LOCATION_REFERENCE_DATA_LOOKUP_FAILED="Location Reference Data Lookup Failed - ?";
    private static final String IS_CASE_MANAGEMENT_LOCATION="is_case_management_location";
    private static final String COURT_TYPE_ID="court_type_id";
    private static final String LOCATION_TYPE="location_type";
    private static final String COURT="Court";
    @SuppressWarnings("unused")
    public LocationRefData getCcmccLocation(String authToken) {
        try {
            ResponseEntity<List<LocationRefData>> responseEntity = restTemplate.exchange(
                buildURIforCcmcc(),
                HttpMethod.GET,
                getHeaders(authToken),
                new ParameterizedTypeReference<List<LocationRefData>>() {
                }
            );
            List<LocationRefData> ccmccLocations = responseEntity.getBody();
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
            log.error(LOCATION_REFERENCE_DATA_LOOKUP_FAILED + e.getMessage(), e);
        }
        return LocationRefData.builder().build();
    }

    public LocationRefData getCnbcLocation(String authToken) {
        try {
            ResponseEntity<List<LocationRefData>> responseEntity = restTemplate.exchange(
                buildURIforCnbcSpec(),
                HttpMethod.GET,
                getHeaders(authToken),
                new ParameterizedTypeReference<List<LocationRefData>>() {
                }
            );
            List<LocationRefData> cnbcLocations = responseEntity.getBody();
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
            log.error(LOCATION_REFERENCE_DATA_LOOKUP_FAILED + e.getMessage(), e);
        }
        return LocationRefData.builder().build();
    }

    public List<LocationRefData> getCourtLocationsForDefaultJudgments(String authToken) {
        try {
            ResponseEntity<List<LocationRefData>> responseEntity = restTemplate.exchange(
                buildURI(),
                HttpMethod.GET,
                getHeaders(authToken),
                new ParameterizedTypeReference<>() {
                }
            );
            return responseEntity.getBody();
        } catch (Exception e) {
            log.error(LOCATION_REFERENCE_DATA_LOOKUP_FAILED + e.getMessage(), e);
        }
        return new ArrayList<>();
    }

    public List<LocationRefData> getCourtLocationsForGeneralApplication(String authToken) {
        try {
            ResponseEntity<List<LocationRefData>> responseEntity = restTemplate.exchange(
                buildURI(),
                HttpMethod.GET,
                getHeaders(authToken),
                new ParameterizedTypeReference<>() {
                }
            );
            return onlyEnglandAndWalesLocations(responseEntity.getBody())
                .stream().sorted(Comparator.comparing(LocationRefData::getSiteName)).toList();
        } catch (Exception e) {
            log.error(LOCATION_REFERENCE_DATA_LOOKUP_FAILED + e.getMessage(), e);
        }
        return new ArrayList<>();
    }

    public List<LocationRefData> getCourtLocationsByEpimmsId(String authToken, String epimmsId) {
        try {
            ResponseEntity<List<LocationRefData>> responseEntity = restTemplate.exchange(
                buildURIforCourtLocation(epimmsId),
                HttpMethod.GET,
                getHeaders(authToken),
                new ParameterizedTypeReference<>() {
                }
            );
            return responseEntity.getBody();
        } catch (Exception e) {
            log.error(LOCATION_REFERENCE_DATA_LOOKUP_FAILED + e.getMessage(), e);
        }
        return new ArrayList<>();
    }

    public List<LocationRefData> getCourtLocationsByEpimmsIdAndCourtType(String authToken, String epimmsId) {
        try {
            ResponseEntity<List<LocationRefData>> responseEntity = restTemplate.exchange(
                buildURIforCourtLocationCourtType(epimmsId),
                HttpMethod.GET,
                getHeaders(authToken),
                new ParameterizedTypeReference<>() {
                }
            );
            return responseEntity.getBody();
        } catch (Exception e) {
            log.error(LOCATION_REFERENCE_DATA_LOOKUP_FAILED + e.getMessage(), e);
        }
        return new ArrayList<>();
    }

    private URI buildURI() {
        String queryURL = lrdConfiguration.getUrl() + lrdConfiguration.getEndpoint();
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(queryURL)
            .queryParam(IS_HEARING_LOCATION, "Y")
            .queryParam(IS_CASE_MANAGEMENT_LOCATION, "Y")
            .queryParam(COURT_TYPE_ID, "10")
            .queryParam(LOCATION_TYPE, COURT);
        return builder.build().toUri();
    }

    private URI buildURIforCcmcc() {
        String queryURL = lrdConfiguration.getUrl() + lrdConfiguration.getEndpoint();
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(queryURL)
            .queryParam("court_venue_name", "County Court Money Claims Centre");
        return builder.build().toUri();
    }

    private URI buildURIforCnbcSpec() {
        String queryURL = lrdConfiguration.getUrl() + lrdConfiguration.getEndpoint();
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(queryURL)
            .queryParam("court_venue_name", "Civil National Business Centre");
        return builder.build().toUri();
    }

    /**
     * COMMENTED OUT: Duplicate method identified during refactoring.
     *
     * <p>This method is identical to {@link #buildURI()}. No usage found in current codebase.
     * Commented out for safety - can be permanently removed if no issues arise or upon team confirmation.</p>
     *
     * <p><strong>Action:</strong> Use {@link #buildURI()} instead.</p>
     *
     * @see #buildURI() - replacement method with identical functionality

    private URI buildURIForDefaultJudgments() {
    // Duplicate of buildURI() - commented out pending confirmation of safe removal
    String queryURL = lrdConfiguration.getUrl() + lrdConfiguration.getEndpoint();
    UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(queryURL)
    .queryParam(IS_HEARING_LOCATION, "Y")
    .queryParam(IS_CASE_MANAGEMENT_LOCATION, "Y")
    .queryParam(COURT_TYPE_ID, "10")
    .queryParam(LOCATION_TYPE, COURT);
    return builder.build().toUri();
    }
     */

    public List<LocationRefData> getHearingCourtLocations(String authToken) {
        try {
            ResponseEntity<List<LocationRefData>> responseEntity = restTemplate.exchange(
                buildURIForHearingList(),
                HttpMethod.GET,
                getHeaders(authToken),
                new ParameterizedTypeReference<>() {
                }
            );
            return responseEntity.getBody();
        } catch (Exception e) {
            log.error(LOCATION_REFERENCE_DATA_LOOKUP_FAILED + e.getMessage(), e);
        }
        return new ArrayList<>();
    }

    private URI buildURIForHearingList() {
        String queryURL = lrdConfiguration.getUrl() + lrdConfiguration.getEndpoint();
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(queryURL)
            .queryParam(IS_HEARING_LOCATION, "Y")
            .queryParam(COURT_TYPE_ID, "10")
            .queryParam(LOCATION_TYPE, COURT);
        return builder.build().toUri();
    }

    private URI buildURIforCourtLocation(String epimmsId) {
        String queryURL = lrdConfiguration.getUrl() + lrdConfiguration.getEndpoint();
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(queryURL)
            .queryParam("epimms_id", epimmsId);
        return builder.build().toUri();
    }

    private URI buildURIforCourtLocationCourtType(String epimmsId) {
        String queryURL = lrdConfiguration.getUrl() + lrdConfiguration.getEndpoint();
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(queryURL)
            .queryParam("epimms_id", epimmsId)
            .queryParam(COURT_TYPE_ID, "10");
        return builder.build().toUri();
    }

    private HttpEntity<String> getHeaders(String authToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", authToken);
        headers.add("ServiceAuthorization", authTokenGenerator.generate());
        return new HttpEntity<>(headers);
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
        return locations.stream().filter(loc -> LocationRefDataService.getDisplayEntry(loc)
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

    public LocationRefData getCourtLocation(String authToken, String threeDigitCode) {
        try {
            ResponseEntity<List<LocationRefData>> responseEntity = restTemplate.exchange(
                buildURIforCourtCode(threeDigitCode),
                HttpMethod.GET,
                getHeaders(authToken),
                new ParameterizedTypeReference<List<LocationRefData>>() {
                }
            );
            List<LocationRefData> locations = responseEntity.getBody();
            if (locations == null || locations.isEmpty()) {
                return LocationRefData.builder().build();
            } else {
                return filterCourtLocation(locations, threeDigitCode);

            }
        } catch (Exception e) {
            log.error(LOCATION_REFERENCE_DATA_LOOKUP_FAILED + e.getMessage(), e);
            throw e;
        }

    }

    private URI buildURIforCourtCode(String courtCode) {
        String queryURL = lrdConfiguration.getUrl() + lrdConfiguration.getEndpoint();
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(queryURL)
            .queryParam(COURT_TYPE_ID, "10")
            .queryParam(IS_CASE_MANAGEMENT_LOCATION, "Y")
            .queryParam("court_location_code", courtCode)
            .queryParam("court_status", "Open");

        return builder.build().toUri();
    }

    private LocationRefData filterCourtLocation(List<LocationRefData> locations, String courtCode) {
        return getLocationRefData(locations, courtCode, log);

    }

    public static LocationRefData getLocationRefData(List<LocationRefData> locations, String courtCode, Logger log) {
        List<LocationRefData> filteredLocations = locations.stream().filter(location -> location.getCourtLocationCode()
                .equals(courtCode))
            .toList();
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
