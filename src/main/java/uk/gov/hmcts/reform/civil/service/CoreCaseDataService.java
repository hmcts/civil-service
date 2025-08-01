package uk.gov.hmcts.reform.civil.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SearchResult;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.config.SystemUpdateUserConfiguration;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.CourtLocation;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.CaseLocationCivil;
import uk.gov.hmcts.reform.civil.model.dq.RequestedCourt;
import uk.gov.hmcts.reform.civil.model.search.Query;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;
import uk.gov.hmcts.reform.civil.service.data.UserAuthContent;
import uk.gov.hmcts.reform.civil.service.referencedata.LocationReferenceDataService;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.CaseDefinitionConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.civil.CaseDefinitionConstants.CMC_CASE_TYPE;
import static uk.gov.hmcts.reform.civil.CaseDefinitionConstants.GENERALAPPLICATION_CASE_TYPE;
import static uk.gov.hmcts.reform.civil.CaseDefinitionConstants.JURISDICTION;
import static uk.gov.hmcts.reform.civil.utils.CaseDataContentConverter.caseDataContentFromStartEventResponse;

@Service
@Slf4j
@RequiredArgsConstructor
public class CoreCaseDataService {

    private final ObjectMapper mapper;
    private static final Integer RETURNED_NUMBER_OF_CASES = 10;
    private final CoreCaseDataApi coreCaseDataApi;
    private final SystemUpdateUserConfiguration userConfig;
    private final AuthTokenGenerator authTokenGenerator;
    private final CaseDetailsConverter caseDetailsConverter;
    private final UserService userService;
    private final FeatureToggleService featureToggleService;
    private final LocationReferenceDataService referenceDataService;

    public void triggerEvent(Long caseId, CaseEvent eventName) {
        triggerEvent(caseId, eventName, Map.of());
    }

    public void triggerEvent(Long caseId, CaseEvent eventName, Map<String, Object> contentModified) {
        StartEventResponse startEventResponse = startUpdate(caseId.toString(), eventName);
        submitUpdate(caseId.toString(), caseDataContentFromStartEventResponse(startEventResponse, contentModified));
    }

    public void triggerEvent(Long caseId, CaseEvent eventName, Map<String, Object> contentModified,
                             String eventSummary, String eventDescription) {
        StartEventResponse startEventResponse = startUpdate(caseId.toString(), eventName);
        CaseDataContent caseDataContent = caseDataContentFromStartEventResponse(startEventResponse, contentModified);
        caseDataContent.getEvent().setSummary(eventSummary);
        caseDataContent.getEvent().setDescription(eventDescription);

        submitUpdate(caseId.toString(), caseDataContent);
    }

    public void triggerUpdateLocationEpimdsIdEvent(Long caseId, CaseEvent eventName,
                                                   String epimdsId,
                                                   String region,
                                                   String caseManagementLocationObj,
                                                   String courtLocationObj,
                                                   String applicant1DQRequestedCourtObj,
                                                   String respondent1DQRequestedCourtObj,
                                                   String eventSummary,
                                                   String eventDescription) {
        StartEventResponse startEventResponse = startUpdate(caseId.toString(), eventName);
        HashMap<String, Object> payload = new HashMap<>(startEventResponse.getCaseDetails().getData());
        List<LocationRefData> locationRefDataList = referenceDataService.getCourtLocationsByEpimmsId(
            getSystemUpdateUser().getUserToken(),
            epimdsId
        );
        LocationRefData locationRefData = locationRefDataList.get(0);
        //set case management location epimsId
        if ("Yes".equalsIgnoreCase(caseManagementLocationObj)) {
            Object caseManagementLocation = payload.get("caseManagementLocation");
            if (caseManagementLocation != null) {
                CaseLocationCivil newCmLocation = CaseLocationCivil.builder()
                    .region(region).baseLocation(epimdsId).build();
                payload.put("caseManagementLocation", newCmLocation);
            }
        }
        //set court Location epimsId
        if ("Yes".equalsIgnoreCase(courtLocationObj)) {
            Object courtLocation = payload.get("courtLocation");
            if (courtLocation != null) {
                CourtLocation existingCourtLocation = getCourtLocationObj(courtLocation);
                CourtLocation newCourtLocation = CourtLocation.builder()
                    .caseLocation(CaseLocationCivil.builder()
                                      .region(region)
                                      .baseLocation(epimdsId).build())
                    .applicantPreferredCourt(existingCourtLocation.getApplicantPreferredCourt())
                    .reasonForHearingAtSpecificCourt(existingCourtLocation.getReasonForHearingAtSpecificCourt())
                    .build();
                payload.put("courtLocation", newCourtLocation);
            }
        }
        //set applicant1 DQ RequestedCourt epimsId
        if ("Yes".equalsIgnoreCase(applicant1DQRequestedCourtObj)) {
            Object applicant1DQRequestedCourt = payload.get("applicant1DQRequestedCourt");
            if (applicant1DQRequestedCourt != null) {
                RequestedCourt existingRequestedCourt = getRequestedCourtObj(applicant1DQRequestedCourt);
                RequestedCourt newRequestedCourt = RequestedCourt.builder()
                    .caseLocation(CaseLocationCivil.builder()
                                      .region(region)
                                      .baseLocation(epimdsId).build())
                    .responseCourtCode(locationRefData.getCourtLocationCode())
                    .responseCourtName(locationRefData.getCourtName())
                    .reasonForHearingAtSpecificCourt(existingRequestedCourt.getReasonForHearingAtSpecificCourt())
                    .otherPartyPreferredSite(existingRequestedCourt.getOtherPartyPreferredSite())
                    .build();
                payload.put("applicant1DQRequestedCourt", newRequestedCourt);
            }
        }

        //set respondent1 DQ RequestedCourt epimsId
        if ("Yes".equalsIgnoreCase(respondent1DQRequestedCourtObj)) {
            Object respondent1DQRequestedCourt = payload.get("respondent1DQRequestedCourt");
            if (respondent1DQRequestedCourt != null) {
                RequestedCourt existingRequestedCourt = getRequestedCourtObj(respondent1DQRequestedCourt);
                RequestedCourt newRequestedCourt = RequestedCourt.builder()
                    .caseLocation(CaseLocationCivil.builder()
                                      .region(region)
                                      .baseLocation(epimdsId).build())
                    .responseCourtCode(locationRefData.getCourtLocationCode())
                    .responseCourtName(locationRefData.getCourtName())
                    .reasonForHearingAtSpecificCourt(existingRequestedCourt.getReasonForHearingAtSpecificCourt())
                    .otherPartyPreferredSite(existingRequestedCourt.getOtherPartyPreferredSite())
                    .build();
                payload.put("respondent1DQRequestedCourt", newRequestedCourt);
            }
        }
        //set payload
        CaseDataContent caseDataContent = caseDataContentFromStartEventResponse(startEventResponse, Map.of());
        caseDataContent.setData(payload);
        caseDataContent.getEvent().setSummary(eventSummary);
        caseDataContent.getEvent().setDescription(eventDescription);
        submitUpdate(caseId.toString(), caseDataContent);
    }

