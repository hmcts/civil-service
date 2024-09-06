package uk.gov.hmcts.reform.civil.model.citizenui;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DashboardClaimStatusFactoryTest {

    private final DashboardClaimStatusFactory claimStatusFactory = new DashboardClaimStatusFactory();

    @ParameterizedTest
    @MethodSource("hearingFeeHistory")
    void shouldReturnCorrectStatus_hearingFeePaid(Claim claim, DashboardClaimStatus status) {
        assertEquals(status, claimStatusFactory.getDashboardClaimStatus(claim));
    }

    static Stream<Arguments> hearingFeeHistory() {
        FeatureToggleService toggleService = Mockito.mock(FeatureToggleService.class);
        Mockito.when(toggleService.isCaseProgressionEnabled()).thenReturn(true);

        return CaseDataHistory.fastClaim()
            .scheduleHearingDays(6*7+1)
            .requestHwF()
            .invalidHwFReferenceNumber()
            .updatedHwFReferenceNumber()
            .moreInformationRequiredHwF()
            .hwfRejected()
            .payHearingFee().getArguments(toggleService);
    }

    @ParameterizedTest
    @MethodSource("feeNotPaid")
    void shouldReturnCorrectStatus_feeNotPaid(Claim claim, DashboardClaimStatus status) {
        assertEquals(status, claimStatusFactory.getDashboardClaimStatus(claim));
    }

    static Stream<Arguments> feeNotPaid() {
        FeatureToggleService toggleService = Mockito.mock(FeatureToggleService.class);
        Mockito.when(toggleService.isCaseProgressionEnabled()).thenReturn(true);

        return CaseDataHistory.smallClaim()
            .scheduleHearingDays(6*7+1)
            .requestHwF()
            .hwfPartial()
            .doNotPayHearingFee()
            .payHearingFee().getArguments(toggleService);
    }

    @ParameterizedTest
    @MethodSource("awaitingJudgment")
    void shouldReturnCorrectStatus_awaitingJudgment(Claim claim, DashboardClaimStatus status) {
        assertEquals(status, claimStatusFactory.getDashboardClaimStatus(claim));
    }

    static Stream<Arguments> awaitingJudgment() {
        FeatureToggleService toggleService = Mockito.mock(FeatureToggleService.class);
        Mockito.when(toggleService.isCaseProgressionEnabled()).thenReturn(true);

        return CaseDataHistory.fastClaim()
            .scheduleHearingDays(6*7+1)
            .requestHwF()
            .hwfFull()
            .scheduleHearingDays(6*7)
            .submitClaimantHearingArrangements()
            .submitDefendantHearingArrangements()
            .scheduleHearingDays(3*7)
            .awaitingJudgment().getArguments(toggleService);
    }
}
