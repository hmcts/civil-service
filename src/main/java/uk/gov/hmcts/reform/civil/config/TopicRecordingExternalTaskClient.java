package uk.gov.hmcts.reform.civil.config;

import org.camunda.bpm.client.ExternalTaskClient;
import org.camunda.bpm.client.task.ExternalTaskHandler;
import org.camunda.bpm.client.topic.TopicSubscriptionBuilder;
import uk.gov.hmcts.reform.civil.handler.tasks.GuardedTopicRegistry;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * Wraps an {@link ExternalTaskClient} so that every {@code subscribe(topic).handler(handler)} call
 * is also recorded in a {@link GuardedTopicRegistry}. Behaviour is otherwise unchanged: every call
 * is forwarded to the real client.
 */
final class TopicRecordingExternalTaskClient {

    private TopicRecordingExternalTaskClient() {
    }

    static ExternalTaskClient wrap(ExternalTaskClient delegate, GuardedTopicRegistry registry) {
        return (ExternalTaskClient) Proxy.newProxyInstance(
            ExternalTaskClient.class.getClassLoader(),
            new Class<?>[]{ExternalTaskClient.class},
            (proxy, method, args) -> {
                Object result = invoke(method, delegate, args);
                if ("subscribe".equals(method.getName())
                    && args != null && args.length == 1
                    && args[0] instanceof String topic
                    && result instanceof TopicSubscriptionBuilder builder) {
                    return wrapBuilder(builder, topic, registry);
                }
                return result == delegate ? proxy : result;
            }
        );
    }

    private static TopicSubscriptionBuilder wrapBuilder(TopicSubscriptionBuilder delegate,
                                                        String topic,
                                                        GuardedTopicRegistry registry) {
        return (TopicSubscriptionBuilder) Proxy.newProxyInstance(
            TopicSubscriptionBuilder.class.getClassLoader(),
            new Class<?>[]{TopicSubscriptionBuilder.class},
            (proxy, method, args) -> {
                if ("handler".equals(method.getName())
                    && args != null && args.length == 1
                    && args[0] instanceof ExternalTaskHandler handler) {
                    registry.register(topic, handler);
                }
                Object result = invoke(method, delegate, args);
                return result == delegate ? proxy : result;
            }
        );
    }

    private static Object invoke(Method method, Object target, Object[] args) throws Throwable {
        try {
            return method.invoke(target, args);
        } catch (InvocationTargetException e) {
            throw e.getCause();
        }
    }
}
