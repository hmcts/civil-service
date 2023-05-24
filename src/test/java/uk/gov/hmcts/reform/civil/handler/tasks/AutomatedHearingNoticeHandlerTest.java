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
import java.util.Arrays;
import java.util.HashMap;
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

    final String serviceIdKey = "serviceId";
    final String dispatchedHearingIdsKey = "dispatchedHearingIds";
    final String hearingId = "hearing-id-1";
    final String caseId = "1111111111111111";
    final String serviceId = "AAA7";
    final String venueId = "00000";
    final Long versionNumber = Long.parseLong("1");
    final String processInstanceId = "process-instance-id";
    final String authToken = "mock_token";
    final LocalDateTime hearingDate = LocalDateTime.of(2030, 1, 1, 12, 0, 0);
    final LocalDateTime hearingReceivedDateTime = LocalDateTime.of(2029, 12, 1, 12, 0, 0);
    final String camundaMessageId = "NOTIFY_HEARING_PARTIES";

    @BeforeEach
    void init() {
        when(featureToggleService.isAutomatedHearingNoticeEnabled()).thenReturn(true);
        when(runtimeService.createMessageCorrelation(any())).thenReturn(messageCorrelationBuilder);
        when(messageCorrelationBuilder.setVariables(any())).thenReturn(messageCorrelationBuilder);
        when(mockTask.getTopicName()).thenReturn("test");
        when(mockTask.getWorkerId()).thenReturn("worker");
        when(mockTask.getVariable(serviceIdKey)).thenReturn(serviceId);
        when(mockTask.getProcessInstanceId()).thenReturn(processInstanceId);
        when(userService.getAccessToken(anyString(), anyString())).thenReturn(authToken);
        when(userService.getUserInfo(anyString())).thenReturn(UserInfo.builder().uid("test-id").build());
        when(userConfig.getUserName()).thenReturn("");
        when(userConfig.getPassword()).thenReturn("");
        when(runtimeService.getVariable(processInstanceId, serviceIdKey)).thenReturn(new ArrayList<>());
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
                .serviceId(serviceId)
                .dispatchedHearingIds(dispatchedHearingIds)
                .build());
        when(mapper.convertValue(any(), eq(HearingNoticeMessageVars.class))).thenReturn(
            HearingNoticeMessageVars.builder()
                .caseId(caseId)
                .hearingId(hearingId)
                .triggeredViaScheduler(true)
                .build());
        when(hearingsService.getUnNotifiedHearingResponses(authToken, serviceId, null, null)).thenReturn(
            createUnnotifiedHearings(List.of(hearingId)));
        when(hearingsService.getHearingResponse(authToken, hearingId)).thenReturn(
            createHearing(caseId, ListAssistCaseStatus.LISTED));
        when(hearingsService.getPartiesNotifiedResponses(authToken, hearingId)).thenReturn(
            PartiesNotifiedResponses.builder().responses(List.of()).build());

        handler.execute(mockTask, externalTaskService);

        verify(runtimeService).setVariables(processInstanceId, HearingNoticeSchedulerVars.builder().totalNumberOfUnnotifiedHearings(1).dispatchedHearingIds(List.of(hearingId)).build().toMap(mapper));
        verify(runtimeService, times(1)).createMessageCorrelation(camundaMessageId);
        verify(messageCorrelationBuilder, times(1)).setVariables(HearingNoticeMessageVars.builder().caseId(caseId).hearingId(hearingId).triggeredViaScheduler(true).build().toMap(mapper));
        verify(messageCorrelationBuilder, times(1)).correlateStartMessage();
        verify(externalTaskService).complete(mockTask);
    }

    @Test
    void shouldDispatchExpectedCamundaMessage_whenHearingIsInListedStatusAndPartiesNotifiedResponsesAreNull() {
        final ArrayList<String> dispatchedHearingIds = new ArrayList<>();

        when(mapper.convertValue(any(), eq(HearingNoticeSchedulerVars.class))).thenReturn(
            HearingNoticeSchedulerVars.builder()
                .serviceId(serviceId)
                .dispatchedHearingIds(dispatchedHearingIds)
                .build());
        when(mapper.convertValue(any(), eq(HearingNoticeMessageVars.class))).thenReturn(
            HearingNoticeMessageVars.builder()
                .caseId(caseId)
                .hearingId(hearingId)
                .triggeredViaScheduler(true)
                .build());
        when(hearingsService.getUnNotifiedHearingResponses(authToken, serviceId, null, null)).thenReturn(
            createUnnotifiedHearings(List.of(hearingId)));
        when(hearingsService.getHearingResponse(authToken, hearingId)).thenReturn(
            createHearing(caseId, ListAssistCaseStatus.LISTED));
        when(hearingsService.getPartiesNotifiedResponses(authToken, hearingId)).thenReturn(
            PartiesNotifiedResponses.builder().build());

        handler.execute(mockTask, externalTaskService);

        verify(runtimeService).setVariables(processInstanceId, HearingNoticeSchedulerVars.builder().totalNumberOfUnnotifiedHearings(1).dispatchedHearingIds(List.of(hearingId)).build().toMap(mapper));
        verify(runtimeService, times(1)).createMessageCorrelation(camundaMessageId);
        verify(messageCorrelationBuilder, times(1)).setVariables(HearingNoticeMessageVars.builder().caseId(caseId).hearingId(hearingId).triggeredViaScheduler(true).build().toMap(mapper));
        verify(messageCorrelationBuilder, times(1)).correlateStartMessage();
        verify(externalTaskService).complete(mockTask);
    }

    @Test
    void shouldNotDispatchCamundaMessage_whenHearingIsNotInListedStatus() {
        when(mapper.convertValue(any(), eq(HearingNoticeSchedulerVars.class))).thenReturn(
            HearingNoticeSchedulerVars.builder()
                .serviceId(serviceId)
                .dispatchedHearingIds(List.of())
                .build());
        when(hearingsService.getUnNotifiedHearingResponses(authToken, serviceId, null, null)).thenReturn(
            createUnnotifiedHearings(List.of(hearingId)));
        when(hearingsService.getHearingResponse(authToken, hearingId)).thenReturn(
            createHearing(caseId, ListAssistCaseStatus.CASE_CLOSED));
        when(hearingsService.getPartiesNotifiedResponses(authToken, hearingId)).thenReturn(
            PartiesNotifiedResponses.builder().responses(List.of()).build());

        handler.execute(mockTask, externalTaskService);

        verify(hearingsService, times(1))
            .updatePartiesNotifiedResponse(
                authToken,
                hearingId,
                versionNumber,
                hearingReceivedDateTime,
                PartiesNotified.builder()
                    .serviceData(PartiesNotifiedServiceData.builder().hearingNoticeGenerated(false).build())
                    .build()
            );
        verify(runtimeService).setVariables(processInstanceId, HearingNoticeSchedulerVars.builder().totalNumberOfUnnotifiedHearings(1).dispatchedHearingIds(List.of()).build().toMap(mapper));
        verify(runtimeService, times(0)).createMessageCorrelation(camundaMessageId);
        verifyNoInteractions(messageCorrelationBuilder);
        verify(externalTaskService).complete(mockTask);
    }

    @Test
    void shouldNotDispatchCamundaMessage_whenHearingIdIsInDispatchedHearingIdsCamundaVariables() {
        var dispatchedHearingIds = List.of(hearingId);

        when(mapper.convertValue(any(), eq(HearingNoticeSchedulerVars.class))).thenReturn(
            HearingNoticeSchedulerVars.builder()
                .serviceId(serviceId)
                .dispatchedHearingIds(dispatchedHearingIds)
                .build());
        when(hearingsService.getUnNotifiedHearingResponses(authToken, serviceId, null, null)).thenReturn(
            createUnnotifiedHearings(List.of(hearingId)));
        when(hearingsService.getHearingResponse(authToken, hearingId)).thenReturn(
            createHearing(caseId, ListAssistCaseStatus.LISTED));
        when(hearingsService.getPartiesNotifiedResponses(authToken, hearingId)).thenReturn(
            PartiesNotifiedResponses.builder().responses(List.of()).build());

        handler.execute(mockTask, externalTaskService);

        verify(runtimeService).setVariables(processInstanceId, HearingNoticeSchedulerVars.builder().totalNumberOfUnnotifiedHearings(1).dispatchedHearingIds(List.of(hearingId)).build().toMap(mapper));
        verify(runtimeService, times(0)).createMessageCorrelation(camundaMessageId);
        verifyNoInteractions(messageCorrelationBuilder);
        verify(externalTaskService).complete(mockTask);
    }

    @Test
    void shouldDispatchCamundaMessage_whenHearingVenueDoesNotMatchLatestPartiesNotifiedResponseHearingVenue() {
        when(mapper.convertValue(any(), eq(HearingNoticeSchedulerVars.class))).thenReturn(
            HearingNoticeSchedulerVars.builder()
                .serviceId(serviceId)
                .dispatchedHearingIds(new ArrayList<>())
                .build());
        when(mapper.convertValue(any(), eq(HearingNoticeMessageVars.class))).thenReturn(
            HearingNoticeMessageVars.builder()
                .caseId(caseId)
                .hearingId(hearingId)
                .triggeredViaScheduler(true)
                .build());
        when(hearingsService.getUnNotifiedHearingResponses(authToken, serviceId, null, null)).thenReturn(
            createUnnotifiedHearings(List.of(hearingId)));
        when(hearingsService.getHearingResponse(authToken, hearingId)).thenReturn(
            createHearing(caseId, ListAssistCaseStatus.LISTED));
        when(hearingsService.getPartiesNotifiedResponses(authToken, hearingId)).thenReturn(
            PartiesNotifiedResponses.builder().responses(List.of(
                PartiesNotifiedResponse.builder()
                    .serviceData(PartiesNotifiedServiceData
                                     .builder()
                                     .hearingDate(hearingDate)
                                     .hearingLocation("111111")
                                     .build())
                    .build()
            )).build());

        handler.execute(mockTask, externalTaskService);

        verify(runtimeService).setVariables(processInstanceId, HearingNoticeSchedulerVars.builder().totalNumberOfUnnotifiedHearings(1).dispatchedHearingIds(List.of(hearingId)).build().toMap(mapper));
        verify(runtimeService, times(1)).createMessageCorrelation(camundaMessageId);
        verify(messageCorrelationBuilder, times(1)).setVariables(HearingNoticeMessageVars.builder().caseId(caseId).hearingId(hearingId).triggeredViaScheduler(true).build().toMap(mapper));
        verify(messageCorrelationBuilder, times(1)).correlateStartMessage();
        verify(externalTaskService).complete(mockTask);
    }

    @Test
    void shouldDispatchCamundaMessage_whenHearingVenueDoesNotMatchLatestPartiesNotifiedResponseHearingDate() {
        when(mapper.convertValue(any(), eq(HearingNoticeSchedulerVars.class))).thenReturn(
            HearingNoticeSchedulerVars.builder()
                .serviceId(serviceId)
                .dispatchedHearingIds(new ArrayList<>())
                .build());
        when(mapper.convertValue(any(), eq(HearingNoticeMessageVars.class))).thenReturn(
            HearingNoticeMessageVars.builder()
                .caseId(caseId)
                .hearingId(hearingId)
                .triggeredViaScheduler(true)
                .build());
        when(hearingsService.getUnNotifiedHearingResponses(authToken, serviceId, null, null)).thenReturn(
            createUnnotifiedHearings(List.of(hearingId)));
        when(hearingsService.getHearingResponse(authToken, hearingId)).thenReturn(
            createHearing(caseId, ListAssistCaseStatus.LISTED));
        when(hearingsService.getPartiesNotifiedResponses(authToken, hearingId)).thenReturn(
            PartiesNotifiedResponses.builder().responses(List.of(
                PartiesNotifiedResponse.builder()
                    .serviceData(PartiesNotifiedServiceData
                                     .builder()
                                     .hearingDate(LocalDateTime.of(2030, 6, 6, 12, 0, 0))
                                     .hearingLocation(venueId)
                                     .build())
                    .build()
            )).build());

        handler.execute(mockTask, externalTaskService);

        verify(runtimeService).setVariables(processInstanceId, HearingNoticeSchedulerVars.builder().totalNumberOfUnnotifiedHearings(1).dispatchedHearingIds(List.of(hearingId)).build().toMap(mapper));
        verify(runtimeService, times(1)).createMessageCorrelation(camundaMessageId);
        verify(messageCorrelationBuilder, times(1)).setVariables(
            HearingNoticeMessageVars.builder().caseId(caseId).hearingId(hearingId).triggeredViaScheduler(true).build().toMap(mapper));
        verify(messageCorrelationBuilder, times(1)).correlateStartMessage();
        verify(externalTaskService).complete(mockTask);
    }

    @Test
    void shouldNotDispatchCamundaMessage_whenHearingDataMatchesLatestHearingResponseData() {
        when(mapper.convertValue(any(), eq(HearingNoticeSchedulerVars.class))).thenReturn(
            HearingNoticeSchedulerVars.builder()
                .serviceId(serviceId)
                .dispatchedHearingIds(List.of())
                .build());

        when(hearingsService.getUnNotifiedHearingResponses(authToken, serviceId, null, null)).thenReturn(
            createUnnotifiedHearings(List.of(hearingId)));

        when(hearingsService.getHearingResponse(authToken, hearingId)).thenReturn(
            createHearing(caseId, ListAssistCaseStatus.LISTED));

        when(hearingsService.getPartiesNotifiedResponses(authToken, hearingId)).thenReturn(
            PartiesNotifiedResponses.builder().responses(List.of(
                PartiesNotifiedResponse.builder()
                    .responseReceivedDateTime(LocalDateTime.now())
                    .serviceData(PartiesNotifiedServiceData
                                     .builder()
                                     .hearingDate(hearingDate)
                                     .hearingLocation(venueId)
                                     .build())
                    .build()
            )).build());

        handler.execute(mockTask, externalTaskService);

        verify(hearingsService, times(1)).updatePartiesNotifiedResponse(
            authToken,
            hearingId,
            versionNumber,
            hearingReceivedDateTime,
            PartiesNotified.builder().serviceData(PartiesNotifiedServiceData.builder()
                                                      .hearingLocation(venueId)
                                                      .hearingDate(hearingDate)
                                                      .hearingNoticeGenerated(false).build()).build()
        );
        verify(runtimeService).setVariables(processInstanceId, HearingNoticeSchedulerVars.builder().totalNumberOfUnnotifiedHearings(1).dispatchedHearingIds(List.of()).build().toMap(mapper));
        verify(runtimeService, times(0)).createMessageCorrelation(camundaMessageId);
        verifyNoInteractions(messageCorrelationBuilder);
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
                                .versionNumber(versionNumber)
                                .build())
            .caseDetails(CaseDetailsHearing.builder().caseRef(caseId).build())
            .partyDetails(List.of(PartyDetailsModel.builder().build()))
            .hearingResponse(
                HearingResponse.builder()
                    .receivedDateTime(hearingReceivedDateTime)
                    .listAssistCaseStatus(hearingStatus)
                    .hearingDaySchedule(List.of(
                        HearingDaySchedule.builder()
                            .hearingStartDateTime(hearingDate)
                            .hearingVenueId(venueId).build()
                    ))
                    .build())
            .build();
    }

}
