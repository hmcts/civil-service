package uk.gov.hmcts.reform.civil.handler.callback.user.directionsorder.pipeline;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.handler.callback.user.directionsorder.tasks.DirectionsOrderCallbackTask;
import uk.gov.hmcts.reform.civil.handler.callback.user.directionsorder.tasks.DirectionsOrderLifecycleStage;
import uk.gov.hmcts.reform.civil.handler.callback.user.directionsorder.tasks.DirectionsOrderTaskContext;
import uk.gov.hmcts.reform.civil.handler.callback.user.directionsorder.tasks.DirectionsOrderTaskResult;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DirectionsOrderCallbackPipelineTest {

    @Test
    void shouldMergeTaskResultsInOrder() {
        CaseData initial = CaseData.builder().legacyCaseReference("initial").build();
        CaseData updated = CaseData.builder().legacyCaseReference("updated").build();
        CaseData finalData = CaseData.builder().legacyCaseReference("final").build();

        DirectionsOrderCallbackTask firstTask = new StubTask(DirectionsOrderLifecycleStage.MID_EVENT, updated, List.of("first"));
        DirectionsOrderCallbackTask secondTask = new StubTask(DirectionsOrderLifecycleStage.MID_EVENT, finalData, List.of("second"));

        DirectionsOrderCallbackPipeline pipeline = new DirectionsOrderCallbackPipeline(List.of(firstTask, secondTask));
        DirectionsOrderTaskContext context = new DirectionsOrderTaskContext(initial, null, DirectionsOrderLifecycleStage.MID_EVENT);

        DirectionsOrderTaskResult result = pipeline.run(context, DirectionsOrderLifecycleStage.MID_EVENT);

        assertThat(result.updatedCaseData()).isEqualTo(finalData);
        assertThat(result.errors()).containsExactly("first", "second");
    }

    @Test
    void shouldReturnDefaultResult_whenNoTasksSupportStage() {
        CaseData initial = CaseData.builder().legacyCaseReference("initial").build();
        DirectionsOrderCallbackTask task = new StubTask(DirectionsOrderLifecycleStage.PRE_POPULATE, initial, List.of());

        DirectionsOrderCallbackPipeline pipeline = new DirectionsOrderCallbackPipeline(List.of(task));
        DirectionsOrderTaskContext context = new DirectionsOrderTaskContext(initial, null, DirectionsOrderLifecycleStage.MID_EVENT);

        DirectionsOrderTaskResult result = pipeline.run(context, DirectionsOrderLifecycleStage.DOCUMENT_GENERATION);

        assertThat(result.updatedCaseData()).isEqualTo(initial);
        assertThat(result.errors()).isEmpty();
    }

    @Test
    void shouldIgnoreTasksThatDoNotApplyToContext() {
        CaseData initial = CaseData.builder().legacyCaseReference("initial").build();
        CaseData applicableResult = CaseData.builder().legacyCaseReference("applicable").build();

        DirectionsOrderCallbackTask applicableTask = new StubTask(DirectionsOrderLifecycleStage.PRE_POPULATE, applicableResult, List.of(), true);
        DirectionsOrderCallbackTask ignoredTask = new StubTask(DirectionsOrderLifecycleStage.PRE_POPULATE, CaseData.builder().legacyCaseReference("ignored").build(), List.of("error"), false);

        DirectionsOrderCallbackPipeline pipeline = new DirectionsOrderCallbackPipeline(List.of(applicableTask, ignoredTask));
        CallbackRequest request = CallbackRequest.builder().eventId("EVENT").build();
        DirectionsOrderTaskContext context = new DirectionsOrderTaskContext(
            initial,
            CallbackParams.builder().request(request).build(),
            DirectionsOrderLifecycleStage.PRE_POPULATE
        );

        DirectionsOrderTaskResult result = pipeline.run(context, DirectionsOrderLifecycleStage.PRE_POPULATE);

        assertThat(result.updatedCaseData()).isEqualTo(applicableResult);
        assertThat(result.errors()).isEmpty();
    }

    private static class StubTask implements DirectionsOrderCallbackTask {
        private final DirectionsOrderLifecycleStage stage;
        private final CaseData caseData;
        private final List<String> errors;
        private final boolean applies;

        StubTask(DirectionsOrderLifecycleStage stage, CaseData caseData, List<String> errors) {
            this(stage, caseData, errors, true);
        }

        StubTask(DirectionsOrderLifecycleStage stage, CaseData caseData, List<String> errors, boolean applies) {
            this.stage = stage;
            this.caseData = caseData;
            this.errors = errors;
            this.applies = applies;
        }

        @Override
        public DirectionsOrderTaskResult execute(DirectionsOrderTaskContext context) {
            return new DirectionsOrderTaskResult(caseData, errors, null);
        }

        @Override
        public boolean supports(DirectionsOrderLifecycleStage stage) {
            return this.stage == stage;
        }

        @Override
        public boolean appliesTo(DirectionsOrderTaskContext context) {
            return applies;
        }
    }
}
