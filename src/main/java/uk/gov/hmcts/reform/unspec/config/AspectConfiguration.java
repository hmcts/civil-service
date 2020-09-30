package uk.gov.hmcts.reform.unspec.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import uk.gov.hmcts.reform.unspec.stereotypes.ExternalTaskLogger;

@Configuration
@EnableAspectJAutoProxy
public class AspectConfiguration {

    @Bean
    public ExternalTaskLogger externalTaskLogger() {
        return new ExternalTaskLogger();
    }
}
