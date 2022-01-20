package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.constants.SpecJourneyConstantLRSpec;
import uk.gov.hmcts.reform.civil.enums.AllocatedTrack;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpecPaidStatus;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.RespondToClaim;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.validation.PaymentDateValidator;
import uk.gov.hmcts.reform.civil.validation.UnavailableDateValidator;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.MID;

@ExtendWith(MockitoExtension.class)
class RespondToClaimSpecCallbackHandlerTest extends BaseCallbackHandlerTest {

    @InjectMocks
    private RespondToClaimSpecCallbackHandler handler;

    @Mock
    private PaymentDateValidator validator;
    @Mock
    private UnavailableDateValidator dateValidator;

    @BeforeEach
    public void setup() {
        ReflectionTestUtils.setField(handler, "objectMapper", new ObjectMapper().registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS));
    }

    @Nested
    class DefendAllOfClaimTests {

        @Test
        public void testNotSpecDefendantResponse() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build();
            CallbackParams params = callbackParamsOf(caseData, MID, "track");
            when(validator.validate(any())).thenReturn(List.of());

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            assertThat(response).isNotNull();
            assertThat(response.getErrors()).isNull();
            assertThat(response.getData()).isNotNull();
        }

        @Test
        public void testSpecDefendantResponseValidationError() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateRespondentFullDefenceFastTrack()
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, "track", "DEFENDANT_RESPONSE_SPEC");
            when(validator.validate(any())).thenReturn(List.of("Validation error"));

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            assertThat(response).isNotNull();
            assertThat(response.getData()).isNull();
            assertThat(response.getErrors()).isNotNull();
            assertEquals(1, response.getErrors().size());
            assertEquals("Validation error", response.getErrors().get(0));
        }

        @Test
        public void testSpecDefendantResponseFastTrack() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateRespondentFullDefenceFastTrack()
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, "track", "DEFENDANT_RESPONSE_SPEC");

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            assertThat(response).isNotNull();
            assertThat(response.getErrors()).isNull();

            assertThat(response.getData()).isNotNull();
            assertThat(response.getData().get("responseClaimTrack")).isEqualTo(AllocatedTrack.FAST_CLAIM.name());
        }

        @Test
        public void testSpecDefendantResponseFastTrackDefendantPaid() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateRespondentFullDefenceFastTrack()
                .build();

            RespondToClaim respondToClaim = RespondToClaim.builder()
                // how much was paid is pence, total claim amount is pounds
                .howMuchWasPaid(caseData.getTotalClaimAmount().multiply(BigDecimal.valueOf(100)))
                .build();

            caseData = caseData.toBuilder()
                .defenceRouteRequired(SpecJourneyConstantLRSpec.HAS_PAID_THE_AMOUNT_CLAIMED)
                .respondToClaim(respondToClaim)
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, "track", "DEFENDANT_RESPONSE_SPEC");

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            assertThat(response).isNotNull();
            assertThat(response.getErrors()).isNull();

            assertThat(response.getData()).isNotNull();
            assertThat(response.getData().get("responseClaimTrack")).isEqualTo(AllocatedTrack.FAST_CLAIM.name());
            assertThat(response.getData().get("respondent1ClaimResponsePaymentAdmissionForSpec"))
                .isEqualTo(RespondentResponseTypeSpecPaidStatus.PAID_FULL_OR_MORE_THAN_CLAIMED_AMOUNT.name());
        }

        @Test
        public void testSpecDefendantResponseFastTrackDefendantPaidLessThanClaimed() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateRespondentFullDefenceFastTrack()
                .build();

            RespondToClaim respondToClaim = RespondToClaim.builder()
                // how much was paid is pence, total claim amount is pounds
                // multiply by less than 100 so defendant paid less than claimed
                .howMuchWasPaid(caseData.getTotalClaimAmount().multiply(BigDecimal.valueOf(50)))
                .build();

            caseData = caseData.toBuilder()
                .defenceRouteRequired(SpecJourneyConstantLRSpec.HAS_PAID_THE_AMOUNT_CLAIMED)
                .respondToClaim(respondToClaim)
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, "track", "DEFENDANT_RESPONSE_SPEC");

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            assertThat(response).isNotNull();
            assertThat(response.getErrors()).isNull();

            assertThat(response.getData()).isNotNull();
            assertThat(response.getData().get("responseClaimTrack")).isEqualTo(AllocatedTrack.FAST_CLAIM.name());
            assertThat(response.getData().get("respondent1ClaimResponsePaymentAdmissionForSpec"))
                .isEqualTo(RespondentResponseTypeSpecPaidStatus.PAID_LESS_THAN_CLAIMED_AMOUNT.name());
        }
    }

    @Nested
    class AdmitsPartOfTheClaimTest {

        @Test
        public void testSpecDefendantResponseAdmitPartOfClaimValidationError() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateRespondentAdmitPartOfClaimFastTrack()
                .build();
            CallbackParams params = callbackParamsOf(
                caseData, MID, "specHandleAdmitPartClaim", "DEFENDANT_RESPONSE_SPEC");
            when(validator.validate(any())).thenReturn(List.of("Validation error"));

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            assertThat(response).isNotNull();
            assertThat(response.getData()).isNull();
            assertThat(response.getErrors()).isNotNull();
            assertEquals(1, response.getErrors().size());
            assertEquals("Validation error", response.getErrors().get(0));
        }

        @Test
        public void testSpecDefendantResponseAdmitPartOfClaimFastTrack() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateRespondentAdmitPartOfClaimFastTrack()
                .build();
            CallbackParams params = callbackParamsOf(
                caseData, MID, "specHandleAdmitPartClaim", "DEFENDANT_RESPONSE_SPEC");

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            assertThat(response).isNotNull();
            assertThat(response.getErrors()).isNull();

            assertThat(response.getData()).isNotNull();
            assertThat(response.getData().get("responseClaimTrack")).isEqualTo(AllocatedTrack.FAST_CLAIM.name());
        }

        @Test
        public void testSpecDefendantResponseAdmitPartOfClaimFastTrackStillOwes() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateRespondentAdmitPartOfClaimFastTrack()
                .build();
            // admitted amount is pence, total claimed is pounds
            BigDecimal admittedAmount = caseData.getTotalClaimAmount().multiply(BigDecimal.valueOf(50));
            caseData = caseData.toBuilder()
                .respondToAdmittedClaimOwingAmount(admittedAmount)
                .build();
            CallbackParams params = callbackParamsOf(
                caseData, MID, "specHandleAdmitPartClaim", "DEFENDANT_RESPONSE_SPEC");

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            assertThat(response).isNotNull();
            assertThat(response.getErrors()).isNull();

            assertThat(response.getData()).isNotNull();
            assertThat(response.getData().get("responseClaimTrack")).isEqualTo(AllocatedTrack.FAST_CLAIM.name());
            assertEquals(0, new BigDecimal(response.getData().get("respondToAdmittedClaimOwingAmount").toString())
                .compareTo(
                    new BigDecimal(response.getData().get("respondToAdmittedClaimOwingAmountPounds").toString())
                        .multiply(BigDecimal.valueOf(100))));
        }

        @Test
        public void testValidateLengthOfUnemploymentWithError() {
            CaseData caseData = CaseDataBuilder.builder().generateYearsAndMonthsIncorrectInput().build();
            CallbackParams params = callbackParamsOf(caseData,
                                                     MID, "validate-length-of-unemployment",
                                                     "DEFENDANT_RESPONSE_SPEC"
            );

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            List<String> expectedErrorArray = new ArrayList<>();
            expectedErrorArray.add("Length of time unemployed must be a whole number, for example, 10.");

            assertThat(response).isNotNull();
            assertThat(response.getErrors()).isEqualTo(expectedErrorArray);
        }

        @Test
        public void testValidateRespondentPaymentDate() {
            CaseData caseData = CaseDataBuilder.builder().generatePaymentDateForAdmitPartResponse().build();
            CallbackParams params = callbackParamsOf(caseData, MID, "validate-payment-date",
                                                     "DEFENDANT_RESPONSE_SPEC"
            );
            when(validator.validate(any())).thenReturn(List.of("Validation error"));

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            List<String> expectedErrorArray = new ArrayList<>();
            expectedErrorArray.add("Date for when will the amount be paid must be today or in the future.");

            assertThat(response).isNotNull();
            /*
             * It was not possible to capture the error message generated by @FutureOrPresent in the class
             * */
            //assertThat(response.getErrors()).isEqualTo(expectedErrorArray);
            assertEquals("Validation error", response.getErrors().get(0));
        }

        @Test
        public void testValidateRepaymentDate() {
            CaseData caseData = CaseDataBuilder.builder().generateRepaymentDateForAdmitPartResponse().build();
            CallbackParams params = callbackParamsOf(caseData, MID,
                                                     "validate-repayment-plan", "DEFENDANT_RESPONSE_SPEC"
            );
            when(dateValidator.validateFuturePaymentDate(any())).thenReturn(List.of("Validation error"));

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            assertEquals("Validation error", response.getErrors().get(0));

        }

    }
}
