package uk.gov.hmcts.reform.civil.notification.handlers.changeofrepresentation.lipvliptolipvlrorlrvliptolrvlr;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.callback.CallbackException;
import uk.gov.hmcts.reform.civil.enums.CaseRole;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.ChangeOfRepresentation;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.prd.model.Organisation;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CASE_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CCD_REF;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.ISSUE_DATE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.NEW_SOL;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.OTHER_SOL_NAME;

@ExtendWith(MockitoExtension.class)
class DefRepresentedApplicantSolEmailDTOGeneratorTest {

    @Mock
    private OrganisationService organisationService;

    @Mock
    private NotificationsProperties notificationsProperties;

    @InjectMocks
    private DefRepresentedApplicantSolEmailDTOGenerator generator;

    @Test
    void shouldReturnCorrectEmailTemplateId() {
        when(notificationsProperties.getNoticeOfChangeOtherParties()).thenReturn("template-id-123");

        String result = generator.getEmailTemplateId(CaseData.builder().build());

        assertThat(result).isEqualTo("template-id-123");
    }

    @Test
    void shouldReturnReferenceTemplate() {
        assertThat(generator.getReferenceTemplate())
            .isEqualTo("notify-claimant-lr-after-defendant-noc-approval-%s");
    }

    @Test
    void shouldAddCustomProperties() {
        String orgId = "org123";
        String newSolName = "New Sol Org";

        Organisation organisation = mock(Organisation.class);
        when(organisation.getName()).thenReturn(newSolName);
        when(organisationService.findOrganisationById(orgId)).thenReturn(Optional.of(organisation));

        CaseData caseData = CaseData.builder()
            .ccdCaseReference(1234567890L)
            .applicant1(Party.builder()
                            .type(Party.Type.INDIVIDUAL)
                            .individualTitle("Mr")
                            .individualFirstName("John")
                            .individualLastName("Claimant")
                            .partyName("John Claimant").build())
            .respondent1(Party.builder()
                             .type(Party.Type.INDIVIDUAL)
                             .individualTitle("Mrs")
                             .individualFirstName("Jane")
                             .individualLastName("Defendant")
                             .partyName("Jane Defendant").build())
            .issueDate(LocalDate.of(2023, 12, 1))
            .changeOfRepresentation(ChangeOfRepresentation.builder()
                                        .caseRole(CaseRole.RESPONDENTSOLICITORONE.getFormattedName())
                                        .organisationToAddID(orgId).build())
            .build();

        Map<String, String> result = generator.addCustomProperties(new HashMap<>(), caseData);

        assertThat(result).containsEntry(CASE_NAME, "Mr John Claimant v Mrs Jane Defendant");
        assertThat(result).containsEntry(ISSUE_DATE, "1 December 2023");
        assertThat(result).containsEntry(NEW_SOL, "New Sol Org");
        assertThat(result).containsEntry(CCD_REF, "1234567890");
        assertThat(result).containsEntry(OTHER_SOL_NAME, "LiP");
    }

    @Test
    void shouldThrowExceptionWhenOrgIdInvalid() {
        String invalidOrgId = "bad-org";
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
