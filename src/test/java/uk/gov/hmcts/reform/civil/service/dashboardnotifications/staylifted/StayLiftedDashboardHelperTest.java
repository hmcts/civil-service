package uk.gov.hmcts.reform.civil.service.dashboardnotifications.staylifted;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.reform.civil.enums.CaseState.CASE_PROGRESSION;

class StayLiftedDashboardHelperTest {

    private final StayLiftedDashboardHelper helper = new StayLiftedDashboardHelper();

    @Nested
    class HadHearingScheduledTests {

        @ParameterizedTest
        @EnumSource(value = CaseState.class, names = {
            "HEARING_READINESS",
            "PREPARE_FOR_HEARING_CONDUCT_HEARING"
        })
        void shouldReturnTrueForHearingStates(CaseState caseState) {
            CaseData caseData = new CaseDataBuilder().build();
            caseData.setPreStayState(caseState.name());

            assertTrue(helper.hadHearingScheduled(caseData));
        }

        @Test
        void shouldReturnFalseForNonHearingState() {
            CaseData caseData = new CaseDataBuilder().build();
            caseData.setPreStayState(CASE_PROGRESSION.name());

            assertFalse(helper.hadHearingScheduled(caseData));
        }
    }

    @Nested
    class IsPreCaseProgressionTests {

        @ParameterizedTest
        @EnumSource(value = CaseState.class, names = {
            "JUDICIAL_REFERRAL",
            "IN_MEDIATION"
        })
        void shouldReturnFalseForPreCaseProgressionStates(CaseState caseState) {
            CaseData caseData = new CaseDataBuilder().build();
            caseData.setPreStayState(caseState.name());

            assertFalse(helper.isNotPreCaseProgression(caseData));
        }

        @Test
        void shouldReturnTrueForNonPreCaseProgressionState() {
            CaseData caseData = new CaseDataBuilder().build();
            caseData.setPreStayState(CASE_PROGRESSION.name());

            assertTrue(helper.isNotPreCaseProgression(caseData));
        }
    }
}
