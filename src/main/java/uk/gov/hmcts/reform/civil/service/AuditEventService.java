package uk.gov.hmcts.reform.civil.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.startup.UserConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.civil.client.CoreCaseDataApiV2;
import uk.gov.hmcts.reform.civil.config.SystemUpdateUserConfiguration;
import uk.gov.hmcts.reform.civil.model.ccd.AuditEvent;
import uk.gov.hmcts.reform.civil.model.ccd.AuditEventsResponse;

import java.util.Comparator;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class AuditEventService {
    private final CoreCaseDataApiV2 caseDataApi;
    private final AuthTokenGenerator authTokenGenerator;
    private final UserService userService;
    private final SystemUpdateUserConfiguration userConfig;

    public Optional<AuditEvent> getLatestAuditEventByName(String caseId, String eventName) {
        String userToken = userService.getAccessToken(userConfig.getUserName(), userConfig.getPassword());

        String authToken = authTokenGenerator.generate();
        AuditEventsResponse auditEventsResponse
            = caseDataApi.getAuditEvents(userToken, authToken, false, caseId);

        return auditEventsResponse.getAuditEvents().stream()
            .filter(auditEvent -> eventName.equals(auditEvent.getId()))
            .max(Comparator.comparing(AuditEvent::getCreatedDate));
    }
}
