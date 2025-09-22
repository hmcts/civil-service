package uk.gov.hmcts.reform.civil.service.dashboardnotifications;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.math.BigDecimal;
import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class HearingFeeRemissionParamsBuilderTest {

    private HearingFeeRemissionParamsBuilder builder;

    @BeforeEach
    void setUp() {
        builder = new HearingFeeRemissionParamsBuilder();
    }

    @Test
    void shouldAddHearingFeeRemissionAmountWhenPresent() {
        // Arrange
        CaseData caseData = mock(CaseData.class);
        BigDecimal remissionAmount = new BigDecimal(12345L);

        when(caseData.getHearingRemissionAmount()).thenReturn(remissionAmount);

        HashMap<String, Object> params = new HashMap<>();

        // Act
        builder.addParams(caseData, params);

        // Assert
        assertThat(params).containsEntry("hearingFeeRemissionAmount", "£123.45");
    }

    @Test
    void shouldNotAddHearingFeeRemissionAmountWhenNull() {
        // Arrange
        CaseData caseData = mock(CaseData.class);

        when(caseData.getHearingRemissionAmount()).thenReturn(null);

        HashMap<String, Object> params = new HashMap<>();

        // Act
        builder.addParams(caseData, params);

        // Assert
        assertThat(params).isEmpty();
    }
}
