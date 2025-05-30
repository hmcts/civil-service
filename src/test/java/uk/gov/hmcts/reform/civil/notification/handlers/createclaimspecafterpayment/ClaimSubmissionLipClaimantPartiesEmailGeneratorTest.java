package uk.gov.hmcts.reform.civil.notification.handlers.createclaimspecafterpayment;

import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.notification.handlers.AllPartiesEmailGenerator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClaimSubmissionLipClaimantPartiesEmailGeneratorTest {

    public static final String TEMPLATE_ID = "template-id";
    @Mock
    private ClaimSubmissionLipClaimantEmailDTOGenerator claimantGenerator;

    @InjectMocks
    private ClaimSubmissionLipClaimantPartiesEmailGenerator emailGenerator;

    @Test
    void shouldExtendAllPartiesEmailGenerator() {
        AssertionsForClassTypes.assertThat(emailGenerator).isInstanceOf(AllPartiesEmailGenerator.class);
    }

    @Test
    void shouldGenerateEmailForClaimant() {
        CaseData caseData = CaseData.builder()
                .applicant1(Party.builder()
                        .individualFirstName("John")
                        .individualLastName("Doe")
                        .partyEmail("john.doe@example.com")
                        .build())
                .build();
        when(claimantGenerator.getEmailTemplateId(caseData)).thenReturn(TEMPLATE_ID);

        String templateId = claimantGenerator.getEmailTemplateId(caseData);

        assertThat(templateId).isEqualTo(TEMPLATE_ID);
    }
}