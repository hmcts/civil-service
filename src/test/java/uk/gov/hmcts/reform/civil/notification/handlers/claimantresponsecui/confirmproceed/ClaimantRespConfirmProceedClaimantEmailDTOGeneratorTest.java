package uk.gov.hmcts.reform.civil.notification.handlers.claimantresponsecui.confirmproceed;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.dq.Language;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.APPLICANT_ONE_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_REFERENCE_NUMBER;

@ExtendWith(MockitoExtension.class)
class ClaimantRespConfirmProceedClaimantEmailDTOGeneratorTest {

    private static final String REFERENCE_TEMPLATE = "claimant-confirms-to-proceed-applicant-notification-%s";

    @Mock
    private NotificationsProperties notificationsProperties;

    @InjectMocks
    private ClaimantConfirmProceedClaimantEmailDTOGenerator emailGenerator;

    @Test
    void shouldReturnCorrectEmailTemplateId_whenClaimantGetTemplateIsInvoked() {
        CaseData caseData = CaseData.builder().applicant1(Party.builder().build()).applicant1Represented(YesOrNo.NO)
            .claimantBilingualLanguagePreference(Language.BOTH.getDisplayedValue()).build();
        String expectedTemplateId = "template-id";
        when(notificationsProperties.getClaimantLipClaimUpdatedBilingualTemplate()).thenReturn(expectedTemplateId);

        String actualTemplateId = emailGenerator.getEmailTemplateId(caseData);

        assertThat(actualTemplateId).isEqualTo(expectedTemplateId);
    }

    @Test
    void shouldReturnCorrectEmailTemplateId_whenClaimantGetTemplateIsInvokedAndBilingual() {
        CaseData caseData = CaseData.builder().applicant1(Party.builder().build()).applicant1Represented(YesOrNo.NO)
            .claimantBilingualLanguagePreference(Language.ENGLISH.getDisplayedValue()).build();
        String expectedTemplateId = "template-id";
        when(notificationsProperties.getClaimantLipClaimUpdatedTemplate()).thenReturn(expectedTemplateId);

        String actualTemplateId = emailGenerator.getEmailTemplateId(caseData);

        assertThat(actualTemplateId).isEqualTo(expectedTemplateId);
    }

    @Test
    void shouldReturnCorrectReferenceTemplate() {
        String referenceTemplate = emailGenerator.getReferenceTemplate();

        assertThat(referenceTemplate).isEqualTo(REFERENCE_TEMPLATE);
    }

    @Test
    void shouldAddCustomProperties() {
        String legacyCaseReference = "000LR001";

        Party applicant = Party.builder()
            .individualTitle("Mr")
            .individualFirstName("John")
            .individualLastName("Doe")
            .type(Party.Type.INDIVIDUAL)
            .build();

        CaseData caseData = CaseData.builder()
            .legacyCaseReference(legacyCaseReference)
            .applicant1(applicant)
            .build();

        Map<String, String> existingProperties = new HashMap<>();

        Map<String, String> result = emailGenerator.addCustomProperties(existingProperties, caseData);

        assertThat(result)
            .containsEntry(APPLICANT_ONE_NAME, "Mr John Doe")
            .containsEntry(CLAIM_REFERENCE_NUMBER, legacyCaseReference);
    }
}
