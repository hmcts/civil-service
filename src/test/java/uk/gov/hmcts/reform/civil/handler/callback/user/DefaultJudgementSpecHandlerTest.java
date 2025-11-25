package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import uk.gov.hmcts.reform.civil.model.FixedCosts;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.breathing.BreathingSpaceEnterInfo;
import uk.gov.hmcts.reform.civil.model.breathing.BreathingSpaceInfo;
import uk.gov.hmcts.reform.civil.model.breathing.BreathingSpaceLiftInfo;
import uk.gov.hmcts.reform.civil.model.caseflags.Flags;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.HelpWithFeesDetails;
import uk.gov.hmcts.reform.civil.model.citizenui.RespondentLiPResponse;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.CaseLocationCivil;
import uk.gov.hmcts.reform.civil.model.robotics.RoboticsAddress;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.PartyBuilder;
import uk.gov.hmcts.reform.civil.service.DeadlinesCalculator;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.Time;
import uk.gov.hmcts.reform.civil.service.robotics.mapper.AddressLinesMapper;
import uk.gov.hmcts.reform.civil.service.robotics.mapper.RoboticsAddressMapper;
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
import static uk.gov.hmcts.reform.civil.model.Party.Type.INDIVIDUAL;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {
    DefaultJudgementSpecHandler.class,
    JacksonAutoConfiguration.class,
    ValidationAutoConfiguration.class,
    CaseDetailsConverter.class,
    DefaultJudgmentOnlineMapper.class,
    RoboticsAddressMapper.class,
    AddressLinesMapper.class
})
public class DefaultJudgementSpecHandlerTest extends BaseCallbackHandlerTest {

    @Autowired
    private DefaultJudgementSpecHandler handler;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private InterestCalculator interestCalculator;

    @MockBean
    private Time time;

    @MockBean
    private FeatureToggleService featureToggleService;
    @MockBean
    private DeadlinesCalculator deadlinesCalculator;

    @MockBean
    private RoboticsAddressMapper addressMapper;

    @Autowired
    private CaseDetailsConverter caseDetailsConverter;

    @Nested
    class AboutToStartCallback {

