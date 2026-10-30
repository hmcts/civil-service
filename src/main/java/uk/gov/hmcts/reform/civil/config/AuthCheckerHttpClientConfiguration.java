package uk.gov.hmcts.reform.civil.config;

import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * auth-checker-lib 3.2.x (its Spring Boot 4 line) is compiled without {@code -parameters}, so its
 * token parser beans can no longer pick between {@code userTokenParserHttpClient} and
 * {@code serviceTokenParserHttpClient} by parameter name. Both are {@code HttpClients.createDefault()},
 * so marking one primary is behaviour-preserving. Remove once the library is rebuilt with
 * {@code -parameters} or qualifies its injection points.
 */
@Configuration
@SuppressWarnings("HideUtilityClassConstructor") // Spring needs a constructor, its not a utility class
public class AuthCheckerHttpClientConfiguration {

    private static final String USER_TOKEN_PARSER_HTTP_CLIENT = "userTokenParserHttpClient";

    @Bean
    public static BeanFactoryPostProcessor authCheckerHttpClientPrimary() {
        return beanFactory -> {
            if (beanFactory.containsBeanDefinition(USER_TOKEN_PARSER_HTTP_CLIENT)) {
                beanFactory.getBeanDefinition(USER_TOKEN_PARSER_HTTP_CLIENT).setPrimary(true);
            }
        };
    }
}
