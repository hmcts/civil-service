package uk.gov.hmcts.reform.civil.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.FeignClientProperties;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import uk.gov.hmcts.reform.civil.model.bundle.BundleCreateRequest;
import uk.gov.hmcts.reform.civil.model.bundle.BundleCreateResponse;

@FeignClient(name = "bundle", url = "${bundle.api.url}", configuration =
    FeignClientProperties.FeignClientConfiguration.class)
public interface BundleApiClient {

    @PostMapping(value = "/api/new-bundle", consumes = "application/json")
    BundleCreateResponse createBundleServiceRequest(
        @RequestHeader("Authorization") String authorization,
        @RequestHeader("ServiceAuthorization") String serviceAuthorization,
        @RequestBody BundleCreateRequest bundleCreateRequest
        );
}
