package uk.gov.hmcts.reform.civil.service.flowstate;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.enums.AllocatedTrack;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.MediationDecision;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseType;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.hearing.ListingOrRelisting;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.SmallClaimMedicalLRspec;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.ClaimantMediationLip;
import uk.gov.hmcts.reform.civil.model.sdo.ReasonNotSuitableSDO;
import uk.gov.hmcts.reform.civil.sampledata.AddressBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDetailsBuilder;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.stateflow.StateFlow;
import uk.gov.hmcts.reform.civil.stateflow.model.State;
import java.time.LocalDate;
import java.time.LocalDateTime;
import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.enums.CaseCategory.SPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.divergentRespondGoOfflineSpec;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.specClaim;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.ALL_RESPONSES_RECEIVED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.AWAITING_RESPONSES_FULL_DEFENCE_RECEIVED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.AWAITING_RESPONSES_NOT_FULL_DEFENCE_RECEIVED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.CLAIM_DETAILS_NOTIFIED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.CLAIM_DETAILS_NOTIFIED_TIME_EXTENSION;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.CLAIM_DISMISSED_HEARING_FEE_DUE_DEADLINE;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.CLAIM_DISMISSED_PAST_CLAIM_DETAILS_NOTIFICATION_DEADLINE;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.CLAIM_DISMISSED_PAST_CLAIM_DISMISSED_DEADLINE;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.CLAIM_DISMISSED_PAST_CLAIM_NOTIFICATION_DEADLINE;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.CLAIM_ISSUED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.CLAIM_ISSUED_PAYMENT_FAILED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.CLAIM_ISSUED_PAYMENT_SUCCESSFUL;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.CLAIM_NOTIFIED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.CLAIM_SUBMITTED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.CONTACT_DETAILS_CHANGE;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.COUNTER_CLAIM;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.DIVERGENT_RESPOND_GENERATE_DQ_GO_OFFLINE;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.DIVERGENT_RESPOND_GO_OFFLINE;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.DRAFT;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.FULL_ADMISSION;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.FULL_ADMIT_NOT_PROCEED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.FULL_ADMIT_PROCEED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.FULL_DEFENCE;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.FULL_DEFENCE_PROCEED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.IN_HEARING_READINESS;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.IN_MEDIATION;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.NOTIFICATION_ACKNOWLEDGED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.NOTIFICATION_ACKNOWLEDGED_TIME_EXTENSION;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.PART_ADMISSION;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.PART_ADMIT_NOT_SETTLED_NO_MEDIATION;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.PAST_APPLICANT_RESPONSE_DEADLINE_AWAITING_CAMUNDA;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.PAST_CLAIM_DETAILS_NOTIFICATION_DEADLINE_AWAITING_CAMUNDA;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.PAST_CLAIM_DISMISSED_DEADLINE_AWAITING_CAMUNDA;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.PAST_CLAIM_NOTIFICATION_DEADLINE_AWAITING_CAMUNDA;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.PENDING_CLAIM_ISSUED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.PENDING_CLAIM_ISSUED_UNREGISTERED_DEFENDANT;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.PENDING_CLAIM_ISSUED_UNREPRESENTED_DEFENDANT;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.PENDING_CLAIM_ISSUED_UNREPRESENTED_DEFENDANT_ONE_V_ONE_SPEC;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.PENDING_CLAIM_ISSUED_UNREPRESENTED_UNREGISTERED_DEFENDANT;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.SPEC_DRAFT;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.TAKEN_OFFLINE_AFTER_CLAIM_DETAILS_NOTIFIED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.TAKEN_OFFLINE_AFTER_CLAIM_NOTIFIED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.TAKEN_OFFLINE_AFTER_SDO;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.TAKEN_OFFLINE_BY_STAFF;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.TAKEN_OFFLINE_PAST_APPLICANT_RESPONSE_DEADLINE;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.TAKEN_OFFLINE_SDO_NOT_DRAWN;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.TAKEN_OFFLINE_UNREGISTERED_DEFENDANT;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.TAKEN_OFFLINE_UNREPRESENTED_DEFENDANT;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.TAKEN_OFFLINE_UNREPRESENTED_UNREGISTERED_DEFENDANT;

@SpringBootTest(classes = {
    JacksonAutoConfiguration.class,
    CaseDetailsConverter.class,
    StateFlowEngine.class
})
class StateFlowEngineTest {

    @Autowired
    private StateFlowEngine stateFlowEngine;

    @MockBean
    private FeatureToggleService featureToggleService;

    @Nested
    class EvaluateStateFlowEngine {

        @Test
        void shouldReturnClaimSubmitted_whenCaseDataAtStateClaimSubmittedWithOneRespondentRepresentative() {
            // Given
            CaseData caseData = CaseDataBuilder.builder().atStateClaimSubmitted().build();

            // When
            StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

            // Then
            assertThat(stateFlow.getState())
                .extracting(State::getName)
                .isNotNull()
                .isEqualTo(CLAIM_SUBMITTED.fullName());
            assertThat(stateFlow.getStateHistory())
                .hasSize(2)
                .extracting(State::getName)
                .containsExactly(
                    DRAFT.fullName(), CLAIM_SUBMITTED.fullName());

            assertThat(stateFlow.getFlags()).hasSize(4).contains(
                entry(FlowFlag.NOTICE_OF_CHANGE.name(), false),
                entry(FlowFlag.CERTIFICATE_OF_SERVICE.name(), false),
                entry(FlowFlag.GENERAL_APPLICATION_ENABLED.name(), false),
                entry("ONE_RESPONDENT_REPRESENTATIVE", true)
            );
        }

        @Test
        void shouldReturnClaimSubmitted_whenCaseDataAtStateClaimSubmittedTwoRespondentRepresentatives() {
            // Given
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimSubmittedTwoRespondentRepresentatives()
                .build();
            if (caseData.getRespondent2OrgRegistered() != null
                && caseData.getRespondent2Represented() == null) {
                caseData = caseData.toBuilder()
                    .respondent2Represented(YES)
                    .build();
            }
            // When
            StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

            // Then
            assertThat(stateFlow.getState())
                .extracting(State::getName)
                .isNotNull()
                .isEqualTo(CLAIM_SUBMITTED.fullName());
            assertThat(stateFlow.getStateHistory())
                .hasSize(2)
                .extracting(State::getName)
                .containsExactly(
                    DRAFT.fullName(), CLAIM_SUBMITTED.fullName());

            assertThat(stateFlow.getFlags()).hasSize(5).contains(
                entry("ONE_RESPONDENT_REPRESENTATIVE", false),
                entry("TWO_RESPONDENT_REPRESENTATIVES", true),
                entry(FlowFlag.GENERAL_APPLICATION_ENABLED.name(), false),
                entry(FlowFlag.NOTICE_OF_CHANGE.name(), false),
                entry(FlowFlag.CERTIFICATE_OF_SERVICE.name(), false)
            );
        }

        @Test
        void shouldReturnClaimSubmitted_whenCaseDataAtStateClaimSubmittedTwoRepresentativesOneUnreg() {
            // Given
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimSubmittedTwoRespondentRepresentatives()
                .respondent2Represented(YES)
                .respondent2OrgRegistered(YES)
                .respondent1OrgRegistered(NO)
                .respondent2SameLegalRepresentative(NO)
                .build();

            // When
            StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

            // Then
            assertThat(stateFlow.getState())
                .extracting(State::getName)
                .isNotNull()
                .isEqualTo(CLAIM_SUBMITTED.fullName());
            assertThat(stateFlow.getStateHistory())
                .hasSize(2)
                .extracting(State::getName)
                .containsExactly(
                    DRAFT.fullName(), CLAIM_SUBMITTED.fullName());

            assertThat(stateFlow.getFlags()).hasSize(5).contains(
                entry("ONE_RESPONDENT_REPRESENTATIVE", false),
                entry("TWO_RESPONDENT_REPRESENTATIVES", true),
                entry(FlowFlag.GENERAL_APPLICATION_ENABLED.name(), false),
                entry(FlowFlag.CERTIFICATE_OF_SERVICE.name(), false),
                entry(FlowFlag.NOTICE_OF_CHANGE.name(), false)
            );
        }

        @Test
        void shouldReturnClaimSubmitted_whenCaseDataAtStateClaimSubmittedNoRespondentIsRepresented() {
            // Given
            CaseData caseData = CaseDataBuilder.builder().atStateClaimSubmittedNoRespondentRepresented().build();

            // When
            StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

            // Then
            assertThat(stateFlow.getState())
                .extracting(State::getName)
                .isNotNull()
                .isEqualTo(CLAIM_SUBMITTED.fullName());
            assertThat(stateFlow.getStateHistory())
                .hasSize(2)
                .extracting(State::getName)
                .containsExactly(
                    DRAFT.fullName(), CLAIM_SUBMITTED.fullName());
        }

        @Test
        void shouldReturnClaimSubmitted_whenCaseDataAtStateClaimSubmittedOnlyFirstRespondentIsRepresented() {
            // Given
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimSubmitted1v2AndOnlyFirstRespondentIsRepresented()
                .build();
            if (caseData.getRespondent2OrgRegistered() != null
                && caseData.getRespondent2Represented() == null) {
                caseData = caseData.toBuilder()
                    .respondent2Represented(YES)
                    .build();
            }

            // When
            StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

            // Then
            assertThat(stateFlow.getState())
                .extracting(State::getName)
                .isNotNull()
                .isEqualTo(CLAIM_SUBMITTED.fullName());
            assertThat(stateFlow.getStateHistory())
                .hasSize(2)
                .extracting(State::getName)
                .containsExactly(
                    DRAFT.fullName(), CLAIM_SUBMITTED.fullName());
        }

        @Test
        void shouldReturnClaimSubmitted_whenCaseDataAtStateClaimSubmittedOnlySecondRespondentIsRepresented() {
            // Given
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimSubmitted1v2AndOnlySecondRespondentIsRepresented()
                .build();
            if (caseData.getRespondent2OrgRegistered() != null
                && caseData.getRespondent2Represented() == null) {
                caseData = caseData.toBuilder()
                    .respondent2Represented(YES)
                    .build();
            }

            // When
            StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

            // Then
            assertThat(stateFlow.getState())
                .extracting(State::getName)
                .isNotNull()
                .isEqualTo(CLAIM_SUBMITTED.fullName());
            assertThat(stateFlow.getStateHistory())
                .hasSize(2)
                .extracting(State::getName)
                .containsExactly(
                    DRAFT.fullName(), CLAIM_SUBMITTED.fullName());
        }

        @Test
        void shouldReturnClaimSubmitted_whenCaseDataAtStateClaimSubmitted2v1RespondentIsUnrepresented() {
            // Given
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimSubmitted2v1RespondentUnrepresented()
                .build();

            // When
            StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

            // Then
            assertThat(stateFlow.getState())
                .extracting(State::getName)
                .isNotNull()
                .isEqualTo(CLAIM_SUBMITTED.fullName());
            assertThat(stateFlow.getStateHistory())
                .hasSize(2)
                .extracting(State::getName)
                .containsExactly(
                    DRAFT.fullName(), CLAIM_SUBMITTED.fullName());
        }

        @Test
        void shouldReturnClaimSubmitted_whenCaseDataAtStateClaimSubmitted2v1RespondentIsUnregistered() {
            // Given
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimSubmitted2v1RespondentUnregistered()
                .build();

            // When
            StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

            // Then
            assertThat(stateFlow.getState())
                .extracting(State::getName)
                .isNotNull()
                .isEqualTo(CLAIM_SUBMITTED.fullName());
            assertThat(stateFlow.getStateHistory())
                .hasSize(2)
                .extracting(State::getName)
                .containsExactly(
                    DRAFT.fullName(), CLAIM_SUBMITTED.fullName());
        }

        @Test
        void shouldReturnClaimSubmitted_whenCaseDataAtStateClaimSubmitted2v1RespondentIsRegistered() {
            // Given
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimSubmitted2v1RespondentRegistered()
                .build();

            // When
            StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

            // Then
            assertThat(stateFlow.getState())
                .extracting(State::getName)
                .isNotNull()
                .isEqualTo(CLAIM_SUBMITTED.fullName());
            assertThat(stateFlow.getStateHistory())
                .hasSize(2)
                .extracting(State::getName)
                .containsExactly(
                    DRAFT.fullName(), CLAIM_SUBMITTED.fullName());
        }

        // remove this when NoC is implemented
        @Nested
        class UnrepresentedTakenOfflineBeforeNoC {
            // 1v1 Unrepresented
            @Test
            void shouldReturnProceedsWithOfflineJourney_1v1_whenCaseDataAtStateClaimDraftIssuedAndResUnrepresented() {
                // Given
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateClaimIssued1v1UnrepresentedDefendant()
                    .defendant1LIPAtClaimIssued(null)
                    .defendant2LIPAtClaimIssued(null)
                    .build();

                // When
                StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

                // Then
                assertThat(stateFlow.getState())
                    .extracting(State::getName)
                    .isNotNull()
                    .isEqualTo(TAKEN_OFFLINE_UNREPRESENTED_DEFENDANT.fullName());
                assertThat(stateFlow.getStateHistory())
                    .hasSize(5)
                    .extracting(State::getName)
                    .containsExactly(
                        DRAFT.fullName(), CLAIM_SUBMITTED.fullName(), CLAIM_ISSUED_PAYMENT_SUCCESSFUL.fullName(),
                        PENDING_CLAIM_ISSUED_UNREPRESENTED_DEFENDANT.fullName(),
                        TAKEN_OFFLINE_UNREPRESENTED_DEFENDANT.fullName()
                    );
                assertThat(stateFlow.getFlags()).hasSize(3).contains(
                    entry(FlowFlag.NOTICE_OF_CHANGE.name(), false),
                    entry(FlowFlag.CERTIFICATE_OF_SERVICE.name(), false),
                    entry(FlowFlag.GENERAL_APPLICATION_ENABLED.name(), false)
                );
            }

            // 1v2
            // Unrepresented
            // 1. Both def1 and def2 unrepresented
            @Test
            void shouldReturnProceedsWithOfflineJourney_whenCaseDataAtStateClaimDraftIssuedRespondentsNotRepresented() {
                // Given
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateClaimIssuedUnrepresentedDefendants()
                    .defendant1LIPAtClaimIssued(null)
                    .defendant2LIPAtClaimIssued(null)
                    .build();

                // When
                StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

                // Then
                assertThat(stateFlow.getState())
                    .extracting(State::getName)
                    .isNotNull()
                    .isEqualTo(TAKEN_OFFLINE_UNREPRESENTED_DEFENDANT.fullName());
                assertThat(stateFlow.getStateHistory())
                    .hasSize(5)
                    .extracting(State::getName)
                    .containsExactly(
                        DRAFT.fullName(), CLAIM_SUBMITTED.fullName(), CLAIM_ISSUED_PAYMENT_SUCCESSFUL.fullName(),
                        PENDING_CLAIM_ISSUED_UNREPRESENTED_DEFENDANT.fullName(),
                        TAKEN_OFFLINE_UNREPRESENTED_DEFENDANT.fullName()
                    );
                assertThat(stateFlow.getFlags()).hasSize(3).contains(
                    entry(FlowFlag.NOTICE_OF_CHANGE.name(), false),
                    entry(FlowFlag.CERTIFICATE_OF_SERVICE.name(), false),
                    entry(FlowFlag.GENERAL_APPLICATION_ENABLED.name(), false)
                );
            }

            // Unrepresented
            // 2. Def1 unrepresented, Def2 registered
            @Test
            void shouldReturnProceedsWithOfflineJourney_whenCaseDataAtStateClaimDraftIssuedRespondent1NotRepresented() {
                // Given
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateClaimIssuedUnrepresentedDefendant1()
                    .defendant1LIPAtClaimIssued(null)
                    .build();

                // When
                StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

                // Then
                assertThat(stateFlow.getState())
                    .extracting(State::getName)
                    .isNotNull()
                    .isEqualTo(TAKEN_OFFLINE_UNREPRESENTED_DEFENDANT.fullName());
                assertThat(stateFlow.getStateHistory())
                    .hasSize(5)
                    .extracting(State::getName)
                    .containsExactly(
                        DRAFT.fullName(), CLAIM_SUBMITTED.fullName(), CLAIM_ISSUED_PAYMENT_SUCCESSFUL.fullName(),
                        PENDING_CLAIM_ISSUED_UNREPRESENTED_DEFENDANT.fullName(),
                        TAKEN_OFFLINE_UNREPRESENTED_DEFENDANT.fullName()
                    );
                assertThat(stateFlow.getFlags()).hasSize(3).contains(
                    entry(FlowFlag.NOTICE_OF_CHANGE.name(), false),
                    entry(FlowFlag.CERTIFICATE_OF_SERVICE.name(), false),
                    entry(FlowFlag.GENERAL_APPLICATION_ENABLED.name(), false)
                );
            }

            // Unrepresented
            // 3. Def1 registered, Def 2 unrepresented
            @Test
            void shouldReturnProceedsWithOfflineJourney_whenCaseDataAtStateClaimDraftIssuedRespondent2NotRepresented() {
                // Given
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateClaimIssuedUnrepresentedDefendant2()
                    .defendant2LIPAtClaimIssued(null)
                    .build();

                // When
                StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

                // Then
                assertThat(stateFlow.getState())
                    .extracting(State::getName)
                    .isNotNull()
                    .isEqualTo(TAKEN_OFFLINE_UNREPRESENTED_DEFENDANT.fullName());
                assertThat(stateFlow.getStateHistory())
                    .hasSize(5)
                    .extracting(State::getName)
                    .containsExactly(
                        DRAFT.fullName(), CLAIM_SUBMITTED.fullName(), CLAIM_ISSUED_PAYMENT_SUCCESSFUL.fullName(),
                        PENDING_CLAIM_ISSUED_UNREPRESENTED_DEFENDANT.fullName(),
                        TAKEN_OFFLINE_UNREPRESENTED_DEFENDANT.fullName()
                    );
                assertThat(stateFlow.getFlags()).hasSize(3).contains(
                    entry(FlowFlag.NOTICE_OF_CHANGE.name(), false),
                    entry(FlowFlag.CERTIFICATE_OF_SERVICE.name(), false),
                    entry(FlowFlag.GENERAL_APPLICATION_ENABLED.name(), false)
                );
            }
        }

        @Nested
        class UnrepresentedDefendant {
            @Test
            void shouldGoOffline_whenDeadlinePassed() {
                // Given
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateClaimIssuedUnrepresentedDefendants()
                    .defendant2LIPAtClaimIssued(YES)
                    .defendant1LIPAtClaimIssued(YES)
                    .claimNotificationDeadline(LocalDateTime.now().minusDays(1))
                    .build();

                // When
                StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

                // Then
                assertThat(stateFlow.getState())
                    .extracting(State::getName)
                    .isNotNull()
                    .isEqualTo(PAST_CLAIM_NOTIFICATION_DEADLINE_AWAITING_CAMUNDA.fullName());
                assertThat(stateFlow.getStateHistory())
                    .hasSize(6)
                    .extracting(State::getName)
                    .containsExactly(
                        DRAFT.fullName(), CLAIM_SUBMITTED.fullName(), CLAIM_ISSUED_PAYMENT_SUCCESSFUL.fullName(),
                        PENDING_CLAIM_ISSUED_UNREPRESENTED_DEFENDANT.fullName(), CLAIM_ISSUED.fullName(),
                        PAST_CLAIM_NOTIFICATION_DEADLINE_AWAITING_CAMUNDA.fullName()
                    );

                assertThat(stateFlow.getFlags()).hasSize(5).contains(
                    entry(FlowFlag.UNREPRESENTED_DEFENDANT_ONE.name(), true),
                    entry(FlowFlag.UNREPRESENTED_DEFENDANT_TWO.name(), true),
                    entry(FlowFlag.NOTICE_OF_CHANGE.name(), false),
                    entry(FlowFlag.CERTIFICATE_OF_SERVICE.name(), false),
                    entry(FlowFlag.GENERAL_APPLICATION_ENABLED.name(), false)
                );
            }

            // 1v1
            // Unrepresented cos service not activated
            @Test
            void shouldContinueOnline_1v1_whenDefendantIsUnrepresented() {
                // Given
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateClaimIssued1v1UnrepresentedDefendant()
                    .defendant1LIPAtClaimIssued(YES)
                    .build();
                when(featureToggleService.isCertificateOfServiceEnabled()).thenReturn(false);

                // When
                StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

                // Then
                assertThat(stateFlow.getState())
                    .extracting(State::getName)
                    .isNotNull()
                    .isEqualTo(PENDING_CLAIM_ISSUED_UNREPRESENTED_DEFENDANT.fullName());
                assertThat(stateFlow.getStateHistory())
                    .hasSize(4)
                    .extracting(State::getName)
                    .containsExactly(
                        DRAFT.fullName(), CLAIM_SUBMITTED.fullName(), CLAIM_ISSUED_PAYMENT_SUCCESSFUL.fullName(),
                        PENDING_CLAIM_ISSUED_UNREPRESENTED_DEFENDANT.fullName()
                    );

                assertThat(stateFlow.getFlags()).hasSize(4).contains(
                    entry(FlowFlag.UNREPRESENTED_DEFENDANT_ONE.name(), true),
                    entry(FlowFlag.NOTICE_OF_CHANGE.name(), false),
                    entry(FlowFlag.CERTIFICATE_OF_SERVICE.name(), false),
                    entry(FlowFlag.GENERAL_APPLICATION_ENABLED.name(), false),
                    entry(FlowFlag.CERTIFICATE_OF_SERVICE.name(), false)
                );
            }

            // Unrepresented cos service activated
            @Test
            void shouldContinueOnline_1v1_cos_whenDefendantIsUnrepresented() {
                // Given
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateClaimIssued1v1UnrepresentedDefendant()
                    .defendant2LIPAtClaimIssued(YES)
                    .build();
                when(featureToggleService.isCertificateOfServiceEnabled()).thenReturn(true);

                // When
                StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

                // Then
                assertThat(stateFlow.getState())
                    .extracting(State::getName)
                    .isNotNull()
                    .isEqualTo(PENDING_CLAIM_ISSUED_UNREPRESENTED_DEFENDANT.fullName());
                assertThat(stateFlow.getStateHistory())
                    .hasSize(4)
                    .extracting(State::getName)
                    .containsExactly(
                        DRAFT.fullName(), CLAIM_SUBMITTED.fullName(), CLAIM_ISSUED_PAYMENT_SUCCESSFUL.fullName(),
                        PENDING_CLAIM_ISSUED_UNREPRESENTED_DEFENDANT.fullName()
                    );

                assertThat(stateFlow.getFlags()).hasSize(4).contains(
                    entry(FlowFlag.UNREPRESENTED_DEFENDANT_ONE.name(), true),
                    entry(FlowFlag.NOTICE_OF_CHANGE.name(), false),
                    entry(FlowFlag.GENERAL_APPLICATION_ENABLED.name(), false),
                    entry(FlowFlag.CERTIFICATE_OF_SERVICE.name(), true)
                );
            }

            // 1v1 spec
            // Unrepresented
            @Test
            void shouldContinueOnline_1v1Spec_whenDefendantIsUnrepresented() {
                // Given
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateClaimIssued1v1UnrepresentedDefendant()
                    .defendant1LIPAtClaimIssued(YES)
                    .build().toBuilder()
                    .takenOfflineDate(null)
                    .caseAccessCategory(SPEC_CLAIM).build();

                // When
                StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

                // Then
                assertThat(stateFlow.getState())
                    .extracting(State::getName)
                    .isNotNull()
                    .isEqualTo(PENDING_CLAIM_ISSUED_UNREPRESENTED_DEFENDANT_ONE_V_ONE_SPEC.fullName());
                assertThat(stateFlow.getStateHistory())
                    .hasSize(4)
                    .extracting(State::getName)
                    .containsExactly(
                        SPEC_DRAFT.fullName(), CLAIM_SUBMITTED.fullName(), CLAIM_ISSUED_PAYMENT_SUCCESSFUL.fullName(),
                        PENDING_CLAIM_ISSUED_UNREPRESENTED_DEFENDANT_ONE_V_ONE_SPEC.fullName()
                    );

                assertThat(stateFlow.getFlags()).hasSize(4).contains(
                    entry(FlowFlag.UNREPRESENTED_DEFENDANT_ONE.name(), true),
                    entry(FlowFlag.NOTICE_OF_CHANGE.name(), false),
                    entry(FlowFlag.CERTIFICATE_OF_SERVICE.name(), false),
                    entry(FlowFlag.GENERAL_APPLICATION_ENABLED.name(), false)
                );
            }

