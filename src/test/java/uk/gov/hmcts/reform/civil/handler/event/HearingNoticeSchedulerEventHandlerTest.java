package uk.gov.hmcts.reform.civil.handler.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.runtime.MessageCorrelationBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.config.SystemUpdateUserConfiguration;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.event.HearingNoticeSchedulerTaskEvent;
import uk.gov.hmcts.reform.civil.handler.tasks.variables.HearingNoticeMessageVars;
import uk.gov.hmcts.reform.civil.handler.tasks.variables.HearingNoticeSchedulerVars;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.hmc.exception.HmcException;
import uk.gov.hmcts.reform.hmc.model.hearing.CaseDetailsHearing;
import uk.gov.hmcts.reform.hmc.model.hearing.HearingDaySchedule;
import uk.gov.hmcts.reform.hmc.model.hearing.HearingDetails;
import uk.gov.hmcts.reform.hmc.model.hearing.HearingGetResponse;
import uk.gov.hmcts.reform.hmc.model.hearing.HearingRequestDetails;
import uk.gov.hmcts.reform.hmc.model.hearing.HearingResponse;
import uk.gov.hmcts.reform.hmc.model.hearing.PartyDetailsModel;
import uk.gov.hmcts.reform.hmc.model.unnotifiedhearings.HearingDay;
import uk.gov.hmcts.reform.hmc.model.unnotifiedhearings.PartiesNotified;
import uk.gov.hmcts.reform.hmc.model.unnotifiedhearings.PartiesNotifiedResponse;
import uk.gov.hmcts.reform.hmc.model.unnotifiedhearings.PartiesNotifiedResponses;
import uk.gov.hmcts.reform.hmc.model.unnotifiedhearings.PartiesNotifiedServiceData;
import uk.gov.hmcts.reform.hmc.service.HearingsService;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;
import uk.gov.hmcts.reform.hmc.model.hearing.ListAssistCaseStatus;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@ExtendWith(SpringExtension.class)
class HearingNoticeSchedulerEventHandlerTest {

    @Mock
    private ExternalTask mockTask;

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
    private CoreCaseDataService coreCaseDataService;

    @InjectMocks
    private HearingNoticeSchedulerEventHandler handler;

    static final String VENUE_ID = "00000";
    static final Integer VERSION = 1;
    static final String SERVICE_ID_KEY = "serviceId";
    static final String HEARING_ID = "hearing-id-1";
    static final String CASE_ID = "1111111111111111";
    static final String SERVICE_ID = "AAA7";
    static final String AUTH_TOKEN = "mock_token";
    static final LocalDateTime HEARING_DATE = LocalDateTime.of(2030, 1, 1, 12, 0, 0);
    static final LocalDateTime RECEIVED_DATETIME = LocalDateTime.of(2029, 12, 1, 12, 0, 0);
    static final String MESSAGE_ID = "NOTIFY_HEARING_PARTIES";

    @BeforeEach
    void init() {
        when(runtimeService.createMessageCorrelation(any())).thenReturn(messageCorrelationBuilder);
        when(messageCorrelationBuilder.setVariables(any())).thenReturn(messageCorrelationBuilder);
        when(mockTask.getVariable(SERVICE_ID_KEY)).thenReturn(SERVICE_ID);
        when(userService.getAccessToken(anyString(), anyString())).thenReturn(AUTH_TOKEN);
        when(userService.getUserInfo(anyString())).thenReturn(UserInfo.builder().uid("test-id").build());
        when(userConfig.getUserName()).thenReturn("");
        when(userConfig.getPassword()).thenReturn("");
        when(coreCaseDataService.getCase(anyLong())).thenReturn(mock(CaseDetails.class));
    }

    @Test
    void isNotAllowedState_shouldReturnFalse_whenCaseStateIsAllowed() {
        for (CaseState allowedState : new CaseState[]{
            CaseState.CASE_SETTLED,
            CaseState.PROCEEDS_IN_HERITAGE_SYSTEM,
            CaseState.CASE_STAYED,
            CaseState.CASE_DISCONTINUED,
            CaseState.CASE_DISMISSED,
            CaseState.CLOSED,
            CaseState.All_FINAL_ORDERS_ISSUED
        }) {
            boolean result = invokeIsNotAllowedState(allowedState.name());
            assertTrue(result, "Allowed state should return false: " + allowedState);
        }
    }

    @Test
    void isNotAllowedState_shouldReturnTrue_whenCaseStateIsInvalid() {
        assertTrue(invokeIsNotAllowedState("INVALID_STATE"), "Invalid state should return true");
    }

    @Test
    void isNotAllowedState_shouldReturnTrue_whenCaseStateIsNotInAllowedList() {
        assertTrue(invokeIsNotAllowedState(CaseState.CASE_DISMISSED.name()), "State not in allowed list should return true");
    }

