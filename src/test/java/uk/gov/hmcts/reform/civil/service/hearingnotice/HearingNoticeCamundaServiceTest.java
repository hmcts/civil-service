package uk.gov.hmcts.reform.civil.service.hearingnotice;

import com.fasterxml.jackson.databind.ObjectMapper;
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
        HearingDay hearingDay = HearingDay.builder()
            .hearingStartDateTime(LocalDateTime.of(2023, 01, 01, 0, 0, 0))
            .hearingEndDateTime(LocalDateTime.of(2023, 01, 01, 12, 0, 0))
            .build();

        HearingNoticeVariables expected = HearingNoticeVariables.builder()
            .caseId(1L)
            .hearingId("hearing-id")
            .caseState(CASE_PROGRESSION.name())
            .requestVersion(1L)
            .hearingStartDateTime(hearingDay.getHearingStartDateTime())
            .hearingLocationEpims("000000")
            .responseDateTime(LocalDateTime.of(2023, 02, 02, 0, 0, 0))
            .days(List.of(hearingDay))
            .build();

        Map<String, Object> expectedVariablesMap = expected.toMap(mapper);
        when(runtimeClient.getProcessVariables(PROCESS_INSTANCE_ID)).thenReturn(expectedVariablesMap);
        when(mapper.convertValue(any(), eq(HearingNoticeVariables.class))).thenReturn(expected);

        HearingNoticeVariables actual = hearingNoticeCamundaService.getProcessVariables(PROCESS_INSTANCE_ID);

        assertEquals(expected, actual);
    }

    @Test
    void shouldCallRunTimeServiceSetVariablesWithExpectedVariables() {
        HearingDay hearingDay = HearingDay.builder()
            .hearingStartDateTime(LocalDateTime.of(2023, 01, 01, 0, 0, 0))
            .hearingEndDateTime(LocalDateTime.of(2023, 01, 01, 12, 0, 0))
            .build();
        HearingNoticeVariables variables = HearingNoticeVariables.builder()
            .caseId(1L)
            .hearingId("hearing-id")
            .caseState(CASE_PROGRESSION.name())
            .requestVersion(1L)
            .hearingStartDateTime(hearingDay.getHearingStartDateTime())
            .hearingLocationEpims("000000")
            .responseDateTime(LocalDateTime.of(2023, 02, 02, 0, 0, 0))
            .days(List.of(hearingDay))
            .build();

        hearingNoticeCamundaService.setProcessVariables(PROCESS_INSTANCE_ID, variables);

        verify(runtimeService).setVariables(PROCESS_INSTANCE_ID, variables.toMap(mapper));
    }
}
