package uk.gov.hmcts.reform.civil.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.FeignClientProperties;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.ResponseBody;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.civil.model.noc.DecisionRequest;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi.SERVICE_AUTHORIZATION;

@FeignClient(
    name = "case-assignment-api",
    url = "${aca.api.baseurl}",
    configuration = FeignClientProperties.FeignClientConfiguration.class
)
public interface CaseAssignmentApi {

    @PostMapping(
        value = "/noc/apply-decision",
        consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    AboutToStartOrSubmitCallbackResponse applyDecision(
        @RequestHeader(AUTHORIZATION) String authorisation,
        @RequestHeader(SERVICE_AUTHORIZATION) String serviceAuthorization,
        @RequestBody DecisionRequest decisionRequest);

    @PostMapping(
        value = "/noc/check-noc-approval",
        consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    AboutToStartOrSubmitCallbackResponse checkNocApproval(
        @RequestHeader(AUTHORIZATION) String authorisation,
        @RequestHeader(SERVICE_AUTHORIZATION) String serviceAuthorization,
        @RequestBody CallbackRequest callbackRequest);
}
