package uk.gov.hmcts.reform.civil.handler.callback.camunda.automatedhearingnotice;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.hearingnotice.HearingNoticeCamundaService;
import uk.gov.hmcts.reform.civil.service.hearingnotice.HearingNoticeVariables;
import uk.gov.hmcts.reform.hmc.model.unnotifiedhearings.HearingDay;
import uk.gov.hmcts.reform.hmc.model.unnotifiedhearings.PartiesNotified;
import uk.gov.hmcts.reform.hmc.model.unnotifiedhearings.PartiesNotifiedServiceData;
import uk.gov.hmcts.reform.hmc.service.HearingsService;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;

@ExtendWith(MockitoExtension.class)
class UpdateHmcPartiesNotifiedHandlerTest extends BaseCallbackHandlerTest {

    @InjectMocks
    private UpdateHmcPartiesNotifiedHandler handler;

    @Mock
    private HearingNoticeCamundaService hearingNoticeCamundaService;

    @Mock
    private HearingsService hearingsService;

    @BeforeEach
    void setUp() {
        when(hearingNoticeCamundaService.getProcessVariables(any()))
            .thenReturn(HearingNoticeVariables.builder()
                            .hearingId("HER1234")
                            .hearingLocationEpims("12345")
                            .days(List.of(HearingDay.builder()
                                      .hearingStartDateTime(LocalDateTime.of(2023, 1, 1, 0, 0, 0))
                                      .hearingEndDateTime(LocalDateTime.of(2023, 1, 1, 12, 0, 0))
                                      .build()))
                            .hearingStartDateTime(LocalDateTime.of(2022, 11, 7, 15, 15))
                            .requestVersion(10L)
                            .responseDateTime(LocalDateTime.of(2022, 10, 10, 15, 15))
                            .build());
        when(hearingsService.updatePartiesNotifiedResponse(anyString(), anyString(), anyInt(), any(), any()))
            .thenReturn(new ResponseEntity<>("Ok", HttpStatus.ACCEPTED));
    }

    @Test
    void shouldReturnExpectedCallbackResponseOnAboutToSubmit() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build().toBuilder()
            .businessProcess(BusinessProcess.builder().processInstanceId("").build())
            .build();
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

        LocalDateTime receivedDate = LocalDateTime.of(2022, 10, 10, 15, 15);
        List<HearingDay> hearingDays = List.of(HearingDay.builder()
                                             .hearingStartDateTime(LocalDateTime.of(2023, 1, 1, 0, 0, 0))
                                             .hearingEndDateTime(LocalDateTime.of(2023, 1, 1, 12, 0, 0))
                                             .build());
        PartiesNotified partiesNotified = PartiesNotified.builder()
            .serviceData(PartiesNotifiedServiceData.builder()
                             .days(hearingDays)
                             .hearingNoticeGenerated(true)
                             .hearingLocation("12345")
                             .build())
            .build();

        handler.handle(params);

        verify(hearingsService).updatePartiesNotifiedResponse("BEARER_TOKEN", "HER1234", 10, receivedDate, partiesNotified);
    }

}
