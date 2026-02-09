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
            pattern = "uk\\.gov\\.hmcts\\.reform\\.civil\\.service\\.dashboardnotifications\\.decisionreconsideration.*"),
        @Filter(type = FilterType.REGEX,
            pattern = "uk\\.gov\\.hmcts\\.reform\\.civil\\.service\\.dashboardnotifications\\.mediationunsuccessful.*"),
        @Filter(type = FilterType.REGEX,
            pattern = "uk\\.gov\\.hmcts\\.reform\\.civil\\.service\\.dashboardnotifications\\.mediationsuccessful.*"),
        @Filter(type = FilterType.REGEX,
            pattern = "uk\\.gov\\.hmcts\\.reform\\.civil\\.service\\.dashboardnotifications\\.courtofficerorder.*"),
        @Filter(type = FilterType.REGEX,
            pattern = "uk\\.gov\\.hmcts\\.reform\\.civil\\.service\\.dashboardnotifications\\.trialreadynotification.*"),
        @Filter(type = FilterType.REGEX,
            pattern = "uk\\.gov\\.hmcts\\.reform\\.civil\\.service\\.dashboardnotifications\\.gentrialreadydocapplicant.*"),
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
<<<<<<< HEAD
            pattern = "uk\\.gov\\.hmcts\\.reform\\.civil\\.service\\.dashboardnotifications\\.djnondivergent.*")
=======
            pattern = "uk\\.gov\\.hmcts\\.reform\\.civil\\.service\\.dashboardnotifications\\.hearingfeeunpaid.*")
>>>>>>> f58adb267936dcedf973c133605a0b1951f1d38c
    }
)
public class DashboardMapperTestConfiguration {

    @Bean
    public SdoCaseClassificationService sdoCaseClassificationService() {
        return new SdoCaseClassificationService();
    }
}
