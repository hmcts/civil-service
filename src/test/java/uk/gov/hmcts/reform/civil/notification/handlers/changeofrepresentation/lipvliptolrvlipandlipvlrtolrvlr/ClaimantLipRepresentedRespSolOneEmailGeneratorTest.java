package uk.gov.hmcts.reform.civil.notification.handlers.changeofrepresentation.lipvliptolrvlipandlipvlrtolrvlr;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.callback.CallbackException;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.ChangeOfRepresentation;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.prd.model.Organisation;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.civil.utils.NotificationUtils;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CASE_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CCD_REF;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.ISSUE_DATE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.NEW_SOL;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.OTHER_SOL_NAME;

@ExtendWith(MockitoExtension.class)
class ClaimantLipRepresentedRespSolOneEmailGeneratorTest {

    @Mock
    private OrganisationService organisationService;

    @Mock
    private NotificationsProperties notificationsProperties;

    @InjectMocks
    private ClaimantLipRepresentedRespSolOneEmailGenerator generator;

    @Test
    void shouldReturnEmailTemplateId() {
        String expectedTemplateId = "template-id-123";
        when(notificationsProperties.getNoticeOfChangeOtherParties()).thenReturn(expectedTemplateId);

        String actual = generator.getEmailTemplateId(CaseData.builder().build());

        assertEquals(expectedTemplateId, actual);
    }

    @Test
    void shouldAddCustomProperties() {
        CaseData caseData = CaseData.builder()
            .ccdCaseReference(1234567890123456L)
            .issueDate(LocalDate.of(2023, 1, 15))
            .changeOfRepresentation(ChangeOfRepresentation.builder()
                                        .organisationToAddID("org-123")
                                        .build())
            .applicant1(Party.builder()
                            .type(Party.Type.INDIVIDUAL)
                            .individualTitle("Mr")
                            .individualLastName("Doe")
                            .individualFirstName("John")
                            .partyName("John Doe").build())
            .respondent1(Party.builder()
                             .type(Party.Type.INDIVIDUAL)
                             .individualTitle("Mrs")
                             .individualLastName("Dan")
                             .individualFirstName("Jane")
                             .partyName("Jane Dan").build())
            .build();

        Organisation org = Organisation.builder().name("New Org Ltd").build();
        when(organisationService.findOrganisationById("org-123")).thenReturn(Optional.of(org));

        MockedStatic<NotificationUtils> notificationUtilsMockedStatic = Mockito.mockStatic(NotificationUtils.class);
        notificationUtilsMockedStatic.when(() -> NotificationUtils.getLegalOrganizationNameForRespondent(caseData, Boolean.TRUE, organisationService))
            .thenReturn("Resp Org Name");

        Map<String, String> props = generator.addCustomProperties(new HashMap<>(), caseData);

        assertEquals("New Org Ltd", props.get(NEW_SOL));
        assertEquals("1234567890123456", props.get(CCD_REF));
        assertEquals("15 January 2023", props.get(ISSUE_DATE));
        assertTrue(props.containsKey(OTHER_SOL_NAME));
        assertTrue(props.containsKey(CASE_NAME));
    }

    @Test
    void shouldReturnOrganisationName() {
        String orgId = "org-xyz";
        Organisation organisation = Organisation.builder().name("My Org Name").build();
        when(organisationService.findOrganisationById(orgId)).thenReturn(Optional.of(organisation));

        String result = generator.getOrganisationName(orgId);

        assertEquals("My Org Name", result);
    }

    @Test
    void shouldReturnLipWhenOrgIdIsNull() {
        String result = generator.getOrganisationName(null);
        assertEquals("LiP", result);
    }

    @Test
    void shouldThrowExceptionForInvalidOrgId() {
        String orgId = "invalid-org";
        when(organisationService.findOrganisationById(orgId)).thenReturn(Optional.empty());

        CallbackException ex = assertThrows(CallbackException.class, () -> {
            generator.getOrganisationName(orgId);
        });

        assertTrue(ex.getMessage().contains("Invalid organisation ID"));
    }
}
