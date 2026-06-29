package uk.gov.hmcts.reform.civil.service.dashboardnotifications;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import uk.gov.hmcts.reform.civil.service.sdo.SdoCaseClassificationService;

@Configuration
@ComponentScan(
    basePackages = "uk.gov.hmcts.reform.civil.service.dashboardnotifications",
    excludeFilters = {
        @Filter(type = FilterType.ASSIGNABLE_TYPE, classes = DashboardNotificationTransactionalService.class),
        @Filter(type = FilterType.REGEX,
            pattern = "uk\\.gov\\.hmcts\\.reform\\.civil\\.service\\.dashboardnotifications\\..*\\..*")
    }
)
public class DashboardMapperTestConfiguration {

    @Bean
    public SdoCaseClassificationService sdoCaseClassificationService() {
        return new SdoCaseClassificationService();
    }
}
