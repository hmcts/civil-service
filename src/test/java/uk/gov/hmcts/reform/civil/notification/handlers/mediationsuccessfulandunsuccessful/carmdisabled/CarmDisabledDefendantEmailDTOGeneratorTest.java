package uk.gov.hmcts.reform.civil.notification.handlers.mediationsuccessfulandunsuccessful.carmdisabled;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIMANT_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.DEFENDANT_NAME;
import static uk.gov.hmcts.reform.civil.notification.handlers.CamundaProcessIdentifier.MediationSuccessfulNotifyParties;

@ExtendWith(MockitoExtension.class)
class CarmDisabledDefendantEmailDTOGeneratorTest {

    @Mock
    private NotificationsProperties notificationsProperties;

    @InjectMocks
    private CarmDisabledDefendantEmailDTOGenerator generator;

    @Mock
    private CaseData caseData;

    private Party applicant1;
    private Party respondent1;

    @BeforeEach
    void setUp() {
        applicant1 = Party.builder()
            .type(Party.Type.INDIVIDUAL)
            .individualFirstName("Alice")
            .individualLastName("Smith")
            .individualTitle("Mrs")
            .build();

        respondent1 = Party.builder()
            .type(Party.Type.INDIVIDUAL)
            .individualFirstName("Bob")
            .individualLastName("Brown")
            .individualTitle("Mr")
            .build();
    }

    @Test
    void shouldReturnMediationSuccessfulTemplate_whenTaskIdMatches_andNotBilingual() {
        when(caseData.isRespondentResponseBilingual()).thenReturn(false);
        when(notificationsProperties.getNotifyRespondentLiPMediationSuccessfulTemplate())
            .thenReturn("template-success");

        String result = generator.getEmailTemplateId(caseData, MediationSuccessfulNotifyParties.toString());

        assertThat(result).isEqualTo("template-success");
    }

    @Test
    void shouldReturnMediationSuccessfulWelshTemplate_whenTaskIdMatches_andIsBilingual() {
        when(caseData.isRespondentResponseBilingual()).thenReturn(true);
        when(notificationsProperties.getNotifyRespondentLiPMediationSuccessfulTemplateWelsh())
            .thenReturn("template-success-welsh");

        String result = generator.getEmailTemplateId(caseData, MediationSuccessfulNotifyParties.toString());

        assertThat(result).isEqualTo("template-success-welsh");
    }

    @Test
    void shouldReturnMediationUnsuccessfulTemplate_whenTaskIdNotMatching_andNotBilingual() {
        when(caseData.isRespondentResponseBilingual()).thenReturn(false);
        when(notificationsProperties.getMediationUnsuccessfulDefendantLIPTemplate())
            .thenReturn("template-fail");

        String result = generator.getEmailTemplateId(caseData, "someOtherTask");

        assertThat(result).isEqualTo("template-fail");
    }

    @Test
    void shouldReturnMediationUnsuccessfulWelshTemplate_whenTaskIdNotMatching_andIsBilingual() {
        when(caseData.isRespondentResponseBilingual()).thenReturn(true);
        when(notificationsProperties.getMediationUnsuccessfulDefendantLIPBilingualTemplate())
            .thenReturn("template-fail-welsh");

        String result = generator.getEmailTemplateId(caseData, "someOtherTask");

        assertThat(result).isEqualTo("template-fail-welsh");
    }

    @Test
    void shouldReturnReferenceTemplate() {
        String reference = generator.getReferenceTemplate();

        assertThat(reference).isEqualTo("mediation-update-respondent-notification-%s");
    }

    @Test
    void shouldAddCustomPropertiesToMap() {
        Map<String, String> props = new HashMap<>();

        when(caseData.getLegacyCaseReference()).thenReturn("CASE123456");
        when(caseData.getApplicant1()).thenReturn(applicant1);
        when(caseData.getRespondent1()).thenReturn(respondent1);
        Map<String, String> result = generator.addCustomProperties(props, caseData);

        assertThat(result)
            .containsEntry(CLAIM_REFERENCE_NUMBER, "CASE123456")
            .containsEntry(CLAIMANT_NAME, "Mrs Alice Smith")
            .containsEntry(DEFENDANT_NAME, "Mr Bob Brown");
    }
}
