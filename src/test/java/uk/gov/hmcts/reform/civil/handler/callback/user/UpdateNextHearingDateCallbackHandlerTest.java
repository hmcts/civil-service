package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.NextHearingDetails;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.nexthearingdate.NextHearingDateCamundaService;
import uk.gov.hmcts.reform.civil.service.nexthearingdate.NextHearingDateVariables;
import uk.gov.hmcts.reform.hmc.model.hearing.HearingDaySchedule;
import uk.gov.hmcts.reform.hmc.model.hearings.CaseHearing;
import uk.gov.hmcts.reform.hmc.model.hearings.HearingsResponse;
import uk.gov.hmcts.reform.hmc.service.HearingsService;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.enums.nexthearingdate.UpdateType.DELETE;
import static uk.gov.hmcts.reform.civil.enums.nexthearingdate.UpdateType.UPDATE;

@SpringBootTest(classes = {
    UpdateNextHearingDateCallbackHandler.class,
    JacksonAutoConfiguration.class,
    CaseDetailsConverter.class
})
class UpdateNextHearingDateCallbackHandlerTest extends BaseCallbackHandlerTest {

    @Autowired
    private UpdateNextHearingDateCallbackHandler handler;

    @MockBean
    private HearingsService hearingsService;

    @MockBean
    private NextHearingDateCamundaService camundaService;

    @Autowired
    private ObjectMapper objectMapper;

    private static final LocalDateTime CURRENT_DATE = LocalDateTime.of(2024, 01, 06, 0, 0, 0);
    private static MockedStatic currentDateMock;

    @SuppressWarnings("unchecked")
    @BeforeAll
    static void setupSuite() {
        currentDateMock = mockStatic(LocalDateTime.class, Mockito.CALLS_REAL_METHODS);
        currentDateMock.when(LocalDateTime::now).thenReturn(CURRENT_DATE);
    }

    @Nested
    class AboutToSubmit {
        private final String processInstanceId = "process-instance-id";
        private final String hearingId = "12345";

        @Test
        void shouldPopulateNextHearingDetailsWithGivenData() {
            CaseData caseData = CaseDataBuilder.builder().businessProcess(
                BusinessProcess.builder()
                    .processInstanceId(processInstanceId)
                    .build()).build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            Long caseId = params.getRequest().getCaseDetails().getId();
            NextHearingDetails nextHearingDetails = NextHearingDetails.builder()
                .hearingID(hearingId)
                .hearingDateTime(LocalDateTime.of(2024, 01, 01, 12, 01, 00))
                .build();

            when(camundaService.getProcessVariables(anyString())).thenReturn(
                NextHearingDateVariables.builder()
                    .updateType(UPDATE)
                    .hearingId(nextHearingDetails.getHearingID())
                    .nextHearingDate(nextHearingDetails.getHearingDateTime())
                    .build());

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            CaseData responseData = objectMapper.convertValue(response.getData(), CaseData.class);

            verifyNoInteractions(hearingsService);
            assertEquals(nextHearingDetails, responseData.getNextHearingDetails());
        }

        @Test
        void shouldUpdateNextHearingDetails_whenAHearingIsScheduledForCurrentDay() {
            CaseData caseData = CaseDataBuilder.builder().businessProcess(
                BusinessProcess.builder()
                    .processInstanceId(processInstanceId)
                    .build()).build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            Long caseId = params.getRequest().getCaseDetails().getId();
            LocalDateTime previousHearingDate = LocalDateTime.of(2024, 01, 05, 9, 00, 00);
            LocalDateTime nextHearingDate = LocalDateTime.of(2024, 01, 06, 9, 00, 00);

            when(camundaService.getProcessVariables(anyString())).thenReturn(
                NextHearingDateVariables.builder()
                    .caseId(caseId)
                    .build());

            when(hearingsService.getHearings(anyString(), eq(caseId), eq("LISTED")))
                .thenReturn(HearingsResponse.builder()
                                .caseRef("reference")
                                .hmctsServiceCode("AAA7")
                                .caseHearings(List.of(
                                    CaseHearing.builder()
                                        .hearingId(Long.valueOf(hearingId))
                                        .hearingDaySchedule(List.of(
                                            HearingDaySchedule.builder().hearingStartDateTime(previousHearingDate).build(),
                                            HearingDaySchedule.builder().hearingStartDateTime(nextHearingDate).build()
                                        ))
                                        .build()))
                                .build());

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            CaseData responseData = objectMapper.convertValue(response.getData(), CaseData.class);

            assertEquals(NextHearingDetails.builder()
                             .hearingID(hearingId)
                             .hearingDateTime(nextHearingDate)
                             .build(), responseData.getNextHearingDetails());
        }

