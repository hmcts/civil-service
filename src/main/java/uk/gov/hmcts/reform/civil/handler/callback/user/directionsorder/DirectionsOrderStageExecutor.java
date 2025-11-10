package uk.gov.hmcts.reform.civil.handler.callback.user.directionsorder;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.handler.callback.user.directionsorder.pipeline.DirectionsOrderCallbackPipeline;
import uk.gov.hmcts.reform.civil.handler.callback.user.directionsorder.tasks.DirectionsOrderLifecycleStage;
import uk.gov.hmcts.reform.civil.handler.callback.user.directionsorder.tasks.DirectionsOrderTaskContext;
import uk.gov.hmcts.reform.civil.handler.callback.user.directionsorder.tasks.DirectionsOrderTaskResult;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.util.Collections;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DirectionsOrderStageExecutor {

    private static final List<DirectionsOrderLifecycleStage> ORDER_GENERATION_SEQUENCE = List.of(
        DirectionsOrderLifecycleStage.ORDER_DETAILS,
        DirectionsOrderLifecycleStage.MID_EVENT,
        DirectionsOrderLifecycleStage.DOCUMENT_GENERATION
    );

    private final DirectionsOrderCallbackPipeline directionsOrderCallbackPipeline;

    public DirectionsOrderStageExecutionResult runOrderGenerationStages(
        CaseData caseData,
        CallbackParams callbackParams
    ) {
        return runStages(caseData, callbackParams, ORDER_GENERATION_SEQUENCE);
    }

    public DirectionsOrderStageExecutionResult runStages(
        CaseData caseData,
        CallbackParams callbackParams,
        List<DirectionsOrderLifecycleStage> stages
    ) {
        CaseData currentCaseData = caseData;

        for (DirectionsOrderLifecycleStage stage : stages) {
            DirectionsOrderTaskResult stageResult = directionsOrderCallbackPipeline.run(
                new DirectionsOrderTaskContext(currentCaseData, callbackParams, stage),
                stage
            );

            currentCaseData = stageResult.updatedCaseData() != null
                ? stageResult.updatedCaseData()
                : currentCaseData;

            List<String> errors = stageResult.errors() == null
                ? Collections.emptyList()
                : stageResult.errors();

            if (!errors.isEmpty()) {
                return new DirectionsOrderStageExecutionResult(currentCaseData, errors);
            }
        }

        return new DirectionsOrderStageExecutionResult(currentCaseData, Collections.emptyList());
    }
}
