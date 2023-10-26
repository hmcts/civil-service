package uk.gov.hmcts.reform.civil.service.nexthearingdate;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.camunda.bpm.engine.RuntimeService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.civil.enums.nexthearingdate.UpdateType;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {
    JacksonAutoConfiguration.class,
    NextHearingDateCamundaService.class
})
public class NextHearingDateCamundaServiceTest {

    @Autowired
    private ObjectMapper mapper;
    @MockBean
    private RuntimeService runtimeService;
    @Autowired
    private NextHearingDateCamundaService nextHearingDateCamundaService;

    private static String PROCESS_INSTANCE_ID = "process-instance-id";

    private final LocalDateTime hearingDate = LocalDateTime.of(2023, 01, 01, 0, 0, 0);

    @Test
    void shouldReturnExpectedProcessVariables() {
        NextHearingDateVariables expected = NextHearingDateVariables.builder()
            .caseId(1L)
            .updateType(UpdateType.UPDATE)
            .hearingId("hearing-id")
            .nextHearingDate(hearingDate)
            .build();

        when(runtimeService.getVariables(PROCESS_INSTANCE_ID)).thenReturn(expected.toMap(mapper));
        NextHearingDateVariables actual = nextHearingDateCamundaService.getProcessVariables(PROCESS_INSTANCE_ID);

        assertEquals(expected, actual);
    }

    @Test
    void shouldCallRunTimeServiceSetVariablesWithExpectedVariables() {
        NextHearingDateVariables variables = NextHearingDateVariables.builder()
            .caseId(1L)
            .updateType(UpdateType.UPDATE)
            .hearingId("hearing-id")
            .nextHearingDate(hearingDate)
            .build();

        nextHearingDateCamundaService.setProcessVariables(PROCESS_INSTANCE_ID, variables);

        verify(runtimeService).setVariables(PROCESS_INSTANCE_ID, variables.toMap(mapper));
    }
}
