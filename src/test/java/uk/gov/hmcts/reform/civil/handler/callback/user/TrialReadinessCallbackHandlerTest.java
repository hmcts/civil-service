package uk.gov.hmcts.reform.civil.handler.callback.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.enums.CaseRole;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.CoreCaseUserService;
import uk.gov.hmcts.reform.civil.utils.UserRoleCaching;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {
    TrialReadinessCallbackHandler.class,
    JacksonAutoConfiguration.class
})

public class TrialReadinessCallbackHandlerTest extends BaseCallbackHandlerTest {

    @Autowired
    private TrialReadinessCallbackHandler handler;

    @MockBean
    private CoreCaseUserService coreCaseUserService;

    @MockBean
    private UserRoleCaching userRoleCaching;

    public static final String READY_HEADER = "## You have said this case is ready for trial or hearing";
    public static final String READY_BODY = "### What happens next \n\n"
        + "You can view your and other party's trial arrangements in documents in the case details.\n\n "
        + "If there are any additional changes between now and the hearing date, "
        + "you will need to make an application as soon as possible and pay the appropriate fee.";
    public static final String NOT_READY_HEADER = "## You have said this case is not ready for trial or hearing";
    public static final String NOT_READY_BODY = "### What happens next \n\n"
        + "You can view your and other party's trial arrangements in documents in the case details. "
        + "If there are any additional changes between now and the hearing date, "
        + "you will need to make an application as soon as possible and pay the appropriate fee.\n\n"
        + "The trial will go ahead on the specified date "
        + "unless a judge makes an order changing the date of the hearing. "
        + "If you want the date of the hearing to be changed (or any other order to make the case ready for trial)"
        + "you will need to make an application to the court and pay the appropriate fee.";

    @BeforeEach
    public void setup() {
        when(userRoleCaching.getCacheKeyToken(anyString())).thenReturn("1234");
        when(userService.getUserInfo(anyString())).thenReturn(UserInfo.builder().uid("uid").build());
    }

    @Nested
    class AboutToStartCallback {

        @Test
        void shouldNotReturnError_WhenAboutToStartIsInvoked_ApplicantSolicitor() {
            //given: applicant solicitor logs in more than 3 weeks before hearing
            CaseData caseData = CaseDataBuilder.builder().atStateTrialReadyCheck().build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_START, caseData).build();

            when(userRoleCaching.getUserRoles(anyString(), anyString(), anyString()))
                .thenReturn(List.of(CaseRole.APPLICANTSOLICITORONE.getFormattedName()));

