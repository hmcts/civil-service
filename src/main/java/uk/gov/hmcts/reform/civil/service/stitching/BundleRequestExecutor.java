package uk.gov.hmcts.reform.civil.service.stitching;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientResponseException;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.client.EvidenceManagementApiClient;
import uk.gov.hmcts.reform.civil.exceptions.RetryableStitchingException;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.BundleRequest;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

@Slf4j
@Service
@RequiredArgsConstructor
public class BundleRequestExecutor {

    private static final String SERVICE_AUTHORIZATION = "ServiceAuthorization";

    private final EvidenceManagementApiClient evidenceManagementApiClient;
    private final AuthTokenGenerator serviceAuthTokenGenerator;
    private final CaseDetailsConverter caseDetailsConverter;

    private final ObjectMapper objectMapper;

    @Retryable(value = {RetryableStitchingException.class}, maxAttempts = 5, backoff = @Backoff(delay = 500))
    public Optional<CaseData> post(final BundleRequest payload, final String endpoint, String authorisation) {
        requireNonNull(payload, "payload must not be null");
        requireNonNull(endpoint, "endpoint must not be null");

        final String serviceAuthorizationToken = serviceAuthTokenGenerator.generate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        headers.set(HttpHeaders.AUTHORIZATION, authorisation);
        headers.set(SERVICE_AUTHORIZATION, serviceAuthorizationToken);

        try {
            ResponseEntity<CaseDetails> response1 = evidenceManagementApiClient.stitchBundle(authorisation,
                                                                                             serviceAuthorizationToken,
                                                                                             payload
            );
            if (response1.getStatusCode().equals(HttpStatus.OK)) {
                return Optional.of(caseDetailsConverter.toCaseData(requireNonNull(response1.getBody())));
            } else {
                log.warn("The call to the endpoint with URL {} returned a non positive outcome (HTTP-{}). This may "
                             + "cause problems down the line.", endpoint, response1.getStatusCode().value());
                log.info(
                    "Stitching endpoint returned {} with reason {}",
                    response1.getStatusCodeValue(),
                    response1.getStatusCode().getReasonPhrase()
                );
                throw new RetryableStitchingException();
            }

        } catch (RestClientResponseException e) {
            log.debug(e.getMessage(), e);
            log.error(
                "The call to the endpoint with URL {} failed. This is likely to cause problems down the line.",
                endpoint
            );
            logRelevantInfoQuietly(e);
            throw new RetryableStitchingException();
        }
    }

    @Recover
    public Optional<CaseData> recover(RetryableStitchingException e,
                                      final BundleRequest payload,
                                      final String endpoint,
                                      String authorisation) {
        log.info("Tried to call {} too many times without success", endpoint);
        return Optional.empty();
    }

    //need to handle maximum retries exception as well, hence method overload
    @Recover
    public Optional<CaseData> recover(RuntimeException e,
                                      final BundleRequest payload,
                                      final String endpoint,
                                      String authorisation) {
        log.info("Tried to call {} too many times without success", endpoint);
        return Optional.empty();
    }

    @SuppressWarnings("unchecked")
    private void logRelevantInfoQuietly(RestClientResponseException e) {
        try {
            log.error("  ^  HTTP Error: {}", e.getRawStatusCode());
            Map<String, Object> response = objectMapper.readValue(e.getResponseBodyAsString(), Map.class);

            List<String> errors = (List<String>) response.get("errors");
            errors.forEach(message -> log.error("  |  {}", message.substring(0, Math.min(message.length(), 250))));

        } catch (Throwable t) {
            log.warn("  ^  The details of the error could not be logged due to an exception while trying to log them."
                         + " Maybe the output could not be parsed as JSON? Maybe the service was unreachable?");
        }
    }

}