            // 1v2
            // Unrepresented
            // 1. Both def1 and def2 unrepresented
            @Test
            void shouldContinueOnline_WhenBothDefendantsAreUnrepresented() {
                // Given
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateClaimIssuedUnrepresentedDefendants()
                    .defendant1LIPAtClaimIssued(YES)
                    .defendant2LIPAtClaimIssued(YES)
                    .build();
                when(featureToggleService.isCertificateOfServiceEnabled()).thenReturn(false);

                // When
                StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

                // Then
                assertThat(stateFlow.getState())
                    .extracting(State::getName)
                    .isNotNull()
                    .isEqualTo(PENDING_CLAIM_ISSUED_UNREPRESENTED_DEFENDANT.fullName());
                assertThat(stateFlow.getStateHistory())
                    .hasSize(4)
                    .extracting(State::getName)
                    .containsExactly(
                        DRAFT.fullName(), CLAIM_SUBMITTED.fullName(), CLAIM_ISSUED_PAYMENT_SUCCESSFUL.fullName(),
                        PENDING_CLAIM_ISSUED_UNREPRESENTED_DEFENDANT.fullName()
                    );

                assertThat(stateFlow.getFlags()).hasSize(5).contains(
                    entry(FlowFlag.UNREPRESENTED_DEFENDANT_ONE.name(), true),
                    entry(FlowFlag.UNREPRESENTED_DEFENDANT_TWO.name(), true),
                    entry(FlowFlag.NOTICE_OF_CHANGE.name(), false),
                    entry(FlowFlag.CERTIFICATE_OF_SERVICE.name(), false),
                    entry(FlowFlag.GENERAL_APPLICATION_ENABLED.name(), false)
                );
            }

            // Unrepresented
            // 2. Def1 unrepresented, Def2 registered when cos service is not activated
            @Test
            void shouldContinueOnline_WhenCaseDataAtStateClaimDraftIssuedAndRespondent1NotRepresented() {
                // Given
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateClaimIssuedUnrepresentedDefendant1()
                    .build();
                when(featureToggleService.isCertificateOfServiceEnabled()).thenReturn(false);

                // When
                StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

                // Then
                assertThat(stateFlow.getState())
                    .extracting(State::getName)
                    .isNotNull()
                    .isEqualTo(PENDING_CLAIM_ISSUED_UNREPRESENTED_DEFENDANT.fullName());
                assertThat(stateFlow.getStateHistory())
                    .hasSize(4)
                    .extracting(State::getName)
                    .containsExactly(
                        DRAFT.fullName(), CLAIM_SUBMITTED.fullName(), CLAIM_ISSUED_PAYMENT_SUCCESSFUL.fullName(),
                        PENDING_CLAIM_ISSUED_UNREPRESENTED_DEFENDANT.fullName()
                    );

                assertThat(stateFlow.getFlags()).hasSize(5).contains(
                    entry(FlowFlag.UNREPRESENTED_DEFENDANT_ONE.name(), true),
                    entry(FlowFlag.UNREPRESENTED_DEFENDANT_TWO.name(), false),
                    entry(FlowFlag.NOTICE_OF_CHANGE.name(), false),
                    entry(FlowFlag.CERTIFICATE_OF_SERVICE.name(), false),
                    entry(FlowFlag.GENERAL_APPLICATION_ENABLED.name(), false)
                );
            }

            // Unrepresented
            // 2. Def1 unrepresented, Def2 registered
            @Test
            void shouldContinueOnline_Cos_WhenCaseDataAtStateClaimDraftIssuedAndRespondent1NotRepresented() {
                // Given
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateClaimIssuedUnrepresentedDefendant1()
                    .build();
                when(featureToggleService.isCertificateOfServiceEnabled()).thenReturn(true);

                // When
                StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

                // Then
                assertThat(stateFlow.getState())
                    .extracting(State::getName)
                    .isNotNull()
                    .isEqualTo(PENDING_CLAIM_ISSUED_UNREPRESENTED_DEFENDANT.fullName());
                assertThat(stateFlow.getStateHistory())
                    .hasSize(4)
                    .extracting(State::getName)
                    .containsExactly(
                        DRAFT.fullName(), CLAIM_SUBMITTED.fullName(), CLAIM_ISSUED_PAYMENT_SUCCESSFUL.fullName(),
                        PENDING_CLAIM_ISSUED_UNREPRESENTED_DEFENDANT.fullName()
                    );

                assertThat(stateFlow.getFlags()).hasSize(5).contains(
                    entry(FlowFlag.UNREPRESENTED_DEFENDANT_ONE.name(), true),
                    entry(FlowFlag.UNREPRESENTED_DEFENDANT_TWO.name(), false),
                    entry(FlowFlag.NOTICE_OF_CHANGE.name(), false),
                    entry(FlowFlag.CERTIFICATE_OF_SERVICE.name(), true),
                    entry(FlowFlag.GENERAL_APPLICATION_ENABLED.name(), false)
                );
            }

            // Unrepresented
            // 3. Def1 registered, Def 2 unrepresented when Cos service not activated
            @Test
            void shouldContinueOnline_WhenCaseDataAtStateClaimDraftIssuedAndRespondent2NotRepresented() {
                // Given
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateClaimIssuedUnrepresentedDefendant2()
                    .defendant2LIPAtClaimIssued(YES)
                    .build();
                when(featureToggleService.isCertificateOfServiceEnabled()).thenReturn(false);

                // When
                StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

                // Then
                assertThat(stateFlow.getState())
                    .extracting(State::getName)
                    .isNotNull()
                    .isEqualTo(PENDING_CLAIM_ISSUED_UNREPRESENTED_DEFENDANT.fullName());
                assertThat(stateFlow.getStateHistory())
                    .hasSize(4)
                    .extracting(State::getName)
                    .containsExactly(
                        DRAFT.fullName(), CLAIM_SUBMITTED.fullName(), CLAIM_ISSUED_PAYMENT_SUCCESSFUL.fullName(),
                        PENDING_CLAIM_ISSUED_UNREPRESENTED_DEFENDANT.fullName()
                    );

                assertThat(stateFlow.getFlags()).hasSize(5).contains(
                    entry(FlowFlag.UNREPRESENTED_DEFENDANT_ONE.name(), false),
                    entry(FlowFlag.UNREPRESENTED_DEFENDANT_TWO.name(), true),
                    entry(FlowFlag.NOTICE_OF_CHANGE.name(), false),
                    entry(FlowFlag.CERTIFICATE_OF_SERVICE.name(), false),
                    entry(FlowFlag.GENERAL_APPLICATION_ENABLED.name(), false)
                );
            }

            // Unrepresented
            // 3. Def1 registered, Def 2 unrepresented when Cos service activated
            @Test
            void shouldContinueOnline_Cos_WhenCaseDataAtStateClaimDraftIssuedAndRespondent2NotRepresented() {
                // Given
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateClaimIssuedUnrepresentedDefendant2()
                    .defendant2LIPAtClaimIssued(YES)
                    .build();

                when(featureToggleService.isCertificateOfServiceEnabled()).thenReturn(true);

                // When
                StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

                // Then
                assertThat(stateFlow.getState())
                    .extracting(State::getName)
                    .isNotNull()
                    .isEqualTo(PENDING_CLAIM_ISSUED_UNREPRESENTED_DEFENDANT.fullName());
                assertThat(stateFlow.getStateHistory())
                    .hasSize(4)
                    .extracting(State::getName)
                    .containsExactly(
                        DRAFT.fullName(), CLAIM_SUBMITTED.fullName(), CLAIM_ISSUED_PAYMENT_SUCCESSFUL.fullName(),
                        PENDING_CLAIM_ISSUED_UNREPRESENTED_DEFENDANT.fullName()
                    );

                assertThat(stateFlow.getFlags()).hasSize(5).contains(
                    entry(FlowFlag.UNREPRESENTED_DEFENDANT_ONE.name(), false),
                    entry(FlowFlag.UNREPRESENTED_DEFENDANT_TWO.name(), true),
                    entry(FlowFlag.NOTICE_OF_CHANGE.name(), false),
                    entry(FlowFlag.GENERAL_APPLICATION_ENABLED.name(), false),
                    entry(FlowFlag.CERTIFICATE_OF_SERVICE.name(), true)
                );
            }

            // 1v2 spec
            // Unrepresented
            // 3. Def1 registered, Def 2 unrepresented
            @Test
            void shouldContinueOnline_WhenCaseDataAtStateClaimDraftIssuedAndRespondent2NotRepresentedSpec() {
                // Given
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateClaimIssuedUnrepresentedDefendant2()
                    .defendant2LIPAtClaimIssued(YES)
                    .build().toBuilder()
                    .caseAccessCategory(SPEC_CLAIM).build();

                // When
                StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

                // Then
                assertThat(stateFlow.getState())
                    .extracting(State::getName)
                    .isNotNull()
                    .isEqualTo(PENDING_CLAIM_ISSUED_UNREPRESENTED_DEFENDANT.fullName());
                assertThat(stateFlow.getStateHistory())
                    .hasSize(4)
                    .extracting(State::getName)
                    .containsExactly(
                        SPEC_DRAFT.fullName(), CLAIM_SUBMITTED.fullName(), CLAIM_ISSUED_PAYMENT_SUCCESSFUL.fullName(),
                        PENDING_CLAIM_ISSUED_UNREPRESENTED_DEFENDANT.fullName()
                    );

                assertThat(stateFlow.getFlags()).hasSize(5).contains(
                    entry(FlowFlag.UNREPRESENTED_DEFENDANT_TWO.name(), true),
                    entry(FlowFlag.NOTICE_OF_CHANGE.name(), false),
                    entry(FlowFlag.CERTIFICATE_OF_SERVICE.name(), false),
                    entry(FlowFlag.GENERAL_APPLICATION_ENABLED.name(), false)
                );
            }
        }

        @Nested
        class UnregisteredSolicitor {
            // 1v1 Unregistered
            @Test
            void shouldReturnProceedsWithOfflineJourney_1v1_whenCaseDataAtStateClaimDraftIssuedAndResUnregistered() {
                // Given
                CaseData caseData = CaseDataBuilder.builder().atStateProceedsOffline1v1UnregisteredDefendant().build();

                // When
                StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

                // Then
                assertThat(stateFlow.getState())
                    .extracting(State::getName)
                    .isNotNull()
                    .isEqualTo(TAKEN_OFFLINE_UNREGISTERED_DEFENDANT.fullName());
                assertThat(stateFlow.getStateHistory())
                    .hasSize(5)
                    .extracting(State::getName)
                    .containsExactly(
                        DRAFT.fullName(), CLAIM_SUBMITTED.fullName(), CLAIM_ISSUED_PAYMENT_SUCCESSFUL.fullName(),
                        PENDING_CLAIM_ISSUED_UNREGISTERED_DEFENDANT.fullName(),
                        TAKEN_OFFLINE_UNREGISTERED_DEFENDANT.fullName()
                    );

                assertThat(stateFlow.getFlags()).hasSize(4).contains(
                    entry("ONE_RESPONDENT_REPRESENTATIVE", true),
                    entry(FlowFlag.NOTICE_OF_CHANGE.name(), false),
                    entry(FlowFlag.CERTIFICATE_OF_SERVICE.name(), false),
                    entry(FlowFlag.GENERAL_APPLICATION_ENABLED.name(), false)
                );
            }


            // Unregistered
            // 1. Both def1 and def2 unregistered
            @Test
            void shouldReturnProceedsWithOfflineJourney_whenCaseDataAtStateClaimDraftIssuedRespondentsNotRegistered() {
                // Given
                CaseData caseData = CaseDataBuilder.builder().atStateProceedsOfflineUnregisteredDefendants().build();

                // When
                StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

                // Then
                assertThat(stateFlow.getState())
                    .extracting(State::getName)
                    .isNotNull()
                    .isEqualTo(TAKEN_OFFLINE_UNREGISTERED_DEFENDANT.fullName());
                assertThat(stateFlow.getStateHistory())
                    .hasSize(5)
                    .extracting(State::getName)
                    .containsExactly(
                        DRAFT.fullName(), CLAIM_SUBMITTED.fullName(), CLAIM_ISSUED_PAYMENT_SUCCESSFUL.fullName(),
                        PENDING_CLAIM_ISSUED_UNREGISTERED_DEFENDANT.fullName(),
                        TAKEN_OFFLINE_UNREGISTERED_DEFENDANT.fullName()
                    );
                assertThat(stateFlow.getFlags()).hasSize(3).contains(
                    entry(FlowFlag.NOTICE_OF_CHANGE.name(), false),
                    entry(FlowFlag.CERTIFICATE_OF_SERVICE.name(), false),
                    entry(FlowFlag.GENERAL_APPLICATION_ENABLED.name(), false)
                );
            }

            // Spec
            // Unregistered
            // 1. Both def1 and def2 unregistered
            @Test
            void shouldReturnProceedsWithOfflineJourney_whenRespondentsNotRegisteredSpec() {
                // Given
                CaseData caseData = CaseDataBuilder.builder().atStateProceedsOfflineUnregisteredDefendants().build()
                    .toBuilder().caseAccessCategory(SPEC_CLAIM).build();

                // When
                StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

                // Then
                assertThat(stateFlow.getState())
                    .extracting(State::getName)
                    .isNotNull()
                    .isEqualTo(TAKEN_OFFLINE_UNREGISTERED_DEFENDANT.fullName());
                assertThat(stateFlow.getStateHistory())
                    .hasSize(5)
                    .extracting(State::getName)
                    .containsExactly(
                        SPEC_DRAFT.fullName(), CLAIM_SUBMITTED.fullName(), CLAIM_ISSUED_PAYMENT_SUCCESSFUL.fullName(),
                        PENDING_CLAIM_ISSUED_UNREGISTERED_DEFENDANT.fullName(),
                        TAKEN_OFFLINE_UNREGISTERED_DEFENDANT.fullName()
                    );
                assertThat(stateFlow.getFlags()).hasSize(3).contains(
                    entry(FlowFlag.NOTICE_OF_CHANGE.name(), false),
                    entry(FlowFlag.CERTIFICATE_OF_SERVICE.name(), false),
                    entry(FlowFlag.GENERAL_APPLICATION_ENABLED.name(), false)
                );
            }

            // Unregistered
            // 2. Def1 unregistered, Def2 registered
            @Test
            void shouldReturnProceedsWithOfflineJourney_whenCaseDataAtStateClaimDraftIssuedRespondent1NotRegistered() {
                // Given
                CaseData caseData = CaseDataBuilder.builder().atStateProceedsOfflineUnregisteredDefendant1().build();

                // When
                StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

                // Then
                assertThat(stateFlow.getState())
                    .extracting(State::getName)
                    .isNotNull()
                    .isEqualTo(TAKEN_OFFLINE_UNREGISTERED_DEFENDANT.fullName());
                assertThat(stateFlow.getStateHistory())
                    .hasSize(5)
                    .extracting(State::getName)
                    .containsExactly(
                        DRAFT.fullName(), CLAIM_SUBMITTED.fullName(), CLAIM_ISSUED_PAYMENT_SUCCESSFUL.fullName(),
                        PENDING_CLAIM_ISSUED_UNREGISTERED_DEFENDANT.fullName(),
                        TAKEN_OFFLINE_UNREGISTERED_DEFENDANT.fullName()
                    );

                assertThat(stateFlow.getFlags()).hasSize(4).contains(
                    entry("ONE_RESPONDENT_REPRESENTATIVE", true),
                    entry(FlowFlag.NOTICE_OF_CHANGE.name(), false),
                    entry(FlowFlag.CERTIFICATE_OF_SERVICE.name(), false),
                    entry(FlowFlag.GENERAL_APPLICATION_ENABLED.name(), false)
                );
            }

            // Unregistered
            // 3. Def1 registered, Def 2 unregistered
            @Test
            void shouldReturnProceedsWithOfflineJourney_whenCaseDataAtStateClaimDraftIssuedRespondent2NotRegistered() {
                // Given
                CaseData caseData = CaseDataBuilder.builder().atStateProceedsOfflineUnregisteredDefendant2().build();
                if (caseData.getRespondent2OrgRegistered() != null
                    && caseData.getRespondent2Represented() == null) {
                    caseData = caseData.toBuilder()
                        .respondent2Represented(YES)
                        .build();
                }

                // When
                StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

                // Then
                assertThat(stateFlow.getState())
                    .extracting(State::getName)
                    .isNotNull()
                    .isEqualTo(TAKEN_OFFLINE_UNREGISTERED_DEFENDANT.fullName());
                assertThat(stateFlow.getStateHistory())
                    .hasSize(5)
                    .extracting(State::getName)
                    .containsExactly(
                        DRAFT.fullName(), CLAIM_SUBMITTED.fullName(), CLAIM_ISSUED_PAYMENT_SUCCESSFUL.fullName(),
                        PENDING_CLAIM_ISSUED_UNREGISTERED_DEFENDANT.fullName(),
                        TAKEN_OFFLINE_UNREGISTERED_DEFENDANT.fullName()
                    );

                assertThat(stateFlow.getFlags()).hasSize(4).contains(
                    entry("ONE_RESPONDENT_REPRESENTATIVE", true),
                    entry(FlowFlag.NOTICE_OF_CHANGE.name(), false),
                    entry(FlowFlag.CERTIFICATE_OF_SERVICE.name(), false),
                    entry(FlowFlag.GENERAL_APPLICATION_ENABLED.name(), false)
                );
            }

            // 1v2 Same Unregistered Solicitor
            @Test
            void shouldReturnProceedsWithOfflineJourney_whenCaseDataAtStateClaimDraftIssuedSameUnregisteredSolicitor() {
                // Given
                CaseData caseData = CaseDataBuilder.builder().atStateProceedsOfflineSameUnregisteredDefendant().build();

                // When
                StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

                // Then
                assertThat(stateFlow.getState())
                    .extracting(State::getName)
                    .isNotNull()
                    .isEqualTo(TAKEN_OFFLINE_UNREGISTERED_DEFENDANT.fullName());
                assertThat(stateFlow.getStateHistory())
                    .hasSize(5)
                    .extracting(State::getName)
                    .containsExactly(
                        DRAFT.fullName(), CLAIM_SUBMITTED.fullName(), CLAIM_ISSUED_PAYMENT_SUCCESSFUL.fullName(),
                        PENDING_CLAIM_ISSUED_UNREGISTERED_DEFENDANT.fullName(),
                        TAKEN_OFFLINE_UNREGISTERED_DEFENDANT.fullName()
                    );

                assertThat(stateFlow.getFlags()).hasSize(4).contains(
                    entry("ONE_RESPONDENT_REPRESENTATIVE", true),
                    entry(FlowFlag.NOTICE_OF_CHANGE.name(), false),
                    entry(FlowFlag.CERTIFICATE_OF_SERVICE.name(), false),
                    entry(FlowFlag.GENERAL_APPLICATION_ENABLED.name(), false)
                );
            }
        }

        @Nested
        class UnrepresentedAndUnregistered {
            // Def1 unrepresented, Def2 unregistered
            @Test
            void shouldReturnProceedsWithOfflineJourney_whenCaseDataAtStateClaimDraftIssuedAndRes1UnrepRes2Unregis() {
                // Given
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateProceedsOfflineUnrepresentedDefendant1UnregisteredDefendant2().build();

                // When
                StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

                // Then
                assertThat(stateFlow.getState())
                    .extracting(State::getName)
                    .isNotNull()
                    .isEqualTo(TAKEN_OFFLINE_UNREPRESENTED_UNREGISTERED_DEFENDANT.fullName());
                assertThat(stateFlow.getStateHistory())
                    .hasSize(5)
                    .extracting(State::getName)
                    .containsExactly(
                        DRAFT.fullName(), CLAIM_SUBMITTED.fullName(), CLAIM_ISSUED_PAYMENT_SUCCESSFUL.fullName(),
                        PENDING_CLAIM_ISSUED_UNREPRESENTED_UNREGISTERED_DEFENDANT.fullName(),
                        TAKEN_OFFLINE_UNREPRESENTED_UNREGISTERED_DEFENDANT.fullName()
                    );
                assertThat(stateFlow.getFlags()).hasSize(3).contains(
                    entry(FlowFlag.NOTICE_OF_CHANGE.name(), false),
                    entry(FlowFlag.CERTIFICATE_OF_SERVICE.name(), false),
                    entry(FlowFlag.GENERAL_APPLICATION_ENABLED.name(), false)
                );
            }

            // Unrepresented and Unregistered
            // 2. Def1 unregistered, Def 2 unrepresented
            @Test
            void shouldReturnProceedsWithOfflineJourney_whenCaseDataAtStateClaimDraftIssuedAndRes1UnregisRes2Unrep() {
                // Given
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateProceedsOfflineUnregisteredDefendant1UnrepresentedDefendant2().build();

                // When
                StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

                // Then
                assertThat(stateFlow.getState())
                    .extracting(State::getName)
                    .isNotNull()
                    .isEqualTo(TAKEN_OFFLINE_UNREPRESENTED_UNREGISTERED_DEFENDANT.fullName());
                assertThat(stateFlow.getStateHistory())
                    .hasSize(5)
                    .extracting(State::getName)
                    .containsExactly(
                        DRAFT.fullName(), CLAIM_SUBMITTED.fullName(), CLAIM_ISSUED_PAYMENT_SUCCESSFUL.fullName(),
                        PENDING_CLAIM_ISSUED_UNREPRESENTED_UNREGISTERED_DEFENDANT.fullName(),
                        TAKEN_OFFLINE_UNREPRESENTED_UNREGISTERED_DEFENDANT.fullName()
                    );

                assertThat(stateFlow.getFlags()).hasSize(3).contains(
                    entry(FlowFlag.NOTICE_OF_CHANGE.name(), false),
                    entry(FlowFlag.CERTIFICATE_OF_SERVICE.name(), false),
                    entry(FlowFlag.GENERAL_APPLICATION_ENABLED.name(), false)
                );
            }

        }

        @Test
        void shouldReturnPaymentSuccessful_whenCaseDataAtStatePaymentSuccessful() {
            // Given
            CaseData caseData = CaseDataBuilder.builder().atStatePaymentSuccessful().build();
            if (caseData.getRespondent2OrgRegistered() != null
                && caseData.getRespondent2Represented() == null) {
                caseData = caseData.toBuilder()
                    .respondent2Represented(YES)
                    .build();
            }

            // When
            StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

            // Then
            assertThat(stateFlow.getState())
                .extracting(State::getName)
                .isNotNull()
                .isEqualTo(CLAIM_ISSUED_PAYMENT_SUCCESSFUL.fullName());
            assertThat(stateFlow.getStateHistory())
                .hasSize(3)
                .extracting(State::getName)
                .containsExactly(
                    DRAFT.fullName(), CLAIM_SUBMITTED.fullName(), CLAIM_ISSUED_PAYMENT_SUCCESSFUL.fullName());

            assertThat(stateFlow.getFlags()).hasSize(4).contains(
                entry("ONE_RESPONDENT_REPRESENTATIVE", true),
                entry(FlowFlag.NOTICE_OF_CHANGE.name(), false),
                entry(FlowFlag.CERTIFICATE_OF_SERVICE.name(), false),
                entry(FlowFlag.GENERAL_APPLICATION_ENABLED.name(), false)
            );
        }

        @Test
        void shouldReturnPaymentSuccessful_whenCaseDataAtStatePaymentSuccessful1v2SameRepresentative() {
            // Given
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimIssued1v2AndSameRepresentative().build();

            // When
            StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

            // Then
            assertThat(stateFlow.getState())
                .extracting(State::getName)
                .isNotNull()
                .isEqualTo(CLAIM_ISSUED_PAYMENT_SUCCESSFUL.fullName());
            assertThat(stateFlow.getStateHistory())
                .hasSize(3)
                .extracting(State::getName)
                .containsExactly(
                    DRAFT.fullName(), CLAIM_SUBMITTED.fullName(), CLAIM_ISSUED_PAYMENT_SUCCESSFUL.fullName());

            assertThat(stateFlow.getFlags()).hasSize(4).contains(
                entry("ONE_RESPONDENT_REPRESENTATIVE", true),
                entry(FlowFlag.NOTICE_OF_CHANGE.name(), false),
                entry(FlowFlag.GENERAL_APPLICATION_ENABLED.name(), false),
                entry(FlowFlag.CERTIFICATE_OF_SERVICE.name(), false)
            );
        }

        @Test
        void shouldReturnPaymentFailed_whenCaseDataAtStatePaymentFailed() {
            // Given
            CaseData caseData = CaseDataBuilder.builder().atStateClaimIssuedPaymentFailed().build();

            // When
            StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

            // Then
            assertThat(stateFlow.getState())
                .extracting(State::getName)
                .isNotNull()
                .isEqualTo(CLAIM_ISSUED_PAYMENT_FAILED.fullName());
            assertThat(stateFlow.getStateHistory())
                .hasSize(3)
                .extracting(State::getName)
                .containsExactly(
                    DRAFT.fullName(), CLAIM_SUBMITTED.fullName(), CLAIM_ISSUED_PAYMENT_FAILED.fullName());

            assertThat(stateFlow.getFlags()).hasSize(4).contains(
                entry("ONE_RESPONDENT_REPRESENTATIVE", true),
                entry(FlowFlag.NOTICE_OF_CHANGE.name(), false),
                entry(FlowFlag.GENERAL_APPLICATION_ENABLED.name(), false),
                entry(FlowFlag.CERTIFICATE_OF_SERVICE.name(), false)
            );
        }

