package uk.gov.hmcts.reform.civil.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ras.client.RoleAssignmentsApi;
import uk.gov.hmcts.reform.ras.model.RoleAssignmentResponse;
import uk.gov.hmcts.reform.ras.model.RoleAssignmentServiceResponse;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {
    RoleAssignmentsService.class
})
class RoleAssignmentsServiceTest {

    private static final String USER_AUTH_TOKEN = "Bearer caa-user-xyz";
    private static final String ACTORID = "1111111";
    private static RoleAssignmentServiceResponse RAS_RESPONSE = RoleAssignmentServiceResponse
        .builder()
        .roleAssignmentResponse(
            List.of(RoleAssignmentResponse
                        .builder()
                        .actorId(ACTORID)
                        .build()
            )
        )
        .build();

    @MockBean
    private AuthTokenGenerator authTokenGenerator;

    @MockBean
    private RoleAssignmentsApi roleAssignmentApi;

    @Autowired
    private RoleAssignmentsService roleAssignmentsService;

    @BeforeEach
    void init() {
        clearInvocations(authTokenGenerator);
        when(authTokenGenerator.generate()).thenReturn(USER_AUTH_TOKEN);
        when(roleAssignmentApi.getRoleAssignments(anyString(), anyString(), anyString())).thenReturn(RAS_RESPONSE);
    }

    @Test
    void shouldReturn() {
        var roleAssignmentsExpected = roleAssignmentsService.getRoleAssignments(ACTORID, authTokenGenerator.generate());
        assertEquals(roleAssignmentsExpected, RAS_RESPONSE);
    }

}
