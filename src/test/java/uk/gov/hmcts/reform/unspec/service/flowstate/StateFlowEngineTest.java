package uk.gov.hmcts.reform.unspec.service.flowstate;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.unspec.enums.YesOrNo;
import uk.gov.hmcts.reform.unspec.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.unspec.model.CaseData;
import uk.gov.hmcts.reform.unspec.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.unspec.sampledata.CaseDetailsBuilder;
import uk.gov.hmcts.reform.unspec.stateflow.StateFlow;
import uk.gov.hmcts.reform.unspec.stateflow.model.State;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.unspec.service.flowstate.FlowState.Main.APPLICANT_RESPOND_TO_DEFENCE;
import static uk.gov.hmcts.reform.unspec.service.flowstate.FlowState.Main.AWAITING_CASE_DETAILS_NOTIFICATION;
import static uk.gov.hmcts.reform.unspec.service.flowstate.FlowState.Main.AWAITING_CASE_NOTIFICATION;
import static uk.gov.hmcts.reform.unspec.service.flowstate.FlowState.Main.CASE_PROCEEDS_IN_CASEMAN;
import static uk.gov.hmcts.reform.unspec.service.flowstate.FlowState.Main.CLAIM_DISCONTINUED;
import static uk.gov.hmcts.reform.unspec.service.flowstate.FlowState.Main.CLAIM_ISSUED;
import static uk.gov.hmcts.reform.unspec.service.flowstate.FlowState.Main.CLAIM_STAYED;
import static uk.gov.hmcts.reform.unspec.service.flowstate.FlowState.Main.CLAIM_WITHDRAWN;
import static uk.gov.hmcts.reform.unspec.service.flowstate.FlowState.Main.DRAFT;
import static uk.gov.hmcts.reform.unspec.service.flowstate.FlowState.Main.PAYMENT_SUCCESSFUL;
import static uk.gov.hmcts.reform.unspec.service.flowstate.FlowState.Main.PENDING_CASE_ISSUED;
import static uk.gov.hmcts.reform.unspec.service.flowstate.FlowState.Main.PROCEEDS_OFFLINE_ADMIT_OR_COUNTER_CLAIM;
import static uk.gov.hmcts.reform.unspec.service.flowstate.FlowState.Main.PROCEEDS_OFFLINE_UNREPRESENTED_DEFENDANT;
import static uk.gov.hmcts.reform.unspec.service.flowstate.FlowState.Main.RESPONDENT_FULL_DEFENCE;
import static uk.gov.hmcts.reform.unspec.service.flowstate.FlowState.Main.SERVICE_ACKNOWLEDGED;

@SpringBootTest(classes = {
    JacksonAutoConfiguration.class,
    CaseDetailsConverter.class,
    StateFlowEngine.class
})
class StateFlowEngineTest {

    @Autowired
    private StateFlowEngine stateFlowEngine;

    @Nested
    class EvaluateStateFlowEngine {

        @Test
        void shouldReturnPendingCaseIssued_whenCaseDataAtStatePendingCaseIssued() {
            CaseData caseData = CaseDataBuilder.builder().atStatePendingCaseIssued().build();

            StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

            assertThat(stateFlow.getState())
                .extracting(State::getName)
                .isNotNull()
                .isEqualTo(PENDING_CASE_ISSUED.fullName());
            assertThat(stateFlow.getStateHistory())
                .hasSize(2)
                .extracting(State::getName)
                .containsExactly(
                    DRAFT.fullName(), PENDING_CASE_ISSUED.fullName());
        }

        @Test
        void shouldReturnProceedsWithOfflineJourney_whenCaseDataAtStateClaimDraftIssuedAndRespondent1NotRepresented() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft()
                .respondent1Represented(YesOrNo.NO)
                .build();

            StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

            assertThat(stateFlow.getState())
                .extracting(State::getName)
                .isNotNull()
                .isEqualTo(PROCEEDS_OFFLINE_UNREPRESENTED_DEFENDANT.fullName());
            assertThat(stateFlow.getStateHistory())
                .hasSize(2)
                .extracting(State::getName)
                .containsExactly(
                    DRAFT.fullName(), PROCEEDS_OFFLINE_UNREPRESENTED_DEFENDANT.fullName());
        }

