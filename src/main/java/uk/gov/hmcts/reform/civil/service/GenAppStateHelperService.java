package uk.gov.hmcts.reform.civil.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.lang.Collections;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.genapplication.GADetailsRespondentSol;
import uk.gov.hmcts.reform.civil.model.genapplication.GeneralApplication;
import uk.gov.hmcts.reform.civil.model.genapplication.GeneralApplicationsDetails;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;

import java.util.*;

import static java.lang.Long.parseLong;
import static org.springframework.util.CollectionUtils.isEmpty;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.wrapElements;

@Slf4j
@Service
@RequiredArgsConstructor
public class GenAppStateHelperService {

    private final CoreCaseDataService coreCaseDataService;
    private final CaseDetailsConverter caseDetailsConverter;
    private final InitiateGeneralApplicationService genAppService;

    private final ObjectMapper objectMapper;

    @Getter
    @RequiredArgsConstructor
    public enum RequiredState {
        APPLICATION_CLOSED("APPLICATION_CLOSED"),
        APPLICATION_PROCEEDS_OFFLINE("PROCEEDS_IN_HERITAGE");
        private final String requiredState;

        public String getRequiredState() {
            return this.requiredState;
        }
    }

    private static final List<String> NON_LIVE_STATES = List.of(
            "Application Closed",
            "Proceeds In Heritage",
            "Order Made",
            "Application Dismissed"
    );

    private void triggerEvent(Long caseId, CaseEvent event) {
        log.info("Triggering {} event on case: [{}]",
                event,
                caseId);
        coreCaseDataService.triggerGeneralApplicationEvent(caseId, event);
    }

    public boolean triggerEvent(CaseData caseData, CaseEvent event) {
        caseData.getGeneralApplications()
                .forEach(application ->
                        triggerEvent(
                                parseLong(application.getValue().getCaseLink().getCaseReference()),
                                event));
        return true;
    }

    public CaseData updateApplicationLocationDetailsInClaim(CaseData caseData, String authToken) {

        if (!Collections.isEmpty(caseData.getGeneralApplications())) {
            List<GeneralApplication> genApps = new ArrayList<>();
            LocationRefData locationDetails = genAppService.getWorkAllocationLocationDetails(caseData.getCaseManagementLocation().getBaseLocation(), authToken);
            CaseData finalCaseData = caseData;
            caseData.getGeneralApplications().forEach(generalApplicationElement -> {
                GeneralApplication generalApplication = generalApplicationElement.getValue();
                generalApplication.getCaseManagementLocation().setBaseLocation(finalCaseData.getCaseManagementLocation().getBaseLocation());
                generalApplication.getCaseManagementLocation().setRegion(finalCaseData.getCaseManagementLocation().getRegion());
                generalApplication.getCaseManagementLocation().setSiteName(locationDetails.getSiteName());
                generalApplication.getCaseManagementLocation().setAddress(locationDetails.getCourtAddress());
                generalApplication.getCaseManagementLocation().setPostcode(locationDetails.getPostcode());
                Map<String, Object> genAppMap = generalApplication.toMap(objectMapper);
                genAppMap.put("isCcmccLocation", YesOrNo.NO);
                generalApplication = objectMapper.convertValue(genAppMap, GeneralApplication.class);
                genApps.add((generalApplication));
            });
            caseData = caseData.toBuilder().generalApplications(wrapElements(genApps)).build();
        }
        return caseData;
    }

