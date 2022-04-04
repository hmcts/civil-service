package uk.gov.hmcts.reform.civil.handler.callback.user;

import org.junit.jupiter.api.BeforeEach;
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
import uk.gov.hmcts.reform.civil.callback.CallbackType;
import uk.gov.hmcts.reform.civil.config.ExitSurveyConfiguration;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.launchdarkly.FeatureToggleService;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.AddressBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.PartyBuilder;
import uk.gov.hmcts.reform.civil.service.CoreCaseUserService;
import uk.gov.hmcts.reform.civil.service.DeadlinesCalculator;
import uk.gov.hmcts.reform.civil.service.ExitSurveyContentService;
import uk.gov.hmcts.reform.civil.service.Time;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.civil.service.flowstate.StateFlowEngine;
import uk.gov.hmcts.reform.civil.validation.DateOfBirthValidator;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static java.lang.String.format;
import static java.time.format.DateTimeFormatter.ISO_DATE_TIME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.MID;
import static uk.gov.hmcts.reform.civil.callback.CallbackVersion.V_1;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.ACKNOWLEDGE_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.CaseRole.RESPONDENTSOLICITORONE;
import static uk.gov.hmcts.reform.civil.enums.CaseRole.RESPONDENTSOLICITORTWO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE_TIME_AT;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.formatLocalDateTime;
import static uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder.RESPONSE_DEADLINE;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {
    AcknowledgeClaimCallbackHandler.class,
    ExitSurveyConfiguration.class,
    ExitSurveyContentService.class,
    JacksonAutoConfiguration.class,
    ValidationAutoConfiguration.class,
    DateOfBirthValidator.class,
    CaseDetailsConverter.class,
    StateFlowEngine.class,
    FeatureToggleService.class
})
class AcknowledgeClaimCallbackHandlerTest extends BaseCallbackHandlerTest {

    @MockBean
    private DeadlinesCalculator deadlinesCalculator;

    @MockBean
    private Time time;

    @Autowired
    private AcknowledgeClaimCallbackHandler handler;

    @Autowired
    private ExitSurveyContentService exitSurveyContentService;

    @Autowired
    private UserService userService;

    @MockBean
    private FeatureToggleService featureToggleService;

    @MockBean
    private CoreCaseUserService coreCaseUserService;

    @Nested
    class AboutToStartCallback {

        @Test
        void shouldPopulateRespondentCopies_WhenAboutToStartIsInvoked() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDetailsNotified()
                .respondent2(PartyBuilder.builder().individual().build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            assertThat(response.getErrors()).isNull();
            assertThat(response.getData().get("respondent1Copy")).isEqualTo(response.getData().get("respondent1"));
            assertThat(response.getData().get("respondent2Copy")).isEqualTo(response.getData().get("respondent2"));
        }

        @Test
        void shouldReturnError_whenRespondentRespondsAgain1v2SameLegalRep() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateNotificationAcknowledged()
                .respondent2(PartyBuilder.builder().individual().build())
                .addRespondent2(YES)
                .respondent2SameLegalRepresentative(YES)
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            assertThat(response.getErrors()).containsExactly("Defendant acknowledgement has already been recorded");
        }

