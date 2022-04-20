package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.constants.SpecJourneyConstantLRSpec;
import uk.gov.hmcts.reform.civil.enums.AllocatedTrack;
import uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpecPaidStatus;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.handler.callback.user.spec.RespondToClaimConfirmationTextSpecGenerator;
import uk.gov.hmcts.reform.civil.handler.callback.user.spec.response.confirmation.FullAdmitAlreadyPaidConfirmationText;
import uk.gov.hmcts.reform.civil.handler.callback.user.spec.response.confirmation.FullAdmitSetDateConfirmationText;
import uk.gov.hmcts.reform.civil.handler.callback.user.spec.response.confirmation.PartialAdmitPaidFullConfirmationText;
import uk.gov.hmcts.reform.civil.handler.callback.user.spec.response.confirmation.PartialAdmitPaidLessConfirmationText;
import uk.gov.hmcts.reform.civil.handler.callback.user.spec.response.confirmation.PartialAdmitPayImmediatelyConfirmationText;
import uk.gov.hmcts.reform.civil.handler.callback.user.spec.response.confirmation.PartialAdmitSetDateConfirmationText;
import uk.gov.hmcts.reform.civil.handler.callback.user.spec.response.confirmation.RepayPlanConfirmationText;
import uk.gov.hmcts.reform.civil.helpers.DateFormatHelper;
import uk.gov.hmcts.reform.civil.helpers.DateFormatHelper;
import uk.gov.hmcts.reform.civil.launchdarkly.FeatureToggleService;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.RespondToClaim;
import uk.gov.hmcts.reform.civil.model.RespondToClaimAdmitPartLRspec;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.PartyBuilder;
import uk.gov.hmcts.reform.civil.utils.MonetaryConversions;
import uk.gov.hmcts.reform.civil.validation.PaymentDateValidator;
import uk.gov.hmcts.reform.civil.validation.UnavailableDateValidator;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.MID;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.formatLocalDateTime;

@ExtendWith(MockitoExtension.class)
class RespondToClaimSpecCallbackHandlerTest extends BaseCallbackHandlerTest {

    @InjectMocks
    private RespondToClaimSpecCallbackHandler handler;

    @Mock
    private PaymentDateValidator validator;
    @Mock
    private UnavailableDateValidator dateValidator;
    @Mock
    private FeatureToggleService toggleService;
    @Spy
    private List<RespondToClaimConfirmationTextSpecGenerator> confirmationTextGenerators = List.of(
        new FullAdmitAlreadyPaidConfirmationText(),
        new FullAdmitSetDateConfirmationText(),
        new PartialAdmitPaidFullConfirmationText(),
        new PartialAdmitPaidLessConfirmationText(),
        new PartialAdmitPayImmediatelyConfirmationText(),
        new PartialAdmitSetDateConfirmationText(),
        new RepayPlanConfirmationText()
    );

