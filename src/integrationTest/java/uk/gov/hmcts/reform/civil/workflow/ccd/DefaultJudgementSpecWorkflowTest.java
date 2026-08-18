package uk.gov.hmcts.reform.civil.workflow.ccd;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentState;
import uk.gov.hmcts.reform.civil.service.DeadlinesCalculator;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.Time;
import uk.gov.hmcts.reform.civil.workflow.WorkflowIntegrationTest;
import uk.gov.hmcts.reform.civil.workflow.ccd.fixture.DefaultJudgementSpecFixtures;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.DEFAULT_JUDGEMENT_NON_DIVERGENT_SPEC;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.DEFAULT_JUDGEMENT_SPEC;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.JUDGMENT_REQUESTED_SPEC;
import static uk.gov.hmcts.reform.civil.enums.BusinessProcessStatus.READY;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;

@SuppressWarnings({"java:S5960", "java:S6813"})
class DefaultJudgementSpecWorkflowTest extends WorkflowIntegrationTest {

    @MockBean
    private FeatureToggleService featureToggleService;

    @MockBean
    private Time time;

    @MockBean
    private DeadlinesCalculator deadlinesCalculator;

    @BeforeEach
    void setUp() {
        when(time.now()).thenReturn(LocalDateTime.now());
        when(deadlinesCalculator.addMonthsToDateToNextWorkingDayAtMidnight(anyInt(), any()))
            .thenReturn(LocalDateTime.now().plusMonths(36));
        when(featureToggleService.isJudgmentBufferEnabled()).thenReturn(false);
    }

    @Test
    void shouldGrantSpec1v1DefaultJudgmentNonDivergentWithoutBuffer() throws Exception {
        CaseData fixture = DefaultJudgementSpecFixtures.specDj1v1NonDivergentNoBuffer();

        startWorkflow(fixture)
            .eventId(DEFAULT_JUDGEMENT_SPEC)
            .aboutToSubmit()
            .then(result -> {
                assertThat(result.response().getErrors()).isNullOrEmpty();
                assertThat(result.response().getState()).isEqualTo(CaseState.All_FINAL_ORDERS_ISSUED.name());

                CaseData updated = result.caseData();
                assertThat(updated.getActiveJudgment()).isNotNull();
                assertThat(updated.getActiveJudgment().getState()).isEqualTo(JudgmentState.ISSUED);
                assertThat(updated.getActiveJudgment().getIssueDate()).isNotNull();
                assertThat(updated.getActiveJudgment().getIsRegisterWithRTL()).isEqualTo(YES);

                assertThat(updated.getBusinessProcess().getStatus()).isEqualTo(READY);
                assertThat(updated.getBusinessProcess().getCamundaEvent())
                    .isEqualTo(DEFAULT_JUDGEMENT_NON_DIVERGENT_SPEC.name());
            })
            .submitted()
            .then(result -> assertThat(result.submittedResponse()).isNotNull());
    }

    @Test
    void shouldParkInJudgmentRequestedStateWhenBufferEnabledAndLipCase() throws Exception {
        when(featureToggleService.isJudgmentBufferEnabled()).thenReturn(true);
        CaseData fixture = DefaultJudgementSpecFixtures.specDj1v1NonDivergentWithBuffer();

        startWorkflow(fixture)
            .eventId(DEFAULT_JUDGEMENT_SPEC)
            .aboutToSubmit()
            .then(result -> {
                assertThat(result.response().getErrors()).isNullOrEmpty();
                assertThat(result.response().getState()).isEqualTo(CaseState.JUDGMENT_REQUESTED.name());

                CaseData updated = result.caseData();
                assertThat(updated.getActiveJudgment()).isNotNull();
                assertThat(updated.getActiveJudgment().getState()).isEqualTo(JudgmentState.PENDING_ISSUE);
                assertThat(updated.getIsJoRequested()).isEqualTo(YES);

                assertThat(updated.getBusinessProcess().getStatus()).isEqualTo(READY);
                assertThat(updated.getBusinessProcess().getCamundaEvent())
                    .isEqualTo(JUDGMENT_REQUESTED_SPEC.name());
            });
    }

    @Test
    void shouldTransitionToHeritageSysWhenDivergent1v2() throws Exception {
        CaseData fixture = DefaultJudgementSpecFixtures.specDj1v2Divergent();

        startWorkflow(fixture)
            .eventId(DEFAULT_JUDGEMENT_SPEC)
            .aboutToSubmit()
            .then(result -> {
                assertThat(result.response().getErrors()).isNullOrEmpty();
                assertThat(result.response().getState())
                    .isEqualTo(CaseState.PROCEEDS_IN_HERITAGE_SYSTEM.name());

                CaseData updated = result.caseData();
                assertThat(updated.getTakenOfflineDate()).isNotNull();
                assertThat(updated.getBusinessProcess().getStatus()).isEqualTo(READY);
                assertThat(updated.getBusinessProcess().getCamundaEvent())
                    .isEqualTo(DEFAULT_JUDGEMENT_SPEC.name());
            });
    }

    @Test
    void shouldReturnErrorOnAboutToStartWhenDeadlineNotPassed() throws Exception {
        CaseData fixture = DefaultJudgementSpecFixtures.specDjDeadlineNotPassed();

        startWorkflow(fixture)
            .eventId(DEFAULT_JUDGEMENT_SPEC)
            .aboutToStart()
            .then(result -> {
                assertThat(result.response().getErrors()).isNotEmpty();
                assertThat(result.response().getErrors().get(0))
                    .contains("The Claim  is not eligible for Default Judgment until");
            });
    }

    @Test
    void shouldReturnErrorOnAboutToStartWhenInBreathingSpace() throws Exception {
        CaseData fixture = DefaultJudgementSpecFixtures.specDjInBreathingSpace();

        startWorkflow(fixture)
            .eventId(DEFAULT_JUDGEMENT_SPEC)
            .aboutToStart()
            .then(result -> {
                assertThat(result.response().getErrors()).isNotEmpty();
                assertThat(result.response().getErrors()).contains(
                    "Default judgment cannot be applied for while claim is in breathing space"
                );
            });
    }
}