        @Test
        void shouldReturnError_whenRespondentRespondsAgain1v2DifferentLegalRep() {
            when(userService.getUserInfo(anyString())).thenReturn(UserInfo.builder().uid("uid").build());
            when(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORONE))).thenReturn(true);
            CaseData caseData = CaseDataBuilder.builder()
                .atStateNotificationAcknowledged()
                .respondent2(PartyBuilder.builder().individual().build())
                .addRespondent2(YES)
                .respondent2SameLegalRepresentative(NO)
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            assertThat(response.getErrors()).containsExactly("Defendant acknowledgement has already been recorded");
        }

        @Test
        void shouldNotError_WhenNoRespondent2() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            assertThat(response.getErrors()).isNull();
            assertThat(response.getData().get("respondent1Copy")).isEqualTo(response.getData().get("respondent1"));
            assertThat(response.getData().get("respondent2Copy")).isNull();
        }
    }

    @Nested
    class MidEventConfirmDetailsCallback {

        private static final String PAGE_ID = "confirm-details";

        @Test
        void shouldReturnError_whenIndividualDateOfBirthIsInTheFuture() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified()
                .respondent1(PartyBuilder.builder().individual()
                                 .individualDateOfBirth(LocalDate.now().plusDays(1))
                                 .build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).containsExactly("The date entered cannot be in the future");
        }

        @Test
        void shouldReturnError_whenSoleTraderDateOfBirthIsInTheFuture() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified()
                .respondent1(PartyBuilder.builder().soleTrader()
                                 .soleTraderDateOfBirth(LocalDate.now().plusDays(1))
                                 .build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).containsExactly("The date entered cannot be in the future");
        }

        @Test
        void shouldReturnNoError_whenIndividualDateOfBirthIsInThePast() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified()
                .respondent1(PartyBuilder.builder().individual()
                                 .individualDateOfBirth(LocalDate.now().minusYears(1))
                                 .build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isEmpty();
        }

        @Test
        void shouldReturnNoError_whenSoleTraderDateOfBirthIsInThePast() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified()
                .respondent1(PartyBuilder.builder().soleTrader()
                                 .soleTraderDateOfBirth(LocalDate.now().minusYears(1))
                                 .build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isEmpty();
        }
    }

    @Nested
    class AboutToSubmitCallbackBackwardsCompatibility {
        private LocalDateTime newDeadline;
        private LocalDateTime acknowledgementDate;

        @BeforeEach
        void setup() {
            newDeadline = LocalDateTime.now().plusDays(14);
            when(deadlinesCalculator.plus14DaysAt4pmDeadline(any())).thenReturn(newDeadline);
            acknowledgementDate = LocalDateTime.now();
            when(time.now()).thenReturn(acknowledgementDate);
            when(userService.getUserInfo(anyString())).thenReturn(UserInfo.builder().uid("uid").build());
        }

        @Test
        void shouldCopyRespondentPrimaryAddresses_whenInvoked() {
            CaseData caseData = CaseDataBuilder.builder().atStateRespondentFullDefenceAfterNotificationAcknowledgement()
                .respondent2(PartyBuilder.builder().individual().build()).addRespondent2(YES).build();
            String address = "test address";
            var expectedAddress = AddressBuilder.defaults().addressLine1(address).build();
            caseData = caseData.toBuilder().respondent1Copy(caseData.getRespondent1().toBuilder().primaryAddress(
                expectedAddress).build()).respondent2Copy(caseData.getRespondent2().toBuilder().primaryAddress(
                expectedAddress).build()).build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response.getData()).doesNotContainKey("respondent1Copy");
            assertThat(response.getData()).extracting("respondent1").extracting("primaryAddress").extracting(
                "AddressLine1").isEqualTo(address);
            assertThat(response.getData()).extracting("respondent2").extracting("primaryAddress").extracting(
                "AddressLine1").isEqualTo(address);
        }

        @Test
        void shouldSetNewResponseDeadlineAndUpdateBusinessProcess_whenInvokedFor1v1() {
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .respondent1Copy(PartyBuilder.builder().individual().build()).addApplicant2(NO).addRespondent2(NO)
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response.getData()).extracting("businessProcess").extracting("camundaEvent").isEqualTo(
                ACKNOWLEDGE_CLAIM.name());
            assertThat(response.getData()).extracting("businessProcess").extracting("status").isEqualTo("READY");
            assertThat(response.getData()).containsEntry("respondent1ResponseDeadline",
                                                         newDeadline.format(ISO_DATE_TIME)
            ).containsEntry("respondent1AcknowledgeNotificationDate", acknowledgementDate.format(ISO_DATE_TIME));
        }

        @Test
        void shouldSetNewResponseDeadlineAndUpdateBusinessProcess_whenInvokedFor2v1() {
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .respondent1Copy(PartyBuilder.builder().individual().build())
                .addApplicant2(YES).applicant2(PartyBuilder.builder().individual().build()).build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response.getData()).extracting("businessProcess").extracting("camundaEvent").isEqualTo(
                ACKNOWLEDGE_CLAIM.name());
            assertThat(response.getData()).extracting("businessProcess").extracting("status").isEqualTo("READY");
            assertThat(response.getData()).containsEntry("respondent1ResponseDeadline",
                                                         newDeadline.format(ISO_DATE_TIME)
            ).containsEntry("respondent1AcknowledgeNotificationDate", acknowledgementDate.format(ISO_DATE_TIME));
        }

        @Test
        void shouldSetNewResponseDeadlineAndUpdateBusinessProcess_whenInvokedFor1V2DiffSolicitor1() {
            when(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORONE))).thenReturn(true);
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().respondent1Copy(
                PartyBuilder.builder().individual().build()).respondent2Copy(
                PartyBuilder.builder().individual().build())
                .multiPartyClaimTwoDefendantSolicitors().build().toBuilder().build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response.getData()).extracting("businessProcess").extracting("camundaEvent").isEqualTo(
                ACKNOWLEDGE_CLAIM.name());
            assertThat(response.getData()).extracting("businessProcess").extracting("status").isEqualTo("READY");
            assertThat(response.getData()).containsEntry("respondent1ResponseDeadline",
                                                         newDeadline.format(ISO_DATE_TIME)
            ).containsEntry("respondent1AcknowledgeNotificationDate", acknowledgementDate.format(ISO_DATE_TIME));
        }

        @Test
        void shouldSetNewResponseDeadlineAndUpdateBusinessProcess_whenInvokedFor1V2SameSolicitor() {
            when(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORONE))).thenReturn(true);
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged1v2SameSolicitor()
                .addRespondent2(
                YES).respondent2(PartyBuilder.builder().individual().build())
                .respondent1Copy(PartyBuilder.builder().individual().build()).respondent2Copy(
                PartyBuilder.builder().individual().build()).respondent2SameLegalRepresentative(YES).build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response.getData()).extracting("businessProcess").extracting("camundaEvent").isEqualTo(
                ACKNOWLEDGE_CLAIM.name());
            assertThat(response.getData()).extracting("businessProcess").extracting("status").isEqualTo("READY");
            assertThat(response.getData()).containsEntry("respondent1ResponseDeadline",
                                                         newDeadline.format(ISO_DATE_TIME)
            ).containsEntry("respondent1AcknowledgeNotificationDate", acknowledgementDate.format(ISO_DATE_TIME))
                .containsEntry("respondent2AcknowledgeNotificationDate", acknowledgementDate.format(ISO_DATE_TIME));
        }

        @Test
        void shouldSetNewResponseDeadlineAndUpdateBusinessProcess_whenInvokedFor1V2DiffSolicitor2() {
            when(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORTWO))).thenReturn(true);
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledgedRespondent2().addRespondent2(
                YES).respondent2SameLegalRepresentative(NO)
                .respondent2(PartyBuilder.builder().individual().build()).respondent1Copy(
                PartyBuilder.builder().individual().build())
                .respondent2Copy(PartyBuilder.builder().individual().build()).build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response.getData()).extracting("businessProcess").extracting("camundaEvent").isEqualTo(
                ACKNOWLEDGE_CLAIM.name());
            assertThat(response.getData()).extracting("businessProcess").extracting("status").isEqualTo("READY");
            assertThat(response.getData()).containsEntry("respondent2ResponseDeadline",
                                                         newDeadline.format(ISO_DATE_TIME)
            ).containsEntry("respondent2AcknowledgeNotificationDate", acknowledgementDate.format(ISO_DATE_TIME));
        }
    }

    @Nested
    class AboutToSubmitCallbackV1 {
        private LocalDateTime newDeadline;
        private LocalDateTime acknowledgementDate;

        @BeforeEach
        void setup() {
            newDeadline = LocalDateTime.now().plusDays(14);
            when(deadlinesCalculator.plus14DaysAt4pmDeadline(any())).thenReturn(newDeadline);
            acknowledgementDate = LocalDateTime.now();
            when(time.now()).thenReturn(acknowledgementDate);
            when(userService.getUserInfo(anyString())).thenReturn(UserInfo.builder().uid("uid").build());

        }

        @Test
        void shouldCopyRespondentPrimaryAddresses_whenInvoked() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateRespondentFullDefenceAfterNotificationAcknowledgement()
                .respondent2(PartyBuilder.builder().individual().build())
                .addRespondent2(YES)
                .build();
            String address = "test address";
            var expectedAddress = AddressBuilder.defaults().addressLine1(address).build();
            caseData = caseData.toBuilder()
                .respondent1Copy(caseData.getRespondent1().toBuilder().primaryAddress(expectedAddress).build())
                .respondent2Copy(caseData.getRespondent2().toBuilder().primaryAddress(expectedAddress).build())
                .build();
            CallbackParams params = callbackParamsOf(V_1, caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData()).doesNotContainKey("respondent1Copy");
            assertThat(response.getData())
                .extracting("respondent1").extracting("primaryAddress").extracting("AddressLine1").isEqualTo(address);
            assertThat(response.getData())
                .extracting("respondent2").extracting("primaryAddress").extracting("AddressLine1").isEqualTo(address);
        }

        @Test
        void shouldSetNewResponseDeadlineAndUpdateBusinessProcess_whenInvokedFor1v1() {

            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .respondent1Copy(PartyBuilder.builder().individual().build())
                .addApplicant2(NO)
                .addRespondent2(NO)
                .build();
            CallbackParams params = callbackParamsOf(V_1, caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData())
                .extracting("businessProcess")
                .extracting("camundaEvent")
                .isEqualTo(ACKNOWLEDGE_CLAIM.name());

            assertThat(response.getData())
                .extracting("businessProcess")
                .extracting("status")
                .isEqualTo("READY");

            assertThat(response.getData())
                .containsEntry("respondent1ResponseDeadline", newDeadline.format(ISO_DATE_TIME))
                .containsEntry("respondent1AcknowledgeNotificationDate", acknowledgementDate.format(ISO_DATE_TIME));
        }

        @Test
        void shouldSetNewResponseDeadlineAndUpdateBusinessProcess_whenInvokedFor2v1() {
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .respondent1Copy(PartyBuilder.builder().individual().build())
                .addApplicant2(YES)
                .applicant2(PartyBuilder.builder().individual().build())
                .build();
            CallbackParams params = callbackParamsOf(V_1, caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData())
                .extracting("businessProcess")
                .extracting("camundaEvent")
                .isEqualTo(ACKNOWLEDGE_CLAIM.name());

            assertThat(response.getData())
                .extracting("businessProcess")
                .extracting("status")
                .isEqualTo("READY");

            assertThat(response.getData())
                .containsEntry("respondent1ResponseDeadline", newDeadline.format(ISO_DATE_TIME))
                .containsEntry("respondent1AcknowledgeNotificationDate", acknowledgementDate.format(ISO_DATE_TIME));
        }

        @Test
        void shouldSetNewResponseDeadlineAndUpdateBusinessProcess_whenInvokedFor1V2DiffSolicitor1() {
            when(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORONE))).thenReturn(true);
            CaseData caseData = CaseDataBuilder.builder()
                .atStateNotificationAcknowledged()
                .respondent1Copy(PartyBuilder.builder().individual().build())
                .respondent2Copy(PartyBuilder.builder().individual().build())
                .multiPartyClaimTwoDefendantSolicitors().build().toBuilder()
                .build();

            CallbackParams params = callbackParamsOf(V_1, caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData())
                .extracting("businessProcess")
                .extracting("camundaEvent")
                .isEqualTo(ACKNOWLEDGE_CLAIM.name());

            assertThat(response.getData())
                .extracting("businessProcess")
                .extracting("status")
                .isEqualTo("READY");

            assertThat(response.getData())
                .containsEntry("respondent1ResponseDeadline", newDeadline.format(ISO_DATE_TIME))
                .containsEntry("respondent1AcknowledgeNotificationDate", acknowledgementDate.format(ISO_DATE_TIME));
        }

        @Test
        void shouldSetNewResponseDeadlineAndUpdateBusinessProcess_whenInvokedFor1V2SameSolicitor() {
            when(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORONE))).thenReturn(true);
            CaseData caseData = CaseDataBuilder.builder()
                .atStateNotificationAcknowledged1v2SameSolicitor()
                .addRespondent2(YES)
                .respondent2(PartyBuilder.builder().individual().build())
                .respondent1Copy(PartyBuilder.builder().individual().build())
                .respondent2Copy(PartyBuilder.builder().individual().build())
                .respondent2SameLegalRepresentative(YES)
                .build();

            CallbackParams params = callbackParamsOf(V_1, caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData())
                .extracting("businessProcess")
                .extracting("camundaEvent")
                .isEqualTo(ACKNOWLEDGE_CLAIM.name());

            assertThat(response.getData())
                .extracting("businessProcess")
                .extracting("status")
                .isEqualTo("READY");

            assertThat(response.getData())
                .containsEntry("respondent1ResponseDeadline", newDeadline.format(ISO_DATE_TIME))
                .containsEntry("respondent1AcknowledgeNotificationDate", acknowledgementDate.format(ISO_DATE_TIME))
                .containsEntry("respondent2AcknowledgeNotificationDate", acknowledgementDate.format(ISO_DATE_TIME));
        }

        @Test
        void shouldSetNewResponseDeadlineAndUpdateBusinessProcess_whenInvokedFor1V2DiffSolicitor2() {
            when(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORTWO))).thenReturn(true);
            CaseData caseData = CaseDataBuilder.builder()
                .atStateNotificationAcknowledgedRespondent2()
                .addRespondent2(YES)
                .respondent2SameLegalRepresentative(NO)
                .respondent2(PartyBuilder.builder().individual().build())
                .respondent1Copy(PartyBuilder.builder().individual().build())
                .respondent2Copy(PartyBuilder.builder().individual().build())
                .build();

            CallbackParams params = callbackParamsOf(V_1, caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData())
                .extracting("businessProcess")
                .extracting("camundaEvent")
                .isEqualTo(ACKNOWLEDGE_CLAIM.name());

            assertThat(response.getData())
                .extracting("businessProcess")
                .extracting("status")
                .isEqualTo("READY");

            assertThat(response.getData())
                .containsEntry("respondent2ResponseDeadline", newDeadline.format(ISO_DATE_TIME))
                .containsEntry("respondent2AcknowledgeNotificationDate", acknowledgementDate.format(ISO_DATE_TIME));
        }
    }

    @Nested
    class SubmittedCallback {

        @Test
        void shouldReturnExpectedResponse_whenInvoked() {
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
            CallbackParams params = callbackParamsOf(caseData, CallbackType.SUBMITTED);

            SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);

            assertThat(response).usingRecursiveComparison().isEqualTo(
                SubmittedCallbackResponse.builder()
                    .confirmationHeader(format("# You have acknowledged the claim%n## Claim number: 000DC001"))
                    .confirmationBody(format(
                        "<br />You need to respond to the claim before %s."
                            + "%n%n[Download the Acknowledgement of Claim form]"
                            + "(/cases/case-details/%s#CaseDocuments)",

                formatLocalDateTime(RESPONSE_DEADLINE, DATE_TIME_AT), caseData.getCcdCaseReference())
                                          + exitSurveyContentService.respondentSurvey())
                    .build());
        }
    }
}
