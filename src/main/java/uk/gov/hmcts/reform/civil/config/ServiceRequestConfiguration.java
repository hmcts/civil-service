package uk.gov.hmcts.reform.civil.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
public class ServiceRequestConfiguration {

    private final String siteId;
    private final String service;

    public ServiceRequestConfiguration(@Value("${serviceRequest.api.site_id}") String siteId,
                                       @Value("${serviceRequest.api.service}") String service) {
        this.siteId = siteId;
        this.service = service;
    }

}
