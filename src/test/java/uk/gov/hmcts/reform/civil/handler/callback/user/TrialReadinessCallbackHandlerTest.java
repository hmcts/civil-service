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
import uk.gov.hmcts.reform.civil.enums.CaseRole;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.CoreCaseUserService;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.formatLocalDateTime;
import static uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder.APPLICANT_RESPONSE_DEADLINE;

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
        + "unless a judge makes an order changing the date of the hearing"
        + "If you want the date of the hearing to be changed (or any other order to make the case ready for trial)"
        + "you will need to make an application to the court and pay the appropriate fee.";

    @BeforeEach
    public void setup() {
        when(userService.getUserInfo(anyString())).thenReturn(UserInfo.builder().uid("uid").build());
    }

    @Nested
    class AboutToStartCallback {

        @Test
        void shouldNotReturnError_WhenAboutToStartIsInvoked_ApplicantSolicitor() {
            CaseData caseData = CaseDataBuilder.builder().atStateTrialReadyCheck().build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_START, caseData).build();
            when(userService.getUserInfo(anyString())).thenReturn(UserInfo.builder().uid("uid").build());
            when(coreCaseUserService.userHasCaseRole(anyString(), anyString(), eq(CaseRole.APPLICANTSOLICITORONE)))
                                                                                                .thenReturn(true);

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);
            assertThat(response.getErrors()).isEmpty();
        }

        @Test
        void shouldNotReturnError_WhenAboutToStartIsInvoked_RespondentSolicitor1() {
            CaseData caseData = CaseDataBuilder.builder().atStateTrialReadyCheck().build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_START, caseData).build();
            when(userService.getUserInfo(anyString())).thenReturn(UserInfo.builder().uid("uid").build());
            when(coreCaseUserService.userHasCaseRole(anyString(), anyString(), eq(CaseRole.RESPONDENTSOLICITORONE)))
                .thenReturn(true);

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);
            assertThat(response.getErrors()).isEmpty();
        }

        @Test
        void shouldNotReturnError_WhenAboutToStartIsInvoked_RespondentSolicitor2() {
            CaseData caseData = CaseDataBuilder.builder().atStateTrialReadyCheck().build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_START, caseData).build();
            when(userService.getUserInfo(anyString())).thenReturn(UserInfo.builder().uid("uid").build());
            when(coreCaseUserService.userHasCaseRole(anyString(), anyString(), eq(CaseRole.RESPONDENTSOLICITORTWO)))
                .thenReturn(true);

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);
            assertThat(response.getErrors()).isEmpty();
        }

        @Test
        void shouldReturnError_WhenAboutToStartIsInvokedWithinThreeWeeksOfHearingDate_ApplicantSolicitor() {
            CaseData caseData = CaseDataBuilder.builder().atStateTrialReadyCheck()
                .hearingDate(LocalDate.now().plusWeeks(2).plusDays(6)).build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_START, caseData).build();
            when(userService.getUserInfo(anyString())).thenReturn(UserInfo.builder().uid("uid").build());
            when(coreCaseUserService.userHasCaseRole(anyString(), anyString(), eq(CaseRole.APPLICANTSOLICITORONE)))
                .thenReturn(true);

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);
            assertThat(response.getErrors()).isNotEmpty();
        }

        @Test
        void shouldReturnError_WhenAboutToStartIsInvokedWithinThreeWeeksOfHearingDate_RespondentSolicitor1() {
            CaseData caseData = CaseDataBuilder.builder().atStateTrialReadyCheck()
                .hearingDate(LocalDate.now().plusWeeks(2).plusDays(6)).build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_START, caseData).build();
            when(userService.getUserInfo(anyString())).thenReturn(UserInfo.builder().uid("uid").build());
            when(coreCaseUserService.userHasCaseRole(anyString(), anyString(), eq(CaseRole.RESPONDENTSOLICITORONE)))
                .thenReturn(true);

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);
            assertThat(response.getErrors()).isNotEmpty();
        }

        @Test
        void shouldReturnError_WhenAboutToStartIsInvokedWithinThreeWeeksOfHearingDate_RespondentSolicitor2() {
            CaseData caseData = CaseDataBuilder.builder().atStateTrialReadyCheck()
                .hearingDate(LocalDate.now().plusWeeks(2).plusDays(6)).build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_START, caseData).build();
            when(userService.getUserInfo(anyString())).thenReturn(UserInfo.builder().uid("uid").build());
            when(coreCaseUserService.userHasCaseRole(anyString(), anyString(), eq(CaseRole.RESPONDENTSOLICITORTWO)))
                .thenReturn(true);

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);
            assertThat(response.getErrors()).isNotEmpty();
        }
    }

    @Nested
    class AboutToSubmitCallback {

        @Test
        void shouldReturnNoError_WhenAboutToSubmitIsInvoked() {
            CaseData caseData = CaseDataBuilder.builder().build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isNull();
        }
    }

    @Nested
    class SubmittedCallback {

        @Test
        void shouldReturnConfirmationScreen_when1v1ResponseSubmitted_ApplicantSolicitor() {
            CaseData caseData = CaseDataBuilder.builder().atStateTrialReadyApplicant().build();
            CallbackParams params = CallbackParamsBuilder.builder().of(SUBMITTED, caseData).build();
            when(userService.getUserInfo(anyString())).thenReturn(UserInfo.builder().uid("uid").build());
            when(coreCaseUserService.userHasCaseRole(anyString(), anyString(), eq(CaseRole.APPLICANTSOLICITORONE)))
                .thenReturn(true);

            SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);

            assertThat(response).usingRecursiveComparison().isEqualTo(
                SubmittedCallbackResponse.builder()
                    .confirmationHeader(READY_HEADER)
                    .confirmationBody(READY_BODY)
                    .build()
            );
        }

        @Test
        void shouldReturnConfirmationScreen_when1v1ResponseSubmitted() {
            CaseData caseData = CaseDataBuilder.builder().atStateTrialReadyRespondent1().build();
            CallbackParams params = CallbackParamsBuilder.builder().of(SUBMITTED, caseData).build();
            when(userService.getUserInfo(anyString())).thenReturn(UserInfo.builder().uid("uid").build());
            when(coreCaseUserService.userHasCaseRole(anyString(), anyString(), eq(CaseRole.RESPONDENTSOLICITORONE)))
                .thenReturn(true);

            SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);

            assertThat(response).usingRecursiveComparison().isEqualTo(
                SubmittedCallbackResponse.builder()
                    .confirmationHeader(READY_HEADER)
                    .confirmationBody(READY_BODY)
                    .build()
            );
        }

        @Test
        void shouldReturnConfirmationScreen_when1v1ResponseSubmitted() {
            CaseData caseData = CaseDataBuilder.builder().atStateTrialReadyApplicant().build();
            CallbackParams params = CallbackParamsBuilder.builder().of(SUBMITTED, caseData).build();
            when(userService.getUserInfo(anyString())).thenReturn(UserInfo.builder().uid("uid").build());
            when(coreCaseUserService.userHasCaseRole(anyString(), anyString(), eq(CaseRole.APPLICANTSOLICITORONE)))
                .thenReturn(true);

            SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);

            assertThat(response).usingRecursiveComparison().isEqualTo(
                SubmittedCallbackResponse.builder()
                    .confirmationHeader(READY_HEADER)
                    .confirmationBody(READY_BODY)
                    .build()
            );
        }
    }
}
