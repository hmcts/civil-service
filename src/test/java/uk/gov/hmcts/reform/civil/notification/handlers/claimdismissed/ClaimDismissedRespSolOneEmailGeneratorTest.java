package uk.gov.hmcts.reform.civil.notification.handlers.claimdismissed;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.notification.handlers.claimdismissed.ClaimDismissedRespSolOneEmailDTOGenerator.REFERENCE_TEMPLATE_RESPONDENT_FOR_CLAIM_DISMISSED;

class ClaimDismissedRespSolOneEmailGeneratorTest {

    @Mock
    private ClaimDismissedEmailTemplater claimDismissedEmailTemplater;

    @InjectMocks
    private ClaimDismissedRespSolOneEmailDTOGenerator emailGenerator;

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

        assertThat(referenceTemplate).isEqualTo(REFERENCE_TEMPLATE_RESPONDENT_FOR_CLAIM_DISMISSED);
    }

    @Test
    void shouldReturnCorrectShouldNotify() {
        CaseData caseData = CaseData.builder()
            .respondent1Represented(YesOrNo.YES)
            .claimDismissedDate(LocalDateTime.now())
            .build();

        Boolean shouldNotify = emailGenerator.getShouldNotify(caseData);

        assertThat(shouldNotify).isTrue();
    }

    @Test
    void shouldNotNotifyForLip() {
        CaseData caseData = CaseData.builder()
            .respondent1Represented(YesOrNo.NO)
            .claimDismissedDate(LocalDateTime.now())
            .build();

        Boolean shouldNotify = emailGenerator.getShouldNotify(caseData);

        assertThat(shouldNotify).isFalse();
    }

    @Test
    void shouldNotifyForRespondentSolcitorWhenClaimDismissedDateGiven() {
        CaseData caseData = CaseData.builder()
            .respondent1Represented(YesOrNo.YES)
            .claimDismissedDate(LocalDateTime.now())
            .build();

        Boolean shouldNotify = emailGenerator.getShouldNotify(caseData);

        assertThat(shouldNotify).isTrue();
    }

    @Test
    void shouldNotNotifyForRespondentSolcitorWhenClaimDismissedDateNotGiven() {
        CaseData caseData = CaseData.builder()
            .respondent1Represented(YesOrNo.YES)
            .claimDismissedDate(null)
            .build();

        Boolean shouldNotify = emailGenerator.getShouldNotify(caseData);

        assertThat(shouldNotify).isFalse();
    }
}