    public StartEventResponse startUpdate(String caseId, CaseEvent eventName) {
        UserAuthContent systemUpdateUser = getSystemUpdateUser();
        return coreCaseDataApi.startEventForCaseWorker(
            systemUpdateUser.getUserToken(),
            authTokenGenerator.generate(),
            systemUpdateUser.getUserId(),
            JURISDICTION,
            CASE_TYPE,
            caseId,
            eventName.name()
        );
    }

    public CaseData submitUpdate(String caseId, CaseDataContent caseDataContent) {
        UserAuthContent systemUpdateUser = getSystemUpdateUser();

        CaseDetails caseDetails = coreCaseDataApi.submitEventForCaseWorker(
            systemUpdateUser.getUserToken(),
            authTokenGenerator.generate(),
            systemUpdateUser.getUserId(),
            JURISDICTION,
            CASE_TYPE,
            caseId,
            true,
            caseDataContent
        );
        return caseDetailsConverter.toCaseData(caseDetails);
    }

    private CourtLocation getCourtLocationObj(Object object) {
        return mapper.convertValue(object, new TypeReference<>() {
        });
    }

    private RequestedCourt getRequestedCourtObj(Object object) {
        return mapper.convertValue(object, new TypeReference<>() {
        });
    }

    public CaseData triggerGeneralApplicationEvent(Long caseId, CaseEvent eventName) {
        return triggerGeneralApplicationEvent(caseId, eventName, Map.of());
    }

    public CaseData triggerGeneralApplicationEvent(Long caseId,
                                                   CaseEvent eventName,
                                                   Map<String, Object> contentModified) {
        StartEventResponse startEventResponse = startGeneralApplicationUpdate(caseId.toString(), eventName);
        return submitGeneralApplicationUpdate(caseId.toString(),
                caseDataContentFromStartEventResponse(startEventResponse, contentModified));
    }

    public StartEventResponse startGeneralApplicationUpdate(String caseId, CaseEvent eventName) {
        UserAuthContent systemUpdateUser = getSystemUpdateUser();

        return coreCaseDataApi.startEventForCaseWorker(
                systemUpdateUser.getUserToken(),
                authTokenGenerator.generate(),
                systemUpdateUser.getUserId(),
                JURISDICTION,
                GENERALAPPLICATION_CASE_TYPE,
                caseId,
                eventName.name()
        );
    }

    public CaseData submitGeneralApplicationUpdate(String caseId, CaseDataContent caseDataContent) {
        UserAuthContent systemUpdateUser = getSystemUpdateUser();

        CaseDetails caseDetails = coreCaseDataApi.submitEventForCaseWorker(
                systemUpdateUser.getUserToken(),
                authTokenGenerator.generate(),
                systemUpdateUser.getUserId(),
                JURISDICTION,
                GENERALAPPLICATION_CASE_TYPE,
                caseId,
                true,
                caseDataContent
        );
        return caseDetailsConverter.toGACaseData(caseDetails);
    }

