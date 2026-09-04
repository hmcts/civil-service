package uk.gov.hmcts.reform.civil.workflow.ga;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.ga.enums.dq.GAJudgeDecisionOption;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.ga.service.ParentCaseUpdateHelper;
import uk.gov.hmcts.reform.civil.workflow.ga.fixture.GaLifecycleFixtures;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@SuppressWarnings("java:S5960")
class EndJudgeMakesDecisionBusinessProcessWorkflowTest extends GAWorkflowIntegrationTest {

    @MockBean
    private ParentCaseUpdateHelper parentCaseUpdateHelper;

    @Test
    void shouldMoveAnApprovedOrderToOrderMade() throws Exception {
        assertEndState(
            GaLifecycleFixtures.decision(GAJudgeDecisionOption.FREE_FORM_ORDER),
            CaseState.ORDER_MADE
        );
    }

    @Test
    void shouldMoveADismissedApplicationToApplicationDismissed() throws Exception {
        assertEndState(GaLifecycleFixtures.dismissedApplication(), CaseState.APPLICATION_DISMISSED);
    }

    @Test
    void shouldMoveDirectionsToAwaitingDirectionsDocuments() throws Exception {
        assertEndState(GaLifecycleFixtures.directionsOrder(), CaseState.AWAITING_DIRECTIONS_ORDER_DOCS);
    }

    @Test
    void shouldMoveWrittenRepresentationsToAwaitingRepresentations() throws Exception {
        assertEndState(
            GaLifecycleFixtures.writtenRepresentations(),
            CaseState.AWAITING_WRITTEN_REPRESENTATIONS
        );
    }

    @Test
    void shouldMoveAListedApplicationToListingForAHearing() throws Exception {
        assertEndState(GaLifecycleFixtures.listForHearing(), CaseState.LISTING_FOR_A_HEARING);
    }

    @Test
    void shouldMoveARequestForInformationToAwaitingAdditionalInformation() throws Exception {
        assertEndState(
            GaLifecycleFixtures.requestMoreInformation(),
            CaseState.AWAITING_ADDITIONAL_INFORMATION
        );
    }

    @Test
    void shouldTakeAnApprovedClaimantStrikeOutApplicationOffline() throws Exception {
        assertEndState(GaLifecycleFixtures.strikeOutOrder(), CaseState.PROCEEDS_IN_HERITAGE);
    }

    private void assertEndState(GeneralApplicationCaseData caseData, CaseState expectedState) throws Exception {
        startWorkflow(caseData)
            .eventId(CaseEvent.END_JUDGE_BUSINESS_PROCESS_GASPEC)
            .aboutToSubmit()
            .then(result -> {
                assertThat(result.response().getErrors()).isNullOrEmpty();
                assertThat(result.response().getState()).isEqualTo(expectedState.name());
                assertThat(result.caseData().getCcdState()).isEqualTo(expectedState);
            });

        verify(parentCaseUpdateHelper).updateParentWithGAState(any(), eq(expectedState.getDisplayedValue()));
    }
}
