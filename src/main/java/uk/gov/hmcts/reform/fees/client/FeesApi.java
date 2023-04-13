package uk.gov.hmcts.reform.fees.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import uk.gov.hmcts.reform.fees.client.health.InternalHealth;
import uk.gov.hmcts.reform.fees.client.model.Fee2Dto;
import uk.gov.hmcts.reform.fees.client.model.FeeLookupResponseDto;

import java.math.BigDecimal;

@FeignClient(name = "fees-api", url = "${fees.api.url}")

public interface FeesApi {

    String baseUrl = "/fees-register/fees";
    
    @GetMapping("/health")
    InternalHealth health();

    @GetMapping(baseUrl + "/lookup"
        + "?service={service}"
        + "&jurisdiction1={jurisdiction1}"
        + "&jurisdiction2={jurisdiction2}"
        + "&channel={channel}"
        + "&event={eventType}"
        + "&keyword={keyword}"
        + "&amount_or_volume={amount}"
    )
    FeeLookupResponseDto lookupFee(
        @PathVariable("service") String service,
        @PathVariable("jurisdiction1") String jurisdiction1,
        @PathVariable("jurisdiction2") String jurisdiction2,
        @PathVariable("channel") String channel,
        @PathVariable("eventType") String eventType,
        @PathVariable("keyword") String keyword,
        @PathVariable("amount") BigDecimal amount
    );

    @GetMapping(baseUrl + "/lookup"
        + "?service={service}"
        + "&jurisdiction1={jurisdiction1}"
        + "&jurisdiction2={jurisdiction2}"
        + "&channel={channel}"
        + "&event={eventType}"
        + "&amount_or_volume={amount}"
    )
    FeeLookupResponseDto lookupFeeWithoutKeyword(
        @PathVariable("service") String service,
        @PathVariable("jurisdiction1") String jurisdiction1,
        @PathVariable("jurisdiction2") String jurisdiction2,
        @PathVariable("channel") String channel,
        @PathVariable("eventType") String eventType,
        @PathVariable("amount") BigDecimal amount
    );

    @GetMapping(baseUrl
        + "?service={service}"
        + "&jurisdiction1={jurisdiction1}"
        + "&jurisdiction2={jurisdiction2}"
        + "&channel={channel}"
        + "&event={eventType}"
        + "&feeVersionStatus=approved"
    )
    Fee2Dto[] findRangeGroup(
        @PathVariable("service") String service,
        @PathVariable("jurisdiction1") String jurisdiction1,
        @PathVariable("jurisdiction2") String jurisdiction2,
        @PathVariable("channel") String channel,
        @PathVariable("eventType") String eventType
    );
}
