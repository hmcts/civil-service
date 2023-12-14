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
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.referencedata.LocationRefDataService;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDocumentBuilder;
import uk.gov.hmcts.reform.civil.service.docmosis.hearing.HearingNoticeHmcGenerator;
import uk.gov.hmcts.reform.civil.service.hearingnotice.HearingNoticeCamundaService;
import uk.gov.hmcts.reform.civil.service.hearingnotice.HearingNoticeVariables;
import uk.gov.hmcts.reform.hmc.model.hearing.HearingDaySchedule;
import uk.gov.hmcts.reform.hmc.model.hearing.HearingGetResponse;
import uk.gov.hmcts.reform.hmc.model.hearing.HearingRequestDetails;
import uk.gov.hmcts.reform.hmc.model.hearing.HearingResponse;
import uk.gov.hmcts.reform.hmc.model.unnotifiedhearings.HearingDay;
import uk.gov.hmcts.reform.hmc.service.HearingsService;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.unwrapElements;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.GENERATE_HEARING_NOTICE_HMC;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.HEARING_FORM;
import static uk.gov.hmcts.reform.civil.enums.CaseState.CASE_PROGRESSION;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.HEARING_NOTICE_HMC;

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
    @MockBean
    private HearingNoticeHmcGenerator hearingNoticeHmcGenerator;
    @MockBean
    private LocationRefDataService locationRefDataService;

    private static Long CASE_ID = 1L;
    private static String HEARING_ID = "1234";
    private static String PROCESS_INSTANCE_ID = "process-instance-id";
    private static String EPIMS = "venue-id";
    private static Long VERSION_NUMBER = 1L;
    private static final String REFERENCE_NUMBER = "000DC001";

    private static final String fileName_application = String.format(
        HEARING_NOTICE_HMC.getDocumentTitle(), REFERENCE_NUMBER);

    private static final CaseDocument CASE_DOCUMENT = CaseDocumentBuilder.builder()
        .documentName(fileName_application)
        .documentType(HEARING_FORM)
        .build();

    @Test
    public void shouldPopulateCamundaProcessVariables_andReturnExpectedCaseData() {
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
                            .hearingVenueId(EPIMS)
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

        List<LocationRefData> locations = List.of(LocationRefData.builder()
                                                      .epimmsId(EPIMS).build());
        when(locationRefDataService.getCourtLocationsForDefaultJudgments(anyString()))
            .thenReturn(locations);
        when(camundaService.getProcessVariables(PROCESS_INSTANCE_ID)).thenReturn(inputVariables);
        when(hearingsService.getHearingResponse(anyString(), anyString())).thenReturn(hearing);
        when(hearingNoticeHmcGenerator.generate(eq(caseData), eq(hearing), anyString(), anyString(), anyString())).thenReturn(List.of(CASE_DOCUMENT));

        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        params.getRequest().setEventId(GENERATE_HEARING_NOTICE_HMC.name());

        var actual = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

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

        CaseData updatedData = mapper.convertValue(actual.getData(), CaseData.class);
        assertThat(updatedData.getHearingDocuments().size()).isEqualTo(1);
        assertThat(unwrapElements(updatedData.getHearingDocuments()).get(0)).isEqualTo(CASE_DOCUMENT);
        assertThat(updatedData.getHearingDate()).isEqualTo(hearingDay.getHearingStartDateTime().toLocalDate());
        assertThat(updatedData.getHearingDueDate()).isEqualTo(LocalDate.of(2023, 1, 1));
    }

    @Test
    public void shouldPopulateCamundaProcessVariables_andReturnExpectedCaseData_BstHearingDate() {
        CaseData caseData = CaseData.builder()
            .businessProcess(BusinessProcess.builder().processInstanceId(PROCESS_INSTANCE_ID).build())
            .ccdState(CASE_PROGRESSION)
            .build();
        HearingDay hearingDay = HearingDay.builder()
            .hearingStartDateTime(LocalDateTime.of(2023, 07, 01, 9, 0, 0))
            .hearingEndDateTime(LocalDateTime.of(2023, 07, 01, 11, 0, 0))
            .build();
        LocalDateTime hearingResponseDate = LocalDateTime.of(2023, 06, 02, 0, 0, 0);
        HearingGetResponse hearing = HearingGetResponse.builder()
            .hearingResponse(HearingResponse.builder().hearingDaySchedule(
                    List.of(
                        HearingDaySchedule.builder()
                            .hearingVenueId(EPIMS)
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

        List<LocationRefData> locations = List.of(LocationRefData.builder()
                                                      .epimmsId(EPIMS).build());
        when(locationRefDataService.getCourtLocationsForDefaultJudgments(anyString()))
            .thenReturn(locations);
        when(camundaService.getProcessVariables(PROCESS_INSTANCE_ID)).thenReturn(inputVariables);
        when(hearingsService.getHearingResponse(anyString(), anyString())).thenReturn(hearing);
        when(hearingNoticeHmcGenerator.generate(eq(caseData), eq(hearing), anyString(), anyString(), anyString())).thenReturn(List.of(CASE_DOCUMENT));

        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        params.getRequest().setEventId(GENERATE_HEARING_NOTICE_HMC.name());
        var expectedHearingDays = List.of(
            HearingDay.builder()
                .hearingStartDateTime(LocalDateTime.of(2023, 07, 01, 10, 0, 0))
                .hearingEndDateTime(LocalDateTime.of(2023, 07, 01, 12, 0, 0))
                .build()
        );

        var actual = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        verify(camundaService).setProcessVariables(
            PROCESS_INSTANCE_ID,
            HearingNoticeVariables.builder()
                .caseId(CASE_ID)
                .hearingId(HEARING_ID)
                .caseState(CASE_PROGRESSION.name())
                .requestVersion(VERSION_NUMBER)
                .hearingStartDateTime(hearingDay.getHearingStartDateTime().plusHours(1))
                .hearingLocationEpims(EPIMS)
                .responseDateTime(hearingResponseDate)
                .days(expectedHearingDays)
                .build()
        );

        CaseData updatedData = mapper.convertValue(actual.getData(), CaseData.class);
        assertThat(updatedData.getHearingDocuments().size()).isEqualTo(1);
        assertThat(unwrapElements(updatedData.getHearingDocuments()).get(0)).isEqualTo(CASE_DOCUMENT);
        assertThat(updatedData.getHearingDate()).isEqualTo(hearingDay.getHearingStartDateTime().toLocalDate());
        assertThat(updatedData.getHearingDueDate()).isEqualTo(LocalDate.of(2023, 7, 1));
    }
}
