package uk.gov.hmcts.reform.civil.notification.handlers.mediationsuccessfulandunsuccessful.carmdisabled;

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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIMANT_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.RESPONDENT_NAME;
import static uk.gov.hmcts.reform.civil.notification.handlers.CamundaProcessIdentifier.MediationSuccessfulNotifyParties;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CarmDisabledClaimantEmailDTOGeneratorTest {

    @Mock
    private NotificationsProperties notificationsProperties;

    @InjectMocks
    private CarmDisabledClaimantEmailDTOGenerator generator;

    private CaseData caseData;

    @BeforeEach
    void setUp() {
        caseData = CaseData.builder()
            .legacyCaseReference("000LR001")
            .applicant1(Party.builder().individualFirstName("Alice").individualLastName("Smith").type(Party.Type.INDIVIDUAL).build())
            .respondent1(Party.builder().individualFirstName("Bob").individualLastName("Jones").type(Party.Type.INDIVIDUAL).build())
            .build();
    }

    @Test
    void shouldReturnMediationSuccessfulTemplate_whenTaskIdMatches_andClaimantNotBilingual() {
        when(notificationsProperties.getNotifyApplicantLiPMediationSuccessfulTemplate())
            .thenReturn("template-success-eng");

        String result = generator.getEmailTemplateId(caseData, MediationSuccessfulNotifyParties.toString());

        assertEquals("template-success-eng", result);
    }

    @Test
    void shouldReturnMediationSuccessfulWelshTemplate_whenTaskIdMatches_andClaimantIsBilingual() {
       CaseData updatedData = caseData.toBuilder()
            .claimantBilingualLanguagePreference(Language.WELSH.getDisplayedValue()).build();
        when(notificationsProperties.getNotifyApplicantLiPMediationSuccessfulWelshTemplate())
            .thenReturn("template-success-welsh");

        String result = generator.getEmailTemplateId(updatedData, MediationSuccessfulNotifyParties.toString());

        assertEquals("template-success-welsh", result);
    }

    @Test
    void shouldReturnMediationUnsuccessfulTemplate_whenTaskIdDoesNotMatch_andClaimantNotBilingual() {
        when(notificationsProperties.getMediationUnsuccessfulClaimantLIPTemplate())
            .thenReturn("template-failure-eng");

        String result = generator.getEmailTemplateId(caseData, "other-task");

        assertEquals("template-failure-eng", result);
    }

    @Test
    void shouldReturnMediationUnsuccessfulWelshTemplate_whenTaskIdDoesNotMatch_andClaimantIsBilingual() {
        CaseData updatedData = caseData.toBuilder()
            .claimantBilingualLanguagePreference(Language.WELSH.getDisplayedValue()).build();
        when(notificationsProperties.getMediationUnsuccessfulClaimantLIPWelshTemplate())
            .thenReturn("template-failure-welsh");

        String result = generator.getEmailTemplateId(updatedData, "other-task");

        assertEquals("template-failure-welsh", result);
    }

    @Test
    void shouldReturnReferenceTemplate() {
        assertEquals("mediation-update-claimant-notification-LIP-%s", generator.getReferenceTemplate());
    }

    @Test
    void shouldAddCustomPropertiesToMap() {
        Map<String, String> props = new HashMap<>();

        Map<String, String> result = generator.addCustomProperties(props, caseData);

        assertEquals("Alice Smith", result.get(CLAIMANT_NAME));
        assertEquals("Bob Jones", result.get(RESPONDENT_NAME));
        assertEquals("000LR001", result.get(CLAIM_REFERENCE_NUMBER));
    }
}
