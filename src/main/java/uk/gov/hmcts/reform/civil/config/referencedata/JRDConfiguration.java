package uk.gov.hmcts.reform.civil.config.referencedata;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
public class JRDConfiguration {

    private final String url;
    private final String endpoint;

    public JRDConfiguration(
            @Value("${genApp.jrd.url}") String url,
            @Value("${genApp.jrd.endpoint}") String endpoint) {
        this.url = url;
        this.endpoint = endpoint;
    }
}
