package uk.gov.hmcts.reform.civil.service.stitching;

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
import uk.gov.hmcts.reform.idam.client.IdamClient;

import static java.util.Objects.requireNonNull;

@Slf4j
@Service
public class BundleRequestExecutor {

    private static final String SERVICE_AUTHORIZATION = "ServiceAuthorization";

    private final RestTemplate restTemplate;
    private final AuthTokenGenerator serviceAuthTokenGenerator;
    private final IdamClient idamClient;
    private final CaseDetailsConverter caseDetailsConverter;

    public BundleRequestExecutor(RestTemplate restTemplate,
                                 AuthTokenGenerator serviceAuthTokenGenerator,
                                 IdamClient idamClient,
                                 CaseDetailsConverter caseDetailsConverter) {
        this.restTemplate = restTemplate;
        this.serviceAuthTokenGenerator = serviceAuthTokenGenerator;
        this.idamClient = idamClient;
        this.caseDetailsConverter = caseDetailsConverter;
    }

    public CaseData post(
        final BundleRequest payload,
        final String endpoint,
        String authorisation
    ) {
        CaseData caseData = null;
        requireNonNull(payload, "payload must not be null");
        requireNonNull(endpoint, "endpoint must not be null");

        final String serviceAuthorizationToken = serviceAuthTokenGenerator.generate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        headers.set(
            HttpHeaders.AUTHORIZATION,
            authorisation
        );
        headers.set(SERVICE_AUTHORIZATION, serviceAuthorizationToken);

        HttpEntity<BundleRequest> requestEntity = new HttpEntity<>(payload, headers);

        ResponseEntity<CaseDetails> response1 = null;

        try {
            response1 =
                restTemplate
                    .exchange(
                        endpoint,
                        HttpMethod.POST,
                        requestEntity,
                        CaseDetails.class
                    );
            if (response1.getStatusCode().equals(HttpStatus.OK)) {
                caseData = caseDetailsConverter.toCaseData(response1.getBody());
            }

        } catch (RestClientResponseException e) {
            e.printStackTrace();
        }
        return caseData;
    }

}

