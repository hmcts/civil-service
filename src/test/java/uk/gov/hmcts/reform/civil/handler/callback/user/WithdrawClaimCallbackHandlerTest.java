package uk.gov.hmcts.reform.civil.handler.callback.user;

import org.hibernate.validator.messageinterpolation.ParameterMessageInterpolator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.CloseClaim;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDetailsBuilder;

import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.MID;

@ExtendWith(MockitoExtension.class)
class WithdrawClaimCallbackHandlerTest extends BaseCallbackHandlerTest {

    private WithdrawClaimCallbackHandler handler;

    @BeforeEach
    void setUp() {
        ValidatorFactory validatorFactory = Validation.byDefaultProvider()
            .configure()
            .messageInterpolator(new ParameterMessageInterpolator())
            .buildValidatorFactory();

        Validator validator = validatorFactory.getValidator();
        handler = new WithdrawClaimCallbackHandler(validator);
    }

    @Nested
    class AboutToStartCallback {

        @Test
        void shouldReturnNoError_WhenAboutToStartIsInvoked() {
            CaseDetails caseDetails = CaseDetailsBuilder.builder().atStateAwaitingRespondentAcknowledgement().build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_START, caseDetails).build();

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            assertThat(response.getErrors()).isNull();
        }
    }

    @Nested
    class MidEventWithdrawalReasonCallback {

        private static final String PAGE_ID = "withdrawal-reason";

        @Test
        void shouldReturnErrors_whenDateInFuture() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified()
                .withdrawClaim(CloseClaim.builder().date(LocalDate.now().plusDays(1)).build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            AboutToStartOrSubmitCallbackResponse response =
                (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).containsOnly("The date must not be in the future");
        }

        @Test
        void shouldReturnNoErrors_whenDateInPast() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified()
                .discontinueClaim(CloseClaim.builder().date(LocalDate.now().minusDays(1)).build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            AboutToStartOrSubmitCallbackResponse response =
                (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isEmpty();
        }
    }
}
