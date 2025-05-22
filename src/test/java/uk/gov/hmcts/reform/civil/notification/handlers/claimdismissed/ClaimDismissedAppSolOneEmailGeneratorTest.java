package uk.gov.hmcts.reform.civil.notification.handlers.claimdismissed;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.civil.model.CaseData;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.notification.handlers.claimdismissed.ClaimDismissedAppSolOneEmailDTOGenerator.CLAIM_DISMISSED_APPLICANT_NOTIFICATION_REFERENCE_TEMPLATE;

class ClaimDismissedAppSolOneEmailGeneratorTest {

    @Mock
    private ClaimDismissedEmailTemplater claimDismissedEmailTemplater;

    @InjectMocks
    private ClaimDismissedAppSolOneEmailDTOGenerator emailGenerator;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldReturnCorrectEmailTemplateId() {
        CaseData caseData = CaseData.builder().build();
        String expectedTemplateId = "template-id";
        when(claimDismissedEmailTemplater.getTemplateId(caseData)).thenReturn(expectedTemplateId);

        String actualTemplateId = emailGenerator.getEmailTemplateId(caseData);

        assertThat(actualTemplateId).isEqualTo(expectedTemplateId);
    }

    @Test
    void shouldReturnCorrectReferenceTemplate() {
        String referenceTemplate = emailGenerator.getReferenceTemplate();

        assertThat(referenceTemplate).isEqualTo(CLAIM_DISMISSED_APPLICANT_NOTIFICATION_REFERENCE_TEMPLATE);
    }

}
