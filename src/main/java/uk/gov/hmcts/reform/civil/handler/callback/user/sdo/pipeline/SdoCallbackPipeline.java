package uk.gov.hmcts.reform.civil.handler.callback.user.sdo.pipeline;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.callback.user.sdo.tasks.SdoCallbackTask;
import uk.gov.hmcts.reform.civil.handler.callback.user.sdo.tasks.SdoLifecycleStage;
import uk.gov.hmcts.reform.civil.handler.callback.user.sdo.tasks.SdoTaskContext;
import uk.gov.hmcts.reform.civil.handler.callback.user.sdo.tasks.SdoTaskResult;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class SdoCallbackPipeline {

    private final List<SdoCallbackTask> tasks;

    public SdoTaskResult run(SdoTaskContext context, SdoLifecycleStage stage) {
        return tasks.stream()
            .filter(task -> supports(task, stage))
            .map(task -> task.execute(context))
            .reduce(SdoTaskResult.empty(context.caseData()), this::mergeResults);
    }

    private boolean supports(SdoCallbackTask task, SdoLifecycleStage stage) {
        return task.supports(stage);
    }

    private SdoTaskResult mergeResults(SdoTaskResult previous, SdoTaskResult current) {
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
        return new SdoTaskResult(caseData, errors, response);
    }
}
