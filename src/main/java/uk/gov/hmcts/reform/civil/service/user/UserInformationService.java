package uk.gov.hmcts.reform.civil.service.user;

import lombok.RequiredArgsConstructor;
import org.camunda.bpm.extension.rest.exception.RemoteProcessEngineException;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.exceptions.CaseNotFoundException;
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
        try {
            UserInfo userInfo = userService.getUserInfo(authorization);
            List<String> roles = coreCaseUserService.getUserCaseRoles(
                caseId,
                userInfo.getUid()
            );
            if (roles.isEmpty()) {
                throw new UserNotFoundOnCaseException(userInfo.getUid());
            }
            return roles;
        } catch (RemoteProcessEngineException e) {
            throw new CaseNotFoundException();
        }
    }
}
