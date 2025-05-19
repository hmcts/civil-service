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

    @BeforeEach
    void setUp() {
        caseData = CaseData.builder()
            .applicant1(Party.builder()
                            .individualFirstName("Alice")
                            .individualLastName("Smith")
                            .build())
            .build();
    }

    @Test
    void shouldReturnSuccessfulTemplate_whenTaskIdMatches() {
        when(notificationsProperties.getNotifyLrDefendantSuccessfulMediationForLipVLrClaim())
            .thenReturn("success-template");

        String templateId = generator.getEmailTemplateId(caseData, MediationSuccessfulNotifyParties.toString());

        assertThat(templateId).isEqualTo("success-template");
    }

    @Test
    void shouldReturnUnsuccessfulTemplate_whenTaskIdDoesNotMatch() {
        when(notificationsProperties.getMediationUnsuccessfulLRTemplateForLipVLr())
            .thenReturn("unsuccessful-template");

        String templateId = generator.getEmailTemplateId(caseData, "other-task");

        assertThat(templateId).isEqualTo("unsuccessful-template");
    }

    @Test
    void shouldReturnUnsuccessfulTemplate_whenTaskIdIsNull() {
        when(notificationsProperties.getMediationUnsuccessfulLRTemplateForLipVLr())
            .thenReturn("unsuccessful-template");

        String templateId = generator.getEmailTemplateId(caseData);

        assertThat(templateId).isEqualTo("unsuccessful-template");
    }

    @Test
    void shouldReturnReferenceTemplate() {
        String reference = generator.getReferenceTemplate();

        assertThat(reference).isEqualTo("mediation-update-defendant-notification-LIP-%s");
    }

    @Test
    void shouldReturnTrueForShouldNotify_whenLipvLROneVOneIsTrue() {
        CaseData caseDataWithFlag = CaseData.builder()
            .respondent1Represented(YesOrNo.YES)
            .applicant1Represented(YesOrNo.NO)
            .build();

        boolean shouldNotify = generator.getShouldNotify(caseDataWithFlag);

        assertThat(shouldNotify).isTrue();
    }

    @Test
    void shouldReturnFalseForShouldNotify_whenLipvLROneVOneIsFalse() {
        CaseData caseDataWithFlag = CaseData.builder()
            .respondent1Represented(YesOrNo.NO)
            .build();

        boolean shouldNotify = generator.getShouldNotify(caseDataWithFlag);

        assertThat(shouldNotify).isFalse();
    }

    @Test
    void shouldAddCustomPropertiesToMap() {
        Map<String, String> properties = new HashMap<>();

        try (MockedStatic<NotificationUtils> notificationUtilsMockedStatic = Mockito.mockStatic(NotificationUtils.class)) {
            notificationUtilsMockedStatic.when(() -> NotificationUtils.getLegalOrganizationNameForRespondent(caseData, Boolean.TRUE, organisationService))
                .thenReturn(RESPONDENT_LEGAL_ORG_NAME);

            Map<String, String> result = generator.addCustomProperties(properties, caseData);

            assertThat(result)
                .containsEntry(CLAIM_LEGAL_ORG_NAME_SPEC, RESPONDENT_LEGAL_ORG_NAME)
                .containsEntry(CLAIMANT_NAME, "Alice Smith");
        }
    }
}
