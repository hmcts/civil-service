package uk.gov.hmcts.reform.civil.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import uk.gov.hmcts.reform.civil.interceptors.CcdPersistenceInterceptor;

@Configuration
@RequiredArgsConstructor
public class CcdPersistenceConfiguration implements WebMvcConfigurer {

    private final CcdPersistenceInterceptor ccdPersistenceInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(ccdPersistenceInterceptor).addPathPatterns(CcdPersistenceInterceptor.PATH_PATTERN);
    }
}
