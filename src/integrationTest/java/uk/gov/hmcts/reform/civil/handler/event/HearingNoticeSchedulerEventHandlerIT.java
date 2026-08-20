package uk.gov.hmcts.reform.civil.handler.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.runtime.MessageCorrelationBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.config.SystemUpdateUserConfiguration;
import uk.gov.hmcts.reform.civil.event.HearingNoticeSchedulerTaskEvent;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.hmc.model.hearing.CaseDetailsHearing;
import uk.gov.hmcts.reform.hmc.model.hearing.HearingDaySchedule;
import uk.gov.hmcts.reform.hmc.model.hearing.HearingDetails;
import uk.gov.hmcts.reform.hmc.model.hearing.HearingGetResponse;
import uk.gov.hmcts.reform.hmc.model.hearing.HearingRequestDetails;
import uk.gov.hmcts.reform.hmc.model.hearing.HearingResponse;
import uk.gov.hmcts.reform.hmc.model.hearing.ListAssistCaseStatus;
import uk.gov.hmcts.reform.hmc.model.unnotifiedhearings.HearingDay;
import uk.gov.hmcts.reform.hmc.model.unnotifiedhearings.PartiesNotified;
import uk.gov.hmcts.reform.hmc.model.unnotifiedhearings.PartiesNotifiedResponse;
import uk.gov.hmcts.reform.hmc.model.unnotifiedhearings.PartiesNotifiedResponses;
import uk.gov.hmcts.reform.hmc.model.unnotifiedhearings.PartiesNotifiedServiceData;
import uk.gov.hmcts.reform.hmc.service.HearingsService;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ActiveProfiles("integration-test")
@SpringBootTest(classes = HearingNoticeSchedulerEventHandlerIT.TestConfig.class)
@SuppressWarnings({"java:S6813", "java:S5960"})
class HearingNoticeSchedulerEventHandlerIT {

    private static final String AUTH_TOKEN = "mock-token";
    private static final String CASE_ID = "1111111111111111";
    private static final String HEARING_ID = "2000222616";
    private static final int VERSION = 1;
    private static final String VENUE_ID = "00000";
    private static final LocalDateTime HEARING_DATE = LocalDateTime.of(2030, 1, 1, 12, 0);
    private static final LocalDateTime RECEIVED_DATE_TIME = LocalDateTime.of(2029, 12, 1, 12, 0);

    @Autowired
    private HearingNoticeSchedulerEventHandler handler;

    @MockBean
    private UserService userService;

    @MockBean
    private SystemUpdateUserConfiguration userConfig;

    @MockBean
    private HearingsService hearingsService;

    @MockBean
    private RuntimeService runtimeService;

    @MockBean
    private MessageCorrelationBuilder messageCorrelationBuilder;

    @MockBean
    private CoreCaseDataService coreCaseDataService;

    @BeforeEach
    void setUp() {
        when(userConfig.getUserName()).thenReturn("system-user");
        when(userConfig.getPassword()).thenReturn("password");
        when(userService.getAccessToken(anyString(), anyString())).thenReturn(AUTH_TOKEN);
        when(userService.getUserInfo(anyString())).thenReturn(UserInfo.builder().uid("system-user-id").build());
        when(runtimeService.createMessageCorrelation(anyString())).thenReturn(messageCorrelationBuilder);
        when(messageCorrelationBuilder.setVariables(any())).thenReturn(messageCorrelationBuilder);
    }

    @Test
    void shouldNotDispatchNoticeOrPutPartiesNotified_whenCurrentChangedResponseIsAlreadyNotified() {
        when(hearingsService.getHearingResponse(AUTH_TOKEN, HEARING_ID)).thenReturn(createHearing());
        when(hearingsService.getPartiesNotifiedResponses(AUTH_TOKEN, HEARING_ID)).thenReturn(
            partiesNotified(
                VERSION,
                RECEIVED_DATE_TIME,
                serviceData(HEARING_DATE.plusDays(1), VENUE_ID)
            ));

        handler.handle(new HearingNoticeSchedulerTaskEvent(HEARING_ID));

        verify(runtimeService, never()).createMessageCorrelation(anyString());
        verify(hearingsService, never()).updatePartiesNotifiedResponse(any(), any(), anyInt(), any(), any());
        verifyNoInteractions(coreCaseDataService);
    }

