package uk.gov.hmcts.reform.civil.handler.tasks;

import org.camunda.bpm.client.task.ExternalTaskHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Guards the one obligation that running several case driven clients in one JVM creates.
 *
 * <p>Each topic is subscribed on every case driven client, and they share the one Spring singleton
 * handler instance, so a handler now runs on more than one thread at a time. Mutable state on a
 * handler would therefore be shared across concurrent tasks. Nothing in the Camunda client checks
 * this, and the failure would be silent and data dependent rather than a startup error.
 *
 * <p>An audit found the handlers clean, but an audit only holds on the day it is run. This asserts
 * the rule instead: a handler may hold static fields and final fields freely, and a non final field
 * only when {@code @Value} injects it once at startup. Anything else fails here with the field
 * named, rather than in production under load.
 *
 * <p>Scoped to Spring managed handlers, since only those are shared as singletons across the
 * clients. It is also necessarily a shallow check: it does not follow a final field into a
 * collaborator that is itself stateful, nor catch a final field holding a mutable collection that
 * the handler writes to during a task. It catches the common and easily made mistake.
 *
 * @see uk.gov.hmcts.reform.civil.config.FanOutTopicSubscriptionBuilder
 */
class ExternalTaskHandlerThreadSafetyTest {

    private static final String BASE_PACKAGE = "uk.gov.hmcts.reform.civil";

    @Test
    void noExternalTaskHandlerShouldHoldMutableInstanceState() throws ClassNotFoundException {
        ClassPathScanningCandidateComponentProvider scanner =
            new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AssignableTypeFilter(ExternalTaskHandler.class));

        List<String> offenders = new ArrayList<>();
        int handlers = 0;

        for (var candidate : scanner.findCandidateComponents(BASE_PACKAGE)) {
            Class<?> type = Class.forName(candidate.getBeanClassName());
            // Only Spring managed handlers are shared as singletons across the clients, so only
            // they carry the risk. This also keeps test doubles on the test classpath out.
            if (!AnnotatedElementUtils.hasAnnotation(type, Component.class)) {
                continue;
            }
            handlers++;
            for (Field field : type.getDeclaredFields()) {
                if (Modifier.isStatic(field.getModifiers())
                    || Modifier.isFinal(field.getModifiers())
                    || field.isAnnotationPresent(Value.class)) {
                    continue;
                }
                offenders.add(type.getSimpleName() + "." + field.getName()
                                  + " (" + field.getType().getSimpleName() + ")");
            }
        }

        assertThat(handlers)
            .as("the scan must actually find the handlers, or this test proves nothing")
            .isGreaterThan(30);

        assertThat(offenders)
            .as("a handler is shared across case driven clients, so mutable instance state is "
                    + "shared across concurrent tasks. Make the field final, inject it with @Value, "
                    + "or move the state into a per task object as IncidentRetryEventHandler does.")
            .isEmpty();
    }
}
