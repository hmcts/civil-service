package uk.gov.hmcts.reform.civil.notification.handlers.mediationsuccessfulandunsuccessful.carmdisabled;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.notification.handlers.CamundaProcessIdentifier;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.civil.utils.NotificationUtils;
import uk.gov.hmcts.reform.civil.utils.PartyUtils;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_LEGAL_ORG_NAME_SPEC;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.DEFENDANT_NAME;

@ExtendWith(MockitoExtension.class)
class CarmDisabledAppSolOneEmailDTOGeneratorTest {

    @Mock
    private OrganisationService organisationService;

    @Mock
    private NotificationsProperties notificationsProperties;

    @InjectMocks
    private CarmDisabledAppSolOneEmailDTOGenerator generator;

    private CaseData caseData;

    @BeforeEach
    void setUp() {
        caseData = CaseData.builder()
            .respondent1(Party.builder().type(Party.Type.INDIVIDUAL).individualFirstName("John").individualLastName("Doe").build())
            .build();
    }

    @Test
    void shouldReturnSuccessfulTemplateId_whenTaskIdIsMediationSuccessful() {
        String expectedTemplateId = "template-success-id";
        when(notificationsProperties.getNotifyApplicantLRMediationSuccessfulTemplate()).thenReturn(expectedTemplateId);

        String actual = generator.getEmailTemplateId(caseData, CamundaProcessIdentifier.MediationSuccessfulNotifyParties.toString());

        assertEquals(expectedTemplateId, actual);
    }

    @Test
    void shouldReturnUnsuccessfulTemplateId_whenTaskIdIsNotMediationSuccessful() {
        String expectedTemplateId = "template-unsuccess-id";
        when(notificationsProperties.getMediationUnsuccessfulClaimantLRTemplate()).thenReturn(expectedTemplateId);

        String actual = generator.getEmailTemplateId(caseData, "some-other-task-id");

        assertEquals(expectedTemplateId, actual);
    }

    @Test
    void shouldReturnReferenceTemplate() {
        assertEquals("mediation-update-applicant-notification-%s", generator.getReferenceTemplate());
    }

    @Test
    void shouldAddCustomProperties() {
        Map<String, String> existingProperties = new HashMap<>();

        try (MockedStatic<NotificationUtils> notificationUtilsMockedStatic = mockStatic(NotificationUtils.class);
             MockedStatic<PartyUtils> partyUtilsMockedStatic = mockStatic(PartyUtils.class)) {

            notificationUtilsMockedStatic.when(() ->
                                                   NotificationUtils.getApplicantLegalOrganizationName(eq(caseData), eq(organisationService))
            ).thenReturn("Applicant Org Ltd");

            partyUtilsMockedStatic.when(() ->
                                            PartyUtils.getPartyNameBasedOnType(caseData.getRespondent1())
            ).thenReturn("John Doe");

            Map<String, String> result = generator.addCustomProperties(existingProperties, caseData);

            assertEquals("Applicant Org Ltd", result.get(CLAIM_LEGAL_ORG_NAME_SPEC));
            assertEquals("John Doe", result.get(DEFENDANT_NAME));
        }
    }
}
