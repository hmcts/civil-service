package uk.gov.hmcts.reform.unspec.aspect;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.unspec.launchdarkly.FeatureToggleService;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class FeatureToggleAspect {

    private final FeatureToggleService featureToggleService;

    @Around("execution(* *(*)) && @annotation(featureToggle)")
    public void checkFeatureEnabled(ProceedingJoinPoint joinPoint, FeatureToggle featureToggle) throws Throwable {

        if (featureToggle.value() && featureToggleService.isFeatureEnabled(featureToggle.feature())) {
            joinPoint.proceed();
        } else if (!featureToggle.value() && !featureToggleService.isFeatureEnabled(featureToggle.feature())) {
            joinPoint.proceed();
        } else {
            log.warn(
                "Feature %s is not enabled for method %s",
                featureToggle.feature(),
                joinPoint.getSignature().getName()
            );
        }
    }
}
