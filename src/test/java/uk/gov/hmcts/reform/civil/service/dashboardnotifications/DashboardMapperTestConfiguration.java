package uk.gov.hmcts.reform.civil.service.dashboardnotifications;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.reform.civil.service.sdo.SdoCaseClassificationService;

@Configuration
@ComponentScan({
    "uk.gov.hmcts.reform.civil.service.dashboardnotifications"
})
public class DashboardMapperTestConfiguration {

    @Bean
    public SdoCaseClassificationService sdoCaseClassificationService() {
        return new SdoCaseClassificationService();
    }
}
