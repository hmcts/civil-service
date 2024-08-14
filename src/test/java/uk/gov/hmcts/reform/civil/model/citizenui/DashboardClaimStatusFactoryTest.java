package uk.gov.hmcts.reform.civil.model.citizenui;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

class DashboardClaimStatusFactoryTest {

    private final DashboardClaimStatusFactory factory = new DashboardClaimStatusFactory();

    @Test
    void allFinalOrdersIssuedCcdClaimant() {
        CaseData caseData = CaseData.builder().ccdState(CaseState.All_FINAL_ORDERS_ISSUED).build();
        FeatureToggleService featureToggleService = Mockito.mock(FeatureToggleService.class);
        Claim claim = new CcdDashboardClaimantClaimMatcher(caseData, featureToggleService);
        Assertions.assertEquals(
            DashboardClaimStatus.All_FINAL_ORDERS_ISSUED,
            factory.getDashboardClaimStatus(claim)
        );
    }

    @Test
    void allFinalOrdersIssuedCcdDefendant() {
        CaseData caseData = CaseData.builder().ccdState(CaseState.All_FINAL_ORDERS_ISSUED).build();
        FeatureToggleService featureToggleService = Mockito.mock(FeatureToggleService.class);
        Claim claim = new CcdDashboardDefendantClaimMatcher(caseData, featureToggleService);
        Assertions.assertEquals(
            DashboardClaimStatus.All_FINAL_ORDERS_ISSUED,
            factory.getDashboardClaimStatus(claim)
        );
    }
}
