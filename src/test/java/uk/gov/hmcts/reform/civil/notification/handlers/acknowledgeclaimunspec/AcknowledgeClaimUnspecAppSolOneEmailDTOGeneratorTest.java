package uk.gov.hmcts.reform.civil.notification.handlers.acknowledgeclaimunspec;

import org.mockito.InjectMocks;
import org.mockito.Mock;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.utils.NotificationUtils;
import uk.gov.hmcts.reform.civil.utils.PartyUtils;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_LEGAL_ORG_NAME_SPEC;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.RESPONDENT_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.RESPONSE_DEADLINE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.RESPONSE_INTENTION;
import static uk.gov.hmcts.reform.civil.notification.handlers.acknowledgeclaimunspec.AcknowledgeClaimUnspecAppSolOneEmailDTOGenerator.APP_SOL_REF_TEMPLATE;

class AcknowledgeClaimUnspecAppSolOneEmailDTOGeneratorTest {

    @Mock
    private NotificationsProperties notificationsProperties;

    @Mock
    private AcknowledgeClaimUnspecHelper acknowledgeClaimUnspecHelper;

    @InjectMocks
    AcknowledgeClaimUnspecAppSolOneEmailDTOGenerator emailGenerator;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldReturnCorrectEmailTemplateId_whenAppSolGetTemplateIsInvoked() {
        CaseData caseData = CaseData.builder().build();
        String expectedTemplateId = "template-id";
        when(notificationsProperties.getRespondentSolicitorAcknowledgeClaim()).thenReturn(expectedTemplateId);

        String actualTemplateId = emailGenerator.getEmailTemplateId(caseData);

        assertThat(actualTemplateId).isEqualTo(expectedTemplateId);
    }

    @Test
    void shouldReturnCorrectReferenceTemplate() {
        String referenceTemplate = emailGenerator.getReferenceTemplate();

        assertThat(referenceTemplate).isEqualTo(APP_SOL_REF_TEMPLATE);
    }

    @Test
    void shouldReturnAppSolTemplateProperties_whenCustomPropsCalled() {
        CaseData caseData = CaseData.builder().build();
        Map<String, String> properties = new HashMap<>();
        Map<String, String> addedProperties = Map.of(
            CLAIM_LEGAL_ORG_NAME_SPEC, "TestOrg",
            RESPONDENT_NAME, "Resp1 name",
            RESPONSE_DEADLINE, "2029-01-01"
        );
        when(acknowledgeClaimUnspecHelper.addTemplateProperties(properties, caseData))
            .thenReturn(new HashMap<>(addedProperties));
        try (MockedStatic<NotificationUtils> notificationUtilsMock = mockStatic(NotificationUtils.class);
             MockedStatic<PartyUtils> partyUtilsMock = mockStatic(PartyUtils.class)) {

            notificationUtilsMock.when(() ->
                                           NotificationUtils.getApplicantLegalOrganizationName(any(), any()))
                .thenReturn("Claim org");
            partyUtilsMock.when(() ->
                                    PartyUtils.getResponseIntentionForEmail(caseData))
                .thenReturn("Response intention");
            properties = emailGenerator.addCustomProperties(properties, caseData);
            assertThat(properties)
                .containsEntry(CLAIM_LEGAL_ORG_NAME_SPEC, "Claim org")
                .containsEntry(RESPONDENT_NAME, "Resp1 name")
                .containsEntry(RESPONSE_DEADLINE, "2029-01-01")
                .containsEntry(RESPONSE_INTENTION, "Response intention");
        }
    }
}
