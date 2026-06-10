package uk.gov.hmcts.reform.civil.aspect;

import feign.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.service.FeignErrorTelemetryService;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class ErrorDecoderTelemetryAspect {

    private final FeignErrorTelemetryService telemetryService;

    @Around("execution(* feign.codec.ErrorDecoder.decode(String, feign.Response)) && args(methodKey, response)")
    public Object trackErrorDecoderTelemetry(ProceedingJoinPoint joinPoint, String methodKey, Response response) throws Throwable {
        if (telemetryService == null) {
            return joinPoint.proceed();
        }

        try {
            Object result = joinPoint.proceed();
            if (result instanceof Exception decodedException) {
                telemetryService.trackErrorClassification(methodKey, response, decodedException);
            }
            return result;
        } catch (Throwable throwable) {
            telemetryService.trackErrorClassification(methodKey, response, throwable);
            throw throwable;
        }
    }
}
