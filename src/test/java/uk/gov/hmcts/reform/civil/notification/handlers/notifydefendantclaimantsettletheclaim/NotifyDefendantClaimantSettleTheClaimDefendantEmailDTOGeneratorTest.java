package uk.gov.hmcts.reform.civil.notification.handlers.notifydefendantclaimantsettletheclaim;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIMANT_NAME;

public class NotifyDefendantClaimantSettleTheClaimDefendantEmailDTOGeneratorTest {

    @InjectMocks
    private NotifyDefendantClaimantSettleTheClaimDefendantEmailDTOGenerator emailDTOGenerator;

    @Mock
    private NotificationsProperties notificationsProperties;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldReturnCorrectEmailTemplateId() {
        CaseData caseData = CaseData.builder().build();

        String expectedTemplateId = "template-id";
        when(notificationsProperties.getNotifyDefendantLIPClaimantSettleTheClaimTemplate()).thenReturn(expectedTemplateId);

        String actualTemplateId = emailDTOGenerator.getEmailTemplateId(caseData);

        assertThat(actualTemplateId).isEqualTo(expectedTemplateId);
    }

    @Test
    void shouldReturnCorrectReferenceTemplate() {
        String referenceTemplate = emailDTOGenerator.getReferenceTemplate();

        assertThat(referenceTemplate).isEqualTo("notify-defendant-claimant-settle-the-claim-notification-%s");
    }

    @Test
    void shouldAddCustomProperties() {
        String claimantName = "Claimant Company Ltd";
        String respondentName = "Defendant Company Ltd";
        CaseData caseData = CaseData.builder()
            .applicant1(Party.builder().type(Party.Type.COMPANY).companyName(claimantName).build())
            .respondent1(Party.builder().type(Party.Type.COMPANY).companyName(respondentName).build())
            .build();

        Map<String, String> properties = new HashMap<>();
        Map<String, String> updatedProperties = emailDTOGenerator.addCustomProperties(properties, caseData);

        assertThat(updatedProperties).containsEntry(CLAIMANT_NAME, claimantName);
    }
}
