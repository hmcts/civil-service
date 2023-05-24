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
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.civil.config.SystemUpdateUserConfiguration;
import uk.gov.hmcts.reform.civil.handler.tasks.variables.HearingNoticeMessageVars;
import uk.gov.hmcts.reform.civil.handler.tasks.variables.HearingNoticeSchedulerVars;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.hmc.model.hearing.CaseDetailsHearing;
import uk.gov.hmcts.reform.hmc.model.hearing.HearingDaySchedule;
import uk.gov.hmcts.reform.hmc.model.hearing.HearingDetails;
import uk.gov.hmcts.reform.hmc.model.hearing.HearingGetResponse;
import uk.gov.hmcts.reform.hmc.model.hearing.HearingRequestDetails;
import uk.gov.hmcts.reform.hmc.model.hearing.HearingResponse;
import uk.gov.hmcts.reform.hmc.model.hearing.ListAssistCaseStatus;
import uk.gov.hmcts.reform.hmc.model.hearing.PartyDetailsModel;
import uk.gov.hmcts.reform.hmc.model.unnotifiedhearings.PartiesNotified;
import uk.gov.hmcts.reform.hmc.model.unnotifiedhearings.PartiesNotifiedResponse;
import uk.gov.hmcts.reform.hmc.model.unnotifiedhearings.PartiesNotifiedResponses;
import uk.gov.hmcts.reform.hmc.model.unnotifiedhearings.PartiesNotifiedServiceData;
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

    @InjectMocks
    private AutomatedHearingNoticeHandler handler;

    static final String SERVICE_ID_KEY = "serviceId";
    static final String HEARING_ID = "hearing-id-1";
    static final String CASE_ID = "1111111111111111";
    static final String SERVICE_ID = "AAA7";
    static final String VENUE_ID = "00000";
    static final Integer VERSION = 1;
    static final String PROCESS_INSTANCE_ID = "process-instance-id";
    static final String AUTH_TOKEN = "mock_token";
    static final LocalDateTime HEARING_DATE = LocalDateTime.of(2030, 1, 1, 12, 0, 0);
    static final LocalDateTime RECEIVED_DATETIME = LocalDateTime.of(2029, 12, 1, 12, 0, 0);
    static final String MESSAGE_ID = "NOTIFY_HEARING_PARTIES";

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
    void shouldDispatchExpectedCamundaMessage_whenHearingIsInListedStatusAndPartiesNotifiedResponsesIsEmpty() {
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
        when(hearingsService.getUnNotifiedHearingResponses(AUTH_TOKEN, SERVICE_ID, null,
                                                           null)).thenReturn(
            createUnnotifiedHearings(List.of(HEARING_ID)));
        when(hearingsService.getHearingResponse(AUTH_TOKEN, HEARING_ID)).thenReturn(
            createHearing(CASE_ID, ListAssistCaseStatus.LISTED));
        when(hearingsService.getPartiesNotifiedResponses(AUTH_TOKEN, HEARING_ID)).thenReturn(
            PartiesNotifiedResponses.builder().responses(List.of()).build());

        handler.execute(mockTask, externalTaskService);

        verify(runtimeService).setVariables(
            PROCESS_INSTANCE_ID,
            HearingNoticeSchedulerVars.builder()
                .totalNumberOfUnnotifiedHearings(1)
                .dispatchedHearingIds(List.of(HEARING_ID))
                .build().toMap(mapper)
        );
        verify(runtimeService, times(1)).createMessageCorrelation(MESSAGE_ID);
        verify(messageCorrelationBuilder, times(1)).setVariables(
            HearingNoticeMessageVars.builder()
                .caseId(CASE_ID)
                .hearingId(HEARING_ID)
                .triggeredViaScheduler(true)
                .build().toMap(mapper));
        verify(messageCorrelationBuilder, times(1)).correlateStartMessage();
        verify(externalTaskService).complete(mockTask);
    }

    @Test
    void shouldDispatchExpectedCamundaMessage_whenHearingIsInListedStatusAndPartiesNotifiedResponsesAreNull() {
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
            AUTH_TOKEN, SERVICE_ID, null, null)).thenReturn(
            createUnnotifiedHearings(List.of(HEARING_ID)));
        when(hearingsService.getHearingResponse(AUTH_TOKEN, HEARING_ID)).thenReturn(
            createHearing(CASE_ID, ListAssistCaseStatus.LISTED));
        when(hearingsService.getPartiesNotifiedResponses(AUTH_TOKEN, HEARING_ID)).thenReturn(
            PartiesNotifiedResponses.builder().build());

        handler.execute(mockTask, externalTaskService);

        verify(runtimeService).setVariables(PROCESS_INSTANCE_ID, HearingNoticeSchedulerVars.builder()
            .totalNumberOfUnnotifiedHearings(1)
            .dispatchedHearingIds(List.of(HEARING_ID))
            .build().toMap(mapper));
        verify(runtimeService, times(1)).createMessageCorrelation(MESSAGE_ID);
        verify(messageCorrelationBuilder, times(1)).setVariables(
            HearingNoticeMessageVars.builder()
                .caseId(CASE_ID)
                .hearingId(HEARING_ID)
                .triggeredViaScheduler(true)
                .build().toMap(mapper));
        verify(messageCorrelationBuilder, times(1)).correlateStartMessage();
        verify(externalTaskService).complete(mockTask);
    }

    @Test
    void shouldDispatchExpectedCamundaMessages_whenMultipleUnnotifedHearingsExist() {
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
            AUTH_TOKEN, SERVICE_ID, null, null)).thenReturn(
            createUnnotifiedHearings(List.of(HEARING_ID, "hearing-id-2", "hearing-id-3")));
        when(hearingsService.getHearingResponse(anyString(), anyString())).thenReturn(
            createHearing(CASE_ID, ListAssistCaseStatus.LISTED));
        when(hearingsService.getPartiesNotifiedResponses(anyString(), anyString())).thenReturn(
            PartiesNotifiedResponses.builder().build());

        handler.execute(mockTask, externalTaskService);

        verify(runtimeService).setVariables(PROCESS_INSTANCE_ID, HearingNoticeSchedulerVars.builder()
            .totalNumberOfUnnotifiedHearings(3)
            .dispatchedHearingIds(List.of(HEARING_ID))
            .build().toMap(mapper));
        verify(runtimeService, times(3)).createMessageCorrelation(MESSAGE_ID);
        verify(messageCorrelationBuilder, times(3)).setVariables(
            HearingNoticeMessageVars.builder()
                .caseId(CASE_ID)
                .hearingId(HEARING_ID)
                .triggeredViaScheduler(true)
                .build().toMap(mapper));
        verify(messageCorrelationBuilder, times(3)).correlateStartMessage();
        verify(externalTaskService).complete(mockTask);
    }

    @Test
    void shouldNotDispatchCamundaMessage_whenHearingIsNotInListedStatus() {
        when(mapper.convertValue(any(), eq(HearingNoticeSchedulerVars.class))).thenReturn(
            HearingNoticeSchedulerVars.builder()
                .serviceId(SERVICE_ID)
                .dispatchedHearingIds(List.of())
                .build());
        when(hearingsService.getUnNotifiedHearingResponses(
            AUTH_TOKEN, SERVICE_ID, null, null)).thenReturn(
            createUnnotifiedHearings(List.of(HEARING_ID)));
        when(hearingsService.getHearingResponse(AUTH_TOKEN, HEARING_ID)).thenReturn(
            createHearing(CASE_ID, ListAssistCaseStatus.CASE_CLOSED));
        when(hearingsService.getPartiesNotifiedResponses(AUTH_TOKEN, HEARING_ID)).thenReturn(
            PartiesNotifiedResponses.builder().responses(List.of()).build());

        handler.execute(mockTask, externalTaskService);

        verify(hearingsService, times(1))
            .updatePartiesNotifiedResponse(
                AUTH_TOKEN,
                HEARING_ID,
                VERSION,
                RECEIVED_DATETIME,
                PartiesNotified.builder()
                    .serviceData(PartiesNotifiedServiceData.builder().hearingNoticeGenerated(false).build())
                    .build()
            );
        verify(runtimeService).setVariables(PROCESS_INSTANCE_ID, HearingNoticeSchedulerVars.builder()
            .totalNumberOfUnnotifiedHearings(1)
            .dispatchedHearingIds(List.of())
            .build().toMap(mapper));
        verify(runtimeService, times(0)).createMessageCorrelation(MESSAGE_ID);
        verifyNoInteractions(messageCorrelationBuilder);
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
            AUTH_TOKEN, SERVICE_ID, null, null)).thenReturn(
            createUnnotifiedHearings(List.of(HEARING_ID)));
        when(hearingsService.getHearingResponse(AUTH_TOKEN, HEARING_ID)).thenReturn(
            createHearing(CASE_ID, ListAssistCaseStatus.LISTED));
        when(hearingsService.getPartiesNotifiedResponses(AUTH_TOKEN, HEARING_ID)).thenReturn(
            PartiesNotifiedResponses.builder().responses(List.of()).build());

        handler.execute(mockTask, externalTaskService);

        verify(runtimeService).setVariables(PROCESS_INSTANCE_ID, HearingNoticeSchedulerVars.builder()
            .totalNumberOfUnnotifiedHearings(1)
            .dispatchedHearingIds(List.of(HEARING_ID))
            .build().toMap(mapper));
        verify(runtimeService, times(0)).createMessageCorrelation(MESSAGE_ID);
        verifyNoInteractions(messageCorrelationBuilder);
        verify(externalTaskService).complete(mockTask);
    }

    @Test
    void shouldDispatchCamundaMessage_whenHearingVenueDoesNotMatchLatestPartiesNotifiedResponseHearingVenue() {
        when(mapper.convertValue(any(), eq(HearingNoticeSchedulerVars.class))).thenReturn(
            HearingNoticeSchedulerVars.builder()
                .serviceId(SERVICE_ID)
                .dispatchedHearingIds(new ArrayList<>())
                .build());
        when(mapper.convertValue(any(), eq(HearingNoticeMessageVars.class))).thenReturn(
            HearingNoticeMessageVars.builder()
                .caseId(CASE_ID)
                .hearingId(HEARING_ID)
                .triggeredViaScheduler(true)
                .build());
        when(hearingsService.getUnNotifiedHearingResponses(
            AUTH_TOKEN, SERVICE_ID, null, null)).thenReturn(
            createUnnotifiedHearings(List.of(HEARING_ID)));
        when(hearingsService.getHearingResponse(AUTH_TOKEN, HEARING_ID)).thenReturn(
            createHearing(CASE_ID, ListAssistCaseStatus.LISTED));
        when(hearingsService.getPartiesNotifiedResponses(AUTH_TOKEN, HEARING_ID)).thenReturn(
            PartiesNotifiedResponses.builder().responses(List.of(
                PartiesNotifiedResponse.builder()
                    .serviceData(PartiesNotifiedServiceData
                                     .builder()
                                     .hearingDate(HEARING_DATE)
                                     .hearingLocation("111111")
                                     .build())
                    .build()
            )).build());

        handler.execute(mockTask, externalTaskService);

        verify(runtimeService).setVariables(PROCESS_INSTANCE_ID, HearingNoticeSchedulerVars.builder()
            .totalNumberOfUnnotifiedHearings(1)
            .dispatchedHearingIds(List.of(HEARING_ID))
            .build().toMap(mapper));
        verify(runtimeService, times(1)).createMessageCorrelation(MESSAGE_ID);
        verify(messageCorrelationBuilder, times(1)).setVariables(
            HearingNoticeMessageVars.builder()
                .caseId(CASE_ID)
                .hearingId(HEARING_ID)
                .triggeredViaScheduler(true)
                .build().toMap(mapper));
        verify(messageCorrelationBuilder, times(1)).correlateStartMessage();
        verify(externalTaskService).complete(mockTask);
    }

    @Test
    void shouldDispatchCamundaMessage_whenHearingVenueDoesNotMatchLatestPartiesNotifiedResponseHearingDate() {
        when(mapper.convertValue(any(), eq(HearingNoticeSchedulerVars.class))).thenReturn(
            HearingNoticeSchedulerVars.builder()
                .serviceId(SERVICE_ID)
                .dispatchedHearingIds(new ArrayList<>())
                .build());
        when(mapper.convertValue(any(), eq(HearingNoticeMessageVars.class))).thenReturn(
            HearingNoticeMessageVars.builder()
                .caseId(CASE_ID)
                .hearingId(HEARING_ID)
                .triggeredViaScheduler(true)
                .build());
        when(hearingsService.getUnNotifiedHearingResponses(
            AUTH_TOKEN, SERVICE_ID, null, null)).thenReturn(
            createUnnotifiedHearings(List.of(HEARING_ID)));
        when(hearingsService.getHearingResponse(AUTH_TOKEN, HEARING_ID)).thenReturn(
            createHearing(CASE_ID, ListAssistCaseStatus.LISTED));
        when(hearingsService.getPartiesNotifiedResponses(AUTH_TOKEN, HEARING_ID)).thenReturn(
            PartiesNotifiedResponses.builder().responses(List.of(
                PartiesNotifiedResponse.builder()
                    .serviceData(PartiesNotifiedServiceData
                                     .builder()
                                     .hearingDate(LocalDateTime.of(2030, 6, 6, 12, 0, 0))
                                     .hearingLocation(VENUE_ID)
                                     .build())
                    .build()
            )).build());

        handler.execute(mockTask, externalTaskService);

        verify(runtimeService).setVariables(PROCESS_INSTANCE_ID, HearingNoticeSchedulerVars.builder()
            .totalNumberOfUnnotifiedHearings(1)
            .dispatchedHearingIds(List.of(HEARING_ID))
            .build().toMap(mapper));
        verify(runtimeService, times(1)).createMessageCorrelation(MESSAGE_ID);
        verify(messageCorrelationBuilder, times(1)).setVariables(
            HearingNoticeMessageVars.builder().caseId(CASE_ID).hearingId(HEARING_ID).triggeredViaScheduler(true)
                .build().toMap(mapper));
        verify(messageCorrelationBuilder, times(1)).correlateStartMessage();
        verify(externalTaskService).complete(mockTask);
    }

    @Test
    void shouldNotDispatchCamundaMessage_whenHearingDataMatchesLatestHearingResponseData() {
        when(mapper.convertValue(any(), eq(HearingNoticeSchedulerVars.class))).thenReturn(
            HearingNoticeSchedulerVars.builder()
                .serviceId(SERVICE_ID)
                .dispatchedHearingIds(List.of())
                .build());

        when(hearingsService.getUnNotifiedHearingResponses(
            AUTH_TOKEN, SERVICE_ID, null, null)).thenReturn(
            createUnnotifiedHearings(List.of(HEARING_ID)));

        when(hearingsService.getHearingResponse(AUTH_TOKEN, HEARING_ID)).thenReturn(
            createHearing(CASE_ID, ListAssistCaseStatus.LISTED));

        when(hearingsService.getPartiesNotifiedResponses(AUTH_TOKEN, HEARING_ID)).thenReturn(
            PartiesNotifiedResponses.builder().responses(List.of(
                PartiesNotifiedResponse.builder()
                    .responseReceivedDateTime(LocalDateTime.now())
                    .serviceData(PartiesNotifiedServiceData
                                     .builder()
                                     .hearingDate(HEARING_DATE)
                                     .hearingLocation(VENUE_ID)
                                     .build())
                    .build()
            )).build());

        handler.execute(mockTask, externalTaskService);

        verify(hearingsService, times(1)).updatePartiesNotifiedResponse(
            AUTH_TOKEN,
            HEARING_ID,
            VERSION,
            RECEIVED_DATETIME,
            PartiesNotified.builder().serviceData(PartiesNotifiedServiceData.builder()
                                                      .hearingLocation(VENUE_ID)
                                                      .hearingDate(HEARING_DATE)
                                                      .hearingNoticeGenerated(false).build()).build()
        );
        verify(runtimeService).setVariables(PROCESS_INSTANCE_ID, HearingNoticeSchedulerVars.builder()
            .totalNumberOfUnnotifiedHearings(1)
            .dispatchedHearingIds(List.of()).build()
            .toMap(mapper));
        verify(runtimeService, times(0)).createMessageCorrelation(MESSAGE_ID);
        verifyNoInteractions(messageCorrelationBuilder);
        verify(externalTaskService).complete(mockTask);
    }

    @Test
    void shouldCallRuntimeServiceComplete_whenCompleteTaskIsTriggered() {
        handler.completeTask(mockTask, externalTaskService);

        verify(externalTaskService).complete(mockTask);
    }

    private UnNotifiedHearingResponse createUnnotifiedHearings(List<String> hearingIds) {
        return UnNotifiedHearingResponse.builder()
            .hearingIds(hearingIds)
            .totalFound(Long.parseLong(String.valueOf(hearingIds.size())))
            .build();
    }

    private HearingGetResponse createHearing(String caseId, ListAssistCaseStatus hearingStatus) {
        return HearingGetResponse.builder()
            .hearingDetails(HearingDetails.builder().build())
            .requestDetails(HearingRequestDetails.builder()
                                .versionNumber(VERSION.longValue())
                                .build())
            .caseDetails(CaseDetailsHearing.builder().caseRef(caseId).build())
            .partyDetails(List.of(PartyDetailsModel.builder().build()))
            .hearingResponse(
                HearingResponse.builder()
                    .receivedDateTime(RECEIVED_DATETIME)
                    .listAssistCaseStatus(hearingStatus)
                    .hearingDaySchedule(List.of(
                        HearingDaySchedule.builder()
                            .hearingStartDateTime(HEARING_DATE)
                            .hearingVenueId(VENUE_ID).build()
                    ))
                    .build())
            .build();
    }
}