        @Test
        void shouldReturnAwaitingCaseNotification_whenCaseDataAtStateAwaitingCaseNotification() {
            // Given
            CaseData caseData = CaseDataBuilder.builder().atStatePendingClaimIssued().build();
            if (caseData.getRespondent2OrgRegistered() != null
                && caseData.getRespondent2Represented() == null) {
                caseData = caseData.toBuilder()
                    .respondent2Represented(YES)
                    .build();
            }

            // When
            StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

            // Then
            assertThat(stateFlow.getState())
                .extracting(State::getName)
                .isNotNull()
                .isEqualTo(PENDING_CLAIM_ISSUED.fullName());
            assertThat(stateFlow.getStateHistory())
                .hasSize(4)
                .extracting(State::getName)
                .containsExactly(
                    DRAFT.fullName(), CLAIM_SUBMITTED.fullName(), CLAIM_ISSUED_PAYMENT_SUCCESSFUL.fullName(),
                    PENDING_CLAIM_ISSUED.fullName()
                );

            assertThat(stateFlow.getFlags()).hasSize(4).contains(
                entry("ONE_RESPONDENT_REPRESENTATIVE", true),
                entry(FlowFlag.NOTICE_OF_CHANGE.name(), false),
                entry(FlowFlag.GENERAL_APPLICATION_ENABLED.name(), false),
                entry(FlowFlag.CERTIFICATE_OF_SERVICE.name(), false)
            );
        }

        @Test
        void shouldReturnProceedsWithOfflineJourney_whenCaseDataAtStateClaimIssued_andOneSolicitorIsToBeNotified() {
            // Given
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimNotified_1v2_andNotifyOnlyOneSolicitor()
                .build();
            if (caseData.getRespondent2OrgRegistered() != null
                && caseData.getRespondent2Represented() == null) {
                caseData = caseData.toBuilder()
                    .respondent2Represented(YES)
                    .build();
            }

            // When
            StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

            // Then
            assertThat(stateFlow.getState())
                .extracting(State::getName)
                .isNotNull()
                .isEqualTo(TAKEN_OFFLINE_AFTER_CLAIM_NOTIFIED.fullName());

            assertThat(stateFlow.getStateHistory())
                .hasSize(6)
                .extracting(State::getName)
                .containsExactly(
                    DRAFT.fullName(), CLAIM_SUBMITTED.fullName(), CLAIM_ISSUED_PAYMENT_SUCCESSFUL.fullName(),
                    PENDING_CLAIM_ISSUED.fullName(),
                    CLAIM_ISSUED.fullName(),
                    TAKEN_OFFLINE_AFTER_CLAIM_NOTIFIED.fullName()
                );
            assertThat(stateFlow.getFlags()).hasSize(5).contains(
                entry("ONE_RESPONDENT_REPRESENTATIVE", false),
                entry("TWO_RESPONDENT_REPRESENTATIVES", true),
                entry(FlowFlag.NOTICE_OF_CHANGE.name(), false),
                entry(FlowFlag.GENERAL_APPLICATION_ENABLED.name(), false),
                entry(FlowFlag.CERTIFICATE_OF_SERVICE.name(), false)
            );
        }

        @Test
        void shouldReturnClaimNotified_whenCaseDataAtStateClaimNotified_andBothSolicitorsAreToBeNotified() {
            // Given
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimNotified_1v2_andNotifyBothSolicitors()
                .build();
            if (caseData.getRespondent2OrgRegistered() != null
                && caseData.getRespondent2Represented() == null) {
                caseData = caseData.toBuilder()
                    .respondent2Represented(YES)
                    .build();
            }

            // When
            StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

            // Then
            assertThat(stateFlow.getState())
                .extracting(State::getName)
                .isNotNull()
                .isEqualTo(CLAIM_NOTIFIED.fullName());

            assertThat(stateFlow.getStateHistory())
                .hasSize(6)
                .extracting(State::getName)
                .containsExactly(
                    DRAFT.fullName(), CLAIM_SUBMITTED.fullName(), CLAIM_ISSUED_PAYMENT_SUCCESSFUL.fullName(),
                    PENDING_CLAIM_ISSUED.fullName(),
                    CLAIM_ISSUED.fullName(),
                    CLAIM_NOTIFIED.fullName()
                );
            assertThat(stateFlow.getFlags()).hasSize(5).contains(
                entry("ONE_RESPONDENT_REPRESENTATIVE", false),
                entry("TWO_RESPONDENT_REPRESENTATIVES", true),
                entry(FlowFlag.NOTICE_OF_CHANGE.name(), false),
                entry(FlowFlag.GENERAL_APPLICATION_ENABLED.name(), false),
                entry(FlowFlag.CERTIFICATE_OF_SERVICE.name(), false)
            );
        }

        @Test
        void shouldReturnAwaitingCaseNotification_whenCaseDataAtStateAwaitingCaseDetailsNotification() {
            // Given
            CaseData caseData = CaseDataBuilder.builder().atStateClaimNotified_1v1().build();
            if (caseData.getRespondent2OrgRegistered() != null
                && caseData.getRespondent2Represented() == null) {
                caseData = caseData.toBuilder()
                    .respondent2Represented(YES)
                    .build();
            }

            // When
            StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

            // Then
            assertThat(stateFlow.getState())
                .extracting(State::getName)
                .isNotNull()
                .isEqualTo(CLAIM_NOTIFIED.fullName());
            assertThat(stateFlow.getStateHistory())
                .hasSize(6)
                .extracting(State::getName)
                .containsExactly(
                    DRAFT.fullName(), CLAIM_SUBMITTED.fullName(), CLAIM_ISSUED_PAYMENT_SUCCESSFUL.fullName(),
                    PENDING_CLAIM_ISSUED.fullName(), CLAIM_ISSUED.fullName(), CLAIM_NOTIFIED.fullName()
                );

            assertThat(stateFlow.getFlags()).hasSize(4).contains(
                entry(FlowFlag.NOTICE_OF_CHANGE.name(), false),
                entry(FlowFlag.GENERAL_APPLICATION_ENABLED.name(), false),
                entry("ONE_RESPONDENT_REPRESENTATIVE", true),
                entry(FlowFlag.CERTIFICATE_OF_SERVICE.name(), false)
            );
        }

        @Test
        void shouldReturnClaimDetailsNotified_whenCaseDataAtStateClaimDetailsNotified() {
            // Given
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build();

            // When
            StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

            // Then
            assertThat(stateFlow.getState())
                .extracting(State::getName)
                .isNotNull()
                .isEqualTo(CLAIM_DETAILS_NOTIFIED.fullName());
            assertThat(stateFlow.getStateHistory())
                .hasSize(7)
                .extracting(State::getName)
                .containsExactly(
                    DRAFT.fullName(), CLAIM_SUBMITTED.fullName(), CLAIM_ISSUED_PAYMENT_SUCCESSFUL.fullName(),
                    PENDING_CLAIM_ISSUED.fullName(), CLAIM_ISSUED.fullName(), CLAIM_NOTIFIED.fullName(),
                    CLAIM_DETAILS_NOTIFIED.fullName()
                );

            assertThat(stateFlow.getFlags()).hasSize(4).contains(
                entry(FlowFlag.NOTICE_OF_CHANGE.name(), false),
                entry(FlowFlag.GENERAL_APPLICATION_ENABLED.name(), false),
                entry("ONE_RESPONDENT_REPRESENTATIVE", true),
                entry(FlowFlag.CERTIFICATE_OF_SERVICE.name(), false)
            );
        }

        @Test
        void shouldReturnClaimDetailsNotified_whenCaseDataAtStateClaimDetailsNotifiedBothSolicitors1v2() {
            // Given
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDetailsNotified_1v2_andNotifyBothSolicitors()
                .build();
            if (caseData.getRespondent2OrgRegistered() != null
                && caseData.getRespondent2Represented() == null) {
                caseData = caseData.toBuilder()
                    .respondent2Represented(YES)
                    .build();
            }

            // When
            StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

            // Then
            assertThat(stateFlow.getState())
                .extracting(State::getName)
                .isNotNull()
                .isEqualTo(CLAIM_DETAILS_NOTIFIED.fullName());
            assertThat(stateFlow.getStateHistory())
                .hasSize(7)
                .extracting(State::getName)
                .containsExactly(
                    DRAFT.fullName(), CLAIM_SUBMITTED.fullName(), CLAIM_ISSUED_PAYMENT_SUCCESSFUL.fullName(),
                    PENDING_CLAIM_ISSUED.fullName(), CLAIM_ISSUED.fullName(), CLAIM_NOTIFIED.fullName(),
                    CLAIM_DETAILS_NOTIFIED.fullName()
                );

            assertThat(stateFlow.getFlags()).hasSize(5).contains(
                entry("ONE_RESPONDENT_REPRESENTATIVE", false),
                entry("TWO_RESPONDENT_REPRESENTATIVES", true),
                entry(FlowFlag.NOTICE_OF_CHANGE.name(), false),
                entry(FlowFlag.GENERAL_APPLICATION_ENABLED.name(), false),
                entry(FlowFlag.CERTIFICATE_OF_SERVICE.name(), false)
            );
        }

        @Test
        void shouldReturnClaimDetailsNotified_whenCaseDataAtStateClaimDetailsNotifiedSingleSolicitorIn1v2() {
            // Given
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDetailsNotified_1v2_andNotifyOnlyOneSolicitor()
                .build();
            if (caseData.getRespondent2OrgRegistered() != null
                && caseData.getRespondent2Represented() == null) {
                caseData = caseData.toBuilder()
                    .respondent2Represented(YES)
                    .build();
            }

            // When
            StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

            // Then
            assertThat(stateFlow.getState())
                .extracting(State::getName)
                .isNotNull()
                .isEqualTo(TAKEN_OFFLINE_AFTER_CLAIM_DETAILS_NOTIFIED.fullName());
            assertThat(stateFlow.getStateHistory())
                .hasSize(7)
                .extracting(State::getName)
                .containsExactly(
                    DRAFT.fullName(), CLAIM_SUBMITTED.fullName(), CLAIM_ISSUED_PAYMENT_SUCCESSFUL.fullName(),
                    PENDING_CLAIM_ISSUED.fullName(), CLAIM_ISSUED.fullName(), CLAIM_NOTIFIED.fullName(),
                    TAKEN_OFFLINE_AFTER_CLAIM_DETAILS_NOTIFIED.fullName()
                );

        }

        @Test
        void shouldReturnClaimDetailsNotifiedTimeExtension_whenCaseDataAtStateClaimDetailsNotifiedTimeExtension() {
            // Given
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotifiedTimeExtension().build();

            // When
            StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

            // Then
            assertThat(stateFlow.getState())
                .extracting(State::getName)
                .isNotNull()
                .isEqualTo(CLAIM_DETAILS_NOTIFIED_TIME_EXTENSION.fullName());
            assertThat(stateFlow.getStateHistory())
                .hasSize(8)
                .extracting(State::getName)
                .containsExactly(
                    DRAFT.fullName(), CLAIM_SUBMITTED.fullName(), CLAIM_ISSUED_PAYMENT_SUCCESSFUL.fullName(),
                    PENDING_CLAIM_ISSUED.fullName(), CLAIM_ISSUED.fullName(), CLAIM_NOTIFIED.fullName(),
                    CLAIM_DETAILS_NOTIFIED.fullName(), CLAIM_DETAILS_NOTIFIED_TIME_EXTENSION.fullName()
                );

            assertThat(stateFlow.getFlags()).hasSize(4).contains(
                entry("ONE_RESPONDENT_REPRESENTATIVE", true),
                entry(FlowFlag.NOTICE_OF_CHANGE.name(), false),
                entry(FlowFlag.GENERAL_APPLICATION_ENABLED.name(), false),
                entry(FlowFlag.CERTIFICATE_OF_SERVICE.name(), false)
            );
        }

        @Test
        void shouldReturnClaimAcknowledge_whenCaseDataAtStateClaimAcknowledge() {
            // Given
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
            if (caseData.getRespondent2OrgRegistered() != null
                && caseData.getRespondent2Represented() == null) {
                caseData = caseData.toBuilder()
                    .respondent2Represented(YES)
                    .build();
            }

            // When
            StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

            // Then
            assertThat(stateFlow.getState())
                .extracting(State::getName)
                .isNotNull()
                .isEqualTo(NOTIFICATION_ACKNOWLEDGED.fullName());
            assertThat(stateFlow.getStateHistory())
                .hasSize(8)
                .extracting(State::getName)
                .containsExactly(
                    DRAFT.fullName(), CLAIM_SUBMITTED.fullName(), CLAIM_ISSUED_PAYMENT_SUCCESSFUL.fullName(),
                    PENDING_CLAIM_ISSUED.fullName(), CLAIM_ISSUED.fullName(), CLAIM_NOTIFIED.fullName(),
                    CLAIM_DETAILS_NOTIFIED.fullName(), NOTIFICATION_ACKNOWLEDGED.fullName()
                );

            assertThat(stateFlow.getFlags()).hasSize(4).contains(
                entry("ONE_RESPONDENT_REPRESENTATIVE", true),
                entry(FlowFlag.NOTICE_OF_CHANGE.name(), false),
                entry(FlowFlag.GENERAL_APPLICATION_ENABLED.name(), false),
                entry(FlowFlag.CERTIFICATE_OF_SERVICE.name(), false)
            );
        }

        @Test
        void shouldReturnNotificationAcknowledgedTimeExtension_whenCaseDataAtStateClaimAcknowledgeTimeExtension() {
            // Given
            CaseData caseData = CaseDataBuilder.builder()
                .atStateNotificationAcknowledgedRespondent1TimeExtension().build();
            if (caseData.getRespondent2OrgRegistered() != null
                && caseData.getRespondent2Represented() == null) {
                caseData = caseData.toBuilder()
                    .respondent2Represented(YES)
                    .build();
            }

            // When
            StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

            // Then
            assertThat(stateFlow.getState())
                .extracting(State::getName)
                .isNotNull()
                .isEqualTo(NOTIFICATION_ACKNOWLEDGED_TIME_EXTENSION.fullName());
            assertThat(stateFlow.getStateHistory())
                .hasSize(9)
                .extracting(State::getName)
                .containsExactly(
                    DRAFT.fullName(), CLAIM_SUBMITTED.fullName(), CLAIM_ISSUED_PAYMENT_SUCCESSFUL.fullName(),
                    PENDING_CLAIM_ISSUED.fullName(), CLAIM_ISSUED.fullName(), CLAIM_NOTIFIED.fullName(),
                    CLAIM_DETAILS_NOTIFIED.fullName(), NOTIFICATION_ACKNOWLEDGED.fullName(),
                    NOTIFICATION_ACKNOWLEDGED_TIME_EXTENSION.fullName()
                );

            assertThat(stateFlow.getFlags()).hasSize(4).contains(
                entry("ONE_RESPONDENT_REPRESENTATIVE", true),
                entry(FlowFlag.NOTICE_OF_CHANGE.name(), false),
                entry(FlowFlag.GENERAL_APPLICATION_ENABLED.name(), false),
                entry(FlowFlag.CERTIFICATE_OF_SERVICE.name(), false)
            );
        }

        @Test
        void shouldReturnClaimDismissed_whenCaseDataAtStateClaimAcknowledgeAndCcdStateIsDismissed() {
            // Given
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged()
                .claimDismissedDate(LocalDateTime.now())
                .claimDismissedDeadline(LocalDateTime.now().minusHours(4))
                .build();

            // When
            StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

            // Then
            assertThat(stateFlow.getState())
                .extracting(State::getName)
                .isNotNull()
                .isEqualTo(CLAIM_DISMISSED_PAST_CLAIM_DISMISSED_DEADLINE.fullName());
            assertThat(stateFlow.getStateHistory())
                .hasSize(10)
                .extracting(State::getName)
                .containsExactly(
                    DRAFT.fullName(),
                    CLAIM_SUBMITTED.fullName(), CLAIM_ISSUED_PAYMENT_SUCCESSFUL.fullName(),
                    PENDING_CLAIM_ISSUED.fullName(), CLAIM_ISSUED.fullName(), CLAIM_NOTIFIED.fullName(),
                    CLAIM_DETAILS_NOTIFIED.fullName(), NOTIFICATION_ACKNOWLEDGED.fullName(),
                    PAST_CLAIM_DISMISSED_DEADLINE_AWAITING_CAMUNDA.fullName(),
                    CLAIM_DISMISSED_PAST_CLAIM_DISMISSED_DEADLINE.fullName()
                );

            assertThat(stateFlow.getFlags()).hasSize(4).contains(
                entry("ONE_RESPONDENT_REPRESENTATIVE", true),
                entry(FlowFlag.NOTICE_OF_CHANGE.name(), false),
                entry(FlowFlag.GENERAL_APPLICATION_ENABLED.name(), false),
                entry(FlowFlag.CERTIFICATE_OF_SERVICE.name(), false)
            );
        }

        @Test
        void shouldReturnExtensionRequested_whenCaseDataAtStateClaimDetailsNotifiedTimeExtension() {
            // Given
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotifiedTimeExtension().build();

            // When
            StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

            // Then
            assertThat(stateFlow.getState())
                .extracting(State::getName)
                .isNotNull()
                .isEqualTo(CLAIM_DETAILS_NOTIFIED_TIME_EXTENSION.fullName());
            assertThat(stateFlow.getStateHistory())
                .hasSize(8)
                .extracting(State::getName)
                .containsExactly(
                    DRAFT.fullName(), CLAIM_SUBMITTED.fullName(), CLAIM_ISSUED_PAYMENT_SUCCESSFUL.fullName(),
                    PENDING_CLAIM_ISSUED.fullName(), CLAIM_ISSUED.fullName(), CLAIM_NOTIFIED.fullName(),
                    CLAIM_DETAILS_NOTIFIED.fullName(), CLAIM_DETAILS_NOTIFIED_TIME_EXTENSION.fullName()
                );

            assertThat(stateFlow.getFlags()).hasSize(4).contains(
                entry("ONE_RESPONDENT_REPRESENTATIVE", true),
                entry(FlowFlag.NOTICE_OF_CHANGE.name(), false),
                entry(FlowFlag.GENERAL_APPLICATION_ENABLED.name(), false),
                entry(FlowFlag.CERTIFICATE_OF_SERVICE.name(), false)
            );
        }

        @Nested
        class RespondentResponse {

            @Test
            void shouldReturnFullDefence_whenCaseDataAtStateRespondentFullDefence() {
                // Given
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateRespondentFullDefenceAfterNotificationAcknowledgement()
                    .build();

                // When
                StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

                // Then
                assertThat(stateFlow.getState())
                    .extracting(State::getName)
                    .isNotNull()
                    .isEqualTo(FULL_DEFENCE.fullName());
                assertThat(stateFlow.getStateHistory())
                    .hasSize(10)
                    .extracting(State::getName)
                    .containsExactly(
                        DRAFT.fullName(), CLAIM_SUBMITTED.fullName(), CLAIM_ISSUED_PAYMENT_SUCCESSFUL.fullName(),
                        PENDING_CLAIM_ISSUED.fullName(), CLAIM_ISSUED.fullName(), CLAIM_NOTIFIED.fullName(),
                        CLAIM_DETAILS_NOTIFIED.fullName(), NOTIFICATION_ACKNOWLEDGED.fullName(),
                        ALL_RESPONSES_RECEIVED.fullName(), FULL_DEFENCE.fullName()
                    );

                assertThat(stateFlow.getFlags()).hasSize(4).contains(
                    entry(FlowFlag.NOTICE_OF_CHANGE.name(), false),
                    entry(FlowFlag.GENERAL_APPLICATION_ENABLED.name(), false),
                    entry("ONE_RESPONDENT_REPRESENTATIVE", true),
                    entry(FlowFlag.CERTIFICATE_OF_SERVICE.name(), false)
                );
            }

            @Test
            void shouldReturnFullAdmission_whenCaseDataAtStateRespondentFullAdmission() {
                // Given
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateRespondentFullAdmissionAfterNotificationAcknowledged()
                    .build();
                if (caseData.getRespondent2OrgRegistered() != null
                    && caseData.getRespondent2Represented() == null) {
                    caseData = caseData.toBuilder()
                        .respondent2Represented(YES)
                        .build();
                }

                // When
                StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

                // Then
                assertThat(stateFlow.getState())
                    .extracting(State::getName)
                    .isNotNull()
                    .isEqualTo(FULL_ADMISSION.fullName());
                assertThat(stateFlow.getStateHistory())
                    .hasSize(10)
                    .extracting(State::getName)
                    .containsExactly(
                        DRAFT.fullName(),
                        CLAIM_SUBMITTED.fullName(),
                        CLAIM_ISSUED_PAYMENT_SUCCESSFUL.fullName(),
                        PENDING_CLAIM_ISSUED.fullName(),
                        CLAIM_ISSUED.fullName(),
                        CLAIM_NOTIFIED.fullName(),
                        CLAIM_DETAILS_NOTIFIED.fullName(),
                        NOTIFICATION_ACKNOWLEDGED.fullName(),
                        ALL_RESPONSES_RECEIVED.fullName(),
                        FULL_ADMISSION.fullName()
                    );

                assertThat(stateFlow.getFlags()).hasSize(4).contains(
                    entry(FlowFlag.NOTICE_OF_CHANGE.name(), false),
                    entry(FlowFlag.GENERAL_APPLICATION_ENABLED.name(), false),
                    entry("ONE_RESPONDENT_REPRESENTATIVE", true),
                    entry(FlowFlag.CERTIFICATE_OF_SERVICE.name(), false)
                );
            }

            @Test
            void shouldReturnPartAdmission_whenCaseDataAtStateRespondentPartAdmission() {
                // Given
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateRespondentPartAdmissionAfterNotificationAcknowledgement()
                    .build();

                // When
                StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

                // Then
                assertThat(stateFlow.getState())
                    .extracting(State::getName)
                    .isNotNull()
                    .isEqualTo(PART_ADMISSION.fullName());
                assertThat(stateFlow.getStateHistory())
                    .hasSize(10)
                    .extracting(State::getName)
                    .containsExactly(
                        DRAFT.fullName(),
                        CLAIM_SUBMITTED.fullName(),
                        CLAIM_ISSUED_PAYMENT_SUCCESSFUL.fullName(),
                        PENDING_CLAIM_ISSUED.fullName(),
                        CLAIM_ISSUED.fullName(),
                        CLAIM_NOTIFIED.fullName(),
                        CLAIM_DETAILS_NOTIFIED.fullName(),
                        NOTIFICATION_ACKNOWLEDGED.fullName(),
                        ALL_RESPONSES_RECEIVED.fullName(),
                        PART_ADMISSION.fullName()
                    );

                assertThat(stateFlow.getFlags()).hasSize(4).contains(
                    entry("ONE_RESPONDENT_REPRESENTATIVE", true),
                    entry(FlowFlag.NOTICE_OF_CHANGE.name(), false),
                    entry(FlowFlag.GENERAL_APPLICATION_ENABLED.name(), false),
                    entry(FlowFlag.CERTIFICATE_OF_SERVICE.name(), false)
                );
            }

            @Test
            void shouldReturnCounterClaim_whenCaseDataAtStateRespondentCounterClaim() {
                // Given
                CaseData caseData = CaseDataBuilder.builder().atStateRespondentCounterClaim().build();
                if (caseData.getRespondent2OrgRegistered() != null
                    && caseData.getRespondent2Represented() == null) {
                    caseData = caseData.toBuilder()
                        .respondent2Represented(YES)
                        .build();
                }

                // When
                StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

                // Then
                assertThat(stateFlow.getState())
                    .extracting(State::getName)
                    .isNotNull()
                    .isEqualTo(COUNTER_CLAIM.fullName());
                assertThat(stateFlow.getStateHistory())
                    .hasSize(10)
                    .extracting(State::getName)
                    .containsExactly(
                        DRAFT.fullName(),
                        CLAIM_SUBMITTED.fullName(),
                        CLAIM_ISSUED_PAYMENT_SUCCESSFUL.fullName(),
                        PENDING_CLAIM_ISSUED.fullName(),
                        CLAIM_ISSUED.fullName(),
                        CLAIM_NOTIFIED.fullName(),
                        CLAIM_DETAILS_NOTIFIED.fullName(),
                        NOTIFICATION_ACKNOWLEDGED.fullName(),
                        ALL_RESPONSES_RECEIVED.fullName(),
                        COUNTER_CLAIM.fullName()
                    );

                assertThat(stateFlow.getFlags()).hasSize(4).contains(
                    entry("ONE_RESPONDENT_REPRESENTATIVE", true),
                    entry(FlowFlag.NOTICE_OF_CHANGE.name(), false),
                    entry(FlowFlag.GENERAL_APPLICATION_ENABLED.name(), false),
                    entry(FlowFlag.CERTIFICATE_OF_SERVICE.name(), false)
                );
            }
        }

