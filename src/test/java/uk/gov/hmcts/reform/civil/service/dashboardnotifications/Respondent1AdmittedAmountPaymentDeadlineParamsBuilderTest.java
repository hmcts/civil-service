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
        assertThat(params).containsEntry("descriptionEn",
                                         "<p class=\"govuk-body\">You have offered to pay 0 by 1 October 2023 ." +
            " The payment must be received in ''s account by then, if not they can request a county court judgment.</p><p class=\"govuk-body\">" +
                                             "<a href=\"{VIEW_RESPONSE_TO_CLAIM}\" class=\"govuk-link\">View your response</a></p>");
        assertThat(params).containsEntry("descriptionCy",
                                         "<p class=\"govuk-body\">Rydych wedi cynnig talu 0 erbyn 1 Hydref 2023. " +
            "Rhaid i’r taliad fod yng nghyfrif  erbyn y dyddiad hwnnw. Os nad yw, yna gallant wneud cais am ddyfarniad llys sirol." +
                                             "</p><p class=\"govuk-body\"><a href=\"{VIEW_RESPONSE_TO_CLAIM}\" class=\"govuk-link\">Gweld eich ymateb</a></p>");
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
