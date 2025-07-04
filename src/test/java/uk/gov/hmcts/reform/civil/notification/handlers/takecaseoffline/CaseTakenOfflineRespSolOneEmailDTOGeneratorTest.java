package uk.gov.hmcts.reform.civil.notification.handlers.takecaseoffline;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CaseTakenOfflineRespSolOneEmailDTOGeneratorTest {

    public static final String TEMPLATE_ID = "template-id";
    public static final String CASE_TAKEN_OFFLINE_RESPONDENT_NOTIFICATION = "case-taken-offline-respondent-notification-%s";

    @Mock
    private NotificationsProperties notificationsProperties;

    @InjectMocks
    private CaseTakenOfflineRespSolOneEmailDTOGenerator generator;

    @Test
    void shouldReturnCorrectEmailTemplateId() {
        CaseData caseData = CaseData.builder().build();

        when(notificationsProperties.getSolicitorCaseTakenOffline()).thenReturn(TEMPLATE_ID);

        String templateId = generator.getEmailTemplateId(caseData);

        assertThat(templateId).isEqualTo(TEMPLATE_ID);
    }

    @Test
    void shouldReturnCorrectReferenceTemplate() {
        String referenceTemplate = generator.getReferenceTemplate();

        assertThat(referenceTemplate).isEqualTo(CASE_TAKEN_OFFLINE_RESPONDENT_NOTIFICATION);
    }
}