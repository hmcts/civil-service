package uk.gov.hmcts.reform.civil.handler.callback.camunda.automatedhearingnotice;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.config.SystemUpdateUserConfiguration;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.civil.service.hearingnotice.HearingNoticeCamundaService;
import uk.gov.hmcts.reform.civil.service.hearingnotice.HearingNoticeVariables;
import uk.gov.hmcts.reform.hmc.model.unnotifiedhearings.HearingDay;
import uk.gov.hmcts.reform.hmc.model.unnotifiedhearings.PartiesNotified;
import uk.gov.hmcts.reform.hmc.model.unnotifiedhearings.PartiesNotifiedResponse;
import uk.gov.hmcts.reform.hmc.model.unnotifiedhearings.PartiesNotifiedResponses;
import uk.gov.hmcts.reform.hmc.service.HearingsService;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
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
    @Mock
    private SystemUpdateUserConfiguration userConfig;
    @Mock
    private UserService userService;
    @Mock
    private ObjectMapper mapper;

    static final String AUTH_TOKEN = "mock_token";

    @BeforeEach
    void init() {
        when(userService.getAccessToken(anyString(), anyString())).thenReturn(AUTH_TOKEN);
        when(userService.getUserInfo(anyString())).thenReturn(UserInfo.builder().uid("test-id").build());
        when(userConfig.getUserName()).thenReturn("USER");
        when(userConfig.getPassword()).thenReturn("PASSWORD");
        when(hearingsService.getPartiesNotifiedResponses(anyString(), anyString())).thenReturn(
            PartiesNotifiedResponses.builder().build());
    }

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
    void shouldSuccessfullyUpdatePartiesNotified_whenServiceReturns200() throws JsonProcessingException {
        when(hearingsService.getPartiesNotifiedResponses(anyString(), anyString())).thenReturn(
            PartiesNotifiedResponses.builder().build());
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
        when(mapper.writeValueAsString(any())).thenReturn("{}");

        // When
        var response = handler.handle(params);

        // Then
        assertNotNull(response);
        verify(mapper).writeValueAsString(any(PartiesNotified.class));
        verify(hearingsService).updatePartiesNotifiedResponse(eq("BEARER_TOKEN"), eq("HER1234"), anyInt(), any(), any(PartiesNotified.class));
    }

    @Test
    void shouldNotUpdatePartiesNotified_whenPartiesAlreadyNotified() {
        // Given
        when(hearingsService.getPartiesNotifiedResponses(anyString(), anyString())).thenReturn(
            PartiesNotifiedResponses.builder().responses(List.of(PartiesNotifiedResponse.builder()
                                                                     .responseReceivedDateTime(LocalDateTime.now()).build())).build());

        CaseData caseData = CaseData.builder().ccdCaseReference(12345L).build().toBuilder()
            .businessProcess(BusinessProcess.builder().processInstanceId("").build())
            .build();
        CallbackParams params = buildCallbackParams(caseData);
        HearingNoticeVariables camundaVars = sampleCamundaVars();
        when(hearingNoticeCamundaService.getProcessVariables(any())).thenReturn(camundaVars);

        CallbackResponse response = handler.handle(params);

        assertNotNull(response);
        verify(hearingsService, never())
            .updatePartiesNotifiedResponse(any(), anyString(), anyInt(), any(), any());
    }

    @Test
    void shouldLogWarning_whenServiceReturnsNon2xx() throws JsonProcessingException {
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setCcdCaseReference(12345L);
        BusinessProcess businessProcess = new BusinessProcess();
        businessProcess.setProcessInstanceId("");
        caseData.setBusinessProcess(businessProcess);
        CallbackParams params = buildCallbackParams(caseData);
        HearingNoticeVariables camundaVars = sampleCamundaVars();

        when(mapper.writeValueAsString(any())).thenReturn("{}");
        when(hearingNoticeCamundaService.getProcessVariables(any())).thenReturn(camundaVars);
        when(hearingsService.updatePartiesNotifiedResponse(anyString(), anyString(), anyInt(), any(), any()))
            .thenReturn(new ResponseEntity<>("Error", HttpStatus.BAD_REQUEST));

        var response = handler.handle(params);

        assertNotNull(response);
        verify(mapper).writeValueAsString(any(PartiesNotified.class));
        verify(hearingsService).updatePartiesNotifiedResponse(eq("BEARER_TOKEN"), eq("HER1234"), anyInt(), any(), any(PartiesNotified.class));
    }

    @Test
    void shouldHandleNullResponseGracefully() throws JsonProcessingException {
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setCcdCaseReference(12345L);
        BusinessProcess businessProcess = new BusinessProcess();
        businessProcess.setProcessInstanceId("");
        caseData.setBusinessProcess(businessProcess);
        CallbackParams params = buildCallbackParams(caseData);
        HearingNoticeVariables camundaVars = sampleCamundaVars();

        when(mapper.writeValueAsString(any())).thenReturn("{}");
        when(hearingNoticeCamundaService.getProcessVariables(any())).thenReturn(camundaVars);
        when(hearingsService.updatePartiesNotifiedResponse(anyString(), anyString(), anyInt(), any(), any()))
            .thenReturn(null);

        var response = handler.handle(params);

        assertNotNull(response);
        verify(hearingsService).updatePartiesNotifiedResponse(anyString(), anyString(), anyInt(), any(), any());
    }

    @Test
    void shouldCatchRestClientException() {
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setCcdCaseReference(12345L);
        BusinessProcess businessProcess = new BusinessProcess();
        businessProcess.setProcessInstanceId("");
        caseData.setBusinessProcess(businessProcess);
        CallbackParams params = buildCallbackParams(caseData);
        HearingNoticeVariables camundaVars = sampleCamundaVars();

        when(hearingNoticeCamundaService.getProcessVariables(any())).thenReturn(camundaVars);
        when(hearingsService.updatePartiesNotifiedResponse(anyString(), anyString(), anyInt(), any(), any()))
            .thenThrow(new RestClientException("Connection refused"));

        var response = handler.handle(params);

        assertNotNull(response);
        verify(hearingsService).updatePartiesNotifiedResponse(anyString(), anyString(), anyInt(), any(), any());
    }

    @Test
    void shouldCatchJsonProcessingException() throws Exception {
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setCcdCaseReference(12345L);
        BusinessProcess businessProcess = new BusinessProcess();
        businessProcess.setProcessInstanceId("");
        caseData.setBusinessProcess(businessProcess);
        CallbackParams params = buildCallbackParams(caseData);
        HearingNoticeVariables camundaVars = sampleCamundaVars();

        when(hearingNoticeCamundaService.getProcessVariables(any())).thenReturn(camundaVars);
        when(mapper.writeValueAsString(any())).thenThrow(new JsonProcessingException("Cannot serialize") {});

        var response = handler.handle(params);

        assertNotNull(response);
        verify(mapper).writeValueAsString(any(PartiesNotified.class));
        verify(hearingsService, never()).updatePartiesNotifiedResponse(anyString(), anyString(), anyInt(), any(), any());
    }
}
