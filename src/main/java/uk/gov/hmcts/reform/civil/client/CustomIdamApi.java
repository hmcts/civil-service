package uk.gov.hmcts.reform.civil.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import uk.gov.hmcts.reform.idam.client.CoreFeignConfiguration;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

@FeignClient(
    name = "custom-idam-api",
    url = "${idam.api.url}",
    configuration = CoreFeignConfiguration.class
)
public interface CustomIdamApi {

    @GetMapping("/users")
    UserDetails getUserByEmail(
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation,
        @RequestParam("email") String email
    );
}
