package uk.gov.hmcts.reform.civil.service.dashboardnotifications;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.math.BigDecimal;
import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ClaimIssueRemissionAmountParamsBuilderTest {

    private ClaimIssueRemissionAmountParamsBuilder builder;

    @BeforeEach
    void setUp() {
        builder = new ClaimIssueRemissionAmountParamsBuilder();
    }

    @Test
    void shouldAddClaimIssueRemissionAmountWhenPresent() {
        // Arrange
        CaseData caseData = mock(CaseData.class);
        when(caseData.getClaimIssueRemissionAmount()).thenReturn(new BigDecimal(5000L)); // Amount in pennies

        HashMap<String, Object> params = new HashMap<>();

        // Act
        builder.addParams(caseData, params);

        // Assert
        assertThat(params).containsEntry("claimIssueRemissionAmount", "£50");
    }

    @Test
    void shouldNotAddClaimIssueRemissionAmountWhenNotPresent() {
        // Arrange
        CaseData caseData = mock(CaseData.class);
        when(caseData.getClaimIssueRemissionAmount()).thenReturn(null);

        HashMap<String, Object> params = new HashMap<>();

        // Act
        builder.addParams(caseData, params);

        // Assert
        assertThat(params).isEmpty();
    }
}
