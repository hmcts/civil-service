package uk.gov.hmcts.reform.civil.model.citizenui;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DashboardClaimStatusFactoryTest {

    private final DashboardClaimStatusFactory claimStatusFactory = new DashboardClaimStatusFactory();

    static Stream<Arguments> caseToExpectedStatus() {
        List<Arguments> argumentList = new ArrayList<>();
        FeatureToggleService toggleService = Mockito.mock(FeatureToggleService.class);
        addCaseStayedCases(argumentList, toggleService);
        addCaseDismissCases(argumentList, toggleService);

        return argumentList.stream();
    }

    @ParameterizedTest
    @MethodSource("caseToExpectedStatus")
    void shouldReturnCorrectStatus_whenInvoked(Claim claim, DashboardClaimStatus status) {
        assertEquals(status, claimStatusFactory.getDashboardClaimStatus(claim));
    }

    private static void addCaseStayedCases(List<Arguments> argumentList, FeatureToggleService toggleService) {
        CaseData caseData = CaseData.builder()
            .ccdState(CaseState.CASE_STAYED)
            .build();
        CcdDashboardDefendantClaimMatcher defendant = new CcdDashboardDefendantClaimMatcher(caseData, toggleService);
        CcdDashboardClaimantClaimMatcher claimant = new CcdDashboardClaimantClaimMatcher(caseData, toggleService);

        argumentList.add(Arguments.arguments(defendant, DashboardClaimStatus.CASE_STAYED));
        argumentList.add(Arguments.arguments(claimant, DashboardClaimStatus.CASE_STAYED));
    }

    private static void addCaseDismissCases(List<Arguments> argumentList, FeatureToggleService toggleService) {
        CaseData caseData = CaseData.builder()
            .ccdState(CaseState.CASE_DISMISSED)
            .build();
        CcdDashboardDefendantClaimMatcher defendant = new CcdDashboardDefendantClaimMatcher(caseData, toggleService);
        CcdDashboardClaimantClaimMatcher claimant = new CcdDashboardClaimantClaimMatcher(caseData, toggleService);

        argumentList.add(Arguments.arguments(defendant, DashboardClaimStatus.CASE_DISMISSED));
        argumentList.add(Arguments.arguments(claimant, DashboardClaimStatus.CASE_DISMISSED));
    }
}
