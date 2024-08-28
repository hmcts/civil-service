package uk.gov.hmcts.reform.civil.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.FeignClientProperties;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import uk.gov.hmcts.reform.civil.model.judgmentonline.cjes.JudgmentDetailsCJES;

@FeignClient(name = "rtl", url = "${rtl.api.url}", configuration =
    FeignClientProperties.FeignClientConfiguration.class)
public interface CjesApiClient {

    @PostMapping(path = "/judgment}", consumes = "application/json")
        ResponseEntity<Void> sendJudgmentDetailsCJES(
        @RequestBody JudgmentDetailsCJES judgmentDetailsCJES
    );
}
