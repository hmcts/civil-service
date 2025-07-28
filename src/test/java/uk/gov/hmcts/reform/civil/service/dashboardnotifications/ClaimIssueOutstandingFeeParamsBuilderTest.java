package uk.gov.hmcts.reform.civil.service.dashboardnotifications;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.math.BigDecimal;
import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ClaimIssueOutstandingFeeParamsBuilderTest {

    private ClaimIssueOutstandingFeeParamsBuilder builder;

    @BeforeEach
    void setUp() {
        builder = new ClaimIssueOutstandingFeeParamsBuilder();
    }

    @Test
    void shouldAddClaimIssueOutstandingAmountWhenPresent() {
        // Arrange
        CaseData caseData = mock(CaseData.class);
        when(caseData.getOutstandingFeeInPounds()).thenReturn(new BigDecimal("150.00"));

        HashMap<String, Object> params = new HashMap<>();

        // Act
        builder.addParams(caseData, params);

        // Assert
        assertThat(params).containsEntry("claimIssueOutStandingAmount", "£150");
    }

    @Test
    void shouldNotAddClaimIssueOutstandingAmountWhenNotPresent() {
        // Arrange
        CaseData caseData = mock(CaseData.class);
        when(caseData.getOutstandingFeeInPounds()).thenReturn(null);

        HashMap<String, Object> params = new HashMap<>();

        // Act
        builder.addParams(caseData, params);

        // Assert
        assertThat(params).isEmpty();
    }
}
