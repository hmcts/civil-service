package uk.gov.hmcts.reform.civil.handler.callback.user;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.DJPaymentTypeSelection;
import uk.gov.hmcts.reform.civil.enums.FeeType;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.helpers.judgmentsonline.DefaultJudgmentOnlineMapper;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Fee;
import uk.gov.hmcts.reform.civil.model.breathing.BreathingSpaceEnterInfo;
import uk.gov.hmcts.reform.civil.model.breathing.BreathingSpaceInfo;
import uk.gov.hmcts.reform.civil.model.breathing.BreathingSpaceLiftInfo;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.HelpWithFeesDetails;
import uk.gov.hmcts.reform.civil.model.citizenui.RespondentLiPResponse;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.CaseLocationCivil;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.PartyBuilder;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.FeesService;
import uk.gov.hmcts.reform.civil.service.Time;
import uk.gov.hmcts.reform.civil.utils.InterestCalculator;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.MID;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CallbackVersion.V_1;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.DEFAULT_JUDGEMENT_NON_DIVERGENT_SPEC;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.DEFAULT_JUDGEMENT_SPEC;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.handler.callback.user.DefaultJudgementSpecHandler.JUDGMENT_GRANTED_HEADER;
import static uk.gov.hmcts.reform.civil.handler.callback.user.DefaultJudgementSpecHandler.JUDGMENT_REQUESTED_HEADER;
import static uk.gov.hmcts.reform.civil.handler.callback.user.DefaultJudgementSpecHandler.JUDGMENT_REQUESTED_LIP_CASE;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.formatLocalDate;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {
    DefaultJudgementSpecHandler.class,
    JacksonAutoConfiguration.class,
    ValidationAutoConfiguration.class,
    CaseDetailsConverter.class,
    InterestCalculator.class,
    FeesService.class,
    DefaultJudgmentOnlineMapper.class
})
public class DefaultJudgementSpecHandlerTest extends BaseCallbackHandlerTest {

    @Autowired
    private DefaultJudgementSpecHandler handler;

    @MockBean
    private FeesService feesService;

    @MockBean
    private InterestCalculator interestCalculator;

    @MockBean
    private Time time;

    @MockBean
    private FeatureToggleService featureToggleService;

    @Nested
    class AboutToStartCallback {

