package uk.gov.hmcts.reform.civil.notification.handlers.mediationsuccessfulandunsuccessful.carmdisabled;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.civil.utils.NotificationUtils;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_LEGAL_ORG_NAME_SPEC;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIMANT_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIMANT_NAME_ONE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIMANT_NAME_TWO;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.PARTY_NAME;
import static uk.gov.hmcts.reform.civil.notification.handlers.CamundaProcessIdentifier.MediationSuccessfulNotifyParties;

@ExtendWith(MockitoExtension.class)
class CarmDisabledRespSolOneEmailDTOGeneratorTest {

    private static final String RESPONDENT_LEGAL_ORG_NAME = "Respondent Legal Org";

    @Mock
    private NotificationsProperties notificationsProperties;

    @Mock
    private OrganisationService organisationService;

    @InjectMocks
    private CarmDisabledRespSolOneEmailDTOGenerator generator;

    private CaseData caseData;
    private CaseData lipVLrCaseData;
    private CaseData twoVOneCaseData;

    @BeforeEach
    void setUp() {
        caseData = CaseData.builder()
            .applicant1(new Party()
                            .setType(Party.Type.INDIVIDUAL)
                            .setIndividualFirstName("Alice")
                            .setIndividualLastName("Smith")
                            .setIndividualTitle("Mrs"))
            .respondent1Represented(YesOrNo.YES)
            .applicant1Represented(YesOrNo.YES)
            .build();

        lipVLrCaseData = CaseData.builder()
            .applicant1(new Party()
                            .setType(Party.Type.INDIVIDUAL)
                            .setIndividualFirstName("Alice")
                            .setIndividualLastName("Smith")
                            .setIndividualTitle("Mrs"))
            .respondent1Represented(YesOrNo.YES)
            .applicant1Represented(YesOrNo.NO)
            .build();

        twoVOneCaseData = CaseData.builder()
            .applicant1(new Party()
                            .setType(Party.Type.INDIVIDUAL)
                            .setIndividualFirstName("Alice")
                            .setIndividualLastName("Smith")
                            .setIndividualTitle("Mrs"))
            .applicant2(new Party()
                            .setType(Party.Type.INDIVIDUAL)
                            .setIndividualFirstName("Bob")
                            .setIndividualLastName("Jones")
                            .setIndividualTitle("Mr"))
            .respondent1Represented(YesOrNo.YES)
            .applicant1Represented(YesOrNo.YES)
            .build();
    }

    @Test
    void shouldReturnLipVLrSuccessfulTemplate_whenLipVLrCase() {
        when(notificationsProperties.getNotifyLrDefendantSuccessfulMediationForLipVLrClaim())
            .thenReturn("lip-v-lr-success-template");

        String templateId = generator.getEmailTemplateId(lipVLrCaseData, MediationSuccessfulNotifyParties.toString());

        assertThat(templateId).isEqualTo("lip-v-lr-success-template");
    }

    @Test
    void shouldReturnTwoVOneSuccessfulTemplate_whenTwoVOneCase() {
        when(notificationsProperties.getNotifyTwoVOneDefendantSuccessfulMediation())
            .thenReturn("two-v-one-success-template");

        String templateId = generator.getEmailTemplateId(twoVOneCaseData, MediationSuccessfulNotifyParties.toString());

        assertThat(templateId).isEqualTo("two-v-one-success-template");
    }

    @Test
    void shouldReturnGenericLrSuccessfulTemplate_whenLrVLrCase() {
        when(notificationsProperties.getNotifyLrDefendantSuccessfulMediation())
            .thenReturn("lr-success-template");

        String templateId = generator.getEmailTemplateId(caseData, MediationSuccessfulNotifyParties.toString());

        assertThat(templateId).isEqualTo("lr-success-template");
    }

    @Test
    void shouldReturnLipVLrUnsuccessfulTemplate_whenLipVLrCase() {
        when(notificationsProperties.getMediationUnsuccessfulLRTemplateForLipVLr())
            .thenReturn("lip-v-lr-unsuccessful-template");

        String templateId = generator.getEmailTemplateId(lipVLrCaseData, "other-task");

        assertThat(templateId).isEqualTo("lip-v-lr-unsuccessful-template");
    }

