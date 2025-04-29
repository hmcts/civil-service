package uk.gov.hmcts.reform.civil.notification.handlers.generateordercourtofficerorder;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.notification.handlers.CamundaProcessIdentifier.GenerateOrderNotifyPartiesCourtOfficerOrder;

class GenerateOrderCOOClaimantEmailDTOGeneratorTest {

    private static final String TEMPLATE_ID = "template-id";
    private static final String TEMPLATE_ID_BILINGUAL = "template-id-bilingual";
    private static final String TEMPLATE_ID_WELSH_TRANSLATION = "template-id-welsh-translation";

    private GenerateOrderCOOClaimantEmailDTOGenerator generator;

    @BeforeEach
    void setUp() {
        NotificationsProperties notificationsProperties = mock(NotificationsProperties.class);
        generator = new GenerateOrderCOOClaimantEmailDTOGenerator(notificationsProperties);

        when(notificationsProperties.getNotifyLipUpdateTemplate()).thenReturn(TEMPLATE_ID);
        when(notificationsProperties.getNotifyLipUpdateTemplateBilingual()).thenReturn(TEMPLATE_ID_BILINGUAL);
        when(notificationsProperties.getOrderBeingTranslatedTemplateWelsh()).thenReturn(TEMPLATE_ID_WELSH_TRANSLATION);
    }

    @Test
    void shouldReturnNotifyLipUpdateTemplate_whenGetEmailTemplateId_withoutTaskId() {
        CaseData caseData = CaseData.builder().build();

        String result = generator.getEmailTemplateId(caseData);

        assertThat(result).isEqualTo(TEMPLATE_ID);
    }

    @Test
    void shouldReturnNotifyLipUpdateTemplate_whenGetEmailTemplateId_withTaskId_andNonBilingualCase() {
        CaseData caseData = mock(CaseData.class);
        when(caseData.isClaimantBilingual()).thenReturn(false);

        String result = generator.getEmailTemplateId(caseData, "someTaskId");

        assertThat(result).isEqualTo(TEMPLATE_ID);
    }

    @Test
    void shouldReturnBilingualTemplate_whenGetEmailTemplateId_withBilingualCase_andCorrectTaskId() {
        CaseData caseData = mock(CaseData.class);
        when(caseData.isClaimantBilingual()).thenReturn(true);

        String result = generator.getEmailTemplateId(caseData, GenerateOrderNotifyPartiesCourtOfficerOrder.toString());

        assertThat(result).isEqualTo(TEMPLATE_ID_BILINGUAL);
    }

    @Test
    void shouldReturnWelshTranslationTemplate_whenGetEmailTemplateId_withBilingualCase_andIncorrectTaskId() {
        CaseData caseData = mock(CaseData.class);
        when(caseData.isClaimantBilingual()).thenReturn(true);

        String result = generator.getEmailTemplateId(caseData, "differentTaskId");

        assertThat(result).isEqualTo(TEMPLATE_ID_WELSH_TRANSLATION);
    }

    @Test
    void shouldReturnReferenceTemplate() {
        String result = generator.getReferenceTemplate();

        assertThat(result).isEqualTo("generate-order-notification-%s");
    }
}
