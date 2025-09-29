package uk.gov.hmcts.reform.civil.service.dashboardnotifications;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.time.LocalDate;
import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TrailArrangementParamsBuilderTest {

    private TrailArrangementParamsBuilder builder;

    @BeforeEach
    void setUp() {
        builder = new TrailArrangementParamsBuilder();
    }

    @Test
    void shouldAddTrialArrangementParamsWhenDataIsPresent() {
        // Arrange
        CaseData caseData = mock(CaseData.class);
        LocalDate trialDate = LocalDate.parse("2023-12-01");

        when(caseData.getHearingDate()).thenReturn(trialDate);

        HashMap<String, Object> params = new HashMap<>();

        // Act
        builder.addParams(caseData, params);

        // Assert
        assertThat(params).containsEntry("trialArrangementDeadlineEn", "3 November 2023");
        assertThat(params).containsEntry("trialArrangementDeadlineCy", "3 Tachwedd 2023");
    }

    @Test
    void shouldNotAddTrialArrangementParamsWhenDataIsMissing() {
        // Arrange
        CaseData caseData = mock(CaseData.class);

        when(caseData.getHearingDate()).thenReturn(null);

        HashMap<String, Object> params = new HashMap<>();

        // Act
        builder.addParams(caseData, params);

        // Assert
        assertThat(params).isEmpty();
    }
}
