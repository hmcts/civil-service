package uk.gov.hmcts.reform.civil.handler.callback.user.hearings;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.config.SystemUpdateUserConfiguration;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.NextHearingDetails;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.Time;
import uk.gov.hmcts.reform.hmc.model.hearing.HearingDaySchedule;
import uk.gov.hmcts.reform.hmc.model.hearings.CaseHearing;
import uk.gov.hmcts.reform.hmc.model.hearings.HearingsResponse;
import uk.gov.hmcts.reform.hmc.model.messaging.HmcStatus;
import uk.gov.hmcts.reform.hmc.service.HearingsService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.hmc.model.messaging.HmcStatus.ADJOURNED;
import static uk.gov.hmcts.reform.hmc.model.messaging.HmcStatus.CANCELLED;
import static uk.gov.hmcts.reform.hmc.model.messaging.HmcStatus.LISTED;

@SpringBootTest(classes = {
    UpdateNextHearingDetailsCallbackHandler.class,
    JacksonAutoConfiguration.class,
    CaseDetailsConverter.class
})
class UpdateNextHearingDetailsCallbackHandlerTest extends BaseCallbackHandlerTest {

    @MockBean
    SystemUpdateUserConfiguration userConfig;

    @MockBean
    HearingsService hearingService;

    @MockBean
    Time datetime;

    @Autowired
    private UpdateNextHearingDetailsCallbackHandler handler;

    @Autowired
    private ObjectMapper mapper;

    @Nested
    class AboutToStart {
        private static final LocalDateTime TODAY = LocalDateTime.of(2024, 1, 22, 0, 0, 0);

        @BeforeEach
        void setup() {
            String mockSystemUsername = "username";
            String mockSystemUserPassword = "password";
            when(userConfig.getUserName()).thenReturn(mockSystemUsername);
            when(userConfig.getPassword()).thenReturn(mockSystemUserPassword);
            when(userService.getAccessToken(eq(mockSystemUsername), eq(mockSystemUserPassword)))
                .thenReturn("mock-token");
            when(datetime.now()).thenReturn(TODAY);
        }

        @Nested
        class UpdateNextHearingDetails {

            @ParameterizedTest(name = "and HMCStatus is {0}")
            @EnumSource(
                value = HmcStatus.class,
                names = {"LISTED", "AWAITING_ACTUALS"})
            void shouldSetNextHearingDetails_whenANextHearingDateExistsForToday(HmcStatus hmcstatus) {
                LocalDateTime hearingStartTime = TODAY.plusHours(10);
                LocalDateTime requestedDateTime = TODAY.plusHours(9);

                CaseData caseData = CaseDataBuilder.builder().build();
                String hearingId = "12345";

                HearingsResponse hearingsResponse = HearingsResponse.builder()
                    .caseHearings(List.of(
                        hearing("11111", CANCELLED, TODAY.minusDays(1), List.of(hearingStartTime)),
                        hearing(hearingId, hmcstatus, requestedDateTime, List.of(hearingStartTime)),
                        hearing("33333", hmcstatus, TODAY.minusDays(3), List.of(hearingStartTime)),
                        hearing("22222", ADJOURNED, TODAY.minusDays(2), List.of(hearingStartTime))
                    ))
                    .build();

                when(hearingService.getHearings(any(), any(), any())).thenReturn(hearingsResponse);

                CallbackParams params = callbackParamsOf(
                    caseData,
                    CaseEvent.UPDATE_NEXT_HEARING_DETAILS,
                    ABOUT_TO_SUBMIT
                );

                AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                    .handle(params);

                CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);

                NextHearingDetails expected = NextHearingDetails.builder()
                    .hearingID(hearingId.toString())
                    .hearingDateTime(hearingStartTime).build();

                assertEquals(expected, updatedData.getNextHearingDetails());
            }

            @ParameterizedTest(name = "and HMCStatus is {0}")
            @EnumSource(
                value = HmcStatus.class,
                names = {"LISTED", "AWAITING_ACTUALS"})
            void shouldSetNextHearingDetails_whenAFutureNextHearingDateExists(HmcStatus hmcstatus) {
                LocalDateTime hearingStartTime = TODAY.plusDays(1).plusHours(10);
                LocalDateTime requestedDateTime = TODAY.plusHours(9);

                CaseData caseData = CaseDataBuilder.builder().build();
                String hearingId = "12345";

                HearingsResponse hearingsResponse = HearingsResponse.builder()
                    .caseHearings(List.of(
                        hearing("11111", CANCELLED, TODAY.minusDays(1), List.of(hearingStartTime)),
                        hearing(hearingId, hmcstatus, requestedDateTime, List.of(TODAY.minusDays(1), hearingStartTime)),
                        hearing("33333", hmcstatus, TODAY.minusDays(3), List.of(hearingStartTime)),
                        hearing("22222", ADJOURNED, TODAY.minusDays(2), List.of(hearingStartTime))
                    ))
                    .build();

                when(hearingService.getHearings(any(), any(), any())).thenReturn(hearingsResponse);

                CallbackParams params = callbackParamsOf(
                    caseData,
                    CaseEvent.UPDATE_NEXT_HEARING_DETAILS,
                    ABOUT_TO_SUBMIT
                );

                AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                    .handle(params);

                CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);

