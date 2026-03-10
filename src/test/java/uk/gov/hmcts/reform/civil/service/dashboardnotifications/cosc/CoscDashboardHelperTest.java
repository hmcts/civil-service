package uk.gov.hmcts.reform.civil.service.dashboardnotifications.cosc;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentDetails;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDetailsBuilder;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CoscDashboardHelperTest {

    @Mock
    private CoreCaseDataService coreCaseDataService;

    @Mock
    private CaseDetailsConverter caseDetailsConverter;

    @InjectMocks
    private CoscDashboardHelper coscDashboardHelper;

    @Nested
    class IsMarkedPaidInFullTests {

        @Test
        void shouldReturnTrue_whenActiveJudgmentFullyPaymentMadeDateIsPresent() {
            CaseData caseData = new CaseDataBuilder().build();
            caseData.setActiveJudgment(new JudgmentDetails().setFullyPaymentMadeDate(LocalDate.now()));

            assertTrue(coscDashboardHelper.isMarkedPaidInFull(caseData));
        }

        @Test
        void shouldReturnFalse_whenActiveJudgmentFullyPaymentMadeDateIsNotPresent() {
            CaseData caseData = new CaseDataBuilder().build();
            caseData.setActiveJudgment(new JudgmentDetails());

            assertFalse(coscDashboardHelper.isMarkedPaidInFull(caseData));
        }

        @Test
        void shouldReturnFalse_whenActiveJudgmentIsNotPresent() {
            CaseData caseData = new CaseDataBuilder().build();
            caseData.setActiveJudgment(null);

            assertFalse(coscDashboardHelper.isMarkedPaidInFull(caseData));
        }
    }

    @Nested
    class GetParentCaseDataTests {

        @Test
        void shouldReturnParentCaseData() {
            Long caseId = 123456L;
            GeneralApplicationCaseData gaCaseData = new GeneralApplicationCaseData();
            gaCaseData.setParentCaseReference(caseId.toString());
            CaseDetails caseDetails = new CaseDetailsBuilder().build();
            CaseData expectedCaseData = new CaseDataBuilder().build();

            when(coreCaseDataService.getCase(caseId)).thenReturn(caseDetails);
            when(caseDetailsConverter.toCaseData(caseDetails)).thenReturn(expectedCaseData);

            CaseData parentCaseData = coscDashboardHelper.getParentCaseData(gaCaseData);

            assertEquals(expectedCaseData, parentCaseData);
        }
    }
}
