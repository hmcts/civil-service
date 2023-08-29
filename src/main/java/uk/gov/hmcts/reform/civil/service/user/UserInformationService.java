package uk.gov.hmcts.reform.civil.service.user;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.exceptions.CaseNotFoundException;
import uk.gov.hmcts.reform.civil.service.CoreCaseUserService;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserInformationService {

    Logger log = LoggerFactory.getLogger(UserInformationService.class);

    private final UserService userService;
    private final CoreCaseUserService coreCaseUserService;

    public List<String> getUserCaseRoles(String caseId, String authorization) {
        try {
            UserInfo userInfo = userService.getUserInfo(authorization);
            List<String> roles = coreCaseUserService.getUserCaseRoles(
                caseId,
                userInfo.getUid()
            );
            return roles;
        } catch (Exception e) {
            log.error(String.format("No case found for %s", caseId));
            throw new CaseNotFoundException();
        }
    }
}
