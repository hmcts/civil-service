package uk.gov.hmcts.reform.civil;

import org.camunda.bpm.extension.rest.EnableCamundaRestClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableCamundaRestClient
@EnableFeignClients(basePackages = {
    "uk.gov.hmcts.reform.idam.client",
    "uk.gov.hmcts.reform.civil",
    "uk.gov.hmcts.reform.prd",
    "uk.gov.hmcts.reform.ccd.document.am",
    "uk.gov.hmcts.reform.ras",
    "uk.gov.hmcts.reform.cmc"
})
@SuppressWarnings("HideUtilityClassConstructor") // Spring needs a constructor, its not a utility class
public class Application {

    public static void main(final String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