    public CaseData updateApplicationDetailsInClaim(CaseData caseData,
                                                     String updatedState, RequiredState gaFlow) {
        List<Element<GeneralApplicationsDetails>> gaDetails = caseData.getClaimantGaAppDetails();
        List<Element<GeneralApplicationsDetails>> gaDetailsMasterCollection = caseData.getGaDetailsMasterCollection();

        Map<Long, GeneralApplication> generalApplicationMap = getLatestStatusOfGeneralApplication(caseData);
        /*
        * Master GA collection for Judge, case worker, legal adviser etc..
        * */
        if (!isEmpty(gaDetailsMasterCollection)) {
            gaDetailsMasterCollection.forEach(gaMasterColl -> {
                if (applicationFilterCriteria(gaMasterColl.getValue(), generalApplicationMap, gaFlow)) {
                    gaMasterColl.getValue().setCaseState(updatedState);
                }
            });
        }
        /*
        * Claimant GA Collection
        * */
        if (!isEmpty(gaDetails)) {
            gaDetails.forEach(gaDetails1 -> {
                if (applicationFilterCriteria(gaDetails1.getValue(), generalApplicationMap, gaFlow)) {
                    gaDetails1.getValue().setCaseState(updatedState);
                }
            });
        }

        /*
        * Respondent one GA collection
        * */
        List<Element<GADetailsRespondentSol>> respondentSpecficGADetails = caseData.getRespondentSolGaAppDetails();

        if (!isEmpty(respondentSpecficGADetails)) {
            respondentSpecficGADetails.forEach(respondentSolElement -> {
                if (applicationFilterCriteria(respondentSolElement.getValue(), generalApplicationMap, gaFlow)) {
                    respondentSolElement.getValue().setCaseState(updatedState);
                }
            });
        }

        /*
        * Respondent two GA collection
        * */
        List<Element<GADetailsRespondentSol>> respondentTwoGADetails = caseData.getRespondentSolTwoGaAppDetails();

        if (!isEmpty(respondentTwoGADetails)) {
            respondentTwoGADetails.forEach(respondentTwoSolElement -> {
                if (applicationFilterCriteria(respondentTwoSolElement.getValue(), generalApplicationMap, gaFlow)) {
                    respondentTwoSolElement.getValue().setCaseState(updatedState);
                }
            });
        }
        return caseData;
    }

    private boolean applicationFilterCriteria(GeneralApplicationsDetails gaDetails,
                                              Map<Long, GeneralApplication> generalApplicationMap,
                                              RequiredState gaFlow) {
        if (gaDetails != null
                && gaDetails.getCaseLink() != null
                && isLive(gaDetails.getCaseState())) {
            long caseId = parseLong(gaDetails.getCaseLink().getCaseReference());
            return isGeneralApplicationCaseStatusUpdated(caseId, generalApplicationMap, gaFlow);
        }
        return false;
    }

    private boolean applicationFilterCriteria(GADetailsRespondentSol gaDetailsRespondentSol,
                                              Map<Long, GeneralApplication> generalApplicationMap,
                                              RequiredState gaFlow) {
        if (gaDetailsRespondentSol != null
                && gaDetailsRespondentSol.getCaseLink() != null
                && isLive(gaDetailsRespondentSol.getCaseState())) {
            long caseId = parseLong(gaDetailsRespondentSol.getCaseLink().getCaseReference());
            return isGeneralApplicationCaseStatusUpdated(caseId, generalApplicationMap, gaFlow);
        }
        return false;
    }

    private boolean isLive(String applicationState) {
        return !NON_LIVE_STATES.contains(applicationState);
    }

    private boolean isGeneralApplicationCaseStatusUpdated(long caseId,
                                                          Map<Long, GeneralApplication> generalApplicationMap,
                                                          RequiredState gaFlow) {
        return generalApplicationMap != null
                && generalApplicationMap.containsKey(caseId)
                && (
                        (gaFlow.equals(RequiredState.APPLICATION_CLOSED)
                                && RequiredState.APPLICATION_CLOSED.getRequiredState()
                                .equals(generalApplicationMap.get(caseId).getGeneralApplicationState())
                                && generalApplicationMap.get(caseId).getApplicationClosedDate() != null)
                     || (gaFlow.equals(RequiredState.APPLICATION_PROCEEDS_OFFLINE)
                                && RequiredState.APPLICATION_PROCEEDS_OFFLINE.getRequiredState()
                                .equals(generalApplicationMap.get(caseId).getGeneralApplicationState())
                                && generalApplicationMap.get(caseId).getApplicationTakenOfflineDate() != null)
                   );
    }

    private Map<Long, GeneralApplication> getLatestStatusOfGeneralApplication(CaseData caseData) {
        Map<Long, GeneralApplication> latestStatus = new HashMap<>();
        if (caseData.getGeneralApplications() != null && !caseData.getGeneralApplications().isEmpty()) {
            for (Element<GeneralApplication> element : caseData.getGeneralApplications()) {
                Long caseReference = parseLong(element.getValue().getCaseLink().getCaseReference());
                GeneralApplication application = caseDetailsConverter.toGeneralApplication(coreCaseDataService
                        .getCase(caseReference));
                latestStatus.put(caseReference, application);
            }
        }
        return latestStatus;
    }

}
