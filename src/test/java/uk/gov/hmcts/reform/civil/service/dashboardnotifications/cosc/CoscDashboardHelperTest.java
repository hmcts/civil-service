package uk.gov.hmcts.reform.civil.service.dashboardnotifications.cosc;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentDetails;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CoscDashboardHelperTest {

    @Test
    void shouldReturnTrue_whenActiveJudgmentFullyPaymentMadeDateIsPresent() {
        CaseData caseData = new CaseDataBuilder().build();
        caseData.setActiveJudgment(new JudgmentDetails().setFullyPaymentMadeDate(LocalDate.now()));

        assertTrue(new CoscDashboardHelper().isMarkedPaidInFull(caseData));
    }

    @Test
    void shouldReturnFalse_whenActiveJudgmentFullyPaymentMadeDateIsNotPresent() {
        CaseData caseData = new CaseDataBuilder().build();
        caseData.setActiveJudgment(new JudgmentDetails());

        assertFalse(new CoscDashboardHelper().isMarkedPaidInFull(caseData));
    }

    @Test
    void shouldReturnFalse_whenActiveJudgmentIsNotPresent() {
        CaseData caseData = new CaseDataBuilder().build();
        caseData.setActiveJudgment(null);

        assertFalse(new CoscDashboardHelper().isMarkedPaidInFull(caseData));
    }
}
