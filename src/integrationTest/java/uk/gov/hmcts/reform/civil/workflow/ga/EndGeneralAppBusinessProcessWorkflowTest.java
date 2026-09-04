package uk.gov.hmcts.reform.civil.workflow.ga;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.ga.service.ParentCaseUpdateHelper;
import uk.gov.hmcts.reform.civil.workflow.ga.fixture.GaLifecycleFixtures;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@SuppressWarnings("java:S5960")
class EndGeneralAppBusinessProcessWorkflowTest extends GAWorkflowIntegrationTest {

    @MockBean
    private ParentCaseUpdateHelper parentCaseUpdateHelper;

    @Test
    void shouldMoveAnUnpaidApplicationToAwaitingPayment() throws Exception {
        assertEndState(GaLifecycleFixtures.awaitingPayment(), CaseState.AWAITING_APPLICATION_PAYMENT);
    }

    @Test
    void shouldWaitForAResponseForAPaidWithNoticeApplication() throws Exception {
        assertEndState(
            GaLifecycleFixtures.paidWithNotice().copy()
                .ccdState(CaseState.PENDING_APPLICATION_ISSUED)
                .build(),
            CaseState.AWAITING_RESPONDENT_RESPONSE
        );
    }

    @Test
    void shouldReferAPaidWithoutNoticeApplicationForJudicialDecision() throws Exception {
        assertEndState(
            GaLifecycleFixtures.paidWithoutNotice(),
            CaseState.APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION
        );
    }

    @Test
    void shouldReferAnApplicationForJudicialDecisionAfterTheRespondentReplies() throws Exception {
        assertEndState(
            GaLifecycleFixtures.paidWithResponse(),
            CaseState.APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION
        );
    }

    @Test
    void shouldWaitForTheSecondOrganisationResponseToAMultiPartyApplication() throws Exception {
        assertEndState(
            GaLifecycleFixtures.multiPartyWithFirstOrganisationResponse(),
            CaseState.AWAITING_RESPONDENT_RESPONSE
        );
    }

    @Test
    void shouldReferAMultiPartyApplicationAfterBothOrganisationsRespond() throws Exception {
        assertEndState(
            GaLifecycleFixtures.multiPartyWithBothOrganisationsResponses(),
            CaseState.APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION
        );
    }

    @Test
    void shouldTakeARespondentVaryJudgmentApplicationOfflineAfterTheResponse() throws Exception {
        assertEndState(
            GaLifecycleFixtures.respondentVaryJudgmentWithResponse(),
            CaseState.PROCEEDS_IN_HERITAGE
        );
    }

    private void assertEndState(GeneralApplicationCaseData caseData,
                                CaseState expectedState) throws Exception {
        startWorkflow(caseData)
            .eventId(CaseEvent.END_BUSINESS_PROCESS_GASPEC)
            .aboutToSubmit()
            .then(result -> {
                assertThat(result.response().getErrors()).isNullOrEmpty();
                assertThat(result.response().getState()).isEqualTo(expectedState.name());
                assertThat(result.caseData().getCcdState()).isEqualTo(expectedState);
            });

        verify(parentCaseUpdateHelper).updateParentWithGAState(any(), eq(expectedState.getDisplayedValue()));
    }
}