                NextHearingDetails expected = NextHearingDetails.builder()
                    .hearingID(hearingId.toString())
                    .hearingDateTime(hearingStartTime).build();

                assertEquals(expected, updatedData.getNextHearingDetails());
            }

            @ParameterizedTest(name = "and HMCStatus is {0}")
            @EnumSource(
                value = HmcStatus.class,
                names = {"LISTED", "AWAITING_ACTUALS"})
            void shouldSetNextHearingDetails_whenNextHearingDateForTodayAndNextHearingDateForTheFutureExists(HmcStatus hmcstatus) {
                LocalDateTime hearingStartTime = TODAY.plusHours(10);
                LocalDateTime requestedDateTime = TODAY.plusHours(9);

                CaseData caseData = CaseDataBuilder.builder().build();
                String hearingId = "12345";

                HearingsResponse hearingsResponse = HearingsResponse.builder()
                    .caseHearings(List.of(
                        hearing("11111", CANCELLED, TODAY.minusDays(1), List.of(hearingStartTime)),
                        hearing(
                            hearingId,
                            hmcstatus,
                            requestedDateTime,
                            List.of(hearingStartTime, hearingStartTime.plusDays(1))
                        ),
                        hearing("33333", hmcstatus, TODAY.minusDays(3), List.of(hearingStartTime)),
                        hearing("22222", ADJOURNED, TODAY.minusDays(2), List.of(hearingStartTime))
                    ))
                    .build();

                when(hearingService.getHearings(any(), any(), any())).thenReturn(hearingsResponse);

                CallbackParams params = callbackParamsOf(
                    caseData,
                    CaseEvent.UPDATE_NEXT_HEARING_DETAILS,
                    ABOUT_TO_SUBMIT
                );

                AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                    .handle(params);

                CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);

                NextHearingDetails expected = NextHearingDetails.builder()
                    .hearingID(hearingId.toString())
                    .hearingDateTime(hearingStartTime).build();

