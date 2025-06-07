package uk.gov.hmcts.reform.civil.notification.handlers.changeofrepresentation.lipvliptolipvlrorlrvliptolrvlr;

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
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIMANT_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_16_DIGIT_NUMBER;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_NUMBER;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.DEFENDANT_NAME_INTERIM;

@ExtendWith(MockitoExtension.class)
class DefRepresentedClaimantLipEmailDTOGeneratorTest {

    @Mock
    private NotificationsProperties notificationsProperties;

    @InjectMocks
    private DefRepresentedClaimantLipEmailDTOGenerator generator;

    private CaseData caseData;

    @BeforeEach
    void setUp() {
        caseData = CaseData.builder()
            .ccdCaseReference(1234567890123456L)
            .legacyCaseReference("LEGACY123456")
            .applicant1(Party.builder()
                            .type(Party.Type.INDIVIDUAL)
                            .individualTitle("Mr")
                            .individualFirstName("John")
                            .individualLastName("Claimant")
                            .partyName("John Claimant").build())
            .respondent1(Party.builder()
                             .type(Party.Type.INDIVIDUAL)
                             .individualTitle("Mrs")
                             .individualFirstName("Jane")
                             .individualLastName("Defendant")
                             .partyName("Jane Defendant").build())
            .build();
    }

    @Test
    void shouldReturnBilingualTemplateIdIfClaimantIsBilingual() {
        CaseData bilingualCaseData = caseData.toBuilder()
            .claimantBilingualLanguagePreference(Language.WELSH.getDisplayedValue()).build();
        when(notificationsProperties.getNotifyClaimantLipBilingualAfterDefendantNOC())
            .thenReturn("bilingual-template-id");

        String templateId = generator.getEmailTemplateId(bilingualCaseData);

        assertThat(templateId).isEqualTo("bilingual-template-id");
    }

    @Test
    void shouldReturnDefaultTemplateIdIfClaimantIsNotBilingual() {
        CaseData monoCaseData = caseData.toBuilder()
            .claimantBilingualLanguagePreference(Language.ENGLISH.getDisplayedValue()).build();
        when(notificationsProperties.getNotifyClaimantLipForDefendantRepresentedTemplate())
            .thenReturn("default-template-id");

        String templateId = generator.getEmailTemplateId(monoCaseData);

        assertThat(templateId).isEqualTo("default-template-id");
    }

    @Test
    void shouldReturnReferenceTemplate() {
        String reference = generator.getReferenceTemplate();
        assertThat(reference).isEqualTo("notify-claimant-lip-after-defendant-noc-approval-%s");
    }

    @Test
    void shouldAddCustomProperties() {
        Map<String, String> result = generator.addCustomProperties(new HashMap<>(), caseData);

        assertThat(result).containsEntry(CLAIMANT_NAME, "Mr John Claimant");
        assertThat(result).containsEntry(DEFENDANT_NAME_INTERIM, "Mrs Jane Defendant");
        assertThat(result).containsEntry(CLAIM_NUMBER, "LEGACY123456");
        assertThat(result).containsEntry(CLAIM_16_DIGIT_NUMBER, "1234567890123456");
    }
}
