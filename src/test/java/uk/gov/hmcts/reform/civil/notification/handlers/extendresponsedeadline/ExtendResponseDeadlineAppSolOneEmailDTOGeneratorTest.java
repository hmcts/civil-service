package uk.gov.hmcts.reform.civil.notification.handlers.extendresponsedeadline;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.helpers.DateFormatHelper;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.civil.utils.NotificationUtils;
import uk.gov.hmcts.reform.civil.utils.PartyUtils;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.AGREED_EXTENSION_DATE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_LEGAL_ORG_NAME_SPEC;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.RESPONDENT_NAME;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.formatLocalDate;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.getPartyNameBasedOnType;

@ExtendWith(MockitoExtension.class)
public class ExtendResponseDeadlineAppSolOneEmailDTOGeneratorTest {

    @Mock
    private NotificationsProperties notificationsProperties;

    @Mock
    private OrganisationService organisationService;

    @InjectMocks
    private ExtendResponseDeadlineAppSolOneEmailDTOGenerator emailDTOGenerator;

    @Test
    void shouldReturnCorrectEmailTemplateId() {
        CaseData caseData = CaseData.builder().build();
        String expectedTemplateId = "template-id";
        when(notificationsProperties.getClaimantDeadlineExtension()).thenReturn(expectedTemplateId);

        String actualTemplateId = emailDTOGenerator.getEmailTemplateId(caseData);

        assertThat(actualTemplateId).isEqualTo(expectedTemplateId);
    }

    @Test
    void shouldReturnCorrectReferenceTemplate() {
        String referenceTemplate = emailDTOGenerator.getReferenceTemplate();

        assertThat(referenceTemplate).isEqualTo("claimant-deadline-extension-notification-%s");
    }

    @Test
    void shouldAddCustomProperties() {
        Party party = Party.builder().build();
        String appLegalOrgName = "applicant-legal-org-name";
        LocalDate agreedExtensionDate = LocalDate.of(2025, 6, 20);

        CaseData caseData = CaseData.builder()
            .respondent1(party)
            .respondentSolicitor1AgreedDeadlineExtension(agreedExtensionDate)
            .build();

        String respondentName = "Respondent Name";
        String formattedDate = "20 June 2025";

        try (
            MockedStatic<NotificationUtils> notificationUtilsMockedStatic = Mockito.mockStatic(NotificationUtils.class);
            MockedStatic<PartyUtils> partyUtilsMockedStatic = Mockito.mockStatic(PartyUtils.class);
            MockedStatic<DateFormatHelper> dateFormatHelperMockedStatic = Mockito.mockStatic(DateFormatHelper.class)
        ) {
            notificationUtilsMockedStatic.when(() ->
                                                   NotificationUtils.getApplicantLegalOrganizationName(caseData, organisationService))
                .thenReturn(appLegalOrgName);

            partyUtilsMockedStatic.when(() ->
                                            getPartyNameBasedOnType(party)).thenReturn(respondentName);

            dateFormatHelperMockedStatic.when(() ->
                                                  formatLocalDate(agreedExtensionDate, DATE)).thenReturn(formattedDate);

            Map<String, String> properties = new HashMap<>();
            Map<String, String> updatedProperties = emailDTOGenerator.addCustomProperties(properties, caseData);

            assertThat(updatedProperties.size()).isEqualTo(3);
            assertThat(updatedProperties).containsEntry(RESPONDENT_NAME, respondentName);
            assertThat(updatedProperties).containsEntry(AGREED_EXTENSION_DATE, formattedDate);
            assertThat(updatedProperties).containsEntry(CLAIM_LEGAL_ORG_NAME_SPEC, appLegalOrgName);
        }
    }
}
