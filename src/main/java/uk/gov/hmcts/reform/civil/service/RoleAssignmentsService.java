package uk.gov.hmcts.reform.civil.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.civil.config.TestUserConfiguration;
import uk.gov.hmcts.reform.ras.client.RoleAssignmentsApi;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.ras.model.RoleAssignmentResponse;

@Service
@RequiredArgsConstructor
public class RoleAssignmentsService {

    Logger log = LoggerFactory.getLogger(RoleAssignmentsService.class);

    private final RoleAssignmentsApi roleAssignmentApi;

    public RoleAssignmentResponse getRoleAssignments(String actorId,
                                                     String authorization,
                                                     String serviceAuthorization) {
        if (log.isDebugEnabled()) {
            log.debug(actorId, "Getting Role assignments for actorId {0}");
        }
        return roleAssignmentApi.getRoleAssignments(
            authorization,
            serviceAuthorization,
            actorId
        );
    }




}
