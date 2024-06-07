package uk.gov.hmcts.reform.civil.stateflow.transitions;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.stateflow.model.Transition;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponseType.COUNTER_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponseType.FULL_ADMISSION;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponseType.FULL_DEFENCE;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponseType.PART_ADMISSION;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.divergentRespondWithDQAndGoOffline;

@ExtendWith(MockitoExtension.class)
public class AllResponsesReceivedTransitionBuilderTest {

    @Mock
    private FeatureToggleService mockFeatureToggleService;

    private CaseDataBuilder caseDataBuilder;
    private List<Transition> result;

    @BeforeEach
    void setUp() {
        AllResponsesReceivedTransitionBuilder allResponsesReceivedTransitionBuilder = new AllResponsesReceivedTransitionBuilder(
            mockFeatureToggleService);
        caseDataBuilder = CaseDataBuilder.builder().multiPartyClaimTwoApplicants();
        result = allResponsesReceivedTransitionBuilder.buildTransitions();
        assertNotNull(result);
    }

    @Test
    void shouldSetUpTransitions_withExpectedSizeAndStates() {
        assertThat(result).hasSize(14);

        assertTransition(result.get(0), "MAIN.ALL_RESPONSES_RECEIVED", "MAIN.FULL_DEFENCE");
        assertTransition(result.get(1), "MAIN.ALL_RESPONSES_RECEIVED", "MAIN.FULL_DEFENCE");
        assertTransition(result.get(2), "MAIN.ALL_RESPONSES_RECEIVED", "MAIN.FULL_ADMISSION");
        assertTransition(result.get(3), "MAIN.ALL_RESPONSES_RECEIVED", "MAIN.PART_ADMISSION");
        assertTransition(result.get(4), "MAIN.ALL_RESPONSES_RECEIVED", "MAIN.COUNTER_CLAIM");
        assertTransition(result.get(5), "MAIN.ALL_RESPONSES_RECEIVED", "MAIN.DIVERGENT_RESPOND_GO_OFFLINE");
        assertTransition(result.get(6), "MAIN.ALL_RESPONSES_RECEIVED", "MAIN.DIVERGENT_RESPOND_GO_OFFLINE");
        assertTransition(result.get(7), "MAIN.ALL_RESPONSES_RECEIVED", "MAIN.DIVERGENT_RESPOND_GO_OFFLINE");
        assertTransition(result.get(8), "MAIN.ALL_RESPONSES_RECEIVED", "MAIN.DIVERGENT_RESPOND_GO_OFFLINE");
        assertTransition(result.get(9), "MAIN.ALL_RESPONSES_RECEIVED", "MAIN.DIVERGENT_RESPOND_GO_OFFLINE");
        assertTransition(result.get(10), "MAIN.ALL_RESPONSES_RECEIVED", "MAIN.DIVERGENT_RESPOND_GENERATE_DQ_GO_OFFLINE");
        assertTransition(result.get(11), "MAIN.ALL_RESPONSES_RECEIVED", "MAIN.DIVERGENT_RESPOND_GENERATE_DQ_GO_OFFLINE");
        assertTransition(result.get(12), "MAIN.ALL_RESPONSES_RECEIVED", "MAIN.TAKEN_OFFLINE_BY_STAFF");
        assertTransition(result.get(13), "MAIN.ALL_RESPONSES_RECEIVED", "MAIN.PAST_CLAIM_DISMISSED_DEADLINE_AWAITING_CAMUNDA");
    }

    @Test
    void shouldReturnFalse_whenBothRespondentResponsesAreNotFullDefence_2v1Scenario() {
        CaseData caseData = caseDataBuilder
            .atStateRespondentFullDefenceAfterNotifyClaimDetails().build().toBuilder()
            .respondent1ClaimResponseType(COUNTER_CLAIM)
            .respondent1ClaimResponseTypeToApplicant2(PART_ADMISSION)
            .build();

        assertFalse(divergentRespondWithDQAndGoOffline.test(caseData));
    }

    @Test
    void shouldReturnTrue_whenOneRespondentResponseIsFullDefence_2v1Scenario() {
        CaseData caseData = caseDataBuilder
            .atStateRespondentFullDefenceAfterNotifyClaimDetails().build().toBuilder()
            .respondent1ClaimResponseType(FULL_DEFENCE)
            .respondent1ClaimResponseTypeToApplicant2(PART_ADMISSION)
            .build();

        assertTrue(divergentRespondWithDQAndGoOffline.test(caseData));
    }

