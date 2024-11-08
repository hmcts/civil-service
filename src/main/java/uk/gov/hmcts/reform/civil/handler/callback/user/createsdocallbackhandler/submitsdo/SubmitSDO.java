package uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandler.submitsdo;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.CaseTask;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.util.List;

import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_SDO;
import static uk.gov.hmcts.reform.civil.utils.HearingUtils.getHearingNotes;

@Component
@AllArgsConstructor
@Slf4j
public class SubmitSDO implements CaseTask {

    private final ObjectMapper objectMapper;
    private final List<SdoCaseDataFieldUpdater> sdoCaseDataFieldUpdaters;

    public CallbackResponse execute(CallbackParams callbackParams) {
        log.info("Executing SubmitSDO callback for case {}", callbackParams.getCaseData().getCcdCaseReference());
        CaseData.CaseDataBuilder<?, ?> dataBuilder = getSharedData(callbackParams);
        CaseData caseData = callbackParams.getCaseData();

        sdoCaseDataFieldUpdaters.forEach(updater -> updater.update(caseData, dataBuilder));
        dataBuilder.hearingNotes(getHearingNotes(caseData));

        log.info("SubmitSDO callback executed successfully for case {}", caseData.getCcdCaseReference());
        return AboutToStartOrSubmitCallbackResponse.builder()
                .data(dataBuilder.build().toMap(objectMapper))
                .build();
    }

    private CaseData.CaseDataBuilder<?, ?> getSharedData(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        CaseData.CaseDataBuilder<?, ?> dataBuilder = caseData.toBuilder();

        dataBuilder.businessProcess(BusinessProcess.ready(CREATE_SDO));
        log.debug("Shared data prepared for case {}", caseData.getCcdCaseReference());

        return dataBuilder;
    }
}
