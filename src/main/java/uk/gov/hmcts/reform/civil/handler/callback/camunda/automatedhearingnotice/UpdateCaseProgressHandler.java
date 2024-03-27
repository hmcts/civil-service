package uk.gov.hmcts.reform.civil.handler.callback.camunda.automatedhearingnotice;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.hearingnotice.HearingNoticeCamundaService;
import uk.gov.hmcts.reform.civil.service.hearingnotice.HearingNoticeVariables;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.enums.CaseState.HEARING_READINESS;
import static uk.gov.hmcts.reform.civil.enums.CaseState.PREPARE_FOR_HEARING_CONDUCT_HEARING;
import static uk.gov.hmcts.reform.civil.utils.HearingUtils.hearingFeeRequired;

@Service
@RequiredArgsConstructor
public class UpdateCaseProgressHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = List.of(
        CaseEvent.UPDATE_CASE_PROGRESS_HMC
    );
    private static final String TASK_ID = "UpdateCaseProgressHmc";

    private final HearingNoticeCamundaService camundaService;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(callbackKey(ABOUT_TO_SUBMIT), this::updateCaseProgress);
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    @Override
    public String camundaActivityId(CallbackParams callbackParams) {
        return TASK_ID;
    }

    private CallbackResponse updateCaseProgress(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        HearingNoticeVariables camundaVariables = camundaService.getProcessVariables(caseData.getBusinessProcess().getProcessInstanceId());
        boolean hearingFeeRequired = hearingFeeRequired(camundaVariables.getHearingType());

        if (!hearingFeeRequired) {
            return AboutToStartOrSubmitCallbackResponse.builder()
                .state(PREPARE_FOR_HEARING_CONDUCT_HEARING.name())
                .build();
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .state(HEARING_READINESS.name())
            .build();
    }
}
