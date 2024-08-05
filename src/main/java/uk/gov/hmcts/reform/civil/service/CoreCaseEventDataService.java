package uk.gov.hmcts.reform.civil.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CaseEventsApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseEventDetail;
import uk.gov.hmcts.reform.civil.config.SystemUpdateUserConfiguration;
import uk.gov.hmcts.reform.civil.service.data.UserAuthContent;

import java.util.List;

import static uk.gov.hmcts.reform.civil.CaseDefinitionConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.civil.CaseDefinitionConstants.JURISDICTION;

@Service
@Slf4j
@RequiredArgsConstructor
public class CoreCaseEventDataService {

    private final CaseEventsApi caseEventsApi;
    private final SystemUpdateUserConfiguration userConfig;
    private final AuthTokenGenerator authTokenGenerator;
    private final UserService userService;

    private UserAuthContent getSystemUpdateUser() {
        String userToken = userService.getAccessToken(userConfig.getUserName(), userConfig.getPassword());
        String userId = userService.getUserInfo(userToken).getUid();
        return UserAuthContent.builder().userToken(userToken).userId(userId).build();
    }

    public List<CaseEventDetail> getEventsForCase(String caseId) {
        UserAuthContent systemUpdateUser = getSystemUpdateUser();

        return caseEventsApi.findEventDetailsForCase(systemUpdateUser.getUserToken(),
                                              authTokenGenerator.generate(),
                                              systemUpdateUser.getUserId(),
                                              JURISDICTION,
                                              CASE_TYPE,
                                              caseId);
    }

}
