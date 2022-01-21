package uk.gov.hmcts.reform.civil.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.civil.config.TestUserConfiguration;
import uk.gov.hmcts.reform.civil.ras.client.RoleAssignmentApi;
import uk.gov.hmcts.reform.idam.client.IdamClient;

@Service
@RequiredArgsConstructor
public class RoleAssignmentService {

    Logger log = LoggerFactory.getLogger(RoleAssignmentService.class);

    private final RoleAssignmentApi roleAssignmentApi;
    private final IdamClient idamClient;
    private final TestUserConfiguration userConfig;
    private final AuthTokenGenerator authTokenGenerator;

    public String getRoleAssignments(String actorId) {
        if (log.isDebugEnabled()) {
            log.debug(actorId, "Getting Role assignments for actorId {0}");
        }
        return roleAssignmentApi.getRoleAssignments(
            //MediaType.ALL_VALUE,
            getUserToken(),
            authTokenGenerator.generate(),
            actorId
        );
    }

    public String getRoleAssignments() {
        return this.getRoleAssignments(getActorId());
    }


    private String getActorId() {
        return idamClient.getUserDetails(getUserToken()).getId();
    }

    private String getUserToken() {
        return idamClient.getAccessToken(userConfig.getUsername(), userConfig.getPassword());
    }
}
