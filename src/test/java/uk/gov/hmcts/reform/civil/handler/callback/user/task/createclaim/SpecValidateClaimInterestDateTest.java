package uk.gov.hmcts.reform.civil.handler.callback.user.task.createclaim;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.createclaim.SpecValidateClaimInterestDateTask;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class SpecValidateClaimInterestDateTest extends BaseCallbackHandlerTest {

    @InjectMocks
    private SpecValidateClaimInterestDateTask specValidateClaimInterestDateTask;

    @Test
    void shouldReturnErrorForFutureInterestDate() {
        LocalDate futureDate = LocalDate.now().plusDays(1);
        CaseData caseData = CaseData.builder()
            .interestFromSpecificDate(futureDate)
            .build();

        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) specValidateClaimInterestDateTask
            .specValidateClaimInterestDate(caseData, "CREATE_CLAIM_SPEC");

        assertThat(response.getErrors()).contains("Correct the date. You can’t use a future date.");
    }

    @Test
    void shouldNotReturnErrorForPastOrTodayInterestDate() {
        LocalDate pastDate = LocalDate.now().minusDays(1);
        CaseData caseData = CaseData.builder()
            .interestFromSpecificDate(pastDate)
            .build();

        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) specValidateClaimInterestDateTask
            .specValidateClaimInterestDate(caseData, "CREATE_CLAIM_SPEC");

        assertThat(response.getErrors()).isEmpty();
    }

}
