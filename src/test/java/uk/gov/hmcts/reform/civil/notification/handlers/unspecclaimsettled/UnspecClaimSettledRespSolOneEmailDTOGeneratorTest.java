package uk.gov.hmcts.reform.civil.notification.handlers.unspecclaimsettled;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.SolicitorReferences;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.civil.utils.NotificationUtils;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_16_DIGIT_NUMBER;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIMANT_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.DEFENDANT_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.LEGAL_REP_NAME;

class UnspecClaimSettledRespSolOneEmailDTOGeneratorTest {

    private static final String SOLICITOR_EMAIL = "solicitor@test.com";

    @InjectMocks
    private UnspecClaimSettledRespSolOneEmailDTOGenerator emailDTOGenerator;

    @Mock
    private NotificationsProperties notificationsProperties;

    @Mock
    private OrganisationService organisationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldReturnUnspecTemplateId() {
        when(notificationsProperties.getNotifyDefendantLRClaimantSettleTheClaimUnspecTemplate()).thenReturn("template-id");

        assertThat(emailDTOGenerator.getEmailTemplateId(CaseData.builder().build())).isEqualTo("template-id");
    }

    @Test
    void shouldReturnCorrectReferenceTemplate() {
        assertThat(emailDTOGenerator.getReferenceTemplate())
            .isEqualTo("notify-defendant-lr-unspec-claim-settled-notification-%s");
    }

    @Test
    void shouldNotify_whenOneVOneAndDefendantRepresented() {
        CaseData caseData = CaseData.builder()
            .respondent1Represented(YesOrNo.YES)
            .respondentSolicitor1EmailAddress(SOLICITOR_EMAIL)
            .build();

        assertThat(emailDTOGenerator.getShouldNotify(caseData)).isTrue();
    }

    @Test
    void shouldNotNotify_whenDefendantIsLip() {
        CaseData caseData = CaseData.builder()
            .respondent1Represented(YesOrNo.NO)
            .respondentSolicitor1EmailAddress(SOLICITOR_EMAIL)
            .build();

        assertThat(emailDTOGenerator.getShouldNotify(caseData)).isFalse();
    }

    @Test
    void shouldNotNotify_whenSolicitorEmailIsMissing() {
        CaseData caseData = CaseData.builder()
            .respondent1Represented(YesOrNo.YES)
            .build();

        assertThat(emailDTOGenerator.getShouldNotify(caseData)).isFalse();
    }

    @Test
    void shouldNotNotify_whenMultiParty() {
        CaseData caseData = CaseData.builder()
            .respondent1Represented(YesOrNo.YES)
            .respondentSolicitor1EmailAddress(SOLICITOR_EMAIL)
            .respondent2(new Party().setType(Party.Type.COMPANY).setCompanyName("Defendant Two"))
            .build();

        assertThat(emailDTOGenerator.getShouldNotify(caseData)).isFalse();
    }

    @Test
    void shouldAddCustomProperties() {
        CaseData caseData = CaseData.builder()
            .applicant1(new Party().setType(Party.Type.COMPANY).setCompanyName("Claimant Company Ltd"))
            .ccdCaseReference(1234567890123456L)
            .solicitorReferences(new SolicitorReferences().setRespondentSolicitor1Reference("DEF-REF-123"))
            .respondent1OrganisationPolicy(new OrganisationPolicy())
            .build();

        Map<String, String> properties;
        try (MockedStatic<NotificationUtils> notificationUtils = Mockito.mockStatic(NotificationUtils.class)) {
            notificationUtils.when(() -> NotificationUtils.getLegalOrganizationNameForRespondent(any(), anyBoolean(), any()))
                .thenReturn("Test Legal Org");
            notificationUtils.when(() -> NotificationUtils.getRespondentLegalOrganizationName(any(), any()))
                .thenReturn("Test Legal Org");
            properties = emailDTOGenerator.addCustomProperties(new HashMap<>(), caseData);
        }

        assertThat(properties)
            .containsEntry(CLAIMANT_NAME, "Claimant Company Ltd")
            .containsEntry(CLAIM_16_DIGIT_NUMBER, "1234567890123456")
            .containsEntry(DEFENDANT_REFERENCE_NUMBER, "DEF-REF-123")
            .containsEntry(LEGAL_REP_NAME, "Test Legal Org");
    }

    @Test
    void shouldDefaultDefendantReference_whenNotProvided() {
        CaseData caseData = CaseData.builder()
            .applicant1(new Party().setType(Party.Type.COMPANY).setCompanyName("Claimant Company Ltd"))
            .ccdCaseReference(1234567890123456L)
            .respondent1OrganisationPolicy(new OrganisationPolicy())
            .build();

        Map<String, String> properties;
        try (MockedStatic<NotificationUtils> notificationUtils = Mockito.mockStatic(NotificationUtils.class)) {
            notificationUtils.when(() -> NotificationUtils.getLegalOrganizationNameForRespondent(any(), anyBoolean(), any()))
                .thenReturn("Test Legal Org");
            notificationUtils.when(() -> NotificationUtils.getRespondentLegalOrganizationName(any(), any()))
                .thenReturn("Test Legal Org");
            properties = emailDTOGenerator.addCustomProperties(new HashMap<>(), caseData);
        }

        assertThat(properties).containsEntry(DEFENDANT_REFERENCE_NUMBER, "Not provided");
    }
}
