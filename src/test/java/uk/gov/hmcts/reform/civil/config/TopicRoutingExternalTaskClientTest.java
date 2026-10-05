package uk.gov.hmcts.reform.civil.config;

import org.camunda.bpm.client.ExternalTaskClient;
import org.camunda.bpm.client.topic.TopicSubscriptionBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TopicRoutingExternalTaskClientTest {

    private static final String SCHEDULER_TOPIC = "BUNDLE_CREATION_CHECK";
    private static final String CASE_TOPIC = "START_BUSINESS_PROCESS";
    private static final String UNKNOWN_TOPIC = "SOME_TOPIC_ADDED_LATER";

    private ExternalTaskClient caseDrivenClient;
    private ExternalTaskClient schedulerClient;
    private TopicRoutingExternalTaskClient router;

    @BeforeEach
    void setUp() {
        caseDrivenClient = mock(ExternalTaskClient.class);
        schedulerClient = mock(ExternalTaskClient.class);
        router = new TopicRoutingExternalTaskClient(
            caseDrivenClient, schedulerClient, Set.of(SCHEDULER_TOPIC, "POLLING_EVENT_EMITTER"));
    }

    @Nested
    class Routing {

        @Test
        void shouldRouteASchedulerTopicToTheSchedulerClient() {
            assertThat(router.clientFor(SCHEDULER_TOPIC)).isSameAs(schedulerClient);
        }

        @Test
        void shouldRouteACaseDrivenTopicToTheCaseDrivenClient() {
            assertThat(router.clientFor(CASE_TOPIC)).isSameAs(caseDrivenClient);
        }

        /**
         * The safe default. A topic nobody has classified behaves exactly as it does today, on the
         * case driven client, so adding a subscription cannot silently move an existing one.
         */
        @Test
        void shouldRouteAnUnknownTopicToTheCaseDrivenClient() {
            assertThat(router.clientFor(UNKNOWN_TOPIC)).isSameAs(caseDrivenClient);
        }

        @Test
        void shouldSubscribeOnTheSchedulerClientOnlyForASchedulerTopic() {
            TopicSubscriptionBuilder builder = mock(TopicSubscriptionBuilder.class);
            when(schedulerClient.subscribe(SCHEDULER_TOPIC)).thenReturn(builder);

            assertThat(router.subscribe(SCHEDULER_TOPIC)).isSameAs(builder);

            verify(schedulerClient).subscribe(SCHEDULER_TOPIC);
            verify(caseDrivenClient, never()).subscribe(SCHEDULER_TOPIC);
        }

        @Test
        void shouldSubscribeOnTheCaseDrivenClientOnlyForACaseDrivenTopic() {
            TopicSubscriptionBuilder builder = mock(TopicSubscriptionBuilder.class);
            when(caseDrivenClient.subscribe(CASE_TOPIC)).thenReturn(builder);

            assertThat(router.subscribe(CASE_TOPIC)).isSameAs(builder);

            verify(caseDrivenClient).subscribe(CASE_TOPIC);
            verify(schedulerClient, never()).subscribe(CASE_TOPIC);
        }
    }

    @Nested
    class Lifecycle {

        @Test
        void shouldStartBothClients() {
            router.start();

            verify(caseDrivenClient).start();
            verify(schedulerClient).start();
        }

        @Test
        void shouldStopBothClients() {
            router.stop();

            verify(caseDrivenClient).stop();
            verify(schedulerClient).stop();
        }

        @Test
        void shouldBeActiveOnlyWhenBothClientsAre() {
            when(caseDrivenClient.isActive()).thenReturn(true);
            when(schedulerClient.isActive()).thenReturn(true);

            assertThat(router.isActive()).isTrue();
        }

        /**
         * A half stopped pair must not report healthy: the scheduler client being down means
         * batch work has silently stopped even though case work continues.
         */
        @Test
        void shouldNotBeActiveWhenOnlyTheSchedulerClientIsDown() {
            when(caseDrivenClient.isActive()).thenReturn(true);
            when(schedulerClient.isActive()).thenReturn(false);

            assertThat(router.isActive()).isFalse();
        }

        @Test
        void shouldNotBeActiveWhenOnlyTheCaseDrivenClientIsDown() {
            when(caseDrivenClient.isActive()).thenReturn(false);

            assertThat(router.isActive()).isFalse();
        }
    }

    @Test
    void shouldNotBeAffectedByLaterMutationOfTheTopicSet() {
        Set<String> mutable = new java.util.HashSet<>(Set.of(SCHEDULER_TOPIC));
        TopicRoutingExternalTaskClient client =
            new TopicRoutingExternalTaskClient(caseDrivenClient, schedulerClient, mutable);

        mutable.add(CASE_TOPIC);

        assertThat(client.clientFor(CASE_TOPIC)).isSameAs(caseDrivenClient);
    }
}
