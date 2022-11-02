package uk.gov.hmcts.reform.civil.service.referencedata;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.civil.config.referencedata.LRDConfiguration;
import uk.gov.hmcts.reform.civil.model.referencedata.response.LocationRefData;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import static org.apache.logging.log4j.util.Strings.concat;

@Slf4j
@Service
@RequiredArgsConstructor
public class LocationRefDataService {

    private final RestTemplate restTemplate;
    private final LRDConfiguration lrdConfiguration;
    private final AuthTokenGenerator authTokenGenerator;

    public List<String> getCourtLocations(String authToken) {
        try {
            ResponseEntity<List<LocationRefData>> responseEntity = restTemplate.exchange(
                buildURI(),
                HttpMethod.GET,
                getHeaders(authToken),
                new ParameterizedTypeReference<List<LocationRefData>>() {
                }
            );
            return onlyEnglandAndWalesLocations(responseEntity.getBody());
        } catch (Exception e) {
            log.error("Location Reference Data Lookup Failed - " + e.getMessage(), e);
        }
        return new ArrayList<>();
    }

    public LocationRefData getCourtLocation(String authToken, String epimsId) {
        try {
            ResponseEntity<List<LocationRefData>> responseEntity = restTemplate.exchange(
                buildURI(),
                HttpMethod.GET,
                getHeaders(authToken),
                new ParameterizedTypeReference<List<LocationRefData>>() {
                }
            );
            var response = responseEntity.getBody();
            if (response == null || response.isEmpty()) {
                log.warn("Location Reference Data Lookup did not return any location");
                return null;
            } else {
                var locations = response.stream()
                    .filter(l -> l.getEpimmsId().equals(epimsId)).collect(Collectors.toList());
                return locations.get(0);
            }
        } catch (Exception e) {
            log.error("Location Reference Data Lookup Failed - " + e.getMessage(), e);
            return null;
        }
    }

    public LocationRefData getCcmccLocation(String authToken) {
        try {
            ResponseEntity<List<LocationRefData>> responseEntity = restTemplate.exchange(
                buildURIforCcmcc(),
                HttpMethod.GET,
                getHeaders(authToken),
                new ParameterizedTypeReference<List<LocationRefData>>() {});
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

    private URI buildURI() {
        String queryURL = lrdConfiguration.getUrl() + lrdConfiguration.getEndpoint();
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(queryURL)
            .queryParam("is_hearing_location", "Y")
            .queryParam("location_type", "Court");
        return builder.buildAndExpand(new HashMap<>()).toUri();
    }

    private URI buildURIforCcmcc() {
        String queryURL = lrdConfiguration.getUrl() + lrdConfiguration.getEndpoint();
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(queryURL)
            .queryParam("court_venue_name", "County Court Money Claims Centre");
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

    private HttpEntity<String> getHeaders(String authToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", authToken);
        headers.add("ServiceAuthorization", authTokenGenerator.generate());
        return new HttpEntity<>(headers);
    }

    private List<String> onlyEnglandAndWalesLocations(List<LocationRefData> locationRefData) {
        return locationRefData == null
            ? new ArrayList<>()
            : locationRefData.stream().filter(location -> !"Scotland".equals(location.getRegion()))
            .map(LocationRefDataService::getDisplayEntry).collect(Collectors.toList());
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
}
