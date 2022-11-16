package uk.gov.hmcts.reform.civil.service.stitching;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.BundleRequest;
import uk.gov.hmcts.reform.civil.model.CaseData;

import static java.util.Objects.requireNonNull;

@Slf4j
@Service
@RequiredArgsConstructor
public class BundleRequestExecutor {

    private static final String SERVICE_AUTHORIZATION = "ServiceAuthorization";

    private final RestTemplate restTemplate;
    private final AuthTokenGenerator serviceAuthTokenGenerator;
    private final CaseDetailsConverter caseDetailsConverter;

    private final ObjectMapper objectMapper;

    public CaseData post(final BundleRequest payload, final String endpoint, String authorisation) {
        requireNonNull(payload, "payload must not be null");
        requireNonNull(endpoint, "endpoint must not be null");

        final String serviceAuthorizationToken = serviceAuthTokenGenerator.generate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        headers.set(HttpHeaders.AUTHORIZATION, authorisation);
        headers.set(SERVICE_AUTHORIZATION, serviceAuthorizationToken);

        try {
            ResponseEntity<CaseDetails> response1 = restTemplate.exchange(
                endpoint,
                HttpMethod.POST,
                new HttpEntity<>(payload, headers),
                CaseDetails.class
            );
            if (response1.getStatusCode().equals(HttpStatus.OK)) {
                return caseDetailsConverter.toCaseData(requireNonNull(response1.getBody()));
            } else {
                log.warn("The call to the endpoint with URL {} returned a non positive outcome (HTTP-{}). This may "
                             + "cause problems down the line.", endpoint, response1.getStatusCode().value());
            }

        } catch (RestClientResponseException e) {
            log.debug(e.getMessage(), e);
            log.error("The call to the endpoint with URL {} failed. This is likely to cause problems down the line.",
                      endpoint);
            logRelevantInfoQuietly(e);
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private void logRelevantInfoQuietly(RestClientResponseException e) {
        try {
            log.error("  ^  HTTP Error: {}", e.getRawStatusCode());
            Map<String, Object> response = objectMapper.readValue(e.getResponseBodyAsString(), Map.class);

            List<String> errors = (List<String>)response.get("errors");
            errors.forEach(message -> log.error("  |  {}", message.substring(0, Math.min(message.length(), 250))));

        } catch (Throwable t) {
            log.warn("  ^  The details of the error could not be logged due to an exception while trying to log them."
                         + " Maybe the output could not be parsed as JSON? Maybe the service was unreachable?");
        }
    }

}

