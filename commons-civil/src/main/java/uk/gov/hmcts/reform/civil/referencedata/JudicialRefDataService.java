package uk.gov.hmcts.reform.civil.referencedata;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.civil.referencedata.model.JudgeRefData;
import uk.gov.hmcts.reform.civil.referencedata.model.JudgeSearchRequest;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class JudicialRefDataService {

    private final RestTemplate restTemplate;
    private final JRDConfiguration jrdConfiguration;
    private final AuthTokenGenerator authTokenGenerator;

    public List<JudgeRefData> getJudgeReferenceData(String searchString, String authToken) {
        JudgeSearchRequest jsr = JudgeSearchRequest.builder()
            .searchString(searchString)
            .build();

        HttpEntity<JudgeSearchRequest> request = new HttpEntity<>(jsr, getHeaders(authToken));

        try {
            ResponseEntity<List<JudgeRefData>> responseEntity = restTemplate.exchange(
                buildURI(),
                HttpMethod.POST,
                request,
                new ParameterizedTypeReference<List<JudgeRefData>>() {
                }
            );

            return responseEntity.getBody();
        } catch (Exception e) {
            log.error("Judicial Reference Data Lookup Failed - " + e.getMessage(), e);
        }

        return new ArrayList<>();
    }

    private URI buildURI() {
        String queryURL = jrdConfiguration.getUrl() + jrdConfiguration.getEndpoint();
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(queryURL);

        return builder.buildAndExpand(new HashMap<>()).toUri();
    }

    private HttpHeaders getHeaders(String authToken) {
        HttpHeaders headers = new HttpHeaders();

        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("Authorization", authToken);
        headers.add("ServiceAuthorization", authTokenGenerator.generate());

        return headers;
    }
}
