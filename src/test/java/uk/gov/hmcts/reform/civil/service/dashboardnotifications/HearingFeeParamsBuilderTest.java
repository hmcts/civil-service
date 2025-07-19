package uk.gov.hmcts.reform.civil.service.dashboardnotifications;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Fee;

import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class HearingFeeParamsBuilderTest {

    private HearingFeeParamsBuilder builder;

    @BeforeEach
    void setUp() {
        builder = new HearingFeeParamsBuilder();
    }

    @Test
    void shouldAddHearingFeeWhenPresent() {
        // Arrange
        CaseData caseData = mock(CaseData.class);
        Fee hearingFee = mock(Fee.class);

        when(caseData.getHearingFee()).thenReturn(hearingFee);
        when(hearingFee.toPounds()).thenReturn(new java.math.BigDecimal("123.45"));

        HashMap<String, Object> params = new HashMap<>();

        // Act
        builder.addParams(caseData, params);

        // Assert
        assertThat(params).containsEntry("hearingFee", "£123.45");
    }

    @Test
    void shouldNotAddHearingFeeWhenNull() {
        // Arrange
        CaseData caseData = mock(CaseData.class);

        when(caseData.getHearingFee()).thenReturn(null);

        HashMap<String, Object> params = new HashMap<>();

        // Act
        builder.addParams(caseData, params);

        // Assert
        assertThat(params).isEmpty();
    }
}
