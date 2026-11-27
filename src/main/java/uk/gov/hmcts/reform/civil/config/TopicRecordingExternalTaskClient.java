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
                if (isEquals(method)) {
                    return proxy == args[0];
                }
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
                if (isEquals(method)) {
                    return proxy == args[0];
                }
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

    /**
     * A wrapper is equal only to itself. Forwarding {@code equals} would compare the delegate with
     * the proxy, so a wrapper would not even equal itself, and matching it against the raw delegate
     * would not be symmetric. Identity keeps {@code hashCode}, which is forwarded, consistent.
     */
    private static boolean isEquals(Method method) {
        return "equals".equals(method.getName())
            && method.getParameterCount() == 1
            && method.getParameterTypes()[0] == Object.class;
    }

    private static Object invoke(Method method, Object target, Object[] args) throws Throwable {
        try {
            return method.invoke(target, args);
        } catch (InvocationTargetException e) {
            throw e.getCause();
        }
    }
}
