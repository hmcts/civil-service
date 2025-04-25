package uk.gov.hmcts.reform.civil.notification.handlers.generateordercourtofficerorder;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.notification.handlers.CamundaProcessIdentifier.GenerateOrderNotifyPartiesCourtOfficerOrder;
import static uk.gov.hmcts.reform.civil.notification.handlers.generateordercourtofficerorder.GenerateOrderCOODefendantEmailDTOGenerator.COO_DEFENDANT_REFERENCE_TEMPLATE;

class GenerateOrderCOODefendantEmailDTOGeneratorTest {

    @Mock
    private NotificationsProperties notificationsProperties;

    @Mock
    private CaseData caseData;

    @InjectMocks
    GenerateOrderCOODefendantEmailDTOGenerator emailGenerator;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldReturnBilingualTemplateWhenRespondentIsBilingualAndTaskInfoMatches() {
        // Setup the caseData and mock behavior
        when(caseData.isRespondentResponseBilingual()).thenReturn(true);

        String expectedTemplateId = "bilingualTemplateId";
        when(notificationsProperties.getNotifyLipUpdateTemplateBilingual()).thenReturn(expectedTemplateId);

        // Set taskInfo for matching task
        emailGenerator.setTaskInfo(GenerateOrderNotifyPartiesCourtOfficerOrder.toString());

        // Verify the template returned is the bilingual one
        String actualTemplateId = emailGenerator.getEmailTemplateId(caseData);
        assertThat(actualTemplateId).isEqualTo(expectedTemplateId);
    }

    @Test
    void shouldReturnWelshTranslateTemplateWhenRespondentIsBilingualAndTaskInfoDoesNotMatch() {
        // Setup the caseData and mock behavior
        when(caseData.isRespondentResponseBilingual()).thenReturn(true);

        String expectedTemplateId = "welshTranslateTemplateId";
        when(notificationsProperties.getOrderBeingTranslatedTemplateWelsh()).thenReturn(expectedTemplateId);

        // Set taskInfo for a non-matching task
        emailGenerator.setTaskInfo("SomeOtherTask");

        // Verify the Welsh template is selected
        String actualTemplateId = emailGenerator.getEmailTemplateId(caseData);
        assertThat(actualTemplateId).isEqualTo(expectedTemplateId);
    }

    @Test
    void shouldReturnDefaultTemplateWhenRespondentIsNotBilingual() {
        // Setup the caseData and mock behavior
        when(caseData.isRespondentResponseBilingual()).thenReturn(false);

        String expectedTemplateId = "defaultTemplateId";
        when(notificationsProperties.getNotifyLipUpdateTemplate()).thenReturn(expectedTemplateId);

        // Set taskInfo for matching task
        emailGenerator.setTaskInfo(GenerateOrderNotifyPartiesCourtOfficerOrder.toString());

        // Verify the default template is selected
        String actualTemplateId = emailGenerator.getEmailTemplateId(caseData);
        assertThat(actualTemplateId).isEqualTo(expectedTemplateId);
    }

    @Test
    void shouldReturnCorrectReferenceTemplate() {
        String referenceTemplate = emailGenerator.getReferenceTemplate();

        assertThat(referenceTemplate).isEqualTo(COO_DEFENDANT_REFERENCE_TEMPLATE);
    }
}
