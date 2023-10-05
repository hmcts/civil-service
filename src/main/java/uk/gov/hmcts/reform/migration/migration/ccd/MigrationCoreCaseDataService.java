package uk.gov.hmcts.reform.migration.migration.ccd;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.Event;
import uk.gov.hmcts.reform.ccd.client.model.PaginatedSearchMetadata;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.User;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.migration.migration.MigrationProperties;
import uk.gov.hmcts.reform.migration.migration.auth.AuthUtil;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
public class MigrationCoreCaseDataService {

    @Autowired
    private IdamClient idamClient;
    @Autowired
    private AuthTokenGenerator authTokenGenerator;
    @Autowired
    private CoreCaseDataApi coreCaseDataApi;
    @Autowired
    private MigrationProperties migrationProperties;

    public Optional<CaseDetails> fetchOne(String authorisation, String caseId) {
        try {
            return Optional.ofNullable(coreCaseDataApi.getCase(authorisation, authTokenGenerator.generate(), caseId));
        } catch (Exception ex) {
            log.error("Case {} not found due to: {}", caseId, ex.getMessage());
        }
        return Optional.empty();
    }

    public int getNumberOfPages(String authorisation, String userId, Map<String, String> searchCriteria) {
        PaginatedSearchMetadata metadata = coreCaseDataApi.getPaginationInfoForSearchForCaseworkers(
            authorisation,
            authTokenGenerator.generate(),
            userId,
            migrationProperties.getJurisdiction(),
            migrationProperties.getCaseType(),
            searchCriteria
        );
        return metadata.getTotalPagesCount();
    }

    public List<CaseDetails> fetchPage(String authorisation, String userId, int pageNumber) {
        try {
            Map<String, String> searchCriteria = new HashMap<>();
            searchCriteria.put("page", String.valueOf(pageNumber));
            return coreCaseDataApi.searchForCaseworker(authorisation, authTokenGenerator.generate(), userId,
                                                       migrationProperties.getJurisdiction(),
                                                       migrationProperties.getCaseType(), searchCriteria
            );
        } catch (Exception e) {
            log.error("Fetching of cases failed for the page no {} due to: {}", pageNumber, e.getMessage());
        }
        return Collections.emptyList();
    }

    public CaseDetails update(String authorisation, String eventId,
                              String eventSummary,
                              String eventDescription,
                              String caseType,
                              CaseDetails caseDetails) {
        String caseId = String.valueOf(caseDetails.getId());
        UserDetails userDetails = idamClient.getUserDetails(AuthUtil.getBearerToken(authorisation));

        StartEventResponse startEventResponse = coreCaseDataApi.startEventForCaseWorker(
            AuthUtil.getBearerToken(authorisation),
            authTokenGenerator.generate(),
            userDetails.getId(),
            caseDetails.getJurisdiction(),
            caseType,
            caseId,
            eventId
        );

        CaseDataContent caseDataContent = CaseDataContent.builder()
            .eventToken(startEventResponse.getToken())
            .event(
                Event.builder()
                    .id(startEventResponse.getEventId())
                    .summary(eventSummary)
                    .description(eventDescription)
                    .build()
            ).data(caseDetails.getData())
            .build();

        return coreCaseDataApi.submitEventForCaseWorker(
            AuthUtil.getBearerToken(authorisation),
            authTokenGenerator.generate(),
            userDetails.getId(),
            caseDetails.getJurisdiction(),
            caseType,
            caseId,
            true,
            caseDataContent
        );
    }

    public CaseDetails update(User user,
                              CaseDetails caseDetails,
                              String caseType,
                              String eventId,
                              String eventSummary,
                              String eventDescription,
                              Map<String, Object> migratedData) {

        UserDetails userDetails = user.getUserDetails();
        String authorisation = user.getAuthToken();
        String caseId = caseDetails.getId().toString();
        StartEventResponse startEventResponse = coreCaseDataApi.startEventForCaseWorker(
            AuthUtil.getBearerToken(authorisation),
            authTokenGenerator.generate(),
            userDetails.getId(),
            caseDetails.getJurisdiction(),
            caseType,
            caseId,
            eventId);

        var payload = new HashMap<>(startEventResponse.getCaseDetails().getData());
        payload.putAll(migratedData);
        CaseDataContent caseDataContent = CaseDataContent.builder()
            .eventToken(startEventResponse.getToken())
            .event(
                Event.builder()
                    .id(startEventResponse.getEventId())
                    .summary(eventSummary)
                    .description(eventDescription)
                    .build()
            ).data(payload)
            .build();

        return coreCaseDataApi.submitEventForCaseWorker(
            AuthUtil.getBearerToken(authorisation),
            authTokenGenerator.generate(),
            userDetails.getId(),
            caseDetails.getJurisdiction(),
            caseType,
            caseId,
            true,
            caseDataContent);
    }
}
