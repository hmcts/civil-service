package uk.gov.hmcts.reform.civil.config.feign;

import feign.Client;
import feign.ExceptionPropagationPolicy;
import feign.Retryer;
import feign.codec.ErrorDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.cloud.openfeign.FeignClientFactory;
import org.springframework.cloud.openfeign.FeignClientSpecification;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@ConditionalOnProperty(prefix = "feign.startup.logging", name = "enabled", havingValue = "true")
public class FeignStartupLogger {

    private static final Logger log = LoggerFactory.getLogger(FeignStartupLogger.class);

    private final FeignClientFactory feignClientFactory;
    private final ApplicationContext applicationContext;

    public FeignStartupLogger(FeignClientFactory feignClientFactory, ApplicationContext applicationContext) {
        this.feignClientFactory = feignClientFactory;
        this.applicationContext = applicationContext;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void logFeignClients() {
        feignClientFactory.getConfigurations().entrySet().stream()
            .filter(entry -> !entry.getKey().startsWith("default."))
            .sorted(Map.Entry.comparingByKey())
            .forEach(entry -> logClient(entry.getKey(), entry.getValue()));
    }

    private void logClient(String contextId, FeignClientSpecification specification) {
        BeanResolution errorDecoder = resolveBean(contextId, ErrorDecoder.class);
        BeanResolution retryer = resolveBean(contextId, Retryer.class);
        BeanResolution client = resolveBean(contextId, Client.class);

        log.info(
            "## Feign client [{}] interface={} configClasses={} "
                + "errorDecoder={} errorDecoderSource={} localErrorDecoders={} parentErrorDecoders={} "
                + "retryer={} retryerSource={} localRetryers={} parentRetryers={} "
                + "client={} clientSource={} localClients={} parentClients={} "
                + "exceptionPropagationPolicy={}",
            contextId,
            specification.getClassName(),
            describeConfiguration(specification),
            errorDecoder.typeName(),
            errorDecoder.source(),
            describeBeans(errorDecoder.localBeans()),
            describeBeans(errorDecoder.parentBeans()),
            retryer.typeName(),
            retryer.source(),
            describeBeans(retryer.localBeans()),
            describeBeans(retryer.parentBeans()),
            client.typeName(),
            client.source(),
            describeBeans(client.localBeans()),
            describeBeans(client.parentBeans()),
            feignClientFactory.getInstance(contextId, ExceptionPropagationPolicy.class)
        );
    }

    private <T> BeanResolution resolveBean(String contextId, Class<T> type) {
        T resolvedBean = feignClientFactory.getInstance(contextId, type);
        Map<String, T> localBeans = new LinkedHashMap<>(feignClientFactory.getInstancesWithoutAncestors(contextId, type));
        Map<String, T> parentBeans = new LinkedHashMap<>(applicationContext.getBeansOfType(type));

        return new BeanResolution(
            typeName(resolvedBean),
            detectSource(resolvedBean, localBeans, parentBeans),
            localBeans,
            parentBeans
        );
    }

    private <T> String detectSource(T resolvedBean, Map<String, T> localBeans, Map<String, T> parentBeans) {
        if (resolvedBean == null) {
            return "<none>";
        }
        Optional<String> localBeanName = findBeanName(resolvedBean, localBeans);
        if (localBeanName.isPresent()) {
            return "local:" + localBeanName.get();
        }
        Optional<String> parentBeanName = findBeanName(resolvedBean, parentBeans);
        if (parentBeanName.isPresent()) {
            return "parent:" + parentBeanName.get();
        }
        return "unknown";
    }

    private <T> Optional<String> findBeanName(T resolvedBean, Map<String, T> candidates) {
        return candidates.entrySet().stream()
            .filter(entry -> sameBean(resolvedBean, entry.getValue()))
            .map(Map.Entry::getKey)
            .findFirst();
    }

    private boolean sameBean(Object left, Object right) {
        if (left == right) {
            return true;
        }
        Object leftTarget = singletonTarget(left);
        Object rightTarget = singletonTarget(right);
        return leftTarget != null && rightTarget != null && Objects.equals(leftTarget, rightTarget);
    }

    private Object singletonTarget(Object bean) {
        try {
            return AopProxyUtils.getSingletonTarget(bean);
        } catch (Exception ignored) {
            return null;
        }
    }

    private String describeConfiguration(FeignClientSpecification specification) {
        Class<?>[] configuration = specification.getConfiguration();
        if (configuration == null || configuration.length == 0) {
            return "[]";
        }
        return Arrays.stream(configuration)
            .map(Class::getName)
            .collect(Collectors.joining(", ", "[", "]"));
    }

    private String describeBeans(Map<String, ?> beans) {
        if (beans == null || beans.isEmpty()) {
            return "{}";
        }
        return beans.entrySet().stream()
            .map(entry -> entry.getKey() + "=" + typeName(entry.getValue()))
            .collect(Collectors.joining(", ", "{", "}"));
    }

    private String typeName(Object bean) {
        if (bean == null) {
            return "<none>";
        }
        Class<?> targetClass = AopProxyUtils.ultimateTargetClass(bean);
        if (targetClass == null || Object.class.equals(targetClass)) {
            targetClass = ClassUtils.getUserClass(bean);
        }
        return targetClass.getName();
    }

    private record BeanResolution(String typeName, String source, Map<String, ?> localBeans, Map<String, ?> parentBeans) {
    }
}
