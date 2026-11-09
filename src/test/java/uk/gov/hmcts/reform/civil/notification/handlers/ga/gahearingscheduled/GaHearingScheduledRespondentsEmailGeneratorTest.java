package uk.gov.hmcts.reform.civil.notification.handlers.ga.gahearingscheduled;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.element;

import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.ga.service.GaForLipService;
import uk.gov.hmcts.reform.civil.ga.service.HearingScheduledNotificationService;
import uk.gov.hmcts.reform.civil.model.GeneralAppParentCaseLink;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.genapplication.GASolicitorDetailsGAspec;
import uk.gov.hmcts.reform.civil.notification.handlers.EmailDTO;
import uk.gov.hmcts.reform.civil.notification.handlers.ga.GaNotificationContext;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * General Application respondents are a collection rather than fixed slots, so this generator is
 * the first real use of the fan out hook on EmailDTOGenerator.
 */
class GaHearingScheduledRespondentsEmailGeneratorTest {

    private static final String PARENT_REF = "1594901956117591";
    private static final String TASK_ID = "NotifyGaHearingScheduled";

    private HearingScheduledNotificationService service;
    private NotificationsProperties notificationsProperties;
    private GaForLipService gaForLipService;
    private GeneralApplicationCaseData gaCaseData;
    private GaNotificationContext context;
    private GaHearingScheduledRespondentsEmailGenerator generator;

    @BeforeEach
    void setUp() {
        service = mock(HearingScheduledNotificationService.class);
        notificationsProperties = mock(NotificationsProperties.class);
        gaForLipService = mock(GaForLipService.class);
        generator = new GaHearingScheduledRespondentsEmailGenerator(
            service, notificationsProperties, gaForLipService);

        gaCaseData = mock(GeneralApplicationCaseData.class);
        when(gaCaseData.getCcdCaseReference()).thenReturn(1234567890123456L);
        when(gaCaseData.getGeneralAppParentCaseLink())
            .thenReturn(new GeneralAppParentCaseLink(PARENT_REF));
        context = new GaNotificationContext(gaCaseData, mock(GeneralApplicationCaseData.class));

        when(service.addPropertiesByType(any(), any(), any())).thenReturn(new HashMap<>());
        when(notificationsProperties.getHearingNoticeTemplate()).thenReturn("hearing-notice");
    }

    private static <T> T any() {
        return org.mockito.ArgumentMatchers.any();
    }

    private Element<GASolicitorDetailsGAspec> solicitor(String email) {
        // no @Builder on this model, it uses chained accessors
        GASolicitorDetailsGAspec s = new GASolicitorDetailsGAspec().setEmail(email);
        return element(s);
    }

    @Test
    void shouldBuildOneEmailPerRespondentSolicitor() {
        when(gaCaseData.getGeneralAppRespondentSolicitors())
            .thenReturn(List.of(solicitor("resp1@example.com"), solicitor("resp2@example.com")));

        Set<EmailDTO> emails = generator.buildEmailDTOs(context, TASK_ID);

        // the aggregator must not assume one email per generator
        assertThat(emails).extracting(EmailDTO::getTargetEmail)
            .containsExactlyInAnyOrder("resp1@example.com", "resp2@example.com");
        assertThat(emails).allSatisfy(e ->
            assertThat(e.getReference()).isEqualTo("general-apps-notice-of-hearing-" + PARENT_REF));
    }

    @Test
    void shouldSkipSolicitorsWithoutAnEmail() {
        List<Element<GASolicitorDetailsGAspec>> respondents = new ArrayList<>();
        respondents.add(solicitor("resp1@example.com"));
        respondents.add(solicitor(null));
        when(gaCaseData.getGeneralAppRespondentSolicitors()).thenReturn(respondents);

        assertThat(generator.buildEmailDTOs(context, TASK_ID))
            .extracting(EmailDTO::getTargetEmail).containsExactly("resp1@example.com");
    }

    @Test
    void shouldFallBackToTheLipRespondentWhenThereAreNoSolicitors() {
        when(gaCaseData.getGeneralAppRespondentSolicitors()).thenReturn(List.of());
        when(gaForLipService.isLipResp(gaCaseData)).thenReturn(true);
        when(service.getLipRespondentEmail(any(), any())).thenReturn("lip-resp@example.com");
        when(service.getLiPRespondentTemplate(gaCaseData)).thenReturn("lip-template");

        Set<EmailDTO> emails = generator.buildEmailDTOs(context, TASK_ID);

        assertThat(emails).extracting(EmailDTO::getTargetEmail).containsExactly("lip-resp@example.com");
        assertThat(emails).extracting(EmailDTO::getEmailTemplate).containsExactly("lip-template");
    }

    @Test
    void shouldNotNotifyWhenThereIsNoSolicitorAndNoLipRespondent() {
        when(gaCaseData.getGeneralAppRespondentSolicitors()).thenReturn(List.of());
        when(gaForLipService.isLipResp(gaCaseData)).thenReturn(false);

        // the retired handler logged and returned rather than failing
        assertThat(generator.getShouldNotify(context)).isFalse();
    }

    @Test
    void shouldCopyThePropertiesTheServiceBuilds() {
        when(gaCaseData.getGeneralAppRespondentSolicitors()).thenReturn(List.of(solicitor("r@example.com")));
        java.util.Map<String, String> shared = new HashMap<>();
        shared.put("key", "value");
        when(service.addPropertiesByType(any(), any(), any())).thenReturn(shared);

        EmailDTO email = generator.buildEmailDTOs(context, TASK_ID).iterator().next();
        shared.put("mutatedAfterwards", "yes");

        // the service builds into a field it reuses, so the DTO must hold a snapshot
        assertThat(email.getParameters()).containsEntry("key", "value")
            .doesNotContainKey("mutatedAfterwards");
        verify(service).addPropertiesByType(gaCaseData, context.mainCaseData(), "respondent");
    }
}