    public SearchResult searchCases(Query query, String authorization) {
        return coreCaseDataApi.searchCases(authorization, authTokenGenerator.generate(), CASE_TYPE, query.toString());
    }

    public SearchResult searchCases(Query query) {
        String userToken = userService.getAccessToken(userConfig.getUserName(), userConfig.getPassword());
        String searchString = query.toString();
        log.info("Searching Elasticsearch with query: " + searchString);
        return coreCaseDataApi.searchCases(userToken, authTokenGenerator.generate(), CASE_TYPE, searchString);
    }

    public SearchResult searchMediationCases(Query query) {
        String userToken = userService.getAccessToken(userConfig.getUserName(), userConfig.getPassword());
        String searchString = query.toMediationQueryString();
        log.info("Searching Elasticsearch with mediation query: " + searchString);
        return coreCaseDataApi.searchCases(userToken, authTokenGenerator.generate(), CASE_TYPE, searchString);
    }

    public SearchResult searchCMCCases(Query query) {
        String userToken = userService.getAccessToken(userConfig.getUserName(), userConfig.getPassword());
        String searchString = query.toString();
        log.info("Searching Elasticsearch with query: " + searchString);
        return coreCaseDataApi.searchCases(userToken, authTokenGenerator.generate(), CMC_CASE_TYPE, searchString);
    }

    public CaseDetails getCase(Long caseId) {
        String userToken = userService.getAccessToken(userConfig.getUserName(), userConfig.getPassword());
        return coreCaseDataApi.getCase(userToken, authTokenGenerator.generate(), caseId.toString());
    }

    public CaseDetails getCase(Long caseId, String authorisation) {
        return coreCaseDataApi.getCase(authorisation, authTokenGenerator.generate(), caseId.toString());
    }

    private UserAuthContent getSystemUpdateUser() {
        String userToken = userService.getAccessToken(userConfig.getUserName(), userConfig.getPassword());
        String userId = userService.getUserInfo(userToken).getUid();
        return UserAuthContent.builder().userToken(userToken).userId(userId).build();
    }

    public CaseDetails setSupplementaryData(Long caseId, Map<String, Map<String,
        Map<String, Object>>> supplementaryData) {
        UserAuthContent systemUpdateUser = getSystemUpdateUser();

        return coreCaseDataApi.submitSupplementaryData(systemUpdateUser.getUserToken(), authTokenGenerator.generate(),
                                                       caseId.toString(), supplementaryData
        );
    }

    public LocalDate getAgreedDeadlineResponseDate(Long caseId, String authorization) {
        CaseData caseData = caseDetailsConverter.toCaseData(this.getCase(caseId, authorization));
        if (caseData.getRespondentSolicitor1AgreedDeadlineExtension() != null) {
            return caseData.getRespondent1ResponseDeadline().toLocalDate();
        }
        return null;
    }

    public SearchResult getCCDClaimsForLipClaimant(String authorization, int startIndex) {
        log.info("-----------calling CCD lip claimant claims-------------");
        SearchResult claims = getCCDDataBasedOnIndex(authorization, startIndex, "data.claimantUserDetails.email");
        log.info("-----------total lip claimant claims received -------------" + claims.getCases().size());
        return claims;
    }

    public SearchResult getCCDClaimsForLipDefendant(String authorization, int startIndex) {
        log.info("-----------calling CCD lip defendant claims-------------");
        SearchResult claims = getCCDDataBasedOnIndex(authorization, startIndex, "data.defendantUserDetails.email");
        log.info("-----------total lip defendant claims received -------------" + claims.getCases().size());
        return claims;
    }

    public SearchResult getCCDDataBasedOnIndex(String authorization, int startIndex, String userEmailField) {
        String query = createQuery(authorization, startIndex, userEmailField);
        return coreCaseDataApi.searchCases(authorization, authTokenGenerator.generate(), CASE_TYPE, query);
    }

    private String createQuery(String authorization, int startIndex, String userEmailField) {
        if (featureToggleService.isLipVLipEnabled()) {
            UserDetails defendantInfo = userService.getUserDetails(authorization);
            return new SearchSourceBuilder()
                .query(QueryBuilders.boolQuery()
                           .must(QueryBuilders.termQuery(userEmailField, defendantInfo.getEmail())))
                .sort("data.submittedDate", SortOrder.DESC)
                .from(startIndex)
                .size(RETURNED_NUMBER_OF_CASES).toString();
        }
        return new SearchSourceBuilder()
            .query(QueryBuilders.matchAllQuery())
            .sort("data.submittedDate", SortOrder.DESC)
            .from(startIndex)
            .size(RETURNED_NUMBER_OF_CASES).toString();
    }
}
