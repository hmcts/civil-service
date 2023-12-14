package uk.gov.hmcts.reform.civil.handler.tasks;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.runtime.MessageCorrelationBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.civil.config.SystemUpdateUserConfiguration;
import uk.gov.hmcts.reform.civil.event.HearingNoticeSchedulerTaskEvent;
import uk.gov.hmcts.reform.civil.handler.tasks.variables.HearingNoticeMessageVars;
import uk.gov.hmcts.reform.civil.handler.tasks.variables.HearingNoticeSchedulerVars;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.hmc.model.unnotifiedhearings.UnNotifiedHearingResponse;
import uk.gov.hmcts.reform.hmc.service.HearingsService;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class AutomatedHearingNoticeHandlerTest {

    @Mock
    private ExternalTask mockTask;

    @Mock
    private ExternalTaskService externalTaskService;

    @Mock
    private UserService userService;

    @Mock
    private RuntimeService runtimeService;

    @Mock
    private HearingsService hearingsService;

    @Mock
    private MessageCorrelationBuilder messageCorrelationBuilder;

    @Mock
    private ObjectMapper mapper;

    @Mock
    private SystemUpdateUserConfiguration userConfig;

    @Mock
    private FeatureToggleService featureToggleService;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @InjectMocks
    private AutomatedHearingNoticeHandler handler;

    static final String SERVICE_ID_KEY = "serviceId";
    static final String HEARING_ID = "hearing-id-1";
    static final String CASE_ID = "1111111111111111";
    static final String SERVICE_ID = "AAA7";
    static final String PROCESS_INSTANCE_ID = "process-instance-id";
    static final String AUTH_TOKEN = "mock_token";

    @BeforeEach
    void init() {
        when(featureToggleService.isAutomatedHearingNoticeEnabled()).thenReturn(true);
        when(runtimeService.createMessageCorrelation(any())).thenReturn(messageCorrelationBuilder);
        when(messageCorrelationBuilder.setVariables(any())).thenReturn(messageCorrelationBuilder);
        when(mockTask.getTopicName()).thenReturn("test");
        when(mockTask.getWorkerId()).thenReturn("worker");
        when(mockTask.getVariable(SERVICE_ID_KEY)).thenReturn(SERVICE_ID);
        when(mockTask.getProcessInstanceId()).thenReturn(PROCESS_INSTANCE_ID);
        when(userService.getAccessToken(anyString(), anyString())).thenReturn(AUTH_TOKEN);
        when(userService.getUserInfo(anyString())).thenReturn(UserInfo.builder().uid("test-id").build());
        when(userConfig.getUserName()).thenReturn("");
        when(userConfig.getPassword()).thenReturn("");
        when(runtimeService.getVariable(PROCESS_INSTANCE_ID, SERVICE_ID_KEY)).thenReturn(new ArrayList<>());
    }

    @Test
    void shouldNotCallUnnotifiedHearings_whenAHNFeatureToggleIsOff() {
        when(featureToggleService.isAutomatedHearingNoticeEnabled()).thenReturn(false);

        handler.execute(mockTask, externalTaskService);

        verifyNoInteractions(hearingsService);
        verify(externalTaskService).complete(mockTask);
    }

    @Test
    void shouldNotDispatchHearingNoticeEvent_whenThereAreNoUnnotifiedHearings() {
        final ArrayList<String> dispatchedHearingIds = new ArrayList<>();

        when(mapper.convertValue(any(), eq(HearingNoticeSchedulerVars.class))).thenReturn(
            HearingNoticeSchedulerVars.builder()
                .serviceId(SERVICE_ID)
                .dispatchedHearingIds(dispatchedHearingIds)
                .build());
        when(mapper.convertValue(any(), eq(HearingNoticeMessageVars.class))).thenReturn(
            HearingNoticeMessageVars.builder()
                .caseId(CASE_ID)
                .hearingId(HEARING_ID)
                .triggeredViaScheduler(true)
                .build());
        when(hearingsService.getUnNotifiedHearingResponses(
            anyString(), anyString(), any(LocalDateTime.class), any())).thenReturn(
            createUnnotifiedHearings(List.of()));

        handler.execute(mockTask, externalTaskService);

        verify(applicationEventPublisher, times(0)).publishEvent(any());
        verify(runtimeService).setVariables(
            PROCESS_INSTANCE_ID,
            HearingNoticeSchedulerVars.builder()
                .totalNumberOfUnnotifiedHearings(0)
                .dispatchedHearingIds(List.of())
                .build().toMap(mapper)
        );
        verify(externalTaskService).complete(mockTask);
    }

    @Test
    void shouldDispatchHearingNoticeEvent_whenThereisOneUnnotifiedHearing() {
        final ArrayList<String> dispatchedHearingIds = new ArrayList<>();

        when(mapper.convertValue(any(), eq(HearingNoticeSchedulerVars.class))).thenReturn(
            HearingNoticeSchedulerVars.builder()
                .serviceId(SERVICE_ID)
                .dispatchedHearingIds(dispatchedHearingIds)
                .build());
        when(mapper.convertValue(any(), eq(HearingNoticeMessageVars.class))).thenReturn(
            HearingNoticeMessageVars.builder()
                .caseId(CASE_ID)
                .hearingId(HEARING_ID)
                .triggeredViaScheduler(true)
                .build());
        when(hearingsService.getUnNotifiedHearingResponses(
            anyString(), anyString(), any(LocalDateTime.class), any())).thenReturn(
            createUnnotifiedHearings(List.of(HEARING_ID)));

        handler.execute(mockTask, externalTaskService);

        verify(applicationEventPublisher, times(dispatchedHearingIds.size())).publishEvent(
            new HearingNoticeSchedulerTaskEvent(HEARING_ID));
        verify(runtimeService).setVariables(
            PROCESS_INSTANCE_ID,
            HearingNoticeSchedulerVars.builder()
                .totalNumberOfUnnotifiedHearings(1)
                .dispatchedHearingIds(List.of(HEARING_ID))
                .build().toMap(mapper)
        );

        verify(externalTaskService).complete(mockTask);
    }

    @Test
    void shouldDispatchHearingNoticeEventMultipleTimes_whenThereAreMultipleUnnotifiedHearing() {
        final ArrayList<String> dispatchedHearingIds = new ArrayList<>();

        List<String> hearingIds = List.of("hearing-id-1", "hearing-id-2", "hearing-id-3");

        when(mapper.convertValue(any(), eq(HearingNoticeSchedulerVars.class))).thenReturn(
            HearingNoticeSchedulerVars.builder()
                .serviceId(SERVICE_ID)
                .dispatchedHearingIds(dispatchedHearingIds)
                .build());
        when(mapper.convertValue(any(), eq(HearingNoticeMessageVars.class))).thenReturn(
            HearingNoticeMessageVars.builder()
                .caseId(CASE_ID)
                .hearingId(HEARING_ID)
                .triggeredViaScheduler(true)
                .build());
        when(hearingsService.getUnNotifiedHearingResponses(
            anyString(), anyString(), any(LocalDateTime.class), any())).thenReturn(
            createUnnotifiedHearings(hearingIds));

        handler.execute(mockTask, externalTaskService);

        verify(applicationEventPublisher, times(1)).publishEvent(
            new HearingNoticeSchedulerTaskEvent(hearingIds.get(0)));
        verify(applicationEventPublisher, times(1)).publishEvent(
            new HearingNoticeSchedulerTaskEvent(hearingIds.get(1)));
        verify(applicationEventPublisher, times(1)).publishEvent(
            new HearingNoticeSchedulerTaskEvent(hearingIds.get(2)));

        verify(runtimeService).setVariables(
            PROCESS_INSTANCE_ID,
            HearingNoticeSchedulerVars.builder()
                .totalNumberOfUnnotifiedHearings(3)
                .dispatchedHearingIds(hearingIds)
                .build().toMap(mapper)
        );

        verify(externalTaskService).complete(mockTask);
    }

    @Test
    void shouldNotDispatchCamundaMessage_whenHearingIdIsInDispatchedHearingIdsCamundaVariables() {
        var dispatchedHearingIds = List.of(HEARING_ID);

        when(mapper.convertValue(any(), eq(HearingNoticeSchedulerVars.class))).thenReturn(
            HearingNoticeSchedulerVars.builder()
                .serviceId(SERVICE_ID)
                .dispatchedHearingIds(dispatchedHearingIds)
                .build());
        when(hearingsService.getUnNotifiedHearingResponses(
            anyString(), anyString(), any(LocalDateTime.class), any())).thenReturn(
            createUnnotifiedHearings(List.of(HEARING_ID)));

        handler.execute(mockTask, externalTaskService);

        verifyNoInteractions(applicationEventPublisher);
        verify(runtimeService).setVariables(PROCESS_INSTANCE_ID, HearingNoticeSchedulerVars.builder()
            .totalNumberOfUnnotifiedHearings(1)
            .dispatchedHearingIds(List.of(HEARING_ID))
            .build().toMap(mapper));
        verifyNoInteractions(messageCorrelationBuilder);
        verify(externalTaskService).complete(mockTask);
    }

    private UnNotifiedHearingResponse createUnnotifiedHearings(List<String> hearingIds) {
        return UnNotifiedHearingResponse.builder()
            .hearingIds(hearingIds)
            .totalFound(Long.parseLong(String.valueOf(hearingIds.size())))
            .build();
    }

}
