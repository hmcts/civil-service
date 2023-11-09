package uk.gov.hmcts.reform.civil.utils;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.civil.service.CoreCaseUserService;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = {
    UserRoleCaching.class,
    CoreCaseUserService.class,
    UserService.class
})
public class UserRoleCachingTest {

    @MockBean
    private CoreCaseUserService coreCaseUserService;
    @MockBean
    private UserService userService;
    @Autowired
    private UserRoleCaching userRoleCaching;

    @Test
    void getUserRolesTest() {
        List<String> caseRoles = new ArrayList<>();
        caseRoles.add("[APPLICANTSOLICITORONE]");
        when(userService.getUserInfo(anyString())).thenReturn(UserInfo.builder().uid("uid").build());
        when(coreCaseUserService.getUserCaseRoles(anyString(), anyString())).thenReturn(caseRoles);
        assertThat(userRoleCaching.getUserRoles("bearerToken", "ccdcase", "keyToken"))
            .contains("[APPLICANTSOLICITORONE]");
    }

}
