package uk.gov.hmcts.reform.civil.service.dashboardnotifications;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Fee;

import java.math.BigDecimal;
import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ClaimFeeParamsBuilderTest {

    private ClaimFeeParamsBuilder builder;

    @BeforeEach
    void setUp() {
        builder = new ClaimFeeParamsBuilder();
    }

    @Test
    void shouldAddClaimFeeWhenPresent() {
        // Arrange
        Fee claimFee = mock(Fee.class);
        when(claimFee.toPounds()).thenReturn(new BigDecimal("100.00"));

        CaseData caseData = mock(CaseData.class);
        when(caseData.getClaimFee()).thenReturn(claimFee);

        HashMap<String, Object> params = new HashMap<>();

        // Act
        builder.addParams(caseData, params);

        // Assert
        assertThat(params).containsEntry("claimFee", "£100");
    }

    @Test
    void shouldNotAddClaimFeeWhenNotPresent() {
        // Arrange
        CaseData caseData = mock(CaseData.class);
        when(caseData.getClaimFee()).thenReturn(null);

        HashMap<String, Object> params = new HashMap<>();

        // Act
        builder.addParams(caseData, params);

        // Assert
        assertThat(params).isEmpty();
    }
}
