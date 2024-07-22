package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.RuntimeService;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.enums.settlediscontinue.ConfirmOrderGivesPermission;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.UPDATE_VISIBILITY_NOTICE_OF_DISCONTINUANCE;

@Service
@RequiredArgsConstructor
public class UpdateVisibilityNoticeOfDiscontinuanceHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = List.of(UPDATE_VISIBILITY_NOTICE_OF_DISCONTINUANCE);
    public static final String TASK_ID = "UpdateVisibilityNoticeOfDiscontinuance";

    private final RuntimeService runTimeService;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_SUBMIT),
            this::updateVisibilityNoticeDiscontinuance
        );
    }

    @Override
    public String camundaActivityId(CallbackParams callbackParams) {
        return TASK_ID;
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse updateVisibilityNoticeDiscontinuance(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        //TODO add update visibility implementation here
        updateCamundaVars(caseData);
        return AboutToStartOrSubmitCallbackResponse.builder().build();
    }

    private void updateCamundaVars(CaseData caseData) {
        runTimeService.setVariable(
            caseData.getBusinessProcess().getProcessInstanceId(),
            "discontinuanceValidationSuccess",
            ConfirmOrderGivesPermission.YES.equals(caseData.getConfirmOrderGivesPermission())
        );
    }
}
