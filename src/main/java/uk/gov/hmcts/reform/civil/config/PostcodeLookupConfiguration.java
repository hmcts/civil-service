package uk.gov.hmcts.reform.civil.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
public class PostcodeLookupConfiguration {

    private final String url;
    private final String accessKey;

    public PostcodeLookupConfiguration(@Value("${os-postcode-lookup.url}") String url,
                                       @Value("${os-postcode-lookup.key}") String accessKey) {
        this.url = url;
        this.accessKey = accessKey;
    }
}
