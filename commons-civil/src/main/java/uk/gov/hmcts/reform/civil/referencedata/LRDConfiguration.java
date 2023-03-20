package uk.gov.hmcts.reform.civil.referencedata;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
public class LRDConfiguration {

    private final String url;
    private final String endpoint;

    public LRDConfiguration(
            @Value("${genApp.lrd.url}") String url,
            @Value("${genApp.lrd.endpoint}") String endpoint) {
        this.url = url;
        this.endpoint = endpoint;
    }
}
