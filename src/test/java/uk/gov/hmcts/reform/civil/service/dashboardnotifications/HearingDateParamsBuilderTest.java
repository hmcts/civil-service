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

class HearingDateParamsBuilderTest {

    private HearingDateParamsBuilder builder;

    @BeforeEach
    void setUp() {
        builder = new HearingDateParamsBuilder();
    }

    @Test
    void shouldAddHearingDateDetailsWhenHearingDateIsPresent() {
        CaseData caseData = mock(CaseData.class);
        LocalDate hearingDate = LocalDate.of(2023, 10, 15);

        when(caseData.getHearingDate()).thenReturn(hearingDate);

        HashMap<String, Object> params = new HashMap<>();
        builder.addParams(caseData, params);

        assertThat(params).containsEntry("hearingDateEn", DateUtils.formatDate(hearingDate));
        assertThat(params).containsEntry("hearingDateCy", DateUtils.formatDateInWelsh(hearingDate, false));
    }

    @Test
    void shouldNotAddHearingDateDetailsWhenHearingDateIsNull() {
        CaseData caseData = mock(CaseData.class);

        when(caseData.getHearingDate()).thenReturn(null);

        HashMap<String, Object> params = new HashMap<>();
        builder.addParams(caseData, params);

        assertThat(params).isEmpty();
    }
}