    // Helper to call the private static method using reflection
    private boolean invokeIsNotAllowedState(String state) {
        try {
            var method = HearingNoticeSchedulerEventHandler.class.getDeclaredMethod(
                "isNotAllowedState", String.class, String.class
            );
            method.setAccessible(true);
            return (boolean) method.invoke(null, state, "test-case-ref");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void shouldDispatchExpectedCamundaMessage_whenHearingIsInListedStatusAndPartiesNotifiedResponsesIsEmpty() {
        when(mapper.convertValue(any(), eq(HearingNoticeMessageVars.class))).thenReturn(
            HearingNoticeMessageVars.builder()
                .caseId(CASE_ID)
                .hearingId(HEARING_ID)
                .triggeredViaScheduler(true)
                .build());
        when(hearingsService.getHearingResponse(anyString(), anyString())).thenReturn(
            createHearing(ListAssistCaseStatus.LISTED));
        when(hearingsService.getPartiesNotifiedResponses(anyString(), anyString())).thenReturn(
            PartiesNotifiedResponses.builder().build());

        CaseDetails mockCaseDetails = CaseDetails.builder()
            .id(Long.parseLong(CASE_ID))
            .state("CASE_PROGRESSION")
            .build();

        when(coreCaseDataService.getCase(Long.parseLong(CASE_ID))).thenReturn(mockCaseDetails);

        handler.handle(new HearingNoticeSchedulerTaskEvent(HEARING_ID));

        verify(runtimeService, times(1)).createMessageCorrelation(MESSAGE_ID);
        verify(messageCorrelationBuilder, times(1)).setVariables(
            HearingNoticeMessageVars.builder()
                .caseId(CASE_ID)
                .hearingId(HEARING_ID)
                .triggeredViaScheduler(true)
                .build().toMap(mapper));
        verify(messageCorrelationBuilder, times(1)).correlateStartMessage();
    }

    @Test
    void shouldNotDispatchCamundaMessage_whenHearingIsNotInListedStatus() {
        when(mapper.convertValue(any(), eq(HearingNoticeSchedulerVars.class))).thenReturn(
            HearingNoticeSchedulerVars.builder()
                .serviceId(SERVICE_ID)
                .dispatchedHearingIds(List.of())
                .build());
        when(hearingsService.getHearingResponse(AUTH_TOKEN, HEARING_ID)).thenReturn(
            createHearing(ListAssistCaseStatus.CASE_CLOSED));
        when(hearingsService.getPartiesNotifiedResponses(AUTH_TOKEN, HEARING_ID)).thenReturn(
            PartiesNotifiedResponses.builder().responses(List.of()).build());

        handler.handle(new HearingNoticeSchedulerTaskEvent(HEARING_ID));

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
        verify(runtimeService, times(0)).createMessageCorrelation(MESSAGE_ID);
        verifyNoInteractions(messageCorrelationBuilder);
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
        when(hearingsService.getHearingResponse(AUTH_TOKEN, HEARING_ID)).thenReturn(
            createHearing(ListAssistCaseStatus.LISTED));
        when(hearingsService.getPartiesNotifiedResponses(AUTH_TOKEN, HEARING_ID)).thenReturn(
            PartiesNotifiedResponses.builder().responses(List.of(
                PartiesNotifiedResponse.builder()
                    .serviceData(PartiesNotifiedServiceData
                                     .builder()
                                     .days(List.of(HearingDay.builder()
                                                       .hearingStartDateTime(LocalDateTime.of(2030, 6, 6, 12, 0, 0))
                                                       .build()))
                                     .hearingLocation(VENUE_ID)
                                     .build())
                    .build()
            )).build());

        CaseDetails mockCaseDetails = CaseDetails.builder()
            .id(Long.parseLong(CASE_ID))
            .state("CASE_PROGRESSION")
            .build();

        when(coreCaseDataService.getCase(Long.parseLong(CASE_ID))).thenReturn(mockCaseDetails);

        handler.handle(new HearingNoticeSchedulerTaskEvent(HEARING_ID));

        verify(runtimeService, times(1)).createMessageCorrelation(MESSAGE_ID);
        verify(messageCorrelationBuilder, times(1)).setVariables(
            HearingNoticeMessageVars.builder().caseId(CASE_ID).hearingId(HEARING_ID).triggeredViaScheduler(true)
                .build().toMap(mapper));
        verify(messageCorrelationBuilder, times(1)).correlateStartMessage();
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
        when(hearingsService.getHearingResponse(AUTH_TOKEN, HEARING_ID)).thenReturn(
            createHearing(ListAssistCaseStatus.LISTED));
        when(hearingsService.getPartiesNotifiedResponses(AUTH_TOKEN, HEARING_ID)).thenReturn(
            PartiesNotifiedResponses.builder().responses(List.of(
                PartiesNotifiedResponse.builder()
                    .serviceData(PartiesNotifiedServiceData
                                     .builder()
                                     .days(List.of(HearingDay.builder()
                                                       .hearingStartDateTime(HEARING_DATE)
                                                       .build()))
                                     .hearingLocation("111111")
                                     .build())
                    .build()
            )).build());


        CaseDetails mockCaseDetails = CaseDetails.builder()
            .id(Long.parseLong(CASE_ID))
            .state("CASE_PROGRESSION")
            .build();

        when(coreCaseDataService.getCase(Long.parseLong(CASE_ID))).thenReturn(mockCaseDetails);

        handler.handle(new HearingNoticeSchedulerTaskEvent(HEARING_ID));

        verify(runtimeService, times(1)).createMessageCorrelation(MESSAGE_ID);
        verify(messageCorrelationBuilder, times(1)).setVariables(
            HearingNoticeMessageVars.builder()
                .caseId(CASE_ID)
                .hearingId(HEARING_ID)
                .triggeredViaScheduler(true)
                .build().toMap(mapper));
        verify(messageCorrelationBuilder, times(1)).correlateStartMessage();
    }

    @Test
    void shouldNotDispatchCamundaMessage_whenHearingDataMatchesLatestHearingResponseData() {
        when(mapper.convertValue(any(), eq(HearingNoticeSchedulerVars.class))).thenReturn(
            HearingNoticeSchedulerVars.builder()
                .serviceId(SERVICE_ID)
                .dispatchedHearingIds(List.of())
                .build());

        when(hearingsService.getHearingResponse(AUTH_TOKEN, HEARING_ID)).thenReturn(
            createHearing(ListAssistCaseStatus.LISTED));

        when(hearingsService.getPartiesNotifiedResponses(AUTH_TOKEN, HEARING_ID)).thenReturn(
            PartiesNotifiedResponses.builder().responses(List.of(
                PartiesNotifiedResponse.builder()
                    .responseReceivedDateTime(LocalDateTime.now())
                    .serviceData(PartiesNotifiedServiceData
                                     .builder()
                                     .days(List.of(HearingDay.builder()
                                                       .hearingStartDateTime(HEARING_DATE)
                                                       .hearingEndDateTime(HEARING_DATE.plusHours(1))
                                                       .build()))
                                     .hearingLocation(VENUE_ID)
                                     .build())
                    .build()
            )).build());


        CaseDetails mockCaseDetails = CaseDetails.builder()
            .id(Long.parseLong(CASE_ID))
            .state("CASE_PROGRESSION")
            .build();

        when(coreCaseDataService.getCase(Long.parseLong(CASE_ID))).thenReturn(mockCaseDetails);

        handler.handle(new HearingNoticeSchedulerTaskEvent(HEARING_ID));

        verify(hearingsService, times(1)).updatePartiesNotifiedResponse(
            AUTH_TOKEN,
            HEARING_ID,
            VERSION,
            RECEIVED_DATETIME,
            PartiesNotified.builder().serviceData(PartiesNotifiedServiceData.builder()
                                                      .hearingLocation(VENUE_ID)
                                                      .days(List.of(HearingDay.builder()
                                                                        .hearingStartDateTime(HEARING_DATE)
                                                                        .hearingEndDateTime(HEARING_DATE.plusHours(1))
                                                                        .build()))
                                                      .hearingNoticeGenerated(false).build()).build()
        );
        verify(runtimeService, times(0)).createMessageCorrelation(MESSAGE_ID);
        verifyNoInteractions(messageCorrelationBuilder);
    }

    @Test
    void shouldAttemptToCallHmcApiThreeTimes_whenGetHearingThrowsException() {
        when(hearingsService.getHearingResponse(anyString(), anyString())).thenThrow(HmcException.class);

        handler.handle(new HearingNoticeSchedulerTaskEvent(HEARING_ID));

        verify(hearingsService, times(3)).getHearingResponse(anyString(), anyString());
        verify(runtimeService, times(0)).createMessageCorrelation(MESSAGE_ID);
    }

    private HearingGetResponse createHearing(ListAssistCaseStatus hearingStatus) {
        return HearingGetResponse.builder()
            .hearingDetails(HearingDetails.builder().build())
            .requestDetails(HearingRequestDetails.builder()
                                .versionNumber(VERSION.longValue())
                                .build())
            .caseDetails(CaseDetailsHearing.builder().caseRef(HearingNoticeSchedulerEventHandlerTest.CASE_ID).build())
            .partyDetails(List.of(PartyDetailsModel.builder().build()))
            .hearingResponse(
                HearingResponse.builder()
                    .receivedDateTime(RECEIVED_DATETIME)
                    .laCaseStatus(hearingStatus)
                    .hearingDaySchedule(List.of(
                        HearingDaySchedule.builder()
                            .hearingStartDateTime(HEARING_DATE)
                            .hearingEndDateTime(HEARING_DATE.plusHours(1))
                            .hearingVenueId(VENUE_ID).build()
                    ))
                    .build())
            .build();
    }

}
