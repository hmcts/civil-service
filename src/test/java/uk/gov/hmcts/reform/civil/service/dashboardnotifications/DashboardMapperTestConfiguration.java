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
            pattern = "uk\\.gov\\.hmcts\\.reform\\.civil\\.service\\.dashboardnotifications\\.defendantnoc.*"),
        @Filter(type = FilterType.REGEX,
            pattern = "uk\\.gov\\.hmcts\\.reform\\.civil\\.service\\.dashboardnotifications\\.ccjrequested.*"),
        @Filter(type = FilterType.REGEX,
            pattern = "uk\\.gov\\.hmcts\\.reform\\.civil\\.service\\.dashboardnotifications\\.evidenceuploaded.*"),
        @Filter(type = FilterType.REGEX,
            pattern = "uk\\.gov\\.hmcts\\.reform\\.civil\\.service\\.dashboardnotifications\\.claimantresponse.*"),
        @Filter(type = FilterType.REGEX,
            pattern = "uk\\.gov\\.hmcts\\.reform\\.civil\\.service\\.dashboardnotifications\\.claimissue.*"),
        @Filter(type = FilterType.REGEX,
            pattern = "uk\\.gov\\.hmcts\\.reform\\.civil\\.service\\.dashboardnotifications\\.decisionreconsideration.*"),
        @Filter(type = FilterType.REGEX,
            pattern = "uk\\.gov\\.hmcts\\.reform\\.civil\\.service\\.dashboardnotifications\\.mediationunsuccessful.*"),
        @Filter(type = FilterType.REGEX,
            pattern = "uk\\.gov\\.hmcts\\.reform\\.civil\\.service\\.dashboardnotifications\\.mediationsuccessful.*"),
        @Filter(type = FilterType.REGEX,
            pattern = "uk\\.gov\\.hmcts\\.reform\\.civil\\.service\\.dashboardnotifications\\.courtofficerorder.*"),
        @Filter(type = FilterType.REGEX,
            pattern = "uk\\.gov\\.hmcts\\.reform\\.civil\\.service\\.dashboardnotifications\\.staycase.*"),
        @Filter(type = FilterType.REGEX,
            pattern = "uk\\.gov\\.hmcts\\.reform\\.civil\\.service\\.dashboardnotifications\\.trialreadynotification.*"),
        @Filter(type = FilterType.REGEX,
            pattern = "uk\\.gov\\.hmcts\\.reform\\.civil\\.service\\.dashboardnotifications\\.gentrialreadydocapplicant.*"),
        @Filter(type = FilterType.REGEX,
            pattern = "uk\\.gov\\.hmcts\\.reform\\.civil\\.service\\.dashboardnotifications\\.trialreadycheck.*"),
        @Filter(type = FilterType.REGEX,
            pattern = "uk\\.gov\\.hmcts\\.reform\\.civil\\.service\\.dashboardnotifications\\.createlipclaim.*"),
        @Filter(type = FilterType.REGEX,
            pattern = "uk\\.gov\\.hmcts\\.reform\\.civil\\.service\\.dashboardnotifications\\.decisionoutcome.*"),
        @Filter(type = FilterType.REGEX,
            pattern = "uk\\.gov\\.hmcts\\.reform\\.civil\\.service\\.dashboardnotifications\\.defendantresponse.*"),
        @Filter(type = FilterType.REGEX,
            pattern = "uk\\.gov\\.hmcts\\.reform\\.civil\\.service\\.dashboardnotifications\\.claimissue.*"),
        @Filter(type = FilterType.REGEX,
            pattern = "uk\\.gov\\.hmcts\\.reform\\.civil\\.service\\.dashboardnotifications\\.helpwithhearingfees.*"),
        @Filter(type = FilterType.REGEX,
            pattern = "uk\\.gov\\.hmcts\\.reform\\.civil\\.service\\.dashboardnotifications\\.raisequery.*"),
        @Filter(type = FilterType.REGEX,
            pattern = "uk\\.gov\\.hmcts\\.reform\\.civil\\.service\\.dashboardnotifications\\.settleclaimmarkedpaidinfull.*"),
        @Filter(type = FilterType.REGEX,
            pattern = "uk\\.gov\\.hmcts\\.reform\\.civil\\.service\\.dashboardnotifications\\.hearingfeeunpaid.*"),
        @Filter(type = FilterType.REGEX,
            pattern = "uk\\.gov\\.hmcts\\.reform\\.civil\\.service\\.dashboardnotifications\\.setasidejudgement.*"),
        @Filter(type = FilterType.REGEX,
            pattern = "uk\\.gov\\.hmcts\\.reform\\.civil\\.service\\.dashboardnotifications\\.hearingscheduled.*"),
        @Filter(type = FilterType.REGEX,
            pattern = "uk\\.gov\\.hmcts\\.reform\\.civil\\.service\\.dashboardnotifications\\.informagreedextensiondatespec.*"),
        @Filter(type = FilterType.REGEX,
            pattern = "uk\\.gov\\.hmcts\\.reform\\.civil\\.service\\.dashboardnotifications\\.respondtoquery.*"),
        @Filter(type = FilterType.REGEX,
            pattern = "uk\\.gov\\.hmcts\\.reform\\.civil\\.service\\.dashboardnotifications\\.citizenhearingfeepayment.*"),
        @Filter(type = FilterType.REGEX,
            pattern = "uk\\.gov\\.hmcts\\.reform\\.civil\\.service\\.dashboardnotifications\\.defendantsignsettlementagreement.*"),
        @Filter(type = FilterType.REGEX,
            pattern = "uk\\.gov\\.hmcts\\.reform\\.civil\\.service\\.dashboardnotifications\\.discontinueclaimclaimant.*"),
        @Filter(type = FilterType.REGEX,
            pattern = "uk\\.gov\\.hmcts\\.reform\\.civil\\.service\\.dashboardnotifications\\.notifylipclaimanthwfoutcome.*"),
        @Filter(type = FilterType.REGEX,
            pattern = "uk\\.gov\\.hmcts\\.reform\\.civil\\.service\\.dashboardnotifications\\.defendantresponsedeadlinecheck.*"),
        @Filter(type = FilterType.REGEX,
            pattern = "uk\\.gov\\.hmcts\\.reform\\.civil\\.service\\.dashboardnotifications\\.hearingscheduled.*"),
        @Filter(type = FilterType.REGEX,
            pattern = "uk\\.gov\\.hmcts\\.reform\\.civil\\.service\\.dashboardnotifications\\.djnondivergent.*"),
        @Filter(type = FilterType.REGEX,
            pattern = "uk\\.gov\\.hmcts\\.reform\\.civil\\.service\\.dashboardnotifications\\.trialreadyrespondent1.*"),
        @Filter(type = FilterType.REGEX,
            pattern = "uk\\.gov\\.hmcts\\.reform\\.civil\\.service\\.dashboardnotifications\\.trialarrangementsnotifyotherparty.*"),
        @Filter(type = FilterType.REGEX,
            pattern = "uk\\.gov\\.hmcts\\.reform\\.civil\\.service\\.dashboardnotifications\\.createsdo.*"),
        @Filter(type = FilterType.REGEX,
            pattern = "uk\\.gov\\.hmcts\\.reform\\.civil\\.service\\.dashboardnotifications\\.uploadhearingdocuments.*"),
        @Filter(type = FilterType.REGEX,
            pattern = "uk\\.gov\\.hmcts\\.reform\\.civil\\.service\\.dashboardnotifications\\.finalorder.*"),
        @Filter(type = FilterType.REGEX,
            pattern = "uk\\.gov\\.hmcts\\.reform\\.civil\\.service\\.dashboardnotifications\\.staylifted.*"),
        @Filter(type = FilterType.REGEX,
            pattern = "uk\\.gov\\.hmcts\\.reform\\.civil\\.service\\.dashboardnotifications\\.helper.*")
    }
)
public class DashboardMapperTestConfiguration {

    @Bean
    public SdoCaseClassificationService sdoCaseClassificationService() {
        return new SdoCaseClassificationService();
    }
}
