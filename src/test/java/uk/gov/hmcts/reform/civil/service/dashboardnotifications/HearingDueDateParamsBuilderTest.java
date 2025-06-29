package uk.gov.hmcts.reform.civil.service.dashboardnotifications;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.utils.DateUtils;

import java.time.LocalDate;
import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardNotificationsParamsBuilder.END_OF_DAY;

class HearingDueDateParamsBuilderTest {

    private HearingDueDateParamsBuilder builder;

    @BeforeEach
    void setUp() {
        builder = new HearingDueDateParamsBuilder();
    }

    @Test
    void shouldAddHearingDueDateDetailsWhenDueDateIsPresent() {
        // Arrange
        CaseData caseData = mock(CaseData.class);
        LocalDate hearingDueDate = LocalDate.of(2023, 10, 20);

        when(caseData.getHearingDueDate()).thenReturn(hearingDueDate);

        HashMap<String, Object> params = new HashMap<>();

        // Act
        builder.addParams(caseData, params);

        // Assert
        assertThat(params).containsEntry("hearingDueDate", hearingDueDate.atTime(END_OF_DAY));
        assertThat(params).containsEntry("hearingDueDateEn", DateUtils.formatDate(hearingDueDate));
        assertThat(params).containsEntry("hearingDueDateCy", DateUtils.formatDateInWelsh(hearingDueDate, false));
    }

    @Test
    void shouldNotAddHearingDueDateDetailsWhenDueDateIsNull() {
        // Arrange
        CaseData caseData = mock(CaseData.class);

        when(caseData.getHearingDueDate()).thenReturn(null);

        HashMap<String, Object> params = new HashMap<>();

        // Act
        builder.addParams(caseData, params);

        // Assert
        assertThat(params).isEmpty();
    }
}
