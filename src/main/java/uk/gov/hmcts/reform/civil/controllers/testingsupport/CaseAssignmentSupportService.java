package uk.gov.hmcts.reform.civil.controllers.testingsupport;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CaseAssignmentApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseAssignmentUserRole;
import uk.gov.hmcts.reform.ccd.client.model.CaseAssignmentUserRoleWithOrganisation;
import uk.gov.hmcts.reform.ccd.client.model.CaseAssignmentUserRolesRequest;
import uk.gov.hmcts.reform.civil.config.CrossAccessUserConfiguration;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static java.lang.String.format;
import static org.springframework.util.StringUtils.hasText;

@Service
@RequiredArgsConstructor
@Slf4j
public class CaseAssignmentSupportService {

    private final CaseAssignmentApi caseAssignmentApi;
    private final UserService userService;
    private final CrossAccessUserConfiguration crossAccessUserConfiguration;
    private final AuthTokenGenerator authTokenGenerator;
    private final IdamClient idamClient;

    private final Map<String, String> userIdCache = new ConcurrentHashMap<>();

    public void unAssignUserFromCases(List<String> caseIds, String organisationId, String userId) {
        String authToken = authTokenGenerator.generate();
        String caaAccessToken = getCaaAccessToken();

        List<CaseAssignmentUserRole> userRoles =
            caseAssignmentApi.getUserRoles(caaAccessToken, authToken, caseIds).getCaseAssignmentUserRoles().stream()
                .filter(role -> role.getUserId().equals(userId)).toList();

        List<CaseAssignmentUserRoleWithOrganisation> userRolesWithOrganisation =
            userRoles.stream().map(role -> CaseAssignmentUserRoleWithOrganisation.builder()
                .caseDataId(role.getCaseDataId())
                .caseRole(role.getCaseRole())
                .userId(role.getUserId())
                .organisationId(organisationId)
                .build()
            ).toList();

        CaseAssignmentUserRolesRequest request = CaseAssignmentUserRolesRequest.builder()
            .caseAssignmentUserRolesWithOrganisation(userRolesWithOrganisation).build();
        caseAssignmentApi.removeCaseUserRoles(caaAccessToken, authToken, request);
    }

    public void unAssignUserFromCasesByEmail(List<String> caseIds, String organisationId, String userEmailAddress) {
        String userId = resolveUserId(userEmailAddress)
            .orElseThrow(() -> new IllegalArgumentException(
                format("Unable to find userId for email %s", userEmailAddress)));
        unAssignUserFromCases(caseIds, organisationId, userId);
    }

    private String getCaaAccessToken() {
        return userService.getAccessToken(
            crossAccessUserConfiguration.getUserName(),
            crossAccessUserConfiguration.getPassword()
        );
    }

    private Optional<String> resolveUserId(String userEmailAddress) {
        if (!hasText(userEmailAddress)) {
            return Optional.empty();
        }

        String cacheKey = userEmailAddress.toLowerCase(Locale.ENGLISH);
        if (userIdCache.containsKey(cacheKey)) {
            return Optional.ofNullable(userIdCache.get(cacheKey));
        }

        try {
            String caaAccessToken = getCaaAccessToken();
            List<UserDetails> users = idamClient.searchUsers(caaAccessToken, format("email:%s", userEmailAddress));
            Optional<String> userId = users.stream().findFirst().map(UserDetails::getId);
            userId.ifPresent(id -> userIdCache.put(cacheKey, id));
            return userId;
        } catch (Exception ex) {
            log.error("Failed to resolve user by email {}", userEmailAddress, ex);
            return Optional.empty();
        }
    }
}
