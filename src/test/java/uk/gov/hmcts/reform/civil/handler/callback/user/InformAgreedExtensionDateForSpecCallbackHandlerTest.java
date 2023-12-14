package uk.gov.hmcts.reform.civil.handler.callback.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilderSpec;
import uk.gov.hmcts.reform.civil.service.CoreCaseUserService;
import uk.gov.hmcts.reform.civil.service.DeadlinesCalculator;
import uk.gov.hmcts.reform.civil.service.ExitSurveyContentService;
import uk.gov.hmcts.reform.civil.service.Time;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.civil.bankholidays.WorkingDayIndicator;
import uk.gov.hmcts.reform.civil.service.flowstate.StateFlowEngine;
import uk.gov.hmcts.reform.civil.stateflow.StateFlow;
import uk.gov.hmcts.reform.civil.validation.DeadlineExtensionValidator;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;
import java.time.LocalDate;
import java.time.LocalDateTime;
import static java.time.LocalDate.now;
import static java.time.format.DateTimeFormatter.ISO_DATE_TIME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.MID;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.EXTEND_RESPONSE_DEADLINE;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.enums.CaseRole.RESPONDENTSOLICITORTWO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.handler.callback.user.InformAgreedExtensionDateForSpecCallbackHandler.SPEC_ACKNOWLEDGEMENT_OF_SERVICE;
import static uk.gov.hmcts.reform.civil.service.DeadlinesCalculator.END_OF_BUSINESS_DAY;

@SpringBootTest(classes = {
    InformAgreedExtensionDateForSpecCallbackHandler.class,
    JacksonAutoConfiguration.class,
    StateFlowEngine.class,
    CaseDetailsConverter.class,
    DeadlineExtensionValidator.class,
    DeadlinesCalculator.class
})
class InformAgreedExtensionDateForSpecCallbackHandlerTest extends BaseCallbackHandlerTest {

    @Autowired
    private InformAgreedExtensionDateForSpecCallbackHandler handler;

    @MockBean
    private ExitSurveyContentService exitSurveyContentService;

    @MockBean
    private DeadlinesCalculator deadlinesCalculator;

    @MockBean
    private Time time;

    @MockBean
    private CoreCaseUserService coreCaseUserService;

    @Autowired
    private UserService userService;

    @MockBean
    private StateFlowEngine stateFlowEngine;

    @MockBean
    private FeatureToggleService toggleService;

    @Mock
    private StateFlow mockedStateFlow;

    @MockBean
    private WorkingDayIndicator workingDayIndicator;

    @Test
    void shouldContainExtendResponseDeadlineEvent_whenPinAndPostEnabled() {
        given(toggleService.isPinInPostEnabled()).willReturn(true);
        assertTrue(handler.handledEvents().contains(EXTEND_RESPONSE_DEADLINE));
    }

    @Test
    void shouldNotContainExendResponseDeadlineEvent_whenPinAndPostDisabled() {
        given(toggleService.isPinInPostEnabled()).willReturn(false);
        assertFalse(handler.handledEvents().contains(EXTEND_RESPONSE_DEADLINE));
    }

    @Nested
    class AboutToStartCallback {

