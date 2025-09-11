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
        params.put("defendantAdmittedAmount", "£100");
        params.put("applicant1PartyName", "John");

        // Act
        builder.addParams(caseData, params);

        // Assert
        assertThat(params).containsEntry("descriptionEn",
                                         "<p class=\"govuk-body\">You have offered to pay £100 by 1 October 2023. The payment must be " +
                                             "received in John's account by then, " +
                                             "if not they can request a county court judgment.</p><p class=\"govuk-body\"><a href=\"{VIEW_RESPONSE_TO_CLAIM}\" " +
                                             "class=\"govuk-link\">View your response</a></p>");
        assertThat(params).containsEntry("descriptionCy",
                                         "<p class=\"govuk-body\">Rydych wedi cynnig talu £100 erbyn 1 Hydref 2023. Rhaid i’r taliad fod " +
                                             "yng nghyfrif John erbyn y dyddiad hwnnw. " +
                                             "Os nad yw, yna gallant wneud cais am ddyfarniad llys sirol." +
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

    @Test
    void shouldAddPaymentDeadlineParamsWhenRespondWithPartAdmitPayImmediately() {
        // Arrange
        CaseData caseData = mock(CaseData.class);
        RespondToClaimAdmitPartLRspec admitPartLRspec = mock(RespondToClaimAdmitPartLRspec.class);
        LocalDate paymentDate = LocalDate.parse("2023-10-01");

        when(caseData.isPartAdmitPayImmediatelyClaimSpec()).thenReturn(true);
        when(caseData.getRespondToClaimAdmitPartLRspec()).thenReturn(admitPartLRspec);
        when(admitPartLRspec.getWhenWillThisAmountBePaid()).thenReturn(paymentDate);

        HashMap<String, Object> params = new HashMap<>();
        params.put("defendantAdmittedAmount", "£100");
        params.put("applicant1PartyName", "John");

        // Act
        builder.addParams(caseData, params);

        // Assert
        assertThat(params).containsEntry("descriptionEn",
                                         "<p class=\"govuk-body\">You've said you owe £100 plus the claim fee " +
                                             "and any fixed costs claimed and offered to pay John immediately. " +
                                             "We will contact you when the claimant responds.</p>");
        assertThat(params).containsEntry("descriptionCy",
                                         "<p class=\"govuk-body\">Rydych chi wedi dweud bod £100 yn ddyledus gennych, " +
                                             "a ffi’r hawliad ac unrhyw gostau sefydlog a hawlir ac " +
                                             "rydych wedi cynnig i dalu John ar unwaith. Byddwn yn cysylltu â chi pan fydd yr hawlydd yn ymateb.</p>");
    }
}
