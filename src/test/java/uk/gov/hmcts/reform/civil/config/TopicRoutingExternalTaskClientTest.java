package uk.gov.hmcts.reform.civil.config;

import org.camunda.bpm.client.ExternalTaskClient;
import org.camunda.bpm.client.topic.TopicSubscriptionBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
            assertThat(router.clientsFor(SCHEDULER_TOPIC)).containsExactly(schedulerClient);
        }

        @Test
        void shouldRouteACaseDrivenTopicToTheCaseDrivenClient() {
            assertThat(router.clientsFor(CASE_TOPIC)).containsExactly(caseDrivenClient);
        }

        /**
         * The safe default. A topic nobody has classified behaves exactly as it does today, on the
         * case driven clients, so adding a subscription cannot silently move an existing one.
         */
        @Test
        void shouldRouteAnUnknownTopicToTheCaseDrivenClient() {
            assertThat(router.clientsFor(UNKNOWN_TOPIC)).containsExactly(caseDrivenClient);
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

    /**
     * The reason the class exists in this shape. The case driven side was measured at 95% thread
     * occupancy, so it is given several clients and every one of them must subscribe to every case
     * driven topic. Giving each client a share of the topics would leave whichever client owned
     * {@code processCaseEvent} as the bottleneck, since that topic alone was 335 of 637 measured
     * task executions.
     */
    @Nested
    class SeveralCaseDrivenClients {

        private ExternalTaskClient secondCaseDrivenClient;
        private TopicRoutingExternalTaskClient multiRouter;

        @BeforeEach
        void setUp() {
            secondCaseDrivenClient = mock(ExternalTaskClient.class);
            multiRouter = new TopicRoutingExternalTaskClient(
                List.of(caseDrivenClient, secondCaseDrivenClient), schedulerClient, Set.of(SCHEDULER_TOPIC));
        }

        @Test
        void shouldRouteACaseDrivenTopicToEveryCaseDrivenClient() {
            assertThat(multiRouter.clientsFor(CASE_TOPIC))
                .containsExactly(caseDrivenClient, secondCaseDrivenClient);
        }

        @Test
        void shouldStillRouteASchedulerTopicToTheSingleSchedulerClient() {
            assertThat(multiRouter.clientsFor(SCHEDULER_TOPIC)).containsExactly(schedulerClient);
        }

        @Test
        void shouldSubscribeACaseDrivenTopicOnEveryCaseDrivenClient() {
            when(caseDrivenClient.subscribe(CASE_TOPIC)).thenReturn(mock(TopicSubscriptionBuilder.class));
            when(secondCaseDrivenClient.subscribe(CASE_TOPIC)).thenReturn(mock(TopicSubscriptionBuilder.class));

            assertThat(multiRouter.subscribe(CASE_TOPIC)).isInstanceOf(FanOutTopicSubscriptionBuilder.class);

            verify(caseDrivenClient).subscribe(CASE_TOPIC);
            verify(secondCaseDrivenClient).subscribe(CASE_TOPIC);
            verify(schedulerClient, never()).subscribe(CASE_TOPIC);
        }

        /**
         * A scheduler topic has one client, so it must not be wrapped. The wrapper is only correct
         * where several subscriptions have to be configured together.
         */
        @Test
        void shouldNotWrapASchedulerSubscriptionWhenOnlyOneClientServesIt() {
            TopicSubscriptionBuilder builder = mock(TopicSubscriptionBuilder.class);
            when(schedulerClient.subscribe(SCHEDULER_TOPIC)).thenReturn(builder);

            assertThat(multiRouter.subscribe(SCHEDULER_TOPIC)).isSameAs(builder);
        }

        @Test
        void shouldStartEveryClient() {
            multiRouter.start();

            verify(caseDrivenClient).start();
            verify(secondCaseDrivenClient).start();
            verify(schedulerClient).start();
        }

        @Test
        void shouldStopEveryClient() {
            multiRouter.stop();

            verify(caseDrivenClient).stop();
            verify(secondCaseDrivenClient).stop();
            verify(schedulerClient).stop();
        }

        /**
         * One dead case driven client means a share of case work has silently stopped being picked
         * up, so the router must not report healthy.
         */
        @Test
        void shouldNotBeActiveWhenOnlyTheSecondCaseDrivenClientIsDown() {
            when(caseDrivenClient.isActive()).thenReturn(true);
            when(secondCaseDrivenClient.isActive()).thenReturn(false);

            assertThat(multiRouter.isActive()).isFalse();
        }

        @Test
        void shouldBeActiveWhenEveryClientIs() {
            when(caseDrivenClient.isActive()).thenReturn(true);
            when(secondCaseDrivenClient.isActive()).thenReturn(true);
            when(schedulerClient.isActive()).thenReturn(true);

            assertThat(multiRouter.isActive()).isTrue();
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
        Set<String> mutable = new HashSet<>(Set.of(SCHEDULER_TOPIC));
        TopicRoutingExternalTaskClient client =
            new TopicRoutingExternalTaskClient(caseDrivenClient, schedulerClient, mutable);

        mutable.add(CASE_TOPIC);

        assertThat(client.clientsFor(CASE_TOPIC)).containsExactly(caseDrivenClient);
    }

    @Test
    void shouldNotBeAffectedByLaterMutationOfTheClientList() {
        List<ExternalTaskClient> mutable = new ArrayList<>(List.of(caseDrivenClient));
        TopicRoutingExternalTaskClient client =
            new TopicRoutingExternalTaskClient(mutable, schedulerClient, Set.of(SCHEDULER_TOPIC));

        mutable.add(mock(ExternalTaskClient.class));

        assertThat(client.clientsFor(CASE_TOPIC)).containsExactly(caseDrivenClient);
    }

    /**
     * Zero case driven clients would leave every case driven topic unsubscribed, which is a silent
     * total loss of case processing rather than a visible failure, so it must not start up.
     */
    @Test
    void shouldRejectAnEmptyCaseDrivenClientList() {
        assertThatThrownBy(() -> new TopicRoutingExternalTaskClient(
            List.of(), schedulerClient, Set.of(SCHEDULER_TOPIC)))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("At least one case driven");
    }
}
