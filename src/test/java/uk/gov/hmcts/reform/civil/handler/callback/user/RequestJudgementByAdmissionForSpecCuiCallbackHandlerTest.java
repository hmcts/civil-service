package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.MockedStatic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.helpers.judgmentsonline.JudgmentByAdmissionOnlineMapper;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.CCJPaymentDetails;
import uk.gov.hmcts.reform.civil.model.Fee;
import uk.gov.hmcts.reform.civil.model.FixedCosts;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.RespondToClaimAdmitPartLRspec;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.CaseLocationCivil;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentDetails;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentRTLStatus;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.PartyBuilder;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.JudgementService;
import uk.gov.hmcts.reform.civil.service.robotics.mapper.AddressLinesMapper;
import uk.gov.hmcts.reform.civil.service.robotics.mapper.RoboticsAddressMapper;
import uk.gov.hmcts.reform.civil.utils.InterestCalculator;
import uk.gov.hmcts.reform.civil.utils.MonetaryConversions;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.MID;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.JUDGEMENT_BY_ADMISSION_NON_DIVERGENT_SPEC;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.REQUEST_JUDGEMENT_ADMISSION_SPEC;
import static uk.gov.hmcts.reform.civil.enums.CaseState.AWAITING_APPLICANT_INTENTION;
import static uk.gov.hmcts.reform.civil.enums.CaseState.All_FINAL_ORDERS_ISSUED;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {
    RequestJudgementByAdmissionForSpecCuiCallbackHandler.class,
    JacksonAutoConfiguration.class,
    CaseDetailsConverter.class,
    JudgementService.class,
    JudgmentByAdmissionOnlineMapper.class,
    RoboticsAddressMapper.class,
    AddressLinesMapper.class
})
public class RequestJudgementByAdmissionForSpecCuiCallbackHandlerTest extends BaseCallbackHandlerTest {

    @Autowired
    private RequestJudgementByAdmissionForSpecCuiCallbackHandler handler;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private FeatureToggleService featureToggleService;

    @Autowired
    private JudgementService judgementService;

    @MockBean
    private CaseDetailsConverter caseDetailsConverter;

    @Autowired
    RoboticsAddressMapper addressMapper;

    @Autowired
    AddressLinesMapper addressLineMapper;

    @MockBean
    private InterestCalculator interestCalculator;

