package uk.gov.hmcts.reform.civil.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
public class PinInPostConfiguration {

    private final String moneyClaimUrl;
    private final String cuiFrontEndUrl;

    public PinInPostConfiguration(
        @Value("${pin-in-post.money-claims.url}") String moneyClaimUrl,
        @Value("${pin-in-post.cui-front-end.url}") String cuiFrontEndUrl
    ) {
        this.moneyClaimUrl = moneyClaimUrl;
        this.cuiFrontEndUrl = cuiFrontEndUrl;
    }

}
