package uk.gov.hmcts.reform.civil.service.dashboardnotifications;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ClaimantRepaymentPlanDecisionParamsBuilderTest {

    private ClaimantRepaymentPlanDecisionParamsBuilder builder;

    @BeforeEach
    void setUp() {
        builder = new ClaimantRepaymentPlanDecisionParamsBuilder();
    }

    @Test
    void shouldAddClaimantRepaymentPlanDecisionParamsWhenAccepted() {
        // Arrange
        CaseData caseData = mock(CaseData.class);
        when(caseData.hasApplicantAcceptedRepaymentPlan()).thenReturn(true);

        HashMap<String, Object> params = new HashMap<>();

        // Act
        builder.addParams(caseData, params);

        // Assert
        assertThat(params).containsEntry("claimantRepaymentPlanDecision", "accepted");
        assertThat(params).containsEntry("claimantRepaymentPlanDecisionCy", "derbyn");
    }

    @Test
    void shouldAddClaimantRepaymentPlanDecisionParamsWhenRejected() {
        // Arrange
        CaseData caseData = mock(CaseData.class);
        when(caseData.hasApplicantAcceptedRepaymentPlan()).thenReturn(false);

        HashMap<String, Object> params = new HashMap<>();

        // Act
        builder.addParams(caseData, params);

        // Assert
        assertThat(params).containsEntry("claimantRepaymentPlanDecision", "rejected");
        assertThat(params).containsEntry("claimantRepaymentPlanDecisionCy", "gwrthod");
    }
}
