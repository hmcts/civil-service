package uk.gov.hmcts.reform.civil.config;

import feign.Request;
import feign.Response;
import feign.codec.ErrorDecoder;
import org.junit.jupiter.api.Test;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import uk.gov.hmcts.reform.civil.BaseIntegrationTest;
import uk.gov.hmcts.reform.civil.service.FeignErrorTelemetryService;

import java.nio.charset.StandardCharsets;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

/**
 * Pins the {@link ErrorDecoder} wiring in the <em>real</em> application context.
 *
 * <p>The sibling tests deliberately do not do this and cannot be made to: {@code
 * ErrorDecoderTelemetryAspectWiringTest} and {@code ErrorDecoderTelemetryAspectIntegrationTest}
 * each declare their own {@code ErrorDecoder} inside a sliced configuration, so both stay green
 * whether the application ends up with none, one or two. This one loads {@code Application.class}
 * so the count is whatever the deployed service would actually resolve.
 *
 * <p>Both failure modes matter and only one of them is loud:
 * <ul>
 *   <li><b>None.</b> Feign builds {@code ErrorDecoder.Default} internally, Spring AOP never sees
 *       it, {@code ErrorDecoderTelemetryAspect} never fires and
 *       {@code httpclient.feign.error.classified} is never emitted. Nothing fails; the events
 *       simply never arrive, which reads as "no errors occurred".</li>
 *   <li><b>Two.</b> Holunda's {@code FeignErrorDecoderConfiguration} used to contribute one, held
 *       off only by {@code camunda.rest.client.error-decoding.enabled} being false against a
 *       {@code matchIfMissing = true} condition. DTSCCI-6513 removed that dependency and the
 *       property with it, so that particular source is gone. This assertion stays because the
 *       failure mode does not depend on Holunda: any starter contributing a second decoder would
 *       silently take over the retry classification, and nothing else would report it.</li>
 * </ul>
 *
 * @see HttpClientFeignConfiguration#feignErrorDecoder()
 */
@SuppressWarnings({"java:S6813", "java:S5960"})
class FeignErrorDecoderWiringIntegrationTest extends BaseIntegrationTest {

    private static final String METHOD_KEY = "CamundaRuntimeApi#startProcessInstance";
    private static final String DECODER_BEAN_NAME = "feignErrorDecoder";

    // MockitoBean rather than the deprecated MockBean: this is new code and MockBean is removed in
    // Spring Boot 4, which DTSCCI-6393 is working towards.
    @MockitoBean
    private FeignErrorTelemetryService telemetryService;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private ErrorDecoder errorDecoder;

    @Test
    void shouldResolveExactlyOneErrorDecoderBeanInTheRealContext() {
        assertThat(applicationContext.getBeanNamesForType(ErrorDecoder.class))
            .as("a second ErrorDecoder means Holunda's is back and the retry classification has "
                    + "reverted; none means Feign builds its own and the telemetry aspect stops firing")
            .containsExactly(DECODER_BEAN_NAME);
    }

    @Test
    void shouldResolveFeignsOwnDefaultDecoderAsThatBean() {
        assertThat(AopProxyUtils.ultimateTargetClass(errorDecoder))
            .as("the decoder behind any proxy should be the one HttpClientFeignConfiguration declares")
            .isEqualTo(ErrorDecoder.Default.class);
    }

    @Test
    void shouldAdviseTheDecoderSoTheTelemetryAspectCanFire() {
        assertThat(AopUtils.isAopProxy(errorDecoder))
            .as("an unproxied decoder is never advised, so httpclient.feign.error.classified is never emitted")
            .isTrue();
    }

    @Test
    void shouldEmitTelemetryWhenTheContextManagedDecoderDecodesAnError() {
        Response response = responseWithStatus(500);

        Exception decoded = errorDecoder.decode(METHOD_KEY, response);

        assertThat(decoded).isInstanceOf(feign.FeignException.class);
        verify(telemetryService).trackErrorClassification(eq(METHOD_KEY), eq(response), any());
    }

    private Response responseWithStatus(int status) {
        Request request = Request.create(
            Request.HttpMethod.POST,
            "http://camunda/engine-rest/process-definition/key/test/start",
            Collections.emptyMap(),
            null,
            StandardCharsets.UTF_8,
            null
        );
        return Response.builder()
            .status(status)
            .request(request)
            .headers(Collections.emptyMap())
            .build();
    }
}
