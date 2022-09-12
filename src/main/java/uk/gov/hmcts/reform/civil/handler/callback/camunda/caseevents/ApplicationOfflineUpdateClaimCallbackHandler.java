package uk.gov.hmcts.reform.civil.handler.callback.camunda.caseevents;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.launchdarkly.FeatureToggleService;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.genapplication.GADetailsRespondentSol;
import uk.gov.hmcts.reform.civil.model.genapplication.GeneralApplication;
import uk.gov.hmcts.reform.civil.model.genapplication.GeneralApplicationsDetails;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.Long.parseLong;
import static org.springframework.util.CollectionUtils.isEmpty;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.APPLICATION_OFFLINE_UPDATE_CLAIM;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApplicationOfflineUpdateClaimCallbackHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = List.of(APPLICATION_OFFLINE_UPDATE_CLAIM);

    private static final List<String> NON_LIVE_STATES = List.of(
            "Application Closed",
            "Proceeds In Heritage",
            "Order Made",
            "Application Dismissed"
    );
    private static final String APPLICATION_PROCEEDS_OFFLINE = "PROCEEDS_IN_HERITAGE";
    private static final String APPLICATION_PROCEEDS_OFFLINE_DESCRIPTION = "Proceeds In Heritage";

    private final CoreCaseDataService coreCaseDataService;
    private final CaseDetailsConverter caseDetailsConverter;
    private final ObjectMapper objectMapper;
    private final FeatureToggleService featureToggle;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
                callbackKey(ABOUT_TO_START), this::validateRequest,
                callbackKey(ABOUT_TO_SUBMIT), this::triggerGeneralApplicationClosure
        );
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse validateRequest(CallbackParams callbackParams) {
        if (!featureToggle.isGeneralApplicationsEnabled()) {
            List<String> errors = new ArrayList<>();
            errors.add("Invalid request since the general application feature is toggled off");
            return AboutToStartOrSubmitCallbackResponse.builder()
                    .errors(errors)
                    .build();
        }
        return emptyCallbackResponse(callbackParams);
    }

    private CallbackResponse triggerGeneralApplicationClosure(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();

        List<Element<GeneralApplicationsDetails>> gaDetails = caseData.getGeneralApplicationsDetails();
        List<Element<GADetailsRespondentSol>> respondentSpecficGADetails = caseData.getGaDetailsRespondentSol();
        List<Element<GADetailsRespondentSol>> respondentSpecficGADetailsTwo = caseData.getGaDetailsRespondentSolTwo();

        Map<Long, GeneralApplication> generalApplicationMap = getLatestStatusOfGeneralApplication(caseData);

        if (!isEmpty(gaDetails)) {
            gaDetails.forEach(gaDetails1 -> {
                if (applicationFilterCriteria(gaDetails1.getValue(), generalApplicationMap)) {
                    gaDetails1.getValue().setCaseState(APPLICATION_PROCEEDS_OFFLINE_DESCRIPTION);
                }
            });
        }
        if (!isEmpty(respondentSpecficGADetails)) {
            respondentSpecficGADetails.forEach(respondentSolElement -> {
                if (applicationFilterCriteria(respondentSolElement.getValue(), generalApplicationMap)) {
                    respondentSolElement.getValue().setCaseState(APPLICATION_PROCEEDS_OFFLINE_DESCRIPTION);
                }
            });
        }
        if (!isEmpty(respondentSpecficGADetailsTwo)) {
            respondentSpecficGADetailsTwo.forEach(respondentSolElementTwo -> {
                if (applicationFilterCriteria(respondentSolElementTwo.getValue(), generalApplicationMap)) {
                    respondentSolElementTwo.getValue().setCaseState(APPLICATION_PROCEEDS_OFFLINE_DESCRIPTION);
                }
            });
        }

        return AboutToStartOrSubmitCallbackResponse.builder().data(caseData.toMap(objectMapper)).build();
    }

    private boolean applicationFilterCriteria(GeneralApplicationsDetails gaDetails,
                                              Map<Long, GeneralApplication> generalApplicationMap) {
        if (gaDetails != null
                && gaDetails.getCaseLink() != null
                && !NON_LIVE_STATES.contains(gaDetails.getCaseState())) {
            long caseId = parseLong(gaDetails.getCaseLink().getCaseReference());
            return isGeneralApplicationCaseStatusUpdated(caseId, generalApplicationMap);
        }
        return false;
    }

    private boolean applicationFilterCriteria(GADetailsRespondentSol gaDetailsRespondentSol,
                                              Map<Long, GeneralApplication> generalApplicationMap) {
        boolean isLive = gaDetailsRespondentSol != null
                && gaDetailsRespondentSol.getCaseLink() != null
                && !NON_LIVE_STATES.contains(gaDetailsRespondentSol.getCaseState());
        if (isLive) {
            long caseId = parseLong(gaDetailsRespondentSol.getCaseLink().getCaseReference());
            return isGeneralApplicationCaseStatusUpdated(caseId, generalApplicationMap);
        }
        return false;
    }

    private boolean isGeneralApplicationCaseStatusUpdated(long caseId,
                                                          Map<Long, GeneralApplication> generalApplicationMap) {
        return generalApplicationMap != null
                && generalApplicationMap.containsKey(caseId)
                && APPLICATION_PROCEEDS_OFFLINE.equals(generalApplicationMap.get(caseId).getGeneralApplicationState())
                && (generalApplicationMap.get(caseId).getApplicationTakenOfflineDate() != null);
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