        @Test
        void shouldReturnError_WhenAboutToStartIsInvoked() {
            BreathingSpaceLiftInfo breathingSpaceLiftInfo = BreathingSpaceLiftInfo.builder()
                .expectedEnd(LocalDate.now().minusDays(5))
                .build();
            BreathingSpaceInfo breathingSpaceInfo = BreathingSpaceInfo.builder()
                .lift(breathingSpaceLiftInfo)
                .build();
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build().toBuilder()
                .breathing(breathingSpaceInfo)
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_START, caseData).build();
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);
            assertThat(response.getErrors()).isNotEmpty();
        }

        @Test
        void shouldNotReturnError_WhenAboutToStartIsInvokedOneDefendant() {
            BreathingSpaceLiftInfo breathingSpaceLiftInfo = BreathingSpaceLiftInfo.builder()
                .expectedEnd(LocalDate.now().minusDays(5))
                .build();
            BreathingSpaceInfo breathingSpaceInfo = BreathingSpaceInfo.builder()
                .lift(breathingSpaceLiftInfo)
                .build();
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build().toBuilder()
                .breathing(breathingSpaceInfo)
                .respondent1ResponseDeadline(LocalDateTime.now().minusDays(15)).build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_START, caseData).build();
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);
            assertThat(response.getErrors()).isEmpty();
        }

        @Test
        void shouldReturnDefendantDetails_WhenAboutToStartIsInvokedOneDefendant() {
            BreathingSpaceLiftInfo breathingSpaceLiftInfo = BreathingSpaceLiftInfo.builder()
                .expectedEnd(LocalDate.now().minusDays(5))
                .build();
            BreathingSpaceInfo breathingSpaceInfo = BreathingSpaceInfo.builder()
                .lift(breathingSpaceLiftInfo)
                .build();
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build().toBuilder()
                .breathing(breathingSpaceInfo)
                .respondent1ResponseDeadline(LocalDateTime.now().minusDays(15)).build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_START, caseData).build();
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);
            assertThat(response.getData().get("defendantDetailsSpec")).isNotNull();
        }

        @Test
        void shouldReturnDefendantDetails_WhenAboutToStartIsInvokedTwoDefendant() {
            BreathingSpaceLiftInfo breathingSpaceLiftInfo = BreathingSpaceLiftInfo.builder()
                .expectedEnd(LocalDate.now().minusDays(5))
                .build();
            BreathingSpaceInfo breathingSpaceInfo = BreathingSpaceInfo.builder()
                .lift(breathingSpaceLiftInfo)
                .build();
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build().toBuilder()
                .breathing(breathingSpaceInfo)
                .respondent2(PartyBuilder.builder().individual().build())
                .addRespondent2(YES)
                .respondent2SameLegalRepresentative(YES)
                .respondent1ResponseDeadline(LocalDateTime.now().minusDays(15)).build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_START, caseData).build();
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);
            assertThat(response.getData().get("defendantDetailsSpec")).isNotNull();
        }

        @Test
        void shouldReturnError_WhenAboutToStartAndInBreathingSpace() {
            BreathingSpaceEnterInfo breathingSpaceEnterInfo = BreathingSpaceEnterInfo.builder()
                .start(LocalDate.now().minusDays(10))
                .build();
            BreathingSpaceInfo breathingSpaceInfo = BreathingSpaceInfo.builder()
                .enter(breathingSpaceEnterInfo)
                .build();

            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build().toBuilder()
                .breathing(breathingSpaceInfo)
                .build();

            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_START, caseData).build();
            var response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);
            assertThat(response.getErrors().contains("Default judgment cannot be applied for while claim is "
                                                         + "in breathing space"));
        }

        @Test
        void shouldReturnError_WhenAboutToStartInvokeWhenRespondentResponseLanguageIsBilingual() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build().toBuilder()
                .breathing(BreathingSpaceInfo.builder().lift(null).build())
                .caseDataLiP(CaseDataLiP.builder().respondent1LiPResponse(RespondentLiPResponse.builder().respondent1ResponseLanguage("BOTH").build()).build())
                .build();

            when(featureToggleService.isPinInPostEnabled()).thenReturn(true);
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_START, caseData).build();
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            assertThat(response.getErrors()).contains("The Claim is not eligible for Default Judgment.");
        }

    }

    @Nested
    class MidEventShowCPRAcceptCallback {

        private static final String PAGE_ID = "acceptCPRSpec";

        @Test
        void shouldReturnError_whenCPRisNotAccepted() {
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response.getErrors()).isNotNull();
        }
    }

    @Nested
    class MidEventShowCertifyConditionCallback {

        private static final String PAGE_ID = "showCertifyStatementSpec";

        @Test
        void shouldReturnBoth_whenHaveTwoDefendants() {
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .respondent2(PartyBuilder.builder().individual().build())
                .addRespondent2(YES)
                .respondent2SameLegalRepresentative(YES)
                .respondent1ResponseDeadline(LocalDateTime.now().minusDays(15))
                .defendantDetailsSpec(DynamicList.builder()
                                          .value(DynamicListElement.builder()
                                                     .label("Both")
                                                     .build())
                                          .build())
                .build();

            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response.getData().get("bothDefendantsSpec")).isEqualTo("Both");
        }

        @Test
        void shouldReturnOne_whenHaveOneDefendants() {
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .respondent2(PartyBuilder.builder().individual().build())
                .addRespondent2(YES)
                .respondent2SameLegalRepresentative(YES)
                .respondent1ResponseDeadline(LocalDateTime.now().minusDays(15))
                .defendantDetailsSpec(DynamicList.builder()
                                          .value(DynamicListElement.builder()
                                                     .label("Test User")
                                                     .build())
                                          .build())
                .build();

            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response.getData().get("bothDefendantsSpec")).isEqualTo("One");
        }

        @Test
        void shouldReturnOneDefendantText_whenOneDefendant() {
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .respondent1ResponseDeadline(LocalDateTime.now().minusDays(15))
                .defendantDetailsSpec(DynamicList.builder()
                                          .value(DynamicListElement.builder()
                                                     .label("Test User")
                                                     .build())
                                          .build())
                .build();

            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response.getData().get("currentDefendant"))
                .isEqualTo("Has Test User paid some of the amount owed?");
        }

        @Test
        void shouldReturnBothDefendantText_whenTwoDefendant() {
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .respondent2(PartyBuilder.builder().individual().build())
                .addRespondent2(YES)
                .respondent2SameLegalRepresentative(YES)
                .respondent1ResponseDeadline(LocalDateTime.now().minusDays(15))
                .defendantDetailsSpec(DynamicList.builder()
                                          .value(DynamicListElement.builder()
                                                     .label("Test User")
                                                     .label("Test User2")
                                                     .label("Both Defendants")
                                                     .build())
                                          .build())
                .build();

            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response.getData().get("currentDefendant"))
                .isEqualTo("Have the defendants paid some of the amount owed?");
        }

        @Test
        void shouldReturnBothDefendant_whenTwoDefendantSelected() {
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .respondent2(PartyBuilder.builder().individual().build())
                .addRespondent2(YES)
                .respondent2SameLegalRepresentative(YES)
                .respondent1ResponseDeadline(LocalDateTime.now().minusDays(15))
                .defendantDetailsSpec(DynamicList.builder()
                                          .value(DynamicListElement.builder()
                                                     .label("Test User")
                                                     .label("Test User2")
                                                     .label("Both Defendants")
                                                     .build())
                                          .build())
                .build();

            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response.getData().get("currentDefendantName"))
                .isEqualTo("both defendants");
        }

        @Test
        void shouldReturnDefendantName_whenOneDefendantSelected() {
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .respondent2(PartyBuilder.builder().individual().build())
                .addRespondent2(YES)
                .respondent2SameLegalRepresentative(YES)
                .respondent1ResponseDeadline(LocalDateTime.now().minusDays(15))
                .defendantDetailsSpec(DynamicList.builder()
                                          .value(DynamicListElement.builder()
                                                     .label("Steve Rodgers")
                                                     .build())
                                          .build())
                .build();

            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response.getData().get("currentDefendantName"))
                .isEqualTo("Steve Rodgers");
            //
            assertThat(response.getData().get("registrationTypeRespondentOne")).isNull();
            assertThat(response.getData().get("registrationTypeRespondentTwo")).isNull();
        }

        @Test
        void shouldReturnRegistrationInfo_whenOneVOne() {
            when(time.now()).thenReturn(LocalDateTime.of(2023, 2, 20, 11, 11, 11));
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .addRespondent2(NO)
                .respondent1ResponseDeadline(LocalDateTime.now().minusDays(15))
                .defendantDetailsSpec(DynamicList.builder()
                                          .value(DynamicListElement.builder()
                                                     .label("Steve Rodgers")
                                                     .build())
                                          .build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData())
                .extracting("registrationTypeRespondentOne")
                .asString()
                .contains("registrationType=R")
                .contains("judgmentDateTime=2023-02-20T11:11:11");
            assertThat(response.getData())
                .doesNotContainKey("registrationTypeRespondentTwo");
        }

        @Test
        void shouldReturnRegistrationInfo_whenTwoVOne() {
            when(time.now()).thenReturn(LocalDateTime.of(2023, 2, 20, 11, 11, 11));
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .addRespondent2(NO)
                .addApplicant2(YesOrNo.YES)
                .applicant2(PartyBuilder.builder().individual().build())
                .respondent1ResponseDeadline(LocalDateTime.now().minusDays(15))
                .defendantDetailsSpec(DynamicList.builder()
                                          .value(DynamicListElement.builder()
                                                     .label("Steve Rodgers")
                                                     .build())
                                          .build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData())
                .extracting("registrationTypeRespondentOne")
                .asString()
                .contains("registrationType=R")
                .contains("judgmentDateTime=2023-02-20T11:11:11");
            assertThat(response.getData())
                .doesNotContainKey("registrationTypeRespondentTwo");
        }

        @Test
        void shouldNotReturnRegistrationInfo_whenOneVTwoAndOneDefendantSelected() {
            when(time.now()).thenReturn(LocalDateTime.of(2023, 2, 20, 11, 11, 11));
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .respondent2(PartyBuilder.builder().individual().build())
                .addRespondent2(YES)
                .respondent2SameLegalRepresentative(YES)
                .respondent1ResponseDeadline(LocalDateTime.now().minusDays(15))
                .defendantDetailsSpec(DynamicList.builder()
                                          .value(DynamicListElement.builder()
                                                     .label("Steve Rodgers")
                                                     .build())
                                          .build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData()).doesNotContainKey("registrationTypeRespondentOne");
            assertThat(response.getData()).doesNotContainKey("registrationTypeRespondentTwo");
        }

        @Test
        void shouldReturnRegistrationInfo_whenOneVTwoAndBothDefendantSelected() {
            when(time.now()).thenReturn(LocalDateTime.of(2023, 2, 20, 11, 11, 11));
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .respondent2(PartyBuilder.builder().individual().build())
                .addRespondent2(YES)
                .respondent2SameLegalRepresentative(YES)
                .respondent1ResponseDeadline(LocalDateTime.now().minusDays(15))
                .defendantDetailsSpec(DynamicList.builder()
                                          .value(DynamicListElement.builder()
                                                     .label("Test User")
                                                     .label("Test User2")
                                                     .label("Both Defendants")
                                                     .build())
                                          .build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData())
                .extracting("registrationTypeRespondentOne")
                .asString()
                .contains("registrationType=R")
                .contains("judgmentDateTime=2023-02-20T11:11:11");
            assertThat(response.getData())
                .extracting("registrationTypeRespondentTwo")
                .asString()
                .contains("registrationType=R")
                .contains("judgmentDateTime=2023-02-20T11:11:11");
        }
    }

    @Nested
    class MidEventPartialPayment {

        private static final String PAGE_ID = "claimPartialPayment";

        @Test
        void shouldReturnError_whenPartialPaymentLargerFullClaim() {
            BigDecimal claimAmount = new BigDecimal(2000);
            BigDecimal interestAmount = new BigDecimal(100);

            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged()
                .build().toBuilder()
                .respondent1ResponseDeadline(LocalDateTime.now().minusDays(15))
                .partialPayment(YesOrNo.YES)
                .totalClaimAmount(claimAmount)
                .totalInterest(interestAmount)
                .partialPaymentAmount("3000000")
                .build();

            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response.getErrors().get(0)).isEqualTo("The amount already paid exceeds the full claim amount");
        }

        @Test
        void shouldNotReturnError_whenPartialPaymentLessFullClaim() {
            BigDecimal claimAmount = new BigDecimal(2000);
            BigDecimal interestAmount = new BigDecimal(100);

            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .respondent1ResponseDeadline(LocalDateTime.now().minusDays(15))
                .partialPayment(YesOrNo.YES)
                .totalClaimAmount(claimAmount)
                .totalInterest(interestAmount)
                .partialPaymentAmount("3000")
                .build();

            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response.getErrors()).isEmpty();
        }
    }

    @Nested
    class MidEventRepaymentAndDateValidate {

        private static final String PAGE_ID = "repaymentValidate";

        @Test
        void shouldNotReturnError_whenRepaymentAmountLessThanOrEqualAmountDue() {
            var testDate = LocalDate.now().plusDays(35);
            String due = "1000"; //in pounds
            String suggest = "99999"; // 999 pound in pennies

            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .repaymentDue(due)
                .repaymentSuggestion(suggest)
                .repaymentDate(testDate)
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response.getErrors()).isEmpty();
        }

        @Test
        void shouldReturnError_whenRepaymentAmountGreaterThanAmountDue() {
            var testDate = LocalDate.now().plusDays(35);
            String due = "1000"; //in pounds
            String suggest = "110000"; // 1100 pound in pennies

            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .repaymentDue(due)
                .repaymentSuggestion(suggest)
                .repaymentDate(testDate)
                .build();

            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response.getErrors().get(0)).isEqualTo("Regular payment cannot exceed the full claim amount");
        }

        @Test
        void shouldNotReturnError_whenDateNotInPastAndEligible() {
            //eligible date is 31 days in the future
            var testDate = LocalDate.now().plusDays(31);
            String due = "1000"; //in pounds
            String suggest = "10000"; // 100 pound in pennies

            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .repaymentDue(due)
                .repaymentSuggestion(suggest)
                .repaymentDate(testDate)
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response.getErrors()).isEmpty();
        }

        @Test
        void shouldReturnError_whenDateInPastAndNotEligible() {
            //eligible date is 31 days in the future, but as text says "after", we set text to one day previous i.e.
            //If 7th is the eligible date, text will say "after the 6th".
            LocalDate eligibleDate = LocalDate.now().plusDays(30);
            var testDate = LocalDate.now().plusDays(25);
            String due = "1000"; //in pounds
            String suggest = "10000"; // 100 pound in pennies

            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .repaymentDue(due)
                .repaymentSuggestion(suggest)
                .repaymentDate(testDate)
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response.getErrors().get(0))
                .isEqualTo("Selected date must be after " + formatLocalDate(
                    eligibleDate,
                    DATE
                ));
        }

    }

    @Nested
    class PaymentDateValidationCallback {

        private static final String PAGE_ID = "claimPaymentDate";

        @Test
        void shouldReturnError_whenPastPaymentDate() {

            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .respondent1ResponseDeadline(LocalDateTime.now().minusDays(15))
                .partialPayment(YesOrNo.YES)
                .paymentSetDate(LocalDate.now().minusDays(15))
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response.getErrors().get(0)).isEqualTo("Payment Date cannot be past date");
        }

        @Test
        void shouldNotReturnError_whenPastPaymentDate() {

            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .respondent1ResponseDeadline(LocalDateTime.now().minusDays(15))
                .partialPayment(YesOrNo.YES)
                .paymentSetDate(LocalDate.now().plusDays(15))
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response.getErrors()).isEmpty();
        }
    }

    @Nested
    class RepaymentBreakdownCallback {

        private static final String PAGE_ID = "repaymentBreakdown";

        @Test
        void shouldReturnFixedAmount_whenClaimAmountLessthan5000() {
            when(interestCalculator.calculateInterest(any()))
                .thenReturn(BigDecimal.valueOf(100)
                );
            when(feesService.getFeeDataByTotalClaimAmount(any()))
                .thenReturn(Fee.builder()
                                .calculatedAmountInPence(BigDecimal.valueOf(100))
                                .version("1")
                                .code("CODE")
                                .build());
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .respondent1ResponseDeadline(LocalDateTime.now().minusDays(15))
                .partialPayment(YesOrNo.YES)
                .paymentSetDate(LocalDate.now().minusDays(15))
                .partialPaymentAmount("100")
                .totalClaimAmount(BigDecimal.valueOf(1010))
                .paymentConfirmationDecisionSpec(YesOrNo.YES)
                .partialPayment(YesOrNo.YES)
                .defendantDetailsSpec(DynamicList.builder()
                                          .value(DynamicListElement.builder()
                                                     .label("Test User")
                                                     .build())
                                          .build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            String test = "The judgment will order " + caseData.getDefendantDetailsSpec().getValue().getLabel()
                + " to pay £1222.00, including the claim fee and"
                + " interest, if applicable, as shown:\n"
                + "### Claim amount \n"
                + " £1010.00\n"
                + " ### Claim interest amount \n"
                + "£100.00\n"
                + " ### Fixed cost amount \n"
                + "£112.00\n"
                + "### Claim fee amount \n"
                + " £1.00\n"
                + " ## Subtotal \n"
                + " £1223.00\n"
                + "\n"
                + " ### Amount already paid \n"
                + "£1.00\n"
                + " ## Total still owed \n"
                + " £1222.00";

            assertThat(response.getData().get("repaymentSummaryObject")).isEqualTo(test);
        }

        @Test
        void shouldReturnFixedAmount_whenClaimAmountLessthan500() {
            when(interestCalculator.calculateInterest(any()))
                .thenReturn(BigDecimal.valueOf(100)
                );
            when(feesService.getFeeDataByTotalClaimAmount(any()))
                .thenReturn(Fee.builder()
                                .calculatedAmountInPence(BigDecimal.valueOf(100))
                                .version("1")
                                .code("CODE")
                                .build());
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .respondent1ResponseDeadline(LocalDateTime.now().minusDays(15))
                .partialPayment(YesOrNo.YES)
                .paymentSetDate(LocalDate.now().minusDays(15))
                .partialPaymentAmount("100")
                .totalClaimAmount(BigDecimal.valueOf(499))
                .paymentConfirmationDecisionSpec(YesOrNo.YES)
                .partialPayment(YesOrNo.YES)
                .defendantDetailsSpec(DynamicList.builder()
                                          .value(DynamicListElement.builder()
                                                     .label("Test User")
                                                     .build())
                                          .build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            String test = "The judgment will order " + caseData.getDefendantDetailsSpec().getValue().getLabel()
                + " to pay £681.00, including the claim fee and"
                + " interest, if applicable, as shown:\n"
                + "### Claim amount \n"
                + " £499.00\n"
                + " ### Claim interest amount \n"
                + "£100.00\n"
                + " ### Fixed cost amount \n"
                + "£82.00\n"
                + "### Claim fee amount \n"
                + " £1.00\n"
                + " ## Subtotal \n"
                + " £682.00\n"
                + "\n"
                + " ### Amount already paid \n"
                + "£1.00\n"
                + " ## Total still owed \n"
                + " £681.00";
            assertThat(response.getData().get("repaymentSummaryObject")).isEqualTo(test);
        }

        @Test
        void shouldReturnFixedAmount_whenClaimAmountLessthan1000AndGreaterThan500() {
            when(interestCalculator.calculateInterest(any()))
                .thenReturn(BigDecimal.valueOf(100)
                );
            when(feesService.getFeeDataByTotalClaimAmount(any()))
                .thenReturn(Fee.builder()
                                .calculatedAmountInPence(BigDecimal.valueOf(100))
                                .version("1")
                                .code("CODE")
                                .build());
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .respondent1ResponseDeadline(LocalDateTime.now().minusDays(15))
                .partialPayment(YesOrNo.YES)
                .paymentSetDate(LocalDate.now().minusDays(15))
                .partialPaymentAmount("100")
                .totalClaimAmount(BigDecimal.valueOf(999))
                .paymentConfirmationDecisionSpec(YesOrNo.YES)
                .partialPayment(YesOrNo.YES)
                .defendantDetailsSpec(DynamicList.builder()
                                          .value(DynamicListElement.builder()
                                                     .label("Test User")
                                                     .build())
                                          .build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
            String test = "The judgment will order " + caseData.getDefendantDetailsSpec().getValue().getLabel()
                + " to pay £1201.00, including the claim fee and"
                + " interest, if applicable, as shown:\n"
                + "### Claim amount \n"
                + " £999.00\n"
                + " ### Claim interest amount \n"
                + "£100.00\n"
                + " ### Fixed cost amount \n"
                + "£102.00\n"
                + "### Claim fee amount \n"
                + " £1.00\n"
                + " ## Subtotal \n"
                + " £1202.00\n"
                + "\n"
                + " ### Amount already paid \n"
                + "£1.00\n"
                + " ## Total still owed \n"
                + " £1201.00";
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response.getData().get("repaymentSummaryObject")).isEqualTo(test);
        }

        @Test
        void shouldReturnFixedAmount_whenClaimAmountGreaterthan5000() {
            when(interestCalculator.calculateInterest(any()))
                .thenReturn(BigDecimal.valueOf(0)
                );
            when(feesService.getFeeDataByTotalClaimAmount(any()))
                .thenReturn(Fee.builder()
                                .calculatedAmountInPence(BigDecimal.valueOf(100))
                                .version("1")
                                .code("CODE")
                                .build());
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .respondent1ResponseDeadline(LocalDateTime.now().minusDays(15))
                .partialPayment(YesOrNo.YES)
                .paymentSetDate(LocalDate.now().minusDays(15))
                .partialPaymentAmount("100")
                .totalClaimAmount(BigDecimal.valueOf(5001))
                .defendantDetailsSpec(DynamicList.builder()
                                          .value(DynamicListElement.builder()
                                                     .label("Test User")
                                                     .build())
                                          .build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            String test = "The judgment will order " + caseData.getDefendantDetailsSpec().getValue().getLabel()
                + " to pay £5001.00, including the claim fee and"
                + " interest, if applicable, as shown:\n"
                + "### Claim amount \n"
                + " £5001.00\n"
                + "### Claim fee amount \n"
                + " £1.00\n"
                + " ## Subtotal \n"
                + " £5002.00\n"
                + "\n"
                + " ### Amount already paid \n"
                + "£1.00\n"
                + " ## Total still owed \n"
                + " £5001.00";
            assertThat(response.getData().get("repaymentSummaryObject")).isEqualTo(test);
        }

        @Test
        void shouldReturnFixedAmount_whenClaimAmountGreaterthan5000And1v2() {
            when(interestCalculator.calculateInterest(any()))
                .thenReturn(BigDecimal.valueOf(0)
                );
            when(feesService.getFeeDataByTotalClaimAmount(any()))
                .thenReturn(Fee.builder()
                                .calculatedAmountInPence(BigDecimal.valueOf(100))
                                .version("1")
                                .code("CODE")
                                .build());
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .respondent2(PartyBuilder.builder().individual().build())
                .addRespondent2(YES)
                .respondent2SameLegalRepresentative(YES)
                .respondent1ResponseDeadline(LocalDateTime.now().minusDays(15))
                .partialPayment(YesOrNo.YES)
                .paymentSetDate(LocalDate.now().minusDays(15))
                .partialPaymentAmount("100")
                .totalClaimAmount(BigDecimal.valueOf(5001))
                .defendantDetailsSpec(DynamicList.builder()
                                          .value(DynamicListElement.builder()
                                                     .label("Test User")
                                                     .label("Test User2")
                                                     .label("Both Defendants")
                                                     .build())
                                          .build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            String test = "The judgment will order the defendants to pay £5001.00, including the claim fee and"
                + " interest, if applicable, as shown:\n"
                + "### Claim amount \n"
                + " £5001.00\n"
                + "### Claim fee amount \n"
                + " £1.00\n"
                + " ## Subtotal \n"
                + " £5002.00\n"
                + "\n"
                + " ### Amount already paid \n"
                + "£1.00\n"
                + " ## Total still owed \n"
                + " £5001.00";
            assertThat(response.getData().get("repaymentSummaryObject")).isEqualTo(test);
        }

        @Test
        void shouldReturnFixedAmount_whenClaimAmountLessthan5000AndLRvLiP() {
            when(interestCalculator.calculateInterest(any()))
                .thenReturn(BigDecimal.valueOf(100)
                );
            when(feesService.getFeeDataByTotalClaimAmount(any()))
                .thenReturn(Fee.builder()
                                .calculatedAmountInPence(BigDecimal.valueOf(100))
                                .version("1")
                                .code("CODE")
                                .build());
            when(featureToggleService.isPinInPostEnabled()).thenReturn(true);
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .respondent1ResponseDeadline(LocalDateTime.now().minusDays(15))
                .partialPayment(YesOrNo.YES)
                .paymentSetDate(LocalDate.now().minusDays(15))
                .partialPaymentAmount("100")
                .totalClaimAmount(BigDecimal.valueOf(1010))
                .paymentConfirmationDecisionSpec(YesOrNo.YES)
                .partialPayment(YesOrNo.YES)
                .defendantDetailsSpec(DynamicList.builder()
                                          .value(DynamicListElement.builder()
                                                     .label("Test User")
                                                     .build())
                                          .build())
                .specRespondent1Represented(NO)
                .respondent1Represented(null)

                .build();
            CallbackParams params = callbackParamsOf(V_1, caseData, MID, PAGE_ID);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            String test = "The Judgement request will be reviewed by the court, this case will proceed offline, you will receive any further updates by post.\n"
                + "### Claim amount \n"
                + " £1010.00\n"
                + " ### Claim interest amount \n"
                + "£100.00\n"
                + " ### Fixed cost amount \n"
                + "£112.00\n"
                + "### Claim fee amount \n"
                + " £1.00\n"
                + " ## Subtotal \n"
                + " £1223.00\n"
                + "\n"
                + " ### Amount already paid \n"
                + "£1.00\n"
                + " ## Total still owed \n"
                + " £1222.00";

            assertThat(response.getData().get("repaymentSummaryObject")).isEqualTo(test);
        }

        @Test
        void shouldReturnClaimFee_whenHWFRemissionGrantedLRvLiP() {
            when(interestCalculator.calculateInterest(any()))
                .thenReturn(BigDecimal.valueOf(100)
                );
            when(feesService.getFeeDataByTotalClaimAmount(any()))
                .thenReturn(Fee.builder()
                                .calculatedAmountInPence(BigDecimal.valueOf(100))
                                .version("1")
                                .code("CODE")
                                .build());
            when(featureToggleService.isPinInPostEnabled()).thenReturn(true);
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .respondent1ResponseDeadline(LocalDateTime.now().minusDays(15))
                .partialPayment(YesOrNo.YES)
                .paymentSetDate(LocalDate.now().minusDays(15))
                .partialPaymentAmount("100")
                .totalClaimAmount(BigDecimal.valueOf(1010))
                .paymentConfirmationDecisionSpec(YesOrNo.YES)
                .partialPayment(YesOrNo.YES)
                .defendantDetailsSpec(DynamicList.builder()
                                          .value(DynamicListElement.builder()
                                                     .label("Test User")
                                                     .build())
                                          .build())
                .specRespondent1Represented(NO)
                .respondent1Represented(null)
                .claimIssuedHwfDetails(HelpWithFeesDetails.builder()
                                           .outstandingFeeInPounds(BigDecimal.ZERO)
                                           .build())
                .hwfFeeType(FeeType.CLAIMISSUED)
                .build();
            CallbackParams params = callbackParamsOf(V_1, caseData, MID, PAGE_ID);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            String test = "The Judgement request will be reviewed by the court, this case will proceed offline, you will receive any further updates by post.\n"
                + "### Claim amount \n"
                + " £1010.00\n"
                + " ### Claim interest amount \n"
                + "£100.00\n"
                + " ### Fixed cost amount \n"
                + "£112.00\n"
                + "### Claim fee amount \n"
                + " £0.00\n"
                + " ## Subtotal \n"
                + " £1222.00\n"
                + "\n"
                + " ### Amount already paid \n"
                + "£1.00\n"
                + " ## Total still owed \n"
                + " £1221.00";

            assertThat(response.getData().get("repaymentSummaryObject")).isEqualTo(test);
        }
    }

    @Nested
    class MidRepaymentTotal {

        private static final String PAGE_ID = "repaymentTotal";

        @Test
        void shouldGetException_whenIsCalledAndTheOverallIsNull() {
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .respondent1ResponseDeadline(LocalDateTime.now().minusDays(15))
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
            assertThrows(NullPointerException.class, () -> {
                handler.handle(params);
            });
        }
    }

    @Nested
    class AboutToSubmitCallback {

        @Test
        void shouldNotGenerateDocumentAndContinueOffline_whenIsCalled1v1AndIsJudgmentOnlineLiveDisabled() {
            when(interestCalculator.calculateInterest(any()))
                .thenReturn(BigDecimal.valueOf(0)
                );
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .respondent1ResponseDeadline(LocalDateTime.now().minusDays(15))
                .defendantDetailsSpec(DynamicList.builder()
                                          .value(DynamicListElement.builder()
                                                     .label("John Smith")
                                                     .build())
                                          .build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            when(featureToggleService.isJudgmentOnlineLive()).thenReturn(false);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response.getData()).extracting("businessProcess").isNotNull();
            assertThat(response.getData().get("businessProcess")).extracting("camundaEvent").isEqualTo(DEFAULT_JUDGEMENT_SPEC.name());
            assertThat(response.getState()).isEqualTo(CaseState.PROCEEDS_IN_HERITAGE_SYSTEM.name());
        }

        @Test
        void shouldGenerateDocumentAndContinueOnline_whenIsCalled1v1() {
            when(interestCalculator.calculateInterest(any()))
                .thenReturn(BigDecimal.valueOf(0)
                );
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .respondent1ResponseDeadline(LocalDateTime.now().minusDays(15))
                .partialPaymentAmount("10")
                .totalClaimAmount(BigDecimal.valueOf(1010))
                .partialPayment(YES)
                .paymentTypeSelection(DJPaymentTypeSelection.IMMEDIATELY)
                .caseManagementLocation(CaseLocationCivil.builder().baseLocation("0123").region("0321").build())
                .defendantDetailsSpec(DynamicList.builder()
                                          .value(DynamicListElement.builder()
                                                     .label("John Smith")
                                                     .build())
                                          .build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            when(featureToggleService.isJudgmentOnlineLive()).thenReturn(true);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response.getData()).extracting("businessProcess").isNotNull();
            assertThat(response.getData().get("businessProcess")).extracting("camundaEvent").isEqualTo(DEFAULT_JUDGEMENT_NON_DIVERGENT_SPEC.name());
            assertThat(response.getState()).isEqualTo(CaseState.All_FINAL_ORDERS_ISSUED.name());
            assertThat(response.getData()).extracting("activeJudgment").isNotNull();
            assertThat(response.getData().get("activeJudgment")).extracting("state").isEqualTo("ISSUED");
            assertThat(response.getData().get("activeJudgment")).extracting("type").isEqualTo("DEFAULT_JUDGMENT");
            assertThat(response.getData().get("activeJudgment")).extracting("judgmentId").isEqualTo(1);
            assertThat(response.getData().get("activeJudgment")).extracting("isRegisterWithRTL").isEqualTo("Yes");

        }

        @Test
        void shouldGenerateDocumentAndContinueOnline_whenIsCalled1v2NonDivergent() {
            when(interestCalculator.calculateInterest(any()))
                .thenReturn(BigDecimal.valueOf(0)
                );
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .respondent1ResponseDeadline(LocalDateTime.now().minusDays(15))
                .applicant1(PartyBuilder.builder().individual().build())
                .respondent1(PartyBuilder.builder().individual().build())
                .respondent2(PartyBuilder.builder().individual().build())
                .addRespondent2(YesOrNo.YES)
                .respondent2SameLegalRepresentative(YesOrNo.YES)
                .partialPaymentAmount("10")
                .totalClaimAmount(BigDecimal.valueOf(1010))
                .partialPayment(YES)
                .paymentTypeSelection(DJPaymentTypeSelection.IMMEDIATELY)
                .caseManagementLocation(CaseLocationCivil.builder().baseLocation("0123").region("0321").build())
                .defendantDetailsSpec(DynamicList.builder()
                                          .value(DynamicListElement.builder()
                                                     .label("Both Defendants")
                                                     .build())
                                          .build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            when(featureToggleService.isJudgmentOnlineLive()).thenReturn(true);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response.getData()).extracting("businessProcess").isNotNull();
            assertThat(response.getData().get("businessProcess")).extracting("camundaEvent").isEqualTo(DEFAULT_JUDGEMENT_NON_DIVERGENT_SPEC.name());
            assertThat(response.getState()).isEqualTo(CaseState.All_FINAL_ORDERS_ISSUED.name());
            assertThat(response.getData()).extracting("activeJudgment").isNotNull();
            assertThat(response.getData().get("activeJudgment")).extracting("state").isEqualTo("ISSUED");
            assertThat(response.getData().get("activeJudgment")).extracting("type").isEqualTo("DEFAULT_JUDGMENT");
            assertThat(response.getData().get("activeJudgment")).extracting("judgmentId").isEqualTo(1);
            assertThat(response.getData().get("activeJudgment")).extracting("isRegisterWithRTL").isEqualTo("Yes");

        }

        @Test
        void shouldNotGenerateDocumentAndContinueOffline_whenIsCalled1v2Divergent() {
            when(interestCalculator.calculateInterest(any()))
                .thenReturn(BigDecimal.valueOf(0)
                );
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .respondent1ResponseDeadline(LocalDateTime.now().minusDays(15))
                .applicant1(PartyBuilder.builder().individual().build())
                .respondent1(PartyBuilder.builder().individual().build())
                .respondent2(PartyBuilder.builder().individual().build())
                .addRespondent2(YesOrNo.YES)
                .respondent2SameLegalRepresentative(YesOrNo.YES)
                .partialPaymentAmount("10")
                .paymentTypeSelection(DJPaymentTypeSelection.IMMEDIATELY)
                .totalClaimAmount(BigDecimal.valueOf(1010))
                .partialPayment(YES)
                .caseManagementLocation(CaseLocationCivil.builder().baseLocation("0123").region("0321").build())
                .defendantDetailsSpec(DynamicList.builder()
                                          .value(DynamicListElement.builder()
                                                     .label("John Smith")
                                                     .build())
                                          .build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            when(featureToggleService.isJudgmentOnlineLive()).thenReturn(true);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response.getData()).extracting("businessProcess").isNotNull();
            assertThat(response.getData().get("businessProcess")).extracting("camundaEvent").isEqualTo(DEFAULT_JUDGEMENT_SPEC.name());
            assertThat(response.getState()).isEqualTo(CaseState.PROCEEDS_IN_HERITAGE_SYSTEM.name());
            assertThat(response.getData()).extracting("activeJudgment").isNotNull();
            assertThat(response.getData().get("activeJudgment")).extracting("state").isEqualTo("REQUESTED");
            assertThat(response.getData().get("activeJudgment")).extracting("type").isEqualTo("DEFAULT_JUDGMENT");
            assertThat(response.getData().get("activeJudgment")).extracting("judgmentId").isEqualTo(1);
            assertThat(response.getData().get("activeJudgment")).extracting("isRegisterWithRTL").isEqualTo("No");

        }

        @Test
        void shouldNotGenerateDocumentAndContinueOffline_whenIsCalled2v1JOIsNotLive() {
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .respondent1ResponseDeadline(LocalDateTime.now().minusDays(15))
                .applicant1(PartyBuilder.builder().individual().build())
                .applicant2(PartyBuilder.builder().individual().build())
                .addApplicant2(YesOrNo.YES)
                .defendantDetailsSpec(DynamicList.builder()
                                          .value(DynamicListElement.builder()
                                                     .label("John Smith")
                                                     .build())
                                          .build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            when(featureToggleService.isJudgmentOnlineLive()).thenReturn(false);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response.getData()).extracting("businessProcess").isNotNull();
            assertThat(response.getData().get("businessProcess")).extracting("camundaEvent").isEqualTo(DEFAULT_JUDGEMENT_SPEC.name());
            assertThat(response.getState()).isEqualTo(CaseState.PROCEEDS_IN_HERITAGE_SYSTEM.name());
        }

        @Test
        void shouldMoveToFinalOrderIssued_whenIsJOOnlineAnd2v1() {
            when(interestCalculator.calculateInterest(any()))
                .thenReturn(BigDecimal.valueOf(0)
                );
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .respondent1ResponseDeadline(LocalDateTime.now().minusDays(15))
                .applicant1(PartyBuilder.builder().individual().build())
                .applicant2(PartyBuilder.builder().individual().build())
                .addApplicant2(YesOrNo.YES)
                .partialPaymentAmount("10")
                .paymentTypeSelection(DJPaymentTypeSelection.IMMEDIATELY)
                .totalClaimAmount(BigDecimal.valueOf(1010))
                .partialPayment(YES)
                .caseManagementLocation(CaseLocationCivil.builder().baseLocation("0123").region("0321").build())
                .defendantDetailsSpec(DynamicList.builder()
                                          .value(DynamicListElement.builder()
                                                     .label("John Smith")
                                                     .build())
                                          .build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            when(featureToggleService.isJudgmentOnlineLive()).thenReturn(true);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response.getData()).extracting("businessProcess").isNotNull();
            assertThat(response.getData().get("businessProcess")).extracting("camundaEvent").isEqualTo(DEFAULT_JUDGEMENT_NON_DIVERGENT_SPEC.name());
            assertThat(response.getState()).isEqualTo(CaseState.All_FINAL_ORDERS_ISSUED.name());
        }
    }

    @Nested
    class SubmittedCallback {

        @Test
        void shouldReturnExpectedSubmittedCallbackResponse_whenInvoked1v1() {
            String body = "<br /><a href=\"/cases/case-details/1594901956117591#Claim documents\" "
                + "target=\"_blank\">Download  default judgment</a> "
                + "%n%n The defendant will be served with the Default Judgment.";
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .respondent1ResponseDeadline(LocalDateTime.now().minusDays(15))
                .build();
            CallbackParams params = callbackParamsOf(caseData, SUBMITTED);
            SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);
            assertThat(response).usingRecursiveComparison().isEqualTo(
                SubmittedCallbackResponse.builder()
                    .confirmationHeader("# Default Judgment Granted ")
                    .confirmationBody(format(body))
                    .build());
        }

        @Test
        void shouldReturnJudgementRequestedResponse_whenInvokedOneDefendantWhen1v2() {
            String header = "# Default judgment requested";
            String body = "A default judgment has been sent to John Smith. "
                + "The claim will now progress offline (on paper)";
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .applicant1(PartyBuilder.builder().build())
                .respondent1(PartyBuilder.builder().build())
                .respondent2(PartyBuilder.builder().build())
                .addRespondent2(YesOrNo.YES)
                .respondent2SameLegalRepresentative(YesOrNo.YES)
                .defendantDetailsSpec(DynamicList.builder()
                                          .value(DynamicListElement.builder()
                                                     .label("John Smith")
                                                     .build())
                                          .build())
                .build();

            CallbackParams params = callbackParamsOf(caseData, SUBMITTED);
            SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);
            assertThat(response).usingRecursiveComparison().isEqualTo(SubmittedCallbackResponse.builder()
                                                                          .confirmationHeader(header)
                                                                          .confirmationBody(String.format(body))
                                                                          .build());
        }

        @Test
        void shouldReturnJudgementRequestedResponse_whenInvokedBothDefendantWhen1v2() {
            String body = "<br /><a href=\"/cases/case-details/1594901956117591#Claim documents\" "
                + "target=\"_blank\">Download  default judgment</a> "
                + "%n%n The defendant will be served the Default Judgment.";
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .applicant1(PartyBuilder.builder().build())
                .respondent1(PartyBuilder.builder().build())
                .respondent2(PartyBuilder.builder().build())
                .addRespondent2(YesOrNo.YES)
                .respondent2SameLegalRepresentative(YesOrNo.YES)
                .defendantDetailsSpec(DynamicList.builder()
                                          .value(DynamicListElement.builder()
                                                     .label("Both")
                                                     .build())
                                          .build())
                .build();

            CallbackParams params = callbackParamsOf(caseData, SUBMITTED);
            SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);
            assertThat(response).usingRecursiveComparison().isEqualTo(SubmittedCallbackResponse.builder()
                                                                          .confirmationHeader(
                                                                              "# Default Judgment Granted ")
                                                                          .confirmationBody(String.format(body))
                                                                          .build());
        }

        @Test
        void shouldReturnJudgementRequestedResponse_whenLrVLip() {
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .applicant1(PartyBuilder.builder().build())
                .respondent1(PartyBuilder.builder().build())
                .respondent1Represented(NO)
                .build();
            CallbackParams params = callbackParamsOf(caseData, SUBMITTED);
            SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);
            assertThat(caseData.isLRvLipOneVOne()).isTrue();
            assertThat(response).usingRecursiveComparison().isEqualTo(SubmittedCallbackResponse.builder()
                                                                          .confirmationHeader(
                                                                              JUDGMENT_REQUESTED_HEADER)
                                                                          .confirmationBody(String.format(
                                                                              JUDGMENT_REQUESTED_LIP_CASE))
                                                                          .build());
        }

        @Test
        void shouldReturnJudgementGrantedResponse_whenisJudgmentLiveTrueAndLrVLip() {
            String body = "<br /><a href=\"/cases/case-details/1594901956117591#Claim documents\" "
                + "target=\"_blank\">Download  default judgment</a> "
                + "%n%n The defendant will be served with the Default Judgment.";
            when(featureToggleService.isJudgmentOnlineLive()).thenReturn(true);
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .applicant1(PartyBuilder.builder().build())
                .respondent1(PartyBuilder.builder().build())
                .respondent1Represented(NO)
                .build();
            CallbackParams params = callbackParamsOf(caseData, SUBMITTED);
            SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);
            assertThat(caseData.isLRvLipOneVOne()).isTrue();
            assertThat(response).usingRecursiveComparison().isEqualTo(SubmittedCallbackResponse.builder()
                                                                          .confirmationHeader(
                                                                              JUDGMENT_GRANTED_HEADER)
                                                                          .confirmationBody(format(body))
                                                                          .build());
        }
    }

}
