package uk.gov.hmcts.reform.civil.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
public class PaymentsConfiguration {

    private final String siteId;
    private final String service;
    private final String specSiteId;
    private final String specService;


    public PaymentsConfiguration(@Value("${payments.api.site_id}") String siteId,
                                 @Value("${payments.api.service}") String service,
                                 @Value("${payments.api.spec_site_id}") String specSiteId,
                                 @Value("${payments.api.spec_service}") String specService) {
        this.siteId = siteId;
        this.service = service;
        this.specSiteId = specSiteId;
        this.specService = specService;
    }
}
