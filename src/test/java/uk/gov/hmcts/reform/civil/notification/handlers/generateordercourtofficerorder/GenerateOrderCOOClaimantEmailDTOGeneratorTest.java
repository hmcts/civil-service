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
import static uk.gov.hmcts.reform.civil.notification.handlers.generateordercourtofficerorder.GenerateOrderCOOClaimantEmailDTOGenerator.COO_CLAIMANT_REFERENCE_TEMPLATE;

class GenerateOrderCOOClaimantEmailDTOGeneratorTest {

    @Mock
    private NotificationsProperties notificationsProperties;

    @Mock
    private CaseData caseData;

    @InjectMocks
    GenerateOrderCOOClaimantEmailDTOGenerator emailGenerator;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldReturnBilingualTemplateWhenClaimantIsBilingualAndTaskInfoMatches() {
        // Setup
        String expectedTemplateId = "bilingualTemplate";
        when(caseData.isClaimantBilingual()).thenReturn(true);
        when(notificationsProperties.getNotifyLipUpdateTemplateBilingual()).thenReturn(expectedTemplateId);
        when(notificationsProperties.getOrderBeingTranslatedTemplateWelsh()).thenReturn("welshTranslateTemplate");

        // Set taskInfo for matching task
        emailGenerator.setTaskInfo(GenerateOrderNotifyPartiesCourtOfficerOrder.toString());

        // Verify that the correct template is selected for bilingual claimant
        String actualTemplateId = emailGenerator.getEmailTemplateId(caseData);
        assertThat(actualTemplateId).isEqualTo(expectedTemplateId);
    }

    @Test
    void shouldReturnWelshTranslateTemplateWhenClaimantIsBilingualAndTaskInfoDoesNotMatch() {
        // Setup
        String expectedTemplateId = "welshTranslateTemplate";
        when(caseData.isClaimantBilingual()).thenReturn(true);
        when(notificationsProperties.getNotifyLipUpdateTemplateBilingual()).thenReturn("bilingualTemplate");
        when(notificationsProperties.getOrderBeingTranslatedTemplateWelsh()).thenReturn(expectedTemplateId);

        // Set taskInfo for non-matching task
        emailGenerator.setTaskInfo("SomeOtherTask");

        // Verify that the Welsh template is selected
        String actualTemplateId = emailGenerator.getEmailTemplateId(caseData);
        assertThat(actualTemplateId).isEqualTo(expectedTemplateId);
    }

    @Test
    void shouldReturnDefaultTemplateWhenClaimantIsNotBilingual() {
        // Setup
        String expectedTemplateId = "defaultTemplate";
        when(caseData.isClaimantBilingual()).thenReturn(false);
        when(notificationsProperties.getNotifyLipUpdateTemplate()).thenReturn(expectedTemplateId);

        // Set taskInfo for matching task
        emailGenerator.setTaskInfo(GenerateOrderNotifyPartiesCourtOfficerOrder.toString());

        // Verify that the default template is selected for non-bilingual claimant
        String actualTemplateId = emailGenerator.getEmailTemplateId(caseData);
        assertThat(actualTemplateId).isEqualTo(expectedTemplateId);
    }

    @Test
    void shouldReturnCorrectReferenceTemplate() {
        String referenceTemplate = emailGenerator.getReferenceTemplate();

        assertThat(referenceTemplate).isEqualTo(COO_CLAIMANT_REFERENCE_TEMPLATE);
    }
}
