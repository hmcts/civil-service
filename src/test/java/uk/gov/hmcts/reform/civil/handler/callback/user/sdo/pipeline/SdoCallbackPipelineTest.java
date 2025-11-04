package uk.gov.hmcts.reform.civil.handler.callback.user.sdo.pipeline;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.handler.callback.user.sdo.tasks.SdoCallbackTask;
import uk.gov.hmcts.reform.civil.handler.callback.user.sdo.tasks.SdoLifecycleStage;
import uk.gov.hmcts.reform.civil.handler.callback.user.sdo.tasks.SdoTaskContext;
import uk.gov.hmcts.reform.civil.handler.callback.user.sdo.tasks.SdoTaskResult;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SdoCallbackPipelineTest {

    @Test
    void shouldMergeTaskResultsInOrder() {
        CaseData initial = CaseData.builder().legacyCaseReference("initial").build();
        CaseData updated = CaseData.builder().legacyCaseReference("updated").build();
        CaseData finalData = CaseData.builder().legacyCaseReference("final").build();

        SdoCallbackTask firstTask = new StubTask(SdoLifecycleStage.MID_EVENT, updated, List.of("first"));
        SdoCallbackTask secondTask = new StubTask(SdoLifecycleStage.MID_EVENT, finalData, List.of("second"));

        SdoCallbackPipeline pipeline = new SdoCallbackPipeline(List.of(firstTask, secondTask));
        SdoTaskContext context = new SdoTaskContext(initial, null, SdoLifecycleStage.MID_EVENT);

        SdoTaskResult result = pipeline.run(context, SdoLifecycleStage.MID_EVENT);

        assertThat(result.updatedCaseData()).isEqualTo(finalData);
        assertThat(result.errors()).containsExactly("first", "second");
    }

    @Test
    void shouldReturnDefaultResult_whenNoTasksSupportStage() {
        CaseData initial = CaseData.builder().legacyCaseReference("initial").build();
        SdoCallbackTask task = new StubTask(SdoLifecycleStage.PRE_POPULATE, initial, List.of());

        SdoCallbackPipeline pipeline = new SdoCallbackPipeline(List.of(task));
        SdoTaskContext context = new SdoTaskContext(initial, null, SdoLifecycleStage.MID_EVENT);

        SdoTaskResult result = pipeline.run(context, SdoLifecycleStage.DOCUMENT_GENERATION);

        assertThat(result.updatedCaseData()).isEqualTo(initial);
        assertThat(result.errors()).isEmpty();
    }

    private static class StubTask implements SdoCallbackTask {
        private final SdoLifecycleStage stage;
        private final CaseData caseData;
        private final List<String> errors;

        StubTask(SdoLifecycleStage stage, CaseData caseData, List<String> errors) {
            this.stage = stage;
            this.caseData = caseData;
            this.errors = errors;
        }

        @Override
        public SdoTaskResult execute(SdoTaskContext context) {
            return new SdoTaskResult(caseData, errors, null);
        }

        @Override
        public boolean supports(SdoLifecycleStage stage) {
            return this.stage == stage;
        }
    }
}
