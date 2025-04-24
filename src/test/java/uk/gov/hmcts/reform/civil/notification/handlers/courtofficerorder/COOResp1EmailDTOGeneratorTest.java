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
import static uk.gov.hmcts.reform.civil.notification.handlers.courtofficerorder.COOResp1EmailDTOGenerator.COO_RESP_SOL_ONE_REFERENCE_TEMPLATE;

class COOResp1EmailDTOGeneratorTest {

    @Mock
    private NotificationsProperties notificationsProperties;

    @InjectMocks
    COOResp1EmailDTOGenerator emailGenerator;

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

        assertThat(referenceTemplate).isEqualTo(COO_RESP_SOL_ONE_REFERENCE_TEMPLATE);
    }
}
