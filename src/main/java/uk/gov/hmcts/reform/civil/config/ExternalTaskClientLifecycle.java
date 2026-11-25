package uk.gov.hmcts.reform.civil.config;

import lombok.RequiredArgsConstructor;
import org.camunda.bpm.client.ExternalTaskClient;
import org.springframework.context.SmartLifecycle;

/**
 * Stops the external task clients at the start of a shutdown, while the beans their handlers use are
 * still available (DTSCCI-6899).
 *
 * <p>A client killed during a long poll or a handler leaves its task locked until
 * {@code async.event.lockDuration} (33 minutes) expires. {@link ExternalTaskClient#stop()} returns only
 * once the poll in progress has come back and any task it fetched has been handled, so waiting for it
 * lets a pod finish what it has locked. The lifecycle runs before singletons are destroyed, and the
 * highest phase runs first, so the web server is still up meanwhile.
 *
 * <p>The wait is limited by {@code spring.lifecycle.timeout-per-shutdown-phase}. The long poll
 * ({@code async.event.responseTimeout}) must be shorter than that, and both must fit in the pod's 30
 * second termination grace period. A handler that runs for longer is still cut off.
 */
@RequiredArgsConstructor
public class ExternalTaskClientLifecycle implements SmartLifecycle {

    private final ExternalTaskClient client;
    private volatile boolean running;

    /**
     * The clients start when they are built, so on a normal start up this does nothing to them.
     */
    @Override
    public void start() {
        client.start();
        running = true;
    }

    @Override
    public void stop() {
        client.stop();
        running = false;
    }

    /**
     * Returns at once and reports back when the clients have stopped, so that the phase timeout applies
     * and other beans in this phase stop at the same time.
     */
    @Override
    public void stop(Runnable callback) {
        Thread.ofPlatform().daemon().name("external-task-client-shutdown").start(() -> {
            try {
                stop();
            } finally {
                callback.run();
            }
        });
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    /**
     * Pausing is used to park cached test contexts. The clients were not paused before this class
     * existed, and waiting for their polls would slow the tests.
     */
    @Override
    public boolean isPauseable() {
        return false;
    }

    @Override
    public int getPhase() {
        return Integer.MAX_VALUE;
    }
}
