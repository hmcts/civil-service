package uk.gov.hmcts.reform.civil.consumer;

import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.context.PropertyPlaceholderAutoConfiguration;
import org.springframework.boot.autoconfigure.http.HttpMessageConvertersAutoConfiguration;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.FeignAutoConfiguration;
import org.springframework.context.annotation.Import;
import uk.gov.hmcts.reform.civil.config.AppInsightsConfiguration;
import uk.gov.hmcts.reform.civil.config.DocumentGeneratorApplicationConfiguration;
import uk.gov.hmcts.reform.civil.config.HttpClientFeignConfiguration;
import uk.gov.hmcts.reform.civil.config.HttpClientRestConfiguration;
import uk.gov.hmcts.reform.civil.config.JacksonConfiguration;
import uk.gov.hmcts.reform.civil.config.ServiceTokenGeneratorConfiguration;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.OAuth2Configuration;

/**
 * HTTP client infrastructure only; consumer tests import the service wrappers they exercise.
 * Deliberately avoids application scanning, security, scheduling and workflow startup.
 * CaseDetailsConverter retains the application mapper's NON_EMPTY serialization setting.
 */
@TestConfiguration(proxyBeanMethods = false)
@EnableConfigurationProperties
@ImportAutoConfiguration({FeignAutoConfiguration.class, JacksonAutoConfiguration.class,
    HttpMessageConvertersAutoConfiguration.class, PropertyPlaceholderAutoConfiguration.class})
@EnableFeignClients(basePackages = {
    "uk.gov.hmcts.reform.idam.client", "uk.gov.hmcts.reform.civil",
    "uk.gov.hmcts.reform.ccd.document.am", "uk.gov.hmcts.reform.ccd.client",
    "uk.gov.hmcts.reform.cmc", "uk.gov.hmcts.reform.hmc",
    "uk.gov.hmcts.reform.authorisation", "uk.gov.hmcts.reform.document",
    "uk.gov.hmcts.reform.payments.client", "uk.gov.hmcts.reform.sendletter"
})
@Import({HttpClientFeignConfiguration.class, HttpClientRestConfiguration.class, JacksonConfiguration.class,
    AppInsightsConfiguration.class, DocumentGeneratorApplicationConfiguration.class,
    ServiceTokenGeneratorConfiguration.class, IdamClient.class, OAuth2Configuration.class, CaseDetailsConverter.class})
public class ConsumerContractTestConfiguration {
}
