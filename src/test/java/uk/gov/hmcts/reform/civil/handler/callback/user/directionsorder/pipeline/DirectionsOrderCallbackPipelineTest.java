package uk.gov.hmcts.reform.civil.handler.callback.user.directionsorder.pipeline;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.handler.callback.user.directionsorder.tasks.DirectionsOrderCallbackTask;
import uk.gov.hmcts.reform.civil.handler.callback.user.directionsorder.tasks.DirectionsOrderLifecycleStage;
import uk.gov.hmcts.reform.civil.handler.callback.user.directionsorder.tasks.DirectionsOrderTaskContext;
import uk.gov.hmcts.reform.civil.handler.callback.user.directionsorder.tasks.DirectionsOrderTaskResult;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DirectionsOrderCallbackPipelineTest {

    @Test
    void shouldMergeTaskResultsInOrder() {
        CaseData initial = caseDataWithLegacyReference("initial");
        CaseData updated = caseDataWithLegacyReference("updated");
        CaseData finalData = caseDataWithLegacyReference("final");

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
        CaseData initial = caseDataWithLegacyReference("initial");
        DirectionsOrderCallbackTask task = new StubTask(DirectionsOrderLifecycleStage.PRE_POPULATE, initial, List.of());

        DirectionsOrderCallbackPipeline pipeline = new DirectionsOrderCallbackPipeline(List.of(task));
        DirectionsOrderTaskContext context = new DirectionsOrderTaskContext(initial, null, DirectionsOrderLifecycleStage.MID_EVENT);

        DirectionsOrderTaskResult result = pipeline.run(context, DirectionsOrderLifecycleStage.DOCUMENT_GENERATION);

        assertThat(result.updatedCaseData()).isEqualTo(initial);
        assertThat(result.errors()).isEmpty();
    }

    @Test
    void shouldIgnoreTasksThatDoNotApplyToContext() {
        CaseData initial = caseDataWithLegacyReference("initial");
        CaseData applicableResult = caseDataWithLegacyReference("applicable");

        DirectionsOrderCallbackTask applicableTask = new StubTask(DirectionsOrderLifecycleStage.PRE_POPULATE, applicableResult, List.of(), true);
        DirectionsOrderCallbackTask ignoredTask = new StubTask(
            DirectionsOrderLifecycleStage.PRE_POPULATE,
            caseDataWithLegacyReference("ignored"),
            List.of("error"),
            false
        );

        DirectionsOrderCallbackPipeline pipeline = new DirectionsOrderCallbackPipeline(List.of(applicableTask, ignoredTask));
        CallbackRequest request = CallbackRequest.builder().eventId("EVENT").build();
        DirectionsOrderTaskContext context = new DirectionsOrderTaskContext(
                initial,
                new CallbackParams().request(request),
                DirectionsOrderLifecycleStage.PRE_POPULATE
        );

        DirectionsOrderTaskResult result = pipeline.run(context, DirectionsOrderLifecycleStage.PRE_POPULATE);

        assertThat(result.updatedCaseData()).isEqualTo(applicableResult);
        assertThat(result.errors()).isEmpty();
    }

    private record StubTask(DirectionsOrderLifecycleStage stage, CaseData caseData, List<String> errors, boolean applies) implements DirectionsOrderCallbackTask {
        StubTask(DirectionsOrderLifecycleStage stage, CaseData caseData, List<String> errors) {
            this(stage, caseData, errors, true);
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

    private CaseData caseDataWithLegacyReference(String legacyCaseReference) {
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setLegacyCaseReference(legacyCaseReference);
        return caseData;
    }
}
