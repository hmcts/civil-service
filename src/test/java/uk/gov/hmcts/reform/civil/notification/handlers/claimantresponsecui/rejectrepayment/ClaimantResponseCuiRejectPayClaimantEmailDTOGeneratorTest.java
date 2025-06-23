package uk.gov.hmcts.reform.civil.notification.handlers.claimantresponsecui.rejectrepayment;

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
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIMANT_V_DEFENDANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_REFERENCE_NUMBER;

@ExtendWith(MockitoExtension.class)
class ClaimantResponseCuiRejectPayClaimantEmailDTOGeneratorTest {

    private static final String REFERENCE_TEMPLATE = "claimant-reject-repayment-respondent-notification-%s";

    @Mock
    private NotificationsProperties notificationsProperties;

    @InjectMocks
    private ClaimantResponseCuiRejectPayClaimantEmailDTOGenerator emailGenerator;

    @Test
    void shouldReturnCorrectEmailTemplateId_whenClaimantGetTemplateIsInvoked() {
        CaseData caseData = CaseData.builder().build();
        String expectedTemplateId = "template-id";
        when(notificationsProperties.getNotifyClaimantLipTemplateManualDetermination()).thenReturn(expectedTemplateId);

        String actualTemplateId = emailGenerator.getEmailTemplateId(caseData);

        assertThat(actualTemplateId).isEqualTo(expectedTemplateId);
    }

    @Test
    void shouldAddCustomProperties() {
        CaseData caseData = CaseData.builder()
            .legacyCaseReference("12345")
            .applicant1(Party.builder().partyName("Claimant Name").individualFirstName("Claimant").individualLastName("Name").type(Party.Type.INDIVIDUAL).build())
            .respondent1(Party.builder().partyName("Defendant Name").individualFirstName("Defendant").individualLastName("Name").type(Party.Type.INDIVIDUAL).build())
            .build();

        Map<String, String> properties = emailGenerator.addCustomProperties(new HashMap<>(), caseData);

        assertThat(properties).containsEntry(CLAIMANT_V_DEFENDANT, "Claimant Name V Defendant Name");
        assertThat(properties).containsEntry(CLAIM_REFERENCE_NUMBER, "12345");
        assertThat(properties).containsEntry(CLAIMANT_NAME, "Claimant Name");
    }

    @Test
    void shouldReturnCorrectReferenceTemplate() {
        String referenceTemplate = emailGenerator.getReferenceTemplate();

        assertThat(referenceTemplate).isEqualTo(REFERENCE_TEMPLATE);
    }

}
