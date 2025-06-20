package uk.gov.hmcts.reform.civil.notification.handlers.claimantresponsecui.rejectrepayment;

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
class ClaimantResponseCuiRejectPayRespSol1EmailDTOGeneratorTest {

    private static final String REFERENCE_TEMPLATE = "claimant-reject-repayment-respondent-notification-%s";

    @Mock
    private NotificationsProperties notificationsProperties;

    @InjectMocks
    private ClaimantResponseCuiRejectPayRespSol1EmailDTOGenerator emailGenerator;

    @Test
    void shouldReturnCorrectEmailTemplateId_whenClaimantGetTemplateIsInvoked() {
        CaseData caseData = CaseData.builder().build();
        String expectedTemplateId = "template-id";
        when(notificationsProperties.getNotifyDefendantLrTemplate()).thenReturn(expectedTemplateId);

        String actualTemplateId = emailGenerator.getEmailTemplateId(caseData);

        assertThat(actualTemplateId).isEqualTo(expectedTemplateId);
    }

    @Test
    void shouldReturnCorrectReferenceTemplate() {
        String referenceTemplate = emailGenerator.getReferenceTemplate();

        assertThat(referenceTemplate).isEqualTo(REFERENCE_TEMPLATE);
    }

}