        @Nested
        class DefendantResponseMultiparty {

            // 1v2 Different solicitor scenario-first response FullDefence received
            @Test
            void shouldGenerateDQ_1v2DiffSol_whenFirstResponseIsFullDefence() {
                // Given
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateRespondentFullDefence()
                    .multiPartyClaimTwoDefendantSolicitors()
                    .build();
                if (caseData.getRespondent2OrgRegistered() != null
                    && caseData.getRespondent2Represented() == null) {
                    caseData = caseData.toBuilder()
                        .respondent2Represented(YES)
                        .build();
                }

                // When
                StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

                // Then
                assertThat(stateFlow.getState())
                    .extracting(State::getName)
                    .isNotNull()
                    .isEqualTo(AWAITING_RESPONSES_FULL_DEFENCE_RECEIVED.fullName());
                assertThat(stateFlow.getStateHistory())
                    .hasSize(9)
                    .extracting(State::getName)
                    .containsExactly(
                        DRAFT.fullName(), CLAIM_SUBMITTED.fullName(), CLAIM_ISSUED_PAYMENT_SUCCESSFUL.fullName(),
                        PENDING_CLAIM_ISSUED.fullName(), CLAIM_ISSUED.fullName(), CLAIM_NOTIFIED.fullName(),
                        CLAIM_DETAILS_NOTIFIED.fullName(), NOTIFICATION_ACKNOWLEDGED.fullName(),
                        AWAITING_RESPONSES_FULL_DEFENCE_RECEIVED.fullName()
                    );

                assertThat(stateFlow.getFlags()).hasSize(5).contains(
                    entry("ONE_RESPONDENT_REPRESENTATIVE", false),
                    entry("TWO_RESPONDENT_REPRESENTATIVES", true),
                    entry(FlowFlag.NOTICE_OF_CHANGE.name(), false),
                    entry(FlowFlag.GENERAL_APPLICATION_ENABLED.name(), false),
                    entry(FlowFlag.CERTIFICATE_OF_SERVICE.name(), false)
                );
            }

            //1v2 Different solicitor scenario-first response FullDefence received
            @Test
            void shouldGenerateDQ_1v2DiffSol_whenFirstResponseIsNotFullDefence() {
                // Given
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateRespondentCounterClaim()
                    .multiPartyClaimTwoDefendantSolicitors()
                    .build();
                if (caseData.getRespondent2OrgRegistered() != null
                    && caseData.getRespondent2Represented() == null) {
                    caseData = caseData.toBuilder()
                        .respondent2Represented(YES)
                        .build();
                }

                // When
                StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

                // Then
                assertThat(stateFlow.getState())
                    .extracting(State::getName)
                    .isNotNull()
                    .isEqualTo(AWAITING_RESPONSES_NOT_FULL_DEFENCE_RECEIVED.fullName());
                assertThat(stateFlow.getStateHistory())
                    .hasSize(9)
                    .extracting(State::getName)
                    .containsExactly(
                        DRAFT.fullName(), CLAIM_SUBMITTED.fullName(), CLAIM_ISSUED_PAYMENT_SUCCESSFUL.fullName(),
                        PENDING_CLAIM_ISSUED.fullName(), CLAIM_ISSUED.fullName(), CLAIM_NOTIFIED.fullName(),
                        CLAIM_DETAILS_NOTIFIED.fullName(), NOTIFICATION_ACKNOWLEDGED.fullName(),
                        AWAITING_RESPONSES_NOT_FULL_DEFENCE_RECEIVED.fullName()
                    );

                assertThat(stateFlow.getFlags()).hasSize(5).contains(
                    entry("ONE_RESPONDENT_REPRESENTATIVE", false),
                    entry("TWO_RESPONDENT_REPRESENTATIVES", true),
                    entry(FlowFlag.NOTICE_OF_CHANGE.name(), false),
                    entry(FlowFlag.GENERAL_APPLICATION_ENABLED.name(), false),
                    entry(FlowFlag.CERTIFICATE_OF_SERVICE.name(), false)
                );
            }

            //1v2 Different solicitor scenario-first response FullDefence received
            @Test
            void shouldGenerateDQ_in1v2Scenario_whenFirstPartySubmitFullDefenceResponse() {
                // Given
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateRespondentFullDefence()
                    .multiPartyClaimTwoDefendantSolicitors()
                    .build();
                if (caseData.getRespondent2OrgRegistered() != null
                    && caseData.getRespondent2Represented() == null) {
                    caseData = caseData.toBuilder()
                        .respondent2Represented(YES)
                        .build();
                }

                // When
                StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

                // Then
                assertThat(stateFlow.getState())
                    .extracting(State::getName)
                    .isNotNull()
                    .isEqualTo(AWAITING_RESPONSES_FULL_DEFENCE_RECEIVED.fullName());
                assertThat(stateFlow.getStateHistory())
                    .hasSize(9)
                    .extracting(State::getName)
                    .containsExactly(
                        DRAFT.fullName(), CLAIM_SUBMITTED.fullName(), CLAIM_ISSUED_PAYMENT_SUCCESSFUL.fullName(),
                        PENDING_CLAIM_ISSUED.fullName(), CLAIM_ISSUED.fullName(), CLAIM_NOTIFIED.fullName(),
                        CLAIM_DETAILS_NOTIFIED.fullName(), NOTIFICATION_ACKNOWLEDGED.fullName(),
                        AWAITING_RESPONSES_FULL_DEFENCE_RECEIVED.fullName()
                    );

                assertThat(stateFlow.getFlags()).hasSize(5).contains(
                    entry("ONE_RESPONDENT_REPRESENTATIVE", false),
                    entry("TWO_RESPONDENT_REPRESENTATIVES", true),
                    entry(FlowFlag.NOTICE_OF_CHANGE.name(), false),
                    entry(FlowFlag.GENERAL_APPLICATION_ENABLED.name(), false),
                    entry(FlowFlag.CERTIFICATE_OF_SERVICE.name(), false)
                );
            }

            //1v2 Different solicitor scenario-first party acknowledges, not responds
            // second party submits response FullDefence
            @Test
            void shouldGenerateDQ_in1v2Scenario_whenSecondPartySubmitFullDefenceResponse() {
                // Given
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateRespondentFullDefenceRespondent2()
                    .multiPartyClaimTwoDefendantSolicitors()
                    .build();
                if (caseData.getRespondent2OrgRegistered() != null
                    && caseData.getRespondent2Represented() == null) {
                    caseData = caseData.toBuilder()
                        .respondent2Represented(YES)
                        .build();
                }

                // When
                StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

                // Then
                assertThat(stateFlow.getState())
                    .extracting(State::getName)
                    .isNotNull()
                    .isEqualTo(AWAITING_RESPONSES_FULL_DEFENCE_RECEIVED.fullName());
                assertThat(stateFlow.getStateHistory())
                    .hasSize(9)
                    .extracting(State::getName)
                    .containsExactly(
                        DRAFT.fullName(), CLAIM_SUBMITTED.fullName(), CLAIM_ISSUED_PAYMENT_SUCCESSFUL.fullName(),
                        PENDING_CLAIM_ISSUED.fullName(), CLAIM_ISSUED.fullName(), CLAIM_NOTIFIED.fullName(),
                        CLAIM_DETAILS_NOTIFIED.fullName(), NOTIFICATION_ACKNOWLEDGED.fullName(),
                        AWAITING_RESPONSES_FULL_DEFENCE_RECEIVED.fullName()
                    );

                assertThat(stateFlow.getFlags()).hasSize(5).contains(
                    entry("ONE_RESPONDENT_REPRESENTATIVE", false),
                    entry(FlowFlag.NOTICE_OF_CHANGE.name(), false),
                    entry(FlowFlag.GENERAL_APPLICATION_ENABLED.name(), false),
                    entry("TWO_RESPONDENT_REPRESENTATIVES", true),
                    entry(FlowFlag.CERTIFICATE_OF_SERVICE.name(), false)
                );
            }

            //Respondent 1 submits FULL DEFENCE, Respondent 2 submits FULL DEFENCE
            @Test
            void shouldReturnFullDefence_in1v2Scenario_whenBothPartiesSubmitFullDefenceResponses() {
                // Given
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateRespondentFullDefence_1v2_BothPartiesFullDefenceResponses()
                    .multiPartyClaimTwoDefendantSolicitors()
                    .build();
                if (caseData.getRespondent2OrgRegistered() != null
                    && caseData.getRespondent2Represented() == null) {
                    caseData = caseData.toBuilder()
                        .respondent2Represented(YES)
                        .build();
                }

                // When
                StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

                // Then
                assertThat(stateFlow.getState())
                    .extracting(State::getName)
                    .isNotNull()
                    .isEqualTo(FULL_DEFENCE.fullName());
                assertThat(stateFlow.getStateHistory())
                    .hasSize(10)
                    .extracting(State::getName)
                    .containsExactly(
                        DRAFT.fullName(), CLAIM_SUBMITTED.fullName(), CLAIM_ISSUED_PAYMENT_SUCCESSFUL.fullName(),
                        PENDING_CLAIM_ISSUED.fullName(), CLAIM_ISSUED.fullName(), CLAIM_NOTIFIED.fullName(),
                        CLAIM_DETAILS_NOTIFIED.fullName(), NOTIFICATION_ACKNOWLEDGED.fullName(),
                        ALL_RESPONSES_RECEIVED.fullName(), FULL_DEFENCE.fullName()
                    );

                assertThat(stateFlow.getFlags()).hasSize(5).contains(
                    entry("ONE_RESPONDENT_REPRESENTATIVE", false),
                    entry(FlowFlag.NOTICE_OF_CHANGE.name(), false),
                    entry(FlowFlag.GENERAL_APPLICATION_ENABLED.name(), false),
                    entry("TWO_RESPONDENT_REPRESENTATIVES", true),
                    entry(FlowFlag.CERTIFICATE_OF_SERVICE.name(), false)
                );
            }

            //Respondent 1 and 2 acknowledges claim, then submits  FULL DEFENCE
            @Test
            void shouldReturnFullDefence_in1v2Scenario_whenBothPartiesAcknowledgedAndSubmitFullDefenceResponses() {
                // Given
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateRespondentFullDefence_1v2_BothPartiesFullDefenceResponses()
                    .atStateNotificationAcknowledgedRespondent2()
                    .multiPartyClaimTwoDefendantSolicitors()
                    .build();
                if (caseData.getRespondent2OrgRegistered() != null
                    && caseData.getRespondent2Represented() == null) {
                    caseData = caseData.toBuilder()
                        .respondent2Represented(YES)
                        .build();
                }

                // When
                StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

                // Then
                assertThat(stateFlow.getState())
                    .extracting(State::getName)
                    .isNotNull()
                    .isEqualTo(FULL_DEFENCE.fullName());
                assertThat(stateFlow.getStateHistory())
                    .hasSize(10)
                    .extracting(State::getName)
                    .containsExactly(
                        DRAFT.fullName(), CLAIM_SUBMITTED.fullName(), CLAIM_ISSUED_PAYMENT_SUCCESSFUL.fullName(),
                        PENDING_CLAIM_ISSUED.fullName(), CLAIM_ISSUED.fullName(), CLAIM_NOTIFIED.fullName(),
                        CLAIM_DETAILS_NOTIFIED.fullName(), NOTIFICATION_ACKNOWLEDGED.fullName(),
                        ALL_RESPONSES_RECEIVED.fullName(), FULL_DEFENCE.fullName()
                    );

                assertThat(stateFlow.getFlags()).hasSize(5).contains(
                    entry("ONE_RESPONDENT_REPRESENTATIVE", false),
                    entry(FlowFlag.NOTICE_OF_CHANGE.name(), false),
                    entry(FlowFlag.GENERAL_APPLICATION_ENABLED.name(), false),
                    entry("TWO_RESPONDENT_REPRESENTATIVES", true),
                    entry(FlowFlag.CERTIFICATE_OF_SERVICE.name(), false)
                );
            }

            //Respondent 1 acknowledges claim, then Respondent 1 & 2 submits  FULL DEFENCE
            @Test
            void shouldReturnFullDefence_in1v2Scenario_whenRep1AcknowledgedAndBothSubmitFullDefenceResponses() {
                // Given
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateRespondentFullDefence_1v2_BothPartiesFullDefenceResponses()
                    .atStateNotificationAcknowledgedRespondent2()
                    .respondent2AcknowledgeNotificationDate(null)
                    .multiPartyClaimTwoDefendantSolicitors()
                    .build();
                if (caseData.getRespondent2OrgRegistered() != null
                    && caseData.getRespondent2Represented() == null) {
                    caseData = caseData.toBuilder()
                        .respondent2Represented(YES)
                        .build();
                }

                // When
                StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

                // Then
                assertThat(stateFlow.getState())
                    .extracting(State::getName)
                    .isNotNull()
                    .isEqualTo(FULL_DEFENCE.fullName());
                assertThat(stateFlow.getStateHistory())
                    .hasSize(10)
                    .extracting(State::getName)
                    .containsExactly(
                        DRAFT.fullName(), CLAIM_SUBMITTED.fullName(), CLAIM_ISSUED_PAYMENT_SUCCESSFUL.fullName(),
                        PENDING_CLAIM_ISSUED.fullName(), CLAIM_ISSUED.fullName(), CLAIM_NOTIFIED.fullName(),
                        CLAIM_DETAILS_NOTIFIED.fullName(), NOTIFICATION_ACKNOWLEDGED.fullName(),
                        ALL_RESPONSES_RECEIVED.fullName(), FULL_DEFENCE.fullName()
                    );

                assertThat(stateFlow.getFlags()).hasSize(5).contains(
                    entry("ONE_RESPONDENT_REPRESENTATIVE", false),
                    entry(FlowFlag.NOTICE_OF_CHANGE.name(), false),
                    entry(FlowFlag.GENERAL_APPLICATION_ENABLED.name(), false),
                    entry("TWO_RESPONDENT_REPRESENTATIVES", true),
                    entry(FlowFlag.CERTIFICATE_OF_SERVICE.name(), false)
                );
            }

            // Respondent 2 acknowledges claim, Respondent 1 & 2 submits  FULL DEFENCE
            @Test
            void shouldReturnFullDefence_in1v2Scenario_whenRep2AcknowledgedAndBothSubmitFullDefenceResponses() {
                // Given
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateRespondentFullDefence_1v2_BothPartiesFullDefenceResponses()
                    .atStateNotificationAcknowledgedRespondent2()
                    .respondent1AcknowledgeNotificationDate(null)
                    .multiPartyClaimTwoDefendantSolicitors()
                    .build();
                if (caseData.getRespondent2OrgRegistered() != null
                    && caseData.getRespondent2Represented() == null) {
                    caseData = caseData.toBuilder()
                        .respondent2Represented(YES)
                        .build();
                }

                // When
                StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

                // Then
                assertThat(stateFlow.getState())
                    .extracting(State::getName)
                    .isNotNull()
                    .isEqualTo(FULL_DEFENCE.fullName());
                assertThat(stateFlow.getStateHistory())
                    .hasSize(10)
                    .extracting(State::getName)
                    .containsExactly(
                        DRAFT.fullName(), CLAIM_SUBMITTED.fullName(), CLAIM_ISSUED_PAYMENT_SUCCESSFUL.fullName(),
                        PENDING_CLAIM_ISSUED.fullName(), CLAIM_ISSUED.fullName(), CLAIM_NOTIFIED.fullName(),
                        CLAIM_DETAILS_NOTIFIED.fullName(), NOTIFICATION_ACKNOWLEDGED.fullName(),
                        ALL_RESPONSES_RECEIVED.fullName(), FULL_DEFENCE.fullName()
                    );

                assertThat(stateFlow.getFlags()).hasSize(5).contains(
                    entry("ONE_RESPONDENT_REPRESENTATIVE", false),
                    entry(FlowFlag.NOTICE_OF_CHANGE.name(), false),
                    entry(FlowFlag.CERTIFICATE_OF_SERVICE.name(), false),
                    entry(FlowFlag.GENERAL_APPLICATION_ENABLED.name(), false),
                    entry("TWO_RESPONDENT_REPRESENTATIVES", true)
                );
            }

            //Respondent 1 submits FULL DEFENCE, Respondent 2 submits COUNTER CLAIM
            @Test
            void shouldReturnDivergentResponseAndGoOffline_1v2Scenario_whenFirstRespondentSubmitsFullDefenceResponse() {
                // Given
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateRespondentFullDefence_1v2_Resp1FullDefenceAndResp2CounterClaim()
                    .multiPartyClaimTwoDefendantSolicitors()
                    .build();
                if (caseData.getRespondent2OrgRegistered() != null
                    && caseData.getRespondent2Represented() == null) {
                    caseData = caseData.toBuilder()
                        .respondent2Represented(YES)
                        .build();
                }

                // When
                StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

                // Then
                assertThat(stateFlow.getState())
                    .extracting(State::getName)
                    .isNotNull()
                    .isEqualTo(DIVERGENT_RESPOND_GO_OFFLINE.fullName());
                assertThat(stateFlow.getStateHistory())
                    .hasSize(10)
                    .extracting(State::getName)
                    .containsExactly(
                        DRAFT.fullName(), CLAIM_SUBMITTED.fullName(), CLAIM_ISSUED_PAYMENT_SUCCESSFUL.fullName(),
                        PENDING_CLAIM_ISSUED.fullName(), CLAIM_ISSUED.fullName(), CLAIM_NOTIFIED.fullName(),
                        CLAIM_DETAILS_NOTIFIED.fullName(), NOTIFICATION_ACKNOWLEDGED.fullName(),
                        ALL_RESPONSES_RECEIVED.fullName(), DIVERGENT_RESPOND_GO_OFFLINE.fullName()
                    );

                assertThat(stateFlow.getFlags()).hasSize(5).contains(
                    entry("ONE_RESPONDENT_REPRESENTATIVE", false),
                    entry(FlowFlag.NOTICE_OF_CHANGE.name(), false),
                    entry(FlowFlag.CERTIFICATE_OF_SERVICE.name(), false),
                    entry(FlowFlag.GENERAL_APPLICATION_ENABLED.name(), false),
                    entry("TWO_RESPONDENT_REPRESENTATIVES", true)
                );
            }

            //Respondent 1 submits FULL DEFENCE, Respondent 2 submits COUNTER CLAIM
            @Test
            void shouldReturnDivergentResponse_in1v2SameSolicitorScenario_whenOneRespondentSubmitsFullDefence() {
                // Given
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateDivergentResponseWithFullDefence1v2SameSol_NotSingleDQ()
                    .atStateNotificationAcknowledged1v2SameSolicitor()
                    .multiPartyClaimOneDefendantSolicitor()
                    .build();
                if (caseData.getRespondent2OrgRegistered() != null
                    && caseData.getRespondent2Represented() == null) {
                    caseData = caseData.toBuilder()
                        .respondent2Represented(YES)
                        .build();
                }

                // When
                StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

                // Then
                assertThat(stateFlow.getState())
                    .extracting(State::getName)
                    .isNotNull()
                    .isEqualTo(DIVERGENT_RESPOND_GENERATE_DQ_GO_OFFLINE.fullName());
                assertThat(stateFlow.getStateHistory())
                    .hasSize(10)
                    .extracting(State::getName)
                    .containsExactly(
                        DRAFT.fullName(), CLAIM_SUBMITTED.fullName(), CLAIM_ISSUED_PAYMENT_SUCCESSFUL.fullName(),
                        PENDING_CLAIM_ISSUED.fullName(), CLAIM_ISSUED.fullName(), CLAIM_NOTIFIED.fullName(),
                        CLAIM_DETAILS_NOTIFIED.fullName(), NOTIFICATION_ACKNOWLEDGED.fullName(),
                        ALL_RESPONSES_RECEIVED.fullName(), DIVERGENT_RESPOND_GENERATE_DQ_GO_OFFLINE.fullName()
                    );

                assertThat(stateFlow.getFlags()).hasSize(4).contains(
                    entry("ONE_RESPONDENT_REPRESENTATIVE", true),
                    entry(FlowFlag.NOTICE_OF_CHANGE.name(), false),
                    entry(FlowFlag.CERTIFICATE_OF_SERVICE.name(), false),
                    entry(FlowFlag.GENERAL_APPLICATION_ENABLED.name(), false)
                );
            }

            //Respondent 1 submits ADMITS PART, Respondent 2 submits COUNTER CLAIM
            @Test
            void shouldReturnDivergentResponse_in1v2Scenario_whenNeitherRespondentSubmitsFullDefenceResponse() {
                // Given
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateDivergentResponse_1v2_Resp1FullAdmissionAndResp2CounterClaim()
                    .multiPartyClaimTwoDefendantSolicitors()
                    .build();
                if (caseData.getRespondent2OrgRegistered() != null
                    && caseData.getRespondent2Represented() == null) {
                    caseData = caseData.toBuilder()
                        .respondent2Represented(YES)
                        .build();
                }

                // When
                StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

                // Then
                assertThat(stateFlow.getState())
                    .extracting(State::getName)
                    .isNotNull()
                    .isEqualTo(DIVERGENT_RESPOND_GO_OFFLINE.fullName());
                assertThat(stateFlow.getStateHistory())
                    .hasSize(10)
                    .extracting(State::getName)
                    .containsExactly(
                        DRAFT.fullName(), CLAIM_SUBMITTED.fullName(), CLAIM_ISSUED_PAYMENT_SUCCESSFUL.fullName(),
                        PENDING_CLAIM_ISSUED.fullName(), CLAIM_ISSUED.fullName(), CLAIM_NOTIFIED.fullName(),
                        CLAIM_DETAILS_NOTIFIED.fullName(), NOTIFICATION_ACKNOWLEDGED.fullName(),
                        ALL_RESPONSES_RECEIVED.fullName(), DIVERGENT_RESPOND_GO_OFFLINE.fullName()
                    );

                assertThat(stateFlow.getFlags()).hasSize(5).contains(
                    entry("ONE_RESPONDENT_REPRESENTATIVE", false),
                    entry("TWO_RESPONDENT_REPRESENTATIVES", true),
                    entry(FlowFlag.NOTICE_OF_CHANGE.name(), false),
                    entry(FlowFlag.CERTIFICATE_OF_SERVICE.name(), false),
                    entry(FlowFlag.GENERAL_APPLICATION_ENABLED.name(), false)
                );
            }

            //Respondent 1 submits ADMITS PART, Respondent 2 submits ADMITS PART
            @Test
            void shouldReturnAdmitsPartResponse_in1v2Scenario_whenBothRespondentsSubmitAdmitPartResponses() {
                // Given
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateFullAdmission_1v2_BothRespondentSolicitorsSubmitFullAdmissionResponse()
                    .multiPartyClaimTwoDefendantSolicitors()
                    .build();
                if (caseData.getRespondent2OrgRegistered() != null
                    && caseData.getRespondent2Represented() == null) {
                    caseData = caseData.toBuilder()
                        .respondent2Represented(YES)
                        .build();
                }

                // When
                StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

                // Then
                assertThat(stateFlow.getState())
                    .extracting(State::getName)
                    .isNotNull()
                    .isEqualTo(FULL_ADMISSION.fullName());
                assertThat(stateFlow.getStateHistory())
                    .hasSize(10)
                    .extracting(State::getName)
                    .containsExactly(
                        DRAFT.fullName(), CLAIM_SUBMITTED.fullName(), CLAIM_ISSUED_PAYMENT_SUCCESSFUL.fullName(),
                        PENDING_CLAIM_ISSUED.fullName(), CLAIM_ISSUED.fullName(), CLAIM_NOTIFIED.fullName(),
                        CLAIM_DETAILS_NOTIFIED.fullName(), NOTIFICATION_ACKNOWLEDGED.fullName(),
                        ALL_RESPONSES_RECEIVED.fullName(), FULL_ADMISSION.fullName()
                    );

                assertThat(stateFlow.getFlags()).hasSize(5).contains(
                    entry("ONE_RESPONDENT_REPRESENTATIVE", false),
                    entry(FlowFlag.NOTICE_OF_CHANGE.name(), false),
                    entry(FlowFlag.CERTIFICATE_OF_SERVICE.name(), false),
                    entry(FlowFlag.GENERAL_APPLICATION_ENABLED.name(), false),
                    entry("TWO_RESPONDENT_REPRESENTATIVES", true)
                );
            }
        }

