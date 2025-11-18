package uk.gov.hmcts.reform.civil;

import org.camunda.community.rest.EnableCamundaRestClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableCamundaRestClient
@EnableJpaRepositories(basePackages = {"uk.gov.hmcts.reform.dashboard"})
@EntityScan("uk.gov.hmcts.reform.dashboard")
@ComponentScan(basePackages = {"uk.gov.hmcts.reform"})
@EnableFeignClients(basePackages = {
    "uk.gov.hmcts.reform.idam.client",
    "uk.gov.hmcts.reform.civil",
    "uk.gov.hmcts.reform.civil.prd",
    "uk.gov.hmcts.reform.ccd.document.am",
    "uk.gov.hmcts.reform.civil.ras",
    "uk.gov.hmcts.reform.cmc",
    "uk.gov.hmcts.reform.civil.crd",
    "uk.gov.hmcts.reform.hmc"
})
@SuppressWarnings("HideUtilityClassConstructor") // Spring needs a constructor, its not a utility class
public class Application {

    public static void main(final String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
