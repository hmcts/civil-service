package uk.gov.hmcts.reform.prd.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import uk.gov.hmcts.reform.prd.model.HearingChannelLov;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi.SERVICE_AUTHORIZATION;

@FeignClient(name = "rd-commondata-api", url = "${rd_common.api.url}")
public interface CommonReferenceDataApi {

    @GetMapping("/refdata/commondata/lov/categories/HearingChannel")
    HearingChannelLov findHearingChannels(
        @RequestHeader(AUTHORIZATION) String authorisation,
        @RequestHeader(SERVICE_AUTHORIZATION) String serviceAuthorization,
        @RequestParam("serviceId") String serviceId);
}
