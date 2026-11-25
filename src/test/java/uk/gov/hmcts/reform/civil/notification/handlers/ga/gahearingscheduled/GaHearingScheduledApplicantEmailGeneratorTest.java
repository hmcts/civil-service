package uk.gov.hmcts.reform.civil.notification.handlers.ga.gahearingscheduled;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.ga.service.GaForLipService;
import uk.gov.hmcts.reform.civil.ga.service.HearingScheduledNotificationService;
import uk.gov.hmcts.reform.civil.model.GeneralAppParentCaseLink;
import uk.gov.hmcts.reform.civil.model.genapplication.GASolicitorDetailsGAspec;
import uk.gov.hmcts.reform.civil.notification.handlers.EmailDTO;
import uk.gov.hmcts.reform.civil.notification.handlers.ga.GaNotificationContext;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;

import java.util.HashMap;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class GaHearingScheduledApplicantEmailGeneratorTest {

    private static final String PARENT_REF = "1594901956117591";
    private static final String TASK_ID = "NotifyGaHearingScheduled";

    private HearingScheduledNotificationService service;
    private NotificationsProperties notificationsProperties;
    private GaForLipService gaForLipService;
    private GeneralApplicationCaseData gaCaseData;
    private GaNotificationContext context;
    private GaHearingScheduledApplicantEmailGenerator generator;

    @BeforeEach
    void setUp() {
        service = mock(HearingScheduledNotificationService.class);
        notificationsProperties = mock(NotificationsProperties.class);
        gaForLipService = mock(GaForLipService.class);
        generator = new GaHearingScheduledApplicantEmailGenerator(
            service, notificationsProperties, gaForLipService);

        gaCaseData = mock(GeneralApplicationCaseData.class);
        when(gaCaseData.getCcdCaseReference()).thenReturn(1234567890123456L);
        when(gaCaseData.getGeneralAppParentCaseLink())
            .thenReturn(new GeneralAppParentCaseLink(PARENT_REF));
        when(gaCaseData.getGeneralAppApplnSolicitor())
            .thenReturn(new GASolicitorDetailsGAspec().setEmail("applicant@example.com"));
        context = new GaNotificationContext(gaCaseData, mock(GeneralApplicationCaseData.class));
        when(service.addPropertiesByType(any(), any(), any())).thenReturn(new HashMap<>());
        when(notificationsProperties.getHearingNoticeTemplate()).thenReturn("hearing-notice");
    }

    @Test
    void shouldNotifyTheApplicantSolicitorUnderTheHearingReference() {
        EmailDTO email = generator.buildEmailDTO(context, TASK_ID);

        assertThat(email.getTargetEmail()).isEqualTo("applicant@example.com");
        assertThat(email.getEmailTemplate()).isEqualTo("hearing-notice");
        // the retired handler sent under this reference, so the migration must not change it
        assertThat(email.getReference()).isEqualTo("general-apps-notice-of-hearing-" + PARENT_REF);
        verify(service).addPropertiesByType(gaCaseData, context.mainCaseData(), "applicant");
    }

    @Test
    void shouldUseTheLipTemplateForALipApplicant() {
        when(gaForLipService.isLipApp(gaCaseData)).thenReturn(true);
        when(service.getLiPApplicantTemplates(gaCaseData)).thenReturn("lip-applicant-template");

        assertThat(generator.buildEmailDTO(context, TASK_ID).getEmailTemplate())
            .isEqualTo("lip-applicant-template");
    }

    @Test
    void shouldNotNotifyWhenThereIsNoApplicantSolicitor() {
        when(gaCaseData.getGeneralAppApplnSolicitor()).thenReturn(null);

        // the retired handler logged and returned rather than failing
        assertThat(generator.getShouldNotify(context)).isFalse();
    }

    @Test
    void shouldNotifyWhenThereIsAnApplicantSolicitor() {
        assertThat(generator.getShouldNotify(context)).isTrue();
    }
}
