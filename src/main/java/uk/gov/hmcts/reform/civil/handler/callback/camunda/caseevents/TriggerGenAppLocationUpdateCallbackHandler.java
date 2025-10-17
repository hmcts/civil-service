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
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.GaCallbackDataUtil;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.GenAppStateHelperService;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.TRIGGER_LOCATION_UPDATE;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.TRIGGER_TASK_RECONFIG;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.TRIGGER_TASK_RECONFIG_GA;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.TRIGGER_UPDATE_GA_LOCATION;

@Slf4j
@Service
@RequiredArgsConstructor
public class TriggerGenAppLocationUpdateCallbackHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = List.of(TRIGGER_UPDATE_GA_LOCATION,
                                                          TRIGGER_TASK_RECONFIG_GA);

    private final GenAppStateHelperService helperService;
    private final FeatureToggleService featureToggleService;
    private final ObjectMapper objectMapper;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_SUBMIT), this::triggerGaEvent
        );
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse triggerGaEvent(CallbackParams callbackParams) {
        GeneralApplicationCaseData gaCaseData = GaCallbackDataUtil.resolveGaCaseData(callbackParams, objectMapper);
        CaseData caseData = GaCallbackDataUtil.mergeToCaseData(gaCaseData, callbackParams.getCaseData(), objectMapper);
        if (caseData == null) {
            throw new IllegalArgumentException("Case data missing from callback params");
        }

        String authToken = callbackParams.getParams().get(BEARER_TOKEN).toString();
        try {
            boolean hasGaApplications = gaCaseData != null
                && !io.jsonwebtoken.lang.Collections.isEmpty(gaCaseData.getGeneralApplications());
            boolean hasCaseApplications = !io.jsonwebtoken.lang.Collections.isEmpty(caseData.getGeneralApplications());
            boolean shouldSetEaFlag = !(featureToggleService.isGaForLipsEnabledAndLocationWhiteListed(
                caseData.getCaseManagementLocation().getBaseLocation()))
                && caseData.isLipCase()
                && (hasGaApplications || hasCaseApplications)
                && !featureToggleService.isCuiGaNroEnabled();

            if (shouldSetEaFlag) {
                caseData = caseData.toBuilder().gaEaCourtLocation(YesOrNo.YES).build();
            }

            if (hasGaApplications || hasCaseApplications) {
                if (hasGaApplications) {
                    caseData = helperService.updateApplicationLocationDetailsInClaim(gaCaseData, authToken);
                    gaCaseData = GaCallbackDataUtil.toGaCaseData(caseData, objectMapper);
                } else {
                    caseData = helperService.updateApplicationLocationDetailsInClaim(caseData, authToken);
                    gaCaseData = GaCallbackDataUtil.toGaCaseData(caseData, objectMapper);
                }
                if (shouldSetEaFlag) {
                    caseData = caseData.toBuilder().gaEaCourtLocation(YesOrNo.YES).build();
                }
                if (callbackParams.getRequest().getEventId().equals(TRIGGER_UPDATE_GA_LOCATION.name())) {
                    if (gaCaseData != null && !io.jsonwebtoken.lang.Collections.isEmpty(gaCaseData.getGeneralApplications())) {
                        helperService.triggerEvent(gaCaseData, TRIGGER_LOCATION_UPDATE);
                    } else {
                        helperService.triggerEvent(caseData, TRIGGER_LOCATION_UPDATE);
                    }
                } else if (callbackParams.getRequest().getEventId().equals(TRIGGER_TASK_RECONFIG_GA.name())) {
                    if (gaCaseData != null && !io.jsonwebtoken.lang.Collections.isEmpty(gaCaseData.getGeneralApplications())) {
                        helperService.triggerEvent(gaCaseData, TRIGGER_TASK_RECONFIG);
                    } else {
                        helperService.triggerEvent(caseData, TRIGGER_TASK_RECONFIG);
                    }
                }
            }
        } catch (Exception e) {
            String errorMessage = "Could not trigger event to update location on application under case: "
                + caseData.getCcdCaseReference();
            log.error(errorMessage, e);
            return AboutToStartOrSubmitCallbackResponse.builder().errors(List.of(errorMessage)).build();
        }
        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseData.toMap(objectMapper))
            .build();
    }

}
