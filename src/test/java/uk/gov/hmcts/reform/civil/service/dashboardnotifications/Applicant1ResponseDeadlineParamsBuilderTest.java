package uk.gov.hmcts.reform.civil.service.dashboardnotifications;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.utils.DateUtils;

import java.time.LocalDateTime;
import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class Applicant1ResponseDeadlineParamsBuilderTest {

    private Applicant1ResponseDeadlineParamsBuilder builder;

    @BeforeEach
    void setUp() {
        builder = new Applicant1ResponseDeadlineParamsBuilder();
    }

    @Test
    void shouldAddParamsWhenApplicant1ResponseDeadlineIsPresent() {
        LocalDateTime responseDeadline = LocalDateTime.now();
        CaseData caseData = mock(CaseData.class);
        when(caseData.getApplicant1ResponseDeadline()).thenReturn(responseDeadline);
        HashMap<String, Object> params = new HashMap<>();

        builder.addParams(caseData, params);

        assertThat(params).containsEntry("applicant1ResponseDeadline", responseDeadline);
        assertThat(params).containsEntry("applicant1ResponseDeadlineEn", DateUtils.formatDate(responseDeadline));
        assertThat(params).containsEntry("applicant1ResponseDeadlineCy",
            DateUtils.formatDateInWelsh(responseDeadline.toLocalDate(), false));

    }

    @Test
    void shouldNotAddParamsWhenApplicant1ResponseDeadlineIsNull() {
        CaseData caseData = mock(CaseData.class);
        when(caseData.getApplicant1ResponseDeadline()).thenReturn(null);
        HashMap<String, Object> params = new HashMap<>();

        builder.addParams(caseData, params);

        assertThat(params).isEmpty();
    }
}
