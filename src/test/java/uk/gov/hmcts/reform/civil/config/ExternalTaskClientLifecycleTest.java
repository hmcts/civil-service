package uk.gov.hmcts.reform.civil.config;

import org.camunda.bpm.client.ExternalTaskClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ExternalTaskClientLifecycleTest {

    @Mock
    private ExternalTaskClient client;

    @Test
    void shouldStartAndStopTheClient() {
        ExternalTaskClientLifecycle lifecycle = new ExternalTaskClientLifecycle(client);
        assertThat(lifecycle.isRunning()).isFalse();

        lifecycle.start();
        assertThat(lifecycle.isRunning()).isTrue();
        verify(client).start();

        lifecycle.stop();
        assertThat(lifecycle.isRunning()).isFalse();
        verify(client).stop();
    }

    @Test
    void shouldReturnAtOnceAndCallBackOnlyWhenTheClientHasStopped() throws InterruptedException {
        CountDownLatch pollInProgress = new CountDownLatch(1);
        CountDownLatch calledBack = new CountDownLatch(1);
        doAnswer(invocation -> pollInProgress.await(5, TimeUnit.SECONDS)).when(client).stop();

        new ExternalTaskClientLifecycle(client).stop(calledBack::countDown);

        assertThat(calledBack.getCount()).isEqualTo(1);
        pollInProgress.countDown();
        assertThat(calledBack.await(5, TimeUnit.SECONDS)).isTrue();
    }

    @Test
    void shouldCallBackWhenStoppingFails() throws InterruptedException {
        CountDownLatch calledBack = new CountDownLatch(1);
        doThrow(new IllegalStateException("stop failed")).when(client).stop();

        new ExternalTaskClientLifecycle(client).stop(calledBack::countDown);

        assertThat(calledBack.await(5, TimeUnit.SECONDS)).isTrue();
    }

    @Test
    void shouldStopInTheFirstPhaseAndNotBePauseable() {
        ExternalTaskClientLifecycle lifecycle = new ExternalTaskClientLifecycle(client);

        assertThat(lifecycle.getPhase()).isEqualTo(Integer.MAX_VALUE);
        assertThat(lifecycle.isPauseable()).isFalse();
    }
}
