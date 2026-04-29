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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;

import static java.lang.Long.parseLong;
import static org.springframework.util.CollectionUtils.isEmpty;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.wrapElements;

@Slf4j
@Service
@RequiredArgsConstructor
public class GenAppStateHelperService {

    private final CoreCaseDataService coreCaseDataService;
    private final CaseDetailsConverter caseDetailsConverter;
    private final LocationService locationService;

    private final ObjectMapper objectMapper;

    @Getter
    @RequiredArgsConstructor
    public enum RequiredState {
        APPLICATION_CLOSED("APPLICATION_CLOSED"),
        APPLICATION_PROCEEDS_OFFLINE("PROCEEDS_IN_HERITAGE");
        private final String state;

        public String getRequiredState() {
            return this.state;
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
        if (Objects.isNull(caseData.getGeneralApplications())) {
            return false;
        }
        caseData.getGeneralApplications().forEach(application -> {
            GeneralApplication value = application.getValue();
            if (value.getCaseLink() != null) {
                triggerEvent(Long.parseLong(value.getCaseLink().getCaseReference()), event);
            }
        });
        return true;
    }

    public CaseData updateApplicationLocationDetailsInClaim(CaseData caseData, String authToken) {
        if (Collections.isEmpty(caseData.getGeneralApplications())) {
            return caseData;
        }

        LocationRefData locationDetails = locationService.getWorkAllocationLocationDetails(
            caseData.getCaseManagementLocation().getBaseLocation(),
            authToken
        );
        caseData.setGeneralApplications(wrapElements(buildUpdatedGeneralApplications(caseData, locationDetails)));
        return caseData;
    }

    public CaseData updateApplicationDetailsInClaim(CaseData caseData,
                                                     String updatedState, RequiredState gaFlow) {
        Map<Long, GeneralApplication> generalApplicationMap = getLatestStatusOfGeneralApplication(caseData);
        if (isEmpty(generalApplicationMap)) {
            return caseData;
        }

        updateGeneralApplicationCollection(caseData.getGaDetailsMasterCollection(), generalApplicationMap, gaFlow, updatedState);
        updateGeneralApplicationCollection(caseData.getClaimantGaAppDetails(), generalApplicationMap, gaFlow, updatedState);
        updateRespondentApplicationCollection(caseData.getRespondentSolGaAppDetails(), generalApplicationMap, gaFlow, updatedState);
        updateRespondentApplicationCollection(caseData.getRespondentSolTwoGaAppDetails(), generalApplicationMap, gaFlow, updatedState);
        return caseData;
    }

    private List<GeneralApplication> buildUpdatedGeneralApplications(CaseData caseData, LocationRefData locationDetails) {
        List<GeneralApplication> generalApplications = new ArrayList<>();
        caseData.getGeneralApplications().forEach(generalApplicationElement ->
            generalApplications.add(updateGeneralApplicationLocation(caseData, locationDetails, generalApplicationElement.getValue()))
        );
        return generalApplications;
    }

    private GeneralApplication updateGeneralApplicationLocation(CaseData caseData,
                                                                LocationRefData locationDetails,
                                                                GeneralApplication generalApplication) {
        generalApplication.getCaseManagementLocation().setBaseLocation(caseData.getCaseManagementLocation().getBaseLocation());
        generalApplication.getCaseManagementLocation().setRegion(caseData.getCaseManagementLocation().getRegion());
        generalApplication.getCaseManagementLocation().setSiteName(locationDetails.getSiteName());
        generalApplication.getCaseManagementLocation().setAddress(locationDetails.getCourtAddress());
        generalApplication.getCaseManagementLocation().setPostcode(locationDetails.getPostcode());
        Map<String, Object> genAppMap = generalApplication.toMap(objectMapper);
        genAppMap.put("isCcmccLocation", YesOrNo.NO);
        return objectMapper.convertValue(genAppMap, GeneralApplication.class);
    }

    private void updateGeneralApplicationCollection(List<Element<GeneralApplicationsDetails>> applications,
                                                    Map<Long, GeneralApplication> generalApplicationMap,
                                                    RequiredState gaFlow,
                                                    String updatedState) {
        updateCollectionState(
            applications,
            generalApplicationMap,
            gaFlow,
            updatedState,
            this::applicationFilterCriteria,
            GeneralApplicationsDetails::setCaseState
        );
    }

    private void updateRespondentApplicationCollection(List<Element<GADetailsRespondentSol>> applications,
                                                       Map<Long, GeneralApplication> generalApplicationMap,
                                                       RequiredState gaFlow,
                                                       String updatedState) {
        updateCollectionState(
            applications,
            generalApplicationMap,
            gaFlow,
            updatedState,
            this::applicationFilterCriteria,
            GADetailsRespondentSol::setCaseState
        );
    }

    private <T> void updateCollectionState(List<Element<T>> applications,
                                           Map<Long, GeneralApplication> generalApplicationMap,
                                           RequiredState gaFlow,
                                           String updatedState,
                                           TriPredicate<T, Map<Long, GeneralApplication>, RequiredState> filterCriteria,
                                           BiConsumer<T, String> stateUpdater) {
        if (isEmpty(applications)) {
            return;
        }
        applications.forEach(application -> {
            if (filterCriteria.test(application.getValue(), generalApplicationMap, gaFlow)) {
                stateUpdater.accept(application.getValue(), updatedState);
            }
        });
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
                if (element.getValue().getCaseLink() != null) {
                    Long caseReference = parseLong(element.getValue().getCaseLink().getCaseReference());
                    GeneralApplication application = caseDetailsConverter.toGeneralApplication(coreCaseDataService
                            .getCase(caseReference));
                    latestStatus.put(caseReference, application);
                }
            }
        }
        return latestStatus;
    }

    @FunctionalInterface
    private interface TriPredicate<T, U, V> {
        boolean test(T value, U other, V third);
    }

}
