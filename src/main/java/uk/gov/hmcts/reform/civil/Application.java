package uk.gov.hmcts.reform.civil;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.scheduling.annotation.EnableScheduling;

// The excluded classes come from client libraries with no Spring Boot 4 release and reference
// Boot 3 types that no longer exist; see Boot3ClientLibraryConfiguration. Excluded here rather than
// via spring.autoconfigure.exclude so profile-specific exclude lists cannot drop them.
@SpringBootApplication(excludeName = {
    "uk.gov.hmcts.reform.sendletter.SendLetterAutoConfiguration",
    "uk.gov.hmcts.reform.ccd.client.CoreCaseDataClientAutoConfiguration"
})
@EnableScheduling
@ComponentScan(
    basePackages = {"uk.gov.hmcts.reform"},
    excludeFilters = @ComponentScan.Filter(
        type = FilterType.REGEX,
        pattern = {
            "uk\\.gov\\.hmcts\\.reform\\.payments\\.client\\.config\\.PaymentClientAutoConfiguration",
            "uk\\.gov\\.hmcts\\.reform\\.payments\\.client\\.health\\..*",
            "uk\\.gov\\.hmcts\\.reform\\.fees\\.client\\.config\\.FeesClientAutoConfiguration",
            "uk\\.gov\\.hmcts\\.reform\\.fees\\.client\\.health\\..*",
            "uk\\.gov\\.hmcts\\.reform\\.sendletter\\.SendLetterAutoConfiguration",
            "uk\\.gov\\.hmcts\\.reform\\.sendletter\\.healthcheck\\..*",
            "uk\\.gov\\.hmcts\\.reform\\.ccd\\.client\\.CoreCaseDataClientAutoConfiguration",
            "uk\\.gov\\.hmcts\\.reform\\.ccd\\.client\\.healthcheck\\..*"
        }
    )
)
@EnableFeignClients(basePackages = {
    "uk.gov.hmcts.reform.idam.client",
    "uk.gov.hmcts.reform.civil",
    "uk.gov.hmcts.reform.civil.prd",
    "uk.gov.hmcts.reform.civil.ras",
    "uk.gov.hmcts.reform.civil.crd",
    "uk.gov.hmcts.reform.ccd.document.am",
    "uk.gov.hmcts.reform.ccd.client",
    "uk.gov.hmcts.reform.cmc",
    "uk.gov.hmcts.reform.hmc"
})
@SuppressWarnings("HideUtilityClassConstructor") // Spring needs a constructor, its not a utility class
public class Application {

    public static void main(final String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
