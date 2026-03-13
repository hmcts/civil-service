package uk.gov.hmcts.reform.civil.service.hearingnotice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.DeserializationFeature;
import org.camunda.bpm.engine.RuntimeService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.service.camunda.CamundaRuntimeClient;
import uk.gov.hmcts.reform.hmc.model.unnotifiedhearings.HearingDay;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.civil.enums.CaseState.CASE_PROGRESSION;

@ExtendWith(MockitoExtension.class)
public class HearingNoticeCamundaServiceTest {

    @Mock
    private ObjectMapper mapper;

    @Mock
    private RuntimeService runtimeService;

    @Mock
    private CamundaRuntimeClient runtimeClient;

    @InjectMocks
    private HearingNoticeCamundaService hearingNoticeCamundaService;

    @Mock
    private CaseDetailsConverter caseDetailsConverter;

    private static final String PROCESS_INSTANCE_ID = "process-instance-id";

    @Test
    void shouldReturnExpectedProcessVariables() {
        HearingDay hearingDay = new HearingDay()
            .setHearingStartDateTime(LocalDateTime.of(2023, 01, 01, 0, 0, 0))
            .setHearingEndDateTime(LocalDateTime.of(2023, 01, 01, 12, 0, 0));

        HearingNoticeVariables expected = new HearingNoticeVariables()
            .setCaseId(1L)
            .setHearingId("hearing-id")
            .setCaseState(CASE_PROGRESSION.name())
            .setRequestVersion(1L)
            .setHearingStartDateTime(hearingDay.getHearingStartDateTime())
            .setHearingLocationEpims("000000")
            .setResponseDateTime(LocalDateTime.of(2023, 02, 02, 0, 0, 0))
            .setDays(List.of(hearingDay));

        Map<String, Object> expectedVariablesMap = expected.toMap(mapper);
        when(runtimeClient.getProcessVariables(PROCESS_INSTANCE_ID)).thenReturn(expectedVariablesMap);
        when(mapper.convertValue(any(), eq(HearingNoticeVariables.class))).thenReturn(expected);

        HearingNoticeVariables actual = hearingNoticeCamundaService.getProcessVariables(PROCESS_INSTANCE_ID);

        assertEquals(expected, actual);
    }

    @Test
    void shouldCallRunTimeServiceSetVariablesWithExpectedVariables() {
        HearingDay hearingDay = new HearingDay()
            .setHearingStartDateTime(LocalDateTime.of(2023, 01, 01, 0, 0, 0))
            .setHearingEndDateTime(LocalDateTime.of(2023, 01, 01, 12, 0, 0));

        HearingNoticeVariables variables = new HearingNoticeVariables()
            .setCaseId(1L)
            .setHearingId("hearing-id")
            .setCaseState(CASE_PROGRESSION.name())
            .setRequestVersion(1L)
            .setHearingStartDateTime(hearingDay.getHearingStartDateTime())
            .setHearingLocationEpims("000000")
            .setResponseDateTime(LocalDateTime.of(2023, 02, 02, 0, 0, 0))
            .setDays(List.of(hearingDay));

        hearingNoticeCamundaService.setProcessVariables(PROCESS_INSTANCE_ID, variables);

        verify(runtimeService).setVariables(PROCESS_INSTANCE_ID, variables.toMap(mapper));
    }

    @Test
    void shouldIgnoreUnknownCamundaVariablesWhenDeserializing() {
        ObjectMapper realMapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        HearingNoticeCamundaService service = new HearingNoticeCamundaService(runtimeService, runtimeClient, realMapper);
        when(runtimeClient.getProcessVariables(PROCESS_INSTANCE_ID)).thenReturn(Map.of(
            "flowState", "MAIN.SOME_STATE",
            "caseId", 123L,
            "hearingId", "hearing-id"
        ));

        HearingNoticeVariables variables = assertDoesNotThrow(() -> service.getProcessVariables(PROCESS_INSTANCE_ID));

        assertEquals(123L, variables.getCaseId());
        assertEquals("hearing-id", variables.getHearingId());
    }
}