        @Test
        void shouldReturnClaimDismissed_whenCaseDataAtStateClaimDismissed() {
            // Given
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDismissed()
                .build();

            // When
            StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

            // Then
            assertThat(stateFlow.getState())
                .extracting(State::getName)
                .isNotNull()
                .isEqualTo(CLAIM_DISMISSED_PAST_CLAIM_DISMISSED_DEADLINE.fullName());
            assertThat(stateFlow.getStateHistory())
                .hasSize(9)
                .extracting(State::getName)
                .containsExactly(
                    DRAFT.fullName(), CLAIM_SUBMITTED.fullName(), CLAIM_ISSUED_PAYMENT_SUCCESSFUL.fullName(),
                    PENDING_CLAIM_ISSUED.fullName(), CLAIM_ISSUED.fullName(), CLAIM_NOTIFIED.fullName(),
                    CLAIM_DETAILS_NOTIFIED.fullName(), PAST_CLAIM_DISMISSED_DEADLINE_AWAITING_CAMUNDA.fullName(),
                    CLAIM_DISMISSED_PAST_CLAIM_DISMISSED_DEADLINE.fullName()
                );

            assertThat(stateFlow.getFlags()).hasSize(4).contains(
                entry("ONE_RESPONDENT_REPRESENTATIVE", true),
                entry(FlowFlag.NOTICE_OF_CHANGE.name(), false),
                entry(FlowFlag.CERTIFICATE_OF_SERVICE.name(), false),
                entry(FlowFlag.GENERAL_APPLICATION_ENABLED.name(), false)
            );
        }

        @ParameterizedTest
        @EnumSource(value = FlowState.Main.class,
            mode = EnumSource.Mode.INCLUDE,
            names = {"TAKEN_OFFLINE_AFTER_SDO", "FULL_DEFENCE_NOT_PROCEED"}
        )
        void shouldReturnFullDefenceProceed_whenCaseDataAtStateApplicantRespondToDefence(FlowState.Main flowState) {
            // Given
            CaseData caseData = CaseDataBuilder.builder().atState(flowState)
                .takenOfflineDate(LocalDateTime.now())
                .build();

            // When
            StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

            // Then
            assertThat(stateFlow.getState())
                .extracting(State::getName)
                .isNotNull()
                .isEqualTo(flowState.fullName());
            if (flowState.fullName().equals(TAKEN_OFFLINE_AFTER_SDO.fullName())) {
                assertThat(stateFlow.getStateHistory())
                    .hasSize(12)
                    .extracting(State::getName)
                    .containsExactly(
                        DRAFT.fullName(), CLAIM_SUBMITTED.fullName(), CLAIM_ISSUED_PAYMENT_SUCCESSFUL.fullName(),
                        PENDING_CLAIM_ISSUED.fullName(), CLAIM_ISSUED.fullName(), CLAIM_NOTIFIED.fullName(),
                        CLAIM_DETAILS_NOTIFIED.fullName(), NOTIFICATION_ACKNOWLEDGED.fullName(),
                        ALL_RESPONSES_RECEIVED.fullName(), FULL_DEFENCE.fullName(), FULL_DEFENCE_PROCEED.fullName(),
                        flowState.fullName()
                    );
            } else {
                assertThat(stateFlow.getStateHistory())
                    .hasSize(11)
                    .extracting(State::getName)
                    .containsExactly(
                        DRAFT.fullName(), CLAIM_SUBMITTED.fullName(), CLAIM_ISSUED_PAYMENT_SUCCESSFUL.fullName(),
                        PENDING_CLAIM_ISSUED.fullName(), CLAIM_ISSUED.fullName(), CLAIM_NOTIFIED.fullName(),
                        CLAIM_DETAILS_NOTIFIED.fullName(), NOTIFICATION_ACKNOWLEDGED.fullName(),
                        ALL_RESPONSES_RECEIVED.fullName(), FULL_DEFENCE.fullName(), flowState.fullName()
                    );
            }

            assertThat(stateFlow.getFlags()).hasSize(4).contains(
                entry(FlowFlag.NOTICE_OF_CHANGE.name(), false),
                entry(FlowFlag.CERTIFICATE_OF_SERVICE.name(), false),
                entry(FlowFlag.GENERAL_APPLICATION_ENABLED.name(), false),
                entry("ONE_RESPONDENT_REPRESENTATIVE", true)
            );
        }

        //1v2 Different solicitor scenario-first response FullDefence received and with time extension
        @Test
        void shouldAwaitResponse_1v2DiffSol_whenFirstResponseIsFullDefenceAndTimeExtension() {
            // Given
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDetailsNotifiedTimeExtension_Defendent2()
                .atStateRespondentFullDefence()
                .multiPartyClaimTwoDefendantSolicitors()
                .build();
            if (caseData.getRespondent2OrgRegistered() != null
                && caseData.getRespondent2Represented() == null) {
                caseData = caseData.toBuilder()
                    .respondent2Represented(YES)
                    .build();
            }

            // When
            StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

            // Then
            assertThat(stateFlow.getState())
                .extracting(State::getName)
                .isNotNull()
                .isEqualTo(AWAITING_RESPONSES_FULL_DEFENCE_RECEIVED.fullName());
            assertThat(stateFlow.getStateHistory())
                .hasSize(10)
                .extracting(State::getName)
                .containsExactly(
                    DRAFT.fullName(),
                    CLAIM_SUBMITTED.fullName(),
                    CLAIM_ISSUED_PAYMENT_SUCCESSFUL.fullName(),
                    PENDING_CLAIM_ISSUED.fullName(),
                    CLAIM_ISSUED.fullName(),
                    CLAIM_NOTIFIED.fullName(),
                    CLAIM_DETAILS_NOTIFIED.fullName(),
                    NOTIFICATION_ACKNOWLEDGED.fullName(),
                    NOTIFICATION_ACKNOWLEDGED_TIME_EXTENSION.fullName(),
                    AWAITING_RESPONSES_FULL_DEFENCE_RECEIVED.fullName()
                );

            assertThat(stateFlow.getFlags()).hasSize(5).contains(
                entry("ONE_RESPONDENT_REPRESENTATIVE", false),
                entry("TWO_RESPONDENT_REPRESENTATIVES", true),
                entry(FlowFlag.NOTICE_OF_CHANGE.name(), false),
                entry(FlowFlag.CERTIFICATE_OF_SERVICE.name(), false),
                entry(FlowFlag.GENERAL_APPLICATION_ENABLED.name(), false)
            );
        }

        @Test
        //1v2 Different solicitor scenario-both responses FullDefence received and with time extension
        void shouldAwaitResponse_1v2DiffSol_whenBothRespondFullDefenceAndTimeExtension() {
            // Given
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDetailsNotifiedTimeExtension_Defendent2()
                .atStateRespondentFullDefence()
                .respondent2Responds(RespondentResponseType.FULL_DEFENCE)
                .multiPartyClaimTwoDefendantSolicitors()
                .build();
            if (caseData.getRespondent2OrgRegistered() != null
                && caseData.getRespondent2Represented() == null) {
                caseData = caseData.toBuilder()
                    .respondent2Represented(YES)
                    .build();
            }

            // When
            StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

            // Then
            assertThat(stateFlow.getState())
                .extracting(State::getName)
                .isNotNull()
                .isEqualTo(FULL_DEFENCE.fullName());
            assertThat(stateFlow.getStateHistory())
                .hasSize(11)
                .extracting(State::getName)
                .containsExactly(
                    DRAFT.fullName(),
                    CLAIM_SUBMITTED.fullName(),
                    CLAIM_ISSUED_PAYMENT_SUCCESSFUL.fullName(),
                    PENDING_CLAIM_ISSUED.fullName(),
                    CLAIM_ISSUED.fullName(),
                    CLAIM_NOTIFIED.fullName(),
                    CLAIM_DETAILS_NOTIFIED.fullName(),
                    NOTIFICATION_ACKNOWLEDGED.fullName(),
                    NOTIFICATION_ACKNOWLEDGED_TIME_EXTENSION.fullName(),
                    ALL_RESPONSES_RECEIVED.fullName(),
                    FULL_DEFENCE.fullName()
                );

            assertThat(stateFlow.getFlags()).hasSize(5).contains(
                entry("ONE_RESPONDENT_REPRESENTATIVE", false),
                entry(FlowFlag.GENERAL_APPLICATION_ENABLED.name(), false),
                entry("TWO_RESPONDENT_REPRESENTATIVES", true),
                entry(FlowFlag.CERTIFICATE_OF_SERVICE.name(), false)
            );
        }

        //1v2 Different solicitor scenario-first response FullDefence received and with time extension
        @Test
        void shouldAwaitResponse_1v2DiffSol_whenFirstResponseIsFullDefenceAfterAcknowledgeClaimAndTimeExtension() {
            // Given
            CaseData caseData = CaseDataBuilder.builder()
                .atStateNotificationAcknowledgedRespondent2()
                .atStateClaimDetailsNotifiedTimeExtension_Defendent2()
                .atStateRespondentFullDefence()
                .multiPartyClaimTwoDefendantSolicitors()
                .build();
            if (caseData.getRespondent2OrgRegistered() != null
                && caseData.getRespondent2Represented() == null) {
                caseData = caseData.toBuilder()
                    .respondent2Represented(YES)
                    .build();
            }

            // When
            StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

            // Then
            assertThat(stateFlow.getState())
                .extracting(State::getName)
                .isNotNull()
                .isEqualTo(AWAITING_RESPONSES_FULL_DEFENCE_RECEIVED.fullName());
            assertThat(stateFlow.getStateHistory())
                .hasSize(10)
                .extracting(State::getName)
                .containsExactly(
                    DRAFT.fullName(), CLAIM_SUBMITTED.fullName(), CLAIM_ISSUED_PAYMENT_SUCCESSFUL.fullName(),
                    PENDING_CLAIM_ISSUED.fullName(), CLAIM_ISSUED.fullName(), CLAIM_NOTIFIED.fullName(),
                    CLAIM_DETAILS_NOTIFIED.fullName(), NOTIFICATION_ACKNOWLEDGED.fullName(),
                    NOTIFICATION_ACKNOWLEDGED_TIME_EXTENSION.fullName(),
                    AWAITING_RESPONSES_FULL_DEFENCE_RECEIVED.fullName()
                );

            assertThat(stateFlow.getFlags()).hasSize(5).contains(
                entry("ONE_RESPONDENT_REPRESENTATIVE", false),
                entry("TWO_RESPONDENT_REPRESENTATIVES", true),
                entry(FlowFlag.NOTICE_OF_CHANGE.name(), false),
                entry(FlowFlag.CERTIFICATE_OF_SERVICE.name(), false),
                entry(FlowFlag.GENERAL_APPLICATION_ENABLED.name(), false)
            );
        }

        @Test
        void shouldReturnProceedsWithOfflineJourney_whenCaseDataIsCaseProceedsInCaseman() {
            // Given
            CaseData caseData = CaseDataBuilder.builder().atStateTakenOfflineByStaff().build();

            // When
            StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

            // Then
            assertThat(stateFlow.getState())
                .extracting(State::getName)
                .isNotNull()
                .isEqualTo(TAKEN_OFFLINE_BY_STAFF.fullName());
            assertThat(stateFlow.getStateHistory())
                .hasSize(6)
                .extracting(State::getName)
                .containsExactly(
                    DRAFT.fullName(), CLAIM_SUBMITTED.fullName(), CLAIM_ISSUED_PAYMENT_SUCCESSFUL.fullName(),
                    PENDING_CLAIM_ISSUED.fullName(), CLAIM_ISSUED.fullName(), TAKEN_OFFLINE_BY_STAFF.fullName()
                );

            assertThat(stateFlow.getFlags()).hasSize(4).contains(
                entry(FlowFlag.NOTICE_OF_CHANGE.name(), false),
                entry(FlowFlag.CERTIFICATE_OF_SERVICE.name(), false),
                entry(FlowFlag.GENERAL_APPLICATION_ENABLED.name(), false),
                entry("ONE_RESPONDENT_REPRESENTATIVE", true)
            );
        }

        @Test
        void shouldReturnAwaitingCamundaState_whenDefendantHasRespondedAndApplicantIsOutOfTime() {
            // Given
            CaseData caseData = CaseDataBuilder.builder().atStatePastApplicantResponseDeadline().build();

            // When
            StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

            // Then
            assertThat(stateFlow.getState())
                .extracting(State::getName)
                .isNotNull()
                .isEqualTo(PAST_APPLICANT_RESPONSE_DEADLINE_AWAITING_CAMUNDA.fullName());
            assertThat(stateFlow.getStateHistory())
                .hasSize(11)
                .extracting(State::getName)
                .containsExactly(
                    DRAFT.fullName(), CLAIM_SUBMITTED.fullName(), CLAIM_ISSUED_PAYMENT_SUCCESSFUL.fullName(),
                    PENDING_CLAIM_ISSUED.fullName(), CLAIM_ISSUED.fullName(), CLAIM_NOTIFIED.fullName(),
                    CLAIM_DETAILS_NOTIFIED.fullName(), NOTIFICATION_ACKNOWLEDGED.fullName(),
                    ALL_RESPONSES_RECEIVED.fullName(), FULL_DEFENCE.fullName(),
                    PAST_APPLICANT_RESPONSE_DEADLINE_AWAITING_CAMUNDA.fullName()
                );
        }

        @Test
        void shouldReturnTakenOffline_whenApplicantIsOutOfTimeAndCamundaHasProcessedCase() {
            // Given
            CaseData caseData = CaseDataBuilder.builder().atStateTakenOfflinePastApplicantResponseDeadline().build();

            // When
            StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

            // Then
            assertThat(stateFlow.getState())
                .extracting(State::getName)
                .isNotNull()
                .isEqualTo(TAKEN_OFFLINE_PAST_APPLICANT_RESPONSE_DEADLINE.fullName());
            assertThat(stateFlow.getStateHistory())
                .hasSize(12)
                .extracting(State::getName)
                .containsExactly(
                    DRAFT.fullName(), CLAIM_SUBMITTED.fullName(), CLAIM_ISSUED_PAYMENT_SUCCESSFUL.fullName(),
                    PENDING_CLAIM_ISSUED.fullName(), CLAIM_ISSUED.fullName(), CLAIM_NOTIFIED.fullName(),
                    CLAIM_DETAILS_NOTIFIED.fullName(), NOTIFICATION_ACKNOWLEDGED.fullName(),
                    ALL_RESPONSES_RECEIVED.fullName(), FULL_DEFENCE.fullName(),
                    PAST_APPLICANT_RESPONSE_DEADLINE_AWAITING_CAMUNDA.fullName(),
                    TAKEN_OFFLINE_PAST_APPLICANT_RESPONSE_DEADLINE.fullName()
                );

            assertThat(stateFlow.getFlags()).hasSize(4).contains(
                entry(FlowFlag.NOTICE_OF_CHANGE.name(), false),
                entry(FlowFlag.CERTIFICATE_OF_SERVICE.name(), false),
                entry(FlowFlag.GENERAL_APPLICATION_ENABLED.name(), false),
                entry("ONE_RESPONDENT_REPRESENTATIVE", true)
            );
        }

        @Test
        void shouldReturnAwaitingCamundaState_whenPastClaimNotificationDeadline() {
            // Given
            CaseData caseData = CaseDataBuilder.builder().atStateClaimPastClaimNotificationDeadline().build();
            if (caseData.getRespondent2OrgRegistered() != null
                && caseData.getRespondent2Represented() == null) {
                caseData = caseData.toBuilder()
                    .respondent2Represented(YES)
                    .build();
            }

            // When
            StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

            // Then
            assertThat(stateFlow.getState())
                .extracting(State::getName)
                .isNotNull()
                .isEqualTo(PAST_CLAIM_NOTIFICATION_DEADLINE_AWAITING_CAMUNDA.fullName());
            assertThat(stateFlow.getStateHistory())
                .hasSize(6)
                .extracting(State::getName)
                .containsExactly(
                    DRAFT.fullName(), CLAIM_SUBMITTED.fullName(), CLAIM_ISSUED_PAYMENT_SUCCESSFUL.fullName(),
                    PENDING_CLAIM_ISSUED.fullName(), CLAIM_ISSUED.fullName(),
                    PAST_CLAIM_NOTIFICATION_DEADLINE_AWAITING_CAMUNDA.fullName()
                );
        }

        @Test
        void shouldReturnClaimDismissedState_whenPastClaimNotificationDeadlineAndProcessedByCamunda() {
            // Given
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDismissedPastHearingFeeDueDeadline().build();

            // When
            StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

            // Then
            assertThat(stateFlow.getState())
                .extracting(State::getName)
                .isNotNull()
                .isEqualTo(CLAIM_DISMISSED_HEARING_FEE_DUE_DEADLINE.fullName());
            assertThat(stateFlow.getStateHistory())
                .hasSize(12)
                .extracting(State::getName)
                .containsExactly(
                    DRAFT.fullName(), CLAIM_SUBMITTED.fullName(), CLAIM_ISSUED_PAYMENT_SUCCESSFUL.fullName(),
                    PENDING_CLAIM_ISSUED.fullName(), CLAIM_ISSUED.fullName(),
                    CLAIM_NOTIFIED.fullName(), CLAIM_DETAILS_NOTIFIED.fullName(),
                    NOTIFICATION_ACKNOWLEDGED.fullName(), ALL_RESPONSES_RECEIVED.fullName(),
                    FULL_DEFENCE.fullName(), FULL_DEFENCE_PROCEED.fullName(),
                    CLAIM_DISMISSED_HEARING_FEE_DUE_DEADLINE.fullName()
                );

            assertThat(stateFlow.getFlags()).hasSize(4).contains(
                entry(FlowFlag.NOTICE_OF_CHANGE.name(), false),
                entry(FlowFlag.CERTIFICATE_OF_SERVICE.name(), false),
                entry(FlowFlag.GENERAL_APPLICATION_ENABLED.name(), false),
                entry("ONE_RESPONDENT_REPRESENTATIVE", true)
            );
        }

        @Test
        void shouldReturnAwaitingCamundaState_whenCaseDataIsPastClaimDetailsNotification() {
            // Given
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimPastClaimDetailsNotificationDeadline()
                .build();
            if (caseData.getRespondent2OrgRegistered() != null
                && caseData.getRespondent2Represented() == null) {
                caseData = caseData.toBuilder()
                    .respondent2Represented(YES)
                    .build();
            }

            // When
            StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

            // Then
            assertThat(stateFlow.getState())
                .extracting(State::getName)
                .isNotNull()
                .isEqualTo(PAST_CLAIM_DETAILS_NOTIFICATION_DEADLINE_AWAITING_CAMUNDA.fullName());
            assertThat(stateFlow.getStateHistory())
                .hasSize(7)
                .extracting(State::getName)
                .containsExactly(
                    DRAFT.fullName(), CLAIM_SUBMITTED.fullName(), CLAIM_ISSUED_PAYMENT_SUCCESSFUL.fullName(),
                    PENDING_CLAIM_ISSUED.fullName(), CLAIM_ISSUED.fullName(), CLAIM_NOTIFIED.fullName(),
                    PAST_CLAIM_DETAILS_NOTIFICATION_DEADLINE_AWAITING_CAMUNDA.fullName()
                );
        }

        @Test
        void shouldReturnCaseDismissedState_whenCaseDataIsPastClaimDetailsNotificationAndProcessedByCamunda() {
            // Given
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDismissedPastClaimDetailsNotificationDeadline()
                .build();

            // When
            StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

            // Then
            assertThat(stateFlow.getState())
                .extracting(State::getName)
                .isNotNull()
                .isEqualTo(CLAIM_DISMISSED_PAST_CLAIM_DETAILS_NOTIFICATION_DEADLINE.fullName());
            assertThat(stateFlow.getStateHistory())
                .hasSize(8)
                .extracting(State::getName)
                .containsExactly(
                    DRAFT.fullName(), CLAIM_SUBMITTED.fullName(), CLAIM_ISSUED_PAYMENT_SUCCESSFUL.fullName(),
                    PENDING_CLAIM_ISSUED.fullName(), CLAIM_ISSUED.fullName(), CLAIM_NOTIFIED.fullName(),
                    PAST_CLAIM_DETAILS_NOTIFICATION_DEADLINE_AWAITING_CAMUNDA.fullName(),
                    CLAIM_DISMISSED_PAST_CLAIM_DETAILS_NOTIFICATION_DEADLINE.fullName()
                );

            assertThat(stateFlow.getFlags()).hasSize(4).contains(
                entry(FlowFlag.NOTICE_OF_CHANGE.name(), false),
                entry(FlowFlag.CERTIFICATE_OF_SERVICE.name(), false),
                entry(FlowFlag.GENERAL_APPLICATION_ENABLED.name(), false),
                entry("ONE_RESPONDENT_REPRESENTATIVE", true)
            );
        }

        @Test
        void shouldReturnClaimDismissedState_whenPastHearingFeeDueDeadlineAndProcessedByCamunda() {
            // Given
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDismissedPastHearingFeeDueDeadline().build();

            // When
            StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

            // Then
            assertThat(stateFlow.getState())
                .extracting(State::getName)
                .isNotNull()
                .isEqualTo(CLAIM_DISMISSED_HEARING_FEE_DUE_DEADLINE.fullName());
            assertThat(stateFlow.getStateHistory())
                .hasSize(12)
                .extracting(State::getName)
                .containsExactly(
                    DRAFT.fullName(), CLAIM_SUBMITTED.fullName(), CLAIM_ISSUED_PAYMENT_SUCCESSFUL.fullName(),
                    PENDING_CLAIM_ISSUED.fullName(), CLAIM_ISSUED.fullName(), CLAIM_NOTIFIED.fullName(),
                    CLAIM_DETAILS_NOTIFIED.fullName(), NOTIFICATION_ACKNOWLEDGED.fullName(),
                    ALL_RESPONSES_RECEIVED.fullName(), FULL_DEFENCE.fullName(), FULL_DEFENCE_PROCEED.fullName(),
                    CLAIM_DISMISSED_HEARING_FEE_DUE_DEADLINE.fullName()
                );

            assertThat(stateFlow.getFlags()).hasSize(4).contains(
                entry(FlowFlag.NOTICE_OF_CHANGE.name(), false),
                entry(FlowFlag.CERTIFICATE_OF_SERVICE.name(), false),
                entry("ONE_RESPONDENT_REPRESENTATIVE", true)
            );
        }

    }

    @Nested
    class TakenOfflineByStaff {

        @Test
        void shouldReturnProceedsWithOfflineJourney_whenCaseTakenOfflineAfterClaimIssue() {
            // Given
            CaseData caseData = CaseDataBuilder.builder().atStateTakenOfflineByStaff()
                .build();

            // When
            StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

            // Then
            assertThat(stateFlow.getState())
                .extracting(State::getName)
                .isNotNull()
                .isEqualTo(TAKEN_OFFLINE_BY_STAFF.fullName());
            assertThat(stateFlow.getStateHistory())
                .hasSize(6)
                .extracting(State::getName)
                .containsExactly(
                    DRAFT.fullName(), CLAIM_SUBMITTED.fullName(), CLAIM_ISSUED_PAYMENT_SUCCESSFUL.fullName(),
                    PENDING_CLAIM_ISSUED.fullName(), CLAIM_ISSUED.fullName(), TAKEN_OFFLINE_BY_STAFF.fullName()
                );

            assertThat(stateFlow.getFlags()).hasSize(4).contains(
                entry("ONE_RESPONDENT_REPRESENTATIVE", true),
                entry(FlowFlag.NOTICE_OF_CHANGE.name(), false),
                entry(FlowFlag.CERTIFICATE_OF_SERVICE.name(), false),
                entry(FlowFlag.GENERAL_APPLICATION_ENABLED.name(), false)
            );
        }

        @Test
        void shouldReturnProceedsWithOfflineJourney_whenCaseTakenOfflineAfterClaimIssueSpec_1v2SS() {
            // Given
            CaseData caseData = CaseDataBuilder.builder().atStateTakenOfflineByStaffSpec1v2SS()
                .build();

            // When
            StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

            // Then
            assertThat(stateFlow.getState())
                .extracting(State::getName)
                .isNotNull()
                .isEqualTo(TAKEN_OFFLINE_BY_STAFF.fullName());
            assertThat(stateFlow.getStateHistory())
                .hasSize(6)
                .extracting(State::getName)
                .containsExactly(
                    DRAFT.fullName(), CLAIM_SUBMITTED.fullName(), CLAIM_ISSUED_PAYMENT_SUCCESSFUL.fullName(),
                    PENDING_CLAIM_ISSUED.fullName(), CLAIM_ISSUED.fullName(), TAKEN_OFFLINE_BY_STAFF.fullName()
                );

            assertThat(stateFlow.getFlags()).hasSize(4).contains(
                entry("ONE_RESPONDENT_REPRESENTATIVE", true),
                entry(FlowFlag.NOTICE_OF_CHANGE.name(), false),
                entry(FlowFlag.CERTIFICATE_OF_SERVICE.name(), false),
                entry(FlowFlag.GENERAL_APPLICATION_ENABLED.name(), false)
            );
        }

