package uk.gov.hmcts.reform.civil.notification.handlers.djnondivergent;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.civil.utils.NotificationUtils;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIMANT_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.DEFENDANT_NAME_INTERIM;

@ExtendWith(MockitoExtension.class)
class DjNonDivergentDefendant1LREmailDTOGeneratorTest {

    @Mock
    private NotificationsProperties notificationsProperties;

    @Mock
    private OrganisationService organisationService;

    @InjectMocks
    private DjNonDivergentDefendant1LREmailDTOGenerator emailDTOGenerator;

    @Test
    void shouldReturnCorrectEmailTemplateId() {
        CaseData caseData = CaseData.builder().build();
        String expectedTemplateId = "dj-non-divergent-template";
        when(notificationsProperties.getNotifyDJNonDivergentSpecDefendantTemplate()).thenReturn(expectedTemplateId);

        String actualTemplateId = emailDTOGenerator.getEmailTemplateId(caseData);

        assertThat(actualTemplateId).isEqualTo(expectedTemplateId);
    }

    @Test
    void shouldReturnCorrectReferenceTemplate() {
        String referenceTemplate = emailDTOGenerator.getReferenceTemplate();

        assertThat(referenceTemplate).isEqualTo("dj-non-divergent-defendant-notification-%s");
    }

    // getShouldNotify() is inherited from RespSolOneEmailDTOGenerator
    // and tested in its own test class

    @Test
    void shouldAddCustomProperties() {
        Party applicant = Party.builder()
            .individualFirstName("Claimant")
            .individualLastName("Name")
            .type(Party.Type.INDIVIDUAL)
            .build();
        CaseData caseData = CaseData.builder()
            .applicant1(applicant)
            .build();

        String defendantName = "Defendant Name";
        try (MockedStatic<NotificationUtils> notificationUtilsMockedStatic = Mockito.mockStatic(NotificationUtils.class)) {
            notificationUtilsMockedStatic.when(() -> NotificationUtils.getDefendantNameBasedOnCaseType(any())).thenReturn(defendantName);

            Map<String, String> properties = new HashMap<>();
            Map<String, String> updatedProperties = emailDTOGenerator.addCustomProperties(properties, caseData);

            assertThat(updatedProperties).containsEntry(DEFENDANT_NAME_INTERIM, defendantName);
            assertThat(updatedProperties).containsEntry(CLAIMANT_NAME, "Claimant Name");
        }
    }
}
