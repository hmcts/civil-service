package uk.gov.hmcts.reform.civil.handler.callback.user;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.launchdarkly.FeatureToggleService;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilderSpec;
import uk.gov.hmcts.reform.civil.service.CoreCaseUserService;
import uk.gov.hmcts.reform.civil.service.DeadlinesCalculator;
import uk.gov.hmcts.reform.civil.service.ExitSurveyContentService;
import uk.gov.hmcts.reform.civil.service.Time;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.civil.service.WorkingDayIndicator;
import uk.gov.hmcts.reform.civil.service.flowstate.StateFlowEngine;
import uk.gov.hmcts.reform.civil.stateflow.StateFlow;
import uk.gov.hmcts.reform.civil.validation.DeadlineExtensionValidator;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;
import java.time.LocalDate;
import java.time.LocalDateTime;
import static java.time.LocalDate.now;
import static java.time.format.DateTimeFormatter.ISO_DATE_TIME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.enums.CaseRole.RESPONDENTSOLICITORTWO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.service.DeadlinesCalculator.END_OF_BUSINESS_DAY;

@SpringBootTest(classes = {
    InformAgreedExtensionDateForSpecCallbackHandler.class,
    JacksonAutoConfiguration.class,
    StateFlowEngine.class,
    CaseDetailsConverter.class,
    DeadlineExtensionValidator.class,
    DeadlinesCalculator.class,
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
    void ldBlock() {
        Mockito.when(toggleService.isLrSpecEnabled()).thenReturn(false, true);
        Assertions.assertTrue(handler.handledEvents().isEmpty());
        Assertions.assertFalse(handler.handledEvents().isEmpty());
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
    }
}
