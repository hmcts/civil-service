package uk.gov.hmcts.reform.civil;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.scheduling.annotation.EnableScheduling;
import uk.gov.hmcts.reform.payments.client.config.PaymentClientAutoConfiguration;
import uk.gov.hmcts.reform.sendletter.SendLetterAutoConfiguration;

// These HMCTS client libraries register health indicators built against the Spring Boot 3 actuator API, which no
// longer exists in Spring Boot 4. Their Feign clients are enabled below instead.
@SpringBootApplication(exclude = SendLetterAutoConfiguration.class)
@EnableScheduling
@ComponentScan(
    basePackages = {"uk.gov.hmcts.reform"},
    excludeFilters = @ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE,
        classes = {PaymentClientAutoConfiguration.class, SendLetterAutoConfiguration.class}
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
    "uk.gov.hmcts.reform.hmc",
    "uk.gov.hmcts.reform.payments.client",
    "uk.gov.hmcts.reform.sendletter"
})
@SuppressWarnings("HideUtilityClassConstructor") // Spring needs a constructor, its not a utility class
public class Application {

    public static void main(final String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
