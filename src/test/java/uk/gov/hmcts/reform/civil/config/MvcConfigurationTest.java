package uk.gov.hmcts.reform.civil.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.config.annotation.InterceptorRegistration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import uk.gov.hmcts.reform.civil.interceptors.RequestInterceptor;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MvcConfigurationTest {

    MvcConfiguration mvcConfiguration = new MvcConfiguration();

    @Mock
    InterceptorRegistry interceptorRegistry;

    @Mock
    InterceptorRegistration interceptorRegistration;

    @Test
    void shouldAddInterceptorForCcdCallback() {
        when(interceptorRegistry.addInterceptor(any(RequestInterceptor.class))).thenReturn(interceptorRegistration);
        mvcConfiguration.addInterceptors(interceptorRegistry);
        verify(interceptorRegistry).addInterceptor(any(RequestInterceptor.class));
        verify(interceptorRegistration).addPathPatterns("/cases/callbacks/**");
    }
}
