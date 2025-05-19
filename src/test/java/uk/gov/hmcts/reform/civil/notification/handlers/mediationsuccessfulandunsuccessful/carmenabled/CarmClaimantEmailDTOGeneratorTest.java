package uk.gov.hmcts.reform.civil.notification.handlers.mediationsuccessfulandunsuccessful.carmenabled;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.enums.dq.Language;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.PARTY_NAME;
import static uk.gov.hmcts.reform.civil.notification.handlers.CamundaProcessIdentifier.MediationSuccessfulNotifyParties;

@ExtendWith(MockitoExtension.class)
class CarmClaimantEmailDTOGeneratorTest {

    private static final String CASE_REFERENCE = "12345";
    private static final String APPLICANT_NAME = "Mr John Doe";

    private static final String TEMPLATE_SUCCESSFUL_EN = "template-successful-en";
    private static final String TEMPLATE_SUCCESSFUL_WELSH = "template-successful-welsh";
    private static final String TEMPLATE_UNSUCCESSFUL_EN = "template-unsuccessful-en";
    private static final String TEMPLATE_UNSUCCESSFUL_WELSH = "template-unsuccessful-welsh";

    @Mock
    private NotificationsProperties notificationsProperties;

    @InjectMocks
    private CarmClaimantEmailDTOGenerator generator;

    private CaseData caseData;

    @BeforeEach
    void setUp() {
        caseData = CaseData.builder()
            .ccdCaseReference(Long.valueOf(CASE_REFERENCE))
            .applicant1(Party.builder()
                            .individualTitle("Mr")
                            .individualFirstName("John")
                            .individualLastName("Doe")
                            .partyName(APPLICANT_NAME).build())
            .build();
    }

    @Test
    void shouldReturnSuccessfulEnglishTemplate_whenTaskIdMatches_andClaimantNotBilingual() {
        CaseData updatedData = caseData.toBuilder()
            .claimantBilingualLanguagePreference(null)
            .build();

        String templateId = generator.getEmailTemplateId(updatedData, MediationSuccessfulNotifyParties.toString());

        assertThat(templateId).isEqualTo(TEMPLATE_SUCCESSFUL_EN);
    }

    @Test
    void shouldReturnSuccessfulWelshTemplate_whenTaskIdMatches_andClaimantBilingual() {
        CaseData updatedData = caseData.toBuilder()
            .claimantBilingualLanguagePreference(Language.WELSH.getDisplayedValue())
            .build();

        when(notificationsProperties.getNotifyLipSuccessfulMediationWelsh())
            .thenReturn(TEMPLATE_SUCCESSFUL_WELSH);
        String templateId = generator.getEmailTemplateId(updatedData, MediationSuccessfulNotifyParties.toString());

        assertThat(templateId).isEqualTo(TEMPLATE_SUCCESSFUL_WELSH);
    }

    @Test
    void shouldReturnUnsuccessfulEnglishTemplate_whenTaskIdDoesNotMatch_andClaimantNotBilingual() {
        CaseData updatedData = caseData.toBuilder()
            .claimantBilingualLanguagePreference(null)
            .build();

        when(notificationsProperties.getMediationUnsuccessfulLIPTemplate())
            .thenReturn(TEMPLATE_UNSUCCESSFUL_EN);
        String templateId = generator.getEmailTemplateId(updatedData, "someOtherTask");

        assertThat(templateId).isEqualTo(TEMPLATE_UNSUCCESSFUL_EN);
    }

    @Test
    void shouldReturnUnsuccessfulWelshTemplate_whenTaskIdDoesNotMatch_andClaimantBilingual() {
        CaseData updatedData = caseData.toBuilder()
            .claimantBilingualLanguagePreference(Language.WELSH.getDisplayedValue())
            .build();

        when(notificationsProperties.getMediationUnsuccessfulLIPTemplateWelsh())
            .thenReturn(TEMPLATE_UNSUCCESSFUL_WELSH);

        String templateId = generator.getEmailTemplateId(updatedData, "someOtherTask");

        assertThat(templateId).isEqualTo(TEMPLATE_UNSUCCESSFUL_WELSH);
    }

    @Test
    void shouldReturnReferenceTemplate() {
        String referenceTemplate = generator.getReferenceTemplate();

        assertThat(referenceTemplate).isEqualTo("mediation-update-claimant-notification-LIP-%s");
    }

    @Test
    void shouldAddCustomPropertiesToMap() {
        Map<String, String> props = new HashMap<>();

        Map<String, String> result = generator.addCustomProperties(props, caseData);

        assertThat(result).containsEntry(PARTY_NAME, APPLICANT_NAME);
        assertThat(result).containsEntry(CLAIM_REFERENCE_NUMBER, CASE_REFERENCE);
    }
}
