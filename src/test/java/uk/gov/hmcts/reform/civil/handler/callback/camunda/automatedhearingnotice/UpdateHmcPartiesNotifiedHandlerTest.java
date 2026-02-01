package uk.gov.hmcts.reform.civil.handler.callback.camunda.automatedhearingnotice;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.hearingnotice.HearingNoticeCamundaService;
import uk.gov.hmcts.reform.civil.service.hearingnotice.HearingNoticeVariables;
import uk.gov.hmcts.reform.hmc.model.unnotifiedhearings.HearingDay;
import uk.gov.hmcts.reform.hmc.model.unnotifiedhearings.PartiesNotified;
import uk.gov.hmcts.reform.hmc.service.HearingsService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;

@ExtendWith(MockitoExtension.class)
class UpdateHmcPartiesNotifiedHandlerTest extends BaseCallbackHandlerTest {

    @InjectMocks
    private UpdateHmcPartiesNotifiedHandler handler;

    @Mock
    private HearingNoticeCamundaService hearingNoticeCamundaService;

    @Mock
    private HearingsService hearingsService;

    private CallbackParams buildCallbackParams(CaseData caseData) {
        return CallbackParams.builder()
            .type(ABOUT_TO_SUBMIT)
            .caseData(caseData)
            .params(Map.of(CallbackParams.Params.BEARER_TOKEN, "BEARER_TOKEN"))
            .build();
    }

    private HearingNoticeVariables sampleCamundaVars() {
        return HearingNoticeVariables.builder()
            .hearingId("HER1234")
            .hearingLocationEpims("12345")
            .days(List.of(HearingDay.builder()
                              .hearingStartDateTime(LocalDateTime.of(2023, 1, 1, 0, 0, 0))
                              .hearingEndDateTime(LocalDateTime.of(2023, 1, 1, 12, 0, 0))
                              .build()))
            .hearingStartDateTime(LocalDateTime.of(2022, 11, 7, 15, 15))
            .requestVersion(10L)
            .responseDateTime(LocalDateTime.of(2022, 10, 10, 15, 15))
            .build();
    }

    @Test
    void shouldSuccessfullyUpdatePartiesNotified_whenServiceReturns200() {
        // Given
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setCcdCaseReference(12345L);
        BusinessProcess businessProcess = new BusinessProcess();
        businessProcess.setProcessInstanceId("");
        caseData.setBusinessProcess(businessProcess);
        CallbackParams params = buildCallbackParams(caseData);
        HearingNoticeVariables camundaVars = sampleCamundaVars();

        when(hearingNoticeCamundaService.getProcessVariables(any())).thenReturn(camundaVars);
        when(hearingsService.updatePartiesNotifiedResponse(anyString(), anyString(), anyInt(), any(), any(PartiesNotified.class)))
            .thenReturn(new ResponseEntity<>(HttpStatus.OK));

        // When
        var response = handler.handle(params);

        // Then
        assertNotNull(response);
        verify(hearingsService).updatePartiesNotifiedResponse(eq("BEARER_TOKEN"), eq("HER1234"), anyInt(), any(), any(PartiesNotified.class));
    }

    @Test
    void shouldLogWarning_whenServiceReturnsNon2xx() {
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setCcdCaseReference(12345L);
        BusinessProcess businessProcess = new BusinessProcess();
        businessProcess.setProcessInstanceId("");
        caseData.setBusinessProcess(businessProcess);
        CallbackParams params = buildCallbackParams(caseData);
        HearingNoticeVariables camundaVars = sampleCamundaVars();

        when(hearingNoticeCamundaService.getProcessVariables(any())).thenReturn(camundaVars);
        when(hearingsService.updatePartiesNotifiedResponse(anyString(), anyString(), anyInt(), any(), any()))
            .thenReturn(new ResponseEntity<>("Error", HttpStatus.BAD_REQUEST));

        var response = handler.handle(params);

        assertNotNull(response);
        verify(hearingsService).updatePartiesNotifiedResponse(eq("BEARER_TOKEN"), eq("HER1234"), anyInt(), any(), any(PartiesNotified.class));
    }

    @Test
    void shouldHandleNullResponseGracefully() {
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setCcdCaseReference(12345L);
        BusinessProcess businessProcess = new BusinessProcess();
        businessProcess.setProcessInstanceId("");
        caseData.setBusinessProcess(businessProcess);
        CallbackParams params = buildCallbackParams(caseData);
        HearingNoticeVariables camundaVars = sampleCamundaVars();

        when(hearingNoticeCamundaService.getProcessVariables(any())).thenReturn(camundaVars);
        when(hearingsService.updatePartiesNotifiedResponse(anyString(), anyString(), anyInt(), any(), any()))
            .thenReturn(null);

        var response = handler.handle(params);

        assertNotNull(response);
        verify(hearingsService).updatePartiesNotifiedResponse(anyString(), anyString(), anyInt(), any(), any());
    }

    @Test
    void shouldCatchHttpClientErrorExceptionBadRequest() {
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setCcdCaseReference(12345L);
        BusinessProcess businessProcess = new BusinessProcess();
        businessProcess.setProcessInstanceId("");
        caseData.setBusinessProcess(businessProcess);
        CallbackParams params = buildCallbackParams(caseData);
        HearingNoticeVariables camundaVars = sampleCamundaVars();

        when(hearingNoticeCamundaService.getProcessVariables(any())).thenReturn(camundaVars);
        doThrow(HttpClientErrorException.BadRequest.create(
            HttpStatus.BAD_REQUEST,
            "Bad Request",
            HttpHeaders.EMPTY,
            null,
            null
        )).when(hearingsService).updatePartiesNotifiedResponse(
            anyString(), anyString(), anyInt(), any(), any()
        );
        AboutToStartOrSubmitCallbackResponse callbackResponse = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        assertNotNull(callbackResponse);
        verify(hearingsService).updatePartiesNotifiedResponse(anyString(), anyString(), anyInt(), any(), any());
    }
}
