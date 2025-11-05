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
        return new ImmutableMap.Builder<String, Callback>()
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
        SdoTaskContext prePopulateContext = new SdoTaskContext(
            originalCaseData,
            callbackParams,
            SdoLifecycleStage.PRE_POPULATE
        );
        SdoTaskResult prePopulateResult = sdoCallbackPipeline.run(prePopulateContext, SdoLifecycleStage.PRE_POPULATE);
        List<String> prePopulateErrors = prePopulateResult.errors() == null
            ? Collections.emptyList()
            : prePopulateResult.errors();
        CaseData caseData = prePopulateResult.updatedCaseData() != null
            ? prePopulateResult.updatedCaseData()
            : originalCaseData;

        AboutToStartOrSubmitCallbackResponse.AboutToStartOrSubmitCallbackResponseBuilder responseBuilder =
            AboutToStartOrSubmitCallbackResponse.builder()
                .data(caseData.toMap(objectMapper));

        if (!prePopulateErrors.isEmpty()) {
            responseBuilder.errors(prePopulateErrors);
        }

        return responseBuilder.build();
    }

    private CallbackResponse setOrderDetailsFlags(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        SdoTaskContext context = new SdoTaskContext(caseData, callbackParams, SdoLifecycleStage.ORDER_DETAILS);
        SdoTaskResult taskResult = sdoCallbackPipeline.run(context, SdoLifecycleStage.ORDER_DETAILS);

        List<String> errors = taskResult.errors() == null ? Collections.emptyList() : taskResult.errors();
        CaseData updatedCaseData = taskResult.updatedCaseData() != null ? taskResult.updatedCaseData() : caseData;

        AboutToStartOrSubmitCallbackResponse.AboutToStartOrSubmitCallbackResponseBuilder responseBuilder =
            AboutToStartOrSubmitCallbackResponse.builder()
                .data(updatedCaseData.toMap(objectMapper));

        if (!errors.isEmpty()) {
            responseBuilder.errors(errors);
        }

        return responseBuilder.build();
    }

    private CallbackResponse generateSdoOrder(CallbackParams callbackParams) {
        log.info("generateSdoOrder ccdCaseReference: {} legacyCaseReference: {}",
                 callbackParams.getCaseData().getCcdCaseReference(), callbackParams.getCaseData().getLegacyCaseReference());

        CaseData originalCaseData = callbackParams.getCaseData();
        SdoTaskContext orderDetailsContext = new SdoTaskContext(
            originalCaseData,
            callbackParams,
            SdoLifecycleStage.ORDER_DETAILS
        );
        SdoTaskResult orderDetailsResult = sdoCallbackPipeline.run(orderDetailsContext, SdoLifecycleStage.ORDER_DETAILS);

        List<String> orderDetailsErrors = orderDetailsResult.errors() == null
            ? Collections.emptyList()
            : orderDetailsResult.errors();
        CaseData caseDataAfterOrderDetails = orderDetailsResult.updatedCaseData() != null
            ? orderDetailsResult.updatedCaseData()
            : originalCaseData;

        if (!orderDetailsErrors.isEmpty()) {
            return AboutToStartOrSubmitCallbackResponse.builder()
                .errors(orderDetailsErrors)
                .data(caseDataAfterOrderDetails.toMap(objectMapper))
                .build();
        }

        SdoTaskContext validationContext = new SdoTaskContext(
            caseDataAfterOrderDetails,
            callbackParams,
            SdoLifecycleStage.MID_EVENT
        );
        SdoTaskResult validationResult = sdoCallbackPipeline.run(validationContext, SdoLifecycleStage.MID_EVENT);

        List<String> validationErrors = validationResult.errors() == null
            ? Collections.emptyList()
            : validationResult.errors();
        CaseData caseDataAfterValidation = validationResult.updatedCaseData() != null
            ? validationResult.updatedCaseData()
            : caseDataAfterOrderDetails;

        if (!validationErrors.isEmpty()) {
            return AboutToStartOrSubmitCallbackResponse.builder()
                .errors(validationErrors)
                .data(caseDataAfterValidation.toMap(objectMapper))
                .build();
        }

        SdoTaskContext documentContext = new SdoTaskContext(
            caseDataAfterValidation,
            callbackParams,
            SdoLifecycleStage.DOCUMENT_GENERATION
        );
        SdoTaskResult documentResult = sdoCallbackPipeline.run(documentContext, SdoLifecycleStage.DOCUMENT_GENERATION);

        CaseData finalCaseData = documentResult.updatedCaseData() != null
            ? documentResult.updatedCaseData()
            : caseDataAfterValidation;

        List<String> documentErrors = documentResult.errors() == null
            ? Collections.emptyList()
            : documentResult.errors();

        return AboutToStartOrSubmitCallbackResponse.builder()
            .errors(documentErrors)
            .data(finalCaseData.toMap(objectMapper))
            .build();
    }

    private CallbackResponse submitSDO(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        SdoTaskContext submissionContext = new SdoTaskContext(caseData, callbackParams, SdoLifecycleStage.SUBMISSION);
        SdoTaskResult submissionResult = sdoCallbackPipeline.run(submissionContext, SdoLifecycleStage.SUBMISSION);

        List<String> submissionErrors = submissionResult.errors() == null
            ? Collections.emptyList()
            : submissionResult.errors();
        if (!submissionErrors.isEmpty()) {
            return AboutToStartOrSubmitCallbackResponse.builder()
                .errors(submissionErrors)
                .build();
        }

        CaseData updatedCaseData = submissionResult.updatedCaseData() != null
            ? submissionResult.updatedCaseData()
            : caseData;

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
        SdoTaskContext confirmationContext = new SdoTaskContext(caseData, callbackParams, SdoLifecycleStage.CONFIRMATION);
        SdoTaskResult confirmationResult = sdoCallbackPipeline.run(confirmationContext, SdoLifecycleStage.CONFIRMATION);

        if (confirmationResult.submittedCallbackResponse() != null) {
            return confirmationResult.submittedCallbackResponse();
        }

        return SubmittedCallbackResponse.builder().build();
    }

}
