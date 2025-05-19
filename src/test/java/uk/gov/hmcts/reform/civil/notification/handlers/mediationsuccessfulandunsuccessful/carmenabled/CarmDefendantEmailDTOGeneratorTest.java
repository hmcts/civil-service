package uk.gov.hmcts.reform.civil.notification.handlers.mediationsuccessfulandunsuccessful.carmenabled;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.notification.handlers.CamundaProcessIdentifier.MediationSuccessfulNotifyParties;

@ExtendWith(MockitoExtension.class)
class CarmDefendantEmailDTOGeneratorTest {

    private static final String TEMPLATE_SUCCESSFUL_EN = "template-successful-en";
    private static final String TEMPLATE_SUCCESSFUL_WELSH = "template-successful-welsh";
    private static final String TEMPLATE_UNSUCCESSFUL_EN = "template-unsuccessful-en";
    private static final String TEMPLATE_UNSUCCESSFUL_WELSH = "template-unsuccessful-welsh";

    @Mock
    private NotificationsProperties notificationsProperties;

    @InjectMocks
    private CarmDefendantEmailDTOGenerator generator;

    @Test
    void shouldReturnSuccessfulEnglishTemplate_whenTaskIdMatches_andRespondentNotBilingual() {
        CaseData caseData = mock(CaseData.class);
        when(caseData.isRespondentResponseBilingual()).thenReturn(false);

        when(notificationsProperties.getNotifyLipSuccessfulMediation())
            .thenReturn(TEMPLATE_SUCCESSFUL_EN);

        String templateId = generator.getEmailTemplateId(caseData, MediationSuccessfulNotifyParties.toString());

        assertThat(templateId).isEqualTo(TEMPLATE_SUCCESSFUL_EN);
    }

    @Test
    void shouldReturnSuccessfulWelshTemplate_whenTaskIdMatches_andRespondentBilingual() {
        CaseData caseData = mock(CaseData.class);
        when(caseData.isRespondentResponseBilingual()).thenReturn(true);
        when(notificationsProperties.getNotifyLipSuccessfulMediationWelsh())
            .thenReturn(TEMPLATE_SUCCESSFUL_WELSH);

        String templateId = generator.getEmailTemplateId(caseData, MediationSuccessfulNotifyParties.toString());

        assertThat(templateId).isEqualTo(TEMPLATE_SUCCESSFUL_WELSH);
    }

    @Test
    void shouldReturnUnsuccessfulEnglishTemplate_whenTaskIdDiffers_andRespondentNotBilingual() {
        CaseData caseData = mock(CaseData.class);
        when(caseData.isRespondentResponseBilingual()).thenReturn(false);

        when(notificationsProperties.getMediationUnsuccessfulLIPTemplate())
            .thenReturn(TEMPLATE_UNSUCCESSFUL_EN);

        String templateId = generator.getEmailTemplateId(caseData, "someOtherTask");

        assertThat(templateId).isEqualTo(TEMPLATE_UNSUCCESSFUL_EN);
    }

    @Test
    void shouldReturnUnsuccessfulWelshTemplate_whenTaskIdDiffers_andRespondentBilingual() {
        CaseData caseData = mock(CaseData.class);
        when(caseData.isRespondentResponseBilingual()).thenReturn(true);

        when(notificationsProperties.getMediationUnsuccessfulLIPTemplateWelsh())
            .thenReturn(TEMPLATE_UNSUCCESSFUL_WELSH);
        String templateId = generator.getEmailTemplateId(caseData, "someOtherTask");

        assertThat(templateId).isEqualTo(TEMPLATE_UNSUCCESSFUL_WELSH);
    }

    @Test
    void shouldReturnReferenceTemplate() {
        String refTemplate = generator.getReferenceTemplate();

        assertThat(refTemplate).isEqualTo("mediation-update-defendant-notification-LIP-%s");
    }
}
