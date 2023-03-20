package uk.gov.hmcts.reform.civil;

import org.camunda.bpm.extension.rest.EnableCamundaRestClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

@SpringBootApplication(
    scanBasePackages = {"uk.gov.hmcts.ccd.sdk", "uk.gov.hmcts.reform.civil.model"}
)
@EnableCamundaRestClient
@EnableFeignClients(basePackages = {
    "uk.gov.hmcts.reform.idam.client",
    "uk.gov.hmcts.reform.civil",
    "uk.gov.hmcts.reform.prd",
    "uk.gov.hmcts.reform.ccd.document.am",
    "uk.gov.hmcts.reform.ras",
    "uk.gov.hmcts.reform.cmc",
    "uk.gov.hmcts.reform.crd"
})
//@ComponentScan({"uk.gov.hmcts.reform.authorisation"})
@SuppressWarnings("HideUtilityClassConstructor") // Spring needs a constructor, its not a utility class
public class Application {

    public static void main(final String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    public RestTemplate getRestTemplate() {
        return new RestTemplate();
    }

    /*@Bean
    public AuthTokenGenerator generator() {
        return new AuthTokenGenerator() {
            @Override
            public String generate() {
                return null;
            }
        };
    }*/

    @Bean
    public AuthTokenGenerator generator() {
        return () -> null;
    }

}
