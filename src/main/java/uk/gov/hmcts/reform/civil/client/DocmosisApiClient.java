package uk.gov.hmcts.reform.civil.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.FeignClientProperties;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import uk.gov.hmcts.reform.civil.model.docmosis.DocmosisRequest;

@FeignClient(name = "docmosis", url = "${docmosis.tornado.url}", configuration =
    FeignClientProperties.FeignClientConfiguration.class)
public interface DocmosisApiClient {

    @PostMapping(value = "/rs/render", consumes = "application/json")
    byte[] createDocument(@RequestBody DocmosisRequest docmosisRequest);
}
