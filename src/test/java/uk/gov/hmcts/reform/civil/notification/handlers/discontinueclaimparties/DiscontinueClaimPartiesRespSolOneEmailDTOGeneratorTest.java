package uk.gov.hmcts.reform.civil.notification.handlers.discontinueclaimparties;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.civil.utils.NotificationUtils;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.LEGAL_ORG_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.PARTY_REFERENCES;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CASEMAN_REF;

class DiscontinueClaimPartiesRespSolOneEmailDTOGeneratorTest {

    private static final String TEMPLATE_ID = "template-id";
    private static final String REFERENCE_NUMBER = "8372942374";
    protected static final String APPLICANT_LEGAL_ORG_NAME = "Test Legal Org";

    @Mock
    private NotificationsProperties notificationsProperties;

    @Mock
    private OrganisationService organisationService;

    private DiscontinueClaimPartiesRespSolOneEmailDTOGenerator generator;

    private MockedStatic<NotificationUtils> notificationUtilsMockedStatic;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        generator = new DiscontinueClaimPartiesRespSolOneEmailDTOGenerator(notificationsProperties, organisationService);
        notificationUtilsMockedStatic = mockStatic(NotificationUtils.class);
    }

    @AfterEach
    void tearDown() {
        if (notificationUtilsMockedStatic != null) {
            notificationUtilsMockedStatic.close();
        }
    }

    @Test
    void shouldReturnEmailTemplateId() {
        when(notificationsProperties.getNotifyClaimDiscontinuedLRTemplate()).thenReturn(TEMPLATE_ID);

        CaseData caseData = CaseData.builder().build();
        String result = generator.getEmailTemplateId(caseData);

        assertThat(result).isEqualTo(TEMPLATE_ID);
    }

    @Test
    void shouldReturnReferenceTemplate() {
        String result = generator.getReferenceTemplate();

        assertThat(result).isEqualTo("defendant-claim-discontinued-%s");
    }

    @Test
    void shouldAddCustomProperties() {
        Map<String, String> properties = new HashMap<>();
        CaseData caseData = mock(CaseData.class);

        notificationUtilsMockedStatic.when(() -> NotificationUtils.getRespondentLegalOrganizationName(caseData.getRespondent1OrganisationPolicy(), organisationService))
            .thenReturn(APPLICANT_LEGAL_ORG_NAME);

        when(caseData.getLegacyCaseReference()).thenReturn(REFERENCE_NUMBER);
        when(caseData.getCcdCaseReference()).thenReturn(12345L);

        Map<String, String> result = generator.addCustomProperties(properties, caseData);

        assertThat(result.get(LEGAL_ORG_NAME)).isEqualTo(APPLICANT_LEGAL_ORG_NAME);
        assertThat(result.get(CLAIM_REFERENCE_NUMBER)).isEqualTo("12345");
        assertThat(result.get(PARTY_REFERENCES)).isEqualTo(NotificationUtils.buildPartiesReferencesEmailSubject(caseData));
        assertThat(result.get(CASEMAN_REF)).isEqualTo(REFERENCE_NUMBER);
    }

    @Test
    void shouldReturnRespondentSolicitorEmailAddressWhenShouldNotifyIsTrue() {
        CaseData caseData = CaseData.builder()
            .respondentSolicitor1EmailAddress("solicitor@example.com")
            .respondent1Represented(YesOrNo.YES)
            .build();

        String result = generator.getEmailAddress(caseData);

        assertThat(result).isEqualTo("solicitor@example.com");
    }

    @Test
    void shouldReturnRespondentPartyEmailWhenShouldNotifyIsFalse() {
        Party respondent1 = mock(Party.class);
        when(respondent1.getPartyEmail()).thenReturn("party@example.com");

        CaseData caseData = CaseData.builder()
            .respondent1(respondent1)
            .respondent1Represented(YesOrNo.NO)
            .build();

        String result = generator.getEmailAddress(caseData);

        assertThat(result).isEqualTo("party@example.com");
    }

    @Test
    void shouldReturnLipEmailTemplateIdWhenShouldNotifyIsFalse() {
        CaseData caseData = CaseData.builder()
            .respondent1Represented(YesOrNo.NO)
            .build();

        when(notificationsProperties.getNotifyClaimDiscontinuedLipTemplate()).thenReturn(TEMPLATE_ID);

        String result = generator.getEmailTemplateId(caseData);

        assertThat(result).isEqualTo(TEMPLATE_ID);
    }

    @Test
    void shouldReturnLRTemplateIdWhenShouldNotifyIsTrue() {
        CaseData caseData = CaseData.builder()
            .respondent1Represented(YesOrNo.YES)
            .build();

        when(notificationsProperties.getNotifyClaimDiscontinuedLRTemplate()).thenReturn(TEMPLATE_ID);

        String result = generator.getEmailTemplateId(caseData);

        assertThat(result).isEqualTo(TEMPLATE_ID);
    }
}
