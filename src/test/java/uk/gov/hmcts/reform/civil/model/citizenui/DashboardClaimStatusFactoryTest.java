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
        FeatureToggleService toggleService = Mockito.mock(FeatureToggleService.class);
        CaseData caseData = CaseData.builder()
            .ccdState(CaseState.CASE_STAYED)
            .build();
        CcdDashboardDefendantClaimMatcher defendant = new CcdDashboardDefendantClaimMatcher(caseData, toggleService);
        CcdDashboardClaimantClaimMatcher claimant = new CcdDashboardClaimantClaimMatcher(caseData, toggleService);

        List<Arguments> argumentList = new ArrayList<>();
        argumentList.add(Arguments.arguments(defendant, DashboardClaimStatus.CASE_STAYED));
        argumentList.add(Arguments.arguments(claimant, DashboardClaimStatus.CASE_STAYED));

        caseData = CaseData.builder()
            .ccdState(CaseState.CASE_DISMISSED)
            .build();
        defendant = new CcdDashboardDefendantClaimMatcher(caseData, toggleService);
        claimant = new CcdDashboardClaimantClaimMatcher(caseData, toggleService);
        argumentList.add(Arguments.arguments(defendant, DashboardClaimStatus.CASE_DISMISSED));
        argumentList.add(Arguments.arguments(claimant, DashboardClaimStatus.CASE_DISMISSED));

        return argumentList.stream();
    }

    @ParameterizedTest
    @MethodSource("caseToExpectedStatus")
    void shouldReturnCorrectStatus_whenInvoked(Claim claim, DashboardClaimStatus status) {
        assertEquals(status, claimStatusFactory.getDashboardClaimStatus(claim));
    }
}
