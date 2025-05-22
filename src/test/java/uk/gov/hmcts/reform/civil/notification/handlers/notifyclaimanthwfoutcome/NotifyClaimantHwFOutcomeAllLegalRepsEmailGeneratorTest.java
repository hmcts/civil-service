package uk.gov.hmcts.reform.civil.notification.handlers.notifyclaimanthwfoutcome;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.citizenui.HelpWithFeesDetails;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NO_REMISSION_HWF;

class NotifyClaimantHwFOutcomeAllLegalRepsEmailGeneratorTest {

    private NotifyClaimantHwFOutcomeAllLegalRepsEmailGenerator generator;

    @BeforeEach
    void setUp() {
        NotifyClaimantHwFOutcomeAppSolOneEmailDTOGenerator appSolOneEmailGenerator = mock(NotifyClaimantHwFOutcomeAppSolOneEmailDTOGenerator.class);
        NotifyClaimantHwFOutcomeRespSolOneEmailDTOGenerator respSolOneEmailGenerator = mock(NotifyClaimantHwFOutcomeRespSolOneEmailDTOGenerator.class);
        NotifyClaimantHwFOutcomeRespSolTwoEmailDTOGenerator respSolTwoEmailGenerator = mock(NotifyClaimantHwFOutcomeRespSolTwoEmailDTOGenerator.class);

        generator = new NotifyClaimantHwFOutcomeAllLegalRepsEmailGenerator(
            appSolOneEmailGenerator,
            respSolOneEmailGenerator,
            respSolTwoEmailGenerator
        );
    }

    @Test
    void shouldNotifyRespondentsWhenHwFEventIsNotNull() {
        HelpWithFeesDetails hwfDetails = HelpWithFeesDetails.builder()
            .hwfCaseEvent(NO_REMISSION_HWF)
            .build();
        CaseData caseData = CaseData.builder()
            .claimIssuedHwfDetails(hwfDetails)
            .build();

        boolean result = generator.shouldNotifyRespondents(caseData);

        assertThat(result).isTrue();
    }

    @Test
    void shouldNotNotifyRespondentsWhenHwFEventIsNull() {
        HelpWithFeesDetails hwfDetails = HelpWithFeesDetails.builder()
            .hwfCaseEvent(null)
            .build();
        CaseData caseData = CaseData.builder()
            .claimIssuedHwfDetails(hwfDetails)
            .build();

        boolean result = generator.shouldNotifyRespondents(caseData);

        assertThat(result).isFalse();
    }

    @Test
    void shouldNotNotifyRespondentsWhenClaimIssuedHwfDetailsIsNull() {
        CaseData caseData = CaseData.builder()
            .claimIssuedHwfDetails(null)
            .build();

        boolean result = generator.shouldNotifyRespondents(caseData);

        assertThat(result).isFalse();
    }
}