    @Test
    void shouldReturnGenericUnsuccessfulTemplate_whenLrVLrCase() {
        when(notificationsProperties.getMediationUnsuccessfulLRTemplate())
            .thenReturn("lr-unsuccessful-template");

        String templateId = generator.getEmailTemplateId(caseData, "other-task");

        assertThat(templateId).isEqualTo("lr-unsuccessful-template");
    }

    @Test
    void shouldReturnGenericUnsuccessfulTemplate_whenTaskIdIsNull() {
        when(notificationsProperties.getMediationUnsuccessfulLRTemplate())
            .thenReturn("lr-unsuccessful-template");

        String templateId = generator.getEmailTemplateId(caseData);

        assertThat(templateId).isEqualTo("lr-unsuccessful-template");
    }

    @Test
    void shouldReturnReferenceTemplate() {
        String reference = generator.getReferenceTemplate();

        assertThat(reference).isEqualTo("mediation-update-defendant-notification-%s");
    }

    @Test
    void shouldReturnTrueForShouldNotify_whenRespondentIsRepresented() {
        CaseData caseDataWithFlag = CaseData.builder()
            .respondent1Represented(YesOrNo.YES)
            .applicant1Represented(YesOrNo.YES)
            .build();

        boolean shouldNotify = generator.getShouldNotify(caseDataWithFlag);

        assertThat(shouldNotify).isTrue();
    }

    @Test
    void shouldReturnTrueForShouldNotify_whenLipvLROneVOne() {
        CaseData caseDataWithFlag = CaseData.builder()
            .respondent1Represented(YesOrNo.YES)
            .applicant1Represented(YesOrNo.NO)
            .build();

        boolean shouldNotify = generator.getShouldNotify(caseDataWithFlag);

        assertThat(shouldNotify).isTrue();
    }

    @Test
    void shouldReturnFalseForShouldNotify_whenRespondentIsNotRepresented() {
        CaseData caseDataWithFlag = CaseData.builder()
            .respondent1Represented(YesOrNo.NO)
            .build();

        boolean shouldNotify = generator.getShouldNotify(caseDataWithFlag);

        assertThat(shouldNotify).isFalse();
    }

    @Test
    void shouldAddCustomPropertiesToMap_forStandardCase() {
        Map<String, String> properties = new HashMap<>();

        try (MockedStatic<NotificationUtils> notificationUtilsMockedStatic = Mockito.mockStatic(NotificationUtils.class)) {
            notificationUtilsMockedStatic.when(() -> NotificationUtils.getLegalOrganizationNameForRespondent(caseData, Boolean.TRUE, organisationService))
                .thenReturn(RESPONDENT_LEGAL_ORG_NAME);

            Map<String, String> result = generator.addCustomProperties(properties, caseData);

            assertThat(result)
                .containsEntry(CLAIM_LEGAL_ORG_NAME_SPEC, RESPONDENT_LEGAL_ORG_NAME)
                .containsEntry(CLAIMANT_NAME, "Mrs Alice Smith")
                .containsEntry(PARTY_NAME, "Mrs Alice Smith's claim against you");
        }
    }

    @Test
    void shouldAddCustomPropertiesToMap_forTwoVOneCase() {
        Map<String, String> properties = new HashMap<>();

        try (MockedStatic<NotificationUtils> notificationUtilsMockedStatic = Mockito.mockStatic(NotificationUtils.class)) {
            notificationUtilsMockedStatic.when(() -> NotificationUtils.getLegalOrganizationNameForRespondent(twoVOneCaseData, Boolean.TRUE, organisationService))
                .thenReturn(RESPONDENT_LEGAL_ORG_NAME);

            Map<String, String> result = generator.addCustomProperties(properties, twoVOneCaseData);

            assertThat(result)
                .containsEntry(CLAIM_LEGAL_ORG_NAME_SPEC, RESPONDENT_LEGAL_ORG_NAME)
                .containsEntry(CLAIMANT_NAME_ONE, "Mrs Alice Smith")
                .containsEntry(CLAIMANT_NAME_TWO, "Mr Bob Jones")
                .containsEntry(PARTY_NAME, "Mrs Alice Smith and Mr Bob Jones's claim against you");
        }
    }
}
