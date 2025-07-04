package uk.gov.hmcts.reform.civil.service.dashboardnotifications;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.RespondToClaimAdmitPartLRspec;

import java.time.LocalDate;
import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardNotificationsParamsBuilder.END_OF_DAY;

class Respondent1AdmittedAmountPaymentDeadlineParamsBuilderTest {

    private Respondent1AdmittedAmountPaymentDeadlineParamsBuilder builder;

    @BeforeEach
    void setUp() {
        builder = new Respondent1AdmittedAmountPaymentDeadlineParamsBuilder();
    }

    @Test
    void shouldAddPaymentDeadlineParamsWhenRespondToClaimAdmitPartLRspecIsPresent() {
        // Arrange
        CaseData caseData = mock(CaseData.class);
        RespondToClaimAdmitPartLRspec admitPartLRspec = mock(RespondToClaimAdmitPartLRspec.class);
        LocalDate paymentDate = LocalDate.parse("2023-10-01");

        when(caseData.getRespondToClaimAdmitPartLRspec()).thenReturn(admitPartLRspec);
        when(admitPartLRspec.getWhenWillThisAmountBePaid()).thenReturn(paymentDate);

        HashMap<String, Object> params = new HashMap<>();

        // Act
        builder.addParams(caseData, params);

        // Assert
        assertThat(params).containsEntry("respondent1AdmittedAmountPaymentDeadline", paymentDate.atTime(END_OF_DAY));
        assertThat(params).containsEntry("respondent1AdmittedAmountPaymentDeadlineEn", "1 October 2023");
        assertThat(params).containsEntry("respondent1AdmittedAmountPaymentDeadlineCy", "1 Hydref 2023");
    }

    @Test
    void shouldNotAddPaymentDeadlineParamsWhenRespondToClaimAdmitPartLRspecIsNull() {
        // Arrange
        CaseData caseData = mock(CaseData.class);

        when(caseData.getRespondToClaimAdmitPartLRspec()).thenReturn(null);

        HashMap<String, Object> params = new HashMap<>();

        // Act
        builder.addParams(caseData, params);

        // Assert
        assertThat(params).isEmpty();
    }
}
