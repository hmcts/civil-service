package uk.gov.hmcts.reform.civil.config.feign;

import feign.Client;
import feign.ExceptionPropagationPolicy;
import feign.Retryer;
import feign.codec.ErrorDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.cloud.openfeign.FeignClientFactory;
import org.springframework.cloud.openfeign.FeignClientSpecification;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@ConditionalOnProperty(prefix = "feign.startup.logging", name = "enabled", havingValue = "true")
public class FeignStartupLogger {

    private static final Logger log = LoggerFactory.getLogger(FeignStartupLogger.class);

    private final FeignClientFactory feignClientFactory;

    public FeignStartupLogger(FeignClientFactory feignClientFactory) {
        this.feignClientFactory = feignClientFactory;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void logFeignClients() {
        feignClientFactory.getConfigurations().entrySet().stream()
            .filter(entry -> !entry.getKey().startsWith("default."))
            .sorted(Map.Entry.comparingByKey())
            .forEach(entry -> logClient(entry.getKey(), entry.getValue()));
    }

    private void logClient(String contextId, FeignClientSpecification specification) {
        log.info(
            "Feign client [{}] interface={} configClasses={} errorDecoder={} localErrorDecoders={} "
                + "retryer={} localRetryers={} client={} localClients={} exceptionPropagationPolicy={}",
            contextId,
            specification.getClassName(),
            describeConfiguration(specification),
            typeName(feignClientFactory.getInstance(contextId, ErrorDecoder.class)),
            describeBeans(feignClientFactory.getInstancesWithoutAncestors(contextId, ErrorDecoder.class)),
            typeName(feignClientFactory.getInstance(contextId, Retryer.class)),
            describeBeans(feignClientFactory.getInstancesWithoutAncestors(contextId, Retryer.class)),
            typeName(feignClientFactory.getInstance(contextId, Client.class)),
            describeBeans(feignClientFactory.getInstancesWithoutAncestors(contextId, Client.class)),
            feignClientFactory.getInstance(contextId, ExceptionPropagationPolicy.class)
        );
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
        return bean == null ? "<none>" : ClassUtils.getUserClass(bean).getName();
    }
}