    @Test
    void shouldPutPartiesNotified_whenUnchangedResponseIsNewerThanExistingNotification() {
        PartiesNotifiedServiceData serviceData = serviceData(HEARING_DATE, VENUE_ID);
        when(hearingsService.getHearingResponse(AUTH_TOKEN, HEARING_ID)).thenReturn(createHearing());
        when(hearingsService.getPartiesNotifiedResponses(AUTH_TOKEN, HEARING_ID)).thenReturn(
            partiesNotified(VERSION, RECEIVED_DATE_TIME.minusDays(1), serviceData));

        handler.handle(new HearingNoticeSchedulerTaskEvent(HEARING_ID));

        verify(hearingsService).updatePartiesNotifiedResponse(
            AUTH_TOKEN,
            HEARING_ID,
            VERSION,
            RECEIVED_DATE_TIME,
            new PartiesNotified().setServiceData(serviceData)
        );
        verify(runtimeService, never()).createMessageCorrelation(anyString());
    }

    @Test
    void shouldDispatchNotice_whenListedChangedResponseIsNotAlreadyNotifiedAndCaseStateAllowsIt() {
        when(hearingsService.getHearingResponse(AUTH_TOKEN, HEARING_ID)).thenReturn(createHearing());
        when(hearingsService.getPartiesNotifiedResponses(AUTH_TOKEN, HEARING_ID)).thenReturn(
            partiesNotified(VERSION, RECEIVED_DATE_TIME.minusDays(1), serviceData(HEARING_DATE.plusDays(1), VENUE_ID)));
        when(coreCaseDataService.getCase(Long.parseLong(CASE_ID))).thenReturn(
            CaseDetails.builder().id(Long.parseLong(CASE_ID)).state("CASE_PROGRESSION").build());

        handler.handle(new HearingNoticeSchedulerTaskEvent(HEARING_ID));

        verify(runtimeService).createMessageCorrelation("NOTIFY_HEARING_PARTIES");
        verify(messageCorrelationBuilder).correlateStartMessage();
        verify(hearingsService, never()).updatePartiesNotifiedResponse(any(), any(), anyInt(), any(), any());
    }

    private HearingGetResponse createHearing() {
        return new HearingGetResponse()
            .setHearingDetails(new HearingDetails())
            .setRequestDetails(new HearingRequestDetails().setVersionNumber((long)VERSION))
            .setCaseDetails(new CaseDetailsHearing().setCaseRef(CASE_ID))
            .setHearingResponse(
                new HearingResponse()
                    .setReceivedDateTime(RECEIVED_DATE_TIME)
                    .setLaCaseStatus(ListAssistCaseStatus.LISTED)
                    .setHearingDaySchedule(List.of(
                        new HearingDaySchedule()
                            .setHearingStartDateTime(HEARING_DATE)
                            .setHearingEndDateTime(HEARING_DATE.plusHours(1))
                            .setHearingVenueId(VENUE_ID)
                    )));
    }

    private PartiesNotifiedResponses partiesNotified(int requestVersion, LocalDateTime responseReceivedDateTime,
                                                     PartiesNotifiedServiceData serviceData) {
        return new PartiesNotifiedResponses().setResponses(List.of(
            new PartiesNotifiedResponse()
                .setRequestVersion(requestVersion)
                .setResponseReceivedDateTime(responseReceivedDateTime)
                .setServiceData(serviceData)
        ));
    }

    private PartiesNotifiedServiceData serviceData(LocalDateTime hearingDate, String venueId) {
        return new PartiesNotifiedServiceData()
            .setDays(List.of(new HearingDay()
                                 .setHearingStartDateTime(hearingDate)
                                 .setHearingEndDateTime(hearingDate.plusHours(1))))
            .setHearingLocation(venueId);
    }

    @Configuration
    @Import(HearingNoticeSchedulerEventHandler.class)
    static class TestConfig {

        @Bean
        ObjectMapper objectMapper() {
            return new ObjectMapper();
        }
    }
}
