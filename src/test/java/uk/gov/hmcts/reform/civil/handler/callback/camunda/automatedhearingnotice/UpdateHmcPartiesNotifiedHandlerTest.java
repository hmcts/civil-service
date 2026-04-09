package uk.gov.hmcts.reform.civil.handler.callback.camunda.automatedhearingnotice;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.config.SystemUpdateUserConfiguration;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.civil.service.hearingnotice.HearingNoticeCamundaService;
import uk.gov.hmcts.reform.civil.service.hearingnotice.HearingNoticeVariables;
import uk.gov.hmcts.reform.hmc.model.unnotifiedhearings.PartiesNotifiedResponse;
import uk.gov.hmcts.reform.hmc.model.unnotifiedhearings.PartiesNotifiedResponses;
import uk.gov.hmcts.reform.hmc.service.HearingsService;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;

@ExtendWith(MockitoExtension.class)
class UpdateHmcPartiesNotifiedHandlerTest {

    private UpdateHmcPartiesNotifiedHandler handler;

    @Mock
    private HearingNoticeCamundaService camundaService;

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
            camundaService,
            hearingsService,
            userService,
            userConfig
        );
    }

    private CaseData sampleCaseData() {
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setCcdCaseReference(12345L);
        BusinessProcess bp = new BusinessProcess();
        bp.setProcessInstanceId("proc-123");
        caseData.setBusinessProcess(bp);
        return caseData;
    }

    private CallbackParams buildParams(CaseData caseData) {
        return new CallbackParams()
            .type(ABOUT_TO_SUBMIT)
            .caseData(caseData)
            .params(Map.of(CallbackParams.Params.BEARER_TOKEN, "BEARER_TOKEN"));
    }

    private HearingNoticeVariables sampleCamundaVars() {
        return new HearingNoticeVariables()
            .setHearingId("H123")
            .setHearingLocationEpims("LOC123")
            .setDays(List.of())
            .setRequestVersion(10L)
            .setResponseDateTime(LocalDateTime.now());
    }

    @Test
    void shouldCallUpdate_ifNotPreviouslyNotified() {
        CaseData caseData = sampleCaseData();
        CallbackParams params = buildParams(caseData);

        when(camundaService.getProcessVariables(any())).thenReturn(sampleCamundaVars());

        handler.handle(params);

        verify(hearingsService).updatePartiesNotifiedResponse(
            anyString(), eq("H123"), eq(10), any(), any()
        );

        verifyNoMoreInteractions(userService, userConfig);
    }

    @Test
    void shouldSwallowException_ifAlreadyNotifiedAfterFailure() {
        CaseData caseData = sampleCaseData();

        when(camundaService.getProcessVariables(any())).thenReturn(sampleCamundaVars());

        doThrow(new RuntimeException("Boom"))
            .when(hearingsService).updatePartiesNotifiedResponse(anyString(), anyString(), anyInt(), any(), any());

        when(hearingsService.getPartiesNotifiedResponses(anyString(), anyString()))
            .thenReturn(new PartiesNotifiedResponses()
                            .setResponses(List.of(
                                new PartiesNotifiedResponse()
                                    .setRequestVersion(10)
                                    .setResponseReceivedDateTime(LocalDateTime.now()))));

        when(userService.getAccessToken(anyString(), anyString())).thenReturn(AUTH_TOKEN);
        when(userService.getUserInfo(anyString()))
            .thenReturn(UserInfo.builder().uid("test-id").build());
        when(userConfig.getUserName()).thenReturn("USER");
        when(userConfig.getPassword()).thenReturn("PASS");

        CallbackParams params = buildParams(caseData);
        handler.handle(params);
    }

    @Test
    void shouldThrowException_ifNotNotifiedAndUpdateFails() {
        CaseData caseData = sampleCaseData();
        CallbackParams params = buildParams(caseData);

        when(camundaService.getProcessVariables(any())).thenReturn(sampleCamundaVars());

        doThrow(new RuntimeException("Boom"))
            .when(hearingsService).updatePartiesNotifiedResponse(anyString(), anyString(), anyInt(), any(), any());

        when(hearingsService.getPartiesNotifiedResponses(anyString(), anyString()))
            .thenReturn(new PartiesNotifiedResponses());

        when(userService.getAccessToken(anyString(), anyString())).thenReturn(AUTH_TOKEN);
        when(userService.getUserInfo(anyString()))
            .thenReturn(UserInfo.builder().uid("test-id").build());
        when(userConfig.getUserName()).thenReturn("USER");
        when(userConfig.getPassword()).thenReturn("PASS");

        assertThatThrownBy(() -> handler.handle(params))
            .hasMessageContaining("Boom");
    }
}