        @Test
        void shouldReturnProceedsWithOfflineJourney_whenCaseTakenOfflineAfterClaimNotified() {
            // Given
            CaseData caseData = CaseDataBuilder.builder().atStateTakenOfflineByStaffAfterClaimNotified().build();

            // When
            StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

            // Then
            assertThat(stateFlow.getState())
                .extracting(State::getName)
                .isNotNull()
                .isEqualTo(TAKEN_OFFLINE_BY_STAFF.fullName());
            assertThat(stateFlow.getStateHistory())
                .hasSize(7)
                .extracting(State::getName)
                .containsExactly(
                    DRAFT.fullName(), CLAIM_SUBMITTED.fullName(), CLAIM_ISSUED_PAYMENT_SUCCESSFUL.fullName(),
                    PENDING_CLAIM_ISSUED.fullName(), CLAIM_ISSUED.fullName(), CLAIM_NOTIFIED.fullName(),
                    TAKEN_OFFLINE_BY_STAFF.fullName()
                );

            assertThat(stateFlow.getFlags()).hasSize(4).contains(
                entry("ONE_RESPONDENT_REPRESENTATIVE", true),
                entry(FlowFlag.NOTICE_OF_CHANGE.name(), false),
                entry(FlowFlag.CERTIFICATE_OF_SERVICE.name(), false),
                entry(FlowFlag.GENERAL_APPLICATION_ENABLED.name(), false)
            );
        }

        @Test
        void shouldReturnProceedsWithOfflineJourney_whenCaseTakenOfflineAfterClaimDetailsNotified() {
            // Given
            CaseData caseData = CaseDataBuilder.builder().atStateTakenOfflineByStaffAfterClaimDetailsNotified().build();

            // When
            StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

            // Then
            assertThat(stateFlow.getState())
                .extracting(State::getName)
                .isNotNull()
                .isEqualTo(TAKEN_OFFLINE_BY_STAFF.fullName());
            assertThat(stateFlow.getStateHistory())
                .hasSize(8)
                .extracting(State::getName)
                .containsExactly(
                    DRAFT.fullName(), CLAIM_SUBMITTED.fullName(), CLAIM_ISSUED_PAYMENT_SUCCESSFUL.fullName(),
                    PENDING_CLAIM_ISSUED.fullName(), CLAIM_ISSUED.fullName(), CLAIM_NOTIFIED.fullName(),
                    CLAIM_DETAILS_NOTIFIED.fullName(), TAKEN_OFFLINE_BY_STAFF.fullName()
                );

            assertThat(stateFlow.getFlags()).hasSize(4).contains(
                entry("ONE_RESPONDENT_REPRESENTATIVE", true),
                entry(FlowFlag.NOTICE_OF_CHANGE.name(), false),
                entry(FlowFlag.CERTIFICATE_OF_SERVICE.name(), false),
                entry(FlowFlag.GENERAL_APPLICATION_ENABLED.name(), false)
            );
        }

        @Test
        void shouldReturnProceedsWithOfflineJourney_whenCaseTakenOfflineAfterClaimDetailsNotifiedExtension() {
            // Given
            CaseData caseData = CaseDataBuilder.builder().atStateTakenOfflineByStaffAfterClaimDetailsNotifiedExtension()
                .build();

            // When
            StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

            // Then
            assertThat(stateFlow.getState())
                .extracting(State::getName)
                .isNotNull()
                .isEqualTo(TAKEN_OFFLINE_BY_STAFF.fullName());
            assertThat(stateFlow.getStateHistory())
                .hasSize(9)
                .extracting(State::getName)
                .containsExactly(
                    DRAFT.fullName(), CLAIM_SUBMITTED.fullName(), CLAIM_ISSUED_PAYMENT_SUCCESSFUL.fullName(),
                    PENDING_CLAIM_ISSUED.fullName(), CLAIM_ISSUED.fullName(), CLAIM_NOTIFIED.fullName(),
                    CLAIM_DETAILS_NOTIFIED.fullName(), CLAIM_DETAILS_NOTIFIED_TIME_EXTENSION.fullName(),
                    TAKEN_OFFLINE_BY_STAFF.fullName()
                );

            assertThat(stateFlow.getFlags()).hasSize(4).contains(
                entry("ONE_RESPONDENT_REPRESENTATIVE", true),
                entry(FlowFlag.NOTICE_OF_CHANGE.name(), false),
                entry(FlowFlag.CERTIFICATE_OF_SERVICE.name(), false),
                entry(FlowFlag.GENERAL_APPLICATION_ENABLED.name(), false)
            );
        }

        @Test
        void shouldReturnProceedsWithOfflineJourney_whenCaseTakenOfflineAfterNotificationAcknowledged() {
            // Given
            CaseData caseData = CaseDataBuilder.builder().atStateTakenOfflineByStaffAfterNotificationAcknowledged()
                .build();

            // When
            StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

            // Then
            assertThat(stateFlow.getState())
                .extracting(State::getName)
                .isNotNull()
                .isEqualTo(TAKEN_OFFLINE_BY_STAFF.fullName());
            assertThat(stateFlow.getStateHistory())
                .hasSize(9)
                .extracting(State::getName)
                .containsExactly(
                    DRAFT.fullName(), CLAIM_SUBMITTED.fullName(), CLAIM_ISSUED_PAYMENT_SUCCESSFUL.fullName(),
                    PENDING_CLAIM_ISSUED.fullName(), CLAIM_ISSUED.fullName(), CLAIM_NOTIFIED.fullName(),
                    CLAIM_DETAILS_NOTIFIED.fullName(), NOTIFICATION_ACKNOWLEDGED.fullName(),
                    TAKEN_OFFLINE_BY_STAFF.fullName()
                );

            assertThat(stateFlow.getFlags()).hasSize(4).contains(
                entry("ONE_RESPONDENT_REPRESENTATIVE", true),
                entry(FlowFlag.NOTICE_OF_CHANGE.name(), false),
                entry(FlowFlag.CERTIFICATE_OF_SERVICE.name(), false),
                entry(FlowFlag.GENERAL_APPLICATION_ENABLED.name(), false)
            );
        }

        @Test
        void shouldReturnProceedsWithOfflineJourney_whenCaseTakenOfflineAfterNotificationAcknowledgeExtension() {
            // Given
            CaseData caseData = CaseDataBuilder.builder()
                .atStateTakenOfflineByStaffAfterNotificationAcknowledgeExtension()
                .build();

            // When
            StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

            // Then
            assertThat(stateFlow.getState())
                .extracting(State::getName)
                .isNotNull()
                .isEqualTo(TAKEN_OFFLINE_BY_STAFF.fullName());
            assertThat(stateFlow.getStateHistory())
                .hasSize(10)
                .extracting(State::getName)
                .containsExactly(
                    DRAFT.fullName(), CLAIM_SUBMITTED.fullName(), CLAIM_ISSUED_PAYMENT_SUCCESSFUL.fullName(),
                    PENDING_CLAIM_ISSUED.fullName(), CLAIM_ISSUED.fullName(), CLAIM_NOTIFIED.fullName(),
                    CLAIM_DETAILS_NOTIFIED.fullName(), NOTIFICATION_ACKNOWLEDGED.fullName(),
                    NOTIFICATION_ACKNOWLEDGED_TIME_EXTENSION.fullName(), TAKEN_OFFLINE_BY_STAFF.fullName()
                );

            assertThat(stateFlow.getFlags()).hasSize(4).contains(
                entry("ONE_RESPONDENT_REPRESENTATIVE", true),
                entry(FlowFlag.NOTICE_OF_CHANGE.name(), false),
                entry(FlowFlag.CERTIFICATE_OF_SERVICE.name(), false),
                entry(FlowFlag.GENERAL_APPLICATION_ENABLED.name(), false)
            );
        }

        @Test
        void shouldAwaitResponse_1v2DiffSol_whenFirstResponseIsFullDefenceAfterAcknowledgeClaimAndTimeExtension1v2() {
            // Given
            CaseData caseData = CaseDataBuilder.builder()
                .atStateTakenOfflineByStaffAfterNotificationAcknowledgeExtension1v2()
                .build();

            if (caseData.getRespondent2OrgRegistered() != null
                && caseData.getRespondent2Represented() == null) {
                caseData = caseData.toBuilder()
                    .respondent2Represented(YES)
                    .build();
            }

            // When
            StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

            // Then
            assertThat(stateFlow.getState())
                .extracting(State::getName)
                .isNotNull()
                .isEqualTo(TAKEN_OFFLINE_BY_STAFF.fullName());
            assertThat(stateFlow.getStateHistory())
                .hasSize(10)
                .extracting(State::getName)
                .containsExactly(
                    DRAFT.fullName(), CLAIM_SUBMITTED.fullName(), CLAIM_ISSUED_PAYMENT_SUCCESSFUL.fullName(),
                    PENDING_CLAIM_ISSUED.fullName(), CLAIM_ISSUED.fullName(), CLAIM_NOTIFIED.fullName(),
                    CLAIM_DETAILS_NOTIFIED.fullName(), NOTIFICATION_ACKNOWLEDGED.fullName(),
                    NOTIFICATION_ACKNOWLEDGED_TIME_EXTENSION.fullName(),
                    TAKEN_OFFLINE_BY_STAFF.fullName()
                );

            assertThat(stateFlow.getFlags()).hasSize(5).contains(
                entry("ONE_RESPONDENT_REPRESENTATIVE", false),
                entry("TWO_RESPONDENT_REPRESENTATIVES", true),
                entry(FlowFlag.NOTICE_OF_CHANGE.name(), false),
                entry(FlowFlag.CERTIFICATE_OF_SERVICE.name(), false),
                entry(FlowFlag.GENERAL_APPLICATION_ENABLED.name(), false)
            );
        }

        @Test
        void shouldReturnProceedsWithOfflineJourney_whenCaseTakenOfflineAfterDefendantResponse() {
            // Given
            CaseData caseData = CaseDataBuilder.builder().atStateTakenOfflineByStaffAfterDefendantResponse()
                .build();

            // When
            StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

            // Then
            assertThat(stateFlow.getState())
                .extracting(State::getName)
                .isNotNull()
                .isEqualTo(TAKEN_OFFLINE_BY_STAFF.fullName());
            assertThat(stateFlow.getStateHistory())
                .hasSize(11)
                .extracting(State::getName)
                .containsExactly(
                    DRAFT.fullName(), CLAIM_SUBMITTED.fullName(), CLAIM_ISSUED_PAYMENT_SUCCESSFUL.fullName(),
                    PENDING_CLAIM_ISSUED.fullName(), CLAIM_ISSUED.fullName(), CLAIM_NOTIFIED.fullName(),
                    CLAIM_DETAILS_NOTIFIED.fullName(), NOTIFICATION_ACKNOWLEDGED.fullName(),
                    ALL_RESPONSES_RECEIVED.fullName(), FULL_DEFENCE.fullName(), TAKEN_OFFLINE_BY_STAFF.fullName()
                );

            assertThat(stateFlow.getFlags()).hasSize(4).contains(
                entry("ONE_RESPONDENT_REPRESENTATIVE", true),
                entry(FlowFlag.NOTICE_OF_CHANGE.name(), false),
                entry(FlowFlag.CERTIFICATE_OF_SERVICE.name(), false),
                entry(FlowFlag.GENERAL_APPLICATION_ENABLED.name(), false)
            );
        }

        @Test
        void shouldReturnProceedsWithOfflineJourney_whenCaseTakenOfflinePastClaimNotificationDeadline() {
            // Given
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDismissedPastClaimNotificationDeadline()
                .takenOfflineByStaffDate(LocalDateTime.now())
                .build();

            // When
            StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

            // Then
            assertThat(stateFlow.getState())
                .extracting(State::getName)
                .isNotNull()
                .isEqualTo(TAKEN_OFFLINE_BY_STAFF.fullName());
            assertThat(stateFlow.getStateHistory())
                .hasSize(8)
                .extracting(State::getName)
                .containsExactly(
                    DRAFT.fullName(), CLAIM_SUBMITTED.fullName(), CLAIM_ISSUED_PAYMENT_SUCCESSFUL.fullName(),
                    PENDING_CLAIM_ISSUED.fullName(), CLAIM_ISSUED.fullName(),
                    PAST_CLAIM_NOTIFICATION_DEADLINE_AWAITING_CAMUNDA.fullName(),
                    CLAIM_DISMISSED_PAST_CLAIM_NOTIFICATION_DEADLINE.fullName(),
                    TAKEN_OFFLINE_BY_STAFF.fullName()
                );

            assertThat(stateFlow.getFlags()).hasSize(4).contains(
                entry("ONE_RESPONDENT_REPRESENTATIVE", true),
                entry(FlowFlag.NOTICE_OF_CHANGE.name(), false),
                entry(FlowFlag.CERTIFICATE_OF_SERVICE.name(), false),
                entry(FlowFlag.GENERAL_APPLICATION_ENABLED.name(), false)
            );
        }

        @Test
        void shouldReturnProceedsWithOfflineJourney_whenCaseTakenOfflinePastClaimDetailsNotificationDeadline() {
            // Given
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDismissedPastClaimDetailsNotificationDeadline()
                .takenOfflineByStaffDate(LocalDateTime.now())
                .build();

            // When
            StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

            // Then
            assertThat(stateFlow.getState())
                .extracting(State::getName)
                .isNotNull()
                .isEqualTo(TAKEN_OFFLINE_BY_STAFF.fullName());
            assertThat(stateFlow.getStateHistory())
                .hasSize(9)
                .extracting(State::getName)
                .containsExactly(
                    DRAFT.fullName(), CLAIM_SUBMITTED.fullName(), CLAIM_ISSUED_PAYMENT_SUCCESSFUL.fullName(),
                    PENDING_CLAIM_ISSUED.fullName(), CLAIM_ISSUED.fullName(), CLAIM_NOTIFIED.fullName(),
                    PAST_CLAIM_DETAILS_NOTIFICATION_DEADLINE_AWAITING_CAMUNDA.fullName(),
                    CLAIM_DISMISSED_PAST_CLAIM_DETAILS_NOTIFICATION_DEADLINE.fullName(),
                    TAKEN_OFFLINE_BY_STAFF.fullName()
                );

            assertThat(stateFlow.getFlags()).hasSize(4).contains(
                entry("ONE_RESPONDENT_REPRESENTATIVE", true),
                entry(FlowFlag.NOTICE_OF_CHANGE.name(), false),
                entry(FlowFlag.CERTIFICATE_OF_SERVICE.name(), false),
                entry(FlowFlag.GENERAL_APPLICATION_ENABLED.name(), false)
            );
        }
    }

    @Nested
    class ClaimDismissedPastClaimDismissedDeadline {

        @Test
        void shouldReturnAwaitingCamundaState_whenDeadlinePassedAfterStateClaimDetailsNotified() {
            // Given
            CaseData caseData = CaseDataBuilder.builder().atStatePastClaimDismissedDeadline().build();
            if (caseData.getRespondent2OrgRegistered() != null
                && caseData.getRespondent2Represented() == null) {
                caseData = caseData.toBuilder()
                    .respondent2Represented(YES)
                    .build();
            }

            // When
            StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

            // Then
            assertThat(stateFlow.getState())
                .extracting(State::getName)
                .isNotNull()
                .isEqualTo(PAST_CLAIM_DISMISSED_DEADLINE_AWAITING_CAMUNDA.fullName());
            assertThat(stateFlow.getStateHistory())
                .hasSize(8)
                .extracting(State::getName)
                .containsExactly(
                    DRAFT.fullName(), CLAIM_SUBMITTED.fullName(), CLAIM_ISSUED_PAYMENT_SUCCESSFUL.fullName(),
                    PENDING_CLAIM_ISSUED.fullName(), CLAIM_ISSUED.fullName(), CLAIM_NOTIFIED.fullName(),
                    CLAIM_DETAILS_NOTIFIED.fullName(), PAST_CLAIM_DISMISSED_DEADLINE_AWAITING_CAMUNDA.fullName()
                );
        }

        @Test
        void shouldReturnAwaitingCamundaState_whenDeadlinePassedAfterStateClaimDetailsNotified_1v2() {
            // Given
            CaseData caseData = CaseDataBuilder.builder().atStatePastClaimDismissedDeadline_1v2().build();
            if (caseData.getRespondent2OrgRegistered() != null
                && caseData.getRespondent2Represented() == null) {
                caseData = caseData.toBuilder()
                    .respondent2Represented(YES)
                    .build();
            }

            // When
            StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

            // Then
            assertThat(stateFlow.getState())
                .extracting(State::getName)
                .isNotNull()
                .isEqualTo(PAST_CLAIM_DISMISSED_DEADLINE_AWAITING_CAMUNDA.fullName());
            assertThat(stateFlow.getStateHistory())
                .hasSize(8)
                .extracting(State::getName)
                .containsExactly(
                    DRAFT.fullName(), CLAIM_SUBMITTED.fullName(), CLAIM_ISSUED_PAYMENT_SUCCESSFUL.fullName(),
                    PENDING_CLAIM_ISSUED.fullName(), CLAIM_ISSUED.fullName(), CLAIM_NOTIFIED.fullName(),
                    CLAIM_DETAILS_NOTIFIED.fullName(), PAST_CLAIM_DISMISSED_DEADLINE_AWAITING_CAMUNDA.fullName()
                );
        }

        @Test
        void shouldReturnClaimDismissedState_whenDeadlinePassedAfterStateClaimDetailsNotifiedAndIsProcessedByCamunda() {
            // Given
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDismissed().build();

            // When
            StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

            // Then
            assertThat(stateFlow.getState())
                .extracting(State::getName)
                .isNotNull()
                .isEqualTo(CLAIM_DISMISSED_PAST_CLAIM_DISMISSED_DEADLINE.fullName());
            assertThat(stateFlow.getStateHistory())
                .hasSize(9)
                .extracting(State::getName)
                .containsExactly(
                    DRAFT.fullName(), CLAIM_SUBMITTED.fullName(), CLAIM_ISSUED_PAYMENT_SUCCESSFUL.fullName(),
                    PENDING_CLAIM_ISSUED.fullName(), CLAIM_ISSUED.fullName(), CLAIM_NOTIFIED.fullName(),
                    CLAIM_DETAILS_NOTIFIED.fullName(), PAST_CLAIM_DISMISSED_DEADLINE_AWAITING_CAMUNDA.fullName(),
                    CLAIM_DISMISSED_PAST_CLAIM_DISMISSED_DEADLINE.fullName()
                );
        }

        @Test
        void shouldReturnAwaitingCamundaState_whenDeadlinePassedAfterStateClaimDetailsNotifiedExtension() {
            // Given
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotifiedTimeExtension()
                .claimDismissedDeadline(LocalDateTime.now().minusDays(5))
                .build();
            if (caseData.getRespondent2OrgRegistered() != null
                && caseData.getRespondent2Represented() == null) {
                caseData = caseData.toBuilder()
                    .respondent2Represented(YES)
                    .build();
            }

            // When
            StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

            // Then
            assertThat(stateFlow.getState())
                .extracting(State::getName)
                .isNotNull()
                .isEqualTo(PAST_CLAIM_DISMISSED_DEADLINE_AWAITING_CAMUNDA.fullName());
            assertThat(stateFlow.getStateHistory())
                .hasSize(9)
                .extracting(State::getName)
                .containsExactly(
                    DRAFT.fullName(), CLAIM_SUBMITTED.fullName(), CLAIM_ISSUED_PAYMENT_SUCCESSFUL.fullName(),
                    PENDING_CLAIM_ISSUED.fullName(), CLAIM_ISSUED.fullName(), CLAIM_NOTIFIED.fullName(),
                    CLAIM_DETAILS_NOTIFIED.fullName(), CLAIM_DETAILS_NOTIFIED_TIME_EXTENSION.fullName(),
                    PAST_CLAIM_DISMISSED_DEADLINE_AWAITING_CAMUNDA.fullName()
                );

            assertThat(stateFlow.getFlags()).hasSize(4).contains(
                entry("ONE_RESPONDENT_REPRESENTATIVE", true),
                entry(FlowFlag.NOTICE_OF_CHANGE.name(), false),
                entry(FlowFlag.CERTIFICATE_OF_SERVICE.name(), false),
                entry(FlowFlag.GENERAL_APPLICATION_ENABLED.name(), false)
            );
        }

        @Test
        void shouldReturnDismissedState_whenDeadlinePassedAfterClaimDetailsNotifiedExtensionAndProcessedByCamunda() {
            // Given
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotifiedTimeExtension()
                .claimDismissedDeadline(LocalDateTime.now().minusDays(5))
                .claimDismissedDate(LocalDateTime.now())
                .build();
            if (caseData.getRespondent2OrgRegistered() != null
                && caseData.getRespondent2Represented() == null) {
                caseData = caseData.toBuilder()
                    .respondent2Represented(YES)
                    .build();
            }

            // When
            StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

            // Then
            assertThat(stateFlow.getState())
                .extracting(State::getName)
                .isNotNull()
                .isEqualTo(CLAIM_DISMISSED_PAST_CLAIM_DISMISSED_DEADLINE.fullName());
            assertThat(stateFlow.getStateHistory())
                .hasSize(10)
                .extracting(State::getName)
                .containsExactly(
                    DRAFT.fullName(), CLAIM_SUBMITTED.fullName(), CLAIM_ISSUED_PAYMENT_SUCCESSFUL.fullName(),
                    PENDING_CLAIM_ISSUED.fullName(), CLAIM_ISSUED.fullName(), CLAIM_NOTIFIED.fullName(),
                    CLAIM_DETAILS_NOTIFIED.fullName(), CLAIM_DETAILS_NOTIFIED_TIME_EXTENSION.fullName(),
                    PAST_CLAIM_DISMISSED_DEADLINE_AWAITING_CAMUNDA.fullName(),
                    CLAIM_DISMISSED_PAST_CLAIM_DISMISSED_DEADLINE.fullName()
                );

            assertThat(stateFlow.getFlags())
                .hasSize(4)
                .contains(
                    entry("ONE_RESPONDENT_REPRESENTATIVE", true),
                    entry(FlowFlag.NOTICE_OF_CHANGE.name(), false),
                    entry(FlowFlag.CERTIFICATE_OF_SERVICE.name(), false),
                    entry(FlowFlag.GENERAL_APPLICATION_ENABLED.name(), false)
                );
        }

        @Test
        void shouldReturnClaimDismissedPastDeadline_whenDeadlinePassedAfterStateNotificationAcknowledged() {
            // Given
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged()
                .claimDismissedDeadline(LocalDateTime.now().minusDays(5))
                .build();
            if (caseData.getRespondent2OrgRegistered() != null
                && caseData.getRespondent2Represented() == null) {
                caseData = caseData.toBuilder()
                    .respondent2Represented(YES)
                    .build();
            }

            // When
            StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

            // Then
            assertThat(stateFlow.getState())
                .extracting(State::getName)
                .isNotNull()
                .isEqualTo(PAST_CLAIM_DISMISSED_DEADLINE_AWAITING_CAMUNDA.fullName());
            assertThat(stateFlow.getStateHistory())
                .hasSize(9)
                .extracting(State::getName)
                .containsExactly(
                    DRAFT.fullName(),
                    CLAIM_SUBMITTED.fullName(), CLAIM_ISSUED_PAYMENT_SUCCESSFUL.fullName(),
                    PENDING_CLAIM_ISSUED.fullName(), CLAIM_ISSUED.fullName(), CLAIM_NOTIFIED.fullName(),
                    CLAIM_DETAILS_NOTIFIED.fullName(), NOTIFICATION_ACKNOWLEDGED.fullName(),
                    PAST_CLAIM_DISMISSED_DEADLINE_AWAITING_CAMUNDA.fullName()
                );

            assertThat(stateFlow.getFlags()).hasSize(4).contains(
                entry("ONE_RESPONDENT_REPRESENTATIVE", true),
                entry(FlowFlag.NOTICE_OF_CHANGE.name(), false),
                entry(FlowFlag.CERTIFICATE_OF_SERVICE.name(), false),
                entry(FlowFlag.GENERAL_APPLICATION_ENABLED.name(), false)
            );
        }

        @Test
        void shouldReturnDismissedState_whenDeadlinePassedAfterNotificationAcknowledgedAndProcessedByCamunda() {
            // Given
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged()
                .claimDismissedDeadline(LocalDateTime.now().minusDays(5))
                .claimDismissedDate(LocalDateTime.now())
                .build();

            // When
            StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

            // Then
            assertThat(stateFlow.getState())
                .extracting(State::getName)
                .isNotNull()
                .isEqualTo(CLAIM_DISMISSED_PAST_CLAIM_DISMISSED_DEADLINE.fullName());
            assertThat(stateFlow.getStateHistory())
                .hasSize(10)
                .extracting(State::getName)
                .containsExactly(
                    DRAFT.fullName(),
                    CLAIM_SUBMITTED.fullName(), CLAIM_ISSUED_PAYMENT_SUCCESSFUL.fullName(),
                    PENDING_CLAIM_ISSUED.fullName(), CLAIM_ISSUED.fullName(), CLAIM_NOTIFIED.fullName(),
                    CLAIM_DETAILS_NOTIFIED.fullName(), NOTIFICATION_ACKNOWLEDGED.fullName(),
                    PAST_CLAIM_DISMISSED_DEADLINE_AWAITING_CAMUNDA.fullName(),
                    CLAIM_DISMISSED_PAST_CLAIM_DISMISSED_DEADLINE.fullName()
                );

            assertThat(stateFlow.getFlags()).hasSize(4).contains(
                entry("ONE_RESPONDENT_REPRESENTATIVE", true),
                entry(FlowFlag.NOTICE_OF_CHANGE.name(), false),
                entry(FlowFlag.CERTIFICATE_OF_SERVICE.name(), false),
                entry(FlowFlag.GENERAL_APPLICATION_ENABLED.name(), false)
            );
        }

