package uk.gov.hmcts.reform.civil.aspect;

import feign.FeignException;
import feign.Request;
import feign.Response;
import feign.codec.ErrorDecoder;
import org.junit.jupiter.api.Test;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import uk.gov.hmcts.reform.civil.service.FeignErrorTelemetryService;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Proves the wiring that makes {@link ErrorDecoderTelemetryAspect} live: Feign's default
 * decoder registered as a Spring bean (see {@code HttpClientFeignConfiguration}) is proxied
 * by Spring AOP and every decode reaches {@link FeignErrorTelemetryService}. Without the
 * bean Feign instantiates the decoder itself and the aspect never fires.
 */
@SpringJUnitConfig(ErrorDecoderTelemetryAspectWiringTest.Config.class)
class ErrorDecoderTelemetryAspectWiringTest {

    @Configuration
    @EnableAspectJAutoProxy
    static class Config {

        @Bean
        FeignErrorTelemetryService feignErrorTelemetryService() {
            return mock(FeignErrorTelemetryService.class);
        }

        @Bean
        ErrorDecoderTelemetryAspect errorDecoderTelemetryAspect(FeignErrorTelemetryService telemetryService) {
            return new ErrorDecoderTelemetryAspect(telemetryService);
        }

        @Bean
        ErrorDecoder feignErrorDecoder() {
            return new ErrorDecoder.Default();
        }
    }

    @Autowired
    private ErrorDecoder errorDecoder;

    @Autowired
    private FeignErrorTelemetryService telemetryService;

    @Test
    void defaultDecoderBeanIsAdvisedAndReportsEveryDecode() {
        assertThat(AopUtils.isAopProxy(errorDecoder)).isTrue();

        Response response = Response.builder()
            .status(502)
            .reason("Bad Gateway")
            .request(Request.create(Request.HttpMethod.POST, "http://ccd/events", Map.of(), null, StandardCharsets.UTF_8, null))
            .headers(Map.of())
            .build();

        Exception decoded = errorDecoder.decode("CoreCaseDataApi#submitEventForCaseWorker", response);

        assertThat(decoded).isInstanceOf(FeignException.BadGateway.class);
        verify(telemetryService).trackErrorClassification(eq("CoreCaseDataApi#submitEventForCaseWorker"), eq(response), any(FeignException.BadGateway.class));
    }
}
