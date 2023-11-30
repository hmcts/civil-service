package uk.gov.hmcts.reform.civil.service;

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
import uk.gov.hmcts.reform.civil.referencedata.LRDConfiguration;
import uk.gov.hmcts.reform.civil.referencedata.LocationRefDataService;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Slf4j
@Service
public class LocationRefDataServiceHelper extends LocationRefDataService {

    private final RestTemplate restTemplate;
    private final LRDConfiguration lrdConfiguration;
    private final AuthTokenGenerator authTokenGenerator;

    public LocationRefDataServiceHelper(RestTemplate restTemplate,
                                        LRDConfiguration lrdConfiguration,
                                        AuthTokenGenerator authTokenGenerator) {
        super(restTemplate, lrdConfiguration, authTokenGenerator);
        this.restTemplate = restTemplate;
        this.lrdConfiguration = lrdConfiguration;
        this.authTokenGenerator = authTokenGenerator;
    }

    @Override
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

    private URI buildURIForDefaultJudgments() {
        String queryURL = lrdConfiguration.getUrl() + lrdConfiguration.getEndpoint();
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(queryURL)
            .queryParam("is_hearing_location", "Y")
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
}
