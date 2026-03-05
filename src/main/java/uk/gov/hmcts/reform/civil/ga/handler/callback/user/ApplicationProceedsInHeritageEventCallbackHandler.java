package uk.gov.hmcts.reform.civil.ga.handler.callback.user;

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
import uk.gov.hmcts.reform.civil.enums.BusinessProcessStatus;
import uk.gov.hmcts.reform.civil.ga.callback.GeneralApplicationCallbackHandler;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.ga.service.DocUploadDashboardNotificationService;
import uk.gov.hmcts.reform.civil.ga.service.GaForLipService;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.service.Time;

import java.util.List;
import java.util.Map;

import static java.util.Collections.singletonList;
import static uk.gov.hmcts.reform.civil.CaseDefinitionConstants.NON_LIVE_STATES;
import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.APPLICATION_PROCEEDS_IN_HERITAGE;
import static uk.gov.hmcts.reform.civil.enums.CaseState.PROCEEDS_IN_HERITAGE;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApplicationProceedsInHeritageEventCallbackHandler extends CallbackHandler implements GeneralApplicationCallbackHandler {

    private static final List<CaseEvent> APPLICATION_PROCEEDS_IN_HERITAGE_EVENTS =
        singletonList(APPLICATION_PROCEEDS_IN_HERITAGE);
    private final ObjectMapper objectMapper;
    private final GaForLipService gaForLipService;
    private final DocUploadDashboardNotificationService dashboardNotificationService;
    private final Time time;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_SUBMIT), this::changeApplicationStateToProceedsInHeritage
        );
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return APPLICATION_PROCEEDS_IN_HERITAGE_EVENTS;
    }

    private CallbackResponse changeApplicationStateToProceedsInHeritage(CallbackParams callbackParams) {
        GeneralApplicationCaseData caseData = callbackParams.getGeneralApplicationCaseData();
        Long caseId = caseData.getCcdCaseReference();
        String authToken = callbackParams.getParams().get(BEARER_TOKEN).toString();
        if (!NON_LIVE_STATES.contains(caseData.getCcdState())) {
            log.info("Changing state to APPLICATION_CLOSED for caseId: {}", caseId);

            GeneralApplicationCaseData caseDataBuilder = caseData.copy();
            caseDataBuilder
                .businessProcess(
                    new BusinessProcess()
                        .setCamundaEvent(APPLICATION_PROCEEDS_IN_HERITAGE.name())
                        .setStatus(BusinessProcessStatus.FINISHED))
                .applicationTakenOfflineDate(time.now());
            if (gaForLipService.isGaForLip(caseData)) {
                if (gaForLipService.isLipApp(caseData)) {
                    dashboardNotificationService.createOfflineResponseDashboardNotification(
                        caseData,
                        "APPLICANT",
                        authToken
                    );
                }
                if (gaForLipService.isLipResp(caseData)) {
                    dashboardNotificationService.createOfflineResponseDashboardNotification(
                        caseData,
                        "RESPONDENT",
                        authToken
                    );
                }
            }
            return AboutToStartOrSubmitCallbackResponse.builder()
                .state(PROCEEDS_IN_HERITAGE.toString())
                .data(caseDataBuilder.build().toMap(objectMapper))
                .build();
        } else {
            return emptyCallbackResponse(callbackParams);
        }
    }
}
