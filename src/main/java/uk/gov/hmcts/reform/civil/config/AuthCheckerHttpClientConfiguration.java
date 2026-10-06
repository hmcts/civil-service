package uk.gov.hmcts.reform.civil.config;

import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * auth-checker-lib 3.2.x registers two {@link CloseableHttpClient} beans ({@code userTokenParserHttpClient} and
 * {@code serviceTokenParserHttpClient}) and injects them by type without a qualifier. Earlier releases were compiled
 * with {@code -parameters}, so Spring matched them by parameter name; 3.2.x is not, so startup fails with an
 * ambiguous dependency. Both library clients are {@link HttpClients#createDefault()}, so a single primary default
 * client keeps the same behaviour.
 *
 * <p>Remove once auth-checker-lib qualifies its own injection points.
 */
@Configuration
public class AuthCheckerHttpClientConfiguration {

    @Bean
    @Primary
    public CloseableHttpClient authCheckerHttpClient() {
        return HttpClients.createDefault();
    }
}
