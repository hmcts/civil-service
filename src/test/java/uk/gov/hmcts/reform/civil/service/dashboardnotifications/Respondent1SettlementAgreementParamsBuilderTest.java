package uk.gov.hmcts.reform.civil.service.dashboardnotifications;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardNotificationsParamsBuilder.END_OF_DAY;

class Respondent1SettlementAgreementParamsBuilderTest {

    private Respondent1SettlementAgreementParamsBuilder builder;

    @BeforeEach
    void setUp() {
        builder = new Respondent1SettlementAgreementParamsBuilder();
    }

    @Test
    void shouldAddSettlementAgreementParamsWhenDeadlineIsPresent() {
        CaseData caseData = mock(CaseData.class);
        LocalDateTime deadline = LocalDateTime.parse("2023-10-01T12:00:00");

        when(caseData.getRespondent1RespondToSettlementAgreementDeadline()).thenReturn(deadline);

        when(caseData.hasApplicantAcceptedRepaymentPlan()).thenReturn(true);

        HashMap<String, Object> params = new HashMap<>();

        builder.addParams(caseData, params);

        LocalDate deadlineDate = deadline.toLocalDate();
        assertThat(params).containsEntry("respondent1SettlementAgreementDeadline", deadlineDate.atTime(END_OF_DAY));
        assertThat(params).containsEntry("respondent1SettlementAgreementDeadlineEn", "1 October 2023");
        assertThat(params).containsEntry("respondent1SettlementAgreementDeadlineCy", "1 Hydref 2023");
        assertThat(params).containsEntry("claimantSettlementAgreementEn", "accepted");
        assertThat(params).containsEntry("claimantSettlementAgreementCy", "derbyn");
    }

    @Test
    void shouldNotAddSettlementAgreementParamsWhenDeadlineIsNull() {
        // Arrange
        CaseData caseData = mock(CaseData.class);

        when(caseData.getRespondent1RespondToSettlementAgreementDeadline()).thenReturn(null);

        HashMap<String, Object> params = new HashMap<>();

        // Act
        builder.addParams(caseData, params);

        // Assert
        assertThat(params).isEmpty();
    }
}
