package uk.gov.hmcts.reform.civil.handler.callback.camunda.docmosis;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.hearingnotice.HearingDay;
import uk.gov.hmcts.reform.civil.service.hearingnotice.HearingNoticeCamundaService;
import uk.gov.hmcts.reform.civil.service.hearingnotice.HearingNoticeVariables;
import uk.gov.hmcts.reform.hmc.model.hearing.HearingDaySchedule;
import uk.gov.hmcts.reform.hmc.model.hearing.HearingGetResponse;
import uk.gov.hmcts.reform.hmc.model.hearing.HearingRequestDetails;
import uk.gov.hmcts.reform.hmc.model.hearing.HearingResponse;
import uk.gov.hmcts.reform.hmc.service.HearingsService;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.GENERATE_HEARING_NOTICE_HMC;
import static uk.gov.hmcts.reform.civil.enums.CaseState.CASE_PROGRESSION;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {
    GenerateHearingNoticeHmcHandler.class,
    JacksonAutoConfiguration.class,
    ValidationAutoConfiguration.class,
    CaseDetailsConverter.class,
})
public class GenerateHearingNoticeHmcHandlerTest extends BaseCallbackHandlerTest {

    @Autowired
    private final ObjectMapper mapper = new ObjectMapper();
    @Autowired
    private GenerateHearingNoticeHmcHandler handler;
    @MockBean
    private HearingsService hearingsService;
    @MockBean
    private HearingNoticeCamundaService camundaService;

    private static Long CASE_ID = 1L;
    private static String HEARING_ID = "1234";
    private static String PROCESS_INSTANCE_ID = "process-instance-id";
    private static String EPIMS = "venue-id";
    private static Long VERSION_NUMBER = 1L;

    @Test
    public void shouldPopulateCamundaProcessVariables() {
        CaseData caseData = CaseData.builder()
            .businessProcess(BusinessProcess.builder().processInstanceId(PROCESS_INSTANCE_ID).build())
            .ccdState(CASE_PROGRESSION)
            .build();
        HearingDay hearingDay = HearingDay.builder()
            .hearingStartDateTime(LocalDateTime.of(2023, 01, 01, 0, 0, 0))
            .hearingEndDateTime(LocalDateTime.of(2023, 01, 01, 12, 0, 0))
            .build();
        LocalDateTime hearingResponseDate = LocalDateTime.of(2023, 02, 02, 0, 0, 0);
        HearingGetResponse hearing = HearingGetResponse.builder()
            .hearingResponse(HearingResponse.builder().hearingDaySchedule(
                    List.of(
                        HearingDaySchedule.builder()
                            .hearingVenueEpimsId(EPIMS)
                            .hearingStartDateTime(hearingDay.getHearingStartDateTime())
                            .hearingEndDateTime(hearingDay.getHearingEndDateTime())
                            .build()))
                                 .receivedDateTime(hearingResponseDate)
                                 .build())
            .requestDetails(HearingRequestDetails.builder()
                                .versionNumber(VERSION_NUMBER)
                                .build())
            .build();
        HearingNoticeVariables inputVariables = HearingNoticeVariables.builder()
            .hearingId(HEARING_ID)
            .caseId(CASE_ID)
            .build();

        when(camundaService.getProcessVariables(PROCESS_INSTANCE_ID)).thenReturn(inputVariables);
        when(hearingsService.getHearingResponse(anyString(), anyString())).thenReturn(hearing);

        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        params.getRequest().setEventId(GENERATE_HEARING_NOTICE_HMC.name());

        handler.handle(params);

        verify(camundaService).setProcessVariables(
            PROCESS_INSTANCE_ID,
            HearingNoticeVariables.builder()
                .caseId(CASE_ID)
                .hearingId(HEARING_ID)
                .caseState(CASE_PROGRESSION.name())
                .requestVersion(VERSION_NUMBER)
                .hearingStartDateTime(hearingDay.getHearingStartDateTime())
                .hearingLocationEpims(EPIMS)
                .responseDateTime(hearingResponseDate)
                .days(List.of(hearingDay))
                .build()
        );
    }
}
