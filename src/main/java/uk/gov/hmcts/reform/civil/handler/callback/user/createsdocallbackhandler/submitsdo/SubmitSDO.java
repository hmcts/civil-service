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
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.camunda.UpdateWaCourtLocationsService;

import java.util.List;
import java.util.Optional;

import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_SDO;
import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.utils.HearingUtils.getHearingNotes;

@Slf4j
@AllArgsConstructor
@Component
public class SubmitSDO implements CaseTask {

    private final ObjectMapper objectMapper;
    private final List<SdoCaseDataFieldUpdater> sdoCaseDataFieldUpdaters;
    private final FeatureToggleService featureToggleService;
    private final Optional<UpdateWaCourtLocationsService> updateWaCourtLocationsService;

    public CallbackResponse execute(CallbackParams callbackParams) {
        log.info("Executing SubmitSDO callback for case {}", callbackParams.getCaseData().getCcdCaseReference());
        CaseData.CaseDataBuilder<?, ?> dataBuilder = getSharedData(callbackParams);
        CaseData caseData = callbackParams.getCaseData();
        sdoCaseDataFieldUpdaters.forEach(updater -> updater.update(caseData, dataBuilder));
        dataBuilder.hearingNotes(getHearingNotes(caseData));
        if (featureToggleService.isMultiOrIntermediateTrackEnabled(caseData)) {
            updateWaCourtLocationsService.ifPresent(service -> service.updateCourtListingWALocations(
                    callbackParams.getParams().get(BEARER_TOKEN).toString(),
                    dataBuilder
            ));
        }
        log.info("SubmitSDO callback executed successfully for case {}", caseData.getCcdCaseReference());
        return AboutToStartOrSubmitCallbackResponse.builder()
                .data(dataBuilder.build().toMap(objectMapper))
                .build();
    }

    private CaseData.CaseDataBuilder<?, ?> getSharedData(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        CaseData.CaseDataBuilder<?, ?> dataBuilder = caseData.toBuilder();
        dataBuilder.businessProcess(BusinessProcess.ready(CREATE_SDO));
        return dataBuilder;
    }
}
