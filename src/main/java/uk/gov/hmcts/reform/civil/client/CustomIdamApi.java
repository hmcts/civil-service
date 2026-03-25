package uk.gov.hmcts.reform.civil.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import uk.gov.hmcts.reform.idam.client.CoreFeignConfiguration;
import uk.gov.hmcts.reform.idam.client.IdamApi;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

@FeignClient(
    name = "custom-idam-api",
    url = "${idam.api.url}",
    configuration = CoreFeignConfiguration.class
)
public interface CustomIdamApi extends IdamApi {

    @GetMapping("/users")
    UserDetails getUserByEmail(
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation,
        @PathVariable("email") String email
    );
}
