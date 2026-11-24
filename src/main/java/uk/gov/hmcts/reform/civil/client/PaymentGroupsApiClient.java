package uk.gov.hmcts.reform.civil.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import uk.gov.hmcts.reform.civil.model.servicerequest.CasePaymentGroupsResponse;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

/**
 * The payment service's list of service requests (payment groups) on a case. The payments client library
 * has no lookup, only create. Answers 204 with no body when the case has none, which decodes to null.
 */
@FeignClient(name = "payment-groups-api", url = "${payments.api.url}")
public interface PaymentGroupsApiClient {

    @GetMapping("/cases/{ccdCaseNumber}/paymentgroups")
    CasePaymentGroupsResponse getPaymentGroups(
        @RequestHeader(AUTHORIZATION) String authorisation,
        @RequestHeader("ServiceAuthorization") String serviceAuthorisation,
        @PathVariable("ccdCaseNumber") String ccdCaseNumber
    );
}