        @Test
        void shouldReturnPaymentSuccessful_whenCaseDataAtStatePaymentSuccessful() {
            CaseData caseData = CaseDataBuilder.builder().atStatePaymentSuccessful().build();

            StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

            assertThat(stateFlow.getState())
                .extracting(State::getName)
                .isNotNull()
                .isEqualTo(PAYMENT_SUCCESSFUL.fullName());
            assertThat(stateFlow.getStateHistory())
                .hasSize(3)
                .extracting(State::getName)
                .containsExactly(
                    DRAFT.fullName(), PENDING_CASE_ISSUED.fullName(), PAYMENT_SUCCESSFUL.fullName());
        }

        @Test
        void shouldReturnAwaitingCaseNotification_whenCaseDataAtStateAwaitingCaseNotification() {
            CaseData caseData = CaseDataBuilder.builder().atStateAwaitingCaseNotification().build();

            StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

            assertThat(stateFlow.getState())
                .extracting(State::getName)
                .isNotNull()
                .isEqualTo(AWAITING_CASE_NOTIFICATION.fullName());
            assertThat(stateFlow.getStateHistory())
                .hasSize(4)
                .extracting(State::getName)
                .containsExactly(
                    DRAFT.fullName(), PENDING_CASE_ISSUED.fullName(), PAYMENT_SUCCESSFUL.fullName(),
                    AWAITING_CASE_NOTIFICATION.fullName()
                );
        }

        @Test
        void shouldReturnAwaitingCaseNotification_whenCaseDataAtStateAwaitingCaseDetailsNotification() {
            CaseData caseData = CaseDataBuilder.builder().atStateAwaitingCaseDetailsNotification().build();

            StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

            assertThat(stateFlow.getState())
                .extracting(State::getName)
                .isNotNull()
                .isEqualTo(AWAITING_CASE_DETAILS_NOTIFICATION.fullName());
            assertThat(stateFlow.getStateHistory())
                .hasSize(5)
                .extracting(State::getName)
                .containsExactly(
                    DRAFT.fullName(), PENDING_CASE_ISSUED.fullName(), PAYMENT_SUCCESSFUL.fullName(),
                    AWAITING_CASE_NOTIFICATION.fullName(), AWAITING_CASE_DETAILS_NOTIFICATION.fullName()
                );
        }

        @Test
        void shouldReturnClaimIssued_whenCaseDataAtStateClaimIssued() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimCreated().build();

            StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

