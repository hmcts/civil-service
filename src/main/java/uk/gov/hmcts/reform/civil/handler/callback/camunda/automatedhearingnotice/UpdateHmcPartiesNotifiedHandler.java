package uk.gov.hmcts.reform.civil.handler.callback.camunda.automatedhearingnotice;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
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

@Slf4j
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

        Long ccdCaseReference = caseData.getCcdCaseReference();
        String hearingId = camundaVariables.getHearingId();

        try {
            log.info(
                "Calling updatePartiesNotifiedResponse for caseId {}: hearingId={}, version={}, responseDateTime={}",
                ccdCaseReference,
                hearingId,
                camundaVariables.getRequestVersion(),
                camundaVariables.getResponseDateTime()
            );
            hearingsService.updatePartiesNotifiedResponse(
                callbackParams.getParams().get(BEARER_TOKEN).toString(),
                hearingId, camundaVariables.getRequestVersion().intValue(),
                camundaVariables.getResponseDateTime(), partiesNotified
            );

        } catch (HttpClientErrorException e) {
            log.info(
                "hearing for caseId {} with hearingId={}, version={}, responseDateTime={} already notified and http status {}",
                ccdCaseReference,
                hearingId,
                camundaVariables.getRequestVersion(),
                camundaVariables.getResponseDateTime(),
                e.getStatusCode()
            );
            if (HttpStatus.BAD_REQUEST.equals(e.getStatusCode())) {
                return AboutToStartOrSubmitCallbackResponse.builder().build();
            } else {
                log.info(
                    "hearing hasn't notified for caseId {} with hearingId={}, version={}, responseDateTime={} and http status {}",
                    ccdCaseReference,
                    hearingId,
                    camundaVariables.getRequestVersion(),
                    camundaVariables.getResponseDateTime(),
                    e.getStatusCode()
                );
                throw e;
            }
        }
        return AboutToStartOrSubmitCallbackResponse.builder().build();
    }
}
