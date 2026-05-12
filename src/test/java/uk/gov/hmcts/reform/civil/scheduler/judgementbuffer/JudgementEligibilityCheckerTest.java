package uk.gov.hmcts.reform.civil.scheduler.judgementbuffer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.Time;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class JudgementEligibilityCheckerTest {

    @Mock
    private Time time;

    @InjectMocks
    private JudgementEligibilityChecker judgementEligibilityChecker;

    private final LocalDateTime now = LocalDateTime.now();

    @BeforeEach
    void setup() {
        lenient().when(time.now()).thenReturn(now);
    }

    @Test
    void shouldReturnTrue_whenEligible() {
        CaseData caseData = CaseData.builder()
            .respondent1ResponseDeadline(now.minusDays(1))
            .build();

        assertTrue(judgementEligibilityChecker.isEligibleForJudgement(caseData));
    }

    @Test
    void shouldReturnFalse_whenDefenceSubmitted() {
        CaseData caseData = CaseData.builder()
            .respondent1ResponseDate(now)
            .respondent1ResponseDeadline(now.minusDays(1))
            .build();

        assertFalse(judgementEligibilityChecker.isEligibleForJudgement(caseData));
    }

    @Test
    void shouldReturnFalse_whenCaseOffline() {
        CaseData caseData = CaseData.builder()
            .takenOfflineDate(now)
            .respondent1ResponseDeadline(now.minusDays(1))
            .build();

        assertFalse(judgementEligibilityChecker.isEligibleForJudgement(caseData));
    }

    @Test
    void shouldReturnFalse_whenDeadlineNotPast() {
        CaseData caseData = CaseData.builder()
            .respondent1ResponseDeadline(now.plusDays(1))
            .build();

        assertFalse(judgementEligibilityChecker.isEligibleForJudgement(caseData));
    }

    @Test
    void shouldReturnFalse_whenDeadlineIsNull() {
        CaseData caseData = CaseData.builder()
            .respondent1ResponseDeadline(null)
            .build();

        assertFalse(judgementEligibilityChecker.isEligibleForJudgement(caseData));
    }
}
