package uk.gov.hmcts.reform.civil.notification.handlers.ga;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import uk.gov.hmcts.reform.civil.notification.handlers.Notifier;
import uk.gov.hmcts.reform.civil.notification.handlers.PartiesEmailGenerator;
import uk.gov.hmcts.reform.civil.notification.handlers.ga.gahearingscheduled.GaHearingScheduledAllPartiesEmailGenerator;
import uk.gov.hmcts.reform.civil.notification.handlers.ga.gahearingscheduled.GaHearingScheduledNotifier;
import uk.gov.hmcts.reform.civil.notification.handlers.gahwfapplicant.GaHwfApplicantEmailGenerator;
import uk.gov.hmcts.reform.civil.notification.handlers.gahwfapplicant.GaHwfApplicantNotifier;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.service.CaseTaskTrackingService;

/**
 * Every General Application notifier is a {@code PartiesEmailGenerator<GeneralApplicationCaseData>}
 * consumer, and type erasure means Spring cannot tell two of them apart. A notifier that injects
 * the unqualified interface therefore starts fine while it is the only GA generator and fails the
 * whole context the moment a second one is added, which is a startup failure rather than a test
 * failure. DTSCCI-6726 adds ten more, so this pins it.
 */
class GaNotifierBeanWiringTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
        .withBean(NotificationService.class, () -> mock(NotificationService.class))
        .withBean(CaseTaskTrackingService.class, () -> mock(CaseTaskTrackingService.class))
        .withBean(GaHwfApplicantEmailGenerator.class, () -> mock(GaHwfApplicantEmailGenerator.class))
        .withBean(GaHearingScheduledAllPartiesEmailGenerator.class,
                  () -> mock(GaHearingScheduledAllPartiesEmailGenerator.class))
        .withBean(GaHwfApplicantNotifier.class)
        .withBean(GaHearingScheduledNotifier.class);

    @Test
    void shouldStartWithMoreThanOneGeneralApplicationGeneratorPresent() {
        runner.run(context -> {
            assertThat(context).hasNotFailed();
            assertThat(context.getBean(GaHwfApplicantNotifier.class).getClass())
                .isAssignableTo(Notifier.class);
        });
    }

    @Test
    void everyGaGeneratorIsAssignableToTheSharedInterface() {
        // the reason the ambiguity exists, stated so the next person does not have to rediscover it
        assertThat(PartiesEmailGenerator.class)
            .isAssignableFrom(GaHwfApplicantEmailGenerator.class)
            .isAssignableFrom(GaHearingScheduledAllPartiesEmailGenerator.class);
    }
}