    @Test
    void shouldReturnFalse_whenBothRespondentResponsesAreFullDefence_1v2SameSolicitorScenario() {
        CaseData caseData = caseDataBuilder
            .atStateRespondentFullDefenceAfterNotifyClaimDetails().build().toBuilder()
            .respondent1ClaimResponseType(FULL_DEFENCE)
            .respondent2ClaimResponseType(FULL_DEFENCE)
            .respondent2(Party.builder().partyName("Respondent2").build())
            .respondent2SameLegalRepresentative(YES)
            .addApplicant2(null)
            .build();

        assertFalse(divergentRespondWithDQAndGoOffline.test(caseData));
    }

    @Test
    void shouldReturnFalse_whenBothRespondentResponsesAreNotFullDefence_1v2SameSolicitorScenario() {
        CaseData caseData = caseDataBuilder
            .atStateRespondentFullDefenceAfterNotifyClaimDetails().build().toBuilder()
            .respondent1ClaimResponseType(COUNTER_CLAIM)
            .respondent2ClaimResponseType(PART_ADMISSION)
            .respondent2(Party.builder().partyName("Respondent2").build())
            .respondent2SameLegalRepresentative(YES)
            .addApplicant2(null)
            .build();

        assertFalse(divergentRespondWithDQAndGoOffline.test(caseData));
    }

    @Test
    void shouldReturnTrue_whenOneRespondentResponseIsFullDefence_1v2SameSolicitorScenario() {
        CaseData caseData = caseDataBuilder
            .atStateRespondentFullDefenceAfterNotifyClaimDetails().build().toBuilder()
            .respondent1ClaimResponseType(FULL_DEFENCE)
            .respondent2ClaimResponseType(FULL_ADMISSION)
            .respondent2(Party.builder().partyName("Respondent2").build())
            .respondent2SameLegalRepresentative(YES)
            .addApplicant2(null)
            .build();

        assertTrue(divergentRespondWithDQAndGoOffline.test(caseData));
    }

    @Test
    void shouldReturnFalse_whenBothRespondentResponsesAreFullDefence_1v2DifferentSolicitorScenario() {
        CaseData caseData = caseDataBuilder
            .atStateRespondentFullDefenceAfterNotifyClaimDetails().build().toBuilder()
            .respondent1ClaimResponseType(FULL_DEFENCE)
            .respondent2ClaimResponseType(FULL_DEFENCE)
            .respondent2(Party.builder().partyName("Respondent2").build())
            .respondent2SameLegalRepresentative(NO)
            .addApplicant2(null)
            .build();

        assertFalse(divergentRespondWithDQAndGoOffline.test(caseData));
    }

    @Test
    void shouldReturnFalse_whenBothRespondentResponsesAreNotFullDefence_1v2DifferentSolicitorScenario() {
        CaseData caseData = caseDataBuilder
            .atStateRespondentFullDefenceAfterNotifyClaimDetails().build().toBuilder()
            .respondent1ClaimResponseType(COUNTER_CLAIM)
            .respondent2ClaimResponseType(PART_ADMISSION)
            .respondent2(Party.builder().partyName("Respondent2").build())
            .respondent2SameLegalRepresentative(NO)
            .addApplicant2(null)
            .build();

        assertFalse(divergentRespondWithDQAndGoOffline.test(caseData));
    }

    @Test
    void shouldReturnTrue_whenOneRespondentResponseIsFullDefence_1v2DifferentSolicitorScenario() {
        CaseData caseData = caseDataBuilder
            .atStateRespondentFullDefenceAfterNotifyClaimDetails().build().toBuilder()
            .respondent1ClaimResponseType(FULL_DEFENCE)
            .respondent2ClaimResponseType(FULL_ADMISSION)
            .respondent2(Party.builder().partyName("Respondent2").build())
            .respondent2SameLegalRepresentative(NO)
            .respondent2ResponseDate(LocalDateTime.now().minusDays(1))
            .addApplicant2(null)
            .build();

        assertTrue(divergentRespondWithDQAndGoOffline.test(caseData));
    }

    @Test
    void shouldReturnFalse_whenBothRespondentResponsesAreFullDefenceWithSameClaimResponseType() {
        CaseData caseData = caseDataBuilder
            .atStateRespondentFullDefenceAfterNotifyClaimDetails().build().toBuilder()
            .respondent1ClaimResponseType(FULL_DEFENCE)
            .respondent1ClaimResponseTypeToApplicant2(FULL_DEFENCE)
            .build();

        assertFalse(divergentRespondWithDQAndGoOffline.test(caseData));
    }

    private void assertTransition(Transition transition, String sourceState, String targetState) {
        assertThat(transition.getSourceState()).isEqualTo(sourceState);
        assertThat(transition.getTargetState()).isEqualTo(targetState);
    }
}
