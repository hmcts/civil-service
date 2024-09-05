package uk.gov.hmcts.reform.civil.model.citizenui;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DashboardClaimStatusFactoryTest {

    private final DashboardClaimStatusFactory claimStatusFactory = new DashboardClaimStatusFactory();

    static Stream<Arguments> caseToExpectedStatus() {
        return Stream.of(
            Arguments.arguments(CaseState.CASE_DISMISSED, DashboardClaimStatus.CASE_DISMISSED)
        );
    }

    @ParameterizedTest
    @MethodSource("caseToExpectedStatus")
    void shouldReturnCorrectStatus_whenInvoked(CaseState ccdState, DashboardClaimStatus expectedStatus) {
        CaseData caseData = CaseData.builder()
            .ccdState(ccdState)
            .build();
        CcdDashboardClaimantClaimMatcher claimant = new CcdDashboardClaimantClaimMatcher(caseData, Mockito.mock(FeatureToggleService.class));

        assertEquals(expectedStatus, claimStatusFactory.getDashboardClaimStatus(claimant));
    }
}
