package uk.gov.hmcts.reform.civil.notification.handlers.acknowledgeclaimunspec;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_LEGAL_ORG_NAME_SPEC;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.RESPONDENT_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.RESPONSE_DEADLINE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.RESPONSE_INTENTION;
import static uk.gov.hmcts.reform.civil.notification.handlers.acknowledgeclaimunspec.AcknowledgeClaimUnspecRespSolTwoEmailDTOGenerator.REF_SOL_TWO_REF_TEMPLATE;

class AcknowledgeClaimUnspecRespSolTwoEmailDTOGeneratorTest {

    @Mock
    private NotificationsProperties notificationsProperties;

    @Mock
    private AcknowledgeClaimUnspecHelper acknowledgeClaimUnspecHelper;

    @InjectMocks
    AcknowledgeClaimUnspecRespSolTwoEmailDTOGenerator emailGenerator;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldReturnCorrectEmailTemplateId_whenAppSolGetTemplateIsInvoked() {
        CaseData caseData = CaseData.builder().build();
        String expectedTemplateId = "template-id";
        when(notificationsProperties.getRespondentSolicitorAcknowledgeClaim()).thenReturn(expectedTemplateId);

        String actualTemplateId = emailGenerator.getEmailTemplateId(caseData);

        assertThat(actualTemplateId).isEqualTo(expectedTemplateId);
    }

    @Test
    void shouldReturnCorrectReferenceTemplate() {
        String referenceTemplate = emailGenerator.getReferenceTemplate();

        assertThat(referenceTemplate).isEqualTo(REF_SOL_TWO_REF_TEMPLATE);
    }

    @Test
    void shouldReturnAppSolTemplateProperties_whenCustomPropsCalled() {
        CaseData caseData = CaseData.builder().build();
        Map<String, String> properties = new HashMap<>();
        Map<String, String> addedProperties = Map.of(
                CLAIM_LEGAL_ORG_NAME_SPEC, "Test1Org",
                RESPONDENT_NAME, "Resp2 name",
                RESPONSE_DEADLINE, "2028-01-01",
                RESPONSE_INTENTION, "Response intention"
        );
        when(acknowledgeClaimUnspecHelper.addTemplateProperties(properties, caseData))
                .thenReturn(new HashMap<>(addedProperties));
        properties = emailGenerator.addCustomProperties(properties, caseData);
        assertThat(properties)
            .containsEntry(CLAIM_LEGAL_ORG_NAME_SPEC, "Test1Org")
            .containsEntry(RESPONDENT_NAME, "Resp2 name")
            .containsEntry(RESPONSE_DEADLINE, "2028-01-01")
            .containsEntry(RESPONSE_INTENTION, "Response intention");
    }

    @Test
    void shouldNotifyReturnFalse_whenResp2IsNotAcknowledged() {
        CaseData caseData = CaseData.builder().build();
        when(acknowledgeClaimUnspecHelper.isRespondentOneAcknowledged(caseData)).thenReturn(true);
        assertThat(emailGenerator.getShouldNotify(caseData)).isFalse();
    }

    @Test
    void shouldNotifyReturnTrue_whenResp1NotAcknowledgedAndResp2NotLiP() {
        CaseData caseData = CaseData.builder()
            .respondent2Represented(YesOrNo.NO)
            .build();
        when(acknowledgeClaimUnspecHelper.isRespondentOneAcknowledged(caseData)).thenReturn(false);

        assertThat(emailGenerator.getShouldNotify(caseData)).isTrue();
    }

    @Test
    void shouldNotifyReturnFalse_whenResp1AcknowledgedAndResp2NotLiP() {
        CaseData caseData = CaseData.builder()
            .respondent2Represented(YesOrNo.NO)
            .build();
        when(acknowledgeClaimUnspecHelper.isRespondentOneAcknowledged(caseData)).thenReturn(true);

        assertThat(emailGenerator.getShouldNotify(caseData)).isFalse();
    }

    @Test
    void shouldNotifyReturnFalse_whenResp1NotAcknowledgedAndResp2IsLiP() {
        CaseData caseData = CaseData.builder()
            .respondent2Represented(YesOrNo.YES)
            .build();
        when(acknowledgeClaimUnspecHelper.isRespondentOneAcknowledged(caseData)).thenReturn(false);

        assertThat(emailGenerator.getShouldNotify(caseData)).isFalse();
    }

    @Test
    void shouldNotifyReturnFalse_whenResp1AcknowledgedAndResp2IsLiP() {
        CaseData caseData = CaseData.builder()
            .respondent2Represented(YesOrNo.YES)
            .build();
        when(acknowledgeClaimUnspecHelper.isRespondentOneAcknowledged(caseData)).thenReturn(true);

        assertThat(emailGenerator.getShouldNotify(caseData)).isFalse();
    }
}
