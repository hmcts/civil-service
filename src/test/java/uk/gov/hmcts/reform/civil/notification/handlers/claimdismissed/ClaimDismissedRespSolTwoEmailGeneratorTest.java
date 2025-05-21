package uk.gov.hmcts.reform.civil.notification.handlers.claimdismissed;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.notification.handlers.claimdismissed.ClaimDismissedRespSolOneEmailDTOGenerator.REFERENCE_TEMPLATE_RESPONDENT_FOR_CLAIM_DISMISSED;

class ClaimDismissedRespSolTwoEmailGeneratorTest {

    @Mock
    private ClaimDismissedEmailTemplater claimDismissedEmailTemplater;

    @InjectMocks
    private ClaimDismissedRespSolTwoEmailDTOGenerator emailGenerator;

    private MockedStatic<MultiPartyScenario> multiPartyScenarioMock;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        multiPartyScenarioMock = org.mockito.Mockito.mockStatic(MultiPartyScenario.class);
    }

    @AfterEach
    void tearDown() {
        multiPartyScenarioMock.close();
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
    void shouldNotifyWhen1V2NoTheSameLegalRepAndClaimDismissedDateIsGiven() {
        CaseData caseData = CaseData.builder()
            .claimDismissedDate(LocalDateTime.now())
            .build();
        multiPartyScenarioMock.when(() -> MultiPartyScenario.isOneVTwoTwoLegalRep(caseData)).thenReturn(true);

        boolean shouldNotify = emailGenerator.getShouldNotify(caseData);

        assertThat(shouldNotify).isTrue();
    }

    @Test
    void shouldNotNotifyWhen1V2AndSameLegalRep() {
        CaseData caseData = CaseData.builder()
            .claimDismissedDate(LocalDateTime.now())
            .build();
        multiPartyScenarioMock.when(() -> MultiPartyScenario.isOneVTwoTwoLegalRep(caseData)).thenReturn(false);

        boolean shouldNotify = emailGenerator.getShouldNotify(caseData);

        assertThat(shouldNotify).isFalse();
    }

    @Test
    void shouldNotNotifyWhen1V2AndSameLegalRepAndClaimDismissedDateIsNotGiven() {
        CaseData caseData = CaseData.builder()
            .claimDismissedDate(null)
            .build();
        multiPartyScenarioMock.when(() -> MultiPartyScenario.isOneVTwoTwoLegalRep(caseData)).thenReturn(true);

        boolean shouldNotify = emailGenerator.getShouldNotify(caseData);

        assertThat(shouldNotify).isFalse();
    }
}
