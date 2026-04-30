package uk.gov.hmcts.reform.civil.notification.handlers.notifylipgenerictemplate;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotifyLipGenericTemplateClaimantEmailDTOGeneratorTest {

    private static final String TEMPLATE_ID = "template-id";
    private static final String TEMPLATE_ID_BILINGUAL = "template-id-bilingual";
    private static final String REFERENCE_TEMPLATE = "generic-notification-lip-%s";

    @Mock
    private NotificationsProperties notificationsProperties;

    @InjectMocks
    private NotifyLipGenericTemplateClaimantEmailDTOGenerator generator;

    @Test
    void shouldReturnDefaultTemplateIdWhenClaimantNotBilingual() {
        CaseData caseData = mock(CaseData.class);
        when(caseData.isClaimantBilingual()).thenReturn(false);
        when(notificationsProperties.getNotifyLipUpdateTemplate()).thenReturn(TEMPLATE_ID);

        assertThat(generator.getEmailTemplateId(caseData)).isEqualTo(TEMPLATE_ID);
    }

    @Test
    void shouldReturnBilingualTemplateIdWhenClaimantBilingual() {
        CaseData caseData = mock(CaseData.class);
        when(caseData.isClaimantBilingual()).thenReturn(true);
        when(notificationsProperties.getNotifyLipUpdateTemplateBilingual()).thenReturn(TEMPLATE_ID_BILINGUAL);

        assertThat(generator.getEmailTemplateId(caseData)).isEqualTo(TEMPLATE_ID_BILINGUAL);
    }

    @Test
    void shouldReturnCorrectReferenceTemplate() {
        assertThat(generator.getReferenceTemplate()).isEqualTo(REFERENCE_TEMPLATE);
    }
}
