package uk.gov.hmcts.reform.civil.utils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.service.CoreCaseUserService;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserRoleCaching {

    private final UserService userService;
    private final CoreCaseUserService coreCaseUserService;

    public List<String> getUserRoles(String bearerToken, String ccdCaseRef) {
        UserInfo userInfo = userService.getUserInfo(bearerToken);
        return coreCaseUserService.getUserCaseRoles(ccdCaseRef, userInfo.getUid());
    }
}
