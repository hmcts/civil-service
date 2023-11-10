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
import uk.gov.hmcts.reform.hmc.model.unnotifiedhearings.PartiesNotified;
import uk.gov.hmcts.reform.hmc.model.unnotifiedhearings.PartiesNotifiedServiceData;
import uk.gov.hmcts.reform.hmc.service.HearingsService;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;

@Service
@RequiredArgsConstructor
public class UpdateHmcPartiesNotifiedHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = List.of(
        CaseEvent.UPDATE_PARTIES_NOTIFIED_HMC
    );

    private final HearingNoticeCamundaService camundaService;
    private final HearingsService hearingsService;

    private static final String TASK_ID = "UpdatePartiesNotifiedHmc";

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(callbackKey(ABOUT_TO_SUBMIT), this::updatePartiesNotified);
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    @Override
    public String camundaActivityId(CallbackParams callbackParams) {
        return TASK_ID;
    }

    private CallbackResponse updatePartiesNotified(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        HearingNoticeVariables camundaVariables = camundaService.getProcessVariables(caseData.getBusinessProcess().getProcessInstanceId());

        PartiesNotified partiesNotified = PartiesNotified.builder()
            .serviceData(PartiesNotifiedServiceData.builder()
                             .hearingNoticeGenerated(true)
                             .hearingLocation(camundaVariables.getHearingLocationEpims())
                             .days(camundaVariables.getDays())
                             .build())
            .build();

        hearingsService.updatePartiesNotifiedResponse(callbackParams.getParams().get(BEARER_TOKEN).toString(),
                                                      camundaVariables.getHearingId(), camundaVariables.getRequestVersion().intValue(),
                                                      camundaVariables.getResponseDateTime(), partiesNotified);

        return AboutToStartOrSubmitCallbackResponse.builder().build();
    }

}
