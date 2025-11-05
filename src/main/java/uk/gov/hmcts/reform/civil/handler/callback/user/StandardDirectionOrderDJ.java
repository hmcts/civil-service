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
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.STANDARD_DIRECTION_ORDER_DJ;

@Slf4j
@Service
@RequiredArgsConstructor
public class StandardDirectionOrderDJ extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = Collections.singletonList(STANDARD_DIRECTION_ORDER_DJ);
    private final ObjectMapper objectMapper;
    private final DirectionsOrderCallbackPipeline directionsOrderCallbackPipeline;

    @Override
    protected Map<String, Callback> callbacks() {
        return new ImmutableMap.Builder<String, Callback>()
            .put(callbackKey(ABOUT_TO_START), this::initiateSDO)
            .put(callbackKey(MID, "trial-disposal-screen"), this::populateDisposalTrialScreen)
            .put(callbackKey(V_1, MID, "trial-disposal-screen"), this::populateDisposalTrialScreen)
            .put(callbackKey(MID, "create-order"), this::createOrderScreen)
            .put(callbackKey(V_1, MID, "create-order"), this::createOrderScreen)
            .put(callbackKey(ABOUT_TO_SUBMIT), this::generateSDONotifications)
            .put(callbackKey(SUBMITTED), this::buildConfirmation)
            .build();
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse initiateSDO(CallbackParams callbackParams) {
        CaseData originalCaseData = callbackParams.getCaseData();
        DirectionsOrderTaskResult prePopulateResult = runStage(
            originalCaseData,
            callbackParams,
            DirectionsOrderLifecycleStage.PRE_POPULATE
        );
        CaseData updatedCaseData = updatedCaseData(prePopulateResult, originalCaseData);
        List<String> errors = extractErrors(prePopulateResult);
        AboutToStartOrSubmitCallbackResponse.AboutToStartOrSubmitCallbackResponseBuilder builder =
            AboutToStartOrSubmitCallbackResponse.builder()
                .data(updatedCaseData.toMap(objectMapper));
        if (!errors.isEmpty()) {
            builder.errors(errors);
        }
        return builder.build();
    }

    private CallbackResponse populateDisposalTrialScreen(CallbackParams callbackParams) {
        CaseData originalCaseData = callbackParams.getCaseData();
        DirectionsOrderTaskResult result = runStage(
            originalCaseData,
            callbackParams,
            DirectionsOrderLifecycleStage.ORDER_DETAILS
        );
        CaseData updatedCaseData = updatedCaseData(result, originalCaseData);
        List<String> errors = extractErrors(result);
        return buildResponse(updatedCaseData, errors);
    }

    private CallbackResponse generateSDONotifications(CallbackParams callbackParams) {
        CaseData originalCaseData = callbackParams.getCaseData();
        DirectionsOrderTaskResult submissionResult =
            runStage(originalCaseData, callbackParams, DirectionsOrderLifecycleStage.SUBMISSION);

        CaseData updatedCaseData = updatedCaseData(submissionResult, originalCaseData);
        List<String> submissionErrors = extractErrors(submissionResult);

        AboutToStartOrSubmitCallbackResponse.AboutToStartOrSubmitCallbackResponseBuilder builder =
            AboutToStartOrSubmitCallbackResponse.builder()
                .data(updatedCaseData.toMap(objectMapper))
                .state("CASE_PROGRESSION");

        if (!submissionErrors.isEmpty()) {
            builder.errors(submissionErrors);
        }

        return builder.build();
    }

    private SubmittedCallbackResponse buildConfirmation(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        DirectionsOrderTaskResult confirmationResult =
            runStage(caseData, callbackParams, DirectionsOrderLifecycleStage.CONFIRMATION);

        if (confirmationResult.submittedCallbackResponse() != null) {
            return confirmationResult.submittedCallbackResponse();
        }

        return SubmittedCallbackResponse.builder().build();
    }

    private CallbackResponse createOrderScreen(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();

        DirectionsOrderTaskResult orderDetailsResult =
            runStage(caseData, callbackParams, DirectionsOrderLifecycleStage.ORDER_DETAILS);
        CaseData caseDataAfterOrderDetails = updatedCaseData(orderDetailsResult, caseData);
        List<String> orderDetailsErrors = extractErrors(orderDetailsResult);
        if (!orderDetailsErrors.isEmpty()) {
            return buildResponse(caseDataAfterOrderDetails, orderDetailsErrors);
        }

        DirectionsOrderTaskResult validationResult =
            runStage(caseDataAfterOrderDetails, callbackParams, DirectionsOrderLifecycleStage.MID_EVENT);
        CaseData caseDataAfterValidation = updatedCaseData(validationResult, caseDataAfterOrderDetails);
        List<String> validationErrors = extractErrors(validationResult);
        if (!validationErrors.isEmpty()) {
            return buildResponse(caseDataAfterValidation, validationErrors);
        }

        DirectionsOrderTaskResult documentResult =
            runStage(caseDataAfterValidation, callbackParams, DirectionsOrderLifecycleStage.DOCUMENT_GENERATION);
        CaseData finalCaseData = updatedCaseData(documentResult, caseDataAfterValidation);
        List<String> documentErrors = extractErrors(documentResult);

        return buildResponse(finalCaseData, documentErrors);
    }

    private AboutToStartOrSubmitCallbackResponse buildResponse(CaseData data, List<String> errors) {
        AboutToStartOrSubmitCallbackResponse.AboutToStartOrSubmitCallbackResponseBuilder builder =
            AboutToStartOrSubmitCallbackResponse.builder()
                .data(data.toMap(objectMapper));
        if (errors != null && !errors.isEmpty()) {
            builder.errors(errors);
        }
        return builder.build();
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

}
