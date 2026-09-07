package uk.gov.hmcts.reform.civil.service.user;

import lombok.RequiredArgsConstructor;
import org.camunda.community.rest.exception.RemoteProcessEngineException;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.exceptions.UpstreamUnavailableException;
import uk.gov.hmcts.reform.civil.exceptions.UserNotFoundOnCaseException;
import uk.gov.hmcts.reform.civil.service.CoreCaseUserService;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserInformationService {

    private final UserService userService;
    private final CoreCaseUserService coreCaseUserService;

    public List<String> getUserCaseRoles(String caseId, String authorization) {
        UserInfo userInfo = userService.getUserInfo(authorization);
        try {
            List<String> roles = coreCaseUserService.getUserCaseRoles(
                caseId,
                userInfo.getUid()
            );
            if (roles.isEmpty()) {
                throw new UserNotFoundOnCaseException(userInfo.getUid());
            }
            return roles;
        } catch (RemoteProcessEngineException e) {
            throw new UpstreamUnavailableException("CCD case-users", caseId, userInfo.getUid(), e);
        }
    }
}
