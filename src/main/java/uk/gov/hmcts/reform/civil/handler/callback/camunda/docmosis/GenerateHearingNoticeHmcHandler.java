package uk.gov.hmcts.reform.civil.handler.callback.camunda.docmosis;

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
import uk.gov.hmcts.reform.civil.utils.HmcDataUtils;
import uk.gov.hmcts.reform.hmc.service.HearingsService;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.utils.HmcDataUtils.getHearingDays;

@Service
@RequiredArgsConstructor
public class GenerateHearingNoticeHmcHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = List.of(
        CaseEvent.GENERATE_HEARING_NOTICE_HMC
    );
    private static final String TASK_ID = "GenerateHearingNotice";

    private final HearingNoticeCamundaService camundaService;

    private final HearingsService hearingsService;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(callbackKey(ABOUT_TO_SUBMIT), this::generateHearingNotice);
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    @Override
    public String camundaActivityId(CallbackParams callbackParams) {
        return TASK_ID;
    }

    private CallbackResponse generateHearingNotice(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        String processInstanceId = caseData.getBusinessProcess().getProcessInstanceId();

        var camundaVars = camundaService.getProcessVariables(processInstanceId);
        var hearing = hearingsService.getHearingResponse(
            callbackParams.getParams().get(BEARER_TOKEN).toString(),
            camundaVars.getHearingId()
        );

        var hearingStartDay = HmcDataUtils.getHearingStartDay(hearing);

        // ToDo: Generate hearing notice

        camundaService.setProcessVariables(
            processInstanceId,
            camundaVars.toBuilder()
                .hearingStartDateTime(hearingStartDay.getHearingStartDateTime())
                .hearingLocationEpims(hearingStartDay.getHearingVenueEpimsId())
                .days(getHearingDays(hearing))
                .requestVersion(hearing.getRequestDetails().getVersionNumber())
                .caseState(caseData.getCcdState().name())
                .responseDateTime(hearing.getHearingResponse().getReceivedDateTime())
                .build()
        );

        return AboutToStartOrSubmitCallbackResponse.builder().build();
    }
}
