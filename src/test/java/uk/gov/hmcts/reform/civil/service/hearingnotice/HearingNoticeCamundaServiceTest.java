package uk.gov.hmcts.reform.civil.service.hearingnotice;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.camunda.bpm.engine.RuntimeService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.hmc.model.unnotifiedhearings.HearingDay;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.enums.CaseState.CASE_PROGRESSION;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {
    JacksonAutoConfiguration.class,
    HearingNoticeCamundaService.class
})
public class HearingNoticeCamundaServiceTest {

    @Autowired
    private ObjectMapper mapper;
    @MockBean
    private RuntimeService runtimeService;
    @Autowired
    private HearingNoticeCamundaService hearingNoticeCamundaService;

    private static String PROCESS_INSTANCE_ID = "process-instance-id";

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

        when(runtimeService.getVariables(PROCESS_INSTANCE_ID)).thenReturn(expected.toMap(mapper));
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
