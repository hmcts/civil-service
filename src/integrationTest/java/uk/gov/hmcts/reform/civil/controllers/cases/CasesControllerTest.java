package uk.gov.hmcts.reform.civil.controllers.cases;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.civil.controllers.BaseIntegrationTest;
import uk.gov.hmcts.reform.civil.service.RoleAssignmentsService;
import uk.gov.hmcts.reform.ras.model.RasResponse;
import uk.gov.hmcts.reform.ras.model.RoleAssignmentResponse;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class CasesControllerTest extends BaseIntegrationTest {

    private static final String CASES_ACTOR_URL = "/cases/actors/{actorId}";
    private static final String ACTORID = "1111111";


    @MockBean
    private RoleAssignmentsService roleAssignmentsService;


    @Test
    @SneakyThrows
    public void shouldReturnRASAssignment() {
        var rasResponse = RasResponse
            .builder()
            .roleAssignmentResponse(
                List.of(RoleAssignmentResponse
                            .builder()
                            .actorId(ACTORID)
                            .build()
                )
            )
            .build();

        when(roleAssignmentsService.getRoleAssignments(anyString(), anyString()))
            .thenReturn(rasResponse);
        doGet(BEARER_TOKEN, CASES_ACTOR_URL, ACTORID)
            .andExpect(content().json(toJson(rasResponse)))
            .andExpect(status().isOk());

    }
}
