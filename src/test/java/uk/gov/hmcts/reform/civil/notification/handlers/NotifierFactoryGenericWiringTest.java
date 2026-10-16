package uk.gov.hmcts.reform.civil.notification.handlers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.service.CaseTaskTrackingService;

/**
 * Both factories take a varargs of {@code Notifier}, differing only in the type argument, which is
 * erased at runtime. The separation therefore rests entirely on Spring resolving generics on array
 * injection. That is worth proving rather than assuming: if it did not hold, a GA notifier would be
 * registered in the Civil factory and dispatch would pick the wrong one for a matching task id.
 */
class NotifierFactoryGenericWiringTest {

    private static final String SHARED_TASK_ID = "SharedTaskId";

    // the factories are registered as beans for Spring to autowire, with BOTH notifiers present in
    // the context. Hand wiring them here would prove nothing about generic resolution.
    private final ApplicationContextRunner runner = new ApplicationContextRunner()
        .withUserConfiguration(TestConfig.class)
        .withBean(NotifierFactory.class)
        .withBean(GaNotifierFactory.class);

    @Test
    void civilFactoryShouldOnlyReceiveCivilNotifiers() {
        runner.run(context -> {
            NotifierFactory factory = context.getBean(NotifierFactory.class);
            assertThat(factory.getNotifier(SHARED_TASK_ID))
                .as("the Civil factory must resolve the Civil notifier, not the GA one")
                .isInstanceOf(CivilTestNotifier.class);
        });
    }

    @Test
    void gaFactoryShouldOnlyReceiveGeneralApplicationNotifiers() {
        runner.run(context -> {
            GaNotifierFactory factory = context.getBean(GaNotifierFactory.class);
            assertThat(factory.getNotifier(SHARED_TASK_ID))
                .as("the GA factory must resolve the GA notifier, not the Civil one")
                .isInstanceOf(GaTestNotifier.class);
        });
    }

    static class CivilTestNotifier extends Notifier<CaseData> {
        CivilTestNotifier(NotificationService n, CaseTaskTrackingService c, PartiesEmailGenerator<CaseData> g) {
            super(n, c, g);
        }

        @Override
        protected String getTaskId() {
            return SHARED_TASK_ID;
        }
    }

    static class GaTestNotifier extends Notifier<GeneralApplicationCaseData> {
        GaTestNotifier(NotificationService n, CaseTaskTrackingService c,
                       PartiesEmailGenerator<GeneralApplicationCaseData> g) {
            super(n, c, g);
        }

        @Override
        protected String getTaskId() {
            return SHARED_TASK_ID;
        }
    }

    @Configuration
    @SuppressWarnings("unchecked")
    static class TestConfig {

        @Bean
        CivilTestNotifier civilTestNotifier() {
            return new CivilTestNotifier(mock(NotificationService.class), mock(CaseTaskTrackingService.class),
                                         mock(PartiesEmailGenerator.class));
        }

        @Bean
        GaTestNotifier gaTestNotifier() {
            return new GaTestNotifier(mock(NotificationService.class), mock(CaseTaskTrackingService.class),
                                      mock(PartiesEmailGenerator.class));
        }

    }
}
