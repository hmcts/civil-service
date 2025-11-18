package uk.gov.hmcts.reform.civil.referencedata;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.apache.logging.log4j.util.Strings.concat;

@Slf4j
@Service
@RequiredArgsConstructor
public class LocationRefDataService {

    private final RestTemplate restTemplate;
    private final LRDConfiguration lrdConfiguration;
    private final AuthTokenGenerator authTokenGenerator;

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
            log.error("Location Reference Data Lookup Failed - " + e.getMessage(), e);
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
            log.error("Location Reference Data Lookup Failed - " + e.getMessage(), e);
        }
        return LocationRefData.builder().build();
    }

    public List<LocationRefData> getCourtLocationsForDefaultJudgments(String authToken) {
        try {
            ResponseEntity<List<LocationRefData>> responseEntity = restTemplate.exchange(
                buildURIForDefaultJudgments(),
                HttpMethod.GET,
                getHeaders(authToken),
                new ParameterizedTypeReference<>() {
                }
            );
            return responseEntity.getBody();
        } catch (Exception e) {
            log.error("Location Reference Data Lookup Failed - " + e.getMessage(), e);
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
                .stream().sorted(Comparator.comparing(LocationRefData::getSiteName)).collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Location Reference Data Lookup Failed - " + e.getMessage(), e);
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
            log.error("Location Reference Data Lookup Failed - " + e.getMessage(), e);
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
            log.error("Location Reference Data Lookup Failed - " + e.getMessage(), e);
        }
        return new ArrayList<>();
    }

    private URI buildURI() {
        String queryURL = lrdConfiguration.getUrl() + lrdConfiguration.getEndpoint();
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(queryURL)
            .queryParam("is_hearing_location", "Y")
            .queryParam("is_case_management_location", "Y")
            .queryParam("court_type_id", "10")
            .queryParam("location_type", "Court");
        return builder.buildAndExpand(new HashMap<>()).toUri();
    }

    private URI buildURIforCcmcc() {
        String queryURL = lrdConfiguration.getUrl() + lrdConfiguration.getEndpoint();
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(queryURL)
            .queryParam("court_venue_name", "County Court Money Claims Centre");
        return builder.buildAndExpand(new HashMap<>()).toUri();
    }

    private URI buildURIforCnbcSpec() {
        String queryURL = lrdConfiguration.getUrl() + lrdConfiguration.getEndpoint();
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(queryURL)
            .queryParam("court_venue_name", "Civil National Business Centre");
        return builder.buildAndExpand(new HashMap<>()).toUri();
    }

    private URI buildURIForDefaultJudgments() {
        String queryURL = lrdConfiguration.getUrl() + lrdConfiguration.getEndpoint();
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(queryURL)
            .queryParam("is_hearing_location", "Y")
            .queryParam("is_case_management_location", "Y")
            .queryParam("court_type_id", "10")
            .queryParam("location_type", "Court");
        return builder.buildAndExpand(new HashMap<>()).toUri();
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
            ResponseEntity<List<LocationRefData>> responseEntity = restTemplate.exchange(
                buildURIForHearingList(),
                HttpMethod.GET,
                getHeaders(authToken),
                new ParameterizedTypeReference<>() {
                }
            );
            return responseEntity.getBody();
        } catch (Exception e) {
            log.error("Location Reference Data Lookup Failed - " + e.getMessage(), e);
        }
        return new ArrayList<>();
    }

    private URI buildURIForHearingList() {
        String queryURL = lrdConfiguration.getUrl() + lrdConfiguration.getEndpoint();
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(queryURL)
            .queryParam("is_hearing_location", "Y")
            .queryParam("court_type_id", "10")
            .queryParam("location_type", "Court");
        return builder.buildAndExpand(new HashMap<>()).toUri();
    }

    private URI buildURIforCourtLocation(String epimmsId) {
        String queryURL = lrdConfiguration.getUrl() + lrdConfiguration.getEndpoint();
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(queryURL)
            .queryParam("epimms_id", epimmsId);
        return builder.buildAndExpand(new HashMap<>()).toUri();
    }

    private URI buildURIforCourtLocationCourtType(String epimmsId) {
        String queryURL = lrdConfiguration.getUrl() + lrdConfiguration.getEndpoint();
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(queryURL)
            .queryParam("epimms_id", epimmsId)
            .queryParam("court_type_id", "10");
        return builder.buildAndExpand(new HashMap<>()).toUri();
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
            .collect(Collectors.toList());
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
            log.error("Location Reference Data Lookup Failed - " + e.getMessage(), e);
            throw e;
        }

    }

    private URI buildURIforCourtCode(String courtCode) {
        String queryURL = lrdConfiguration.getUrl() + lrdConfiguration.getEndpoint();
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(queryURL)
            .queryParam("court_type_id", "10")
            .queryParam("is_case_management_location", "Y")
            .queryParam("court_location_code", courtCode)
            .queryParam("court_status", "Open");

        return builder.buildAndExpand(new HashMap<>()).toUri();
    }

    private LocationRefData filterCourtLocation(List<LocationRefData> locations, String courtCode) {
        List<LocationRefData> filteredLocations = locations.stream().filter(location -> location.getCourtLocationCode()
                .equals(courtCode))
            .collect(Collectors.toList());
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
