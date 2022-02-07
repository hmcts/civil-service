package uk.gov.hmcts.reform.civil.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ras.client.RoleAssignmentsApi;
import uk.gov.hmcts.reform.ras.model.RasResponse;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoleAssignmentsService {

    private final RoleAssignmentsApi roleAssignmentApi;
    private final AuthTokenGenerator authTokenGenerator;

    public RasResponse getRoleAssignments(String actorId,
                                          String authorization) {

        if (log.isDebugEnabled()) {
            log.debug(actorId, "Getting Role assignments for actorId {0}");
        }

        return roleAssignmentApi.getRoleAssignments(
            authorization,
            authTokenGenerator.generate(),
            actorId
        );
    }
}
