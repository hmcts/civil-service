package uk.gov.hmcts.reform.civil.workflow.ccd;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentRTLStatus;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentState;
import uk.gov.hmcts.reform.civil.workflow.WorkflowIntegrationTest;
import uk.gov.hmcts.reform.civil.workflow.ccd.fixture.DefaultJudgementGrantedSpecFixtures;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.DEFAULT_JUDGEMENT_GRANTED_SPEC;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.DEFAULT_JUDGEMENT_NON_DIVERGENT_SPEC;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;

public class DefaultJudgementGrantedSpecWorkflowTest extends WorkflowIntegrationTest {

    @Test
    void shouldGrantDefaultJudgmentSuccessfully() throws Exception {
        CaseData fixture = DefaultJudgementGrantedSpecFixtures.caseData();

        startWorkflow(fixture)
            .eventId(DEFAULT_JUDGEMENT_GRANTED_SPEC)
            .aboutToSubmit()
            .then(result -> {
                assertThat(result.response().getErrors()).isNullOrEmpty();
                assertThat(result.response().getState()).isEqualTo(CaseState.All_FINAL_ORDERS_ISSUED.name());

                CaseData updatedCaseData = result.caseData();
                assertThat(updatedCaseData.getActiveJudgment().getState()).isEqualTo(JudgmentState.ISSUED);
                assertThat(updatedCaseData.getActiveJudgment().getIssueDate()).isNotNull();
                assertThat(updatedCaseData.getActiveJudgment().getRtlState()).isEqualTo(JudgmentRTLStatus.ISSUED.getRtlState());
                assertThat(updatedCaseData.getActiveJudgment().getIsRegisterWithRTL()).isEqualTo(YES);

                assertThat(updatedCaseData.getBusinessProcess().getCamundaEvent()).isEqualTo(
                    DEFAULT_JUDGEMENT_NON_DIVERGENT_SPEC.name());
            })
            .submitted()
            .then(result -> assertThat(result.submittedResponse()).isNotNull());
    }

    @Test
    void shouldReturnErrorWhenCaseNotInJudgmentRequestedState() throws Exception {
        CaseData fixture = DefaultJudgementGrantedSpecFixtures.caseDataWithWrongState();

        startWorkflow(fixture)
            .eventId(DEFAULT_JUDGEMENT_GRANTED_SPEC)
            .aboutToSubmit()
            .then(result ->
                      assertThat(result.response().getErrors()).contains(
                          String.format(
                              "Event DEFAULT_JUDGEMENT_GRANTED_SPEC: Cannot grant default judgment for case in state %s for caseId %d",
                              CaseState.CASE_ISSUED,
                              fixture.getCcdCaseReference()
                          )
                      ));
    }

    @Test
    void shouldReturnErrorWhenActiveJudgmentIsNull() throws Exception {
        CaseData fixture = DefaultJudgementGrantedSpecFixtures.caseDataWithNoActiveJudgment();

        startWorkflow(fixture)
            .eventId(DEFAULT_JUDGEMENT_GRANTED_SPEC)
            .aboutToSubmit()
            .then(result ->
                      assertThat(result.response().getErrors()).contains(
                          String.format(
                              "Event DEFAULT_JUDGEMENT_GRANTED_SPEC: Active judgment is null for caseId %d",
                              fixture.getCcdCaseReference()
                          )
                      ));
    }
}
