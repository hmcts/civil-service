package uk.gov.hmcts.reform.civil.handler.callback.camunda.automatedhearingnotice;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
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
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;


@ExtendWith(MockitoExtension.class)
class UpdateHmcPartiesNotifiedHandlerTest extends BaseCallbackHandlerTest {

    private UpdateHmcPartiesNotifiedHandler handler;

    @Mock
    private HearingNoticeCamundaService hearingNoticeCamundaService;

    @Mock
    private HearingsService hearingsService;

    @Mock
    private UserService userService;

    @Mock
    private SystemUpdateUserConfiguration userConfig;

    private static final String AUTH_TOKEN = "mock_token";

    @BeforeEach
    void setUp() {
        handler = new UpdateHmcPartiesNotifiedHandler(
            hearingNoticeCamundaService,
            hearingsService,
            userService,
            userConfig
        );

        when(userService.getAccessToken(anyString(), anyString())).thenReturn(AUTH_TOKEN);
        when(userService.getUserInfo(anyString())).thenReturn(UserInfo.builder().uid("test-id").build());
        when(userConfig.getUserName()).thenReturn("USER");
        when(userConfig.getPassword()).thenReturn("PASSWORD");

        when(hearingsService.getPartiesNotifiedResponses(anyString(), anyString()))
            .thenReturn(PartiesNotifiedResponses.builder().build());
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
                              .hearingStartDateTime(LocalDateTime.of(2023, 1, 1, 0, 0))
                              .hearingEndDateTime(LocalDateTime.of(2023, 1, 1, 12, 0))
                              .build()))
            .requestVersion(10L)
            .responseDateTime(LocalDateTime.of(2022, 10, 10, 15, 15))
            .build();
    }

    private CaseData sampleCaseData() {
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setCcdCaseReference(12345L);
        BusinessProcess bp = new BusinessProcess();
        bp.setProcessInstanceId("proc-123");
        caseData.setBusinessProcess(bp);
        return caseData;
    }

    @Test
    void shouldSuccessfullyUpdatePartiesNotified_whenServiceReturns200() {
        CaseData caseData = sampleCaseData();
        CallbackParams params = buildCallbackParams(caseData);

        when(hearingNoticeCamundaService.getProcessVariables(any()))
            .thenReturn(sampleCamundaVars());

        when(hearingsService.updatePartiesNotifiedResponse(anyString(), anyString(), anyInt(), any(), any(PartiesNotified.class)))
            .thenReturn(new ResponseEntity<>(HttpStatus.OK));

        var response = handler.handle(params);

        assertNotNull(response);
        verify(hearingsService)
            .updatePartiesNotifiedResponse(eq("BEARER_TOKEN"), eq("HER1234"), eq(10), any(), any());
    }

    @Test
    void shouldLogWarning_whenServiceReturnsNon2xx() {
        CaseData caseData = sampleCaseData();
        CallbackParams params = buildCallbackParams(caseData);

        when(hearingNoticeCamundaService.getProcessVariables(any()))
            .thenReturn(sampleCamundaVars());

        when(hearingsService.updatePartiesNotifiedResponse(anyString(), anyString(), anyInt(), any(), any()))
            .thenReturn(new ResponseEntity<>("Error", HttpStatus.BAD_REQUEST));

        var response = handler.handle(params);

        assertNotNull(response);
        verify(hearingsService)
            .updatePartiesNotifiedResponse(eq("BEARER_TOKEN"), eq("HER1234"), eq(10), any(), any());
    }

    @Test
    void shouldHandleNullResponseGracefully() {
        CaseData caseData = sampleCaseData();
        CallbackParams params = buildCallbackParams(caseData);

        when(hearingNoticeCamundaService.getProcessVariables(any()))
            .thenReturn(sampleCamundaVars());

        when(hearingsService.updatePartiesNotifiedResponse(anyString(), anyString(), anyInt(), any(), any()))
            .thenReturn(null);

        var response = handler.handle(params);

        assertNotNull(response);
        verify(hearingsService)
            .updatePartiesNotifiedResponse(any(), any(), anyInt(), any(), any());
    }

    @Test
    void shouldCatchRestClientException() {
        CaseData caseData = sampleCaseData();
        CallbackParams params = buildCallbackParams(caseData);

        when(hearingNoticeCamundaService.getProcessVariables(any()))
            .thenReturn(sampleCamundaVars());

        when(hearingsService.updatePartiesNotifiedResponse(anyString(), anyString(), anyInt(), any(), any()))
            .thenThrow(new RestClientException("Boom"));

        var response = handler.handle(params);

        assertNotNull(response);
        verify(hearingsService)
            .updatePartiesNotifiedResponse(any(), any(), anyInt(), any(), any());
    }

    @Test
    void shouldNotCallUpdate_whenPartiesAlreadyNotifiedForSameRequestVersion() {
        CaseData caseData = sampleCaseData();
        CallbackParams params = buildCallbackParams(caseData);

        when(hearingNoticeCamundaService.getProcessVariables(any()))
            .thenReturn(sampleCamundaVars());

        when(hearingsService.getPartiesNotifiedResponses(anyString(), anyString()))
            .thenReturn(PartiesNotifiedResponses.builder()
                            .responses(List.of(
                                PartiesNotifiedResponse.builder()
                                    .requestVersion(10)
                                    .responseReceivedDateTime(LocalDateTime.now())
                                    .build()
                            ))
                            .build());

        var response = handler.handle(params);

        assertNotNull(response);
        verify(hearingsService, never())
            .updatePartiesNotifiedResponse(any(), any(), anyInt(), any(), any());
    }

    @Test
    void shouldCallUpdate_whenExistingResponseForDifferentRequestVersion() {
        CaseData caseData = sampleCaseData();
        CallbackParams params = buildCallbackParams(caseData);

        when(hearingNoticeCamundaService.getProcessVariables(any()))
            .thenReturn(sampleCamundaVars());

        when(hearingsService.getPartiesNotifiedResponses(anyString(), anyString()))
            .thenReturn(PartiesNotifiedResponses.builder()
                            .responses(List.of(
                                PartiesNotifiedResponse.builder()
                                    .requestVersion(99)
                                    .responseReceivedDateTime(LocalDateTime.now())
                                    .build()
                            ))
                            .build());

        when(hearingsService.updatePartiesNotifiedResponse(any(), any(), anyInt(), any(), any()))
            .thenReturn(new ResponseEntity<>(HttpStatus.OK));

        var response = handler.handle(params);

        assertNotNull(response);
        verify(hearingsService)
            .updatePartiesNotifiedResponse(eq("BEARER_TOKEN"), eq("HER1234"), eq(10), any(), any());
    }

    @Test
    void shouldHandleException_whenFetchingExistingPartiesNotifiedResponses() {
        CaseData caseData = sampleCaseData();
        CallbackParams params = buildCallbackParams(caseData);

        when(hearingNoticeCamundaService.getProcessVariables(any()))
            .thenReturn(sampleCamundaVars());

        when(hearingsService.getPartiesNotifiedResponses(anyString(), anyString()))
            .thenThrow(new RuntimeException("HMC down"));

        when(hearingsService.updatePartiesNotifiedResponse(any(), any(), anyInt(), any(), any()))
            .thenReturn(new ResponseEntity<>(HttpStatus.OK));

        var response = handler.handle(params);

        assertNotNull(response);
        verify(hearingsService)
            .updatePartiesNotifiedResponse(any(), any(), anyInt(), any(), any());
    }
}
