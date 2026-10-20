package uk.gov.hmcts.reform.civil.handler.callback.user.task.createclaim;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.interestcalc.InterestClaimOptions;
import uk.gov.hmcts.reform.civil.model.interestcalc.SameRateInterestSelection;
import uk.gov.hmcts.reform.civil.model.interestcalc.SameRateInterestType;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.utils.InterestCalculator;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class SpecValidateClaimInterestDateTest extends BaseCallbackHandlerTest {

    @Spy
    private InterestCalculator interestCalculator = new InterestCalculator();

    @InjectMocks
    private SpecValidateClaimInterestDateTask specValidateClaimInterestDateTask;

    @Test
    void shouldReturnErrorForFutureInterestDate() {
        LocalDate futureDate = LocalDate.now().plusDays(1);
        CaseData caseData = CaseDataBuilder.builder()
            .interestFromSpecificDate(futureDate)
            .build();

        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) specValidateClaimInterestDateTask
            .specValidateClaimInterestDate(caseData, "CREATE_CLAIM_SPEC");

        assertThat(response.getErrors()).contains("Correct the date. You can’t use a future date.");
    }

    @Test
    void shouldNotReturnErrorForPastOrTodayInterestDate() {
        LocalDate pastDate = LocalDate.now().minusDays(1);
        CaseData caseData = CaseDataBuilder.builder()
            .interestFromSpecificDate(pastDate)
            .build();

        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) specValidateClaimInterestDateTask
            .specValidateClaimInterestDate(caseData, "CREATE_CLAIM_SPEC");

        assertThat(response.getErrors()).isEmpty();
    }

    @Test
    void shouldReturnErrorForNegativeInterestRate() {
        SameRateInterestSelection selection = new SameRateInterestSelection();
        selection.setSameRateInterestType(SameRateInterestType.SAME_RATE_INTEREST_DIFFERENT_RATE);
        selection.setDifferentRate(BigDecimal.valueOf(-5));
        CaseData caseData = CaseDataBuilder.builder()
            .claimInterest(YesOrNo.YES)
            .interestClaimOptions(InterestClaimOptions.SAME_RATE_INTEREST)
            .sameRateInterestSelection(selection)
            .interestFromSpecificDate(LocalDate.now().minusDays(1))
            .build();

        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) specValidateClaimInterestDateTask
            .specValidateClaimInterestDate(caseData, "CREATE_CLAIM_SPEC");

        assertThat(response.getErrors()).contains(InterestCalculator.INTEREST_RATE_MUST_NOT_BE_NEGATIVE);
    }

}