        @Test
        void shouldReturnError_WhenAboutToStartIsInvoked() {
            BreathingSpaceLiftInfo breathingSpaceLiftInfo = new BreathingSpaceLiftInfo();
            breathingSpaceLiftInfo.setExpectedEnd(LocalDate.now().minusDays(5));
            BreathingSpaceInfo breathingSpaceInfo = new BreathingSpaceInfo();
            breathingSpaceInfo.setLift(breathingSpaceLiftInfo);

            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build();
            caseData.setBreathing(breathingSpaceInfo);
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_START, caseData).build();
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);
            assertThat(response.getErrors()).isNotEmpty();
        }

        @Test
        void shouldNotReturnError_WhenAboutToStartIsInvokedOneDefendant() {
            BreathingSpaceLiftInfo breathingSpaceLiftInfo = new BreathingSpaceLiftInfo();
            breathingSpaceLiftInfo.setExpectedEnd(LocalDate.now().minusDays(5));
            BreathingSpaceInfo breathingSpaceInfo = new BreathingSpaceInfo();
            breathingSpaceInfo.setLift(breathingSpaceLiftInfo);
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build();
            caseData.setBreathing(breathingSpaceInfo);
            caseData.setRespondent1ResponseDeadline(LocalDateTime.now().minusDays(15));
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_START, caseData).build();
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);
            assertThat(response.getErrors()).isEmpty();
        }

        @Test
        void shouldReturnDefendantDetails_WhenAboutToStartIsInvokedOneDefendant() {
            BreathingSpaceLiftInfo breathingSpaceLiftInfo = new BreathingSpaceLiftInfo();
            breathingSpaceLiftInfo.setExpectedEnd(LocalDate.now().minusDays(5));
            BreathingSpaceInfo breathingSpaceInfo = new BreathingSpaceInfo();
            breathingSpaceInfo.setLift(breathingSpaceLiftInfo);
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build();
            caseData.setBreathing(breathingSpaceInfo);
            caseData.setRespondent1ResponseDeadline(LocalDateTime.now().minusDays(15));
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_START, caseData).build();
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);
            CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
            assertThat(updatedData.getDefendantDetailsSpec()).isNotNull();
        }

        @Test
        void shouldReturnDefendantDetails_WhenAboutToStartIsInvokedTwoDefendant() {
            BreathingSpaceLiftInfo breathingSpaceLiftInfo = new BreathingSpaceLiftInfo();
            breathingSpaceLiftInfo.setExpectedEnd(LocalDate.now().minusDays(5));
            BreathingSpaceInfo breathingSpaceInfo = new BreathingSpaceInfo();
            breathingSpaceInfo.setLift(breathingSpaceLiftInfo);
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build();
            caseData.setBreathing(breathingSpaceInfo);
            caseData.setRespondent2(PartyBuilder.builder().individual().build());
            caseData.setAddRespondent2(YES);
            caseData.setRespondent2SameLegalRepresentative(YES);
            caseData.setRespondent1ResponseDeadline(LocalDateTime.now().minusDays(15));
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_START, caseData).build();
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);
            CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
            assertThat(updatedData.getDefendantDetailsSpec()).isNotNull();
        }

        @Test
        void shouldReturnError_WhenAboutToStartAndInBreathingSpace() {
            BreathingSpaceEnterInfo breathingSpaceEnterInfo = new BreathingSpaceEnterInfo();
            breathingSpaceEnterInfo.setStart(LocalDate.now().minusDays(10));
            BreathingSpaceInfo breathingSpaceInfo = new BreathingSpaceInfo();
            breathingSpaceInfo.setEnter(breathingSpaceEnterInfo);

            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build();
            caseData.setBreathing(breathingSpaceInfo);

            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_START, caseData).build();
            var response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);
            assertThat(response.getErrors().contains("Default judgment cannot be applied for while claim is "
                + "in breathing space"));
        }

        @Test
        void shouldReturnError_WhenAboutToStartInvokeWhenRespondentResponseLanguageIsBilingual() {
            RespondentLiPResponse respondentLiPResponse  = new RespondentLiPResponse();
            respondentLiPResponse.setRespondent1ResponseLanguage("BOTH");
            CaseDataLiP caseDataLiP = new CaseDataLiP();
            caseDataLiP.setRespondent1LiPResponse(respondentLiPResponse);
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build();
            caseData.setBreathing(BreathingSpaceInfo.builder().lift(null).build());
            caseData.setCaseDataLiP(caseDataLiP);

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
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
            caseData.setRespondent2(PartyBuilder.builder().individual().build());
            caseData.setAddRespondent2(YES);
            caseData.setRespondent2SameLegalRepresentative(YES);
            caseData.setRespondent1ResponseDeadline(LocalDateTime.now().minusDays(15));
            DynamicList dynamicList = new DynamicList();
            dynamicList.setValue(new DynamicListElement(null, "Both"));
            caseData.setDefendantDetailsSpec(dynamicList);

            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
            assertThat(updatedData.getBothDefendantsSpec()).isEqualTo("Both");
        }

        @Test
        void shouldReturnOne_whenHaveOneDefendants() {
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
            caseData.setRespondent2(PartyBuilder.builder().individual().build());
            caseData.setAddRespondent2(YES);
            caseData.setRespondent2SameLegalRepresentative(YES);
            caseData.setRespondent1ResponseDeadline(LocalDateTime.now().minusDays(15));
            DynamicListElement dynamicListElement = new DynamicListElement(null, "Test User");
            DynamicList dynamicList = new DynamicList();
            dynamicList.setValue(dynamicListElement);
            caseData.setDefendantDetailsSpec(dynamicList);

            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
            assertThat(updatedData.getBothDefendantsSpec()).isEqualTo("One");
        }

        @Test
        void shouldReturnOneDefendantText_whenOneDefendant() {
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
            caseData.setRespondent1ResponseDeadline(LocalDateTime.now().minusDays(15));
            DynamicListElement dynamicListElement = new DynamicListElement(null, "Test User");
            DynamicList dynamicList = new DynamicList();
            dynamicList.setValue(dynamicListElement);
            caseData.setDefendantDetailsSpec(dynamicList);

            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
            assertThat(updatedData.getCurrentDefendant())
                .isEqualTo("Has Test User paid some of the amount owed?");
        }

        @Test
        void shouldReturnBothDefendantText_whenTwoDefendant() {
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
            caseData.setRespondent2(PartyBuilder.builder().individual().build());
            caseData.setAddRespondent2(YES);
            caseData.setRespondent2SameLegalRepresentative(YES);
            caseData.setRespondent1ResponseDeadline(LocalDateTime.now().minusDays(15));
            DynamicListElement dynamicListElement = new DynamicListElement(null, "Both Defendants");
            DynamicList dynamicList = new DynamicList();
            dynamicList.setValue(dynamicListElement);
            caseData.setDefendantDetailsSpec(dynamicList);

            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
            assertThat(updatedData.getCurrentDefendant())
                .isEqualTo("Have the defendants paid some of the amount owed?");
        }

        @Test
        void shouldReturnBothDefendant_whenTwoDefendantSelected() {
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
            caseData.setRespondent2(PartyBuilder.builder().individual().build());
            caseData.setAddRespondent2(YES);
            caseData.setRespondent2SameLegalRepresentative(YES);
            caseData.setRespondent1ResponseDeadline(LocalDateTime.now().minusDays(15));
            DynamicListElement element = new DynamicListElement(null, "Both Defendants");
            DynamicList list = new DynamicList();
            list.setValue(element);
            caseData.setDefendantDetailsSpec(list);

            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
            assertThat(updatedData.getCurrentDefendantName())
                .isEqualTo("both defendants");
        }

        @Test
        void shouldReturnDefendantName_whenOneDefendantSelected() {
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
            caseData.setRespondent2(PartyBuilder.builder().individual().build());
            caseData.setAddRespondent2(YES);
            caseData.setRespondent2SameLegalRepresentative(YES);
            caseData.setRespondent1ResponseDeadline(LocalDateTime.now().minusDays(15));
            DynamicListElement element = new DynamicListElement(null, "Steve Rodgers");
            DynamicList list = new DynamicList();
            list.setValue(element);
            caseData.setDefendantDetailsSpec(list);

            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
            assertThat(updatedData.getCurrentDefendantName())
                .isEqualTo("Steve Rodgers");
            assertThat(updatedData.getRegistrationTypeRespondentOne()).isNull();
            assertThat(updatedData.getRegistrationTypeRespondentTwo()).isNull();
        }

        @Test
        void shouldReturnRegistrationInfo_whenOneVOne() {
            when(time.now()).thenReturn(LocalDateTime.of(2023, 2, 20, 11, 11, 11));
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
            caseData.setAddRespondent2(NO);
            caseData.setRespondent1ResponseDeadline(LocalDateTime.now().minusDays(15));
            DynamicListElement element = new DynamicListElement(null, "Steve Rodgers");
            DynamicList list = new DynamicList();
            list.setValue(element);
            caseData.setDefendantDetailsSpec(list);
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
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
            caseData.setAddRespondent2(NO);
            caseData.setAddApplicant2(YesOrNo.YES);
            caseData.setApplicant2(PartyBuilder.builder().individual().build());
            caseData.setRespondent1ResponseDeadline(LocalDateTime.now().minusDays(15));
            DynamicListElement element = new DynamicListElement(null, "Steve Rodgers");
            DynamicList list = new DynamicList();
            list.setValue(element);
            caseData.setDefendantDetailsSpec(list);
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
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
            caseData.setRespondent2(PartyBuilder.builder().individual().build());
            caseData.setAddRespondent2(YES);
            caseData.setRespondent2SameLegalRepresentative(YES);
            caseData.setRespondent1ResponseDeadline(LocalDateTime.now().minusDays(15));
            DynamicListElement element = new DynamicListElement(null, "Steve Rodgers");
            DynamicList list = new DynamicList();
            list.setValue(element);
            caseData.setDefendantDetailsSpec(list);
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData()).doesNotContainKey("registrationTypeRespondentOne");
            assertThat(response.getData()).doesNotContainKey("registrationTypeRespondentTwo");
        }

        @Test
        void shouldReturnRegistrationInfo_whenOneVTwoAndBothDefendantSelected() {
            when(time.now()).thenReturn(LocalDateTime.of(2023, 2, 20, 11, 11, 11));
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
            caseData.setRespondent2(PartyBuilder.builder().individual().build());
            caseData.setAddRespondent2(YES);
            caseData.setRespondent2SameLegalRepresentative(YES);
            caseData.setRespondent1ResponseDeadline(LocalDateTime.now().minusDays(15));
            DynamicListElement element = new DynamicListElement(null, "Both Defendants");
            DynamicList list = new DynamicList();
            list.setValue(element);
            caseData.setDefendantDetailsSpec(list);
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
        void shouldReturnError_whenPartiallyPaid() {
            BigDecimal interestAmount = new BigDecimal(100);

            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
            caseData.setRespondent1ResponseDeadline(LocalDateTime.now().minusDays(15));
            caseData.setPartialPayment(YesOrNo.YES);

            when(interestCalculator.calculateInterest(caseData)).thenReturn(interestAmount);

            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response.getErrors().get(0)).isEqualTo("This feature is currently not available, please see guidance below");
        }

        @Test
        void shouldShowOldFixedCostsPage_whenNoErrorsAndPreClaimIssueFixedCosts() {
            when(interestCalculator.calculateInterest(any()))
                .thenReturn(BigDecimal.valueOf(100));

            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
            caseData.setRespondent1ResponseDeadline(LocalDateTime.now().minusDays(15));
            caseData.setPartialPayment(NO);
            BigDecimal claimAmount = new BigDecimal(2000);
            BigDecimal interestAmount = new BigDecimal(100);
            caseData.setTotalClaimAmount(claimAmount);
            caseData.setTotalInterest(interestAmount);
            caseData.setPartialPaymentAmount("3000");

            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response.getData().get("showOldDJFixedCostsScreen")).isEqualTo("Yes");
            assertThat(response.getData().get("repaymentSummaryObject")).isNull();
        }

        @Test
        void shouldShowNewFixedCostsPage_whenNoErrorsAndJudgmentAmountMoreThan25AndYesClaimIssueFixedCosts() {
            when(interestCalculator.calculateInterest(any()))
                .thenReturn(BigDecimal.valueOf(1));

            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
            caseData.setRespondent1ResponseDeadline(LocalDateTime.now().minusDays(15));
            caseData.setPartialPayment(NO);
            BigDecimal claimAmount = new BigDecimal(2000);
            BigDecimal interestAmount = new BigDecimal(100);
            caseData.setTotalClaimAmount(claimAmount);
            caseData.setTotalInterest(interestAmount);
            Fee fee = new Fee();
            fee.setCalculatedAmountInPence(BigDecimal.valueOf(1));
            caseData.setClaimFee(fee);
            FixedCosts fixedCosts  = new FixedCosts();
            fixedCosts.setClaimFixedCosts(YES);
            fixedCosts.setFixedCostAmount("10000");
            caseData.setFixedCosts(fixedCosts);

            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData().get("showOldDJFixedCostsScreen")).isNull();
            assertThat(response.getData().get("showDJFixedCostsScreen")).isEqualTo("Yes");
            assertThat(response.getData().get("repaymentSummaryObject")).isNull();
        }

        @Test
        void shouldNotShowNewFixedCostsPage_whenJudgmentAmountLessThan25AndYesClaimIssueFixedCostsAndShouldNotCalculateRepaymentBreakdown() {
            when(interestCalculator.calculateInterest(any()))
                .thenReturn(BigDecimal.valueOf(1));

            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
            caseData.setRespondent1ResponseDeadline(LocalDateTime.now().minusDays(15));
            caseData.setPartialPayment(NO);
            BigDecimal claimAmount = new BigDecimal(20);
            BigDecimal interestAmount = new BigDecimal(1);
            caseData.setTotalClaimAmount(claimAmount);
            caseData.setTotalInterest(interestAmount);
            DynamicListElement element = new DynamicListElement(null, "Test User");
            DynamicList list = new DynamicList();
            list.setValue(element);
            caseData.setDefendantDetailsSpec(list);
            Fee fee = new Fee();
            fee.setCalculatedAmountInPence(BigDecimal.valueOf(1));
            caseData.setClaimFee(fee);
            FixedCosts fixedCosts  = new FixedCosts();
            fixedCosts.setClaimFixedCosts(YES);
            fixedCosts.setFixedCostAmount("10");
            caseData.setFixedCosts(fixedCosts);

            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData().get("showOldDJFixedCostsScreen")).isNull();
            assertThat(response.getData().get("showDJFixedCostsScreen")).isEqualTo("No");
            assertThat(response.getData().get("repaymentSummaryObject")).isNotNull();
        }

        @Test
        void shouldShowNewFixedCostsPage_whenJudgmentAmountWithInterestMoreThan25() {
            when(interestCalculator.calculateInterest(any()))
                .thenReturn(BigDecimal.valueOf(1));

            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
            caseData.setRespondent1ResponseDeadline(LocalDateTime.now().minusDays(15));
            caseData.setPartialPayment(NO);
            BigDecimal claimAmount = new BigDecimal(2000);
            BigDecimal interestAmount = new BigDecimal(100);
            caseData.setTotalClaimAmount(claimAmount);
            caseData.setTotalInterest(interestAmount);
            DynamicListElement element = new DynamicListElement(null, "Test User");
            DynamicList list = new DynamicList();
            list.setValue(element);
            caseData.setDefendantDetailsSpec(list);
            Fee fee = new Fee();
            fee.setCalculatedAmountInPence(BigDecimal.valueOf(10000));
            caseData.setClaimFee(fee);
            FixedCosts fixedCosts  = new FixedCosts();
            fixedCosts.setClaimFixedCosts(YES);
            fixedCosts.setFixedCostAmount("100");
            caseData.setFixedCosts(fixedCosts);

            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData().get("showOldDJFixedCostsScreen")).isNull();
            assertThat(response.getData().get("showDJFixedCostsScreen")).isEqualTo("Yes");
            assertThat(response.getData().get("repaymentSummaryObject")).isNull();
        }

        @Test
        void shouldNotShowNewFixedCostsPage_whenNoErrorsAndNoClaimIssueFixedCosts() {
            when(interestCalculator.calculateInterest(any()))
                .thenReturn(BigDecimal.valueOf(1));

            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
            caseData.setRespondent1ResponseDeadline(LocalDateTime.now().minusDays(15));
            caseData.setPartialPayment(NO);
            BigDecimal claimAmount = new BigDecimal(2000);
            BigDecimal interestAmount = new BigDecimal(100);
            caseData.setTotalClaimAmount(claimAmount);
            caseData.setTotalInterest(interestAmount);
            DynamicListElement element = new DynamicListElement(null, "Test User");
            DynamicList list = new DynamicList();
            list.setValue(element);
            caseData.setDefendantDetailsSpec(list);
            Fee fee = new Fee();
            fee.setCalculatedAmountInPence(BigDecimal.valueOf(1));
            caseData.setClaimFee(fee);
            FixedCosts fixedCosts  = new FixedCosts();
            fixedCosts.setClaimFixedCosts(NO);
            caseData.setFixedCosts(fixedCosts);

            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData().get("showOldDJFixedCostsScreen")).isNull();
            assertThat(response.getData().get("showDJFixedCostsScreen")).isNull();
            assertThat(response.getData().get("repaymentSummaryObject")).isNotNull();
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

            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
            caseData.setRepaymentDue(due);
            caseData.setRepaymentSuggestion(suggest);
            caseData.setRepaymentDate(testDate);
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response.getErrors()).isEmpty();
        }

        @Test
        void shouldReturnError_whenRepaymentAmountGreaterThanAmountDue() {
            var testDate = LocalDate.now().plusDays(35);
            String due = "1000"; //in pounds
            String suggest = "110000"; // 1100 pound in pennies

            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
            caseData.setRepaymentDue(due);
            caseData.setRepaymentSuggestion(suggest);
            caseData.setRepaymentDate(testDate);

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

            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
            caseData.setRepaymentDue(due);
            caseData.setRepaymentSuggestion(suggest);
            caseData.setRepaymentDate(testDate);
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response.getErrors()).isEmpty();
        }

        @Test
        void shouldReturnError_whenDateInPastAndNotEligible() {
            String due = "1000"; //in pounds
            String suggest = "10000"; // 100 pound in pennies

            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
            caseData.setRepaymentDue(due);
            caseData.setRepaymentSuggestion(suggest);
            var testDate = LocalDate.now().plusDays(25);
            caseData.setRepaymentDate(testDate);
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            //eligible date is 31 days in the future, but as text says "after", we set text to one day previous i.e.
            //If 7th is the eligible date, text will say "after the 6th".
            LocalDate eligibleDate = LocalDate.now().plusDays(30);
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

            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
            caseData.setRespondent1ResponseDeadline(LocalDateTime.now().minusDays(15));
            caseData.setPartialPayment(YesOrNo.YES);
            caseData.setPaymentSetDate(LocalDate.now().minusDays(15));
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response.getErrors().get(0)).isEqualTo("Payment Date cannot be past date");
        }

        @Test
        void shouldNotReturnError_whenPastPaymentDate() {

            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
            caseData.setRespondent1ResponseDeadline(LocalDateTime.now().minusDays(15));
            caseData.setPartialPayment(YesOrNo.YES);
            caseData.setPaymentSetDate(LocalDate.now().plusDays(15));
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response.getErrors()).isEmpty();
        }
    }

    @Nested
    class RepaymentBreakdownCallback {

        private static final String PAGE_ID = "repaymentBreakdown";

        @Test
        void shouldReturnFixedAmount_whenJudgmentAmountGreaterThan5000AndClaimIssueFixedCostsYesClaimDJFixedCosts() {
            when(interestCalculator.calculateInterest(any()))
                .thenReturn(BigDecimal.valueOf(0)
                );

            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
            caseData.setRespondent1ResponseDeadline(LocalDateTime.now().minusDays(15));
            caseData.setPartialPayment(YES);
            caseData.setPaymentSetDate(LocalDate.now().minusDays(15));
            caseData.setPartialPaymentAmount("100");
            caseData.setTotalClaimAmount(BigDecimal.valueOf(5002));
            FixedCosts fixedCosts = new FixedCosts();
            fixedCosts.setClaimFixedCosts(YES);
            fixedCosts.setFixedCostAmount("10000");
            caseData.setFixedCosts(fixedCosts);
            caseData.setClaimFixedCostsOnEntryDJ(YES);
            DynamicListElement element = new DynamicListElement(null, "Test User");
            DynamicList list = new DynamicList();
            list.setValue(element);
            caseData.setDefendantDetailsSpec(list);
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            String test = "The judgment will order " + caseData.getDefendantDetailsSpec().getValue().getLabel()
                + " to pay £5132.00, including the claim fee and"
                + " interest, if applicable, as shown:\n"
                + "### Claim amount \n"
                + " £5002.00\n"
                + " ### Fixed cost amount \n"
                + "£130.00\n"
                + "### Claim fee amount \n"
                + " £1.00\n"
                + " ## Subtotal \n"
                + " £5133.00\n"
                + "\n"
                + " ### Amount already paid \n"
                + "£1.00\n"
                + " ## Total still owed \n"
                + " £5132.00";
            assertThat(response.getData().get("repaymentSummaryObject")).isEqualTo(test);
        }

        @Test
        void shouldReturnFixedAmount_whenJudgmentAmountGreaterThan5000AndClaimIssueFixedCostsYesAndClaimDJFixedCostsNo() {
            when(interestCalculator.calculateInterest(any()))
                .thenReturn(BigDecimal.valueOf(0)
                );

            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
            caseData.setRespondent1ResponseDeadline(LocalDateTime.now().minusDays(15));
            caseData.setPartialPayment(YES);
            caseData.setPaymentSetDate(LocalDate.now().minusDays(15));
            caseData.setPartialPaymentAmount("100");
            caseData.setTotalClaimAmount(BigDecimal.valueOf(5001));
            FixedCosts fixedCosts = new FixedCosts();
            fixedCosts.setClaimFixedCosts(YES);
            fixedCosts.setFixedCostAmount("10000");
            caseData.setFixedCosts(fixedCosts);
            caseData.setClaimFixedCostsOnEntryDJ(NO);
            DynamicListElement element = new DynamicListElement(null, "Test User");
            DynamicList list = new DynamicList();
            list.setValue(element);
            caseData.setDefendantDetailsSpec(list);
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            String test = "The judgment will order " + caseData.getDefendantDetailsSpec().getValue().getLabel()
                + " to pay £5101.00, including the claim fee and"
                + " interest, if applicable, as shown:\n"
                + "### Claim amount \n"
                + " £5001.00\n"
                + " ### Fixed cost amount \n"
                + "£100.00\n"
                + "### Claim fee amount \n"
                + " £1.00\n"
                + " ## Subtotal \n"
                + " £5102.00\n"
                + "\n"
                + " ### Amount already paid \n"
                + "£1.00\n"
                + " ## Total still owed \n"
                + " £5101.00";
            assertThat(response.getData().get("repaymentSummaryObject")).isEqualTo(test);
        }

        @Test
        void shouldReturnFixedAmount_whenJudgmentAmountMoreThan25LessThan5000AndClaimIssueFixedCostsYesClaimDJFixedCosts() {
            when(interestCalculator.calculateInterest(any()))
                .thenReturn(BigDecimal.valueOf(0)
                );

            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
            caseData.setRespondent1ResponseDeadline(LocalDateTime.now().minusDays(15));
            caseData.setPartialPayment(YES);
            caseData.setPaymentSetDate(LocalDate.now().minusDays(15));
            caseData.setPartialPaymentAmount("100");
            caseData.setTotalClaimAmount(BigDecimal.valueOf(3001));
            FixedCosts fixedCosts = new FixedCosts();
            fixedCosts.setClaimFixedCosts(YES);
            fixedCosts.setFixedCostAmount("10000");
            caseData.setFixedCosts(fixedCosts);
            caseData.setClaimFixedCostsOnEntryDJ(YES);
            DynamicListElement element = new DynamicListElement(null, "Test User");
            DynamicList list = new DynamicList();
            list.setValue(element);
            caseData.setDefendantDetailsSpec(list);
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            String test = "The judgment will order " + caseData.getDefendantDetailsSpec().getValue().getLabel()
                + " to pay £3123.00, including the claim fee and"
                + " interest, if applicable, as shown:\n"
                + "### Claim amount \n"
                + " £3001.00\n"
                + " ### Fixed cost amount \n"
                + "£122.00\n"
                + "### Claim fee amount \n"
                + " £1.00\n"
                + " ## Subtotal \n"
                + " £3124.00\n"
                + "\n"
                + " ### Amount already paid \n"
                + "£1.00\n"
                + " ## Total still owed \n"
                + " £3123.00";
            assertThat(response.getData().get("repaymentSummaryObject")).isEqualTo(test);
        }

        @Test
        void shouldReturnFixedAmount_whenJudgmentAmountMoreThan25LessThan5000AndClaimIssueFixedCostsYesAndNoClaimDJFixedCosts() {
            when(interestCalculator.calculateInterest(any()))
                .thenReturn(BigDecimal.valueOf(0)
                );

            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
            caseData.setRespondent1ResponseDeadline(LocalDateTime.now().minusDays(15));
            caseData.setPartialPayment(YES);
            caseData.setPaymentSetDate(LocalDate.now().minusDays(15));
            caseData.setPartialPaymentAmount("100");
            caseData.setTotalClaimAmount(BigDecimal.valueOf(3001));
            FixedCosts fixedCosts = new FixedCosts();
            fixedCosts.setClaimFixedCosts(YES);
            fixedCosts.setFixedCostAmount("10000");
            caseData.setFixedCosts(fixedCosts);
            caseData.setClaimFixedCostsOnEntryDJ(NO);
            DynamicListElement element = new DynamicListElement(null, "Test User");
            DynamicList list = new DynamicList();
            list.setValue(element);
            caseData.setDefendantDetailsSpec(list);
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            String test = "The judgment will order " + caseData.getDefendantDetailsSpec().getValue().getLabel()
                + " to pay £3101.00, including the claim fee and"
                + " interest, if applicable, as shown:\n"
                + "### Claim amount \n"
                + " £3001.00\n"
                + " ### Fixed cost amount \n"
                + "£100.00\n"
                + "### Claim fee amount \n"
                + " £1.00\n"
                + " ## Subtotal \n"
                + " £3102.00\n"
                + "\n"
                + " ### Amount already paid \n"
                + "£1.00\n"
                + " ## Total still owed \n"
                + " £3101.00";
            assertThat(response.getData().get("repaymentSummaryObject")).isEqualTo(test);
        }

        @Test
        void shouldReturnFixedAmount_whenJudgmentAmountLessThan25AndClaimIssueFixedCostsYes() {
            when(interestCalculator.calculateInterest(any()))
                .thenReturn(BigDecimal.valueOf(0)
                );

            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
            caseData.setRespondent1ResponseDeadline(LocalDateTime.now().minusDays(15));
            caseData.setPartialPayment(YES);
            caseData.setPaymentSetDate(LocalDate.now().minusDays(15));
            caseData.setPartialPaymentAmount("299500");
            caseData.setTotalClaimAmount(BigDecimal.valueOf(3000));
            FixedCosts fixedCosts = new FixedCosts();
            fixedCosts.setClaimFixedCosts(YES);
            fixedCosts.setFixedCostAmount("10000");
            caseData.setFixedCosts(fixedCosts);
            DynamicListElement element = new DynamicListElement(null, "Test User");
            DynamicList list = new DynamicList();
            list.setValue(element);
            caseData.setDefendantDetailsSpec(list);
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            String test = "The judgment will order " + caseData.getDefendantDetailsSpec().getValue().getLabel()
                + " to pay £106.00, including the claim fee and"
                + " interest, if applicable, as shown:\n"
                + "### Claim amount \n"
                + " £3000.00\n"
                + " ### Fixed cost amount \n"
                + "£100.00\n"
                + "### Claim fee amount \n"
                + " £1.00\n"
                + " ## Subtotal \n"
                + " £3101.00\n"
                + "\n"
                + " ### Amount already paid \n"
                + "£2995.00\n"
                + " ## Total still owed \n"
                + " £106.00";
            assertThat(response.getData().get("repaymentSummaryObject")).isEqualTo(test);
        }

        @Test
        void shouldReturnFixedAmount_whenJudgmentAmountLessThan25AndClaimIssueFixedCostsNo() {
            when(interestCalculator.calculateInterest(any()))
                .thenReturn(BigDecimal.valueOf(0)
                );

            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
            caseData.setRespondent1ResponseDeadline(LocalDateTime.now().minusDays(15));
            caseData.setPartialPayment(YES);
            caseData.setPaymentSetDate(LocalDate.now().minusDays(15));
            caseData.setPartialPaymentAmount("299500");
            caseData.setTotalClaimAmount(BigDecimal.valueOf(3000));
            FixedCosts fixedCosts = new FixedCosts();
            fixedCosts.setClaimFixedCosts(NO);
            caseData.setFixedCosts(fixedCosts);
            DynamicListElement element = new DynamicListElement(null, "Test User");
            DynamicList list = new DynamicList();
            list.setValue(element);
            caseData.setDefendantDetailsSpec(list);
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            String test = "The judgment will order " + caseData.getDefendantDetailsSpec().getValue().getLabel()
                + " to pay £6.00, including the claim fee and"
                + " interest, if applicable, as shown:\n"
                + "### Claim amount \n"
                + " £3000.00\n"
                + "### Claim fee amount \n"
                + " £1.00\n"
                + " ## Subtotal \n"
                + " £3001.00\n"
                + "\n"
                + " ### Amount already paid \n"
                + "£2995.00\n"
                + " ## Total still owed \n"
                + " £6.00";
            assertThat(response.getData().get("repaymentSummaryObject")).isEqualTo(test);
        }

        @Test
        void shouldReturnFixedAmount_whenClaimAmountLessthan5000() {
            when(interestCalculator.calculateInterest(any()))
                .thenReturn(BigDecimal.valueOf(100)
                );
            Fee fee = new Fee();
            fee.setCalculatedAmountInPence(BigDecimal.valueOf(100));
            fee.setVersion("1");
            fee.setCode("CODE");
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
            caseData.setRespondent1ResponseDeadline(LocalDateTime.now().minusDays(15));
            caseData.setPartialPayment(YesOrNo.YES);
            caseData.setClaimFee(fee);
            caseData.setPaymentSetDate(LocalDate.now().minusDays(15));
            caseData.setPartialPaymentAmount("100");
            caseData.setTotalClaimAmount(BigDecimal.valueOf(1010));
            caseData.setPaymentConfirmationDecisionSpec(YesOrNo.YES);
            caseData.setPartialPayment(YesOrNo.YES);
            DynamicListElement element = new DynamicListElement(null, "Test User");
            DynamicList list = new DynamicList();
            list.setValue(element);
            caseData.setDefendantDetailsSpec(list);
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            String test = "The judgment will order " + caseData.getDefendantDetailsSpec().getValue().getLabel()
                + " to pay £1212.00, including the claim fee and"
                + " interest, if applicable, as shown:\n"
                + "### Claim amount \n"
                + " £1010.00\n"
                + " ### Claim interest amount \n"
                + "£100.00\n"
                + " ### Fixed cost amount \n"
                + "£102.00\n"
                + "### Claim fee amount \n"
                + " £1.00\n"
                + " ## Subtotal \n"
                + " £1213.00\n"
                + "\n"
                + " ### Amount already paid \n"
                + "£1.00\n"
                + " ## Total still owed \n"
                + " £1212.00";

            assertThat(response.getData().get("repaymentSummaryObject")).isEqualTo(test);

        }

        @Test
        void shouldReturnFixedAmount_whenClaimAmountLessthan500() {
            when(interestCalculator.calculateInterest(any()))
                .thenReturn(BigDecimal.valueOf(100)
                );
            Fee fee = new Fee();
            fee.setCalculatedAmountInPence(BigDecimal.valueOf(100));
            fee.setVersion("1");
            fee.setCode("CODE");
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
            caseData.setRespondent1ResponseDeadline(LocalDateTime.now().minusDays(15));
            caseData.setPartialPayment(YesOrNo.YES);
            caseData.setClaimFee(fee);
            caseData.setPaymentSetDate(LocalDate.now().minusDays(15));
            caseData.setPartialPaymentAmount("100");
            caseData.setTotalClaimAmount(BigDecimal.valueOf(499));
            caseData.setPaymentConfirmationDecisionSpec(YesOrNo.YES);
            caseData.setPartialPayment(YesOrNo.YES);
            DynamicListElement element = new DynamicListElement(null, "Test User");
            DynamicList list = new DynamicList();
            list.setValue(element);
            caseData.setDefendantDetailsSpec(list);
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            String test = "The judgment will order " + caseData.getDefendantDetailsSpec().getValue().getLabel()
                + " to pay £671.00, including the claim fee and"
                + " interest, if applicable, as shown:\n"
                + "### Claim amount \n"
                + " £499.00\n"
                + " ### Claim interest amount \n"
                + "£100.00\n"
                + " ### Fixed cost amount \n"
                + "£72.00\n"
                + "### Claim fee amount \n"
                + " £1.00\n"
                + " ## Subtotal \n"
                + " £672.00\n"
                + "\n"
                + " ### Amount already paid \n"
                + "£1.00\n"
                + " ## Total still owed \n"
                + " £671.00";
            assertThat(response.getData().get("repaymentSummaryObject")).isEqualTo(test);
        }

        @Test
        void shouldReturnFixedAmount_whenClaimAmountLessthan1000AndGreaterThan500() {
            when(interestCalculator.calculateInterest(any()))
                .thenReturn(BigDecimal.valueOf(100)
                );
            Fee fee = new Fee();
            fee.setCalculatedAmountInPence(BigDecimal.valueOf(100));
            fee.setVersion("1");
            fee.setCode("CODE");
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
            caseData.setRespondent1ResponseDeadline(LocalDateTime.now().minusDays(15));
            caseData.setPartialPayment(YesOrNo.YES);
            caseData.setClaimFee(fee);
            caseData.setPaymentSetDate(LocalDate.now().minusDays(15));
            caseData.setPartialPaymentAmount("100");
            caseData.setTotalClaimAmount(BigDecimal.valueOf(999));
            caseData.setPaymentConfirmationDecisionSpec(YesOrNo.YES);
            caseData.setPartialPayment(YesOrNo.YES);
            DynamicListElement element = new DynamicListElement(null, "Test User");
            DynamicList list = new DynamicList();
            list.setValue(element);
            caseData.setDefendantDetailsSpec(list);
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
            String test = "The judgment will order " + caseData.getDefendantDetailsSpec().getValue().getLabel()
                + " to pay £1191.00, including the claim fee and"
                + " interest, if applicable, as shown:\n"
                + "### Claim amount \n"
                + " £999.00\n"
                + " ### Claim interest amount \n"
                + "£100.00\n"
                + " ### Fixed cost amount \n"
                + "£92.00\n"
                + "### Claim fee amount \n"
                + " £1.00\n"
                + " ## Subtotal \n"
                + " £1192.00\n"
                + "\n"
                + " ### Amount already paid \n"
                + "£1.00\n"
                + " ## Total still owed \n"
                + " £1191.00";
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response.getData().get("repaymentSummaryObject")).isEqualTo(test);
        }

        @Test
        void shouldReturnFixedAmount_whenClaimAmountGreaterthan5000() {
            when(interestCalculator.calculateInterest(any()))
                .thenReturn(BigDecimal.valueOf(0)
                );
            Fee fee = new Fee();
            fee.setCalculatedAmountInPence(BigDecimal.valueOf(100));
            fee.setVersion("1");
            fee.setCode("CODE");
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
            caseData.setRespondent1ResponseDeadline(LocalDateTime.now().minusDays(15));
            caseData.setPartialPayment(YesOrNo.YES);
            caseData.setClaimFee(fee);
            caseData.setPaymentSetDate(LocalDate.now().minusDays(15));
            caseData.setPartialPaymentAmount("100");
            caseData.setTotalClaimAmount(BigDecimal.valueOf(5001));
            DynamicListElement element = new DynamicListElement(null, "Test User");
            DynamicList list = new DynamicList();
            list.setValue(element);
            caseData.setDefendantDetailsSpec(list);
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
            Fee fee = new Fee();
            fee.setCalculatedAmountInPence(BigDecimal.valueOf(100));
            fee.setVersion("1");
            fee.setCode("CODE");
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
            caseData.setRespondent2(PartyBuilder.builder().individual().build());
            caseData.setAddRespondent2(YES);
            caseData.setRespondent2SameLegalRepresentative(YES);
            caseData.setRespondent1ResponseDeadline(LocalDateTime.now().minusDays(15));
            caseData.setPartialPayment(YesOrNo.YES);
            caseData.setClaimFee(fee);
            caseData.setPaymentSetDate(LocalDate.now().minusDays(15));
            caseData.setPartialPaymentAmount("100");
            caseData.setTotalClaimAmount(BigDecimal.valueOf(5001));
            DynamicListElement element = new DynamicListElement(null, "Both Defendants");
            DynamicList list = new DynamicList();
            list.setValue(element);
            caseData.setDefendantDetailsSpec(list);
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
            Fee fee = new Fee();
            fee.setCalculatedAmountInPence(BigDecimal.valueOf(100));
            fee.setVersion("1");
            fee.setCode("CODE");
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
            caseData.setRespondent1ResponseDeadline(LocalDateTime.now().minusDays(15));
            caseData.setPartialPayment(YesOrNo.YES);
            caseData.setPaymentSetDate(LocalDate.now().minusDays(15));
            caseData.setPartialPaymentAmount("100");
            caseData.setClaimFee(fee);
            caseData.setTotalClaimAmount(BigDecimal.valueOf(1010));
            caseData.setPaymentConfirmationDecisionSpec(YesOrNo.YES);
            caseData.setPartialPayment(YesOrNo.YES);
            DynamicListElement element = new DynamicListElement(null, "Test User");
            DynamicList list = new DynamicList();
            list.setValue(element);
            caseData.setDefendantDetailsSpec(list);
            caseData.setSpecRespondent1Represented(NO);
            caseData.setRespondent1Represented(null);
            CallbackParams params = callbackParamsOf(V_1, caseData, MID, PAGE_ID);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            String test =
                "The Judgment request will be reviewed by the court, this case will proceed offline, you will receive any further updates by post.\n"
                    + "### Claim amount \n"
                    + " £1010.00\n"
                    + " ### Claim interest amount \n"
                    + "£100.00\n"
                    + " ### Fixed cost amount \n"
                    + "£102.00\n"
                    + "### Claim fee amount \n"
                    + " £1.00\n"
                    + " ## Subtotal \n"
                    + " £1213.00\n"
                    + "\n"
                    + " ### Amount already paid \n"
                    + "£1.00\n"
                    + " ## Total still owed \n"
                    + " £1212.00";

            assertThat(response.getData().get("repaymentSummaryObject")).isEqualTo(test);

        }

        @Test
        void shouldReturnClaimFee_whenHWFRemissionGrantedLRvLiP() {
            when(interestCalculator.calculateInterest(any()))
                .thenReturn(BigDecimal.valueOf(100)
                );
            Fee fee = new Fee();
            fee.setCalculatedAmountInPence(BigDecimal.valueOf(100));
            fee.setVersion("1");
            fee.setCode("CODE");
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
            caseData.setRespondent1ResponseDeadline(LocalDateTime.now().minusDays(15));
            caseData.setPartialPayment(YesOrNo.YES);
            caseData.setClaimFee(fee);
            caseData.setPaymentSetDate(LocalDate.now().minusDays(15));
            caseData.setPartialPaymentAmount("100");
            caseData.setTotalClaimAmount(BigDecimal.valueOf(1010));
            caseData.setPaymentConfirmationDecisionSpec(YesOrNo.YES);
            caseData.setPartialPayment(YesOrNo.YES);
            DynamicListElement element = new DynamicListElement(null, "Test User");
            DynamicList list = new DynamicList();
            list.setValue(element);
            caseData.setDefendantDetailsSpec(list);
            caseData.setSpecRespondent1Represented(NO);
            caseData.setRespondent1Represented(null);
            HelpWithFeesDetails helpWithFeesDetails = new HelpWithFeesDetails();
            helpWithFeesDetails.setOutstandingFeeInPounds(BigDecimal.ZERO);
            caseData.setClaimIssuedHwfDetails(helpWithFeesDetails);
            caseData.setHwfFeeType(FeeType.CLAIMISSUED);
            CallbackParams params = callbackParamsOf(V_1, caseData, MID, PAGE_ID);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            String test =
                "The Judgment request will be reviewed by the court, this case will proceed offline, you will receive any further updates by post.\n"
                    + "### Claim amount \n"
                    + " £1010.00\n"
                    + " ### Claim interest amount \n"
                    + "£100.00\n"
                    + " ### Fixed cost amount \n"
                    + "£102.00\n"
                    + "### Claim fee amount \n"
                    + " £0.00\n"
                    + " ## Subtotal \n"
                    + " £1212.00\n"
                    + "\n"
                    + " ### Amount already paid \n"
                    + "£1.00\n"
                    + " ## Total still owed \n"
                    + " £1211.00";

            assertThat(response.getData().get("repaymentSummaryObject")).isEqualTo(test);
        }
    }

    private static void assertInterestIsPopulated(AboutToStartOrSubmitCallbackResponse response, int val) {
        assertThat(response.getData().get("totalInterest")).isEqualTo(BigDecimal.valueOf(val));
    }

    @Nested
    class MidRepaymentTotal {

        private static final String PAGE_ID = "repaymentTotal";

        @Test
        void shouldGetException_whenIsCalledAndTheOverallIsNull() {
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
            caseData.setRespondent1ResponseDeadline(LocalDateTime.now().minusDays(15));
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
            Flags respondent1Flags = new Flags();
            respondent1Flags.setPartyName("respondent1name");
            respondent1Flags.setRoleOnCase("respondent1");
            Party respondent = new Party();
            respondent.setIndividualFirstName("Dis");
            respondent.setIndividualLastName("Guy");
            respondent.setType(INDIVIDUAL);
            respondent.setFlags(respondent1Flags);

            CaseData caseDataBefore = CaseDataBuilder.builder()
                .atStateApplicantRespondToDefenceAndProceed()
                .respondent1(respondent).build();
            caseDataBefore.setRespondent1DetailsForClaimDetailsTab(respondent);
            caseDataBefore.setCaseNameHmctsInternal("Mr. John Rambo v Dis Guy");
            caseDataBefore.setCaseNamePublic("'John Rambo' v 'Dis Guy'");

            when(interestCalculator.calculateInterest(any()))
                .thenReturn(BigDecimal.valueOf(0)
                );
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
            caseData.setRespondent1ResponseDeadline(LocalDateTime.now().minusDays(15));
            DynamicListElement element = new DynamicListElement(null, "John Smith");
            DynamicList list = new DynamicList();
            list.setValue(element);
            caseData.setDefendantDetailsSpec(list);
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT, caseDataBefore.toMap(mapper));

            when(featureToggleService.isJudgmentOnlineLive()).thenReturn(false);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
            assertThat(updatedData.getBusinessProcess()).isNotNull();
            assertThat(updatedData.getBusinessProcess().getCamundaEvent()).isEqualTo(DEFAULT_JUDGEMENT_SPEC.name());
            assertThat(response.getState()).isEqualTo(CaseState.PROCEEDS_IN_HERITAGE_SYSTEM.name());
            assertInterestIsPopulated(response, 0);
        }

        @Test
        void shouldGenerateDocumentAndContinueOnline_whenIsCalled1v1() {
            Flags respondent1Flags = new Flags();
            respondent1Flags.setPartyName("respondent1name");
            respondent1Flags.setRoleOnCase("respondent1");
            Party respondent = new Party();
            respondent.setIndividualFirstName("Dis");
            respondent.setIndividualLastName("Guy");
            respondent.setType(INDIVIDUAL);
            respondent.setFlags(respondent1Flags);

            CaseData caseDataBefore = CaseDataBuilder.builder()
                .atStateApplicantRespondToDefenceAndProceed()
                .respondent1(respondent).build();
            caseDataBefore.setRespondent1DetailsForClaimDetailsTab(respondent);
            caseDataBefore.setCaseNameHmctsInternal("Mr. John Rambo v Dis Guy");
            caseDataBefore.setCaseNamePublic("'John Rambo' v 'Dis Guy'");

            when(interestCalculator.calculateInterest(any()))
                .thenReturn(BigDecimal.valueOf(0)
                );
            when(addressMapper.toRoboticsAddress(any())).thenReturn(new RoboticsAddress());
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
            caseData.setRespondent1ResponseDeadline(LocalDateTime.now().minusDays(15));
            caseData.setPartialPaymentAmount("10");
            caseData.setTotalClaimAmount(BigDecimal.valueOf(1010));
            caseData.setPartialPayment(YES);
            caseData.setPaymentTypeSelection(DJPaymentTypeSelection.IMMEDIATELY);
            CaseLocationCivil caseLocationCivil = new CaseLocationCivil();
            caseLocationCivil.setBaseLocation("0123");
            caseLocationCivil.setRegion("0321");;
            caseData.setCaseManagementLocation(caseLocationCivil);
            DynamicListElement element = new DynamicListElement(null, "John Smith");
            DynamicList list = new DynamicList();
            list.setValue(element);
            caseData.setDefendantDetailsSpec(list);
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT, caseDataBefore.toMap(mapper));

            when(featureToggleService.isJudgmentOnlineLive()).thenReturn(true);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
            assertThat(updatedData.getBusinessProcess()).isNotNull();
            assertThat(updatedData.getBusinessProcess().getCamundaEvent()).isEqualTo(DEFAULT_JUDGEMENT_NON_DIVERGENT_SPEC.name());
            assertThat(response.getState()).isEqualTo(CaseState.All_FINAL_ORDERS_ISSUED.name());
            // Use Map-based verification for activeJudgment due to complex nested objects
            assertThat(response.getData()).extracting("activeJudgment").isNotNull();
            assertThat(response.getData().get("activeJudgment")).extracting("state").isEqualTo("ISSUED");
            assertThat(response.getData().get("activeJudgment")).extracting("type").isEqualTo("DEFAULT_JUDGMENT");
            assertThat(response.getData().get("activeJudgment")).extracting("judgmentId").isEqualTo(1);
            assertThat(response.getData().get("activeJudgment")).extracting("isRegisterWithRTL").isEqualTo("Yes");
            assertThat(response.getData().get("activeJudgment")).extracting("defendant1Name").isEqualTo("Mr. Sole Trader");
            assertThat(response.getData().get("activeJudgment")).extracting("defendant1Address").isNotNull();
            assertInterestIsPopulated(response, 0);

        }

        @Test
        void shouldGenerateDocumentAndContinueOnline_whenIsCalled1v2NonDivergent() {
            Flags respondent1Flags = new Flags();
            respondent1Flags.setPartyName("respondent1name");
            respondent1Flags.setRoleOnCase("respondent1");
            Party respondent = new Party();
            respondent.setIndividualFirstName("Dis");
            respondent.setIndividualLastName("Guy");
            respondent.setType(INDIVIDUAL);
            respondent.setFlags(respondent1Flags);

            CaseData caseDataBefore = CaseDataBuilder.builder()
                .atStateApplicantRespondToDefenceAndProceed()
                .respondent1(respondent).build();
            caseDataBefore.setRespondent1DetailsForClaimDetailsTab(respondent);
            caseDataBefore.setCaseNameHmctsInternal("Mr. John Rambo v Dis Guy");
            caseDataBefore.setCaseNamePublic("'John Rambo' v 'Dis Guy'");

            when(interestCalculator.calculateInterest(any()))
                .thenReturn(BigDecimal.valueOf(0)
                );
            when(addressMapper.toRoboticsAddress(any())).thenReturn(new RoboticsAddress());
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
            caseData.setRespondent1ResponseDeadline(LocalDateTime.now().minusDays(15));
            caseData.setApplicant1(PartyBuilder.builder().individual().build());
            caseData.setRespondent1(PartyBuilder.builder().individual().build());
            caseData.setRespondent2(PartyBuilder.builder().individual().build());
            caseData.setAddRespondent2(YesOrNo.YES);
            caseData.setRespondent2SameLegalRepresentative(YesOrNo.YES);
            caseData.setPartialPaymentAmount("10");
            caseData.setTotalClaimAmount(BigDecimal.valueOf(1010));
            caseData.setPartialPayment(YES);
            caseData.setPaymentTypeSelection(DJPaymentTypeSelection.IMMEDIATELY);
            CaseLocationCivil caseLocationCivil =  new CaseLocationCivil();
            caseLocationCivil.setBaseLocation("0123");
            caseLocationCivil.setRegion("0321");
            caseData.setCaseManagementLocation(caseLocationCivil);
            DynamicListElement element = new DynamicListElement(null, "Both Defendants");
            DynamicList list = new DynamicList();
            list.setValue(element);
            caseData.setDefendantDetailsSpec(list);
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT, caseDataBefore.toMap(mapper));

            when(featureToggleService.isJudgmentOnlineLive()).thenReturn(true);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
            assertThat(updatedData.getBusinessProcess()).isNotNull();
            assertThat(updatedData.getBusinessProcess().getCamundaEvent()).isEqualTo(DEFAULT_JUDGEMENT_NON_DIVERGENT_SPEC.name());
            assertThat(response.getState()).isEqualTo(CaseState.All_FINAL_ORDERS_ISSUED.name());
            assertThat(response.getData()).extracting("activeJudgment").isNotNull();
            assertThat(response.getData().get("activeJudgment")).extracting("state").isEqualTo("ISSUED");
            assertThat(response.getData().get("activeJudgment")).extracting("type").isEqualTo("DEFAULT_JUDGMENT");
            assertThat(response.getData().get("activeJudgment")).extracting("judgmentId").isEqualTo(1);
            assertThat(response.getData().get("activeJudgment")).extracting("isRegisterWithRTL").isEqualTo("Yes");
            assertThat(response.getData().get("activeJudgment")).extracting("defendant1Name").isEqualTo("Mr. John Rambo");
            assertThat(response.getData().get("activeJudgment")).extracting("defendant1Address").isNotNull();
            assertThat(response.getData().get("activeJudgment")).extracting("defendant2Name").isEqualTo("Mr. John Rambo");
            assertThat(response.getData().get("activeJudgment")).extracting("defendant2Address").isNotNull();
            assertThat(response.getData().get("activeJudgment")).extracting("defendant2Dob").isNotNull();
            assertInterestIsPopulated(response, 0);

        }

        @Test
        void shouldNotGenerateDocumentAndContinueOffline_whenIsCalled1v2Divergent() {
            Flags respondent1Flags = new Flags();
            respondent1Flags.setPartyName("respondent1name");
            respondent1Flags.setRoleOnCase("respondent1");
            Party respondent = new Party();
            respondent.setIndividualFirstName("Dis");
            respondent.setIndividualLastName("Guy");
            respondent.setType(INDIVIDUAL);
            respondent.setFlags(respondent1Flags);

            CaseData caseDataBefore = CaseDataBuilder.builder()
                .atStateApplicantRespondToDefenceAndProceed()
                .respondent1(respondent).build();
            caseDataBefore.setRespondent1DetailsForClaimDetailsTab(respondent);
            caseDataBefore.setCaseNameHmctsInternal("Mr. John Rambo v Dis Guy");
            caseDataBefore.setCaseNamePublic("'John Rambo' v 'Dis Guy'");

            when(interestCalculator.calculateInterest(any()))
                .thenReturn(BigDecimal.valueOf(0)
                );
            when(addressMapper.toRoboticsAddress(any())).thenReturn(new RoboticsAddress());
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
            caseData.setRespondent1ResponseDeadline(LocalDateTime.now().minusDays(15));
            caseData.setApplicant1(PartyBuilder.builder().individual().build());
            caseData.setRespondent1(PartyBuilder.builder().individual().build());
            caseData.setRespondent2(PartyBuilder.builder().individual().build());
            caseData.setAddRespondent2(YesOrNo.YES);
            caseData.setRespondent2SameLegalRepresentative(YesOrNo.YES);
            caseData.setPartialPaymentAmount("10");
            caseData.setPaymentTypeSelection(DJPaymentTypeSelection.IMMEDIATELY);
            caseData.setTotalClaimAmount(BigDecimal.valueOf(1010));
            caseData.setPartialPayment(YES);
            CaseLocationCivil caseLocationCivil  = new CaseLocationCivil();
            caseLocationCivil.setBaseLocation("0123");
            caseLocationCivil.setRegion("0321");
            caseData.setCaseManagementLocation(caseLocationCivil);
            DynamicListElement element = new DynamicListElement(null, "John Smith");
            DynamicList list = new DynamicList();
            list.setValue(element);
            caseData.setDefendantDetailsSpec(list);
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT, caseDataBefore.toMap(mapper));

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
            assertThat(response.getData().get("activeJudgment")).extracting("defendant1Name").isEqualTo("Mr. John Rambo");
            assertThat(response.getData().get("activeJudgment")).extracting("defendant1Address").isNotNull();
            assertThat(response.getData().get("activeJudgment")).extracting("defendant2Name").isEqualTo("Mr. John Rambo");
            assertThat(response.getData().get("activeJudgment")).extracting("defendant2Address").isNotNull();
            assertThat(response.getData().get("activeJudgment")).extracting("defendant2Dob").isNotNull();
            assertInterestIsPopulated(response, 0);

        }

        @Test
        void shouldNotGenerateDocumentAndContinueOffline_whenIsCalled2v1JOIsNotLive() {
            Flags respondent1Flags = new Flags();
            respondent1Flags.setPartyName("respondent1name");
            respondent1Flags.setRoleOnCase("respondent1");
            Party respondent = new Party();
            respondent.setIndividualFirstName("Dis");
            respondent.setIndividualLastName("Guy");
            respondent.setType(INDIVIDUAL);
            respondent.setFlags(respondent1Flags);

            CaseData caseDataBefore = CaseDataBuilder.builder()
                .atStateApplicantRespondToDefenceAndProceed()
                .respondent1(respondent).build();
            caseDataBefore.setRespondent1DetailsForClaimDetailsTab(respondent);
            caseDataBefore.setCaseNameHmctsInternal("Mr. John Rambo v Dis Guy");
            caseDataBefore.setCaseNamePublic("'John Rambo' v 'Dis Guy'");

            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
            caseData.setRespondent1ResponseDeadline(LocalDateTime.now().minusDays(15));
            caseData.setApplicant1(PartyBuilder.builder().individual().build());
            caseData.setApplicant2(PartyBuilder.builder().individual().build());
            caseData.setAddApplicant2(YesOrNo.YES);
            DynamicListElement element = new DynamicListElement(null, "John Smith");
            DynamicList list = new DynamicList();
            list.setValue(element);
            caseData.setDefendantDetailsSpec(list);

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT, caseDataBefore.toMap(mapper));
            when(featureToggleService.isJudgmentOnlineLive()).thenReturn(false);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response.getData()).extracting("businessProcess").isNotNull();
            assertThat(response.getData().get("businessProcess")).extracting("camundaEvent").isEqualTo(DEFAULT_JUDGEMENT_SPEC.name());
            assertThat(response.getState()).isEqualTo(CaseState.PROCEEDS_IN_HERITAGE_SYSTEM.name());

        }

        @Test
        void shouldMoveToFinalOrderIssued_whenIsJOOnlineAnd2v1() {
            Flags respondent1Flags = new Flags();
            respondent1Flags.setPartyName("respondent1name");
            respondent1Flags.setRoleOnCase("respondent1");
            Party respondent = new Party();
            respondent.setIndividualFirstName("Dis");
            respondent.setIndividualLastName("Guy");
            respondent.setType(INDIVIDUAL);
            respondent.setFlags(respondent1Flags);

            CaseData caseDataBefore = CaseDataBuilder.builder()
                .atStateApplicantRespondToDefenceAndProceed()
                .respondent1(respondent).build();
            caseDataBefore.setRespondent1DetailsForClaimDetailsTab(respondent);
            caseDataBefore.setCaseNameHmctsInternal("Mr. John Rambo v Dis Guy");
            caseDataBefore.setCaseNamePublic("'John Rambo' v 'Dis Guy'");

            when(interestCalculator.calculateInterest(any()))
                .thenReturn(BigDecimal.valueOf(0)
                );
            when(addressMapper.toRoboticsAddress(any())).thenReturn(new RoboticsAddress());
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
            caseData.setRespondent1ResponseDeadline(LocalDateTime.now().minusDays(15));
            caseData.setApplicant1(PartyBuilder.builder().individual().build());
            caseData.setApplicant2(PartyBuilder.builder().individual().build());
            caseData.setAddApplicant2(YesOrNo.YES);
            caseData.setPartialPaymentAmount("10");
            caseData.setPaymentTypeSelection(DJPaymentTypeSelection.IMMEDIATELY);
            caseData.setTotalClaimAmount(BigDecimal.valueOf(1010));
            caseData.setPartialPayment(YES);
            CaseLocationCivil caseLocationCivil = new CaseLocationCivil();
            caseLocationCivil.setBaseLocation("0123");
            caseLocationCivil.setRegion("0321");
            caseData.setCaseManagementLocation(caseLocationCivil);
            DynamicListElement element = new DynamicListElement(null, "John Smith");
            DynamicList list = new DynamicList();
            list.setValue(element);
            caseData.setDefendantDetailsSpec(list);
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT, caseDataBefore.toMap(mapper));

            when(featureToggleService.isJudgmentOnlineLive()).thenReturn(true);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            CaseData responseData = mapper.convertValue(response.getData(), CaseData.class);
            assertThat(responseData.getBusinessProcess()).isNotNull();
            assertThat(responseData.getBusinessProcess().getCamundaEvent()).isEqualTo(DEFAULT_JUDGEMENT_NON_DIVERGENT_SPEC.name());
            assertThat(response.getState()).isEqualTo(CaseState.All_FINAL_ORDERS_ISSUED.name());
            assertThat(responseData.getJoRepaymentSummaryObject()).doesNotContain("Claim interest amount");
            assertInterestIsPopulated(response, 0);
        }
    }

    @Nested
    class SubmittedCallback {

        @Test
        void shouldReturnExpectedSubmittedCallbackResponse_whenInvoked1v1() {
            String body = "<br /><a href=\"/cases/case-details/1594901956117591#Claim documents\" "
                + "target=\"_blank\">Download  default judgment</a> "
                + "%n%n The defendant will be served with the Default Judgment.";
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
            caseData.setRespondent1ResponseDeadline(LocalDateTime.now().minusDays(15));
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
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
            caseData.setApplicant1(PartyBuilder.builder().build());
            caseData.setRespondent1(PartyBuilder.builder().build());
            caseData.setRespondent2(PartyBuilder.builder().build());
            caseData.setAddRespondent2(YesOrNo.YES);
            caseData.setRespondent2SameLegalRepresentative(YesOrNo.YES);
            DynamicListElement element = new DynamicListElement(null, "John Smith");
            DynamicList list = new DynamicList();
            list.setValue(element);
            caseData.setDefendantDetailsSpec(list);

            CallbackParams params = callbackParamsOf(caseData, SUBMITTED);
            SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);
            String header = "# Default judgment requested";
            String body = "A default judgment has been sent to John Smith. "
                + "The claim will now progress offline (on paper)";
            assertThat(response).usingRecursiveComparison().isEqualTo(SubmittedCallbackResponse.builder()
                .confirmationHeader(header)
                .confirmationBody(String.format(body))
                .build());
        }

        @Test
        void shouldReturnJudgementRequestedResponse_whenInvokedBothDefendantWhen1v2() {
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
            caseData.setApplicant1(PartyBuilder.builder().build());
            caseData.setRespondent1(PartyBuilder.builder().build());
            caseData.setRespondent2(PartyBuilder.builder().build());
            caseData.setAddRespondent2(YesOrNo.YES);
            caseData.setRespondent2SameLegalRepresentative(YesOrNo.YES);
            DynamicListElement element = new DynamicListElement(null, "Both");
            DynamicList list = new DynamicList();
            list.setValue(element);
            caseData.setDefendantDetailsSpec(list);

            CallbackParams params = callbackParamsOf(caseData, SUBMITTED);
            SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);
            String body = "<br /><a href=\"/cases/case-details/1594901956117591#Claim documents\" "
                + "target=\"_blank\">Download  default judgment</a> "
                + "%n%n The defendant will be served the Default Judgment.";
            assertThat(response).usingRecursiveComparison().isEqualTo(SubmittedCallbackResponse.builder()
                .confirmationHeader(
                    "# Default Judgment Granted ")
                .confirmationBody(String.format(body))
                .build());
        }

        @Test
        void shouldReturnJudgementRequestedResponse_whenLrVLip() {
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
            caseData.setApplicant1(PartyBuilder.builder().build());
            caseData.setRespondent1(PartyBuilder.builder().build());
            caseData.setRespondent1Represented(NO);
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
            when(featureToggleService.isJudgmentOnlineLive()).thenReturn(true);
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
            caseData.setApplicant1(PartyBuilder.builder().build());
            caseData.setRespondent1(PartyBuilder.builder().build());
            caseData.setRespondent1Represented(NO);
            CallbackParams params = callbackParamsOf(caseData, SUBMITTED);
            SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);
            String body = "<br /><a href=\"/cases/case-details/1594901956117591#Claim documents\" "
                + "target=\"_blank\">Download  default judgment</a> "
                + "%n%n The defendant will be served with the Default Judgment.";
            assertThat(caseData.isLRvLipOneVOne()).isTrue();
            assertThat(response).usingRecursiveComparison().isEqualTo(SubmittedCallbackResponse.builder()
                .confirmationHeader(
                    JUDGMENT_GRANTED_HEADER)
                .confirmationBody(format(body))
                .build());
        }
    }

    @Test
    void shouldExtendDeadline() {
        when(deadlinesCalculator.addMonthsToDateToNextWorkingDayAtMidnight(36, LocalDate.now()))
            .thenReturn(LocalDateTime.now().plusMonths(36));

        Flags respondent1Flags = new Flags();
        respondent1Flags.setPartyName("respondent1name");
        respondent1Flags.setRoleOnCase("respondent1");
        Party respondent = new Party();
        respondent.setIndividualFirstName("Dis");
        respondent.setIndividualLastName("Guy");
        respondent.setType(INDIVIDUAL);
        respondent.setFlags(respondent1Flags);

        CaseData caseDataBefore = CaseDataBuilder.builder()
            .atStateApplicantRespondToDefenceAndProceed()
            .respondent1(respondent).build();
        caseDataBefore.setRespondent1DetailsForClaimDetailsTab(respondent);
        caseDataBefore.setCaseNameHmctsInternal("Mr. John Rambo v Dis Guy");
        caseDataBefore.setCaseNamePublic("'John Rambo' v 'Dis Guy'");

        when(interestCalculator.calculateInterest(any()))
            .thenReturn(BigDecimal.valueOf(0)
            );
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
        caseData.setRespondent1ResponseDeadline(LocalDateTime.now().minusDays(15));
        DynamicListElement element = new DynamicListElement(null, "John Smith");
        DynamicList list = new DynamicList();
        list.setValue(element);
        caseData.setDefendantDetailsSpec(list);
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT, caseDataBefore.toMap(mapper));

        when(featureToggleService.isJudgmentOnlineLive()).thenReturn(false);

        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        Object deadlineValue = response.getData().get("claimDismissedDeadline");
        assertThat(deadlineValue).isNotNull();

        LocalDate expectedDate = LocalDate.now().plusMonths(36);
        LocalDate actualDate = LocalDateTime.parse(deadlineValue.toString()).toLocalDate();

        assertThat(actualDate).isEqualTo(expectedDate);
    }
}