        @Test
        void shouldUpdateNextHearingDetails_whenAHearingIsScheduledForFutureDay() {
            CaseData caseData = CaseDataBuilder.builder().businessProcess(
                BusinessProcess.builder()
                    .processInstanceId(processInstanceId)
                    .build()).build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            Long caseId = params.getRequest().getCaseDetails().getId();
            LocalDateTime previousHearingDate = LocalDateTime.of(2024, 01, 05, 9, 00, 00);
            LocalDateTime nextHearingDate = LocalDateTime.of(2024, 01, 10, 9, 00, 00);

            when(camundaService.getProcessVariables(anyString())).thenReturn(
                NextHearingDateVariables.builder()
                    .caseId(caseId)
                    .build());

            when(hearingsService.getHearings(anyString(), eq(caseId), eq("LISTED")))
                .thenReturn(HearingsResponse.builder()
                                .caseRef("reference")
                                .hmctsServiceCode("AAA7")
                                .caseHearings(List.of(
                                    CaseHearing.builder()
                                        .hearingId(Long.valueOf(hearingId))
                                        .hearingDaySchedule(List.of(
                                            HearingDaySchedule.builder().hearingStartDateTime(previousHearingDate).build(),
                                            HearingDaySchedule.builder().hearingStartDateTime(nextHearingDate).build()
                                        ))
                                        .build()))
                                .build());

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            CaseData responseData = objectMapper.convertValue(response.getData(), CaseData.class);

            assertEquals(NextHearingDetails.builder()
                             .hearingID(hearingId)
                             .hearingDateTime(nextHearingDate)
                             .build(), responseData.getNextHearingDetails());
        }

        @Test
        void shouldUpdateNextHearingDetailsWithEarliestFutureDay_whenAHearingHasMultipleFutureDays() {
            CaseData caseData = CaseDataBuilder.builder().businessProcess(
                BusinessProcess.builder()
                    .processInstanceId(processInstanceId)
                    .build()).build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            Long caseId = params.getRequest().getCaseDetails().getId();
            LocalDateTime previousHearingDate = LocalDateTime.of(2024, 01, 05, 9, 00, 00);
            LocalDateTime nextHearingDate = LocalDateTime.of(2024, 01, 10, 9, 00, 00);
            LocalDateTime futureHearingDate = LocalDateTime.of(2024, 01, 10, 9, 00, 00);

            when(camundaService.getProcessVariables(anyString())).thenReturn(
                NextHearingDateVariables.builder()
                    .caseId(caseId)
                    .build());

            when(hearingsService.getHearings(anyString(), eq(caseId), eq("LISTED")))
                .thenReturn(HearingsResponse.builder()
                                .caseRef("reference")
                                .hmctsServiceCode("AAA7")
                                .caseHearings(List.of(
                                    CaseHearing.builder()
                                        .hearingId(Long.valueOf(hearingId))
                                        .hearingDaySchedule(List.of(
                                            HearingDaySchedule.builder().hearingStartDateTime(previousHearingDate).build(),
                                            HearingDaySchedule.builder().hearingStartDateTime(nextHearingDate).build(),
                                            HearingDaySchedule.builder().hearingStartDateTime(futureHearingDate).build()
                                        ))
                                        .build()))
                                .build());

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            CaseData responseData = objectMapper.convertValue(response.getData(), CaseData.class);

            assertEquals(NextHearingDetails.builder()
                             .hearingID(hearingId)
                             .hearingDateTime(nextHearingDate)
                             .build(), responseData.getNextHearingDetails());
        }

        @Test
        void shouldClearNextHearingDetails_whenAHearingAllHearingDaysHaveElapsed() {
            CaseData caseData = CaseDataBuilder.builder().businessProcess(
                BusinessProcess.builder()
                    .processInstanceId(processInstanceId)
                    .build()).build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            Long caseId = params.getRequest().getCaseDetails().getId();
            LocalDateTime previousHearingDate = LocalDateTime.of(2024, 01, 04, 9, 00, 00);
            LocalDateTime nextHearingDate = LocalDateTime.of(2024, 01, 05, 9, 00, 00);

            when(camundaService.getProcessVariables(anyString())).thenReturn(
                NextHearingDateVariables.builder()
                    .caseId(caseId)
                    .build());

            when(hearingsService.getHearings(anyString(), eq(caseId), eq("LISTED")))
                .thenReturn(HearingsResponse.builder()
                                .caseRef("reference")
                                .hmctsServiceCode("AAA7")
                                .caseHearings(List.of(
                                    CaseHearing.builder()
                                        .hearingId(Long.valueOf(hearingId))
                                        .hearingDaySchedule(List.of(
                                            HearingDaySchedule.builder().hearingStartDateTime(previousHearingDate).build(),
                                            HearingDaySchedule.builder().hearingStartDateTime(nextHearingDate).build()
                                        ))
                                        .build()))
                                .build());

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            CaseData responseData = objectMapper.convertValue(response.getData(), CaseData.class);

            assertNull(responseData.getNextHearingDetails());
        }

        @Test
        void shouldClearNextHearingDetails() {
            CaseData caseData = CaseDataBuilder.builder().businessProcess(
                BusinessProcess.builder()
                    .processInstanceId(processInstanceId)
                    .build()).build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            when(camundaService.getProcessVariables(anyString())).thenReturn(
                NextHearingDateVariables.builder()
                    .updateType(DELETE)
                    .build());

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            CaseData responseData = objectMapper.convertValue(response.getData(), CaseData.class);
            verifyNoInteractions(hearingsService);
            assertNull(responseData.getNextHearingDetails());
        }
    }
}