        @Test
        void shouldReturnAwaitingCamundaState_whenDeadlinePassedAfterStateNotificationAcknowledgedTimeExtension() {
            // Given
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledgedRespondent1TimeExtension()
                .claimDismissedDeadline(LocalDateTime.now().minusDays(5))
                .build();
            if (caseData.getRespondent2OrgRegistered() != null
                && caseData.getRespondent2Represented() == null) {
                caseData = caseData.toBuilder()
                    .respondent2Represented(YES)
                    .build();
            }

            // When
            StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

            // Then
            assertThat(stateFlow.getState())
                .extracting(State::getName)
                .isNotNull()
                .isEqualTo(PAST_CLAIM_DISMISSED_DEADLINE_AWAITING_CAMUNDA.fullName());
            assertThat(stateFlow.getStateHistory())
                .hasSize(10)
                .extracting(State::getName)
                .containsExactly(
                    DRAFT.fullName(),
                    CLAIM_SUBMITTED.fullName(), CLAIM_ISSUED_PAYMENT_SUCCESSFUL.fullName(),
                    PENDING_CLAIM_ISSUED.fullName(), CLAIM_ISSUED.fullName(), CLAIM_NOTIFIED.fullName(),
                    CLAIM_DETAILS_NOTIFIED.fullName(), NOTIFICATION_ACKNOWLEDGED.fullName(),
                    NOTIFICATION_ACKNOWLEDGED_TIME_EXTENSION.fullName(),
                    PAST_CLAIM_DISMISSED_DEADLINE_AWAITING_CAMUNDA.fullName()
                );
        }

        @Test
        void shouldReturnClaimDismissed_whenDeadlinePassedAfterNotificationAckTimeExtensionAndProcessedByCamunda() {
            // Given
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledgedRespondent1TimeExtension()
                .claimDismissedDeadline(LocalDateTime.now().minusDays(5))
                .claimDismissedDate(LocalDateTime.now())
                .build();
            if (caseData.getRespondent2OrgRegistered() != null
                && caseData.getRespondent2Represented() == null) {
                caseData = caseData.toBuilder()
                    .respondent2Represented(YES)
                    .build();
            }

            // When
            StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

            // Then
            assertThat(stateFlow.getState())
                .extracting(State::getName)
                .isNotNull()
                .isEqualTo(CLAIM_DISMISSED_PAST_CLAIM_DISMISSED_DEADLINE.fullName());
            assertThat(stateFlow.getStateHistory())
                .hasSize(11)
                .extracting(State::getName)
                .containsExactly(
                    DRAFT.fullName(),
                    CLAIM_SUBMITTED.fullName(), CLAIM_ISSUED_PAYMENT_SUCCESSFUL.fullName(),
                    PENDING_CLAIM_ISSUED.fullName(), CLAIM_ISSUED.fullName(), CLAIM_NOTIFIED.fullName(),
                    CLAIM_DETAILS_NOTIFIED.fullName(), NOTIFICATION_ACKNOWLEDGED.fullName(),
                    NOTIFICATION_ACKNOWLEDGED_TIME_EXTENSION.fullName(),
                    PAST_CLAIM_DISMISSED_DEADLINE_AWAITING_CAMUNDA.fullName(),
                    CLAIM_DISMISSED_PAST_CLAIM_DISMISSED_DEADLINE.fullName()
                );
        }

        @Test
        void shouldReturnClaimDismissed_whenCaseDataAtStateClaimDismissed() {
            // Given
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDismissed().build();

            // When
            StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

            // Then
            assertThat(stateFlow.getState())
                .extracting(State::getName)
                .isNotNull()
                .isEqualTo(CLAIM_DISMISSED_PAST_CLAIM_DISMISSED_DEADLINE.fullName());
            assertThat(stateFlow.getStateHistory())
                .hasSize(9)
                .extracting(State::getName)
                .containsExactly(
                    DRAFT.fullName(), CLAIM_SUBMITTED.fullName(), CLAIM_ISSUED_PAYMENT_SUCCESSFUL.fullName(),
                    PENDING_CLAIM_ISSUED.fullName(), CLAIM_ISSUED.fullName(), CLAIM_NOTIFIED.fullName(),
                    CLAIM_DETAILS_NOTIFIED.fullName(), PAST_CLAIM_DISMISSED_DEADLINE_AWAITING_CAMUNDA.fullName(),
                    CLAIM_DISMISSED_PAST_CLAIM_DISMISSED_DEADLINE.fullName()
                );

            assertThat(stateFlow.getFlags()).hasSize(4).contains(
                entry("ONE_RESPONDENT_REPRESENTATIVE", true),
                entry(FlowFlag.NOTICE_OF_CHANGE.name(), false),
                entry(FlowFlag.CERTIFICATE_OF_SERVICE.name(), false),
                entry(FlowFlag.GENERAL_APPLICATION_ENABLED.name(), false)
            );
        }
    }

    @Nested
    class HasTransitionedTo {

        @ParameterizedTest
        @CsvSource({
            "true,CLAIM_ISSUED",
            "true,CLAIM_ISSUED_PAYMENT_SUCCESSFUL",
            "true,PENDING_CLAIM_ISSUED",
            "true,DRAFT",
            "false,FULL_DEFENCE",
            "false,FULL_DEFENCE_PROCEED",
            "false,FULL_DEFENCE_NOT_PROCEED",
            "false,NOTIFICATION_ACKNOWLEDGED",
        })
        void shouldReturnValidResult_whenCaseDataAtStateAwaitingRespondentAcknowledgement(boolean expected,
                                                                                          FlowState.Main state) {
            CaseDetails caseDetails = CaseDetailsBuilder.builder()
                .atStateAwaitingRespondentAcknowledgement()
                .build();

            assertThat(stateFlowEngine.hasTransitionedTo(caseDetails, state)).isEqualTo(expected);
        }
    }

    @Nested
    class SpecScenarios {

        @Nested
        class DefendantResponseMultiparty {

            @Test
            //1v2 Different solicitor scenario-first response FullDefence received
            void shouldGenerateDQ_1v2DiffSol_whenFirstResponseIsFullDefence() {
                // Given
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateRespondentFullDefence()
                    .multiPartyClaimTwoDefendantSolicitors()
                    .build();
                if (caseData.getRespondent2OrgRegistered() != null
                    && caseData.getRespondent2Represented() == null) {
                    caseData = caseData.toBuilder()
                        .respondent2Represented(YES)
                        .build();
                }

                // When
                StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

                // Then
                assertThat(stateFlow.getState())
                    .extracting(State::getName)
                    .isNotNull()
                    .isEqualTo(AWAITING_RESPONSES_FULL_DEFENCE_RECEIVED.fullName());
                assertThat(stateFlow.getStateHistory())
                    .hasSize(9)
                    .extracting(State::getName)
                    .containsExactly(
                        DRAFT.fullName(), CLAIM_SUBMITTED.fullName(), CLAIM_ISSUED_PAYMENT_SUCCESSFUL.fullName(),
                        PENDING_CLAIM_ISSUED.fullName(), CLAIM_ISSUED.fullName(), CLAIM_NOTIFIED.fullName(),
                        CLAIM_DETAILS_NOTIFIED.fullName(), NOTIFICATION_ACKNOWLEDGED.fullName(),
                        AWAITING_RESPONSES_FULL_DEFENCE_RECEIVED.fullName()
                    );

                assertThat(stateFlow.getFlags()).hasSize(5).contains(
                    entry("ONE_RESPONDENT_REPRESENTATIVE", false),
                    entry("TWO_RESPONDENT_REPRESENTATIVES", true),
                    entry(FlowFlag.NOTICE_OF_CHANGE.name(), false),
                    entry(FlowFlag.CERTIFICATE_OF_SERVICE.name(), false),
                    entry(FlowFlag.GENERAL_APPLICATION_ENABLED.name(), false)
                );
            }

            @Test
            //1v2 Different solicitor scenario-first response FullDefence received
            void shouldGenerateDQ_1v2DiffSol_whenFirstResponseIsNotFullDefence() {
                // Given
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateRespondentCounterClaim()
                    .multiPartyClaimTwoDefendantSolicitors()
                    .build();
                if (caseData.getRespondent2OrgRegistered() != null
                    && caseData.getRespondent2Represented() == null) {
                    caseData = caseData.toBuilder()
                        .respondent2Represented(YES)
                        .build();
                }

                // When
                StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

                // Then
                assertThat(stateFlow.getState())
                    .extracting(State::getName)
                    .isNotNull()
                    .isEqualTo(AWAITING_RESPONSES_NOT_FULL_DEFENCE_RECEIVED.fullName());
                assertThat(stateFlow.getStateHistory())
                    .hasSize(9)
                    .extracting(State::getName)
                    .containsExactly(
                        DRAFT.fullName(), CLAIM_SUBMITTED.fullName(), CLAIM_ISSUED_PAYMENT_SUCCESSFUL.fullName(),
                        PENDING_CLAIM_ISSUED.fullName(), CLAIM_ISSUED.fullName(), CLAIM_NOTIFIED.fullName(),
                        CLAIM_DETAILS_NOTIFIED.fullName(), NOTIFICATION_ACKNOWLEDGED.fullName(),
                        AWAITING_RESPONSES_NOT_FULL_DEFENCE_RECEIVED.fullName()
                    );

                assertThat(stateFlow.getFlags()).hasSize(5).contains(
                    entry("ONE_RESPONDENT_REPRESENTATIVE", false),
                    entry("TWO_RESPONDENT_REPRESENTATIVES", true),
                    entry(FlowFlag.NOTICE_OF_CHANGE.name(), false),
                    entry(FlowFlag.CERTIFICATE_OF_SERVICE.name(), false),
                    entry(FlowFlag.GENERAL_APPLICATION_ENABLED.name(), false)
                );
            }

            @Test
            //1v2 Different solicitor scenario-first response FullDefence received
            void shouldGenerateDQ_in1v2Scenario_whenFirstPartySubmitFullDefenceResponse() {
                // Given
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateRespondentFullDefence()
                    .multiPartyClaimTwoDefendantSolicitors()
                    .build();
                if (caseData.getRespondent2OrgRegistered() != null
                    && caseData.getRespondent2Represented() == null) {
                    caseData = caseData.toBuilder()
                        .respondent2Represented(YES)
                        .build();
                }

                // When
                StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

                // Then
                assertThat(stateFlow.getState())
                    .extracting(State::getName)
                    .isNotNull()
                    .isEqualTo(AWAITING_RESPONSES_FULL_DEFENCE_RECEIVED.fullName());
                assertThat(stateFlow.getStateHistory())
                    .hasSize(9)
                    .extracting(State::getName)
                    .containsExactly(
                        DRAFT.fullName(), CLAIM_SUBMITTED.fullName(), CLAIM_ISSUED_PAYMENT_SUCCESSFUL.fullName(),
                        PENDING_CLAIM_ISSUED.fullName(), CLAIM_ISSUED.fullName(), CLAIM_NOTIFIED.fullName(),
                        CLAIM_DETAILS_NOTIFIED.fullName(), NOTIFICATION_ACKNOWLEDGED.fullName(),
                        AWAITING_RESPONSES_FULL_DEFENCE_RECEIVED.fullName()
                    );

                assertThat(stateFlow.getFlags()).hasSize(5).contains(
                    entry("ONE_RESPONDENT_REPRESENTATIVE", false),
                    entry("TWO_RESPONDENT_REPRESENTATIVES", true),
                    entry(FlowFlag.NOTICE_OF_CHANGE.name(), false),
                    entry(FlowFlag.CERTIFICATE_OF_SERVICE.name(), false),
                    entry(FlowFlag.GENERAL_APPLICATION_ENABLED.name(), false)
                );
            }

            @Test
            //1v2 Different solicitor scenario-first party acknowledges, not responds
            // second party submits response FullDefence
            void shouldGenerateDQ_in1v2Scenario_whenSecondPartySubmitFullDefenceResponse() {
                // Given
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateRespondentFullDefenceRespondent2()
                    .multiPartyClaimTwoDefendantSolicitors()
                    .build();
                if (caseData.getRespondent2OrgRegistered() != null
                    && caseData.getRespondent2Represented() == null) {
                    caseData = caseData.toBuilder()
                        .respondent2Represented(YES)
                        .build();
                }

                // When
                StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

                // Then
                assertThat(stateFlow.getState())
                    .extracting(State::getName)
                    .isNotNull()
                    .isEqualTo(AWAITING_RESPONSES_FULL_DEFENCE_RECEIVED.fullName());
                assertThat(stateFlow.getStateHistory())
                    .hasSize(9)
                    .extracting(State::getName)
                    .containsExactly(
                        DRAFT.fullName(), CLAIM_SUBMITTED.fullName(), CLAIM_ISSUED_PAYMENT_SUCCESSFUL.fullName(),
                        PENDING_CLAIM_ISSUED.fullName(), CLAIM_ISSUED.fullName(), CLAIM_NOTIFIED.fullName(),
                        CLAIM_DETAILS_NOTIFIED.fullName(), NOTIFICATION_ACKNOWLEDGED.fullName(),
                        AWAITING_RESPONSES_FULL_DEFENCE_RECEIVED.fullName()
                    );

                assertThat(stateFlow.getFlags()).hasSize(5).contains(
                    entry("ONE_RESPONDENT_REPRESENTATIVE", false),
                    entry("TWO_RESPONDENT_REPRESENTATIVES", true),
                    entry(FlowFlag.NOTICE_OF_CHANGE.name(), false),
                    entry(FlowFlag.CERTIFICATE_OF_SERVICE.name(), false),
                    entry(FlowFlag.GENERAL_APPLICATION_ENABLED.name(), false)
                );
            }

            @Test
            //Respondent 1 submits FULL DEFENCE, Respondent 2 submits FULL DEFENCE
            void shouldReturnFullDefence_in1v2Scenario_whenBothPartiesSubmitFullDefenceResponses() {
                // Given
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateRespondentFullDefence_1v2_BothPartiesFullDefenceResponses()
                    .multiPartyClaimTwoDefendantSolicitors()
                    .build();
                if (caseData.getRespondent2OrgRegistered() != null
                    && caseData.getRespondent2Represented() == null) {
                    caseData = caseData.toBuilder()
                        .respondent2Represented(YES)
                        .build();
                }

                // When
                StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

                // Then
                assertThat(stateFlow.getState())
                    .extracting(State::getName)
                    .isNotNull()
                    .isEqualTo(FULL_DEFENCE.fullName());
                assertThat(stateFlow.getStateHistory())
                    .hasSize(10)
                    .extracting(State::getName)
                    .containsExactly(
                        DRAFT.fullName(), CLAIM_SUBMITTED.fullName(), CLAIM_ISSUED_PAYMENT_SUCCESSFUL.fullName(),
                        PENDING_CLAIM_ISSUED.fullName(), CLAIM_ISSUED.fullName(), CLAIM_NOTIFIED.fullName(),
                        CLAIM_DETAILS_NOTIFIED.fullName(), NOTIFICATION_ACKNOWLEDGED.fullName(),
                        ALL_RESPONSES_RECEIVED.fullName(), FULL_DEFENCE.fullName()
                    );

                assertThat(stateFlow.getFlags()).hasSize(5).contains(
                    entry("ONE_RESPONDENT_REPRESENTATIVE", false),
                    entry("TWO_RESPONDENT_REPRESENTATIVES", true),
                    entry(FlowFlag.NOTICE_OF_CHANGE.name(), false),
                    entry(FlowFlag.CERTIFICATE_OF_SERVICE.name(), false),
                    entry(FlowFlag.GENERAL_APPLICATION_ENABLED.name(), false)
                );
            }

            @Test
            //Respondent 1 and 2 acknowledges claim, then submits  FULL DEFENCE
            void shouldReturnFullDefence_in1v2Scenario_whenBothPartiesAcknowledgedAndSubmitFullDefenceResponses() {
                // Given
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateRespondentFullDefence_1v2_BothPartiesFullDefenceResponses()
                    .atStateNotificationAcknowledgedRespondent2()
                    .multiPartyClaimTwoDefendantSolicitors()
                    .build();
                if (caseData.getRespondent2OrgRegistered() != null
                    && caseData.getRespondent2Represented() == null) {
                    caseData = caseData.toBuilder()
                        .respondent2Represented(YES)
                        .build();
                }

                // When
                StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

                // Then
                assertThat(stateFlow.getState())
                    .extracting(State::getName)
                    .isNotNull()
                    .isEqualTo(FULL_DEFENCE.fullName());
                assertThat(stateFlow.getStateHistory())
                    .hasSize(10)
                    .extracting(State::getName)
                    .containsExactly(
                        DRAFT.fullName(), CLAIM_SUBMITTED.fullName(), CLAIM_ISSUED_PAYMENT_SUCCESSFUL.fullName(),
                        PENDING_CLAIM_ISSUED.fullName(), CLAIM_ISSUED.fullName(), CLAIM_NOTIFIED.fullName(),
                        CLAIM_DETAILS_NOTIFIED.fullName(), NOTIFICATION_ACKNOWLEDGED.fullName(),
                        ALL_RESPONSES_RECEIVED.fullName(), FULL_DEFENCE.fullName()
                    );

                assertThat(stateFlow.getFlags()).hasSize(5).contains(
                    entry("ONE_RESPONDENT_REPRESENTATIVE", false),
                    entry("TWO_RESPONDENT_REPRESENTATIVES", true),
                    entry(FlowFlag.NOTICE_OF_CHANGE.name(), false),
                    entry(FlowFlag.CERTIFICATE_OF_SERVICE.name(), false),
                    entry(FlowFlag.GENERAL_APPLICATION_ENABLED.name(), false)
                );
            }

            @Test
            //Respondent 1 acknowledges claim, then Respondent 1 & 2 submits  FULL DEFENCE
            void shouldReturnFullDefence_in1v2Scenario_whenRep1AcknowledgedAndBothSubmitFullDefenceResponses() {
                // Given
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateRespondentFullDefence_1v2_BothPartiesFullDefenceResponses()
                    .atStateNotificationAcknowledgedRespondent2()
                    .respondent2AcknowledgeNotificationDate(null)
                    .multiPartyClaimTwoDefendantSolicitors()
                    .build();
                if (caseData.getRespondent2OrgRegistered() != null
                    && caseData.getRespondent2Represented() == null) {
                    caseData = caseData.toBuilder()
                        .respondent2Represented(YES)
                        .build();
                }

                // When
                StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

                // Then
                assertThat(stateFlow.getState())
                    .extracting(State::getName)
                    .isNotNull()
                    .isEqualTo(FULL_DEFENCE.fullName());
                assertThat(stateFlow.getStateHistory())
                    .hasSize(10)
                    .extracting(State::getName)
                    .containsExactly(
                        DRAFT.fullName(), CLAIM_SUBMITTED.fullName(), CLAIM_ISSUED_PAYMENT_SUCCESSFUL.fullName(),
                        PENDING_CLAIM_ISSUED.fullName(), CLAIM_ISSUED.fullName(), CLAIM_NOTIFIED.fullName(),
                        CLAIM_DETAILS_NOTIFIED.fullName(), NOTIFICATION_ACKNOWLEDGED.fullName(),
                        ALL_RESPONSES_RECEIVED.fullName(), FULL_DEFENCE.fullName()
                    );

                assertThat(stateFlow.getFlags()).hasSize(5).contains(
                    entry("ONE_RESPONDENT_REPRESENTATIVE", false),
                    entry("TWO_RESPONDENT_REPRESENTATIVES", true),
                    entry(FlowFlag.NOTICE_OF_CHANGE.name(), false),
                    entry(FlowFlag.CERTIFICATE_OF_SERVICE.name(), false),
                    entry(FlowFlag.GENERAL_APPLICATION_ENABLED.name(), false)
                );
            }

            @Test
            // Respondent 2 acknowledges claim, Respondent 1 & 2 submits  FULL DEFENCE
            void shouldReturnFullDefence_in1v2Scenario_whenRep2AcknowledgedAndBothSubmitFullDefenceResponses() {
                // Given
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateRespondentFullDefence_1v2_BothPartiesFullDefenceResponses()
                    .atStateNotificationAcknowledgedRespondent2()
                    .respondent1AcknowledgeNotificationDate(null)
                    .multiPartyClaimTwoDefendantSolicitors()
                    .build();
                if (caseData.getRespondent2OrgRegistered() != null
                    && caseData.getRespondent2Represented() == null) {
                    caseData = caseData.toBuilder()
                        .respondent2Represented(YES)
                        .build();
                }

                // When
                StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

                // Then
                assertThat(stateFlow.getState())
                    .extracting(State::getName)
                    .isNotNull()
                    .isEqualTo(FULL_DEFENCE.fullName());
                assertThat(stateFlow.getStateHistory())
                    .hasSize(10)
                    .extracting(State::getName)
                    .containsExactly(
                        DRAFT.fullName(), CLAIM_SUBMITTED.fullName(), CLAIM_ISSUED_PAYMENT_SUCCESSFUL.fullName(),
                        PENDING_CLAIM_ISSUED.fullName(), CLAIM_ISSUED.fullName(), CLAIM_NOTIFIED.fullName(),
                        CLAIM_DETAILS_NOTIFIED.fullName(), NOTIFICATION_ACKNOWLEDGED.fullName(),
                        ALL_RESPONSES_RECEIVED.fullName(), FULL_DEFENCE.fullName()
                    );

                assertThat(stateFlow.getFlags()).hasSize(5).contains(
                    entry("ONE_RESPONDENT_REPRESENTATIVE", false),
                    entry("TWO_RESPONDENT_REPRESENTATIVES", true),
                    entry(FlowFlag.NOTICE_OF_CHANGE.name(), false),
                    entry(FlowFlag.GENERAL_APPLICATION_ENABLED.name(), false),
                    entry(FlowFlag.CERTIFICATE_OF_SERVICE.name(), false)
                );
            }

            @Test
            //Respondent 1 submits FULL DEFENCE, Respondent 2 submits COUNTER CLAIM
            void shouldReturnDivergentResponseAndGoOffline_1v2Scenario_whenFirstRespondentSubmitsFullDefenceResponse() {
                // Given
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateRespondentFullDefence_1v2_Resp1FullDefenceAndResp2CounterClaim()
                    .multiPartyClaimTwoDefendantSolicitors()
                    .build();
                if (caseData.getRespondent2OrgRegistered() != null
                    && caseData.getRespondent2Represented() == null) {
                    caseData = caseData.toBuilder()
                        .respondent2Represented(YES)
                        .build();
                }

                // When
                StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

                // Then
                assertThat(stateFlow.getState())
                    .extracting(State::getName)
                    .isNotNull()
                    .isEqualTo(DIVERGENT_RESPOND_GO_OFFLINE.fullName());
                assertThat(stateFlow.getStateHistory())
                    .hasSize(10)
                    .extracting(State::getName)
                    .containsExactly(
                        DRAFT.fullName(), CLAIM_SUBMITTED.fullName(), CLAIM_ISSUED_PAYMENT_SUCCESSFUL.fullName(),
                        PENDING_CLAIM_ISSUED.fullName(), CLAIM_ISSUED.fullName(), CLAIM_NOTIFIED.fullName(),
                        CLAIM_DETAILS_NOTIFIED.fullName(), NOTIFICATION_ACKNOWLEDGED.fullName(),
                        ALL_RESPONSES_RECEIVED.fullName(), DIVERGENT_RESPOND_GO_OFFLINE.fullName()
                    );

                assertThat(stateFlow.getFlags()).hasSize(5).contains(
                    entry("ONE_RESPONDENT_REPRESENTATIVE", false),
                    entry("TWO_RESPONDENT_REPRESENTATIVES", true),
                    entry(FlowFlag.NOTICE_OF_CHANGE.name(), false),
                    entry(FlowFlag.CERTIFICATE_OF_SERVICE.name(), false),
                    entry(FlowFlag.GENERAL_APPLICATION_ENABLED.name(), false)
                );
            }

