package uk.gov.hmcts.reform.civil.service.dashboardnotifications;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;

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
            pattern = "uk\\.gov\\.hmcts\\.reform\\.civil\\.service\\.dashboardnotifications\\.mediationunsuccessful.*"),
        @Filter(type = FilterType.REGEX,
            pattern = "uk\\.gov\\.hmcts\\.reform\\.civil\\.service\\.dashboardnotifications\\.courtofficerorder.*"),
        @Filter(type = FilterType.REGEX,
            pattern = "uk\\.gov\\.hmcts\\.reform\\.civil\\.service\\.dashboardnotifications\\.trailreadycheck.*"),
        @Filter(type = FilterType.REGEX,
            pattern = "uk\\.gov\\.hmcts\\.reform\\.civil\\.service\\.dashboardnotifications\\.createlipclaim.*"),
        @Filter(type = FilterType.REGEX,
            pattern = "uk\\.gov\\.hmcts\\.reform\\.civil\\.service\\.dashboardnotifications\\.decisionoutcome.*"),
        @Filter(type = FilterType.REGEX,
            pattern = "uk\\.gov\\.hmcts\\.reform\\.civil\\.service\\.dashboardnotifications\\.defendantresponse.*")
    }
)
public class DashboardMapperTestConfiguration {
}