    @Nested
    class AboutToStartCallback {
        @Test
        void shouldReturnError_WhenAboutToStartIsInvoked() {
            LocalDate whenWillPay = LocalDate.now().plusDays(5);
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build().toBuilder()
                .respondent1ResponseDate(LocalDateTime.now())
                .defenceAdmitPartPaymentTimeRouteRequired(RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY)
                .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION)
                .ccdState(AWAITING_APPLICANT_INTENTION)
                .respondToClaimAdmitPartLRspec(RespondToClaimAdmitPartLRspec.builder().whenWillThisAmountBePaid(whenWillPay).build())
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_START, caseData).build();
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);
            assertThat(response.getErrors()).isNotEmpty();
        }

        @Test
        void shouldNotReturnError_WhenAboutToStartIsInvokedOneDefendant() {
            LocalDate whenWillPay = LocalDate.of(2023, 10, 11);
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build().toBuilder()
                .respondent1ResponseDate(LocalDateTime.now().minusDays(15))
                .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION)
                .defenceAdmitPartPaymentTimeRouteRequired(RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY)
                .ccdState(AWAITING_APPLICANT_INTENTION)
                .respondToClaimAdmitPartLRspec(RespondToClaimAdmitPartLRspec.builder().whenWillThisAmountBePaid(whenWillPay).build())
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_START, caseData).build();
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);
            assertThat(response.getErrors()).isEmpty();
        }

        @Test
        void shouldReturnError_WhenAboutToStartIsInvokedForPAPayImmediately() {
            LocalDate whenWillPay = LocalDate.now().plusDays(5);
            when(featureToggleService.isJudgmentOnlineLive()).thenReturn(true);
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build().toBuilder()
                .defenceAdmitPartPaymentTimeRouteRequired(RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY)
                .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION)
                .ccdState(AWAITING_APPLICANT_INTENTION)
                .respondToClaimAdmitPartLRspec(RespondToClaimAdmitPartLRspec.builder().whenWillThisAmountBePaid(whenWillPay).build())
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_START, caseData).build();
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);
            assertThat(response.getErrors()).isNotEmpty();
        }

        @ParameterizedTest
        @CsvSource({"true,IMMEDIATELY", "true,BY_SET_DATE", "false,IMMEDIATELY"})
        void shouldNotReturnError_WhenAboutToStartIsInvokedForPAPayImmediately(boolean toggleState, RespondentResponsePartAdmissionPaymentTimeLRspec paymentOption) {
            LocalDate whenWillPay = LocalDate.of(2024, 11, 11);
            when(featureToggleService.isJudgmentOnlineLive()).thenReturn(toggleState);
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build().toBuilder()
                .defenceAdmitPartPaymentTimeRouteRequired(paymentOption)
                .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION)
                .ccdState(AWAITING_APPLICANT_INTENTION)
                .respondToClaimAdmitPartLRspec(RespondToClaimAdmitPartLRspec.builder().whenWillThisAmountBePaid(whenWillPay).build())
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_START, caseData).build();
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);
            assertThat(response.getErrors()).isEmpty();
        }

        @Test
        void shouldSetCcjJudgmentAmountShowInterestToNoWhenPayImmediatelyAndLrAdmissionBulkEnabled() {
            when(featureToggleService.isLrAdmissionBulkEnabled()).thenReturn(true);
            LocalDate whenWillPay = LocalDate.of(2024, 11, 11);

            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build().toBuilder()
                .respondent1ResponseDate(LocalDateTime.now())
                .defenceAdmitPartPaymentTimeRouteRequired(RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY)
                .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION)
                .ccdState(AWAITING_APPLICANT_INTENTION)
                .respondToClaimAdmitPartLRspec(RespondToClaimAdmitPartLRspec.builder().whenWillThisAmountBePaid(whenWillPay).build())
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_START, caseData).build();

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(getCaseData(response).getCcjJudgmentAmountShowInterest()).isEqualTo(YesOrNo.NO);
        }

        @Test
        void shouldNotSetCcjJudgmentAmountShowInterestToNoWhenPayImmediatelyAndLrAdmissionBulkEnabled_1v2() {
            when(featureToggleService.isLrAdmissionBulkEnabled()).thenReturn(true);
            LocalDate whenWillPay = LocalDate.of(2024, 11, 11);

            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build().toBuilder()
                .respondent1ResponseDate(LocalDateTime.now())
                .defenceAdmitPartPaymentTimeRouteRequired(RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY)
                .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION)
                .ccdState(AWAITING_APPLICANT_INTENTION)
                .respondent2(Party.builder()
                                 .type(Party.Type.INDIVIDUAL)
                                 .individualFirstName("John")
                                 .individualLastName("Doe")
                                 .build())
                .respondToClaimAdmitPartLRspec(RespondToClaimAdmitPartLRspec.builder().whenWillThisAmountBePaid(whenWillPay).build())
                .build();

            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_START, caseData).build();

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(getCaseData(response).getCcjJudgmentAmountShowInterest()).isNull();
        }

        @Test
        void shouldNotSetCcjJudgmentAmountShowInterestToNoWhenPayBySetDateAndLrAdmissionBulkEnabled() {
            when(featureToggleService.isLrAdmissionBulkEnabled()).thenReturn(true);
            when(interestCalculator.calculateInterest(any())).thenReturn(BigDecimal.valueOf(0));
            LocalDate whenWillPay = LocalDate.of(2024, 11, 11);

            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build().toBuilder()
                .respondent1ResponseDate(LocalDateTime.now())
                .defenceAdmitPartPaymentTimeRouteRequired(RespondentResponsePartAdmissionPaymentTimeLRspec.BY_SET_DATE)
                .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION)
                .ccdState(AWAITING_APPLICANT_INTENTION)
                .respondent2(Party.builder()
                                 .type(Party.Type.INDIVIDUAL)
                                 .individualFirstName("John")
                                 .individualLastName("Doe")
                                 .build())
                .respondToClaimAdmitPartLRspec(RespondToClaimAdmitPartLRspec.builder().whenWillThisAmountBePaid(whenWillPay).build())
                .build();

            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_START, caseData).build();

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(getCaseData(response).getCcjJudgmentAmountShowInterest()).isNull();
        }
    }

    @Nested
    class MidEventCallbackSetUpCcjSummaryPage {
        private static final String PAGE_ID = "set-up-ccj-amount-summary";

        @Test
        void shouldSetTheJudgmentSummaryDetailsToProceed() {
            Fee fee = Fee.builder().version("1").code("CODE").calculatedAmountInPence(BigDecimal.valueOf(100)).build();
            CCJPaymentDetails ccjPaymentDetails = CCJPaymentDetails.builder()
                .ccjPaymentPaidSomeAmount(BigDecimal.valueOf(10000))
                .ccjPaymentPaidSomeOption(YesOrNo.YES)
                .build();

            BigDecimal interestAmount = BigDecimal.valueOf(100);
            CaseData caseData = CaseDataBuilder.builder()
                .ccjPaymentDetails(ccjPaymentDetails)
                .totalClaimAmount(BigDecimal.valueOf(1000))
                .claimFee(fee)
                .totalInterest(interestAmount)
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            BigDecimal claimFee = getCaseData(response).getCcjPaymentDetails().getCcjJudgmentAmountClaimFee();
            assertThat(claimFee).isEqualTo(MonetaryConversions.penniesToPounds(fee.getCalculatedAmountInPence()));

            BigDecimal subTotal = getCaseData(response).getCcjPaymentDetails().getCcjJudgmentSummarySubtotalAmount();
            BigDecimal expectedSubTotal = getCaseData(response).getCcjPaymentDetails().getCcjJudgmentAmountClaimAmount()
                .add(caseData.getTotalInterest())
                .add(caseData.getClaimFee().toFeeDto().getCalculatedAmount());
            assertThat(subTotal).isEqualTo(expectedSubTotal);

            BigDecimal finalTotal = getCaseData(response).getCcjPaymentDetails().getCcjJudgmentTotalStillOwed();
            assertThat(finalTotal).isEqualTo(subTotal.subtract(BigDecimal.valueOf(100)));
        }

        @Test
        void shouldSetTheJudgmentSummaryDetailsToProceedWhenPartPaymentAccepted() {
            Fee fee = Fee.builder().version("1").code("CODE").calculatedAmountInPence(BigDecimal.valueOf(100)).build();
            CCJPaymentDetails ccjPaymentDetails = CCJPaymentDetails.builder()
                .ccjPaymentPaidSomeAmount(BigDecimal.valueOf(10000))
                .ccjPaymentPaidSomeOption(YesOrNo.YES)
                .build();

            BigDecimal interestAmount = BigDecimal.valueOf(100);
            CaseData caseData = CaseDataBuilder.builder()
                .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION)
                .applicant1AcceptPartAdmitPaymentPlanSpec(YES)
                .ccjPaymentDetails(ccjPaymentDetails)
                .totalClaimAmount(BigDecimal.valueOf(1000))
                .respondToAdmittedClaimOwingAmountPounds(BigDecimal.valueOf(500))
                .claimFee(fee)
                .totalInterest(interestAmount)
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            BigDecimal claimAmount = getCaseData(response).getCcjPaymentDetails().getCcjJudgmentAmountClaimAmount();
            assertThat(claimAmount).isEqualTo(BigDecimal.valueOf(500).setScale(2));

            BigDecimal claimFee = getCaseData(response).getCcjPaymentDetails().getCcjJudgmentAmountClaimFee();
            assertThat(claimFee).isEqualTo(MonetaryConversions.penniesToPounds(fee.getCalculatedAmountInPence()));

            BigDecimal subTotal = getCaseData(response).getCcjPaymentDetails().getCcjJudgmentSummarySubtotalAmount();
            BigDecimal expectedSubTotal = getCaseData(response).getCcjPaymentDetails().getCcjJudgmentAmountClaimAmount()
                .add(caseData.getTotalInterest())
                .add(caseData.getClaimFee().toFeeDto().getCalculatedAmount());
            assertThat(subTotal).isEqualTo(expectedSubTotal);

            BigDecimal finalTotal = getCaseData(response).getCcjPaymentDetails().getCcjJudgmentTotalStillOwed();
            assertThat(finalTotal).isEqualTo(subTotal.subtract(BigDecimal.valueOf(100)));
        }

        @Test
        void shouldSetTheJudgmentSummaryDetailsToProceedWithFixedCost() {
            Fee fee = Fee.builder().version("1").code("CODE").calculatedAmountInPence(BigDecimal.valueOf(100)).build();
            CCJPaymentDetails ccjPaymentDetails = CCJPaymentDetails.builder()
                .ccjPaymentPaidSomeAmount(BigDecimal.valueOf(10000))
                .ccjPaymentPaidSomeOption(YesOrNo.YES)
                .ccjJudgmentFixedCostOption(YES)
                .build();

            BigDecimal interestAmount = BigDecimal.valueOf(100);
            CaseData caseData = CaseDataBuilder.builder()
                .ccjPaymentDetails(ccjPaymentDetails)
                .totalClaimAmount(BigDecimal.valueOf(1000))
                .claimFee(fee)
                .totalInterest(interestAmount)
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            BigDecimal claimFee = getCaseData(response).getCcjPaymentDetails().getCcjJudgmentAmountClaimFee();
            assertThat(claimFee).isEqualTo(MonetaryConversions.penniesToPounds(fee.getCalculatedAmountInPence()));

            BigDecimal subTotal = getCaseData(response).getCcjPaymentDetails().getCcjJudgmentSummarySubtotalAmount();
            BigDecimal expectedSubTotal = getCaseData(response).getCcjPaymentDetails().getCcjJudgmentAmountClaimAmount()
                .add(caseData.getTotalInterest())
                .add(caseData.getClaimFee().toFeeDto().getCalculatedAmount())
                .add(BigDecimal.valueOf(40));
            BigDecimal fixedCost = getCaseData(response).getCcjPaymentDetails().getCcjJudgmentFixedCostAmount();
            BigDecimal expectedFixedCost = BigDecimal.valueOf(40);
            assertThat(subTotal).isEqualTo(expectedSubTotal);
            assertThat(fixedCost).isEqualTo(expectedFixedCost);

            BigDecimal finalTotal = getCaseData(response).getCcjPaymentDetails().getCcjJudgmentTotalStillOwed();
            assertThat(finalTotal).isEqualTo(subTotal.subtract(BigDecimal.valueOf(100)));
        }

        @Test
        void shouldSetTheJudgmentSummaryDetailsToProceedWithoutPayAmount() {
            Fee fee = Fee.builder().version("1").code("CODE").calculatedAmountInPence(BigDecimal.valueOf(100)).build();
            CCJPaymentDetails ccjPaymentDetails = CCJPaymentDetails.builder()
                .ccjPaymentPaidSomeOption(YesOrNo.NO)
                .build();

            BigDecimal interestAmount = BigDecimal.valueOf(100);
            CaseData caseData = CaseDataBuilder.builder()
                .ccjPaymentDetails(ccjPaymentDetails)
                .totalClaimAmount(BigDecimal.valueOf(1000))
                .claimFee(fee)
                .totalInterest(interestAmount)
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            BigDecimal claimFee = getCaseData(response).getCcjPaymentDetails().getCcjJudgmentAmountClaimFee();
            assertThat(claimFee).isEqualTo(MonetaryConversions.penniesToPounds(fee.getCalculatedAmountInPence()));

            BigDecimal subTotal = getCaseData(response).getCcjPaymentDetails().getCcjJudgmentSummarySubtotalAmount();
            BigDecimal expectedSubTotal = getCaseData(response).getCcjPaymentDetails().getCcjJudgmentAmountClaimAmount()
                .add(caseData.getTotalInterest())
                .add(caseData.getClaimFee().toFeeDto().getCalculatedAmount());
            assertThat(subTotal).isEqualTo(expectedSubTotal);

            BigDecimal finalTotal = getCaseData(response).getCcjPaymentDetails().getCcjJudgmentTotalStillOwed();
            assertThat(finalTotal).isEqualTo(subTotal);
        }

        @Test
        void shouldSetTheJudgmentSummaryDetailsToProceedWithoutDefendantSolicitor() {
            String expected = "The Judgement request will be reviewed by the court, this case will proceed offline, you will receive any further updates by post.";

            when(featureToggleService.isPinInPostEnabled()).thenReturn(true);

            Fee fee = Fee.builder().version("1").code("CODE").calculatedAmountInPence(BigDecimal.valueOf(100)).build();
            CCJPaymentDetails ccjPaymentDetails = CCJPaymentDetails.builder()
                .ccjPaymentPaidSomeAmount(BigDecimal.valueOf(10000))
                .ccjPaymentPaidSomeOption(YesOrNo.YES)
                .ccjJudgmentFixedCostOption(YES)
                .build();

            BigDecimal interestAmount = BigDecimal.valueOf(100);
            CaseData caseData = CaseDataBuilder.builder()
                .ccjPaymentDetails(ccjPaymentDetails)
                .totalClaimAmount(BigDecimal.valueOf(1000))
                .claimFee(fee)
                .totalInterest(interestAmount)
                .specRespondent1Represented(NO)
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            String judgementStatement = getCaseData(response).getCcjPaymentDetails().getCcjJudgmentStatement();

            assertThat(judgementStatement).isEqualTo(expected);
        }

        @Test
        void shouldNotSetTheJudgmentSummaryDetailsToProceedWithoutFlag() {
            String expected = "The Judgement request will be reviewed by the court, this case will proceed offline, you will receive any further updates by post.";

            when(featureToggleService.isPinInPostEnabled()).thenReturn(false);

            Fee fee = Fee.builder().version("1").code("CODE").calculatedAmountInPence(BigDecimal.valueOf(100)).build();
            CCJPaymentDetails ccjPaymentDetails = CCJPaymentDetails.builder()
                .ccjPaymentPaidSomeAmount(BigDecimal.valueOf(10000))
                .ccjPaymentPaidSomeOption(YesOrNo.YES)
                .ccjJudgmentFixedCostOption(YES)
                .build();

            BigDecimal interestAmount = BigDecimal.valueOf(100);
            CaseData caseData = CaseDataBuilder.builder()
                .ccjPaymentDetails(ccjPaymentDetails)
                .totalClaimAmount(BigDecimal.valueOf(1000))
                .claimFee(fee)
                .totalInterest(interestAmount)
                .specRespondent1Represented(NO)
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            String judgementStatement = getCaseData(response).getCcjPaymentDetails().getCcjJudgmentStatement();

            assertThat(judgementStatement).isNotEqualTo(expected);
        }

        @Test
        void shouldShowSummaryForAllFinalOrdersIssued() {
            String expected = "The judgment request will be processed and a County"
                + " Court Judgment (CCJ) will be issued, you will receive any further updates by email.";

            when(featureToggleService.isPinInPostEnabled()).thenReturn(true);
            when(featureToggleService.isJudgmentOnlineLive()).thenReturn(true);

            Fee fee = Fee.builder().version("1").code("CODE").calculatedAmountInPence(BigDecimal.valueOf(100)).build();
            CCJPaymentDetails ccjPaymentDetails = CCJPaymentDetails.builder()
                .ccjPaymentPaidSomeAmount(BigDecimal.valueOf(10000))
                .ccjPaymentPaidSomeOption(YesOrNo.YES)
                .ccjJudgmentFixedCostOption(YES)
                .build();

            BigDecimal interestAmount = BigDecimal.valueOf(100);

            CaseData caseData = CaseDataBuilder.builder().build().toBuilder()
                .respondent1Represented(NO)
                .specRespondent1Represented(NO)
                .applicant1Represented(YES)
                .defenceAdmitPartPaymentTimeRouteRequired(RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY)
                .defendantDetailsSpec(DynamicList.builder()
                                          .value(DynamicListElement.builder()
                                                     .label("John Doe")
                                                     .build())
                                          .build())
                .ccjPaymentDetails(ccjPaymentDetails)
                .totalClaimAmount(BigDecimal.valueOf(1000))
                .claimFee(fee)
                .totalInterest(interestAmount)
                .caseManagementLocation(CaseLocationCivil.builder().baseLocation("0123").region("0321").build())
                .build();

            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            String judgementStatement = getCaseData(response).getCcjPaymentDetails().getCcjJudgmentStatement();

            assertThat(judgementStatement).isEqualTo(expected);
        }
    }

    @Nested
    class MidEventCallbackValidateAmountPaidFlag {

        private static final String PAGE_ID = "validate-amount-paid";

        @Test
        void shouldCheckValidateAmountPaid_withErrorMessage() {
            CCJPaymentDetails ccjPaymentDetails = CCJPaymentDetails.builder()
                .ccjPaymentPaidSomeAmount(BigDecimal.valueOf(150000))
                .build();

            CaseData caseData = CaseDataBuilder.builder()
                .ccjPaymentDetails(ccjPaymentDetails)
                .totalClaimAmount(new BigDecimal(1000))
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).contains("The amount paid must be less than the full claim amount.");
        }

        @Test
        void shouldCheckValidateAmountPaid_withNoMessage() {

            CCJPaymentDetails ccjPaymentDetails = CCJPaymentDetails.builder()
                .ccjPaymentPaidSomeAmount(BigDecimal.valueOf(1500))
                .build();

            CaseData caseData = CaseDataBuilder.builder()
                .ccjPaymentDetails(ccjPaymentDetails)
                .totalClaimAmount(new BigDecimal(1000))
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isEmpty();
        }

        @Test
        void shouldCheckValidateAmountPaid_withCcJPaymentDetailsWhenNoFixedCosts() {
            Fee fee = Fee.builder().version("1").code("CODE").calculatedAmountInPence(BigDecimal.valueOf(100)).build();
            CCJPaymentDetails ccjPaymentDetails = CCJPaymentDetails.builder()
                .ccjPaymentPaidSomeAmount(BigDecimal.valueOf(1500))
                .build();

            CaseData caseData = CaseDataBuilder.builder()
                .respondent1Represented(YES)
                .specRespondent1Represented(YES)
                .applicant1Represented(YES)
                .defenceAdmitPartPaymentTimeRouteRequired(RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY)
                .ccjPaymentDetails(ccjPaymentDetails)
                .totalClaimAmount(new BigDecimal(1000))
                .claimFee(fee)
                .fixedCosts(FixedCosts.builder().claimFixedCosts(NO).build())
                .totalInterest(BigDecimal.valueOf(100))
                .build();
            when(featureToggleService.isLrAdmissionBulkEnabled()).thenReturn(true);

            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            BigDecimal claimFee = getCaseData(response).getCcjPaymentDetails().getCcjJudgmentAmountClaimFee();
            assertThat(claimFee).isEqualTo(MonetaryConversions.penniesToPounds(fee.getCalculatedAmountInPence()));

            BigDecimal subTotal = getCaseData(response).getCcjPaymentDetails().getCcjJudgmentSummarySubtotalAmount();
            BigDecimal expectedSubTotal = getCaseData(response).getCcjPaymentDetails().getCcjJudgmentAmountClaimAmount()
                    .add(caseData.getClaimFee().toFeeDto().getCalculatedAmount());
            assertThat(subTotal).isEqualTo(expectedSubTotal);

            BigDecimal finalTotal = getCaseData(response).getCcjPaymentDetails().getCcjJudgmentTotalStillOwed();
            assertThat(finalTotal).isEqualTo(subTotal);
        }

        @Test
        void shouldCheckValidateAmountPaid_withCcJPaymentDetailsWhenYesFixedCosts() {
            CCJPaymentDetails ccjPaymentDetails = CCJPaymentDetails.builder()
                .ccjPaymentPaidSomeAmount(BigDecimal.valueOf(1500))
                .build();

            CaseData caseData = CaseDataBuilder.builder()
                .respondent1Represented(YES)
                .specRespondent1Represented(YES)
                .applicant1Represented(YES)
                .defenceAdmitPartPaymentTimeRouteRequired(RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY)
                .ccjPaymentDetails(ccjPaymentDetails)
                .totalClaimAmount(new BigDecimal(1000))
                .fixedCosts(FixedCosts.builder().claimFixedCosts(YES).build())
                .build();
            when(featureToggleService.isLrAdmissionBulkEnabled()).thenReturn(true);

            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isEmpty();
        }
    }

    @Nested
    class AboutToSubmitCallbackTest {
        LocalDateTime now = LocalDate.now().atTime(12, 0, 1);

        JudgmentDetails activeJudgment = JudgmentDetails.builder()
            .judgmentId(123)
            .lastUpdateTimeStamp(now)
            .courtLocation("123456")
            .totalAmount("123")
            .orderedAmount("500")
            .costs("150")
            .claimFeeAmount("12")
            .amountAlreadyPaid("234")
            .issueDate(now.toLocalDate())
            .rtlState(JudgmentRTLStatus.ISSUED.getRtlState())
            .cancelDate(now.toLocalDate())
            .defendant1Name("Defendant 1")
            .defendant1Dob(LocalDate.of(1980, 1, 1))
            .build();

        @Test
        void shouldSetUpBusinessProcessAndContinueOfflineAndCaseState_whenIsJudgmentOnlineLiveDisabled() {
            CaseData caseData = CaseDataBuilder.builder().build().toBuilder()
                .respondent1Represented(YES)
                .specRespondent1Represented(YES)
                .applicant1Represented(YES)
                .defenceAdmitPartPaymentTimeRouteRequired(RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY)
                .defendantDetailsSpec(DynamicList.builder()
                                          .value(DynamicListElement.builder()
                                                     .label("John Doe")
                                                     .build())
                                          .build())
                .build();

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            when(caseDetailsConverter.toCaseData(params.getRequest().getCaseDetails())).thenReturn(caseData);
            when(featureToggleService.isJudgmentOnlineLive()).thenReturn(false);

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);
            assertThat(response.getState())
                .isEqualTo(CaseState.PROCEEDS_IN_HERITAGE_SYSTEM.name());
            assertThat(response.getData())
                .extracting("businessProcess")
                .extracting("camundaEvent")
                .isEqualTo(REQUEST_JUDGEMENT_ADMISSION_SPEC.name());
        }

        @Test
        void shouldSetUpBusinessProcessAndContinueOfflineAndCaseState_whenIsLRvLROneVOne() {
            CaseData caseData = CaseDataBuilder.builder().build().toBuilder()
                .respondent1Represented(YES)
                .specRespondent1Represented(YES)
                .applicant1Represented(YES)
                .defenceAdmitPartPaymentTimeRouteRequired(RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY)
                .defendantDetailsSpec(DynamicList.builder()
                                          .value(DynamicListElement.builder()
                                                     .label("John Doe")
                                                     .build())
                                          .build())
                .build();

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            when(caseDetailsConverter.toCaseData(params.getRequest().getCaseDetails())).thenReturn(caseData);
            when(featureToggleService.isJudgmentOnlineLive()).thenReturn(false);

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);
            assertThat(response.getState())
                .isEqualTo(CaseState.PROCEEDS_IN_HERITAGE_SYSTEM.name());
            assertThat(response.getData())
                .extracting("businessProcess")
                .extracting("camundaEvent")
                .isEqualTo(REQUEST_JUDGEMENT_ADMISSION_SPEC.name());
        }

        @Test
        void shouldSetUpBusinessProcessAndContinueOfflineAndCaseState_whenIsLRvLiPOneVOneAndNotPaidImmediately() {
            CaseData caseData = CaseDataBuilder.builder().build().toBuilder()
                .respondent1Represented(NO)
                .specRespondent1Represented(NO)
                .applicant1Represented(YES)
                .defenceAdmitPartPaymentTimeRouteRequired(RespondentResponsePartAdmissionPaymentTimeLRspec.BY_SET_DATE)
                .defendantDetailsSpec(DynamicList.builder()
                                          .value(DynamicListElement.builder()
                                                     .label("John Doe")
                                                     .build())
                                          .build())
                .respondent1(PartyBuilder.builder().individual().build())
                .caseManagementLocation(CaseLocationCivil.builder().baseLocation("0123").region("0321").build())
                .activeJudgment(activeJudgment)
                .build();

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            when(caseDetailsConverter.toCaseData(params.getRequest().getCaseDetails())).thenReturn(caseData);
            when(featureToggleService.isJudgmentOnlineLive()).thenReturn(true);

            AboutToStartOrSubmitCallbackResponse response;
            try (MockedStatic<LocalDateTime> mock = mockStatic(LocalDateTime.class, CALLS_REAL_METHODS)) {
                mock.when(LocalDateTime::now).thenReturn(now);
                response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            }
            assertThat(response.getState())
                .isEqualTo(CaseState.PROCEEDS_IN_HERITAGE_SYSTEM.name());
            assertThat(response.getData())
                .extracting("businessProcess")
                .extracting("camundaEvent")
                .isEqualTo(REQUEST_JUDGEMENT_ADMISSION_SPEC.name());
        }

        @Test
        void shouldSetUpBusinessProcessAndContinueOnlineAndCaseState_whenIsLRvLiP1v1AndPaidImmediately() {
            CCJPaymentDetails ccjPaymentDetails = CCJPaymentDetails.builder()
                .ccjPaymentPaidSomeOption(YesOrNo.YES)
                .ccjPaymentPaidSomeAmount(BigDecimal.valueOf(500.0))
                .ccjJudgmentLipInterest(BigDecimal.valueOf(300))
                .ccjJudgmentAmountClaimFee(BigDecimal.valueOf(0))
                .build();
            CaseData caseData = CaseDataBuilder.builder().build().toBuilder()
                .respondent1Represented(NO)
                .specRespondent1Represented(NO)
                .applicant1Represented(YES)
                .defenceAdmitPartPaymentTimeRouteRequired(RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY)
                .defendantDetailsSpec(DynamicList.builder()
                                          .value(DynamicListElement.builder()
                                                     .label("John Doe")
                                                     .build())
                                          .build())
                .ccjPaymentDetails(ccjPaymentDetails)
                .respondent1(PartyBuilder.builder().individual().build())
                .caseManagementLocation(CaseLocationCivil.builder().baseLocation("0123").region("0321").build())
                .activeJudgment(activeJudgment)
                .build();

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            when(caseDetailsConverter.toCaseData(params.getRequest().getCaseDetails())).thenReturn(caseData);
            when(featureToggleService.isJudgmentOnlineLive()).thenReturn(true);

            AboutToStartOrSubmitCallbackResponse response;
            try (MockedStatic<LocalDateTime> mock = mockStatic(LocalDateTime.class, CALLS_REAL_METHODS)) {
                mock.when(LocalDateTime::now).thenReturn(now);
                response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            }
            assertThat(response.getState())
                .isEqualTo(CaseState.All_FINAL_ORDERS_ISSUED.name());
            assertThat(response.getData())
                .extracting("businessProcess")
                .extracting("camundaEvent")
                .isEqualTo(JUDGEMENT_BY_ADMISSION_NON_DIVERGENT_SPEC.name());
            assertThat(response.getData()).extracting("activeJudgment").isNotNull();
            assertThat(response.getData().get("activeJudgment")).extracting("state").isEqualTo("ISSUED");
            assertThat(response.getData().get("activeJudgment")).extracting("type").isEqualTo("JUDGMENT_BY_ADMISSION");
            assertThat(response.getData().get("activeJudgment")).extracting("judgmentId").isEqualTo(123);
            assertThat(response.getData().get("activeJudgment")).extracting("isRegisterWithRTL").isEqualTo("Yes");
            assertThat(response.getData().get("activeJudgment")).extracting("defendant1Name").isEqualTo("Mr. John Rambo");
            assertThat(response.getData().get("activeJudgment")).extracting("defendant1Address").isNotNull();
            assertThat(response.getData().get("activeJudgment")).extracting("defendant1Dob").isNotNull();
            assertThat(response.getData().get("joJudgementByAdmissionIssueDate")).isEqualTo(now.toString());
        }

        @Test
        void shouldSetUpBusinessProcessAndContinueOfflineAndCaseState_whenIs1v2AndPaidImmediately() {
            CCJPaymentDetails ccjPaymentDetails = CCJPaymentDetails.builder()
                .ccjPaymentPaidSomeOption(YesOrNo.YES)
                .ccjPaymentPaidSomeAmount(BigDecimal.valueOf(500.0))
                .ccjJudgmentLipInterest(BigDecimal.valueOf(300))
                .ccjJudgmentAmountClaimFee(BigDecimal.valueOf(0))
                .build();
            CaseData caseData = CaseDataBuilder.builder().build().toBuilder()
                .applicant1(PartyBuilder.builder().individual().build())
                .respondent1(PartyBuilder.builder().individual().build())
                .respondent2(PartyBuilder.builder().individual().build())
                .addRespondent2(YesOrNo.YES)
                .respondent2SameLegalRepresentative(YesOrNo.YES)
                .specRespondent1Represented(YES)
                .respondent1Represented(YES)
                .applicant1Represented(YES)
                .defenceAdmitPartPaymentTimeRouteRequired(RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY)
                .defendantDetailsSpec(DynamicList.builder()
                                                  .value(DynamicListElement.builder()
                                                             .label("John Doe")
                                                             .build())
                                                  .build())
                .ccjPaymentDetails(ccjPaymentDetails)
                .caseManagementLocation(CaseLocationCivil.builder().baseLocation("0123").region("0321").build())
                .activeJudgment(activeJudgment)
                .build();

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            when(caseDetailsConverter.toCaseData(params.getRequest().getCaseDetails())).thenReturn(caseData);
            when(featureToggleService.isJudgmentOnlineLive()).thenReturn(true);

            AboutToStartOrSubmitCallbackResponse response;
            try (MockedStatic<LocalDateTime> mock = mockStatic(LocalDateTime.class, CALLS_REAL_METHODS)) {
                mock.when(LocalDateTime::now).thenReturn(now);
                response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            }

            assertThat(response.getState())
                .isEqualTo(CaseState.PROCEEDS_IN_HERITAGE_SYSTEM.name());
            assertThat(response.getData())
                .extracting("businessProcess")
                .extracting("camundaEvent")
                .isEqualTo(REQUEST_JUDGEMENT_ADMISSION_SPEC.name());
        }

        @Test
        void shouldSetUpBusinessProcessAndContinueOfflineAndCaseState_whenIs2v1AndPaidImmediately() {

            CCJPaymentDetails ccjPaymentDetails = CCJPaymentDetails.builder()
                .ccjPaymentPaidSomeOption(YesOrNo.YES)
                .ccjPaymentPaidSomeAmount(BigDecimal.valueOf(500.0))
                .ccjJudgmentLipInterest(BigDecimal.valueOf(300))
                .ccjJudgmentAmountClaimFee(BigDecimal.valueOf(0))
                .build();
            CaseData caseData = CaseDataBuilder.builder().build().toBuilder()
                .applicant1(PartyBuilder.builder().individual().build())
                .applicant2(PartyBuilder.builder().individual().build())
                .addApplicant2(YesOrNo.YES)
                .defenceAdmitPartPaymentTimeRouteRequired(RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY)
                .defendantDetailsSpec(DynamicList.builder()
                                          .value(DynamicListElement.builder()
                                                     .label("John Doe")
                                                     .build())
                                          .build())
                .ccjPaymentDetails(ccjPaymentDetails)
                .respondent1(PartyBuilder.builder().individual().build())
                .caseManagementLocation(CaseLocationCivil.builder().baseLocation("0123").region("0321").build())
                .build();

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            when(caseDetailsConverter.toCaseData(params.getRequest().getCaseDetails())).thenReturn(caseData);
            when(featureToggleService.isJudgmentOnlineLive()).thenReturn(false);

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);
            assertThat(response.getState())
                .isEqualTo(CaseState.PROCEEDS_IN_HERITAGE_SYSTEM.name());
            assertThat(response.getData())
                .extracting("businessProcess")
                .extracting("camundaEvent")
                .isEqualTo(REQUEST_JUDGEMENT_ADMISSION_SPEC.name());
        }

        @Test
        void shouldSetUpBusinessProcessAndCaseStateForLip() {
            BigDecimal subToatal = BigDecimal.valueOf(1300).setScale(2);
            BigDecimal stillOwed = new BigDecimal("1295.00");
            CCJPaymentDetails ccjPaymentDetails = CCJPaymentDetails.builder()
                .ccjPaymentPaidSomeOption(YesOrNo.YES)
                .ccjPaymentPaidSomeAmount(BigDecimal.valueOf(500.0))
                .ccjJudgmentLipInterest(BigDecimal.valueOf(300.00))
                .ccjJudgmentAmountClaimFee(BigDecimal.valueOf(0))
                .build();
            CaseData caseData = CaseData.builder()
                .respondent1Represented(YesOrNo.NO)
                .specRespondent1Represented(YesOrNo.NO)
                .applicant1Represented(YesOrNo.NO)
                .totalClaimAmount(BigDecimal.valueOf(1000))
                .ccjPaymentDetails(ccjPaymentDetails)
                .claimFee(Fee.builder()
                    .calculatedAmountInPence(BigDecimal.valueOf(0))
                    .build())
                .defendantDetailsSpec(DynamicList.builder()
                                          .value(DynamicListElement.builder()
                                                     .label("John Doe")
                                                     .build())
                                          .build())
                .build();

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            when(caseDetailsConverter.toCaseData(params.getRequest().getCaseDetails())).thenReturn(caseData);
            when(featureToggleService.isLipVLipEnabled()).thenReturn(true);
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);
            CCJPaymentDetails ccjResponseForJudgement =
                getCaseData(response).getCcjPaymentDetails();
            assertThat(response.getState())
                .isEqualTo(CaseState.PROCEEDS_IN_HERITAGE_SYSTEM.name());
            assertThat(response.getData())
                .extracting("businessProcess")
                .extracting("camundaEvent")
                .isEqualTo(REQUEST_JUDGEMENT_ADMISSION_SPEC.name());
            assertThat(ccjPaymentDetails.getCcjPaymentPaidSomeOption()).isEqualTo(ccjResponseForJudgement.getCcjPaymentPaidSomeOption());
            assertThat(MonetaryConversions.penniesToPounds(ccjPaymentDetails.getCcjPaymentPaidSomeAmount())).isEqualTo(
                ccjResponseForJudgement.getCcjPaymentPaidSomeAmountInPounds());
            assertThat(caseData.getTotalClaimAmount().setScale(2)).isEqualTo(ccjResponseForJudgement.getCcjJudgmentAmountClaimAmount());
            assertThat(subToatal).isEqualTo(ccjResponseForJudgement.getCcjJudgmentSummarySubtotalAmount());
            assertThat(stillOwed).isEqualTo(ccjResponseForJudgement.getCcjJudgmentTotalStillOwed());

        }

        @Test
        void shouldSetUpBusinessProcessAndContinueOnlineAndCaseState_whenIsJudgmentOnlineLiveEnabledLRvLR1V1() {
            CCJPaymentDetails ccjPaymentDetails = CCJPaymentDetails.builder()
                .ccjPaymentPaidSomeOption(YesOrNo.YES)
                .ccjPaymentPaidSomeAmount(BigDecimal.valueOf(500.0))
                .ccjJudgmentLipInterest(BigDecimal.valueOf(300))
                .ccjJudgmentAmountClaimFee(BigDecimal.valueOf(0))
                .build();
            CaseData caseData = CaseDataBuilder.builder().build().toBuilder()
                .respondent1Represented(YES)
                .specRespondent1Represented(YES)
                .applicant1Represented(YES)
                .defenceAdmitPartPaymentTimeRouteRequired(RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY)
                .ccjPaymentDetails(ccjPaymentDetails)
                .respondent1(PartyBuilder.builder().individual().build())
                .caseManagementLocation(CaseLocationCivil.builder().baseLocation("0123").region("0321").build())
                .activeJudgment(activeJudgment)
                .build();

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            when(caseDetailsConverter.toCaseData(params.getRequest().getCaseDetails())).thenReturn(caseData);
            when(featureToggleService.isJudgmentOnlineLive()).thenReturn(true);
            when(featureToggleService.isLrAdmissionBulkEnabled()).thenReturn(true);

            AboutToStartOrSubmitCallbackResponse response;
            try (MockedStatic<LocalDateTime> mock = mockStatic(LocalDateTime.class, CALLS_REAL_METHODS)) {
                mock.when(LocalDateTime::now).thenReturn(now);
                response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            }

            assertThat(response.getState())
                .isEqualTo(CaseState.All_FINAL_ORDERS_ISSUED.name());
            assertThat(response.getData())
                .extracting("businessProcess")
                .extracting("camundaEvent")
                .isEqualTo(JUDGEMENT_BY_ADMISSION_NON_DIVERGENT_SPEC.name());
        }

        @Test
        void shouldUpdateJudgmentTabDetailsForLipvLip() {
            CCJPaymentDetails ccjPaymentDetails = CCJPaymentDetails.builder()
                .ccjPaymentPaidSomeOption(YesOrNo.YES)
                .ccjPaymentPaidSomeAmount(BigDecimal.valueOf(500.0))
                .ccjJudgmentLipInterest(BigDecimal.valueOf(300.00))
                .ccjJudgmentAmountClaimFee(BigDecimal.valueOf(0))
                .build();
            CaseData caseData = CaseData.builder()
                .respondent1Represented(YesOrNo.NO)
                .specRespondent1Represented(YesOrNo.NO)
                .applicant1Represented(YesOrNo.NO)
                .totalClaimAmount(BigDecimal.valueOf(1000))
                .ccjPaymentDetails(ccjPaymentDetails)
                .respondent1(PartyBuilder.builder().individual().build())
                .activeJudgment(activeJudgment)
                .claimFee(Fee.builder()
                              .calculatedAmountInPence(BigDecimal.valueOf(0))
                              .build())
                .defendantDetailsSpec(DynamicList.builder()
                                          .value(DynamicListElement.builder()
                                                     .label("John Doe")
                                                     .build())
                                          .build())
                .build();

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            when(caseDetailsConverter.toCaseData(params.getRequest().getCaseDetails())).thenReturn(caseData);
            when(featureToggleService.isJudgmentOnlineLive()).thenReturn(true);
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);
            CaseData data = getCaseData(response);
            assertThat(data.getActiveJudgment()).isNotNull();
            assertThat(data.getJoRepaymentSummaryObject()).isNotNull();
        }

    }

    @Nested
    class SubmittedCallbackTest {

        @Test
        void shouldSetUpBusinessProcessAndCaseState() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build();

            CallbackParams params = callbackParamsOf(caseData, SUBMITTED);

            SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler
                .handle(params);
            assertEquals(format("# Judgment Submitted %n## A county court judgment(CCJ) has been submitted for case %s", caseData.getLegacyCaseReference()),
                         response.getConfirmationHeader());
            assertEquals("<br /><h2 class=\"govuk-heading-m\"><u>What happens next</u></h2>"
                              + "<br>This case will now proceed offline. Any updates will be sent by post.<br><br>", response.getConfirmationBody());
        }

        @Test
        void shouldSetUpBusinessProcessAndCaseStateAll_Final_Ordered_Issued() {

            CCJPaymentDetails ccjPaymentDetails = CCJPaymentDetails.builder()
                .ccjPaymentPaidSomeOption(YesOrNo.YES)
                .ccjPaymentPaidSomeAmount(BigDecimal.valueOf(500.0))
                .ccjJudgmentLipInterest(BigDecimal.valueOf(300))
                .ccjJudgmentAmountClaimFee(BigDecimal.valueOf(0))
                .build();
            CaseData caseData = CaseDataBuilder.builder().build().toBuilder()
                .respondent1Represented(NO)
                .specRespondent1Represented(NO)
                .applicant1Represented(YES)
                .defenceAdmitPartPaymentTimeRouteRequired(RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY)
                .defendantDetailsSpec(DynamicList.builder()
                                          .value(DynamicListElement.builder()
                                                     .label("John Doe")
                                                     .build())
                                          .build())
                .ccjPaymentDetails(ccjPaymentDetails)
                .caseManagementLocation(CaseLocationCivil.builder().baseLocation("0123").region("0321").build())
                .ccdState(All_FINAL_ORDERS_ISSUED)
                .build();

            CallbackParams params = callbackParamsOf(caseData, SUBMITTED);
            when(caseDetailsConverter.toCaseData(params.getRequest().getCaseDetails())).thenReturn(caseData);
            when(featureToggleService.isJudgmentOnlineLive()).thenReturn(true);

            SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler
                .handle(params);
            assertEquals(format("# Judgment Submitted %n## A county court judgment(CCJ) has been submitted for case %s",
                                caseData.getLegacyCaseReference()), response.getConfirmationHeader());
            assertThat(response.getConfirmationBody()).contains("Download county court judgment");
        }
    }

    private CaseData getCaseData(AboutToStartOrSubmitCallbackResponse response) {
        return objectMapper.convertValue(response.getData(), CaseData.class);
    }

    @Test
    void handleEventsReturnsTheExpectedCallbackEvents() {
        assertThat(handler.handledEvents()).containsOnly(REQUEST_JUDGEMENT_ADMISSION_SPEC);
    }
}