            assertThat(stateFlow.getState())
                .extracting(State::getName)
                .isNotNull()
                .isEqualTo(CLAIM_ISSUED.fullName());
            assertThat(stateFlow.getStateHistory())
                .hasSize(6)
                .extracting(State::getName)
                .containsExactly(
                    DRAFT.fullName(), PENDING_CASE_ISSUED.fullName(), PAYMENT_SUCCESSFUL.fullName(),
                    AWAITING_CASE_NOTIFICATION.fullName(), AWAITING_CASE_DETAILS_NOTIFICATION.fullName(),
                    CLAIM_ISSUED.fullName()
                );
        }

        @Test
        void shouldReturnClaimStayed_whenCaseDataAtStateClaimStayed() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimStayed().build();

            StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

            assertThat(stateFlow.getState())
                .extracting(State::getName)
                .isNotNull()
                .isEqualTo(CLAIM_STAYED.fullName());
            assertThat(stateFlow.getStateHistory())
                .hasSize(7)
                .extracting(State::getName)
                .containsExactly(
                    DRAFT.fullName(), PENDING_CASE_ISSUED.fullName(), PAYMENT_SUCCESSFUL.fullName(),
                    AWAITING_CASE_NOTIFICATION.fullName(), AWAITING_CASE_DETAILS_NOTIFICATION.fullName(),
                    CLAIM_ISSUED.fullName(), CLAIM_STAYED.fullName()
                );
        }

        @Test
        void shouldReturnServiceAcknowledge_whenCaseDataAtStateServiceAcknowledge() {
            CaseData caseData = CaseDataBuilder.builder().atStateServiceAcknowledge().build();

            StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

            assertThat(stateFlow.getState())
                .extracting(State::getName)
                .isNotNull()
                .isEqualTo(SERVICE_ACKNOWLEDGED.fullName());
            assertThat(stateFlow.getStateHistory())
                .hasSize(7)
                .extracting(State::getName)
                .containsExactly(
                    DRAFT.fullName(), PENDING_CASE_ISSUED.fullName(), PAYMENT_SUCCESSFUL.fullName(),
                    AWAITING_CASE_NOTIFICATION.fullName(), AWAITING_CASE_DETAILS_NOTIFICATION.fullName(),
                    CLAIM_ISSUED.fullName(), SERVICE_ACKNOWLEDGED.fullName()
                );
        }

        @Test
        void shouldReturnRespondToClaim_whenCaseDataAtStateRespondentFullDefence() {
            CaseData caseData = CaseDataBuilder.builder().atStateRespondentFullDefence().build();

            StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

            assertThat(stateFlow.getState())
                .extracting(State::getName)
                .isNotNull()
                .isEqualTo(RESPONDENT_FULL_DEFENCE.fullName());
            assertThat(stateFlow.getStateHistory())
                .hasSize(7)
                .extracting(State::getName)
                .containsExactly(
                    DRAFT.fullName(), PENDING_CASE_ISSUED.fullName(), PAYMENT_SUCCESSFUL.fullName(),
                    AWAITING_CASE_NOTIFICATION.fullName(), AWAITING_CASE_DETAILS_NOTIFICATION.fullName(),
                    CLAIM_ISSUED.fullName(), RESPONDENT_FULL_DEFENCE.fullName()
                );
        }

        @Test
        void shouldReturnClaimantRespond_whenCaseDataAtStateApplicantRespondToDefence() {
            CaseData caseData = CaseDataBuilder.builder().atStateApplicantRespondToDefence().build();

            StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

            assertThat(stateFlow.getState())
                .extracting(State::getName)
                .isNotNull()
                .isEqualTo(CLAIM_STAYED.fullName());
            assertThat(stateFlow.getStateHistory())
                .hasSize(9)
                .extracting(State::getName)
                .containsExactly(
                    DRAFT.fullName(), PENDING_CASE_ISSUED.fullName(), PAYMENT_SUCCESSFUL.fullName(),
                    AWAITING_CASE_NOTIFICATION.fullName(), AWAITING_CASE_DETAILS_NOTIFICATION.fullName(),
                    CLAIM_ISSUED.fullName(), RESPONDENT_FULL_DEFENCE.fullName(),
                    APPLICANT_RESPOND_TO_DEFENCE.fullName(), CLAIM_STAYED.fullName()
                );
        }

        @Test
        void shouldReturnProceedsWithOfflineJourney_whenCcdStateIsProceedsWithOfflineJourney() {
            CaseData caseData = CaseDataBuilder.builder().atStateProceedsOfflineAdmissionOrCounterClaim().build();

            StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

            assertThat(stateFlow.getState())
                .extracting(State::getName)
                .isNotNull()
                .isEqualTo(PROCEEDS_OFFLINE_ADMIT_OR_COUNTER_CLAIM.fullName());
            assertThat(stateFlow.getStateHistory())
                .hasSize(8)
                .extracting(State::getName)
                .containsExactly(
                    DRAFT.fullName(), PENDING_CASE_ISSUED.fullName(), PAYMENT_SUCCESSFUL.fullName(),
                    AWAITING_CASE_NOTIFICATION.fullName(), AWAITING_CASE_DETAILS_NOTIFICATION.fullName(),
                    CLAIM_ISSUED.fullName(), RESPONDENT_FULL_DEFENCE.fullName(),
                    PROCEEDS_OFFLINE_ADMIT_OR_COUNTER_CLAIM.fullName()
                );
        }

        @Test
        void shouldReturnProceedsWithOfflineJourney_whenCaseDataIsCaseProceedsInCaseman() {
            CaseData caseData = CaseDataBuilder.builder().atStateCaseProceedsInCaseman().build();

            StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

            assertThat(stateFlow.getState())
                .extracting(State::getName)
                .isNotNull()
                .isEqualTo(CASE_PROCEEDS_IN_CASEMAN.fullName());
            assertThat(stateFlow.getStateHistory())
                .hasSize(5)
                .extracting(State::getName)
                .containsExactly(
                    DRAFT.fullName(), PENDING_CASE_ISSUED.fullName(), PAYMENT_SUCCESSFUL.fullName(),
                    AWAITING_CASE_NOTIFICATION.fullName(), CASE_PROCEEDS_IN_CASEMAN.fullName()
                );
        }
    }

    @Nested
    class HasTransitionedTo {

        @ParameterizedTest
        @CsvSource({
            "true,CLAIM_ISSUED",
            "true,PAYMENT_SUCCESSFUL",
            "true,PENDING_CASE_ISSUED",
            "true,DRAFT",
            "false,RESPONDENT_FULL_DEFENCE",
            "false,APPLICANT_RESPOND_TO_DEFENCE",
            "false,CLAIM_STAYED",
            "false,SERVICE_ACKNOWLEDGED",
        })
        void shouldReturnValidResult_whenCaseDataAtStateClaimCreated(boolean expected, FlowState.Main state) {
            CaseDetails caseDetails = CaseDetailsBuilder.builder()
                .atStateClaimCreated()
                .build();

            assertThat(stateFlowEngine.hasTransitionedTo(caseDetails, state)).isEqualTo(expected);
        }
    }

    @Nested
    class EvaluateWithdrawClaim {

        @EnumSource(value = FlowState.Main.class,
            mode = EnumSource.Mode.EXCLUDE,
            names = {
                "DRAFT",
                "PENDING_CASE_ISSUED",
                "PAYMENT_FAILED",
                "PAYMENT_SUCCESSFUL",
                "CLAIM_DISCONTINUED",
                "PROCEEDS_OFFLINE_UNREPRESENTED_DEFENDANT",
                "PROCEEDS_OFFLINE_ADMIT_OR_COUNTER_CLAIM",
                "AWAITING_CASE_NOTIFICATION",
                "AWAITING_CASE_DETAILS_NOTIFICATION",
                "CASE_PROCEEDS_IN_CASEMAN"
            })
        @ParameterizedTest(name = "{index} => should withdraw claim after claim state {0}")
        void shouldReturnValidState_whenCaseIsWithdrawnAfter(FlowState.Main flowState) {
            CaseData caseData = CaseDataBuilder.builder().withdrawClaimFrom(flowState).build();

            StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

            assertThat(FlowState.fromFullName(stateFlow.getState().getName()))
                .isEqualTo(CLAIM_WITHDRAWN);
        }
    }

    @Nested
    class EvaluateDiscontinueClaim {

        @EnumSource(value = FlowState.Main.class,
            mode = EnumSource.Mode.EXCLUDE,
            names = {
                "DRAFT",
                "PENDING_CASE_ISSUED",
                "PAYMENT_FAILED",
                "PAYMENT_SUCCESSFUL",
                "CLAIM_WITHDRAWN",
                "PROCEEDS_OFFLINE_UNREPRESENTED_DEFENDANT",
                "PROCEEDS_OFFLINE_ADMIT_OR_COUNTER_CLAIM",
                "AWAITING_CASE_NOTIFICATION",
                "AWAITING_CASE_DETAILS_NOTIFICATION",
                "CASE_PROCEEDS_IN_CASEMAN"
            })
        @ParameterizedTest(name = "{index} => should discontinue claim after claim state {0}")
        void shouldReturnValidState_whenCaseIsDiscontinuedAfter(FlowState.Main flowState) {
            CaseData caseData = CaseDataBuilder.builder().discontinueClaimFrom(flowState).build();

            StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

            assertThat(FlowState.fromFullName(stateFlow.getState().getName()))
                .isEqualTo(CLAIM_DISCONTINUED);
        }
    }
}
