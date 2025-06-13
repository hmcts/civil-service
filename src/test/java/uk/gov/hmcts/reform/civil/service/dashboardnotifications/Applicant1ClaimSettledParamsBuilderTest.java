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
import static uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardNotificationsParamsBuilder.CLAIM_SETTLED_OBJECTION_DEADLINE_DAYS;
import static uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardNotificationsParamsBuilder.END_OF_DAY;

class Applicant1ClaimSettledParamsBuilderTest {

    private Applicant1ClaimSettledParamsBuilder builder;

    @BeforeEach
    void setUp() {
        builder = new Applicant1ClaimSettledParamsBuilder();
    }

    @Test
    void shouldAddParamsWhenClaimSettleDateIsPresent() {
        LocalDate claimSettleDate = LocalDate.now();
        CaseData caseData = mock(CaseData.class);
        when(caseData.getApplicant1ClaimSettleDate()).thenReturn(claimSettleDate);
        HashMap<String, Object> params = new HashMap<>();

        builder.addParams(caseData, params);

        assertThat(params).containsEntry("applicant1ClaimSettledObjectionsDeadline",
            params.put("applicant1ClaimSettledObjectionsDeadline",
                claimSettleDate.plusDays(CLAIM_SETTLED_OBJECTION_DEADLINE_DAYS).atTime(END_OF_DAY)));
        assertThat(params).containsEntry("applicant1ClaimSettledDateEn", DateUtils.formatDate(claimSettleDate));
        assertThat(params).containsEntry("applicant1ClaimSettledDateCy", DateUtils.formatDateInWelsh(claimSettleDate, false));
    }

    @Test
    void shouldNotAddParamsWhenClaimSettleDateIsNull() {
        CaseData caseData = mock(CaseData.class);
        when(caseData.getApplicant1ClaimSettleDate()).thenReturn(null);
        HashMap<String, Object> params = new HashMap<>();

        builder.addParams(caseData, params);

        assertThat(params).isEmpty();
    }
}
