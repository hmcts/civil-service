package uk.gov.hmcts.reform.civil.utils;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.enums.cosc.CoscApplicationStatus;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentDetails;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MarkPaidInFullUtilTest {

    @Nested
    class CheckMarkPaidInFull {
        @Test
        void shouldReturnTrue_whenAllConditionsAreMet() {
            CaseData caseData = CaseData.builder()
                .activeJudgment(new JudgmentDetails().setFullyPaymentMadeDate(LocalDate.now()))
                .coSCApplicationStatus(CoscApplicationStatus.ACTIVE)
                .build();

            assertTrue(MarkPaidInFullUtil.checkMarkPaidInFull(caseData));
        }

        @Test
        void shouldReturnFalse_whenActiveJudgmentIsNull() {
            CaseData caseData = CaseData.builder()
                .coSCApplicationStatus(CoscApplicationStatus.ACTIVE)
                .build();

            assertFalse(MarkPaidInFullUtil.checkMarkPaidInFull(caseData));
        }

        @Test
        void shouldReturnFalse_whenFullyPaymentMadeDateIsNull() {
            CaseData caseData = CaseData.builder()
                .activeJudgment(new JudgmentDetails())
                .coSCApplicationStatus(CoscApplicationStatus.ACTIVE)
                .build();

            assertFalse(MarkPaidInFullUtil.checkMarkPaidInFull(caseData));
        }

        @Test
        void shouldReturnFalse_whenCoSCApplicationStatusIsNull() {
            CaseData caseData = CaseData.builder()
                .activeJudgment(new JudgmentDetails().setFullyPaymentMadeDate(LocalDate.now()))
                .build();

            assertFalse(MarkPaidInFullUtil.checkMarkPaidInFull(caseData));
        }

        @Test
        void shouldReturnFalse_whenCoSCApplicationStatusIsNotActive() {
            CaseData caseData = CaseData.builder()
                .activeJudgment(new JudgmentDetails().setFullyPaymentMadeDate(LocalDate.now()))
                .coSCApplicationStatus(CoscApplicationStatus.PROCESSED)
                .build();

            assertFalse(MarkPaidInFullUtil.checkMarkPaidInFull(caseData));
        }
    }

    @Nested
    class CheckMarkPaidInFullAndPaidForApplication {
        @Test
        void shouldReturnTrue_whenAllConditionsAreMet() {
            CaseData caseData = CaseData.builder()
                .activeJudgment(new JudgmentDetails().setFullyPaymentMadeDate(LocalDate.now()))
                .coSCApplicationStatus(CoscApplicationStatus.PROCESSED)
                .build();

            assertTrue(MarkPaidInFullUtil.checkMarkPaidInFullAndPaidForApplication(caseData));
        }

        @Test
        void shouldReturnFalse_whenActiveJudgmentIsNull() {
            CaseData caseData = CaseData.builder()
                .coSCApplicationStatus(CoscApplicationStatus.PROCESSED)
                .build();

            assertFalse(MarkPaidInFullUtil.checkMarkPaidInFullAndPaidForApplication(caseData));
        }

        @Test
        void shouldReturnFalse_whenFullyPaymentMadeDateIsNull() {
            CaseData caseData = CaseData.builder()
                .activeJudgment(new JudgmentDetails())
                .coSCApplicationStatus(CoscApplicationStatus.PROCESSED)
                .build();

            assertFalse(MarkPaidInFullUtil.checkMarkPaidInFullAndPaidForApplication(caseData));
        }

        @Test
        void shouldReturnFalse_whenCoSCApplicationStatusIsNull() {
            CaseData caseData = CaseData.builder()
                .activeJudgment(new JudgmentDetails().setFullyPaymentMadeDate(LocalDate.now()))
                .build();

            assertFalse(MarkPaidInFullUtil.checkMarkPaidInFullAndPaidForApplication(caseData));
        }

        @Test
        void shouldReturnFalse_whenCoSCApplicationStatusIsNotProcessed() {
            CaseData caseData = CaseData.builder()
                .activeJudgment(new JudgmentDetails().setFullyPaymentMadeDate(LocalDate.now()))
                .coSCApplicationStatus(CoscApplicationStatus.ACTIVE)
                .build();

            assertFalse(MarkPaidInFullUtil.checkMarkPaidInFullAndPaidForApplication(caseData));
        }
    }
}
