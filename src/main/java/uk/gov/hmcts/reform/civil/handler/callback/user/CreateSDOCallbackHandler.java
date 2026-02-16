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
import uk.gov.hmcts.reform.civil.handler.callback.user.directionsorder.DirectionsOrderStageExecutionResult;
import uk.gov.hmcts.reform.civil.handler.callback.user.directionsorder.DirectionsOrderStageExecutor;
import uk.gov.hmcts.reform.civil.handler.callback.user.directionsorder.pipeline.DirectionsOrderCallbackPipeline;
import uk.gov.hmcts.reform.civil.handler.callback.user.directionsorder.tasks.DirectionsOrderLifecycleStage;
import uk.gov.hmcts.reform.civil.handler.callback.user.directionsorder.tasks.DirectionsOrderTaskContext;
import uk.gov.hmcts.reform.civil.handler.callback.user.directionsorder.tasks.DirectionsOrderTaskResult;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.util.Collections;
import java.util.List;
import java.util.Map;

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

    public static final String DEFAULT_PENAL_NOTICE = """
        WARNING

        [DEFENDANT] IF YOU DO NOT COMPLY WITH THIS ORDER YOU MAY BE HELD IN CONTEMPT OF COURT AND PUNISHED BY A FINE, \
        IMPRISONMENT, CONFISCATION OF ASSETS OR OTHER PUNISHMENT UNDER THE LAW.

        A penal notice against the Defendant is attached to paragraph X below.



        """;

    private final ObjectMapper objectMapper;
    private final DirectionsOrderCallbackPipeline directionsOrderCallbackPipeline;
    private final DirectionsOrderStageExecutor directionsOrderStageExecutor;

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
        log.info("Pre-populating SDO for caseId {}", originalCaseData.getCcdCaseReference());
        DirectionsOrderTaskResult prePopulateResult =
            runStage(originalCaseData, callbackParams, DirectionsOrderLifecycleStage.PRE_POPULATE);
        List<String> prePopulateErrors = extractErrors(prePopulateResult);
        CaseData caseData = updatedCaseData(prePopulateResult, originalCaseData);

        return buildResponse(caseData, prePopulateErrors);
    }

    private CallbackResponse setOrderDetailsFlags(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        log.info("Setting SDO order detail flags for caseId {}", caseData.getCcdCaseReference());
        DirectionsOrderTaskResult taskResult =
            runStage(caseData, callbackParams, DirectionsOrderLifecycleStage.ORDER_DETAILS);
        List<String> errors = extractErrors(taskResult);
        CaseData updatedCaseData = updatedCaseData(taskResult, caseData);

        return buildResponse(updatedCaseData, errors);
    }

    private CallbackResponse generateSdoOrder(CallbackParams callbackParams) {
        log.info("generateSdoOrder ccdCaseReference: {} legacyCaseReference: {}",
                 callbackParams.getCaseData().getCcdCaseReference(), callbackParams.getCaseData().getLegacyCaseReference());

        DirectionsOrderStageExecutionResult executionResult = directionsOrderStageExecutor.runOrderGenerationStages(
            callbackParams.getCaseData(),
            callbackParams
        );

        return buildResponse(executionResult.caseData(), executionResult.errors());
    }

    private CallbackResponse submitSDO(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        log.info("Submitting SDO for caseId {}", caseData.getCcdCaseReference());
        DirectionsOrderTaskResult submissionResult =
            runStage(caseData, callbackParams, DirectionsOrderLifecycleStage.SUBMISSION);

        List<String> submissionErrors = extractErrors(submissionResult);
        if (!submissionErrors.isEmpty()) {
            return AboutToStartOrSubmitCallbackResponse.builder()
                .errors(submissionErrors)
                .build();
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(updatedCaseData(submissionResult, caseData).toMap(objectMapper))
            .build();
    }

    private SubmittedCallbackResponse buildConfirmation(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        log.info("Building SDO confirmation for caseId {}", caseData.getCcdCaseReference());
        DirectionsOrderTaskResult confirmationResult =
            runStage(caseData, callbackParams, DirectionsOrderLifecycleStage.CONFIRMATION);

        if (confirmationResult.submittedCallbackResponse() != null) {
            return confirmationResult.submittedCallbackResponse();
        }

        return SubmittedCallbackResponse.builder().build();
    }

    private DirectionsOrderTaskResult runStage(
        CaseData caseData,
        CallbackParams callbackParams,
        DirectionsOrderLifecycleStage stage
    ) {
        return directionsOrderCallbackPipeline.run(
            new DirectionsOrderTaskContext(caseData, callbackParams, stage),
            stage
        );
    }

    private List<String> extractErrors(DirectionsOrderTaskResult result) {
        return result.errors() == null ? Collections.emptyList() : result.errors();
    }

    private CaseData updatedCaseData(DirectionsOrderTaskResult result, CaseData fallback) {
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
