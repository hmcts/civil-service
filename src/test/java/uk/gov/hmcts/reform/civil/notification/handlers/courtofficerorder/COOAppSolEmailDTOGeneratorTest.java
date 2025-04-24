package uk.gov.hmcts.reform.civil.notification.handlers.courtofficerorder;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.notification.handlers.courtofficerorder.COOAppSolEmailDTOGenerator.COO_APP_SOL_REFERENCE_TEMPLATE;

class COOAppSolEmailDTOGeneratorTest {

    @Mock
    private NotificationsProperties notificationsProperties;

    @InjectMocks
    COOAppSolEmailDTOGenerator emailGenerator;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldReturnCorrectEmailTemplateId_whenAppSolGetTemplateIsInvoked() {
        CaseData caseData = CaseData.builder().build();
        String expectedTemplateId = "template-id";
        when(notificationsProperties.getGenerateOrderNotificationTemplate()).thenReturn(expectedTemplateId);

        String actualTemplateId = emailGenerator.getEmailTemplateId(caseData);

        assertThat(actualTemplateId).isEqualTo(expectedTemplateId);
    }

    @Test
    void shouldReturnCorrectReferenceTemplate() {
        String referenceTemplate = emailGenerator.getReferenceTemplate();

        assertThat(referenceTemplate).isEqualTo(COO_APP_SOL_REFERENCE_TEMPLATE);
    }
}
