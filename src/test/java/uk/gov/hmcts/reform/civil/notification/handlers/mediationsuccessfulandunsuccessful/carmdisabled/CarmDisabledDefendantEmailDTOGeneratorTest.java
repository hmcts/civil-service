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
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.*;
import static uk.gov.hmcts.reform.civil.notification.handlers.CamundaProcessIdentifier.MediationSuccessfulNotifyParties;

@ExtendWith(MockitoExtension.class)
class CarmDisabledDefendantEmailDTOGeneratorTest {

    @Mock
    private NotificationsProperties notificationsProperties;

    @InjectMocks
    private CarmDisabledDefendantEmailDTOGenerator generator;

    private CaseData caseData;

    @BeforeEach
    void setUp() {
        caseData = CaseData.builder()
            .legacyCaseReference("CASE123456")
            .applicant1(Party.builder()
                            .individualFirstName("Alice")
                            .individualLastName("Smith")
                            .type(Party.Type.INDIVIDUAL)
                            .build())
            .respondent1(Party.builder()
                             .individualFirstName("Bob")
                             .individualLastName("Brown")
                             .type(Party.Type.INDIVIDUAL)
                             .build())
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

        Map<String, String> result = generator.addCustomProperties(props, caseData);

        assertThat(result)
            .containsEntry(CLAIM_REFERENCE_NUMBER, "CASE123456")
            .containsEntry(CLAIMANT_NAME, "Alice Smith")
            .containsEntry(DEFENDANT_NAME, "Bob Brown");
    }
}
