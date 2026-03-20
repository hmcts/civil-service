package uk.gov.hmcts.reform.civil.notification.handlers.informagreedextensiondatespec;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.helpers.DateFormatHelper;
import uk.gov.hmcts.reform.civil.model.CaseData;
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
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.DEFENDANT_NAME;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE;

@ExtendWith(MockitoExtension.class)
class InformAgreedExtensionDateSpecAppSolOneEmailDTOGeneratorTest {

    @Mock
    private NotificationsProperties notificationsProperties;

    @Mock
    private OrganisationService organisationService;

    @InjectMocks
    private InformAgreedExtensionDateSpecAppSolOneEmailDTOGenerator generator;

    @Test
    void shouldReturnCorrectTemplateId() {
        CaseData caseData = CaseData.builder().build();
        String templateId = "spec-template";
        when(notificationsProperties.getClaimantSolicitorAgreedExtensionDateForSpec()).thenReturn(templateId);

        assertThat(generator.getEmailTemplateId(caseData)).isEqualTo(templateId);
    }

    @Test
    void shouldReturnReferenceTemplate() {
        assertThat(generator.getReferenceTemplate()).isEqualTo("agreed-extension-date-applicant-notification-spec-%s");
    }

    @Test
    void shouldAddCustomProperties() {
        LocalDate extensionDate = LocalDate.of(2025, 3, 15);
        CaseData caseData = CaseData.builder()
            .respondentSolicitor1AgreedDeadlineExtension(extensionDate)
            .build();

        try (MockedStatic<NotificationUtils> notificationUtils = Mockito.mockStatic(NotificationUtils.class);
             MockedStatic<PartyUtils> partyUtils = Mockito.mockStatic(PartyUtils.class);
             MockedStatic<DateFormatHelper> dateFormatHelper = Mockito.mockStatic(DateFormatHelper.class)) {

            notificationUtils.when(() -> NotificationUtils.getApplicantLegalOrganizationName(caseData, organisationService))
                .thenReturn("Applicant Org");
            partyUtils.when(() -> PartyUtils.fetchDefendantName(caseData)).thenReturn("Defendant");
            dateFormatHelper.when(() -> DateFormatHelper.formatLocalDate(extensionDate, DATE))
                .thenReturn("15 March 2025");

            Map<String, String> properties = new HashMap<>();
            Map<String, String> updatedProperties = generator.addCustomProperties(properties, caseData);

            assertThat(updatedProperties)
                .containsEntry(CLAIM_LEGAL_ORG_NAME_SPEC, "Applicant Org")
                .containsEntry(AGREED_EXTENSION_DATE, "15 March 2025")
                .containsEntry(DEFENDANT_NAME, "Defendant");
        }
    }
}
