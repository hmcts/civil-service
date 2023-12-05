package uk.gov.hmcts.reform.civil.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
public class CMCPinVerifyConfiguration {

    private final String redirectUrl;
    private final String clientId;

    public CMCPinVerifyConfiguration(
        @Value("${ocmc.client.id}") String redirectUrl,
        @Value("${ocmc.client.url}") String clientId
    ) {
        this.redirectUrl = redirectUrl;
        this.clientId = clientId;
    }
}
