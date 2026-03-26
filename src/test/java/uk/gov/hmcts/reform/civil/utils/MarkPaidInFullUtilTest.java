package uk.gov.hmcts.reform.civil.utils;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.citizenui.CertOfSC;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentDetails;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MarkPaidInFullUtilTest {

    @Test
    void shouldReturnTrue_whenAllConditionsAreMet() {
        CaseData caseData = CaseData.builder()
            .activeJudgment(new JudgmentDetails().setFullyPaymentMadeDate(LocalDate.now()))
            .certOfSC(new CertOfSC().setDefendantFinalPaymentDate(LocalDate.now()))
            .build();

        assertTrue(MarkPaidInFullUtil.checkMarkPaidInFull(caseData));
    }

    @Test
    void shouldReturnFalse_whenActiveJudgmentIsNull() {
        CaseData caseData = CaseData.builder()
            .certOfSC(new CertOfSC().setDefendantFinalPaymentDate(LocalDate.now()))
            .build();

        assertFalse(MarkPaidInFullUtil.checkMarkPaidInFull(caseData));
    }

    @Test
    void shouldReturnFalse_whenFullyPaymentMadeDateIsNull() {
        CaseData caseData = CaseData.builder()
            .activeJudgment(new JudgmentDetails())
            .certOfSC(new CertOfSC().setDefendantFinalPaymentDate(LocalDate.now()))
            .build();

        assertFalse(MarkPaidInFullUtil.checkMarkPaidInFull(caseData));
    }

    @Test
    void shouldReturnFalse_whenCertOfSCIsNull() {
        CaseData caseData = CaseData.builder()
            .activeJudgment(new JudgmentDetails().setFullyPaymentMadeDate(LocalDate.now()))
            .build();

        assertFalse(MarkPaidInFullUtil.checkMarkPaidInFull(caseData));
    }

    @Test
    void shouldReturnFalse_whenDefendantFinalPaymentDateIsNull() {
        CaseData caseData = CaseData.builder()
            .activeJudgment(new JudgmentDetails().setFullyPaymentMadeDate(LocalDate.now()))
            .certOfSC(new CertOfSC())
            .build();

        assertFalse(MarkPaidInFullUtil.checkMarkPaidInFull(caseData));
    }
}
