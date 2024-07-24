package uk.gov.hmcts.reform.civil.handler.callback.camunda.automatedhearingnotice;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackType;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.hearingnotice.HearingNoticeCamundaService;
import uk.gov.hmcts.reform.civil.service.hearingnotice.HearingNoticeVariables;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.enums.CaseState.HEARING_READINESS;
import static uk.gov.hmcts.reform.civil.enums.CaseState.PREPARE_FOR_HEARING_CONDUCT_HEARING;

@ExtendWith(MockitoExtension.class)
class UpdateCaseProgressHandlerTest extends BaseCallbackHandlerTest {

    @InjectMocks
    private UpdateCaseProgressHandler handler;

    @Mock
    private HearingNoticeCamundaService camundaService;

    @Test
    void shouldReturnCallbackResponseWithHearingReadinessCaseStateOnAboutToSubmitTrialHearing() {
        when(camundaService.getProcessVariables(any()))
            .thenReturn(HearingNoticeVariables.builder()
                            .hearingType("AAA7-TRI")
                            .build());
        CaseData caseData = CaseData.builder()
            .businessProcess(BusinessProcess.builder()
                                 .processInstanceId("PROCESS_ID")
                                 .build())
            .build();

        var params = callbackParamsOf(caseData, CallbackType.ABOUT_TO_SUBMIT);

        var result = handler.handle(params);

        assertEquals(result, AboutToStartOrSubmitCallbackResponse.builder()
            .state(HEARING_READINESS.name())
            .build());
    }

    @ParameterizedTest
    @CsvSource({
        "AAA7-DIS",
        "AAA7-DRH"
    })
    void shouldReturnCallbackResponseWithHearingReadinessCaseStateOnAboutToSubmit(String hearingType) {
        when(camundaService.getProcessVariables(any()))
            .thenReturn(HearingNoticeVariables.builder()
                            .hearingType(hearingType)
                            .build());
        CaseData caseData = CaseData.builder()
            .businessProcess(BusinessProcess.builder()
                                 .processInstanceId("PROCESS_ID")
                                 .build())
            .build();

        var params = callbackParamsOf(caseData, CallbackType.ABOUT_TO_SUBMIT);

        var result = handler.handle(params);

        assertEquals(result, AboutToStartOrSubmitCallbackResponse.builder()
            .state(PREPARE_FOR_HEARING_CONDUCT_HEARING.name())
            .build());
    }

    @Test
    void shouldReturnCallbackResponseWithHearingReadinessCaseStateOnAboutToSubmitDisputeResolutionHearing() {
        when(camundaService.getProcessVariables(any()))
                .thenReturn(HearingNoticeVariables.builder()
                        .hearingType("AAA7-DRH")
                        .build());
        CaseData caseData = CaseData.builder()
                .businessProcess(BusinessProcess.builder()
                        .processInstanceId("PROCESS_ID")
                        .build())
                .build();

        var params = callbackParamsOf(caseData, CallbackType.ABOUT_TO_SUBMIT);

        var result = handler.handle(params);

        assertEquals(result, AboutToStartOrSubmitCallbackResponse.builder()
                .state(PREPARE_FOR_HEARING_CONDUCT_HEARING.name())
                .build());
    }

}
