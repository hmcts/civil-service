package uk.gov.hmcts.reform.civil.service.dashboardnotifications;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.enums.FeeType;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class HwfFeeTypeParamsBuilderTest {

    private HwfFeeTypeParamsBuilder builder;

    @BeforeEach
    void setUp() {
        builder = new HwfFeeTypeParamsBuilder();
    }

    @Test
    void shouldAddTypeOfFeeWhenPresent() {
        // Arrange
        CaseData caseData = mock(CaseData.class);
        FeeType hwfFeeType = mock(FeeType.class);

        when(caseData.getHwfFeeType()).thenReturn(hwfFeeType);
        when(hwfFeeType.getLabel()).thenReturn("Fee Type Label");

        HashMap<String, Object> params = new HashMap<>();

        // Act
        builder.addParams(caseData, params);

        // Assert
        assertThat(params).containsEntry("typeOfFee", "Fee Type Label");
    }

    @Test
    void shouldNotAddTypeOfFeeWhenNull() {
        // Arrange
        CaseData caseData = mock(CaseData.class);

        when(caseData.getHwfFeeType()).thenReturn(null);

        HashMap<String, Object> params = new HashMap<>();

        // Act
        builder.addParams(caseData, params);

        // Assert
        assertThat(params).isEmpty();
    }
}
