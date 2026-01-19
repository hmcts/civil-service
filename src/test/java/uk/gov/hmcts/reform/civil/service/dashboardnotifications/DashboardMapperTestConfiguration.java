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
        @Filter(type = FilterType.REGEX,
            pattern = "uk\\.gov\\.hmcts\\.reform\\.civil\\.service\\.dashboardnotifications\\.caseproceedsoffline.*"),
        @Filter(type = FilterType.REGEX,
            pattern = "uk\\.gov\\.hmcts\\.reform\\.civil\\.service\\.dashboardnotifications\\.dismisscase.*"),
        @Filter(type = FilterType.REGEX,
            pattern = "uk\\.gov\\.hmcts\\.reform\\.civil\\.service\\.dashboardnotifications\\.amendrestitchbundle.*"),
        @Filter(type = FilterType.REGEX,
            pattern = "uk\\.gov\\.hmcts\\.reform\\.civil\\.service\\.dashboardnotifications\\.trialarrangements.*"),
        @Filter(type = FilterType.REGEX,
            pattern = "uk\\.gov\\.hmcts\\.reform\\.civil\\.service\\.dashboardnotifications\\.bundlecreation.*"),
        @Filter(type = FilterType.REGEX,
            pattern = "uk\\.gov\\.hmcts\\.reform\\.civil\\.service\\.dashboardnotifications\\.claimsettled.*"),
        @Filter(type = FilterType.REGEX,
            pattern = "uk\\.gov\\.hmcts\\.reform\\.civil\\.service\\.dashboardnotifications\\.evidenceuploaded.*"),
        @Filter(type = FilterType.REGEX,
            pattern = "uk\\.gov\\.hmcts\\.reform\\.civil\\.service\\.dashboardnotifications\\.courtofficerorder.*"),
        @Filter(type = FilterType.REGEX,
            pattern = "uk\\.gov\\.hmcts\\.reform\\.civil\\.service\\.dashboardnotifications\\.trailreadycheck.*"),
        @Filter(type = FilterType.REGEX,
            pattern = "uk\\.gov\\.hmcts\\.reform\\.civil\\.service\\.dashboardnotifications\\.createlipclaim.*"),
        @Filter(type = FilterType.REGEX,
            pattern = "uk\\.gov\\.hmcts\\.reform\\.civil\\.service\\.dashboardnotifications\\.decisionoutcome.*"),
        @Filter(type = FilterType.REGEX,
            pattern = "uk\\.gov\\.hmcts\\.reform\\.civil\\.service\\.dashboardnotifications\\.defendantresponse.*"),
        @Filter(type = FilterType.REGEX,
            pattern = "uk\\.gov\\.hmcts\\.reform\\.civil\\.service\\.dashboardnotifications\\.claimissue.*"),
        @Filter(type = FilterType.REGEX,
            pattern = "uk\\.gov\\.hmcts\\.reform\\.civil\\.service\\.dashboardnotifications\\.raisequery.*"),
        @Filter(type = FilterType.REGEX,
            pattern = "uk\\.gov\\.hmcts\\.reform\\.civil\\.service\\.dashboardnotifications\\.respondtoquery.*"),
        @Filter(type = FilterType.REGEX,
            pattern = "uk\\.gov\\.hmcts\\.reform\\.civil\\.service\\.dashboardnotifications\\.citizenhearingfeepayment.*"),
        @Filter(type = FilterType.REGEX,
            pattern = "uk\\.gov\\.hmcts\\.reform\\.civil\\.service\\.dashboardnotifications\\.defendantsignsettlementagreement.*"),
        @Filter(type = FilterType.REGEX,
            pattern = "uk\\.gov\\.hmcts\\.reform\\.civil\\.service\\.dashboardnotifications\\.discontinueclaimclaimant.*"),
        @Filter(type = FilterType.REGEX,
            pattern = "uk\\.gov\\.hmcts\\.reform\\.civil\\.service\\.dashboardnotifications\\.notifylipclaimanthwfoutcome.*")
        @Filter(type = FilterType.REGEX,
            pattern = "uk\\.gov\\.hmcts\\.reform\\.civil\\.service\\.dashboardnotifications\\.defendantresponsedeadlinecheck.*")
    }
)
public class DashboardMapperTestConfiguration {

    @Bean
    public SdoCaseClassificationService sdoCaseClassificationService() {
        return new SdoCaseClassificationService();
    }
}
