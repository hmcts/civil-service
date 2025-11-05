package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.handler.callback.user.sdo.pipeline.SdoCallbackPipeline;
import uk.gov.hmcts.reform.civil.handler.callback.user.sdo.tasks.SdoLifecycleStage;
import uk.gov.hmcts.reform.civil.handler.callback.user.sdo.tasks.SdoTaskContext;
import uk.gov.hmcts.reform.civil.handler.callback.user.sdo.tasks.SdoTaskResult;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.camunda.UpdateWaCourtLocationsService;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.MID;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CallbackVersion.V_1;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_SDO;

@Slf4j
@Service
@RequiredArgsConstructor
public class CreateSDOCallbackHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = Collections.singletonList(CREATE_SDO);
    public static final String ERROR_MESSAGE_DATE_MUST_BE_IN_THE_FUTURE = "Date must be in the future";
    public static final String ERROR_MESSAGE_NUMBER_CANNOT_BE_LESS_THAN_ZERO = "The number entered cannot be less than zero";
    public static final String ERROR_MINTI_DISPOSAL_NOT_ALLOWED = "Disposal Hearing is not available for Multi Track and Intermediate Track Claims. "
        + "This can be requested by using the Make an Order event.";

    private final ObjectMapper objectMapper;
    private final FeatureToggleService featureToggleService;
    private final SdoCallbackPipeline sdoCallbackPipeline;
    private final Optional<UpdateWaCourtLocationsService> updateWaCourtLocationsService;

    @Override
    protected Map<String, Callback> callbacks() {
        return ImmutableMap.<String, Callback>builder()
            .put(callbackKey(ABOUT_TO_START), this::prePopulateOrderDetailsPages)
            .put(callbackKey(V_1, ABOUT_TO_START), this::prePopulateOrderDetailsPages)
            .put(callbackKey(MID, "order-details-navigation"), this::setOrderDetailsFlags)
            .put(callbackKey(MID, "generate-sdo-order"), this::generateSdoOrder)
            .put(callbackKey(V_1, MID, "generate-sdo-order"), this::generateSdoOrder)
            .put(callbackKey(ABOUT_TO_SUBMIT), this::submitSDO)
            .put(callbackKey(SUBMITTED), this::buildConfirmation)
            .build();
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    // This is currently a mid event but once pre states are defined it should be moved to an about to start event.
    // Once it has been moved to an about to start event the following file will need to be updated:
    //  FlowStateAllowedEventService.java.
    // This way pressing previous on the ccd page won't end up calling this method again and thus
    // repopulating the fields if they have been changed.
    // There is no reason to add conditionals to avoid this here since having it as an about to start event will mean
    // it is only ever called once.
    // Then any changes to fields in ccd will persist in ccd regardless of backwards or forwards page navigation.
    private CallbackResponse prePopulateOrderDetailsPages(CallbackParams callbackParams) {
        CaseData originalCaseData = callbackParams.getCaseData();
        SdoTaskResult prePopulateResult = runStage(originalCaseData, callbackParams, SdoLifecycleStage.PRE_POPULATE);
        List<String> prePopulateErrors = extractErrors(prePopulateResult);
        CaseData caseData = updatedCaseData(prePopulateResult, originalCaseData);

        return buildResponse(caseData, prePopulateErrors);
    }

    private CallbackResponse setOrderDetailsFlags(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        SdoTaskResult taskResult = runStage(caseData, callbackParams, SdoLifecycleStage.ORDER_DETAILS);
        List<String> errors = extractErrors(taskResult);
        CaseData updatedCaseData = updatedCaseData(taskResult, caseData);

        return buildResponse(updatedCaseData, errors);
    }

    private CallbackResponse generateSdoOrder(CallbackParams callbackParams) {
        log.info("generateSdoOrder ccdCaseReference: {} legacyCaseReference: {}",
                 callbackParams.getCaseData().getCcdCaseReference(), callbackParams.getCaseData().getLegacyCaseReference());

        CaseData originalCaseData = callbackParams.getCaseData();
        SdoTaskResult orderDetailsResult = runStage(originalCaseData, callbackParams, SdoLifecycleStage.ORDER_DETAILS);

        List<String> orderDetailsErrors = extractErrors(orderDetailsResult);
        CaseData caseDataAfterOrderDetails = updatedCaseData(orderDetailsResult, originalCaseData);

        if (!orderDetailsErrors.isEmpty()) {
            return buildResponse(caseDataAfterOrderDetails, orderDetailsErrors);
        }

        SdoTaskResult validationResult = runStage(caseDataAfterOrderDetails, callbackParams, SdoLifecycleStage.MID_EVENT);

        List<String> validationErrors = extractErrors(validationResult);
        CaseData caseDataAfterValidation = updatedCaseData(validationResult, caseDataAfterOrderDetails);

        if (!validationErrors.isEmpty()) {
            return buildResponse(caseDataAfterValidation, validationErrors);
        }

        SdoTaskResult documentResult = runStage(caseDataAfterValidation, callbackParams, SdoLifecycleStage.DOCUMENT_GENERATION);

        CaseData finalCaseData = updatedCaseData(documentResult, caseDataAfterValidation);

        List<String> documentErrors = extractErrors(documentResult);

        return buildResponse(finalCaseData, documentErrors);
    }

    private CallbackResponse submitSDO(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        SdoTaskResult submissionResult = runStage(caseData, callbackParams, SdoLifecycleStage.SUBMISSION);

        List<String> submissionErrors = extractErrors(submissionResult);
        if (!submissionErrors.isEmpty()) {
            return AboutToStartOrSubmitCallbackResponse.builder()
                .errors(submissionErrors)
                .build();
        }

        CaseData updatedCaseData = updatedCaseData(submissionResult, caseData);

        if (featureToggleService.isMultiOrIntermediateTrackEnabled(updatedCaseData)) {
            CaseData.CaseDataBuilder<?, ?> waUpdateBuilder = updatedCaseData.toBuilder();
            updateWaCourtLocationsService.ifPresent(service -> service.updateCourtListingWALocations(
                callbackParams.getParams().get(BEARER_TOKEN).toString(),
                waUpdateBuilder
            ));
            updatedCaseData = waUpdateBuilder.build();
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(updatedCaseData.toMap(objectMapper))
            .build();
    }

    private SubmittedCallbackResponse buildConfirmation(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        SdoTaskResult confirmationResult = runStage(caseData, callbackParams, SdoLifecycleStage.CONFIRMATION);

        if (confirmationResult.submittedCallbackResponse() != null) {
            return confirmationResult.submittedCallbackResponse();
        }

        return SubmittedCallbackResponse.builder().build();
    }

    private SdoTaskResult runStage(CaseData caseData, CallbackParams callbackParams, SdoLifecycleStage stage) {
        return sdoCallbackPipeline.run(new SdoTaskContext(caseData, callbackParams, stage), stage);
    }

    private List<String> extractErrors(SdoTaskResult result) {
        return result.errors() == null ? Collections.emptyList() : result.errors();
    }

    private CaseData updatedCaseData(SdoTaskResult result, CaseData fallback) {
        return result.updatedCaseData() != null ? result.updatedCaseData() : fallback;
    }

    private AboutToStartOrSubmitCallbackResponse buildResponse(CaseData data, List<String> errors) {
        List<String> safeErrors = errors == null ? Collections.emptyList() : errors;
        AboutToStartOrSubmitCallbackResponse.AboutToStartOrSubmitCallbackResponseBuilder builder =
            AboutToStartOrSubmitCallbackResponse.builder()
                .data(data.toMap(objectMapper))
                .errors(safeErrors);

        return builder.build();
    }

}
