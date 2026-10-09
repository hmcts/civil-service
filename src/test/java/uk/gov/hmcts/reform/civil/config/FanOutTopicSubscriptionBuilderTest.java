package uk.gov.hmcts.reform.civil.config;

import org.camunda.bpm.client.task.ExternalTaskHandler;
import org.camunda.bpm.client.topic.TopicSubscription;
import org.camunda.bpm.client.topic.TopicSubscriptionBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class FanOutTopicSubscriptionBuilderTest {

    private TopicSubscriptionBuilder first;
    private TopicSubscriptionBuilder second;
    private FanOutTopicSubscriptionBuilder builder;

    @BeforeEach
    void setUp() {
        first = mock(TopicSubscriptionBuilder.class);
        second = mock(TopicSubscriptionBuilder.class);
        builder = new FanOutTopicSubscriptionBuilder(List.of(first, second));
    }

    /**
     * The one method every listener calls. If the handler reached only one delegate, the other
     * client would hold a subscription with no handler and the extra thread would do nothing.
     */
    @Test
    void shouldApplyTheHandlerToEveryDelegate() {
        ExternalTaskHandler handler = mock(ExternalTaskHandler.class);

        assertThat(builder.handler(handler)).isSameAs(builder);

        verify(first).handler(handler);
        verify(second).handler(handler);
    }

    @Test
    void shouldApplyLockDurationToEveryDelegate() {
        assertThat(builder.lockDuration(1980000L)).isSameAs(builder);

        verify(first).lockDuration(1980000L);
        verify(second).lockDuration(1980000L);
    }

    /**
     * Nothing subscribes with these today, but a divergent subscription definition between the
     * clients serving one topic would be very hard to spot, so they all fan out.
     */
    @Nested
    class EveryOtherBuilderMethodFansOut {

        @Test
        void variables() {
            builder.variables("a", "b");
            verify(first).variables("a", "b");
            verify(second).variables("a", "b");
        }

        @Test
        void localVariables() {
            builder.localVariables(true);
            verify(first).localVariables(true);
            verify(second).localVariables(true);
        }

        @Test
        void businessKey() {
            builder.businessKey("key");
            verify(first).businessKey("key");
            verify(second).businessKey("key");
        }

        @Test
        void processDefinitionId() {
            builder.processDefinitionId("id");
            verify(first).processDefinitionId("id");
            verify(second).processDefinitionId("id");
        }

        @Test
        void processDefinitionIdIn() {
            builder.processDefinitionIdIn("id1", "id2");
            verify(first).processDefinitionIdIn("id1", "id2");
            verify(second).processDefinitionIdIn("id1", "id2");
        }

        @Test
        void processDefinitionKey() {
            builder.processDefinitionKey("key");
            verify(first).processDefinitionKey("key");
            verify(second).processDefinitionKey("key");
        }

        @Test
        void processDefinitionKeyIn() {
            builder.processDefinitionKeyIn("k1", "k2");
            verify(first).processDefinitionKeyIn("k1", "k2");
            verify(second).processDefinitionKeyIn("k1", "k2");
        }

        @Test
        void processDefinitionVersionTag() {
            builder.processDefinitionVersionTag("tag");
            verify(first).processDefinitionVersionTag("tag");
            verify(second).processDefinitionVersionTag("tag");
        }

        @Test
        void processVariablesEqualsIn() {
            Map<String, Object> vars = Map.of("k", "v");
            builder.processVariablesEqualsIn(vars);
            verify(first).processVariablesEqualsIn(vars);
            verify(second).processVariablesEqualsIn(vars);
        }

        @Test
        void processVariableEquals() {
            builder.processVariableEquals("k", "v");
            verify(first).processVariableEquals("k", "v");
            verify(second).processVariableEquals("k", "v");
        }

        @Test
        void withoutTenantId() {
            builder.withoutTenantId();
            verify(first).withoutTenantId();
            verify(second).withoutTenantId();
        }

        @Test
        void tenantIdIn() {
            builder.tenantIdIn("t1");
            verify(first).tenantIdIn("t1");
            verify(second).tenantIdIn("t1");
        }

        @Test
        void includeExtensionProperties() {
            builder.includeExtensionProperties(true);
            verify(first).includeExtensionProperties(true);
            verify(second).includeExtensionProperties(true);
        }
    }

    @Nested
    class Open {

        @Test
        void shouldOpenEveryDelegateAndReturnOneHandleOverThem() {
            TopicSubscription firstSub = mock(TopicSubscription.class);
            TopicSubscription secondSub = mock(TopicSubscription.class);
            when(first.open()).thenReturn(firstSub);
            when(second.open()).thenReturn(secondSub);

            TopicSubscription opened = builder.open();

            assertThat(opened).isInstanceOf(FanOutTopicSubscription.class);
            verify(first).open();
            verify(second).open();

            opened.close();
            verify(firstSub).close();
            verify(secondSub).close();
        }

        /**
         * A topic left with some of its intended threads is a silent capacity loss, so a failed
         * open must not leave the earlier subscriptions running.
         */
        @Test
        void shouldCloseAlreadyOpenedSubscriptionsWhenALaterOneFails() {
            TopicSubscription firstSub = mock(TopicSubscription.class);
            when(first.open()).thenReturn(firstSub);
            when(second.open()).thenThrow(new IllegalStateException("engine unreachable"));

            assertThatThrownBy(() -> builder.open())
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("engine unreachable");

            verify(firstSub).close();
        }

        /**
         * The original failure is the useful one; a close that also fails while unwinding must not
         * replace it.
         */
        @Test
        void shouldReportTheOriginalFailureEvenWhenUnwindingAlsoFails() {
            TopicSubscription firstSub = mock(TopicSubscription.class);
            when(first.open()).thenReturn(firstSub);
            when(second.open()).thenThrow(new IllegalStateException("engine unreachable"));
            doThrow(new IllegalStateException("close failed")).when(firstSub).close();

            assertThatThrownBy(() -> builder.open())
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("engine unreachable");
        }
    }

    /**
     * The production shape is three case driven clients, so the fan out is exercised beyond the
     * two-delegate case: an implementation that only ever reached the first and last delegate, or
     * that stopped after a pair, would pass every test above.
     */
    @Test
    void shouldFanOutToThreeDelegates() {
        TopicSubscriptionBuilder third = mock(TopicSubscriptionBuilder.class);
        FanOutTopicSubscriptionBuilder wide =
            new FanOutTopicSubscriptionBuilder(List.of(first, second, third));
        ExternalTaskHandler handler = mock(ExternalTaskHandler.class);

        wide.handler(handler).lockDuration(1980000L);

        for (TopicSubscriptionBuilder d : List.of(first, second, third)) {
            verify(d).handler(handler);
            verify(d).lockDuration(1980000L);
        }
    }

    @Test
    void shouldOpenAllThreeAndCloseAllThree() {
        TopicSubscriptionBuilder third = mock(TopicSubscriptionBuilder.class);
        TopicSubscription s1 = mock(TopicSubscription.class);
        TopicSubscription s2 = mock(TopicSubscription.class);
        TopicSubscription s3 = mock(TopicSubscription.class);
        when(first.open()).thenReturn(s1);
        when(second.open()).thenReturn(s2);
        when(third.open()).thenReturn(s3);

        new FanOutTopicSubscriptionBuilder(List.of(first, second, third)).open().close();

        verify(s1).close();
        verify(s2).close();
        verify(s3).close();
    }

    @Test
    void shouldRejectNoDelegates() {
        assertThatThrownBy(() -> new FanOutTopicSubscriptionBuilder(List.of()))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("at least one delegate");
    }

    @Test
    void shouldNotBeAffectedByLaterMutationOfTheDelegateList() {
        List<TopicSubscriptionBuilder> mutable = new ArrayList<>(List.of(first));
        FanOutTopicSubscriptionBuilder fanOut = new FanOutTopicSubscriptionBuilder(mutable);
        TopicSubscriptionBuilder added = mock(TopicSubscriptionBuilder.class);
        mutable.add(added);

        fanOut.handler(mock(ExternalTaskHandler.class));

        verifyNoInteractions(added);
    }
}
