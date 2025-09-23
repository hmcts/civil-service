package uk.gov.hmcts.reform.civil.service.dashboardnotifications;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.math.BigDecimal;
import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class HearingFeeOutstandingAmountParamsBuilderTest {

    private HearingFeeOutstandingAmountParamsBuilder builder;

    @BeforeEach
    void setUp() {
        builder = new HearingFeeOutstandingAmountParamsBuilder();
    }

    @Test
    void shouldAddHearingFeeOutstandingAmountWhenPresent() {
        // Arrange
        CaseData caseData = mock(CaseData.class);
        BigDecimal outstandingFee = new BigDecimal("123.45");

        when(caseData.getOutstandingFeeInPounds()).thenReturn(outstandingFee);

        HashMap<String, Object> params = new HashMap<>();

        // Act
        builder.addParams(caseData, params);

        // Assert
        assertThat(params).containsEntry("hearingFeeOutStandingAmount", "£123.45");
    }

    @Test
    void shouldNotAddHearingFeeOutstandingAmountWhenNull() {
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
