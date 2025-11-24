package uk.gov.hmcts.reform.civil.handler.callback.user.directionsorder.pipeline;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.callback.user.directionsorder.tasks.DirectionsOrderCallbackTask;
import uk.gov.hmcts.reform.civil.handler.callback.user.directionsorder.tasks.DirectionsOrderLifecycleStage;
import uk.gov.hmcts.reform.civil.handler.callback.user.directionsorder.tasks.DirectionsOrderTaskContext;
import uk.gov.hmcts.reform.civil.handler.callback.user.directionsorder.tasks.DirectionsOrderTaskResult;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DirectionsOrderCallbackPipeline {

    private final List<DirectionsOrderCallbackTask> tasks;

    public DirectionsOrderTaskResult run(DirectionsOrderTaskContext context, DirectionsOrderLifecycleStage stage) {
        return tasks.stream()
            .filter(task -> supports(task, context, stage))
            .peek(task -> log.debug(
                "Executing task {} for stage {} and caseId {}",
                task.getClass().getSimpleName(),
                stage,
                context.caseData().getCcdCaseReference()
            ))
            .map(task -> task.execute(context))
            .reduce(DirectionsOrderTaskResult.empty(context.caseData()), this::mergeResults);
    }

    private boolean supports(
        DirectionsOrderCallbackTask task,
        DirectionsOrderTaskContext context,
        DirectionsOrderLifecycleStage stage
    ) {
        return task.supports(stage) && task.appliesTo(context);
    }

    private DirectionsOrderTaskResult mergeResults(
        DirectionsOrderTaskResult previous,
        DirectionsOrderTaskResult current
    ) {
        List<String> errors = new ArrayList<>();
        if (previous.errors() != null) {
            errors.addAll(previous.errors());
        }
        if (current.errors() != null) {
            errors.addAll(current.errors());
        }
        var response = current.submittedCallbackResponse() != null
            ? current.submittedCallbackResponse()
            : previous.submittedCallbackResponse();
        var caseData = current.updatedCaseData() != null
            ? current.updatedCaseData()
            : previous.updatedCaseData();
        if (!errors.isEmpty()) {
            log.debug("Aggregated {} error(s) while merging task results for caseId {}", errors.size(), caseData.getCcdCaseReference());
        }
        return new DirectionsOrderTaskResult(caseData, errors, response);
    }
}
