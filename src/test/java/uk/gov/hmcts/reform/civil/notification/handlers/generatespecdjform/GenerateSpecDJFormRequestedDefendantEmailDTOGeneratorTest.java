package uk.gov.hmcts.reform.civil.notification.handlers.generatespecdjform;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.utils.PartyUtils;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mockStatic;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.APPLICANT_ONE_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_NUMBER_INTERIM;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.DEFENDANT_NAME_INTERIM;

@ExtendWith(MockitoExtension.class)
class GenerateSpecDJFormRequestedDefendantEmailDTOGeneratorTest {

    private static final String TEMPLATE_ID = "template-id";
    private static final String REFERENCE_TEMPLATE = "default-judgment-respondent-requested-notification-%s";

    @Mock
    private NotificationsProperties notificationsProperties;

    private GenerateSpecDJFormRequestedDefendantEmailDTOGenerator generator;
    private MockedStatic<PartyUtils> partyUtilsMockedStatic;

    @BeforeEach
    void setUp() {
        generator = new GenerateSpecDJFormRequestedDefendantEmailDTOGenerator(notificationsProperties);
        partyUtilsMockedStatic = mockStatic(PartyUtils.class);
    }

    @AfterEach
    void tearDown() {
        if (partyUtilsMockedStatic != null) {
            partyUtilsMockedStatic.close();
        }
    }

    @Test
    void shouldReturnTemplateId() {
        CaseData caseData = mock(CaseData.class);
        when(notificationsProperties.getRespondent1DefaultJudgmentRequestedTemplate()).thenReturn(TEMPLATE_ID);

        assertThat(generator.getEmailTemplateId(caseData)).isEqualTo(TEMPLATE_ID);
    }

    @Test
    void shouldReturnReferenceTemplate() {
        assertThat(generator.getReferenceTemplate()).isEqualTo(REFERENCE_TEMPLATE);
    }

    @Test
    void shouldAddCustomProperties() {
        Map<String, String> properties = new HashMap<>();
        CaseData caseData = mock(CaseData.class);
        Party respondent1 = mock(Party.class);
        Party applicant1 = mock(Party.class);

        when(caseData.getRespondent1()).thenReturn(respondent1);
        when(caseData.getApplicant1()).thenReturn(applicant1);
        when(caseData.getCcdCaseReference()).thenReturn(1234567890123456L);
        partyUtilsMockedStatic.when(() -> PartyUtils.getPartyNameBasedOnType(respondent1)).thenReturn("Defendant 1");
        partyUtilsMockedStatic.when(() -> PartyUtils.getPartyNameBasedOnType(applicant1)).thenReturn("Applicant 1");
        partyUtilsMockedStatic.when(() -> PartyUtils.getAllPartyNames(caseData)).thenReturn("Claimant v Defendant");

        Map<String, String> result = generator.addCustomProperties(properties, caseData);

        assertThat(result).containsEntry(CLAIM_NUMBER_INTERIM, "1234567890123456");
        assertThat(result).containsEntry(DEFENDANT_NAME_INTERIM, "Defendant 1");
        assertThat(result).containsEntry(APPLICANT_ONE_NAME, "Applicant 1");
    }
}

