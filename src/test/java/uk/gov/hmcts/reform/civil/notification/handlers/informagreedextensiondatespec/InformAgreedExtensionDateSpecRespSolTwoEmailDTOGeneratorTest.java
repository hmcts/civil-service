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

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.AGREED_EXTENSION_DATE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_LEGAL_ORG_NAME_SPEC;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE;

@ExtendWith(MockitoExtension.class)
class InformAgreedExtensionDateSpecRespSolTwoEmailDTOGeneratorTest {

    @Mock
    private NotificationsProperties notificationsProperties;

    @Mock
    private OrganisationService organisationService;

    @InjectMocks
    private InformAgreedExtensionDateSpecRespSolTwoEmailDTOGenerator generator;

    @Test
    void shouldReturnCorrectTemplateId() {
        CaseData caseData = CaseData.builder().build();
        String templateId = "respondent2-spec-template";
        when(notificationsProperties.getRespondentSolicitorAgreedExtensionDateForSpec()).thenReturn(templateId);

        assertThat(generator.getEmailTemplateId(caseData)).isEqualTo(templateId);
    }

    @Test
    void shouldReturnReferenceTemplate() {
        assertThat(generator.getReferenceTemplate()).isEqualTo("agreed-extension-date-applicant-notification-spec-%s");
    }

    @Test
    void shouldAddCustomProperties() {
        LocalDate extensionDate = LocalDate.of(2025, 11, 11);
        CaseData caseData = CaseData.builder()
            .respondentSolicitor1AgreedDeadlineExtension(extensionDate)
            .build();

        try (MockedStatic<NotificationUtils> notificationUtils = Mockito.mockStatic(NotificationUtils.class);
             MockedStatic<DateFormatHelper> dateFormatHelper = Mockito.mockStatic(DateFormatHelper.class)) {

            notificationUtils.when(() -> NotificationUtils.getLegalOrganizationNameForRespondent(caseData, false, organisationService))
                .thenReturn("Respondent Two Org");
            dateFormatHelper.when(() -> DateFormatHelper.formatLocalDate(extensionDate, DATE))
                .thenReturn("11 November 2025");

            Map<String, String> properties = new HashMap<>();
            Map<String, String> updatedProperties = generator.addCustomProperties(properties, caseData);

            assertThat(updatedProperties)
                .containsEntry(CLAIM_LEGAL_ORG_NAME_SPEC, "Respondent Two Org")
                .containsEntry(AGREED_EXTENSION_DATE, "11 November 2025");
        }
    }
}
