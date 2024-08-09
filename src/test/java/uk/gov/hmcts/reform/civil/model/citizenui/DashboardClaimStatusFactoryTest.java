package uk.gov.hmcts.reform.civil.model.citizenui;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import static org.junit.jupiter.api.Assertions.*;

class DashboardClaimStatusFactoryTest {

    private final DashboardClaimStatusFactory factory = new DashboardClaimStatusFactory();

    @Test
    void decisionOutcomeCcdClaimant() {
        CaseData caseData = CaseData.builder().ccdState(CaseState.DECISION_OUTCOME).build();
        FeatureToggleService featureToggleService = Mockito.mock(FeatureToggleService.class);
        Claim claim = new CcdDashboardClaimantClaimMatcher(caseData, featureToggleService);
        Assertions.assertEquals(
            DashboardClaimStatus.DECISION_OUTCOME,
            factory.getDashboardClaimStatus(claim)
        );
    }

    @Test
    void decisionOutcomeCcdDefendant() {
        CaseData caseData = CaseData.builder().ccdState(CaseState.DECISION_OUTCOME).build();
        FeatureToggleService featureToggleService = Mockito.mock(FeatureToggleService.class);
        Claim claim = new CcdDashboardDefendantClaimMatcher(caseData, featureToggleService);
        Assertions.assertEquals(
            DashboardClaimStatus.DECISION_OUTCOME,
            factory.getDashboardClaimStatus(claim)
        );
    }
}
