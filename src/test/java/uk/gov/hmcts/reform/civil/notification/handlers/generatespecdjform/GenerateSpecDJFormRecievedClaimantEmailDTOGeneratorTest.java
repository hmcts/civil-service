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
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_NUMBER;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.DEFENDANT_NAME;

@ExtendWith(MockitoExtension.class)
class GenerateSpecDJFormRecievedClaimantEmailDTOGeneratorTest {

    private static final String EN_TEMPLATE = "english-template";
    private static final String CY_TEMPLATE = "welsh-template";
    private static final String REFERENCE_TEMPLATE = "default-judgment-applicant-received-notification-%s";

    @Mock
    private NotificationsProperties notificationsProperties;

    private GenerateSpecDJFormRecievedClaimantEmailDTOGenerator generator;
    private MockedStatic<PartyUtils> partyUtilsMockedStatic;

    @BeforeEach
    void setUp() {
        generator = new GenerateSpecDJFormRecievedClaimantEmailDTOGenerator(notificationsProperties);
        partyUtilsMockedStatic = mockStatic(PartyUtils.class);
    }

    @AfterEach
    void tearDown() {
        if (partyUtilsMockedStatic != null) {
            partyUtilsMockedStatic.close();
        }
    }

    @Test
    void shouldReturnEnglishTemplateWhenNotBilingual() {
        CaseData caseData = mock(CaseData.class);
        when(caseData.isClaimantBilingual()).thenReturn(false);
        when(notificationsProperties.getApplicantLiPDefaultJudgmentRequested()).thenReturn(EN_TEMPLATE);

        assertThat(generator.getEmailTemplateId(caseData)).isEqualTo(EN_TEMPLATE);
    }

    @Test
    void shouldReturnWelshTemplateWhenBilingual() {
        CaseData caseData = mock(CaseData.class);
        when(caseData.isClaimantBilingual()).thenReturn(true);
        when(notificationsProperties.getApplicantLiPDefaultJudgmentRequestedBilingualTemplate()).thenReturn(CY_TEMPLATE);

        assertThat(generator.getEmailTemplateId(caseData)).isEqualTo(CY_TEMPLATE);
    }

    @Test
    void shouldReturnReferenceTemplate() {
        assertThat(generator.getReferenceTemplate()).isEqualTo(REFERENCE_TEMPLATE);
    }

    @Test
    void shouldAddCustomProperties() {
        Map<String, String> properties = new HashMap<>();
        CaseData caseData = mock(CaseData.class);
        Party applicant1 = mock(Party.class);
        Party respondent1 = mock(Party.class);

        when(caseData.getApplicant1()).thenReturn(applicant1);
        when(caseData.getRespondent1()).thenReturn(respondent1);
        when(caseData.getLegacyCaseReference()).thenReturn("000DC001");
        partyUtilsMockedStatic.when(() -> PartyUtils.getPartyNameBasedOnType(applicant1)).thenReturn("Applicant 1");
        partyUtilsMockedStatic.when(() -> PartyUtils.getPartyNameBasedOnType(respondent1)).thenReturn("Defendant 1");
        partyUtilsMockedStatic.when(() -> PartyUtils.getAllPartyNames(caseData)).thenReturn("Applicant v Defendant");

        Map<String, String> result = generator.addCustomProperties(properties, caseData);

        assertThat(result).containsEntry(APPLICANT_ONE_NAME, "Applicant 1");
        assertThat(result).containsEntry(CLAIM_NUMBER, "000DC001");
        assertThat(result).containsEntry(DEFENDANT_NAME, "Defendant 1");
    }
}

