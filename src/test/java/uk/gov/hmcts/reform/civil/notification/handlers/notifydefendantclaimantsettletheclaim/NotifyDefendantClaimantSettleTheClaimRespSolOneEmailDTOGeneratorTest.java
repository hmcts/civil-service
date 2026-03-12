package uk.gov.hmcts.reform.civil.notification.handlers.notifydefendantclaimantsettletheclaim;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
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
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_LEGAL_ORG_NAME_SPEC;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIMANT_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.DEFENDANT_REFERENCE_NUMBER;

public class NotifyDefendantClaimantSettleTheClaimRespSolOneEmailDTOGeneratorTest {

    @InjectMocks
    private NotifyDefendantClaimantSettleTheClaimRespSolOneEmailDTOGenerator emailDTOGenerator;

    @Mock
    private NotificationsProperties notificationsProperties;

    @Mock
    private OrganisationService organisationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldReturnCorrectEmailTemplateId() {
        CaseData caseData = CaseData.builder().build();

        String expectedTemplateId = "template-id";
        when(notificationsProperties.getNotifyDefendantLRClaimantSettleTheClaimTemplate()).thenReturn(expectedTemplateId);

        String actualTemplateId = emailDTOGenerator.getEmailTemplateId(caseData);

        assertThat(actualTemplateId).isEqualTo(expectedTemplateId);
    }

    @Test
    void shouldReturnCorrectReferenceTemplate() {
        String referenceTemplate = emailDTOGenerator.getReferenceTemplate();

        assertThat(referenceTemplate).isEqualTo("notify-defendant-lr-claimant-settle-the-claim-notification-%s");
    }

    @Test
    void shouldAddCustomPropertiesWithDefendantReference() {
        String claimantName = "Claimant Company Ltd";
        String defendantReference = "DEF-REF-123";
        Long ccdCaseReference = 1234567890123456L;
        String legalOrgName = "Test Legal Org";

        OrganisationPolicy organisationPolicy = new OrganisationPolicy();
        SolicitorReferences solicitorReferences = new SolicitorReferences();
        solicitorReferences.setRespondentSolicitor1Reference(defendantReference);

        CaseData caseData = CaseData.builder()
            .applicant1(Party.builder().type(Party.Type.COMPANY).companyName(claimantName).build())
            .ccdCaseReference(ccdCaseReference)
            .solicitorReferences(solicitorReferences)
            .respondent1OrganisationPolicy(organisationPolicy)
            .build();

        MockedStatic<NotificationUtils> notificationUtilsMockedStatic = Mockito.mockStatic(NotificationUtils.class);
        notificationUtilsMockedStatic.when(() -> NotificationUtils.getLegalOrganizationNameForRespondent(any(), anyBoolean(), any()))
            .thenReturn(legalOrgName);

        Map<String, String> properties = new HashMap<>();
        Map<String, String> updatedProperties = emailDTOGenerator.addCustomProperties(properties, caseData);

        notificationUtilsMockedStatic.close();

        assertThat(updatedProperties).containsEntry(CLAIM_LEGAL_ORG_NAME_SPEC, legalOrgName);
        assertThat(updatedProperties).containsEntry(CLAIMANT_NAME, claimantName);
        assertThat(updatedProperties).containsEntry(CLAIM_16_DIGIT_NUMBER, ccdCaseReference.toString());
        assertThat(updatedProperties).containsEntry(DEFENDANT_REFERENCE_NUMBER, defendantReference);
    }

    @Test
    void shouldAddCustomPropertiesWithoutDefendantReference() {
        String claimantName = "Claimant Company Ltd";
        Long ccdCaseReference = 1234567890123456L;
        String legalOrgName = "Test Legal Org";

        OrganisationPolicy organisationPolicy = new OrganisationPolicy();

        CaseData caseData = CaseData.builder()
            .applicant1(Party.builder().type(Party.Type.COMPANY).companyName(claimantName).build())
            .ccdCaseReference(ccdCaseReference)
            .respondent1OrganisationPolicy(organisationPolicy)
            .build();

        MockedStatic<NotificationUtils> notificationUtilsMockedStatic = Mockito.mockStatic(NotificationUtils.class);
        notificationUtilsMockedStatic.when(() -> NotificationUtils.getLegalOrganizationNameForRespondent(any(), anyBoolean(), any()))
            .thenReturn(legalOrgName);

        Map<String, String> properties = new HashMap<>();
        Map<String, String> updatedProperties = emailDTOGenerator.addCustomProperties(properties, caseData);

        notificationUtilsMockedStatic.close();

        assertThat(updatedProperties).containsEntry(CLAIM_LEGAL_ORG_NAME_SPEC, legalOrgName);
        assertThat(updatedProperties).containsEntry(CLAIMANT_NAME, claimantName);
        assertThat(updatedProperties).containsEntry(CLAIM_16_DIGIT_NUMBER, ccdCaseReference.toString());
        assertThat(updatedProperties).containsEntry(DEFENDANT_REFERENCE_NUMBER, "Not provided");
    }
}
