package uk.gov.hmcts.reform.civil.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.FeignClientProperties;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import uk.gov.hmcts.reform.civil.model.payments.PaymentDto;

@FeignClient(name = "payment-api", url = "${payments.api.url}", configuration =
    FeignClientProperties.FeignClientConfiguration.class)
public interface CardPaymentClient {

    @GetMapping({"/card-payments/{paymentReference}/status"})
    PaymentDto retrieveCardPaymentStatus(@PathVariable("paymentReference") String paymentReference,
                                         @RequestHeader("Authorization") String authorization,
                                         @RequestHeader("ServiceAuthorization") String serviceAuthorization);
}