        @BeforeEach
        void setup() {
            when(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORTWO))).thenReturn(false);
            when(userService.getUserInfo(anyString())).thenReturn(UserInfo.builder().uid("uid").build());
            when(mockedStateFlow.isFlagSet(any())).thenReturn(false);
            when(stateFlowEngine.evaluate(any(CaseData.class))).thenReturn(mockedStateFlow);
        }

        @Test
        void shouldSetRespondent1FlagToYes_whenOneRespondentRepresentative() {
            // Given
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build().toBuilder()
                .addRespondent2(NO)
                .build();

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);

            // When
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            // Then
            assertThat(response.getErrors()).isNull();
            assertThat(response.getData()).extracting("isRespondent1").isEqualTo("Yes");
        }

        @Test
        void shouldSetRespondent1FlagToYes_whenRespondentTwoRepresentative() {
            // Given
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build().toBuilder()
                .addRespondent2(YES)
                .build();

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);

            // When
            when(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORTWO))).thenReturn(true);
            when(mockedStateFlow.isFlagSet(any())).thenReturn(true);
            when(stateFlowEngine.evaluate(any(CaseData.class))).thenReturn(mockedStateFlow);
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            // Then
            assertThat(response.getErrors()).isNull();
            assertThat(response.getData()).extracting("isRespondent1").isEqualTo("No");
        }

        @Test
        void shouldReturnError_whenIssueDateIsBeforeOf28Days() {
            // Given
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build().toBuilder()
                .addRespondent2(NO)
                .issueDate(LocalDate.now().minusDays(35))
                .build();

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);

            // When
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            // Then
            assertThat(response.getErrors().get(0))
                .isEqualTo("You can no longer request an 'Inform Agreed Extension Date' as the deadline has passed");
        }

        @Test
        void shouldReturnError_whenIsMultiPartyScenarioTwoVsOne() {
            // Given
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build().toBuilder()
                .addRespondent2(NO)
                .addApplicant2(YES)
                .respondent1TimeExtensionDate(LocalDateTime.now())
                .build();

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);

            // When
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            // Then
            assertThat(response.getErrors().get(0))
                .isEqualTo("This action cannot currently be performed because it has already been completed");
        }

        @Test
        void shouldReturnError_whenIsMultiPartyScenarioOneVsOne() {
            // Given
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build().toBuilder()
                .addRespondent2(NO)
                .respondent1TimeExtensionDate(LocalDateTime.now())
                .build();

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);

            // When
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            // Then
            assertThat(response.getErrors().get(0))
                .isEqualTo("This action cannot currently be performed because it has already been completed");
        }

        @Test
        void shouldReturnError_whenIsMultiPartyScenarioOneVsTwoOneLegalReps() {
            // Given
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build().toBuilder()
                .addRespondent2(NO)
                .respondent2(Party.builder().build())
                .respondent1TimeExtensionDate(LocalDateTime.now())
                .build();

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);

            // When
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            // Then
            assertThat(response.getErrors().get(0))
                .isEqualTo("This action cannot currently be performed because it has already been completed");
        }

        @Test
        void shouldReturnError_whenIsMultiPartyScenarioOneVsTwoTwoLegalReps() {
            // Given
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build().toBuilder()
                .addRespondent2(NO)
                .respondent2(Party.builder().build())
                .respondent2SameLegalRepresentative(NO)
                .respondent1TimeExtensionDate(LocalDateTime.now())
                .build();

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);

            // When
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            // Then
            assertThat(response.getErrors().get(0))
                .isEqualTo("This action cannot currently be performed because it has already been completed");
        }
    }

    @Nested
    class MidEventValidateExtensionDateCallback {
        private static final String PAGE_ID = "extension-date";

        @BeforeEach
        void setup() {
            when(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORTWO))).thenReturn(true);
            when(userService.getUserInfo(anyString())).thenReturn(UserInfo.builder().uid("uid").build());
            when(mockedStateFlow.isFlagSet(any())).thenReturn(true);
            when(stateFlowEngine.evaluate(any(CaseData.class))).thenReturn(mockedStateFlow);
        }

        @Test
        void shouldValidateExtensionDate_whenAllDataIsProvided() {
            // Given
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build().toBuilder()
                .addRespondent2(NO)
                .respondentSolicitor1AgreedDeadlineExtension(now())
                .respondent2(Party.builder().build())
                .respondent2SameLegalRepresentative(NO)
                .respondent2ResponseDeadline(LocalDateTime.now())
                .respondentSolicitor2AgreedDeadlineExtension(LocalDate.now())
                .businessProcess(BusinessProcess.builder().camundaEvent(SPEC_ACKNOWLEDGEMENT_OF_SERVICE).build())
                .build();

            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            // When
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            // Then
            assertThat(response.getErrors().get(0))
                .isEqualTo("The agreed extension date must be a date in the future");
        }

        @Test
        void shouldReturnError_whenAllDataIsProvided() {
            // Given
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build().toBuilder()
                .addRespondent2(NO)
                .respondentSolicitor1AgreedDeadlineExtension(now())
                .respondent2(Party.builder().build())
                .respondent2SameLegalRepresentative(NO)
                .respondent2ResponseDeadline(LocalDateTime.now().plusDays(99))
                .respondentSolicitor2AgreedDeadlineExtension(LocalDate.now())
                .businessProcess(BusinessProcess.builder().camundaEvent(SPEC_ACKNOWLEDGEMENT_OF_SERVICE).build())
                .build();

            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            // When
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            // Then
            assertThat(response.getErrors().get(0))
                .isEqualTo("Date must be from claim issue date plus a maximum of between 29 and 56 days.");
        }

    }

    @Nested
    class MidEventResponseDeadlineCallback {
        private static final String PAGE_ID = "response-deadline-extension";

        @Test
        void shouldResponseDeadline_whenAllDataIsProvided() {
            // Given
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build().toBuilder()
                .addRespondent2(NO)
                .respondentSolicitor1AgreedDeadlineExtension(now())
                .respondent2(Party.builder().build())
                .respondent2SameLegalRepresentative(NO)
                .respondent2ResponseDeadline(LocalDateTime.now())
                .respondentSolicitor2AgreedDeadlineExtension(LocalDate.now())
                .businessProcess(BusinessProcess.builder().camundaEvent(SPEC_ACKNOWLEDGEMENT_OF_SERVICE).build())
                .build();

            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            // When
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            // Then
            assertThat(response.getErrors().get(0))
                .isEqualTo("The agreed extension date must be a date in the future");
        }
    }

    @Nested
    class AboutToSubmitCallback {
        LocalDateTime timeExtensionDate;
        LocalDate extensionDateRespondent1;
        LocalDate extensionDateRespondent2;

        @BeforeEach
        void setup() {
            timeExtensionDate = LocalDateTime.of(2020, 1, 1, 12, 0, 0);
            when(time.now()).thenReturn(timeExtensionDate);
            extensionDateRespondent1 = now().plusDays(14);
            extensionDateRespondent2 = now().plusDays(16);
            when(deadlinesCalculator.calculateFirstWorkingDay(any())).thenReturn(
                extensionDateRespondent1);
            when(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORTWO))).thenReturn(true);
            when(userService.getUserInfo(anyString())).thenReturn(UserInfo.builder().uid("uid").build());
        }

        @Test
        void shouldUpdateBothRespondentResponseDeadlinesToExtensionDate_whenSolicitorRepresentingBothRespondents() {
            when(mockedStateFlow.isFlagSet(any())).thenReturn(false);
            when(stateFlowEngine.evaluate(any(CaseData.class))).thenReturn(mockedStateFlow);
            LocalDateTime nextDeadline = extensionDateRespondent1.atStartOfDay();

            CaseData caseData = CaseDataBuilderSpec.builder().atStateClaim1v2SameSolicitorTimeExtension()
                .addRespondent2(YES)
                .respondent2SameLegalRepresentative(YES)
                .respondentSolicitor1AgreedDeadlineExtension(extensionDateRespondent1)
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            LocalDateTime newDeadline = extensionDateRespondent1.atTime(END_OF_BUSINESS_DAY);

            assertThat(response.getData())
                .containsEntry("respondent1ResponseDeadline", newDeadline.format(ISO_DATE_TIME))
                .containsEntry("respondent1TimeExtensionDate", timeExtensionDate.format(ISO_DATE_TIME))
                .containsEntry("respondent2ResponseDeadline", newDeadline.format(ISO_DATE_TIME))
                .containsEntry("respondent2TimeExtensionDate", timeExtensionDate.format(ISO_DATE_TIME));

            assertThat(response.getData())
                .extracting("businessProcess")
                .extracting("status")
                .isEqualTo("READY");
        }

        @Test
        void shouldUpdateRespondent2ResponseDeadlinesToExtensionDate_whenSolicitorRepresentingBothRespondents() {
            when(stateFlowEngine.evaluate(any(CaseData.class))).thenReturn(mockedStateFlow);
            when(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORTWO))).thenReturn(true);
            when(userService.getUserInfo(anyString())).thenReturn(UserInfo.builder().uid("uid").build());
            when(mockedStateFlow.isFlagSet(any())).thenReturn(true);
            LocalDateTime nextDeadline = extensionDateRespondent1.atStartOfDay();

            CaseData caseData = CaseDataBuilderSpec.builder().atStateClaim1v2SameSolicitorTimeExtension()
                .addRespondent2(YES)
                .respondent2SameLegalRepresentative(NO)
                .respondentSolicitor1AgreedDeadlineExtension(extensionDateRespondent1)
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            LocalDateTime newDeadline = extensionDateRespondent1.atTime(END_OF_BUSINESS_DAY);

            assertThat(response.getData())
                .containsEntry("respondent2ResponseDeadline", newDeadline.format(ISO_DATE_TIME))
                .containsEntry("respondent2TimeExtensionDate", timeExtensionDate.format(ISO_DATE_TIME));
        }

        @Test
        void shouldUpdateBothRespondentResponseHaveOwnRep_whenSolicitorRepresentingBothRespondents() {
            when(mockedStateFlow.isFlagSet(any())).thenReturn(false);
            when(stateFlowEngine.evaluate(any(CaseData.class))).thenReturn(mockedStateFlow);
            LocalDateTime nextDeadline = extensionDateRespondent1.atStartOfDay();

            CaseData caseData = CaseDataBuilderSpec.builder().atStateClaim1v2SameSolicitorTimeExtension()
                .addRespondent2(YES)
                .respondent2SameLegalRepresentative(NO)
                .respondentSolicitor1AgreedDeadlineExtension(extensionDateRespondent1)
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            LocalDateTime newDeadline = extensionDateRespondent1.atTime(END_OF_BUSINESS_DAY);

            assertThat(response.getData())
                .containsEntry("respondent1ResponseDeadline", newDeadline.format(ISO_DATE_TIME));

            assertThat(response.getData())
                .extracting("businessProcess")
                .extracting("status")
                .isEqualTo("READY");
        }
    }

    @Nested
    class SubmittedCallback {

        @Test
        void shouldReturnHeaderAndBody_whenAboutToSubmitIsCalled() {
            when(exitSurveyContentService.respondentSurvey()).thenReturn("---");
            String header = "# Extension deadline submitted";
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .respondent1ResponseDeadline(LocalDateTime.now())
                .build();

            CallbackParams params = callbackParamsOf(caseData, SUBMITTED);
            SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);
            assertThat(response.getConfirmationHeader()).isEqualTo(header);
        }

    }
}