    @BeforeEach
    public void setup() {
        ReflectionTestUtils.setField(handler, "objectMapper", new ObjectMapper().registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS));
    }

    @Test
    public void ldBlock() {
        Mockito.when(toggleService.isLrSpecEnabled()).thenReturn(false, true);
        Assertions.assertTrue(handler.handledEvents().isEmpty());
        Assertions.assertFalse(handler.handledEvents().isEmpty());
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

    @Nested
    class SubmittedCallback {
        @Test
        void shouldReturnExpectedResponse_whenApplicantIsProceedingWithClaim() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateApplicantRespondToDefenceAndProceed()
                .build();
            CallbackParams params = callbackParamsOf(caseData, SUBMITTED);

            SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);

            LocalDateTime responseDeadline = caseData.getApplicant1ResponseDeadline();
            String claimNumber = caseData.getLegacyCaseReference();

            String body = format(
                "<h2 class=\"govuk-heading-m\">What happens next</h2>"
                    + "%n%nThe claimant has until 4pm on %s to respond to your claim. "
                    + "We will let you know when they respond."
                    + "%n%n<a href=\"%s\" target=\"_blank\">Download questionnaire (opens in a new tab)</a>",
                formatLocalDateTime(responseDeadline, DATE),
                format("/cases/case-details/%s#Claim documents", caseData.getCcdCaseReference())
            );

            assertThat(response).usingRecursiveComparison().isEqualTo(
                SubmittedCallbackResponse.builder()
                    .confirmationHeader(format("# You've submitted your response%n## Claim number: %s", claimNumber))
                    .confirmationBody(body)
                    .build());
        }

        @Test
        void specificSummary_whenPartialAdmitNotPay() {
            BigDecimal admitted = BigDecimal.valueOf(1000);
            LocalDate whenWillPay = LocalDate.now().plusMonths(1);
            CaseData caseData = CaseDataBuilder.builder()
                .atStateApplicantRespondToDefenceAndProceed()
                .build().toBuilder()
                .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION)
                .defenceAdmitPartPaymentTimeRouteRequired(RespondentResponsePartAdmissionPaymentTimeLRspec.BY_SET_DATE)
                .respondToAdmittedClaimOwingAmountPounds(admitted)
                .respondToClaimAdmitPartLRspec(
                    RespondToClaimAdmitPartLRspec.builder()
                        .whenWillThisAmountBePaid(whenWillPay)
                        .build()
                )
                .totalClaimAmount(admitted.multiply(BigDecimal.valueOf(2)))
                .build();
            CallbackParams params = callbackParamsOf(caseData, SUBMITTED);

            SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);

            LocalDateTime responseDeadline = caseData.getApplicant1ResponseDeadline();
            String claimNumber = caseData.getLegacyCaseReference();

            assertThat(response.getConfirmationBody())
                .contains(caseData.getApplicant1().getPartyName())
                .contains(admitted.toString())
                .contains(caseData.getTotalClaimAmount().toString())
                .contains(DateFormatHelper.formatLocalDate(whenWillPay, DATE));
        }

        @Test
        void specificSummary_whenPartialAdmitPayImmediately() {
            BigDecimal admitted = BigDecimal.valueOf(1000);
            LocalDate whenWillPay = LocalDate.now().plusDays(5);
            CaseData caseData = CaseDataBuilder.builder()
                .atStateApplicantRespondToDefenceAndProceed()
                .build().toBuilder()
                .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION)
                .defenceAdmitPartPaymentTimeRouteRequired(RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY)
                .respondToAdmittedClaimOwingAmountPounds(admitted)
                .build();
            CallbackParams params = callbackParamsOf(caseData, SUBMITTED);

            SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);

            assertThat(response.getConfirmationBody())
                .contains(caseData.getApplicant1().getPartyName())
                .contains(DateFormatHelper.formatLocalDate(whenWillPay, DATE));
        }

        @Test
        void specificSummary_whenRepayPlanFullAdmit() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateApplicantRespondToDefenceAndProceed()
                .build().toBuilder()
                .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION)
                .specDefenceFullAdmittedRequired(YesOrNo.NO)
                .defenceAdmitPartPaymentTimeRouteRequired(
                    RespondentResponsePartAdmissionPaymentTimeLRspec.SUGGESTION_OF_REPAYMENT_PLAN)
                .build();
            CallbackParams params = callbackParamsOf(caseData, SUBMITTED);

            SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);

            assertThat(response.getConfirmationBody())
                .contains(caseData.getApplicant1().getPartyName())
                .contains("repayment plan");
        }

        @Test
        void specificSummary_whenRepayPlanPartialAdmit() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateApplicantRespondToDefenceAndProceed()
                .build().toBuilder()
                .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION)
                .specDefenceFullAdmittedRequired(YesOrNo.NO)
                .defenceAdmitPartPaymentTimeRouteRequired(
                    RespondentResponsePartAdmissionPaymentTimeLRspec.SUGGESTION_OF_REPAYMENT_PLAN)
                .build();
            CallbackParams params = callbackParamsOf(caseData, SUBMITTED);

            SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);

            assertThat(response.getConfirmationBody())
                .contains(caseData.getApplicant1().getPartyName())
                .contains("repayment plan");
        }

        @Test
        void specificSummary_whenFullAdmitAlreadyPaid() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateApplicantRespondToDefenceAndProceed()
                .build().toBuilder()
                .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION)
                .specDefenceFullAdmittedRequired(YesOrNo.YES)
                .totalClaimAmount(BigDecimal.valueOf(1000))
                .build();
            CallbackParams params = callbackParamsOf(caseData, SUBMITTED);

            SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);

            assertThat(response.getConfirmationBody())
                .contains(caseData.getApplicant1().getPartyName())
                .contains(caseData.getTotalClaimAmount().toString())
                .contains("you've paid");
        }

        @Test
        void specificSummary_whenFullAdmitBySetDate() {
            LocalDate whenWillPay = LocalDate.now().plusDays(5);
            CaseData caseData = CaseDataBuilder.builder()
                .atStateApplicantRespondToDefenceAndProceed()
                .build().toBuilder()
                .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION)
                .specDefenceFullAdmittedRequired(YesOrNo.NO)
                .totalClaimAmount(BigDecimal.valueOf(1000))
                .defenceAdmitPartPaymentTimeRouteRequired(
                    RespondentResponsePartAdmissionPaymentTimeLRspec.BY_SET_DATE)
                .respondToClaimAdmitPartLRspec(
                    RespondToClaimAdmitPartLRspec.builder()
                        .whenWillThisAmountBePaid(whenWillPay)
                        .build()
                )
                .build();
            CallbackParams params = callbackParamsOf(caseData, SUBMITTED);

            SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);

            assertThat(response.getConfirmationBody())
                .contains(caseData.getApplicant1().getPartyName())
                .contains(" and your explanation of why you cannot pay before then.")
                .contains(DateFormatHelper.formatLocalDate(whenWillPay, DATE))
                .doesNotContain(caseData.getTotalClaimAmount().toString());

        }

        @Test
        void specificSummary_whenPartialAdmitPaidFull() {
            BigDecimal totalClaimAmount = BigDecimal.valueOf(1000);
            BigDecimal howMuchWasPaid = new BigDecimal(MonetaryConversions.poundsToPennies(totalClaimAmount));
            CaseData caseData = CaseDataBuilder.builder()
                .atStateApplicantRespondToDefenceAndProceed()
                .build().toBuilder()
                .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION)
                .specDefenceAdmittedRequired(YesOrNo.YES)
                .respondToAdmittedClaim(RespondToClaim.builder().howMuchWasPaid(howMuchWasPaid).build())
                .totalClaimAmount(totalClaimAmount)
                .build();
            CallbackParams params = callbackParamsOf(caseData, SUBMITTED);

            SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);

            assertThat(response.getConfirmationBody())
                .contains(caseData.getApplicant1().getPartyName())
                .contains(caseData.getTotalClaimAmount().toString());
        }

        @Test
        void shouldReturnExpectedResponse_when1v2SameSolicitorDivergentResponseFullDefenceFullAdmission() {
            CaseData caseData = CaseDataBuilder.builder()
                .atState1v2SameSolicitorDivergentResponseSpec(RespondentResponseTypeSpec.FULL_DEFENCE,
                                                              RespondentResponseTypeSpec.FULL_ADMISSION)
                .respondent2(PartyBuilder.builder().individual().build())
                .respondent2SameLegalRepresentative(YES)
                .build();
            CallbackParams params = callbackParamsOf(caseData, SUBMITTED);

            SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);

            String claimNumber = caseData.getLegacyCaseReference();

            StringBuilder body = new StringBuilder();
            body.append("<br>The defendants have chosen different responses and the claim cannot continue online.")
                .append("<br>Use form N9A to admit, or form N9B to counterclaim. Do not create a new claim to "
                            + "counterclaim.")
                .append(String.format("%n%n<a href=\"%s\" target=\"_blank\">Download form N9A (opens in a new tab)</a>",
                                      format("https://www.gov.uk/respond-money-claim")))
                .append(String.format("<br><a href=\"%s\" target=\"_blank\">Download form N9B (opens in a new tab)</a>",
                                      format("https://www.gov.uk/respond-money-claim")))
                .append("<br><br>Post the completed form to:")
                .append("<br><br>County Court Business Centre<br>St. Katherine's House")
                .append("<br>21-27 St.Katherine Street<br>Northampton<br>NN1 2LH");
            assertThat(response).usingRecursiveComparison().isEqualTo(
                SubmittedCallbackResponse.builder()
                    .confirmationHeader(format("# The defendants have chosen their responses%n## Claim number <br>%s",
                                                claimNumber))
                    .confirmationBody(body.toString())
                    .build());
        }

        @Test
        void shouldReturnExpectedResponse_when1v2SameSolicitorDivergentResponsePartAdmissionFullAdmission() {
            CaseData caseData = CaseDataBuilder.builder()
                .atState1v2SameSolicitorDivergentResponseSpec(RespondentResponseTypeSpec.PART_ADMISSION,
                                                              RespondentResponseTypeSpec.FULL_ADMISSION)
                .respondent2(PartyBuilder.builder().individual().build())
                .respondent2SameLegalRepresentative(YES)
                .build();
            CallbackParams params = callbackParamsOf(caseData, SUBMITTED);

            SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);

            String claimNumber = caseData.getLegacyCaseReference();

            StringBuilder body = new StringBuilder();
            body.append("<br>The defendants have chosen different responses and the claim cannot continue online.")
                .append("<br>Use form N9A to admit, or form N9B to counterclaim. Do not create a new claim to "
                            + "counterclaim.")
                .append(String.format("%n%n<a href=\"%s\" target=\"_blank\">Download form N9A (opens in a new tab)</a>",
                                      format("https://www.gov.uk/respond-money-claim")))
                .append(String.format("<br><a href=\"%s\" target=\"_blank\">Download form N9B (opens in a new tab)</a>",
                                      format("https://www.gov.uk/respond-money-claim")))
                .append("<br><br>Post the completed form to:")
                .append("<br><br>County Court Business Centre<br>St. Katherine's House")
                .append("<br>21-27 St.Katherine Street<br>Northampton<br>NN1 2LH");
            assertThat(response).usingRecursiveComparison().isEqualTo(
                SubmittedCallbackResponse.builder()
                    .confirmationHeader(format("# The defendants have chosen their responses%n## Claim number <br>%s",
                                                claimNumber))
                    .confirmationBody(body.toString())
                    .build());
        }

        @Test
        void shouldReturnExpectedResponse_when1v2SameSolicitorDivergentResponseCounterClaimFullAdmission() {
            CaseData caseData = CaseDataBuilder.builder()
                .atState1v2SameSolicitorDivergentResponseSpec(RespondentResponseTypeSpec.COUNTER_CLAIM,
                                                              RespondentResponseTypeSpec.FULL_ADMISSION)
                .respondent2(PartyBuilder.builder().individual().build())
                .respondent2SameLegalRepresentative(YES)
                .build();
            CallbackParams params = callbackParamsOf(caseData, SUBMITTED);

            SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);

            String claimNumber = caseData.getLegacyCaseReference();

            StringBuilder body = new StringBuilder();
            body.append("<br>The defendants have chosen different responses and the claim cannot continue online.")
                .append("<br>Use form N9A to admit, or form N9B to counterclaim. Do not create a new claim to "
                            + "counterclaim.")
                .append(String.format("%n%n<a href=\"%s\" target=\"_blank\">Download form N9A (opens in a new tab)</a>",
                                      format("https://www.gov.uk/respond-money-claim")))
                .append(String.format("<br><a href=\"%s\" target=\"_blank\">Download form N9B (opens in a new tab)</a>",
                                      format("https://www.gov.uk/respond-money-claim")))
                .append("<br><br>Post the completed form to:")
                .append("<br><br>County Court Business Centre<br>St. Katherine's House")
                .append("<br>21-27 St.Katherine Street<br>Northampton<br>NN1 2LH");
            assertThat(response).usingRecursiveComparison().isEqualTo(
                SubmittedCallbackResponse.builder()
                    .confirmationHeader(format("# The defendants have chosen their responses%n## Claim number <br>%s",
                                               claimNumber))
                    .confirmationBody(body.toString())
                    .build());
        }

        @Test
        void shouldReturnExpectedResponse_when1v2SameSolicitorDivergentResponseCounterClaimPartAdmission() {
            CaseData caseData = CaseDataBuilder.builder()
                .atState1v2SameSolicitorDivergentResponseSpec(RespondentResponseTypeSpec.COUNTER_CLAIM,
                                                              RespondentResponseTypeSpec.PART_ADMISSION)
                .respondent2(PartyBuilder.builder().individual().build())
                .respondent2SameLegalRepresentative(YES)
                .build();
            CallbackParams params = callbackParamsOf(caseData, SUBMITTED);

            SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);

            String claimNumber = caseData.getLegacyCaseReference();

            StringBuilder body = new StringBuilder();
            body.append("<br>The defendants have chosen different responses and the claim cannot continue online.")
                .append("<br>Use form N9A to admit, or form N9B to counterclaim. Do not create a new claim to "
                            + "counterclaim.")
                .append(String.format("%n%n<a href=\"%s\" target=\"_blank\">Download form N9A (opens in a new tab)</a>",
                                      format("https://www.gov.uk/respond-money-claim")))
                .append(String.format("<br><a href=\"%s\" target=\"_blank\">Download form N9B (opens in a new tab)</a>",
                                      format("https://www.gov.uk/respond-money-claim")))
                .append("<br><br>Post the completed form to:")
                .append("<br><br>County Court Business Centre<br>St. Katherine's House")
                .append("<br>21-27 St.Katherine Street<br>Northampton<br>NN1 2LH");
            assertThat(response).usingRecursiveComparison().isEqualTo(
                SubmittedCallbackResponse.builder()
                    .confirmationHeader(format("# The defendants have chosen their responses%n## Claim number <br>%s",
                                               claimNumber))
                    .confirmationBody(body.toString())
                    .build());
        }
    }

    @Test
    void specificSummary_whenPartialAdmitPaidLess() {
        BigDecimal howMuchWasPaid = BigDecimal.valueOf(1000);
        BigDecimal totalClaimAmount = BigDecimal.valueOf(10000);
        CaseData caseData = CaseDataBuilder.builder()
            .atStateApplicantRespondToDefenceAndProceed()
            .build().toBuilder()
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION)
            .specDefenceAdmittedRequired(YesOrNo.YES)
            .respondToAdmittedClaim(RespondToClaim.builder().howMuchWasPaid(howMuchWasPaid).build())
            .totalClaimAmount(totalClaimAmount)
            .build();
        CallbackParams params = callbackParamsOf(caseData, SUBMITTED);

        SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);

        assertThat(response.getConfirmationBody())
            .contains(caseData.getApplicant1().getPartyName())
            .contains("The claim will be settled. We'll contact you when they respond.")
            .contains(MonetaryConversions.penniesToPounds(caseData.getRespondToAdmittedClaim().getHowMuchWasPaid())
                          .toString());
    }

    @Nested
    class MidEventSetGenericResponseTypeFlagCallback {

        private static final String PAGE_ID = "set-generic-response-type-flag";

        @Test
        void shouldSetMultiPartyResponseTypeFlags_Counter_Admit_OR_Admit_Part_combination1() {
            CaseData caseData = CaseDataBuilder.builder().atStateRespondent2v1BothNotFullDefence_PartAdmissionX2()
                .build();

            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData()).extracting("multiPartyResponseTypeFlags")
                .isEqualTo("COUNTER_ADMIT_OR_ADMIT_PART");
        }

        @Test
        void shouldSetMultiPartyResponseTypeFlags_Counter_Admit_OR_Admit_Part_combination2() {
            CaseData caseData = CaseDataBuilder.builder().atStateRespondent2v1BothNotFullDefence_CounterClaimX2()
                .build();

            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData()).extracting("multiPartyResponseTypeFlags")
                .isEqualTo("COUNTER_ADMIT_OR_ADMIT_PART");
        }

        @Test
        void shouldSetMultiPartyResponseTypeFlags_1v2_sameSolicitor_DifferentResponse() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateRespondentFullDefenceSpec_1v2_BothPartiesFullDefenceResponses()
                .addRespondent2(YES)
                .respondent2SameLegalRepresentative(YES)
                .respondent2(PartyBuilder.builder().individual().build())
                .respondentResponseIsSame(NO)
                .build();

            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData()).extracting("multiPartyResponseTypeFlags")
                .isEqualTo("FULL_DEFENCE");
            assertThat(response.getData()).extracting("sameSolicitorSameResponse")
                .isEqualTo("No");
        }

        @Test
        void shouldSetMultiPartyResponseTypeFlags_AdmitAll_OR_Admit_Part_1v2() {
            CaseData caseData = CaseDataBuilder.builder().atStateRespondent1v2AdmitAll_AdmitPart()
                .addRespondent2(YES)
                .respondent2SameLegalRepresentative(YES)
                .respondent2(PartyBuilder.builder().individual().build())
                .respondentResponseIsSame(NO)
                .build();

            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData()).extracting("multiPartyResponseTypeFlags")
                .isEqualTo("COUNTER_ADMIT_OR_ADMIT_PART");
        }

        @Test
        void shouldSetMultiPartyResponseTypeFlags_FullDefence_OR_AdmitAll_1v2() {
            CaseData caseData = CaseDataBuilder.builder().atStateRespondent1v2FullDefence_AdmitPart()
                .addRespondent2(YES)
                .respondent2SameLegalRepresentative(YES)
                .respondent2(PartyBuilder.builder().individual().build())
                .respondentResponseIsSame(NO)
                .build();

            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData()).extracting("multiPartyResponseTypeFlags")
                .isEqualTo("FULL_DEFENCE");
        }
    }
}
