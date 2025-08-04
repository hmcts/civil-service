package uk.gov.hmcts.reform.civil.service.bundle;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientResponseException;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.client.EvidenceManagementApiClient;
import uk.gov.hmcts.reform.civil.config.SystemUpdateUserConfiguration;
import uk.gov.hmcts.reform.civil.event.BundleCreationTriggerEvent;
import uk.gov.hmcts.reform.civil.exceptions.StitchingFailedException;
import uk.gov.hmcts.reform.civil.helpers.bundle.BundleRequestMapper;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.bundle.BundleCreateRequest;
import uk.gov.hmcts.reform.civil.model.bundle.BundleCreateResponse;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.civil.service.UserService;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class BundleCreationService {

    private final CoreCaseDataService coreCaseDataService;
    private final BundleRequestMapper bundleRequestMapper;
    private final AuthTokenGenerator serviceAuthTokenGenerator;
    private final EvidenceManagementApiClient evidenceManagementApiClient;
    private final UserService userService;
    private final SystemUpdateUserConfiguration userConfig;
    private final ObjectMapper objectMapper;

    @Value("${bundle.config}")
    private String bundleConfig;
    private final CaseDetailsConverter caseDetailsConverter;

    public BundleCreateResponse createBundle(BundleCreationTriggerEvent event) {
        CaseDetails caseDetails = coreCaseDataService.getCase(event.getCaseId());
        return createNewBundleRequest(getAccessToken(), serviceAuthTokenGenerator.generate(),
                            bundleRequestMapper.mapCaseDataToBundleCreateRequest(caseDetailsConverter.toCaseData(caseDetails),
                                bundleConfig,
                                caseDetails.getJurisdiction(), caseDetails.getCaseTypeId()));
    }

    public BundleCreateResponse createBundle(Long caseId) {
        CaseDetails caseDetails = coreCaseDataService.getCase(caseId);
        return createNewBundleRequest(getAccessToken(), serviceAuthTokenGenerator.generate(),
                                      bundleRequestMapper.mapCaseDataToBundleCreateRequest(caseDetailsConverter.toCaseData(caseDetails),
                                                                                           bundleConfig,
                                                                                           caseDetails.getJurisdiction(), caseDetails.getCaseTypeId()));
    }

    private String getAccessToken() {
        return userService.getAccessToken(
            userConfig.getUserName(),
            userConfig.getPassword()
        );
    }

    private BundleCreateResponse createNewBundleRequest(String authorization,
                                                        String serviceAuthorization,
                                                        BundleCreateRequest bundleCreateRequest) {
        try {
            ResponseEntity<BundleCreateResponse> response = evidenceManagementApiClient
                .createNewBundle(authorization, serviceAuthorization, bundleCreateRequest);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return response.getBody();
            } else {
                log.warn("Call to createNewBundle returned non-success HTTP status: {}", response.getStatusCodeValue());
                throw new StitchingFailedException("Non-success response from stitching service: HTTP " + response.getStatusCodeValue());
            }
        } catch (RestClientResponseException e) {
            log.error("createNewBundle call failed with HTTP status {} - {}", e.getRawStatusCode(), e.getStatusText());
            logRelevantErrorDetails(e);
            throw new StitchingFailedException("Stitching service call failed");
        } catch (Exception e) {
            log.error("Unexpected exception during createNewBundle call", e);
            throw new StitchingFailedException("Unexpected error during bundle creation");
        }
    }

    @SuppressWarnings("unchecked")
    private void logRelevantErrorDetails(RestClientResponseException e) {
        try {
            Map<String, Object> response = objectMapper.readValue(e.getResponseBodyAsString(), Map.class);
            List<String> errors = (List<String>) response.get("errors");

            if (errors != null) {
                errors.forEach(error -> log.error("Bundle service error: {}",
                                                  error.substring(0, Math.min(error.length(), 250))));
            } else {
                log.error("Bundle service error response: {}", response);
            }

        } catch (Exception ex) {
            log.warn("Failed to parse bundle service error response: {}", ex.getMessage());
        }
    }

}
