package uk.gov.hmcts.reform.civil.service;

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
import uk.gov.hmcts.reform.civil.config.GeneralAppLRDConfiguration;
import uk.gov.hmcts.reform.civil.model.genapplication.LocationRefData;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import static org.apache.logging.log4j.util.Strings.concat;

@Slf4j
@Service
@RequiredArgsConstructor
public class GeneralAppLocationRefDataService {

    private final RestTemplate restTemplate;
    private final GeneralAppLRDConfiguration lrdConfiguration;
    private final AuthTokenGenerator authTokenGenerator;

    public List<String> getCourtLocations(String authToken) {
        try {
            ResponseEntity<List<LocationRefData>> responseEntity = restTemplate.exchange(
                    buildURI(),
                    HttpMethod.GET,
                    getHeaders(authToken),
                    new ParameterizedTypeReference<List<LocationRefData>>() {});
            return onlyEnglandAndWalesLocations(responseEntity.getBody());
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
                .map(this::getDisplayEntry).collect(Collectors.toList());
    }

    private String getDisplayEntry(LocationRefData location) {
        return concat(concat(location.getSiteName(), " - "), location.getPostcode());
    }
}