            //when: Event is started
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);
            //then: no error is given
            assertThat(response.getErrors()).isEmpty();
        }

        @Test
        void shouldNotReturnError_WhenAboutToStartIsInvoked_Claimant() {
            //given: claimant logs in more than 3 weeks before hearing
            CaseData caseData = CaseDataBuilder.builder().atStateTrialReadyCheck().build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_START, caseData).build();
            when(userRoleCaching.getUserRoles(anyString(), anyString(), anyString()))
                .thenReturn(List.of(CaseRole.CLAIMANT.getFormattedName()));

            //when: Event is started
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);
            //then: no error is given
            assertThat(response.getErrors()).isEmpty();
        }

        @Test
        void shouldNotReturnError_WhenAboutToStartIsInvoked_RespondentSolicitor1() {
            //given: respondent 1 solicitor logs in more than 3 weeks before hearing
            CaseData caseData = CaseDataBuilder.builder().atStateTrialReadyCheck().build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_START, caseData).build();

            when(userRoleCaching.getUserRoles(anyString(), anyString(), anyString()))
                .thenReturn(List.of(CaseRole.RESPONDENTSOLICITORONE.getFormattedName()));

            //when: Event is started
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);
            //then: no error is given
            assertThat(response.getErrors()).isEmpty();
        }

        @Test
        void shouldNotReturnError_WhenAboutToStartIsInvoked_Defendant() {
            //given: defendant logs in more than 3 weeks before hearing
            CaseData caseData = CaseDataBuilder.builder().atStateTrialReadyCheck().build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_START, caseData).build();
            when(userRoleCaching.getUserRoles(anyString(), anyString(), anyString()))
                .thenReturn(List.of(CaseRole.DEFENDANT.getFormattedName()));

            //when: Event is started
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);
            //then: no error is given
            assertThat(response.getErrors()).isEmpty();
        }

        @Test
        void shouldNotReturnError_WhenAboutToStartIsInvoked_RespondentSolicitor2() {
            //given: respondent 2 solicitor logs in more than 3 weeks before hearing
            CaseData caseData = CaseDataBuilder.builder().atStateTrialReadyCheck().build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_START, caseData).build();

            when(userRoleCaching.getUserRoles(anyString(), anyString(), anyString()))
                .thenReturn(List.of(CaseRole.RESPONDENTSOLICITORTWO.getFormattedName()));

            //when: Event is started
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);
            //then: no error is given
            assertThat(response.getErrors()).isEmpty();
        }

        @Test
        void shouldReturnError_WhenAboutToStartIsInvokedWithinThreeWeeksOfHearingDate_ApplicantSolicitor() {
            //given: applicant solicitor logs in less than 3 weeks before the hearing
            CaseData caseData = CaseDataBuilder.builder().atStateTrialReadyCheck()
                .hearingDate(LocalDate.now().plusWeeks(2).plusDays(6)).build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_START, caseData).build();

            when(userRoleCaching.getUserRoles(anyString(), anyString(), anyString()))
                .thenReturn(List.of(CaseRole.APPLICANTSOLICITORONE.getFormattedName()));

            //when: Event is started
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);
            //then: an error is returned
            assertThat(response.getErrors()).isNotEmpty();
        }

        @Test
        void shouldReturnError_WhenAboutToStartIsInvokedWithinThreeWeeksOfHearingDate_Claimant() {
            //given: claimant logs in less than 3 weeks before the hearing
            CaseData caseData = CaseDataBuilder.builder().atStateTrialReadyCheck()
                .hearingDate(LocalDate.now().plusWeeks(2).plusDays(6)).build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_START, caseData).build();
            when(userRoleCaching.getUserRoles(anyString(), anyString(), anyString()))
                .thenReturn(List.of(CaseRole.CLAIMANT.getFormattedName()));

            //when: Event is started
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);
            //then: an error is returned
            assertThat(response.getErrors()).isNotEmpty();
        }

        @Test
        void shouldReturnError_WhenAboutToStartIsInvokedWithinThreeWeeksOfHearingDate_RespondentSolicitor1() {
            //given: respondent 1 solicitor logs in less than 3 weeks before the hearing
            CaseData caseData = CaseDataBuilder.builder().atStateTrialReadyCheck()
                .hearingDate(LocalDate.now().plusWeeks(2).plusDays(6)).build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_START, caseData).build();

            when(userRoleCaching.getUserRoles(anyString(), anyString(), anyString()))
                .thenReturn(List.of(CaseRole.RESPONDENTSOLICITORONE.getFormattedName()));

            //when: Event is started
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);
            //then: an error is returned
            assertThat(response.getErrors()).isNotEmpty();
        }

        @Test
        void shouldReturnError_WhenAboutToStartIsInvokedWithinThreeWeeksOfHearingDate_Defendant() {
            //given: respondent 1 solicitor logs in less than 3 weeks before the hearing
            CaseData caseData = CaseDataBuilder.builder().atStateTrialReadyCheck()
                .hearingDate(LocalDate.now().plusWeeks(2).plusDays(6)).build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_START, caseData).build();
            when(userRoleCaching.getUserRoles(anyString(), anyString(), anyString()))
                .thenReturn(List.of(CaseRole.DEFENDANT.getFormattedName()));

            //when: Event is started
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);
            //then: an error is returned
            assertThat(response.getErrors()).isNotEmpty();
        }

        @Test
        void shouldReturnError_WhenAboutToStartIsInvokedWithinThreeWeeksOfHearingDate_RespondentSolicitor2() {
            //given: respondent 2 solicitor logs in less than 3 weeks before the hearing
            CaseData caseData = CaseDataBuilder.builder().atStateTrialReadyCheck()
                .hearingDate(LocalDate.now().plusWeeks(2).plusDays(6)).build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_START, caseData).build();

            when(userRoleCaching.getUserRoles(anyString(), anyString(), anyString()))
                .thenReturn(List.of(CaseRole.RESPONDENTSOLICITORTWO.getFormattedName()));

            //when: Event is started
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);
            //then: an error is returned
            assertThat(response.getErrors()).isNotEmpty();
        }
    }

    @Nested
    class AboutToSubmitCallback {

        @BeforeEach
        public void setup() {
            when(userService.getUserInfo(anyString())).thenReturn(UserInfo.builder().uid("uid").build());
        }

        @Test
        void shouldTriggerApplicantNotifyOthers_WhenAboutToSubmitIsInvoked_ApplicantSolicitor() {
            CaseData caseData = CaseDataBuilder.builder().atStateTrialReadyCheck().build().toBuilder()
                .trialReadyApplicant(YesOrNo.YES).build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            when(userRoleCaching.getUserRoles(anyString(), anyString(), anyString()))
                .thenReturn(List.of(CaseRole.APPLICANTSOLICITORONE.getFormattedName()));

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isNull();
            assertThat(response.getData()).extracting("businessProcess")
                .extracting("camundaEvent", "status")
                .containsOnly(CaseEvent.APPLICANT_TRIAL_READY_NOTIFY_OTHERS.name(), "READY");
        }

        @Test
        void shouldTriggerApplicantNotifyOthers_WhenAboutToSubmitIsInvoked_Claimant() {
            CaseData caseData = CaseDataBuilder.builder().atStateTrialReadyCheck().build().toBuilder()
                .trialReadyApplicant(YesOrNo.YES).build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            when(userRoleCaching.getUserRoles(anyString(), anyString(), anyString()))
                .thenReturn(List.of(CaseRole.CLAIMANT.getFormattedName()));

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isNull();
            assertThat(response.getData()).extracting("businessProcess")
                .extracting("camundaEvent", "status")
                .containsOnly(CaseEvent.APPLICANT_TRIAL_READY_NOTIFY_OTHERS.name(), "READY");

        }

        @Test

        void shouldTriggerRespondent1NotifyOthers_WhenAboutToSubmitIsInvoked_Respondent1Solicitor() {
            CaseData caseData = CaseDataBuilder.builder().atStateTrialReadyCheck().build().toBuilder()
                .trialReadyRespondent1(YesOrNo.YES).build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            when(userRoleCaching.getUserRoles(anyString(), anyString(), anyString()))
                .thenReturn(List.of(CaseRole.RESPONDENTSOLICITORONE.getFormattedName()));

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isNull();
            assertThat(response.getData()).extracting("businessProcess")
                .extracting("camundaEvent", "status")
                .containsOnly(CaseEvent.RESPONDENT1_TRIAL_READY_NOTIFY_OTHERS.name(), "READY");
        }

        @Test
        void shouldTriggerRespondent1NotifyOthers_WhenAboutToSubmitIsInvoked_Defendant() {
            CaseData caseData = CaseDataBuilder.builder().atStateTrialReadyCheck().build().toBuilder()
                .trialReadyRespondent1(YesOrNo.YES).build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            when(userRoleCaching.getUserRoles(anyString(), anyString(), anyString()))
                .thenReturn(List.of(CaseRole.DEFENDANT.getFormattedName()));

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isNull();
            assertThat(response.getData()).extracting("businessProcess")
                .extracting("camundaEvent", "status")
                .containsOnly(CaseEvent.RESPONDENT1_TRIAL_READY_NOTIFY_OTHERS.name(), "READY");
        }

        @Test
        void shouldTriggerRespondent2NotifyOthers_WhenAboutToSubmitIsInvoked_Respondent2Solicitor() {
            CaseData caseData = CaseDataBuilder.builder().atStateTrialReadyCheck().build().toBuilder()
                .trialReadyRespondent2(YesOrNo.YES).build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            when(userRoleCaching.getUserRoles(anyString(), anyString(), anyString()))
                .thenReturn(List.of(CaseRole.RESPONDENTSOLICITORTWO.getFormattedName()));

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isNull();
            assertThat(response.getData()).extracting("businessProcess")
                .extracting("camundaEvent", "status")
                .containsOnly(CaseEvent.RESPONDENT2_TRIAL_READY_NOTIFY_OTHERS.name(), "READY");
        }

        @Test
        void shouldTriggerApplicantDocument_WhenAboutToSubmitIsInvoked_ApplicantSolicitor() {
            CaseData caseData = CaseDataBuilder.builder().atStateTrialReadyCheck().build().toBuilder()
                .trialReadyApplicant(YesOrNo.NO).build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            when(userRoleCaching.getUserRoles(anyString(), anyString(), anyString()))
                .thenReturn(List.of(CaseRole.APPLICANTSOLICITORONE.getFormattedName()));

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isNull();
            assertThat(response.getData()).extracting("businessProcess")
                .extracting("camundaEvent", "status")
                .containsOnly(CaseEvent.GENERATE_TRIAL_READY_DOCUMENT_APPLICANT.name(), "READY");
        }

        @Test
        void shouldTriggerApplicantDocument_WhenAboutToSubmitIsInvoked_Claimant() {
            CaseData caseData = CaseDataBuilder.builder().atStateTrialReadyCheck().build().toBuilder()
                .trialReadyApplicant(YesOrNo.NO).build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            when(coreCaseUserService.getUserCaseRoles(anyString(), anyString()))
                .thenReturn(List.of(CaseRole.CLAIMANT.getFormattedName()));

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isNull();
            assertThat(response.getData()).extracting("businessProcess")
                .extracting("camundaEvent", "status")
                .containsOnly(CaseEvent.GENERATE_TRIAL_READY_DOCUMENT_APPLICANT.name(), "READY");
        }

        @Test
        void shouldTriggerRespondent1Document_WhenAboutToSubmitIsInvoked_Respondent1Solicitor() {
            CaseData caseData = CaseDataBuilder.builder().atStateTrialReadyCheck().build().toBuilder()
                .trialReadyRespondent1(YesOrNo.NO).build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            when(userRoleCaching.getUserRoles(anyString(), anyString(), anyString()))
                .thenReturn(List.of(CaseRole.RESPONDENTSOLICITORONE.getFormattedName()));

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isNull();
            assertThat(response.getData()).extracting("businessProcess")
                .extracting("camundaEvent", "status")
                .containsOnly(CaseEvent.GENERATE_TRIAL_READY_DOCUMENT_RESPONDENT1.name(), "READY");
        }

        @Test
        void shouldTriggerRespondent1Document_WhenAboutToSubmitIsInvoked_Defendant() {
            CaseData caseData = CaseDataBuilder.builder().atStateTrialReadyCheck().build().toBuilder()
                .trialReadyRespondent1(YesOrNo.NO).build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            when(userRoleCaching.getUserRoles(anyString(), anyString(), anyString()))
                .thenReturn(List.of(CaseRole.DEFENDANT.getFormattedName()));

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isNull();
            assertThat(response.getData()).extracting("businessProcess")
                .extracting("camundaEvent", "status")
                .containsOnly(CaseEvent.GENERATE_TRIAL_READY_DOCUMENT_RESPONDENT1.name(), "READY");
        }

        @Test
        void shouldTriggerRespondent2Document_WhenAboutToSubmitIsInvoked_Respondent2Solicitor() {
            CaseData caseData = CaseDataBuilder.builder().atStateTrialReadyCheck().build().toBuilder()
                .trialReadyRespondent2(YesOrNo.NO).build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            when(userRoleCaching.getUserRoles(anyString(), anyString(), anyString()))
                .thenReturn(List.of(CaseRole.RESPONDENTSOLICITORTWO.getFormattedName()));

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isNull();
            assertThat(response.getData()).extracting("businessProcess")
                .extracting("camundaEvent", "status")
                .containsOnly(CaseEvent.GENERATE_TRIAL_READY_DOCUMENT_RESPONDENT2.name(), "READY");
        }
    }

    @Nested
    class SubmittedCallback {

        @BeforeEach
        public void setup() {
            when(userService.getUserInfo(anyString())).thenReturn(UserInfo.builder().uid("uid").build());
        }

        @Test
        void shouldReturnConfirmationScreen_when1v1ReadySubmitted_ApplicantSolicitor() {
            //given: applicant solicitor selects Ready
            CaseData caseData = CaseDataBuilder.builder().atStateTrialReadyApplicant().build();
            CallbackParams params = CallbackParamsBuilder.builder().of(SUBMITTED, caseData).build();

            when(userRoleCaching.getUserRoles(anyString(), anyString(), anyString()))
                .thenReturn(List.of(CaseRole.APPLICANTSOLICITORONE.getFormattedName()));

            //when: Event is submitted
            SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);
            //then: header + body for the ready status get used in the confirmation
            assertThat(response).usingRecursiveComparison().isEqualTo(
                SubmittedCallbackResponse.builder()
                    .confirmationHeader(READY_HEADER)
                    .confirmationBody(READY_BODY)
                    .build()
            );
        }

        @Test
        void shouldReturnConfirmationScreen_when1v1ReadySubmitted_Claimant() {
            //given: claimant selects Ready
            CaseData caseData = CaseDataBuilder.builder().atStateTrialReadyApplicant().build();
            CallbackParams params = CallbackParamsBuilder.builder().of(SUBMITTED, caseData).build();
            when(userRoleCaching.getUserRoles(anyString(), anyString(), anyString()))
                .thenReturn(List.of(CaseRole.CLAIMANT.getFormattedName()));

            //when: Event is submitted
            SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);

            //then: header + body for the ready status get used in the confirmation
            assertThat(response).usingRecursiveComparison().isEqualTo(
                SubmittedCallbackResponse.builder()
                    .confirmationHeader(READY_HEADER)
                    .confirmationBody(READY_BODY)
                    .build()
            );
        }

        @Test
        void shouldReturnConfirmationScreen_when1v1NotReadySubmitted_ApplicantSolicitor() {
            //given: applicant solicitor selects Not Ready
            CaseData caseData = CaseDataBuilder.builder().atStateTrialNotReadyApplicant().build();
            CallbackParams params = CallbackParamsBuilder.builder().of(SUBMITTED, caseData).build();

            when(userRoleCaching.getUserRoles(anyString(), anyString(), anyString()))
                .thenReturn(List.of(CaseRole.APPLICANTSOLICITORONE.getFormattedName()));

            //when: Event is submitted
            SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);

            //then: header + body for the not ready status get used in the confirmation
            assertThat(response).usingRecursiveComparison().isEqualTo(
                SubmittedCallbackResponse.builder()
                    .confirmationHeader(NOT_READY_HEADER)
                    .confirmationBody(NOT_READY_BODY)
                    .build()
            );
        }

        @Test
        void shouldReturnConfirmationScreen_when1v1NotReadySubmitted_Claimant() {
            //given: claimant selects Not Ready
            CaseData caseData = CaseDataBuilder.builder().atStateTrialNotReadyApplicant().build();
            CallbackParams params = CallbackParamsBuilder.builder().of(SUBMITTED, caseData).build();
            when(userRoleCaching.getUserRoles(anyString(), anyString(), anyString()))
                .thenReturn(List.of(CaseRole.CLAIMANT.getFormattedName()));

            //when: Event is submitted
            SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);

            //then: header + body for the not ready status get used in the confirmation
            assertThat(response).usingRecursiveComparison().isEqualTo(
                SubmittedCallbackResponse.builder()
                    .confirmationHeader(NOT_READY_HEADER)
                    .confirmationBody(NOT_READY_BODY)
                    .build()
            );
        }

        @Test
        void shouldReturnConfirmationScreen_when1v1ReadySubmitted_Respondent1Solicitor() {
            //given: respondent 1 solicitor selects Ready
            CaseData caseData = CaseDataBuilder.builder().atStateTrialReadyRespondent1().build();
            CallbackParams params = CallbackParamsBuilder.builder().of(SUBMITTED, caseData).build();

            when(userRoleCaching.getUserRoles(anyString(), anyString(), anyString()))
                .thenReturn(List.of(CaseRole.RESPONDENTSOLICITORONE.getFormattedName()));

            //when: Event is submitted
            SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);

            //then: header + body for the ready status get used in the confirmation
            assertThat(response).usingRecursiveComparison().isEqualTo(
                SubmittedCallbackResponse.builder()
                    .confirmationHeader(READY_HEADER)
                    .confirmationBody(READY_BODY)
                    .build()
            );
        }

        @Test
        void shouldReturnConfirmationScreen_when1v1ReadySubmitted_Defendant() {
            //given: defendant selects Ready
            CaseData caseData = CaseDataBuilder.builder().atStateTrialReadyRespondent1().build();
            CallbackParams params = CallbackParamsBuilder.builder().of(SUBMITTED, caseData).build();
            when(userRoleCaching.getUserRoles(anyString(), anyString(), anyString()))
                .thenReturn(List.of(CaseRole.DEFENDANT.getFormattedName()));

            //when: Event is submitted
            SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);

            //then: header + body for the ready status get used in the confirmation
            assertThat(response).usingRecursiveComparison().isEqualTo(
                SubmittedCallbackResponse.builder()
                    .confirmationHeader(READY_HEADER)
                    .confirmationBody(READY_BODY)
                    .build()
            );
        }

        @Test
        void shouldReturnConfirmationScreen_when1v1NotReadySubmitted_Respondent1Solicitor() {
            //given: respondent 1 solicitor selects Not Ready
            CaseData caseData = CaseDataBuilder.builder().atStateTrialNotReadyRespondent1().build();
            CallbackParams params = CallbackParamsBuilder.builder().of(SUBMITTED, caseData).build();

            when(userRoleCaching.getUserRoles(anyString(), anyString(), anyString()))
                .thenReturn(List.of(CaseRole.RESPONDENTSOLICITORONE.getFormattedName()));

            //when: Event is submitted
            SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);

            //then: header + body for the not ready status get used in the confirmation
            assertThat(response).usingRecursiveComparison().isEqualTo(
                SubmittedCallbackResponse.builder()
                    .confirmationHeader(NOT_READY_HEADER)
                    .confirmationBody(NOT_READY_BODY)
                    .build()
            );
        }

        @Test
        void shouldReturnConfirmationScreen_when1v1NotReadySubmitted_Defendant() {
            //given: defendant selects Not Ready
            CaseData caseData = CaseDataBuilder.builder().atStateTrialNotReadyRespondent1().build();
            CallbackParams params = CallbackParamsBuilder.builder().of(SUBMITTED, caseData).build();
            when(userRoleCaching.getUserRoles(anyString(), anyString(), anyString()))
                .thenReturn(List.of(CaseRole.DEFENDANT.getFormattedName()));

            //when: Event is submitted
            SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);

            //then: header + body for the not ready status get used in the confirmation
            assertThat(response).usingRecursiveComparison().isEqualTo(
                SubmittedCallbackResponse.builder()
                    .confirmationHeader(NOT_READY_HEADER)
                    .confirmationBody(NOT_READY_BODY)
                    .build()
            );
        }

        @Test
        void shouldReturnConfirmationScreen_when1v1ReadySubmitted_Respondent2Solicitor() {
            //given: Respondent 2 selects Ready
            CaseData caseData = CaseDataBuilder.builder().atStateTrialReadyRespondent2().build();
            CallbackParams params = CallbackParamsBuilder.builder().of(SUBMITTED, caseData).build();

            when(userRoleCaching.getUserRoles(anyString(), anyString(), anyString()))
                .thenReturn(List.of(CaseRole.RESPONDENTSOLICITORTWO.getFormattedName()));

            //when: Event is submitted
            SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);
            //then: head + body for ready status are used in confirmation
            assertThat(response).usingRecursiveComparison().isEqualTo(
                SubmittedCallbackResponse.builder()
                    .confirmationHeader(READY_HEADER)
                    .confirmationBody(READY_BODY)
                    .build()
            );
        }

        @Test
        void shouldReturnConfirmationScreen_when1v1NotReadySubmitted_Respondent2Solicitor() {
            //given: Respondent 2 solicitor selects Not Ready
            CaseData caseData = CaseDataBuilder.builder().atStateTrialNotReadyRespondent2().build();
            CallbackParams params = CallbackParamsBuilder.builder().of(SUBMITTED, caseData).build();

            when(userRoleCaching.getUserRoles(anyString(), anyString(), anyString()))
                .thenReturn(List.of(CaseRole.RESPONDENTSOLICITORTWO.getFormattedName()));

            //when: Event is submitted
            SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);
            //then: head + body for not ready status are used in confirmation
            assertThat(response).usingRecursiveComparison().isEqualTo(
                SubmittedCallbackResponse.builder()
                    .confirmationHeader(NOT_READY_HEADER)
                    .confirmationBody(NOT_READY_BODY)
                    .build()
            );
        }
    }
}
