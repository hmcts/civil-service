package uk.gov.hmcts.reform.civil.handler.callback.camunda.businessprocess;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.genapplication.GeneralApplication;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.START_BUSINESS_PROCESS_GASPEC;

@Service
@RequiredArgsConstructor
public class StartGeneralApplicationBusinessProcessCallbackHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = List.of(START_BUSINESS_PROCESS_GASPEC);

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(callbackKey(ABOUT_TO_SUBMIT), this::startGeneralApplicationBusinessProcess);
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private final CaseDetailsConverter caseDetailsConverter;

    private CallbackResponse startGeneralApplicationBusinessProcess(CallbackParams callbackParams) {
        CaseData data = caseDetailsConverter.toCaseData(callbackParams.getRequest().getCaseDetails());

        if (data.getGeneralApplications() != null) {
            List<Element<GeneralApplication>> generalApplications = data.getGeneralApplications();
            for (Element<GeneralApplication> generalApplication : generalApplications) {
                if (checkGAExitsWithBusinessProcessReady(generalApplication)) {
                    switch (generalApplication.getValue().getBusinessProcess().getStatus()) {
                        case READY:
                        case DISPATCHED:
                            BusinessProcess  businessProcess = generalApplication.getValue().getBusinessProcess();
                            businessProcess.start();
                            break;
                        default:
                            break;
                    }
                }
            }
            return evaluateReady(callbackParams, generalApplications);
        }
        return throwConcurrenyError();
    }

    private AboutToStartOrSubmitCallbackResponse throwConcurrenyError() {
        return AboutToStartOrSubmitCallbackResponse.builder()
            .errors(List.of("Concurrency Error"))
            .build();
    }

    private Boolean checkGAExitsWithBusinessProcessReady(Element<GeneralApplication> generalApplication) {
        return generalApplication.getValue() != null
            && generalApplication.getValue().getBusinessProcess() != null
            && StringUtils.isNotBlank(generalApplication.getValue().getBusinessProcess().getProcessInstanceId())
            && generalApplication.getValue().getBusinessProcess().getStatus() != null;
    }

    private CallbackResponse evaluateReady(CallbackParams callbackParams,
                                           List<Element<GeneralApplication>> generalApplications) {
        Map<String, Object> output = callbackParams.getRequest().getCaseDetails().getData();
        output.put("generalApplications", generalApplications);

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(output)
            .build();
    }
}
