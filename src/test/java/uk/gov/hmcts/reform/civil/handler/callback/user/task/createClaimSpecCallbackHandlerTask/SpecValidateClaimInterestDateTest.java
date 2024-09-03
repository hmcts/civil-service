package uk.gov.hmcts.reform.civil.handler.callback.user.task.createClaimSpecCallbackHandlerTask;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.createClaimSpecCallbackHanderTask.SpecValidateClaimInterestDateTask;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.interestcalc.InterestClaimFromType;
import uk.gov.hmcts.reform.civil.model.interestcalc.InterestClaimOptions;
import uk.gov.hmcts.reform.civil.model.interestcalc.SameRateInterestSelection;
import uk.gov.hmcts.reform.civil.model.interestcalc.SameRateInterestType;
import uk.gov.hmcts.reform.civil.utils.InterestCalculator;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;

@ExtendWith(MockitoExtension.class)
public class SpecValidateClaimInterestDateTest extends BaseCallbackHandlerTest {

    @InjectMocks
    private SpecValidateClaimInterestDateTask specValidateClaimInterestDateTask;

    @Test
    void shouldReturnErrorForFutureInterestDate() {
        // Given
        LocalDate futureDate = LocalDate.now().plusDays(1);
        CaseData caseData = CaseData.builder()
            .interestFromSpecificDate(futureDate)
            .build();

        // When
        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) specValidateClaimInterestDateTask
            .specValidateClaimInterestDate(caseData, "CREATE_CLAIM_SPEC");

        // Then
        assertThat(response.getErrors()).contains("Correct the date. You can’t use a future date.");
    }

    @Test
    void shouldNotReturnErrorForPastOrTodayInterestDate() {
        // Given
        LocalDate pastDate = LocalDate.now().minusDays(1);
        CaseData caseData = CaseData.builder()
            .interestFromSpecificDate(pastDate)
            .build();

        // When
        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) specValidateClaimInterestDateTask
            .specValidateClaimInterestDate(caseData, "CREATE_CLAIM_SPEC");

        // Then
        assertThat(response.getErrors()).isEmpty();
    }

}
