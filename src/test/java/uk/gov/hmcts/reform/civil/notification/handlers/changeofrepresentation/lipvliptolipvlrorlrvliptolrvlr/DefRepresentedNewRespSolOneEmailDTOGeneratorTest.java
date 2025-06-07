package uk.gov.hmcts.reform.civil.notification.handlers.changeofrepresentation.lipvliptolipvlrorlrvliptolrvlr;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.callback.CallbackException;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.ChangeOfRepresentation;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.prd.model.Organisation;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_16_DIGIT_NUMBER;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.DEFENDANT_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.LEGAL_REP_NAME;

@ExtendWith(MockitoExtension.class)
class DefRepresentedNewRespSolOneEmailDTOGeneratorTest {

    @Mock
    private OrganisationService organisationService;

    @Mock
    private NotificationsProperties notificationsProperties;

    @InjectMocks
    private DefRepresentedNewRespSolOneEmailDTOGenerator generator;

    @Test
    void shouldReturnCorrectEmailTemplateId() {
        when(notificationsProperties.getNotifyDefendantLrAfterNoticeOfChangeTemplate())
            .thenReturn("template-123");

        String templateId = generator.getEmailTemplateId(CaseData.builder().build());

        assertThat(templateId).isEqualTo("template-123");
    }

    @Test
    void shouldReturnCorrectReferenceTemplate() {
        assertThat(generator.getReferenceTemplate())
            .isEqualTo("notify-lr-after-defendant-noc-approval-%s");
    }

    @Test
    void shouldAddCustomPropertiesCorrectly() {
        String orgId = "org123";
        Organisation organisation = mock(Organisation.class);
        when(organisation.getName()).thenReturn("New Legal Rep Org");
        when(organisationService.findOrganisationById(orgId)).thenReturn(Optional.of(organisation));

        CaseData caseData = CaseData.builder()
            .ccdCaseReference(1234567890123456L)
            .respondent1(Party.builder()
                             .type(Party.Type.INDIVIDUAL)
                             .individualTitle("Mrs")
                             .individualFirstName("Jane")
                             .individualLastName("Defendant")
                             .partyName("Jane Defendant").build())
            .changeOfRepresentation(ChangeOfRepresentation.builder().organisationToAddID(orgId).build())
            .build();

        Map<String, String> result = generator.addCustomProperties(new HashMap<>(), caseData);

        assertThat(result)
            .containsEntry(DEFENDANT_NAME, "Mrs Jane Defendant")
            .containsEntry(CLAIM_16_DIGIT_NUMBER, "1234567890123456")
            .containsEntry(LEGAL_REP_NAME, "New Legal Rep Org");
    }

    @Test
    void shouldThrowExceptionWhenOrganisationNotFound() {
        String invalidOrgId = "invalid-org";
        when(organisationService.findOrganisationById(invalidOrgId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> generator.getOrganisationName(invalidOrgId))
            .isInstanceOf(CallbackException.class)
            .hasMessageContaining("Invalid organisation ID: " + invalidOrgId);
    }

    @Test
    void shouldReturnLipIfOrganisationIdIsNull() {
        String result = generator.getOrganisationName(null);
        assertThat(result).isEqualTo("LiP");
    }
}