                assertEquals(expected, updatedData.getNextHearingDetails());
            }

            @ParameterizedTest(name = "and HMCStatus is {0}")
            @EnumSource(
                value = HmcStatus.class,
                names = {"LISTED", "AWAITING_ACTUALS"})
            void shouldClearNextHearingDetails_whenOnlyElapsedNextHearingDatesExist(HmcStatus hmcstatus) {
                LocalDateTime requestedDateTime = TODAY.plusHours(9);

                CaseData caseData = CaseDataBuilder.builder().build();
                String hearingId = "12345";

                HearingsResponse hearingsResponse = HearingsResponse.builder()
                    .caseHearings(List.of(
                        hearing("11111", CANCELLED, TODAY.minusDays(1), List.of(TODAY.minusDays(1))),
                        hearing(
                            hearingId,
                            hmcstatus,
                            requestedDateTime,
                            List.of(TODAY.minusDays(1), TODAY.minusDays(2), TODAY.minusDays(3))
                        ),
                        hearing("22222", ADJOURNED, TODAY.minusDays(2), List.of(TODAY.minusDays(2)))
                    ))
                    .build();

                when(hearingService.getHearings(any(), any(), any())).thenReturn(hearingsResponse);

                CallbackParams params = callbackParamsOf(
                    caseData,
                    CaseEvent.UPDATE_NEXT_HEARING_DETAILS,
                    ABOUT_TO_SUBMIT
                );

                AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                    .handle(params);

                CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);

                assertNull(updatedData.getNextHearingDetails());
            }

            @ParameterizedTest(name = "when the latest requested hearing HMCStatus is {0}")
            @EnumSource(
                value = HmcStatus.class,
                names = {"COMPLETED", "CANCELLED", "ADJOURNED"})
            void shouldClearNextHearingDetails(HmcStatus hmcstatus) {
                LocalDateTime hearingStartTime = TODAY.plusHours(10);
                LocalDateTime requestedDateTime = TODAY.plusHours(9);

                CaseData caseData = CaseDataBuilder.builder().build();
                String hearingId = "12345";

                HearingsResponse hearingsResponse = HearingsResponse.builder()
                    .caseHearings(List.of(
                        hearing("11111", LISTED, requestedDateTime.minusDays(1), List.of(hearingStartTime)),
                        hearing(hearingId, hmcstatus, requestedDateTime, List.of(hearingStartTime))
                    ))
                    .build();

                when(hearingService.getHearings(any(), any(), any())).thenReturn(hearingsResponse);

                CallbackParams params = callbackParamsOf(
                    caseData,
                    CaseEvent.UPDATE_NEXT_HEARING_DETAILS,
                    ABOUT_TO_SUBMIT
                );

                AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                    .handle(params);

                CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);

                assertNull(updatedData.getNextHearingDetails());
            }
        }

        @Nested
        class UpdateNextHearingInfo {
            @ParameterizedTest(name = "and HMCStatus is {0}")
            @EnumSource(
                value = HmcStatus.class,
                names = {"LISTED", "AWAITING_ACTUALS"})
            void shouldSetNextHearingDetails_whenANextHearingDateExistsForToday(HmcStatus hmcstatus) {
                LocalDateTime hearingStartTime = TODAY.plusHours(10);
                LocalDateTime requestedDateTime = TODAY.plusHours(9);

                CaseData caseData = CaseDataBuilder.builder().build();
                String hearingId = "12345";

                HearingsResponse hearingsResponse = HearingsResponse.builder()
                    .caseHearings(List.of(
                        hearing("11111", CANCELLED, TODAY.minusDays(1), List.of(hearingStartTime)),
                        hearing(hearingId, hmcstatus, requestedDateTime, List.of(hearingStartTime)),
                        hearing("33333", hmcstatus, TODAY.minusDays(3), List.of(hearingStartTime)),
                        hearing("22222", ADJOURNED, TODAY.minusDays(2), List.of(hearingStartTime))
                    ))
                    .build();

                when(hearingService.getHearings(any(), any(), any())).thenReturn(hearingsResponse);

                CallbackParams params = callbackParamsOf(
                    caseData,
                    CaseEvent.UpdateNextHearingInfo,
                    ABOUT_TO_START
                );

                AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                    .handle(params);

                CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);

                NextHearingDetails expected = NextHearingDetails.builder()
                    .hearingID(hearingId.toString())
                    .hearingDateTime(hearingStartTime).build();

                assertEquals(expected, updatedData.getNextHearingDetails());
            }

            @ParameterizedTest(name = "and HMCStatus is {0}")
            @EnumSource(
                value = HmcStatus.class,
                names = {"LISTED", "AWAITING_ACTUALS"})
            void shouldSetNextHearingDetails_whenAFutureNextHearingDateExists(HmcStatus hmcstatus) {
                LocalDateTime hearingStartTime = TODAY.plusDays(1).plusHours(10);
                LocalDateTime requestedDateTime = TODAY.plusHours(9);

                CaseData caseData = CaseDataBuilder.builder().build();
                String hearingId = "12345";

                HearingsResponse hearingsResponse = HearingsResponse.builder()
                    .caseHearings(List.of(
                        hearing("11111", CANCELLED, TODAY.minusDays(1), List.of(hearingStartTime)),
                        hearing(hearingId, hmcstatus, requestedDateTime, List.of(TODAY.minusDays(1), hearingStartTime)),
                        hearing("33333", hmcstatus, TODAY.minusDays(3), List.of(hearingStartTime)),
                        hearing("22222", ADJOURNED, TODAY.minusDays(2), List.of(hearingStartTime))
                    ))
                    .build();

                when(hearingService.getHearings(any(), any(), any())).thenReturn(hearingsResponse);

                CallbackParams params = callbackParamsOf(
                    caseData,
                    CaseEvent.UpdateNextHearingInfo,
                    ABOUT_TO_START
                );

                AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                    .handle(params);

                CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);

                NextHearingDetails expected = NextHearingDetails.builder()
                    .hearingID(hearingId.toString())
                    .hearingDateTime(hearingStartTime).build();

                assertEquals(expected, updatedData.getNextHearingDetails());
            }

            @ParameterizedTest(name = "and HMCStatus is {0}")
            @EnumSource(
                value = HmcStatus.class,
                names = {"LISTED", "AWAITING_ACTUALS"})
            void shouldSetNextHearingDetails_whenNextHearingDateForTodayAndNextHearingDateFortheFutureExists(HmcStatus hmcstatus) {
                LocalDateTime hearingStartTime = TODAY.plusHours(10);
                LocalDateTime requestedDateTime = TODAY.plusHours(9);

                CaseData caseData = CaseDataBuilder.builder().build();
                String hearingId = "12345";

                HearingsResponse hearingsResponse = HearingsResponse.builder()
                    .caseHearings(List.of(
                        hearing("11111", CANCELLED, TODAY.minusDays(1), List.of(hearingStartTime)),
                        hearing(
                            hearingId,
                            hmcstatus,
                            requestedDateTime,
                            List.of(hearingStartTime, hearingStartTime.plusDays(1))
                        ),
                        hearing("33333", hmcstatus, TODAY.minusDays(3), List.of(hearingStartTime)),
                        hearing("22222", ADJOURNED, TODAY.minusDays(2), List.of(hearingStartTime))
                    ))
                    .build();

                when(hearingService.getHearings(any(), any(), any())).thenReturn(hearingsResponse);

                CallbackParams params = callbackParamsOf(
                    caseData,
                    CaseEvent.UpdateNextHearingInfo,
                    ABOUT_TO_START
                );

                AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                    .handle(params);

                CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);

                NextHearingDetails expected = NextHearingDetails.builder()
                    .hearingID(hearingId.toString())
                    .hearingDateTime(hearingStartTime).build();

                assertEquals(expected, updatedData.getNextHearingDetails());
            }

            @ParameterizedTest(name = "and HMCStatus is {0}")
            @EnumSource(
                value = HmcStatus.class,
                names = {"LISTED", "AWAITING_ACTUALS"})
            void shouldClearNextHearingDetails_whenOnlyElapsedNextHearingDatesExist(HmcStatus hmcstatus) {
                LocalDateTime requestedDateTime = TODAY.plusHours(9);

                CaseData caseData = CaseDataBuilder.builder().build();
                String hearingId = "12345";

                HearingsResponse hearingsResponse = HearingsResponse.builder()
                    .caseHearings(List.of(
                        hearing("11111", CANCELLED, TODAY.minusDays(1), List.of(TODAY.minusDays(1))),
                        hearing(
                            hearingId,
                            hmcstatus,
                            requestedDateTime,
                            List.of(TODAY.minusDays(1), TODAY.minusDays(2), TODAY.minusDays(3))
                        ),
                        hearing("22222", ADJOURNED, TODAY.minusDays(2), List.of(TODAY.minusDays(2)))
                    ))
                    .build();

                when(hearingService.getHearings(any(), any(), any())).thenReturn(hearingsResponse);

                CallbackParams params = callbackParamsOf(
                    caseData,
                    CaseEvent.UpdateNextHearingInfo,
                    ABOUT_TO_START
                );

                AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                    .handle(params);

                CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);

                assertNull(updatedData.getNextHearingDetails());
            }

            @ParameterizedTest(name = "when the latest requested hearing HMCStatus is {0}")
            @EnumSource(
                value = HmcStatus.class,
                names = {"COMPLETED", "CANCELLED", "ADJOURNED"})
            void shouldClearNextHearingDetails(HmcStatus hmcstatus) {
                LocalDateTime hearingStartTime = TODAY.plusHours(10);
                LocalDateTime requestedDateTime = TODAY.plusHours(9);

                CaseData caseData = CaseDataBuilder.builder().build();
                String hearingId = "12345";

                HearingsResponse hearingsResponse = HearingsResponse.builder()
                    .caseHearings(List.of(
                        hearing("11111", LISTED, requestedDateTime.minusDays(1), List.of(hearingStartTime)),
                        hearing(hearingId, hmcstatus, requestedDateTime, List.of(hearingStartTime))
                    ))
                    .build();

                when(hearingService.getHearings(any(), any(), any())).thenReturn(hearingsResponse);

                CallbackParams params = callbackParamsOf(
                    caseData,
                    CaseEvent.UpdateNextHearingInfo,
                    ABOUT_TO_START
                );

                AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                    .handle(params);

                CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);

                assertNull(updatedData.getNextHearingDetails());
            }
        }
    }

    private CaseHearing hearing(String hearingId, HmcStatus hmcStatus, LocalDateTime hearingRequestTime, List<LocalDateTime> startTimes) {
        return CaseHearing.builder()
            .hmcStatus(hmcStatus.name())
            .hearingId(Long.valueOf(hearingId))
            .hearingRequestDateTime(hearingRequestTime)
            .hearingDaySchedule(startTimes.stream().map(startTime -> HearingDaySchedule.builder().hearingStartDateTime(
                startTime).build()).collect(
                Collectors.toList()))
            .build();
    }

}