            @Test
            //Respondent 1 submits FULL DEFENCE, Respondent 2 submits COUNTER CLAIM
            void shouldReturnDivergentResponse_in1v2SameSolicitorScenario_whenOneRespondentSubmitsFullDefence() {
                // Given
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateDivergentResponseWithFullDefence1v2SameSol_NotSingleDQ()
                    .atStateNotificationAcknowledged1v2SameSolicitor()
                    .multiPartyClaimOneDefendantSolicitor()
                    .build();
                if (caseData.getRespondent2OrgRegistered() != null
                    && caseData.getRespondent2Represented() == null) {
                    caseData = caseData.toBuilder()
                        .respondent2Represented(YES)
                        .build();
                }

                // When
                StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

                // Then
                assertThat(stateFlow.getState())
                    .extracting(State::getName)
                    .isNotNull()
                    .isEqualTo(DIVERGENT_RESPOND_GENERATE_DQ_GO_OFFLINE.fullName());
                assertThat(stateFlow.getStateHistory())
                    .hasSize(10)
                    .extracting(State::getName)
                    .containsExactly(
                        DRAFT.fullName(), CLAIM_SUBMITTED.fullName(), CLAIM_ISSUED_PAYMENT_SUCCESSFUL.fullName(),
                        PENDING_CLAIM_ISSUED.fullName(), CLAIM_ISSUED.fullName(), CLAIM_NOTIFIED.fullName(),
                        CLAIM_DETAILS_NOTIFIED.fullName(), NOTIFICATION_ACKNOWLEDGED.fullName(),
                        ALL_RESPONSES_RECEIVED.fullName(), DIVERGENT_RESPOND_GENERATE_DQ_GO_OFFLINE.fullName()
                    );

                assertThat(stateFlow.getFlags()).hasSize(4).contains(
                    entry("ONE_RESPONDENT_REPRESENTATIVE", true),
                    entry(FlowFlag.NOTICE_OF_CHANGE.name(), false),
                    entry(FlowFlag.CERTIFICATE_OF_SERVICE.name(), false),
                    entry(FlowFlag.GENERAL_APPLICATION_ENABLED.name(), false)
                );
            }

            //Respondent 1 submits ADMITS PART, Respondent 2 submits COUNTER CLAIM
            @Test
            void shouldReturnDivergentResponse_in1v2Scenario_whenNeitherRespondentSubmitsFullDefenceResponse() {
                // Given
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateDivergentResponse_1v2_Resp1FullAdmissionAndResp2CounterClaim()
                    .multiPartyClaimTwoDefendantSolicitors()
                    .build();
                if (caseData.getRespondent2OrgRegistered() != null
                    && caseData.getRespondent2Represented() == null) {
                    caseData = caseData.toBuilder()
                        .respondent2Represented(YES)
                        .build();
                }

                // When
                StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

                // Then
                assertThat(stateFlow.getState())
                    .extracting(State::getName)
                    .isNotNull()
                    .isEqualTo(DIVERGENT_RESPOND_GO_OFFLINE.fullName());
                assertThat(stateFlow.getStateHistory())
                    .hasSize(10)
                    .extracting(State::getName)
                    .containsExactly(
                        DRAFT.fullName(), CLAIM_SUBMITTED.fullName(), CLAIM_ISSUED_PAYMENT_SUCCESSFUL.fullName(),
                        PENDING_CLAIM_ISSUED.fullName(), CLAIM_ISSUED.fullName(), CLAIM_NOTIFIED.fullName(),
                        CLAIM_DETAILS_NOTIFIED.fullName(), NOTIFICATION_ACKNOWLEDGED.fullName(),
                        ALL_RESPONSES_RECEIVED.fullName(), DIVERGENT_RESPOND_GO_OFFLINE.fullName()
                    );

                assertThat(stateFlow.getFlags()).hasSize(5).contains(
                    entry("ONE_RESPONDENT_REPRESENTATIVE", false),
                    entry("TWO_RESPONDENT_REPRESENTATIVES", true),
                    entry(FlowFlag.NOTICE_OF_CHANGE.name(), false),
                    entry(FlowFlag.CERTIFICATE_OF_SERVICE.name(), false),
                    entry(FlowFlag.GENERAL_APPLICATION_ENABLED.name(), false)
                );
            }

            //Respondent 1 submits ADMITS PART, Respondent 2 submits ADMITS PART
            @Test
            void shouldReturnAdmitsPartResponse_in1v2Scenario_whenBothRespondentsSubmitAdmitPartResponses() {
                // Given
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateFullAdmission_1v2_BothRespondentSolicitorsSubmitFullAdmissionResponse()
                    .multiPartyClaimTwoDefendantSolicitors()
                    .build();
                if (caseData.getRespondent2OrgRegistered() != null
                    && caseData.getRespondent2Represented() == null) {
                    caseData = caseData.toBuilder()
                        .respondent2Represented(YES)
                        .build();
                }

                // When
                StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

                // Then
                assertThat(stateFlow.getState())
                    .extracting(State::getName)
                    .isNotNull()
                    .isEqualTo(FULL_ADMISSION.fullName());
                assertThat(stateFlow.getStateHistory())
                    .hasSize(10)
                    .extracting(State::getName)
                    .containsExactly(
                        DRAFT.fullName(), CLAIM_SUBMITTED.fullName(), CLAIM_ISSUED_PAYMENT_SUCCESSFUL.fullName(),
                        PENDING_CLAIM_ISSUED.fullName(), CLAIM_ISSUED.fullName(), CLAIM_NOTIFIED.fullName(),
                        CLAIM_DETAILS_NOTIFIED.fullName(), NOTIFICATION_ACKNOWLEDGED.fullName(),
                        ALL_RESPONSES_RECEIVED.fullName(), FULL_ADMISSION.fullName()
                    );

                assertThat(stateFlow.getFlags()).hasSize(5).contains(
                    entry("ONE_RESPONDENT_REPRESENTATIVE", false),
                    entry("TWO_RESPONDENT_REPRESENTATIVES", true),
                    entry(FlowFlag.NOTICE_OF_CHANGE.name(), false),
                    entry(FlowFlag.CERTIFICATE_OF_SERVICE.name(), false),
                    entry(FlowFlag.GENERAL_APPLICATION_ENABLED.name(), false)
                );
            }
        }
    }

    @Nested
    class AmbiguousErrors {

        @Test
        void claimIssue_fullAdmitAndDivergentRespondGoOffline() {
            CaseData caseData = CaseData.builder()
                .caseAccessCategory(SPEC_CLAIM)
                .applicant1(Party.builder().build())
                .respondent1(Party.builder().build())
                .respondent2(Party.builder().build())
                .respondent2SameLegalRepresentative(YES)
                .respondent1ResponseDate(LocalDateTime.now())
                .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION)
                .respondentResponseIsSame(YES)
                .build();
            assertThat(FlowPredicate.fullAdmissionSpec.test(caseData))
                .isTrue();
            assertThat(divergentRespondGoOfflineSpec.and(specClaim).test(caseData))
                .isFalse();
        }

        @Test
        void claim1v1_reachFullAdmitProceed() {
            CaseData.CaseDataBuilder<?, ?> builder = claim1v1Submitted();

            assertThat(stateFlowEngine.evaluate(builder.build()).getState().getName())
                .isEqualTo(CLAIM_SUBMITTED.fullName());

            payPBA(builder);

            assertThat(stateFlowEngine.evaluate(builder.build()).getState().getName())
                .isEqualTo(CLAIM_ISSUED_PAYMENT_SUCCESSFUL.fullName());

            issuedAndRepresented(builder);

            assertThat(stateFlowEngine.evaluate(builder.build()).getState().getName())
                .isEqualTo(PENDING_CLAIM_ISSUED.fullName());

            issued(builder);

            assertThat(stateFlowEngine.evaluate(builder.build()).getState().getName())
                .isEqualTo(CLAIM_ISSUED.fullName());

            fullAdmit1v1(builder);

            assertThat(stateFlowEngine.evaluate(builder.build()).getState().getName())
                .isEqualTo(FULL_ADMISSION.fullName());

            applicantProceeds1v1(builder);

            assertThat(stateFlowEngine.evaluate(builder.build()).getState().getName())
                .isEqualTo(FULL_ADMIT_PROCEED.fullName());
        }

        @Test
        void claim1v1_reachFullAdmitNoProceed() {
            CaseData.CaseDataBuilder<?, ?> builder = claim1v1Submitted();

            assertThat(stateFlowEngine.evaluate(builder.build()).getState().getName())
                .isEqualTo(CLAIM_SUBMITTED.fullName());

            payPBA(builder);

            assertThat(stateFlowEngine.evaluate(builder.build()).getState().getName())
                .isEqualTo(CLAIM_ISSUED_PAYMENT_SUCCESSFUL.fullName());

            issuedAndRepresented(builder);

            assertThat(stateFlowEngine.evaluate(builder.build()).getState().getName())
                .isEqualTo(PENDING_CLAIM_ISSUED.fullName());

            issued(builder);

            assertThat(stateFlowEngine.evaluate(builder.build()).getState().getName())
                .isEqualTo(CLAIM_ISSUED.fullName());

            fullAdmit1v1(builder);

            assertThat(stateFlowEngine.evaluate(builder.build()).getState().getName())
                .isEqualTo(FULL_ADMISSION.fullName());

            applicantDoesntProceed1v1(builder);

            assertThat(stateFlowEngine.evaluate(builder.build()).getState().getName())
                .isEqualTo(FULL_ADMIT_NOT_PROCEED.fullName());
        }

        private CaseData.CaseDataBuilder<?, ?> claim1v1Submitted() {
            return CaseData.builder()
                .caseAccessCategory(SPEC_CLAIM)
                .applicant1(Party.builder().build())
                .respondent1(Party.builder().build())
                .submittedDate(LocalDateTime.now());
        }

        private void payPBA(CaseData.CaseDataBuilder<?, ?> builder) {
            builder.paymentSuccessfulDate(LocalDateTime.now());
        }

        private void issuedAndRepresented(CaseData.CaseDataBuilder<?, ?> builder) {
            builder
                .issueDate(LocalDate.now())
                .respondent1Represented(YesOrNo.YES)
                .respondent1OrgRegistered(YesOrNo.YES);
        }

        private void issued(CaseData.CaseDataBuilder<?, ?> builder) {
            builder
                .claimNotificationDeadline(LocalDateTime.now().plusDays(14));
        }

        private void fullAdmit1v1(CaseData.CaseDataBuilder<?, ?> builder) {
            builder.respondent1ResponseDate(LocalDateTime.now())
                .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION);
        }

        private void applicantProceeds1v1(CaseData.CaseDataBuilder<?, ?> builder) {
            builder.applicant1ProceedWithClaim(YES);
        }

        private void applicantDoesntProceed1v1(CaseData.CaseDataBuilder<?, ?> builder) {
            builder.applicant1ProceedWithClaim(NO);
        }
    }

    @Nested
    class FromFullDefence {

        @Test
        void fullDefenceNoMediationSpec() {
            // Given
            CaseData caseData = CaseData.builder()
                // spec claim
                .caseAccessCategory(SPEC_CLAIM)
                // claim submitted
                .submittedDate(LocalDateTime.now())
                .respondent1Represented(YES)
                // payment successful
                .paymentSuccessfulDate(LocalDateTime.now())
                // pending claim issued
                .issueDate(LocalDate.now())
                .respondent1OrgRegistered(YES)
                // claim issued
                .claimNotificationDeadline(LocalDateTime.now())
                // full defence
                .respondent1ResponseDate(LocalDateTime.now())
                .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE)
                .claimNotificationDate(LocalDateTime.now())
                .build();

            // When
            StateFlow fullState = stateFlowEngine.evaluate(caseData);

            // Then
            assertEquals(fullState.getState().getName(), FULL_DEFENCE.fullName());

            StateFlow newState = stateFlowEngine.evaluate(caseData.toBuilder()
                                                              .applicant1ProceedWithClaim(YES)
                                                              .build());

            assertEquals(newState.getState().getName(), FULL_DEFENCE_PROCEED.fullName());
            assertNull(newState.getFlags().get(FlowFlag.AGREED_TO_MEDIATION.name()));
        }

        @Test
        void fullDefencePartialMediationSpec() {
            // Given
            CaseData caseData = CaseData.builder()
                // spec claim
                .caseAccessCategory(SPEC_CLAIM)
                // claim submitted
                .submittedDate(LocalDateTime.now())
                .respondent1Represented(YES)
                // payment successful
                .paymentSuccessfulDate(LocalDateTime.now())
                // pending claim issued
                .issueDate(LocalDate.now())
                .respondent1OrgRegistered(YES)
                // claim issued
                .claimNotificationDeadline(LocalDateTime.now())
                // full defence
                .respondent1ResponseDate(LocalDateTime.now())
                .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE)
                .claimNotificationDate(LocalDateTime.now())
                // defendant agrees to mediation
                .responseClaimTrack(AllocatedTrack.SMALL_CLAIM.name())
                .responseClaimMediationSpecRequired(YES)
                .build();

            // When
            StateFlow fullState = stateFlowEngine.evaluate(caseData);

            // Then
            assertEquals(fullState.getState().getName(), FULL_DEFENCE.fullName());

            StateFlow newState = stateFlowEngine.evaluate(caseData.toBuilder()
                                                              .applicant1ProceedWithClaim(YES)
                                                              .applicant1ClaimMediationSpecRequired(
                                                                  SmallClaimMedicalLRspec.builder()
                                                                      .hasAgreedFreeMediation(NO)
                                                                      .build()
                                                              )
                                                              .build());

            assertEquals(newState.getState().getName(), FULL_DEFENCE_PROCEED.fullName());
            assertNull(newState.getFlags().get(FlowFlag.AGREED_TO_MEDIATION.name()));
        }

        @Test
        void fullDefenceAllMediationSpec() {
            // Given
            CaseData caseData = CaseData.builder()
                // spec claim
                .caseAccessCategory(SPEC_CLAIM)
                // claim submitted
                .submittedDate(LocalDateTime.now())
                .respondent1Represented(YES)
                // payment successful
                .paymentSuccessfulDate(LocalDateTime.now())
                // pending claim issued
                .issueDate(LocalDate.now())
                .respondent1OrgRegistered(YES)
                // claim issued
                .claimNotificationDeadline(LocalDateTime.now())
                // full defence
                .respondent1ResponseDate(LocalDateTime.now())
                .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE)
                .claimNotificationDate(LocalDateTime.now())
                // defendant agrees to mediation
                .responseClaimTrack(AllocatedTrack.SMALL_CLAIM.name())
                .responseClaimMediationSpecRequired(YES)
                .applicant1ClaimMediationSpecRequired(SmallClaimMedicalLRspec.builder()
                                                          .hasAgreedFreeMediation(YES).build())
                .build();

            // When
            StateFlow fullState = stateFlowEngine.evaluate(caseData);

            // Then
            assertEquals(fullState.getState().getName(), FULL_DEFENCE.fullName());

            StateFlow newState = stateFlowEngine.evaluate(caseData.toBuilder()
                                                              .applicant1ProceedWithClaim(YES)
                                                              .applicant1ClaimMediationSpecRequired(
                                                                  SmallClaimMedicalLRspec.builder()
                                                                      .hasAgreedFreeMediation(YES)
                                                                      .build()
                                                              )
                                                              .build());

            assertEquals(newState.getState().getName(), FULL_DEFENCE_PROCEED.fullName());
            assertEquals(Boolean.TRUE, newState.getFlags().get(FlowFlag.AGREED_TO_MEDIATION.name()));
        }
    }

    @Nested
    class ContactDetailsChange {
        @Test
        void shouldReturnContactDetailsChange_whenCaseDataAtStateRespondentContactDetailsChange() {
            // Given
            CaseData caseData = CaseDataBuilder.builder()
                .atStateRespondentFullDefenceAfterNotificationAcknowledgement()
                .atSpecAoSApplicantCorrespondenceAddressRequired(NO)
                .atSpecAoSApplicantCorrespondenceAddressDetails(AddressBuilder.defaults().build())
                .build().toBuilder()
                .caseAccessCategory(SPEC_CLAIM).build();

            // When
            StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

            // Then
            assertThat(stateFlow.getState())
                .extracting(State::getName)
                .isNotNull()
                .isEqualTo(CONTACT_DETAILS_CHANGE.fullName());
            assertThat(stateFlow.getStateHistory())
                .hasSize(6)
                .extracting(State::getName)
                .containsExactly(
                    SPEC_DRAFT.fullName(), CLAIM_SUBMITTED.fullName(), CLAIM_ISSUED_PAYMENT_SUCCESSFUL.fullName(),
                    PENDING_CLAIM_ISSUED.fullName(), CLAIM_ISSUED.fullName(), CONTACT_DETAILS_CHANGE.fullName()
                );

            assertThat(stateFlow.getFlags()).hasSize(5).contains(
                entry(FlowFlag.NOTICE_OF_CHANGE.name(), false),
                entry(FlowFlag.CONTACT_DETAILS_CHANGE.name(), true),
                entry(FlowFlag.GENERAL_APPLICATION_ENABLED.name(), false),
                entry(FlowFlag.CERTIFICATE_OF_SERVICE.name(), false),
                entry("ONE_RESPONDENT_REPRESENTATIVE", true)
            );
        }
    }

    @Nested
    class FromPartAdmit {
        @Test
        void partAdmitInMediationSpec() {
            // Given
            CaseData caseData = CaseData.builder()
                // spec claim
                .caseAccessCategory(SPEC_CLAIM)
                // claim submitted
                .submittedDate(LocalDateTime.now())
                .respondent1Represented(YES)
                // payment successful
                .paymentSuccessfulDate(LocalDateTime.now())
                // pending claim issued
                .issueDate(LocalDate.now())
                .respondent1OrgRegistered(YES)
                // claim issued
                .claimNotificationDeadline(LocalDateTime.now())
                // part admit
                .respondent1ResponseDate(LocalDateTime.now())
                .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION)
                .claimNotificationDate(LocalDateTime.now())
                .responseClaimTrack(AllocatedTrack.SMALL_CLAIM.name())
                .responseClaimMediationSpecRequired(YES)
                .applicant1PartAdmitConfirmAmountPaidSpec(NO)
                .caseDataLiP(CaseDataLiP.builder()
                                 .applicant1ClaimMediationSpecRequiredLip(
                                     ClaimantMediationLip.builder()
                                         .hasAgreedFreeMediation(MediationDecision.Yes)
                                         .build()).build())
                .build();

            // When
            StateFlow fullState = stateFlowEngine.evaluate(caseData);

            // Then
            assertEquals(IN_MEDIATION.fullName(), fullState.getState().getName());
        }

        @Test
        void partAdmitPartMediationSpec() {
            // Given
            CaseData caseData = CaseData.builder()
                // spec claim
                .caseAccessCategory(SPEC_CLAIM)
                // claim submitted
                .submittedDate(LocalDateTime.now())
                .respondent1Represented(YES)
                // payment successful
                .paymentSuccessfulDate(LocalDateTime.now())
                // pending claim issued
                .issueDate(LocalDate.now())
                .respondent1OrgRegistered(YES)
                // claim issued
                .claimNotificationDeadline(LocalDateTime.now())
                // part admit
                .respondent1ResponseDate(LocalDateTime.now())
                .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION)
                .claimNotificationDate(LocalDateTime.now())
                .responseClaimTrack(AllocatedTrack.SMALL_CLAIM.name())
                .responseClaimMediationSpecRequired(YES)
                .applicant1PartAdmitConfirmAmountPaidSpec(NO)
                .caseDataLiP(CaseDataLiP.builder()
                                 .applicant1ClaimMediationSpecRequiredLip(
                                     ClaimantMediationLip.builder()
                                         .hasAgreedFreeMediation(MediationDecision.No)
                                         .build()).build())
                .build();

            // When
            StateFlow fullState = stateFlowEngine.evaluate(caseData);

            // Then
            assertEquals(PART_ADMIT_NOT_SETTLED_NO_MEDIATION.fullName(), fullState.getState().getName());
        }

        @Test
        void partAdmitNoMediationSpec() {
            CaseData caseData = CaseData.builder()
                // spec claim
                .caseAccessCategory(SPEC_CLAIM)
                // claim submitted
                .submittedDate(LocalDateTime.now())
                .respondent1Represented(YES)
                // payment successful
                .paymentSuccessfulDate(LocalDateTime.now())
                // pending claim issued
                .issueDate(LocalDate.now())
                .respondent1OrgRegistered(YES)
                // claim issued
                .claimNotificationDeadline(LocalDateTime.now())
                // part admit
                .respondent1ResponseDate(LocalDateTime.now())
                .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION)
                .claimNotificationDate(LocalDateTime.now())
                .responseClaimTrack(AllocatedTrack.SMALL_CLAIM.name())
                .responseClaimMediationSpecRequired(NO)
                .applicant1PartAdmitConfirmAmountPaidSpec(NO)
                .build();

            // When
            StateFlow fullState = stateFlowEngine.evaluate(caseData);

            // Then
            assertEquals(PART_ADMIT_NOT_SETTLED_NO_MEDIATION.fullName(), fullState.getState().getName());
        }
    }

    @Nested
    class FromPartAdmitNotSettledNoMediation {
        @Test
        void partAdmitNoMediationSpec() {
            CaseData caseData = CaseData.builder()
                // spec claim
                .caseAccessCategory(SPEC_CLAIM)
                // claim submitted
                .submittedDate(LocalDateTime.now())
                .respondent1Represented(YES)
                // payment successful
                .paymentSuccessfulDate(LocalDateTime.now())
                // pending claim issued
                .issueDate(LocalDate.now())
                .respondent1OrgRegistered(YES)
                // claim issued
                .claimNotificationDeadline(LocalDateTime.now())
                // part admit
                .respondent1ResponseDate(LocalDateTime.now())
                .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION)
                .claimNotificationDate(LocalDateTime.now())
                .responseClaimTrack(AllocatedTrack.SMALL_CLAIM.name())
                .responseClaimMediationSpecRequired(NO)
                .applicant1PartAdmitConfirmAmountPaidSpec(NO)
                .reasonNotSuitableSDO(new ReasonNotSuitableSDO("test"))
                .ccdState(CaseState.PROCEEDS_IN_HERITAGE_SYSTEM)
                .build();

            // When
            StateFlow fullState = stateFlowEngine.evaluate(caseData);

            // Then
            assertEquals(TAKEN_OFFLINE_SDO_NOT_DRAWN.fullName(), fullState.getState().getName());
        }
    }

    @Test
    void shouldReturnInHearingReadiness_whenTransitionedFromCaseDetailsNotified() {
        // Given
        CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build().toBuilder()
            .hearingReferenceNumber("11111111")
            .listingOrRelisting(ListingOrRelisting.LISTING)
            .build();

        // When
        StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

        // Then
        assertThat(stateFlow.getState())
            .extracting(State::getName)
            .isNotNull()
            .isEqualTo(IN_HEARING_READINESS.fullName());
        assertThat(stateFlow.getStateHistory())
            .hasSize(8)
            .extracting(State::getName)
            .containsExactly(
                DRAFT.fullName(), CLAIM_SUBMITTED.fullName(), CLAIM_ISSUED_PAYMENT_SUCCESSFUL.fullName(),
                PENDING_CLAIM_ISSUED.fullName(), CLAIM_ISSUED.fullName(), CLAIM_NOTIFIED.fullName(),
                CLAIM_DETAILS_NOTIFIED.fullName(), IN_HEARING_READINESS.fullName()
            );

        assertThat(stateFlow.getFlags()).hasSize(4).contains(
            entry(FlowFlag.NOTICE_OF_CHANGE.name(), false),
            entry(FlowFlag.GENERAL_APPLICATION_ENABLED.name(), false),
            entry("ONE_RESPONDENT_REPRESENTATIVE", true),
            entry(FlowFlag.CERTIFICATE_OF_SERVICE.name(), false)
        );
    }

    @Test
    void shouldReturnInHearingReadiness_whenTransitionedFromFullDefenseProceed() {
        // Given
        CaseData caseData = CaseData.builder()
            .caseAccessCategory(SPEC_CLAIM)
            .submittedDate(LocalDateTime.now())
            .respondent1Represented(YES)
            .paymentSuccessfulDate(LocalDateTime.now())
            .issueDate(LocalDate.now())
            .respondent1OrgRegistered(YES)
            .claimNotificationDeadline(LocalDateTime.now())
            .respondent1ResponseDate(LocalDateTime.now())
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE)
            .claimNotificationDate(LocalDateTime.now())
            .responseClaimTrack(AllocatedTrack.SMALL_CLAIM.name())
            .responseClaimMediationSpecRequired(YES)
            .applicant1ProceedWithClaim(YES)
            .applicant1ClaimMediationSpecRequired(
                SmallClaimMedicalLRspec.builder()
                    .hasAgreedFreeMediation(NO)
                    .build())
            .hearingReferenceNumber("11111111")
            .listingOrRelisting(ListingOrRelisting.LISTING)
            .build();

        // When
        StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

        // Then
        assertThat(stateFlow.getState())
            .extracting(State::getName)
            .isNotNull()
            .isEqualTo(IN_HEARING_READINESS.fullName());
        assertThat(stateFlow.getStateHistory())
            .hasSize(8)
            .extracting(State::getName)
            .containsExactly(
                SPEC_DRAFT.fullName(), CLAIM_SUBMITTED.fullName(), CLAIM_ISSUED_PAYMENT_SUCCESSFUL.fullName(),
                PENDING_CLAIM_ISSUED.fullName(), CLAIM_ISSUED.fullName(),  FULL_DEFENCE.fullName(),
                FULL_DEFENCE_PROCEED.fullName(), IN_HEARING_READINESS.fullName()
            );

    }

}
