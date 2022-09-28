package uk.gov.hmcts.reform.civil.service.flowstate;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
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
import uk.gov.hmcts.reform.civil.enums.RespondentResponseType;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.enums.SuperClaimType;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.launchdarkly.FeatureToggleService;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.SmallClaimMedicalLRspec;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilderUnspec;
import uk.gov.hmcts.reform.civil.sampledata.CaseDetailsBuilder;
import uk.gov.hmcts.reform.civil.stateflow.StateFlow;
import uk.gov.hmcts.reform.civil.stateflow.model.State;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.divergentRespondGoOfflineSpec;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.specClaim;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.ALL_RESPONSES_RECEIVED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.AWAITING_RESPONSES_FULL_DEFENCE_RECEIVED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.AWAITING_RESPONSES_NOT_FULL_DEFENCE_RECEIVED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.CLAIM_DETAILS_NOTIFIED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.CLAIM_DETAILS_NOTIFIED_TIME_EXTENSION;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.CLAIM_DISMISSED_PAST_CLAIM_DETAILS_NOTIFICATION_DEADLINE;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.CLAIM_DISMISSED_PAST_CLAIM_DISMISSED_DEADLINE;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.CLAIM_DISMISSED_PAST_CLAIM_NOTIFICATION_DEADLINE;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.CLAIM_ISSUED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.CLAIM_ISSUED_PAYMENT_FAILED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.CLAIM_ISSUED_PAYMENT_SUCCESSFUL;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.CLAIM_NOTIFIED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.CLAIM_SUBMITTED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.COUNTER_CLAIM;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.DIVERGENT_RESPOND_GENERATE_DQ_GO_OFFLINE;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.DIVERGENT_RESPOND_GO_OFFLINE;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.DRAFT;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.FULL_ADMISSION;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.FULL_ADMIT_NOT_PROCEED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.FULL_ADMIT_PROCEED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.FULL_DEFENCE;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.FULL_DEFENCE_PROCEED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.NOTIFICATION_ACKNOWLEDGED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.NOTIFICATION_ACKNOWLEDGED_TIME_EXTENSION;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.PART_ADMISSION;
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
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.TAKEN_OFFLINE_BY_STAFF;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.TAKEN_OFFLINE_PAST_APPLICANT_RESPONSE_DEADLINE;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.TAKEN_OFFLINE_UNREGISTERED_DEFENDANT;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.TAKEN_OFFLINE_UNREPRESENTED_DEFENDANT;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.TAKEN_OFFLINE_UNREPRESENTED_UNREGISTERED_DEFENDANT;

@SpringBootTest(classes = {
    JacksonAutoConfiguration.class,
    CaseDetailsConverter.class,
    StateFlowEngine.class
})
class StateFlowEngineUnspecTest {

    @Autowired
    private StateFlowEngine stateFlowEngine;

    @MockBean
    private FeatureToggleService featureToggleService;

    @BeforeEach
    void setup() {
        when(featureToggleService.isRpaContinuousFeedEnabled()).thenReturn(true);
    }

    @Nested
    class EvaluateStateFlowEngine {

        /*
        Testing initialState -> CLAIM_SUBMITTED (CS1)
        claimSubmittedOneRespondentRepresentative -
        CASE 1: caseData.getRespondent1Represented() == YES  and caseData.getAddRespondent2() == NO
        */
        @Test
        void shouldReturnClaimSubmitted_whenCaseDataAtStateClaimSubmittedWithOneRespondentAndNoSecondRespondent() {
            CaseData caseData = CaseDataBuilderUnspec.builder().atStateClaimSubmitted()
                .respondent1Represented(YES)
                .addRespondent2(NO)
                .respondent2OrganisationPolicy(null)
                .respondent2Represented(NO)
                .respondent2OrgRegistered(NO)
                .respondent2SameLegalRepresentative(NO)
                .build();

            StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

            assertThat(stateFlow.getState())
                .extracting(State::getName)
                .isNotNull()
                .isEqualTo(CLAIM_SUBMITTED.fullName());
            assertThat(stateFlow.getStateHistory())
                .hasSize(2)
                .extracting(State::getName)
                .containsExactly(
                    DRAFT.fullName(), CLAIM_SUBMITTED.fullName());
            verify(featureToggleService).isRpaContinuousFeedEnabled();
            assertThat(stateFlow.getFlags()).hasSize(4).contains(
                entry(FlowFlag.SPEC_RPA_CONTINUOUS_FEED.name(), false),
                entry(FlowFlag.NOTICE_OF_CHANGE.name(), false),
                entry("ONE_RESPONDENT_REPRESENTATIVE", true),
                entry("RPA_CONTINUOUS_FEED", true)
            );
        }

        /*
           Testing initialState -> CLAIM_SUBMITTED for claimSubmittedOneRespondentRepresentative (CS1)
           CASE 2: caseData.getRespondent1Represented() == YES
                   caseData.getAddRespondent2() == YES && caseData.getRespondent2SameLegalRepresentative() == YES
       */
        @Test
        void shouldReturnClaimSubmitted_Respondent1RepresentedAndSecondRespondentHasSameLegalRepresentative() {
            CaseData caseData = CaseDataBuilderUnspec.builder().atStateClaimSubmitted()
                .respondent1Represented(YES)
                .addRespondent2(YES)
                .respondent2SameLegalRepresentative(YES)
                .build();

            StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

            assertThat(stateFlow.getState())
                .extracting(State::getName)
                .isNotNull()
                .isEqualTo(CLAIM_SUBMITTED.fullName());
            assertThat(stateFlow.getStateHistory())
                .hasSize(2)
                .extracting(State::getName)
                .containsExactly(
                    DRAFT.fullName(), CLAIM_SUBMITTED.fullName());
            verify(featureToggleService).isRpaContinuousFeedEnabled();
            assertThat(stateFlow.getFlags()).hasSize(4).contains(
                entry(FlowFlag.SPEC_RPA_CONTINUOUS_FEED.name(), false),
                entry(FlowFlag.NOTICE_OF_CHANGE.name(), false),
                entry("ONE_RESPONDENT_REPRESENTATIVE", true),
                entry("RPA_CONTINUOUS_FEED", true)
            );
        }

        /*
          Testing initialState -> CLAIM_SUBMITTED for claimSubmittedOneRespondentRepresentative (CS1)
          CASE 3: caseData.getRespondent1Represented() == NO
                   caseData.getAddRespondent2() == NO
      */
        @Test
        void shouldReturnClaimSubmitted_Respondent1NotRepresentedAndSecondRespondentNotPresent() {
            CaseData caseData = CaseDataBuilderUnspec.builder().atStateClaimSubmitted()
                .respondent1Represented(NO)
                .addRespondent2(NO)
                .build();

            StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

            assertThat(stateFlow.getState())
                .extracting(State::getName)
                .isNotNull()
                .isEqualTo(CLAIM_SUBMITTED.fullName());
            assertThat(stateFlow.getStateHistory())
                .hasSize(2)
                .extracting(State::getName)
                .containsExactly(
                    DRAFT.fullName(), CLAIM_SUBMITTED.fullName());
            verify(featureToggleService).isRpaContinuousFeedEnabled();
            assertThat(stateFlow.getFlags()).hasSize(4).contains(
                entry(FlowFlag.SPEC_RPA_CONTINUOUS_FEED.name(), false),
                entry(FlowFlag.NOTICE_OF_CHANGE.name(), false),
                entry("ONE_RESPONDENT_REPRESENTATIVE", true),
                entry("RPA_CONTINUOUS_FEED", true)
            );
        }

        /*
          Testing initialState -> CLAIM_SUBMITTED for claimSubmittedTwoRegisteredRespondentRepresentatives (CS2)
          Case 1:  caseData.getAddRespondent2() == YES
                   && caseData.getRespondent2SameLegalRepresentative() == NO
                   && caseData.getRespondent1Represented() == YES
                   && caseData.getRespondent2Represented() == YES
                   && caseData.getRespondent1OrgRegistered() == YES
                   && caseData.getRespondent2OrgRegistered() == YES
       */
        @Test
        void shouldReturnClaimSubmitted_whenCaseDataAtStateClaimSubmittedTwoRespondentRepresentatives() {
            CaseData caseData = CaseDataBuilderUnspec.builder()
                .atStateClaimSubmittedTwoRespondentRepresentativesDiffLegalRepresentative()
                .build();

            if (caseData.getRespondent2OrgRegistered() != null
                && caseData.getRespondent2Represented() == null) {
                caseData = caseData.toBuilder()
                    .respondent2Represented(YES)
                    .build();
            }
            StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

            assertThat(stateFlow.getState())
                .extracting(State::getName)
                .isNotNull()
                .isEqualTo(CLAIM_SUBMITTED.fullName());
            assertThat(stateFlow.getStateHistory())
                .hasSize(2)
                .extracting(State::getName)
                .containsExactly(
                    DRAFT.fullName(), CLAIM_SUBMITTED.fullName());
            verify(featureToggleService).isRpaContinuousFeedEnabled();

            assertThat(stateFlow.getFlags()).hasSize(5).contains(
                entry("ONE_RESPONDENT_REPRESENTATIVE", false),
                entry("TWO_RESPONDENT_REPRESENTATIVES", true),
                entry("RPA_CONTINUOUS_FEED", true),
                entry(FlowFlag.SPEC_RPA_CONTINUOUS_FEED.name(), false),
                entry(FlowFlag.NOTICE_OF_CHANGE.name(), false)
            );
        }

        /*
        Testing initialState -> CLAIM_SUBMITTED for claimSubmittedTwoRespondentRepresentativesOneUnregistered
        2 respondents present but different legal representative
        and respondent 1 is unregistered(CS2)
        Case 3:  caseData.getAddRespondent2() == YES
                   && caseData.getRespondent2SameLegalRepresentative() == NO
                   && caseData.getRespondent1Represented() == YES
                   && caseData.getRespondent2Represented() == YES
                   && caseData.getRespondent1OrgRegistered() == NO
                   && caseData.getRespondent2OrgRegistered() == YES
      */
        @Test
        void shouldReturnClaimSubmitted_whenCaseDataAtStateClaimSubmittedTwoRepresentativesOneUnregistered() {
            CaseData caseData = CaseDataBuilderUnspec.builder()
                .atStateClaimSubmittedTwoRespondentRepresentativesDiffLegalRepresentative()
                .respondent2Represented(YES)
                .respondent2OrgRegistered(YES)
                .respondent1OrgRegistered(NO)
                .build();

            StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

            assertThat(stateFlow.getState())
                .extracting(State::getName)
                .isNotNull()
                .isEqualTo(CLAIM_SUBMITTED.fullName());
            assertThat(stateFlow.getStateHistory())
                .hasSize(2)
                .extracting(State::getName)
                .containsExactly(
                    DRAFT.fullName(), CLAIM_SUBMITTED.fullName());
            verify(featureToggleService).isRpaContinuousFeedEnabled();

            assertThat(stateFlow.getFlags()).hasSize(5).contains(
                entry("ONE_RESPONDENT_REPRESENTATIVE", false),
                entry("TWO_RESPONDENT_REPRESENTATIVES", true),
                entry("RPA_CONTINUOUS_FEED", true),
                entry(FlowFlag.SPEC_RPA_CONTINUOUS_FEED.name(), false),
                entry(FlowFlag.NOTICE_OF_CHANGE.name(), false)
            );
        }

        /*
       Testing initialState -> CLAIM_SUBMITTED for claimSubmittedTwoRespondentRepresentativesOneUnregistered
       2 respondents present but different legal representative
       and respondent 2 is unregistered(CS2)
       Case 3:  caseData.getAddRespondent2() == YES
                   && caseData.getRespondent2SameLegalRepresentative() == NO
                   && caseData.getRespondent1Represented() == YES
                   && caseData.getRespondent2Represented() == YES
                   && caseData.getRespondent1OrgRegistered() == YES
                   && caseData.getRespondent2OrgRegistered() == NO
       */
        @Test
        void shouldReturnClaimSubmitted_whenCaseDataAtStateClaimSubmitted2RespondentRepresentativesSecondUnreg() {
            CaseData caseData = CaseDataBuilderUnspec.builder()
                .atStateClaimSubmittedTwoRespondentRepresentativesDiffLegalRepresentative()
                .respondent2Represented(YES)
                .respondent2OrgRegistered(YES)
                .respondent1OrgRegistered(YES)
                .respondent2OrgRegistered(NO)
                .build();

            StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

            assertThat(stateFlow.getState())
                .extracting(State::getName)
                .isNotNull()
                .isEqualTo(CLAIM_SUBMITTED.fullName());
            assertThat(stateFlow.getStateHistory())
                .hasSize(2)
                .extracting(State::getName)
                .containsExactly(
                    DRAFT.fullName(), CLAIM_SUBMITTED.fullName());
            verify(featureToggleService).isRpaContinuousFeedEnabled();

            assertThat(stateFlow.getFlags()).hasSize(5).contains(
                entry("ONE_RESPONDENT_REPRESENTATIVE", false),
                entry("TWO_RESPONDENT_REPRESENTATIVES", true),
                entry("RPA_CONTINUOUS_FEED", true),
                entry(FlowFlag.SPEC_RPA_CONTINUOUS_FEED.name(), false),
                entry(FlowFlag.NOTICE_OF_CHANGE.name(), false)
            );
        }

        /*
        Testing initialState -> CLAIM_SUBMITTED for claimSubmittedBothRespondentUnrepresented
        Both the respondents are unrepresented (CS3)
         Case 1: caseData.getAddLegalRepDeadline() == null &&
                 && caseData.getAddRespondent2() == YES
                 && caseData.getRespondent1Represented() == NO
                 && caseData.getRespondent2Represented() == NO
        */
        @Test
        void shouldReturnClaimSubmitted_whenCaseDataAtStateClaimSubmittedBothRespondentUnrepresented() {
            CaseData caseData = CaseDataBuilderUnspec.builder().atStateClaimSubmittedNoRespondentRepresented().build();

            StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

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

        /*
        Testing initialState -> CLAIM_SUBMITTED for claimSubmittedOnlyOneRespondentRepresented
         Only first respondent is present (CS3)
          Case 2: caseData.getAddLegalRepDeadline() == null &&
                 && caseData.getAddRespondent2() == YES
                 && caseData.getRespondent1Represented() == YES
                 && caseData.getRespondent2Represented() == NO
        */
        @Test
        void shouldReturnClaimSubmitted_whenCaseDataAtStateClaimSubmittedOnlyFirstRespondentRepresented() {
            CaseData caseData = CaseDataBuilderUnspec.builder()
                .atStateClaimSubmitted1v2AndOnlyFirstRespondentIsRepresented()
                .build();

            if (caseData.getRespondent2OrgRegistered() != null
                && caseData.getRespondent2Represented() == null) {
                caseData = caseData.toBuilder()
                    .respondent2Represented(YES)
                    .build();
            }
            StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

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

        /*
        Testing initialState -> CLAIM_SUBMITTED for claimSubmittedOnlyOneRespondentRepresented
        Only second respondent is present (CS3)
        Case 3: caseData.getAddLegalRepDeadline() == null &&
                 && caseData.getAddRespondent2() == YES
                 && caseData.getRespondent1Represented() == NO
                 && caseData.getRespondent2Represented() == YES
        */
        @Test
        void shouldReturnClaimSubmitted_whenCaseDataAtStateClaimSubmittedOnlySecondRespondentRepresented() {
            CaseData caseData = CaseDataBuilderUnspec.builder()
                .atStateClaimSubmitted1v2AndOnlySecondRespondentIsRepresented()
                .build();
            if (caseData.getRespondent2OrgRegistered() != null
                && caseData.getRespondent2Represented() == null) {
                caseData = caseData.toBuilder()
                    .respondent2Represented(YES)
                    .build();
            }
            StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

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

        /*
        Testing initialState -> CLAIM_SUBMITTED for claimSubmittedBothUnregisteredSolicitors
        Both respondents unregistered (CS3)
        Case 4:  caseData.getAddLegalRepDeadline() == null &&
                 && caseData.getAddRespondent2() == YES
                 && caseData.getRespondent1Represented() == NO
                 && caseData.getRespondent2Represented() == NO
        */
        @Test
        void shouldReturnClaimSubmitted_whenCaseDataAtStateClaimBothUnregisteredSolicitors() {
            CaseData caseData = CaseDataBuilderUnspec.builder()
                .atStateClaimSubmitted()
                .addRespondent2(YES)
                .respondent1OrgRegistered(NO)
                .build();

            if (caseData.getRespondent2OrgRegistered() != null
                && caseData.getRespondent2Represented() == null) {
                caseData = caseData.toBuilder()
                    .respondent2Represented(YES)
                    .build();
            }
            StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

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

        /*
       Testing initialState -> CLAIM_SUBMITTED for claimSubmittedOneUnrepresentedDefendantOnly
        Only one unrepresented respondent (CS3 - 4)
        Case 4:  caseData.getAddLegalRepDeadline() == null
                 && caseData.getRespondent1Represented() == NO
       */
        @Test
        void shouldReturnClaimSubmitted_whenCaseDataAtStateClaimOneUnrepresentedDefendantOnly() {
            CaseData caseData = CaseDataBuilderUnspec.builder()
                .atStateClaimSubmitted()
                .respondent1Represented(NO)
                .addRespondent2(NO)
                .build();

            if (caseData.getRespondent2OrgRegistered() != null
                && caseData.getRespondent2Represented() == null) {
                caseData = caseData.toBuilder()
                    .respondent2Represented(YES)
                    .build();
            }
            StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

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

        /*
           Testing initialState -> CLAIM_SUBMITTED for combination(CS4)
           noticeOfChangeEnabledAndLiP && claimSubmittedRespondent1Unrepresented
           Case 1: caseData.getAddLegalRepDeadline() != null
                   && caseData.getRespondent1Represented() == NO
       */
        @Test
        void shouldReturnClaimSubmitted_whenCaseDataAtStateClaimSubmittedRes1UnrepresentedWithLegalRepDeadline() {
            CaseData caseData = CaseDataBuilderUnspec.builder()
                .atStateClaimSubmitted()
                .addLegalRepDeadline(LocalDateTime.now())
                .respondent1Represented(NO)
                .addRespondent2(NO)
                .build();

            StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

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

        /*
           Testing initialState -> CLAIM_SUBMITTED for combination
           noticeOfChangeEnabledAndLiP && claimSubmittedRespondent1Unrepresented
           AddLegalRepDeadline is not null (CS4)
           Case 2: caseData.getAddLegalRepDeadline() == null
                   && caseData.getRespondent1Represented() == NO
           What will happen when respondent1 is represented
       */
        @Test
        void shouldReturnClaimSubmitted_whenCaseDataAtStateClaimSubmittedRes1RepresentedWithLegalRepDeadline() {
            CaseData caseData = CaseDataBuilderUnspec.builder()
                .atStateClaimSubmitted()
                .addLegalRepDeadline(null)
                .respondent1Represented(YES)
                .addRespondent2(NO)
                .build();

            StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

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

        /*
           Testing initialState -> CLAIM_SUBMITTED for combination  (CS5)
           noticeOfChangeEnabledAndLiP
           .and(claimSubmittedRespondent1Unrepresented) caseData.getRespondent1Represented() == NO
           .and(claimSubmittedOneUnrepresentedDefendantOnly.negate())  !(caseData.getRespondent1Represented() == NO
                                                                       caseData.getAddRespondent2() == NO)
           .and(claimSubmittedRespondent2Unrepresented.negate())  !caseData.getAddRespondent2() == YES
                                                                   caseData.getRespondent2Represented() == NO;
           Case 1: All true
                    caseData.getRespondent1Represented() == NO
                    caseData.getAddRespondent2() == yes
                    caseData.getRespondent2Represented() == yes
       */
        @Test
        void shouldReturnClaimSubmitted_whenCaseDataAtStateClaimSubmittedCS5_1() {
            CaseData caseData = CaseDataBuilderUnspec.builder()
                .atStateClaimSubmitted()
                .addLegalRepDeadline(LocalDateTime.now())
                .respondent1Represented(NO)
                .addRespondent2(YES)
                .respondent2Represented(NO)
                .build();

            StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

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
        /*
         Testing initialState -> CLAIM_SUBMITTED for combination  (CS5)
           noticeOfChangeEnabledAndLiP
           .and(claimSubmittedRespondent1Unrepresented) caseData.getRespondent1Represented() == NO
           .and(claimSubmittedOneUnrepresentedDefendantOnly.negate())  !(caseData.getRespondent1Represented() == NO
                                                                       caseData.getAddRespondent2() == NO)
           .and(claimSubmittedRespondent2Unrepresented.negate())  !caseData.getAddRespondent2() == YES
                                                                   caseData.getRespondent2Represented() == NO;
         Case 2: caseData.getRespondent1Represented() == Yes

         */
        @Test
        void shouldReturnClaimSubmitted_whenCaseDataAtStateClaimSubmittedCS5_2() {
            CaseData caseData = CaseDataBuilderUnspec.builder()
                .atStateClaimSubmitted()
                .addLegalRepDeadline(LocalDateTime.now())
                .respondent1Represented(YES)
                .addRespondent2(YES)
                .respondent2Represented(NO)
                .build();

            StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

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
        /*
        Testing initialState -> CLAIM_SUBMITTED for combination  (CS5)
           noticeOfChangeEnabledAndLiP
           .and(claimSubmittedRespondent1Unrepresented) caseData.getRespondent1Represented() == NO
           .and(claimSubmittedOneUnrepresentedDefendantOnly.negate())  !(caseData.getRespondent1Represented() == NO
                                                                       caseData.getAddRespondent2() == NO)
           .and(claimSubmittedRespondent2Unrepresented.negate())  !caseData.getAddRespondent2() == YES
                                                                   caseData.getRespondent2Represented() == NO;
         Case 3: caseData.getRespondent1Represented() == YES
                   caseData.getAddRespondent2() == NO

         */
        @Test
        void shouldReturnClaimSubmitted_whenCaseDataAtStateClaimSubmittedCS5_3() {
            CaseData caseData = CaseDataBuilderUnspec.builder()
                .atStateClaimSubmitted()
                .addLegalRepDeadline(LocalDateTime.now())
                .respondent1Represented(NO)
                .addRespondent2(YES)
                .build();

            StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

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
        /*
           Testing initialState -> CLAIM_SUBMITTED for combination  (CS5)
           noticeOfChangeEnabledAndLiP
           .and(claimSubmittedRespondent1Unrepresented) caseData.getRespondent1Represented() == NO
           .and(claimSubmittedOneUnrepresentedDefendantOnly.negate())  !(caseData.getRespondent1Represented() == NO
                                                                       caseData.getAddRespondent2() == NO)
           .and(claimSubmittedRespondent2Unrepresented.negate())  !caseData.getAddRespondent2() == YES
                                                                   caseData.getRespondent2Represented() == NO;
           Case 4: All false
                    caseData.getRespondent1Represented() == YES - false

                    caseData.getRespondent1Represented() == YES|false
                    caseData.getAddRespondent2() == YES         |

                    caseData.getAddRespondent2() == YES         |
                    caseData.getRespondent2Represented() == YES |false
         */
        @Test
        void shouldReturnClaimSubmitted_whenCaseDataAtStateClaimSubmittedCS5_4() {

            CaseData caseData = CaseDataBuilderUnspec.builder()
                .atStateClaimSubmitted()
                .addLegalRepDeadline(LocalDateTime.now())
                //.addRespondent2(YES)
                .respondent1Represented(YES)
                .respondent2Represented(YES)
                .build();

            StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

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

        /*
           Testing initialState -> CLAIM_SUBMITTED for combination   (CS6)
           .onlyIf(noticeOfChangeEnabledAndLiP - caseData.getAddLegalRepDeadline() != null
           .and(claimSubmittedRespondent2Unrepresented -  && caseData.getAddRespondent2() == YES
                                                          && caseData.getRespondent2Represented() == NO
           .and(claimSubmittedRespondent1Unrepresented.negate() - !caseData.getRespondent1Represented() == NO

           Case 1: All ok
                   caseData.getAddLegalRepDeadline() != null
                   caseData.getAddRespondent2() == YES && caseData.getRespondent2Represented() == NO
                   caseData.getRespondent1Represented() == YES
       */
        @Test
        void shouldReturnClaimSubmitted_whenCaseDataAtStateClaimSubmittedCS6_1() {
            CaseData caseData = CaseDataBuilderUnspec.builder()
                .atStateClaimSubmitted()
                .addLegalRepDeadline(LocalDateTime.now())
                .respondent1Represented(YES)
                .addRespondent2(YES)
                .respondent2Represented(YES)
                .build();

            StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

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

        /*
           Testing initialState -> CLAIM_SUBMITTED for combination   (CS6)
           .onlyIf(noticeOfChangeEnabledAndLiP - caseData.getAddLegalRepDeadline() != null
           .and(claimSubmittedRespondent2Unrepresented -  && caseData.getAddRespondent2() == YES
                                                          && caseData.getRespondent2Represented() == NO
           .and(claimSubmittedRespondent1Unrepresented.negate() - !caseData.getRespondent1Represented() == NO
           Case 2: caseData.getAddLegalRepDeadline() != null
                   caseData.getAddRespondent2() == No && caseData.getRespondent1Represented() == YES
       */
        @Test
        void shouldReturnClaimSubmitted_whenCaseDataAtStateClaimSubmittedCS6_2() {
            CaseData caseData = CaseDataBuilderUnspec.builder()
                .atStateClaimSubmitted()
                .addLegalRepDeadline(LocalDateTime.now())
                .respondent1Represented(YES)
                .addRespondent2(NO)
                .build();

            StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

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

        /*
           Testing initialState -> CLAIM_SUBMITTED for combination   (CS6)
           .onlyIf(noticeOfChangeEnabledAndLiP - caseData.getAddLegalRepDeadline() != null
           .and(claimSubmittedRespondent2Unrepresented -  && caseData.getAddRespondent2() == YES
                                                          && caseData.getRespondent2Represented() == NO
           .and(claimSubmittedRespondent1Unrepresented.negate() - !caseData.getRespondent1Represented() == NO
           Case 3: caseData.getAddLegalRepDeadline() != null
                   caseData.getAddRespondent2() == YES && caseData.getRespondent2Represented() == YES
                   caseData.getRespondent1Represented() == YES
       */
        @Test
        void shouldReturnClaimSubmitted_whenCaseDataAtStateClaimSubmittedCS6_3() {
            CaseData caseData = CaseDataBuilderUnspec.builder()
                .atStateClaimSubmitted()
                .addLegalRepDeadline(LocalDateTime.now())
                .respondent1Represented(YES)
                .addRespondent2(YES)
                .respondent2Represented(YES)
                .build();

            StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

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

        /*
           Testing initialState -> CLAIM_SUBMITTED for combination   (CS6)
           .onlyIf(noticeOfChangeEnabledAndLiP - caseData.getAddLegalRepDeadline() != null
           .and(claimSubmittedRespondent2Unrepresented -  && caseData.getAddRespondent2() == YES
                                                          && caseData.getRespondent2Represented() == NO
           .and(claimSubmittedRespondent1Unrepresented.negate() - !caseData.getRespondent1Represented() == NO

          Case 4: caseData.getAddLegalRepDeadline() != null
                   caseData.getAddRespondent2() == YES && caseData.getRespondent2Represented() == NO
                   caseData.getRespondent1Represented() == NO
       */
        @Test
        void shouldReturnClaimSubmitted_whenCaseDataAtStateClaimSubmittedCS6_4() {
            CaseData caseData = CaseDataBuilderUnspec.builder()
                .atStateClaimSubmitted()
                .addLegalRepDeadline(LocalDateTime.now())
                .respondent1Represented(NO)
                .addRespondent2(YES)
                .respondent2Represented(NO)
                .build();

            StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

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

        /*
           Testing initialState -> CLAIM_SUBMITTED for combination   (CS6)
           .onlyIf(noticeOfChangeEnabledAndLiP - caseData.getAddLegalRepDeadline() != null
           .and(claimSubmittedRespondent2Unrepresented -  && caseData.getAddRespondent2() == YES
                                                          && caseData.getRespondent2Represented() == NO
           .and(claimSubmittedRespondent1Unrepresented.negate() - caseData.getRespondent1Represented() == YES
           Case 5: caseData.getAddLegalRepDeadline() == null
                   caseData.getAddRespondent2() == YES && caseData.getRespondent2Represented() == YES
                   caseData.getRespondent1Represented() == NO
       */
        @Test
        void shouldReturnClaimSubmitted_whenCaseDataAtStateClaimSubmittedCS6_5() {
            CaseData caseData = CaseDataBuilderUnspec.builder()
                .atStateClaimSubmitted()
                .addLegalRepDeadline(null)
                .respondent1Represented(NO)
                .addRespondent2(YES)
                .respondent2Represented(YES)
                .build();

            StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

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

        /*
          Testing initialState -> CLAIM_SUBMITTED for combination   (CS7)
              noticeOfChangeEnabledAndLiP - caseData.getAddLegalRepDeadline() != null
              claimSubmittedRespondent1Unrepresented - caseData.getRespondent1Represented() == NO
              claimSubmittedRespondent2Unrepresented - caseData.getAddRespondent2() == YES
                                                      && caseData.getRespondent2Represented() == NO
          Case 1: All ok
                  caseData.getAddLegalRepDeadline() != null
                  caseData.getRespondent1Represented() == NO
                  caseData.getAddRespondent2() == YES && caseData.getRespondent2Represented() == NO

      */
        @Test
        void shouldReturnClaimSubmitted_whenCaseDataAtStateClaimSubmittedCS7_1() {
            CaseData caseData = CaseDataBuilderUnspec.builder()
                .atStateClaimSubmitted()
                .addLegalRepDeadline(LocalDateTime.now())
                .respondent1Represented(NO)
                .addRespondent2(YES)
                .respondent2Represented(NO)
                .build();

            StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

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
        /*
          Testing initialState -> CLAIM_SUBMITTED for combination   (CS7)
              noticeOfChangeEnabledAndLiP - caseData.getAddLegalRepDeadline() != null
              claimSubmittedRespondent1Unrepresented - caseData.getRespondent1Represented() == NO
              claimSubmittedRespondent2Unrepresented - caseData.getAddRespondent2() == YES
                                                      && caseData.getRespondent2Represented() == NO
          Case 2: caseData.getAddLegalRepDeadline() != null
                  caseData.getRespondent1Represented() == YES
                  caseData.getAddRespondent2() == YES && caseData.getRespondent2Represented() == NO

      */
        @Test
        void shouldReturnClaimSubmitted_whenCaseDataAtStateClaimSubmittedCS7_2() {
            CaseData caseData = CaseDataBuilderUnspec.builder()
                .atStateClaimSubmitted()
                .addLegalRepDeadline(LocalDateTime.now())
                .respondent1Represented(YES)
                .addRespondent2(YES)
                .respondent2Represented(NO)
                .build();

            StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

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
        /*
          Testing initialState -> CLAIM_SUBMITTED for combination   (CS7)
              noticeOfChangeEnabledAndLiP - caseData.getAddLegalRepDeadline() != null
              claimSubmittedRespondent1Unrepresented - caseData.getRespondent1Represented() == NO
              claimSubmittedRespondent2Unrepresented - caseData.getAddRespondent2() == YES
                                                      && caseData.getRespondent2Represented() == NO
         Case 3: caseData.getAddLegalRepDeadline() != null
                  caseData.getRespondent1Represented() == NO
                  caseData.getAddRespondent2() == YES && caseData.getRespondent2Represented() == YES

      */
        @Test
        void shouldReturnClaimSubmitted_whenCaseDataAtStateClaimSubmittedCS7_3() {
            CaseData caseData = CaseDataBuilderUnspec.builder()
                .atStateClaimSubmitted()
                .addLegalRepDeadline(LocalDateTime.now())
                .respondent1Represented(NO)
                .addRespondent2(YES)
                .respondent2Represented(YES)
                .build();

            StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

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

        /*
          Testing initialState -> CLAIM_SUBMITTED for combination   (CS7)
              noticeOfChangeEnabledAndLiP - caseData.getAddLegalRepDeadline() != null
              claimSubmittedRespondent1Unrepresented - caseData.getRespondent1Represented() == NO
              claimSubmittedRespondent2Unrepresented - caseData.getAddRespondent2() == YES
                                                      && caseData.getRespondent2Represented() == NO
         Case 4: caseData.getAddLegalRepDeadline() == null
                  caseData.getRespondent1Represented() == YES
                  caseData.getAddRespondent2() == YES && caseData.getRespondent2Represented() == YES
      */
        @Test
        void shouldReturnClaimSubmitted_whenCaseDataAtStateClaimSubmittedCS7_4() {
            CaseData caseData = CaseDataBuilderUnspec.builder()
                .atStateClaimSubmitted()
                .addLegalRepDeadline(null)
                .respondent1Represented(YES)
                .addRespondent2(YES)
                .respondent2Represented(YES)
                .build();

            StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

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
        void shouldReturnClaimSubmitted_whenCaseDataAtStateClaimSubmittedCS4() {
            CaseData caseData = CaseDataBuilderUnspec.builder()
                .atStateClaimSubmitted()
                .addLegalRepDeadline(LocalDateTime.now())
                .respondent1Represented(NO)
                .addRespondent2(NO)
                .build();

            StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

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
            CaseData caseData = CaseDataBuilderUnspec.builder()
                .atStateClaimSubmitted2v1RespondentUnrepresented()
                .build();

            StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

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
            CaseData caseData = CaseDataBuilderUnspec.builder()
                .atStateClaimSubmitted2v1RespondentUnregistered()
                .build();

            StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

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
            CaseData caseData = CaseDataBuilderUnspec.builder()
                .atStateClaimSubmitted2v1RespondentRegistered()
                .build();

            StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

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
                CaseData caseData = CaseDataBuilderUnspec.builder().atStateClaimIssued1v1UnrepresentedDefendant().build();

                StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

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
                    entry("RPA_CONTINUOUS_FEED", true),
                    entry(FlowFlag.NOTICE_OF_CHANGE.name(), false),
                    entry(FlowFlag.SPEC_RPA_CONTINUOUS_FEED.name(), false)
                );
            }

            // 1v2
            // Unrepresented
            // 1. Both def1 and def2 unrepresented
            @Test
            void shouldReturnProceedsWithOfflineJourney_whenCaseDataAtStateClaimDraftIssuedRespondentsNotRepresented() {
                CaseData caseData = CaseDataBuilderUnspec.builder().atStateClaimIssuedUnrepresentedDefendants().build();

                StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

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
                    entry("RPA_CONTINUOUS_FEED", true),
                    entry(FlowFlag.NOTICE_OF_CHANGE.name(), false),
                    entry(FlowFlag.SPEC_RPA_CONTINUOUS_FEED.name(), false)
                );
            }

            // Unrepresented
            // 2. Def1 unrepresented, Def2 registered
            @Test
            void shouldReturnProceedsWithOfflineJourney_whenCaseDataAtStateClaimDraftIssuedRespondent1NotRepresented() {
                CaseData caseData = CaseDataBuilderUnspec.builder().atStateClaimIssuedUnrepresentedDefendant1().build();

                StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

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
                    entry("RPA_CONTINUOUS_FEED", true),
                    entry(FlowFlag.NOTICE_OF_CHANGE.name(), false),
                    entry(FlowFlag.SPEC_RPA_CONTINUOUS_FEED.name(), false)
                );
            }

            // Unrepresented
            // 3. Def1 registered, Def 2 unrepresented
            @Test
            void shouldReturnProceedsWithOfflineJourney_whenCaseDataAtStateClaimDraftIssuedRespondent2NotRepresented() {
                CaseData caseData = CaseDataBuilderUnspec.builder().atStateClaimIssuedUnrepresentedDefendant2().build();

                StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

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
                    entry("RPA_CONTINUOUS_FEED", true),
                    entry(FlowFlag.NOTICE_OF_CHANGE.name(), false),
                    entry(FlowFlag.SPEC_RPA_CONTINUOUS_FEED.name(), false)
                );
            }
        }

        @Nested
        class UnrepresentedDefendant {
            @Test
            void shouldGoOffline_whenDeadlinePassed() {
                CaseData caseData = CaseDataBuilderUnspec.builder()
                    .atStateClaimIssuedUnrepresentedDefendants()
                    .addLegalRepDeadline(LocalDateTime.now().minusHours(4))
                    .build();

                StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

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
                verify(featureToggleService).isRpaContinuousFeedEnabled();
                assertThat(stateFlow.getFlags()).hasSize(5).contains(
                    entry(FlowFlag.UNREPRESENTED_DEFENDANT_ONE.name(), true),
                    entry(FlowFlag.UNREPRESENTED_DEFENDANT_TWO.name(), true),
                    entry(FlowFlag.RPA_CONTINUOUS_FEED.name(), true),
                    entry(FlowFlag.NOTICE_OF_CHANGE.name(), false),
                    entry(FlowFlag.SPEC_RPA_CONTINUOUS_FEED.name(), false)
                );
            }

            // 1v1
            // Unrepresented
            @Test
            void shouldContinueOnline_1v1_whenDefendantIsUnrepresented() {
                CaseData caseData = CaseDataBuilderUnspec.builder()
                    .atStateClaimIssued1v1UnrepresentedDefendant()
                    .addLegalRepDeadline(LocalDateTime.now().plusDays(14))
                    .build();
                StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

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
                verify(featureToggleService).isRpaContinuousFeedEnabled();
                assertThat(stateFlow.getFlags()).hasSize(4).contains(
                    entry(FlowFlag.UNREPRESENTED_DEFENDANT_ONE.name(), true),
                    entry(FlowFlag.RPA_CONTINUOUS_FEED.name(), true),
                    entry(FlowFlag.NOTICE_OF_CHANGE.name(), false),
                    entry(FlowFlag.SPEC_RPA_CONTINUOUS_FEED.name(), false)
                );
            }

            // 1v1 spec
            // Unrepresented
            @Test
            void shouldContinueOnline_1v1Spec_whenDefendantIsUnrepresented() {
                CaseData caseData = CaseDataBuilderUnspec.builder()
                    .atStateClaimIssued1v1UnrepresentedDefendant()
                    .addLegalRepDeadline(LocalDateTime.now().plusDays(14))
                    .build().toBuilder()
                    .superClaimType(SuperClaimType.SPEC_CLAIM).build();
                StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

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
                verify(featureToggleService).isRpaContinuousFeedEnabled();
                assertThat(stateFlow.getFlags()).hasSize(4).contains(
                    entry(FlowFlag.UNREPRESENTED_DEFENDANT_ONE.name(), true),
                    entry(FlowFlag.RPA_CONTINUOUS_FEED.name(), true),
                    entry(FlowFlag.NOTICE_OF_CHANGE.name(), false),
                    entry(FlowFlag.SPEC_RPA_CONTINUOUS_FEED.name(), false)
                );
            }

            // 1v2
            // Unrepresented
            // 1. Both def1 and def2 unrepresented
            @Test
            void shouldContinueOnline_WhenBothDefendantsAreUnrepresented() {
                CaseData caseData = CaseDataBuilderUnspec.builder()
                    .atStateClaimIssuedUnrepresentedDefendants()
                    .addLegalRepDeadline(LocalDateTime.now().plusDays(14))
                    .build();
                StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

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
                verify(featureToggleService).isRpaContinuousFeedEnabled();
                assertThat(stateFlow.getFlags()).hasSize(5).contains(
                    entry(FlowFlag.UNREPRESENTED_DEFENDANT_ONE.name(), true),
                    entry(FlowFlag.UNREPRESENTED_DEFENDANT_TWO.name(), true),
                    entry(FlowFlag.RPA_CONTINUOUS_FEED.name(), true),
                    entry(FlowFlag.NOTICE_OF_CHANGE.name(), false),
                    entry(FlowFlag.SPEC_RPA_CONTINUOUS_FEED.name(), false)
                );
            }

            // Unrepresented
            // 2. Def1 unrepresented, Def2 registered
            @Test
            void shouldContinueOnline_WhenCaseDataAtStateClaimDraftIssuedAndRespondent1NotRepresented() {
                CaseData caseData = CaseDataBuilderUnspec.builder()
                    .atStateClaimIssuedUnrepresentedDefendant1()
                    .addLegalRepDeadline(LocalDateTime.now().plusDays(14))
                    .build();

                StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

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
                verify(featureToggleService).isRpaContinuousFeedEnabled();
                assertThat(stateFlow.getFlags()).hasSize(5).contains(
                    entry(FlowFlag.UNREPRESENTED_DEFENDANT_ONE.name(), true),
                    entry(FlowFlag.UNREPRESENTED_DEFENDANT_TWO.name(), false),
                    entry(FlowFlag.RPA_CONTINUOUS_FEED.name(), true),
                    entry(FlowFlag.NOTICE_OF_CHANGE.name(), false),
                    entry(FlowFlag.SPEC_RPA_CONTINUOUS_FEED.name(), false)
                );
            }

            // Unrepresented
            // 3. Def1 registered, Def 2 unrepresented
            @Test
            void shouldContinueOnline_WhenCaseDataAtStateClaimDraftIssuedAndRespondent2NotRepresented() {
                CaseData caseData = CaseDataBuilderUnspec.builder()
                    .atStateClaimIssuedUnrepresentedDefendant2()
                    .addLegalRepDeadline(LocalDateTime.now().plusDays(14))
                    .build();

                StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

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
                verify(featureToggleService).isRpaContinuousFeedEnabled();
                assertThat(stateFlow.getFlags()).hasSize(5).contains(
                    entry(FlowFlag.UNREPRESENTED_DEFENDANT_ONE.name(), false),
                    entry(FlowFlag.UNREPRESENTED_DEFENDANT_TWO.name(), true),
                    entry(FlowFlag.RPA_CONTINUOUS_FEED.name(), true),
                    entry(FlowFlag.NOTICE_OF_CHANGE.name(), false),
                    entry(FlowFlag.SPEC_RPA_CONTINUOUS_FEED.name(), false)
                );
            }

            // 1v2 spec
            // Unrepresented
            // 3. Def1 registered, Def 2 unrepresented
            @Test
            void shouldContinueOnline_WhenCaseDataAtStateClaimDraftIssuedAndRespondent2NotRepresentedSpec() {
                CaseData caseData = CaseDataBuilderUnspec.builder()
                    .atStateClaimIssuedUnrepresentedDefendant2()
                    .addLegalRepDeadline(LocalDateTime.now().plusDays(14))
                    .build().toBuilder()
                    .superClaimType(SuperClaimType.SPEC_CLAIM).build();

                StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

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
                verify(featureToggleService).isRpaContinuousFeedEnabled();
                assertThat(stateFlow.getFlags()).hasSize(5).contains(
                    entry(FlowFlag.UNREPRESENTED_DEFENDANT_ONE.name(), false),
                    entry(FlowFlag.UNREPRESENTED_DEFENDANT_TWO.name(), true),
                    entry(FlowFlag.RPA_CONTINUOUS_FEED.name(), true),
                    entry(FlowFlag.NOTICE_OF_CHANGE.name(), false),
                    entry(FlowFlag.SPEC_RPA_CONTINUOUS_FEED.name(), false)
                );
            }
        }

        @Nested
        class UnregisteredSolicitor {
            // 1v1 Unregistered
            @Test
            void shouldReturnProceedsWithOfflineJourney_1v1_whenCaseDataAtStateClaimDraftIssuedAndResUnregistered() {
                CaseData caseData = CaseDataBuilderUnspec.builder().atStateProceedsOffline1v1UnregisteredDefendant().build();

                StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

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
                verify(featureToggleService).isRpaContinuousFeedEnabled();
                assertThat(stateFlow.getFlags()).hasSize(4).contains(
                    entry("ONE_RESPONDENT_REPRESENTATIVE", true),
                    entry("RPA_CONTINUOUS_FEED", true),
                    entry(FlowFlag.NOTICE_OF_CHANGE.name(), false),
                    entry(FlowFlag.SPEC_RPA_CONTINUOUS_FEED.name(), false)
                );
            }


            // Unregistered
            // 1. Both def1 and def2 unregistered
            @Test
            void shouldReturnProceedsWithOfflineJourney_whenCaseDataAtStateClaimDraftIssuedRespondentsNotRegistered() {
                CaseData caseData = CaseDataBuilderUnspec.builder().atStateProceedsOfflineUnregisteredDefendants().build();

                StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

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
                    entry("RPA_CONTINUOUS_FEED", true),
                    entry(FlowFlag.NOTICE_OF_CHANGE.name(), false),
                    entry(FlowFlag.SPEC_RPA_CONTINUOUS_FEED.name(), false)
                );
            }

            // Spec
            // Unregistered
            // 1. Both def1 and def2 unregistered
            @Test
            void shouldReturnProceedsWithOfflineJourney_whenRespondentsNotRegisteredSpec() {
                CaseData caseData = CaseDataBuilderUnspec.builder().atStateProceedsOfflineUnregisteredDefendants().build()
                    .toBuilder().superClaimType(SuperClaimType.SPEC_CLAIM).build();

                StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

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
                    entry("RPA_CONTINUOUS_FEED", true),
                    entry(FlowFlag.NOTICE_OF_CHANGE.name(), false),
                    entry(FlowFlag.SPEC_RPA_CONTINUOUS_FEED.name(), false)
                );
            }

            // Unregistered
            // 2. Def1 unregistered, Def2 registered
            @Test
            void shouldReturnProceedsWithOfflineJourney_whenCaseDataAtStateClaimDraftIssuedRespondent1NotRegistered() {
                CaseData caseData = CaseDataBuilderUnspec.builder().atStateProceedsOfflineUnregisteredDefendant1().build();

                StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

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
                verify(featureToggleService).isRpaContinuousFeedEnabled();
                assertThat(stateFlow.getFlags()).hasSize(4).contains(
                    entry("ONE_RESPONDENT_REPRESENTATIVE", true),
                    entry("RPA_CONTINUOUS_FEED", true),
                    entry(FlowFlag.NOTICE_OF_CHANGE.name(), false),
                    entry(FlowFlag.SPEC_RPA_CONTINUOUS_FEED.name(), false)
                );
            }

            // Unregistered
            // 3. Def1 registered, Def 2 unregistered
            @Test
            void shouldReturnProceedsWithOfflineJourney_whenCaseDataAtStateClaimDraftIssuedRespondent2NotRegistered() {
                CaseData caseData = CaseDataBuilderUnspec.builder().atStateProceedsOfflineUnregisteredDefendant2().build();
                if (caseData.getRespondent2OrgRegistered() != null
                    && caseData.getRespondent2Represented() == null) {
                    caseData = caseData.toBuilder()
                        .respondent2Represented(YES)
                        .build();
                }
                StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

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
                verify(featureToggleService).isRpaContinuousFeedEnabled();
                assertThat(stateFlow.getFlags()).hasSize(4).contains(
                    entry("ONE_RESPONDENT_REPRESENTATIVE", true),
                    entry("RPA_CONTINUOUS_FEED", true),
                    entry(FlowFlag.NOTICE_OF_CHANGE.name(), false),
                    entry(FlowFlag.SPEC_RPA_CONTINUOUS_FEED.name(), false)
                );
            }

            // 1v2 Same Unregistered Solicitor
            @Test
            void shouldReturnProceedsWithOfflineJourney_whenCaseDataAtStateClaimDraftIssuedSameUnregisteredSolicitor() {
                CaseData caseData = CaseDataBuilderUnspec.builder().atStateProceedsOfflineSameUnregisteredDefendant().build();

                StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

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
                verify(featureToggleService).isRpaContinuousFeedEnabled();
                assertThat(stateFlow.getFlags()).hasSize(4).contains(
                    entry("ONE_RESPONDENT_REPRESENTATIVE", true),
                    entry("RPA_CONTINUOUS_FEED", true),
                    entry(FlowFlag.NOTICE_OF_CHANGE.name(), false),
                    entry(FlowFlag.SPEC_RPA_CONTINUOUS_FEED.name(), false)
                );
            }
        }

        @Nested
        class UnrepresentedAndUnregistered {
            // Def1 unrepresented, Def2 unregistered
            @Test
            void shouldReturnProceedsWithOfflineJourney_whenCaseDataAtStateClaimDraftIssuedAndRes1UnrepRes2Unregis() {
                CaseData caseData = CaseDataBuilderUnspec.builder()
                    .atStateProceedsOfflineUnrepresentedDefendant1UnregisteredDefendant2().build();

                StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

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
                    entry("RPA_CONTINUOUS_FEED", true),
                    entry(FlowFlag.NOTICE_OF_CHANGE.name(), false),
                    entry(FlowFlag.SPEC_RPA_CONTINUOUS_FEED.name(), false)
                );
            }

            // Unrepresented and Unregistered
            // 2. Def1 unregistered, Def 2 unrepresented
            @Test
            void shouldReturnProceedsWithOfflineJourney_whenCaseDataAtStateClaimDraftIssuedAndRes1UnregisRes2Unrep() {
                CaseData caseData = CaseDataBuilderUnspec.builder()
                    .atStateProceedsOfflineUnregisteredDefendant1UnrepresentedDefendant2().build();
                StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

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
                verify(featureToggleService).isRpaContinuousFeedEnabled();
                assertThat(stateFlow.getFlags()).hasSize(3).contains(
                    entry("RPA_CONTINUOUS_FEED", true),
                    entry(FlowFlag.NOTICE_OF_CHANGE.name(), false),
                    entry(FlowFlag.SPEC_RPA_CONTINUOUS_FEED.name(), false)
                );
            }

        }

        @Test
        void shouldReturnPaymentSuccessful_whenCaseDataAtStatePaymentSuccessful() {
            CaseData caseData = CaseDataBuilderUnspec.builder().atStatePaymentSuccessful().build();
            if (caseData.getRespondent2OrgRegistered() != null
                && caseData.getRespondent2Represented() == null) {
                caseData = caseData.toBuilder()
                    .respondent2Represented(YES)
                    .build();
            }
            StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

            assertThat(stateFlow.getState())
                .extracting(State::getName)
                .isNotNull()
                .isEqualTo(CLAIM_ISSUED_PAYMENT_SUCCESSFUL.fullName());
            assertThat(stateFlow.getStateHistory())
                .hasSize(3)
                .extracting(State::getName)
                .containsExactly(
                    DRAFT.fullName(), CLAIM_SUBMITTED.fullName(), CLAIM_ISSUED_PAYMENT_SUCCESSFUL.fullName());
            verify(featureToggleService).isRpaContinuousFeedEnabled();
            assertThat(stateFlow.getFlags()).hasSize(4).contains(
                entry("ONE_RESPONDENT_REPRESENTATIVE", true),
                entry("RPA_CONTINUOUS_FEED", true),
                entry(FlowFlag.NOTICE_OF_CHANGE.name(), false),
                entry(FlowFlag.SPEC_RPA_CONTINUOUS_FEED.name(), false)
            );
        }

        @Test
        void shouldReturnPaymentSuccessful_whenCaseDataAtStatePaymentSuccessful1v2SameRepresentative() {
            CaseData caseData = CaseDataBuilderUnspec.builder()
                .atStateClaimIssued1v2AndSameRepresentative().build();

            StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

            assertThat(stateFlow.getState())
                .extracting(State::getName)
                .isNotNull()
                .isEqualTo(CLAIM_ISSUED_PAYMENT_SUCCESSFUL.fullName());
            assertThat(stateFlow.getStateHistory())
                .hasSize(3)
                .extracting(State::getName)
                .containsExactly(
                    DRAFT.fullName(), CLAIM_SUBMITTED.fullName(), CLAIM_ISSUED_PAYMENT_SUCCESSFUL.fullName());
            verify(featureToggleService).isRpaContinuousFeedEnabled();
            assertThat(stateFlow.getFlags()).hasSize(4).contains(
                entry("ONE_RESPONDENT_REPRESENTATIVE", true),
                entry("RPA_CONTINUOUS_FEED", true),
                entry(FlowFlag.NOTICE_OF_CHANGE.name(), false),
                entry(FlowFlag.SPEC_RPA_CONTINUOUS_FEED.name(), false)
            );
        }

        @Test
        void shouldReturnPaymentFailed_whenCaseDataAtStatePaymentFailed() {
            CaseData caseData = CaseDataBuilderUnspec.builder().atStatePaymentFailed().build();

            StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

            assertThat(stateFlow.getState())
                .extracting(State::getName)
                .isNotNull()
                .isEqualTo(CLAIM_ISSUED_PAYMENT_FAILED.fullName());
            assertThat(stateFlow.getStateHistory())
                .hasSize(3)
                .extracting(State::getName)
                .containsExactly(
                    DRAFT.fullName(), CLAIM_SUBMITTED.fullName(), CLAIM_ISSUED_PAYMENT_FAILED.fullName());
            verify(featureToggleService).isRpaContinuousFeedEnabled();
            assertThat(stateFlow.getFlags()).hasSize(4).contains(
                entry("ONE_RESPONDENT_REPRESENTATIVE", true),
                entry("RPA_CONTINUOUS_FEED", true),
                entry(FlowFlag.NOTICE_OF_CHANGE.name(), false),
                entry(FlowFlag.SPEC_RPA_CONTINUOUS_FEED.name(), false)
            );
        }

        @Test
        void shouldReturnAwaitingCaseNotification_whenCaseDataAtStateAwaitingCaseNotification() {
            CaseData caseData = CaseDataBuilderUnspec.builder().atStatePendingClaimIssued().build();
            if (caseData.getRespondent2OrgRegistered() != null
                && caseData.getRespondent2Represented() == null) {
                caseData = caseData.toBuilder()
                    .respondent2Represented(YES)
                    .build();
            }
            StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

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
            verify(featureToggleService).isRpaContinuousFeedEnabled();
            assertThat(stateFlow.getFlags()).hasSize(4).contains(
                entry("ONE_RESPONDENT_REPRESENTATIVE", true),
                entry("RPA_CONTINUOUS_FEED", true),
                entry(FlowFlag.NOTICE_OF_CHANGE.name(), false),
                entry(FlowFlag.SPEC_RPA_CONTINUOUS_FEED.name(), false)
            );
        }

        @Test
        void shouldReturnProceedsWithOfflineJourney_whenCaseDataAtStateClaimIssued_andOneSolicitorIsToBeNotified() {
            CaseData caseData = CaseDataBuilderUnspec.builder()
                .atStateClaimNotified_1v2_andNotifyOnlyOneSolicitor()
                .build();
            if (caseData.getRespondent2OrgRegistered() != null
                && caseData.getRespondent2Represented() == null) {
                caseData = caseData.toBuilder()
                    .respondent2Represented(YES)
                    .build();
            }
            StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

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
                entry("RPA_CONTINUOUS_FEED", true),
                entry(FlowFlag.NOTICE_OF_CHANGE.name(), false),
                entry(FlowFlag.SPEC_RPA_CONTINUOUS_FEED.name(), false)
            );
        }

        @Test
        void shouldReturnClaimNotified_whenCaseDataAtStateClaimNotified_andBothSolicitorsAreToBeNotified() {
            CaseData caseData = CaseDataBuilderUnspec.builder()
                .atStateClaimNotified_1v2_andNotifyBothSolicitors()
                .build();
            if (caseData.getRespondent2OrgRegistered() != null
                && caseData.getRespondent2Represented() == null) {
                caseData = caseData.toBuilder()
                    .respondent2Represented(YES)
                    .build();
            }
            StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

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
                entry("RPA_CONTINUOUS_FEED", true),
                entry(FlowFlag.NOTICE_OF_CHANGE.name(), false),
                entry(FlowFlag.SPEC_RPA_CONTINUOUS_FEED.name(), false)
            );
        }

        @Test
        void shouldReturnAwaitingCaseNotification_whenCaseDataAtStateAwaitingCaseDetailsNotification() {
            CaseData caseData = CaseDataBuilderUnspec.builder().atStateClaimNotified_1v1().build();
            if (caseData.getRespondent2OrgRegistered() != null
                && caseData.getRespondent2Represented() == null) {
                caseData = caseData.toBuilder()
                    .respondent2Represented(YES)
                    .build();
            }
            StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

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
            verify(featureToggleService).isRpaContinuousFeedEnabled();
            assertThat(stateFlow.getFlags()).hasSize(4).contains(
                entry(FlowFlag.SPEC_RPA_CONTINUOUS_FEED.name(), false),
                entry(FlowFlag.NOTICE_OF_CHANGE.name(), false),
                entry("ONE_RESPONDENT_REPRESENTATIVE", true),
                entry("RPA_CONTINUOUS_FEED", true)
            );
        }

        @Test
        void shouldReturnClaimDetailsNotified_whenCaseDataAtStateClaimDetailsNotified() {
            CaseData caseData = CaseDataBuilderUnspec.builder().atStateClaimDetailsNotified().build();

            StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

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
            verify(featureToggleService).isRpaContinuousFeedEnabled();
            assertThat(stateFlow.getFlags()).hasSize(4).contains(
                entry(FlowFlag.SPEC_RPA_CONTINUOUS_FEED.name(), false),
                entry(FlowFlag.NOTICE_OF_CHANGE.name(), false),
                entry("ONE_RESPONDENT_REPRESENTATIVE", true),
                entry("RPA_CONTINUOUS_FEED", true)
            );
        }

        @Test
        void shouldReturnClaimDetailsNotified_whenCaseDataAtStateClaimDetailsNotifiedBothSolicitors1v2() {
            CaseData caseData = CaseDataBuilderUnspec.builder()
                .atStateClaimDetailsNotified_1v2_andNotifyBothSolicitors()
                .build();
            if (caseData.getRespondent2OrgRegistered() != null
                && caseData.getRespondent2Represented() == null) {
                caseData = caseData.toBuilder()
                    .respondent2Represented(YES)
                    .build();
            }
            StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

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
            verify(featureToggleService).isRpaContinuousFeedEnabled();

            assertThat(stateFlow.getFlags()).hasSize(5).contains(
                entry("ONE_RESPONDENT_REPRESENTATIVE", false),
                entry("TWO_RESPONDENT_REPRESENTATIVES", true),
                entry("RPA_CONTINUOUS_FEED", true),
                entry(FlowFlag.NOTICE_OF_CHANGE.name(), false),
                entry(FlowFlag.SPEC_RPA_CONTINUOUS_FEED.name(), false)
            );
        }

        @Test
        void shouldReturnClaimDetailsNotified_whenCaseDataAtStateClaimDetailsNotifiedSingleSolicitorIn1v2() {
            CaseData caseData = CaseDataBuilderUnspec.builder()
                .atStateClaimDetailsNotified_1v2_andNotifyOnlyOneSolicitor()
                .build();
            if (caseData.getRespondent2OrgRegistered() != null
                && caseData.getRespondent2Represented() == null) {
                caseData = caseData.toBuilder()
                    .respondent2Represented(YES)
                    .build();
            }
            StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

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
            verify(featureToggleService).isRpaContinuousFeedEnabled();
        }

        @Test
        void shouldReturnClaimDetailsNotifiedTimeExtension_whenCaseDataAtStateClaimDetailsNotifiedTimeExtension() {
            CaseData caseData = CaseDataBuilderUnspec.builder().atStateClaimDetailsNotifiedTimeExtension().build();

            StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

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
            verify(featureToggleService).isRpaContinuousFeedEnabled();
            assertThat(stateFlow.getFlags()).hasSize(4).contains(
                entry("ONE_RESPONDENT_REPRESENTATIVE", true),
                entry("RPA_CONTINUOUS_FEED", true),
                entry(FlowFlag.NOTICE_OF_CHANGE.name(), false),
                entry(FlowFlag.SPEC_RPA_CONTINUOUS_FEED.name(), false)
            );
        }

        @Test
        void shouldReturnClaimAcknowledge_whenCaseDataAtStateClaimAcknowledge() {
            CaseData caseData = CaseDataBuilderUnspec.builder().atStateNotificationAcknowledged().build();
            if (caseData.getRespondent2OrgRegistered() != null
                && caseData.getRespondent2Represented() == null) {
                caseData = caseData.toBuilder()
                    .respondent2Represented(YES)
                    .build();
            }
            StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

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
            verify(featureToggleService).isRpaContinuousFeedEnabled();
            assertThat(stateFlow.getFlags()).hasSize(4).contains(
                entry("ONE_RESPONDENT_REPRESENTATIVE", true),
                entry("RPA_CONTINUOUS_FEED", true),
                entry(FlowFlag.NOTICE_OF_CHANGE.name(), false),
                entry(FlowFlag.SPEC_RPA_CONTINUOUS_FEED.name(), false)
            );
        }

        @Test
        void shouldReturnNotificationAcknowledgedTimeExtension_whenCaseDataAtStateClaimAcknowledgeTimeExtension() {
            CaseData caseData = CaseDataBuilderUnspec.builder()
                .atStateNotificationAcknowledgedRespondent1TimeExtension().build();
            if (caseData.getRespondent2OrgRegistered() != null
                && caseData.getRespondent2Represented() == null) {
                caseData = caseData.toBuilder()
                    .respondent2Represented(YES)
                    .build();
            }
            StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

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
            verify(featureToggleService).isRpaContinuousFeedEnabled();
            assertThat(stateFlow.getFlags()).hasSize(4).contains(
                entry("ONE_RESPONDENT_REPRESENTATIVE", true),
                entry("RPA_CONTINUOUS_FEED", true),
                entry(FlowFlag.NOTICE_OF_CHANGE.name(), false),
                entry(FlowFlag.SPEC_RPA_CONTINUOUS_FEED.name(), false)
            );
        }

        @Test
        void shouldReturnClaimDismissed_whenCaseDataAtStateClaimAcknowledgeAndCcdStateIsDismissed() {
            CaseData caseData = CaseDataBuilderUnspec.builder().atStateNotificationAcknowledged()
                .claimDismissedDate(LocalDateTime.now())
                .claimDismissedDeadline(LocalDateTime.now().minusHours(4))
                .build();

            StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

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
            verify(featureToggleService).isRpaContinuousFeedEnabled();
            assertThat(stateFlow.getFlags()).hasSize(4).contains(
                entry("ONE_RESPONDENT_REPRESENTATIVE", true),
                entry("RPA_CONTINUOUS_FEED", true),
                entry(FlowFlag.NOTICE_OF_CHANGE.name(), false),
                entry(FlowFlag.SPEC_RPA_CONTINUOUS_FEED.name(), false)
            );
        }

        @Test
        void shouldReturnExtensionRequested_whenCaseDataAtStateClaimDetailsNotifiedTimeExtension() {
            CaseData caseData = CaseDataBuilderUnspec.builder().atStateClaimDetailsNotifiedTimeExtension().build();

            StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

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
            verify(featureToggleService).isRpaContinuousFeedEnabled();
            assertThat(stateFlow.getFlags()).hasSize(4).contains(
                entry("ONE_RESPONDENT_REPRESENTATIVE", true),
                entry("RPA_CONTINUOUS_FEED", true),
                entry(FlowFlag.NOTICE_OF_CHANGE.name(), false),
                entry(FlowFlag.SPEC_RPA_CONTINUOUS_FEED.name(), false)
            );
        }

        @Nested
        class RespondentResponse {

            @Test
            void shouldReturnFullDefence_whenCaseDataAtStateRespondentFullDefence() {
                CaseData caseData = CaseDataBuilderUnspec.builder()
                    .atStateRespondentFullDefenceAfterNotificationAcknowledgement()
                    .build();

                StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

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
                verify(featureToggleService).isRpaContinuousFeedEnabled();
                assertThat(stateFlow.getFlags()).hasSize(4).contains(
                    entry(FlowFlag.SPEC_RPA_CONTINUOUS_FEED.name(), false),
                    entry(FlowFlag.NOTICE_OF_CHANGE.name(), false),
                    entry("ONE_RESPONDENT_REPRESENTATIVE", true),
                    entry("RPA_CONTINUOUS_FEED", true)
                );
            }

            @Test
            void shouldReturnFullAdmission_whenCaseDataAtStateRespondentFullAdmission() {
                CaseData caseData = CaseDataBuilderUnspec.builder()
                    .atStateRespondentFullAdmissionAfterNotificationAcknowledged()
                    .build();
                if (caseData.getRespondent2OrgRegistered() != null
                    && caseData.getRespondent2Represented() == null) {
                    caseData = caseData.toBuilder()
                        .respondent2Represented(YES)
                        .build();
                }
                StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

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
                verify(featureToggleService).isRpaContinuousFeedEnabled();
                assertThat(stateFlow.getFlags()).hasSize(4).contains(
                    entry(FlowFlag.SPEC_RPA_CONTINUOUS_FEED.name(), false),
                    entry(FlowFlag.NOTICE_OF_CHANGE.name(), false),
                    entry("ONE_RESPONDENT_REPRESENTATIVE", true),
                    entry("RPA_CONTINUOUS_FEED", true)
                );
            }

            @Test
            void shouldReturnPartAdmission_whenCaseDataAtStateRespondentPartAdmission() {
                CaseData caseData = CaseDataBuilderUnspec.builder()
                    .atStateRespondentPartAdmissionAfterNotificationAcknowledgement()
                    .build();

                StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

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
                verify(featureToggleService).isRpaContinuousFeedEnabled();
                assertThat(stateFlow.getFlags()).hasSize(4).contains(
                    entry("ONE_RESPONDENT_REPRESENTATIVE", true),
                    entry("RPA_CONTINUOUS_FEED", true),
                    entry(FlowFlag.NOTICE_OF_CHANGE.name(), false),
                    entry(FlowFlag.SPEC_RPA_CONTINUOUS_FEED.name(), false)
                );
            }

            @Test
            void shouldReturnCounterClaim_whenCaseDataAtStateRespondentCounterClaim() {
                CaseData caseData = CaseDataBuilderUnspec.builder().atStateRespondentCounterClaim().build();
                if (caseData.getRespondent2OrgRegistered() != null
                    && caseData.getRespondent2Represented() == null) {
                    caseData = caseData.toBuilder()
                        .respondent2Represented(YES)
                        .build();
                }
                StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

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
                verify(featureToggleService).isRpaContinuousFeedEnabled();
                assertThat(stateFlow.getFlags()).hasSize(4).contains(
                    entry("ONE_RESPONDENT_REPRESENTATIVE", true),
                    entry("RPA_CONTINUOUS_FEED", true),
                    entry(FlowFlag.NOTICE_OF_CHANGE.name(), false),
                    entry(FlowFlag.SPEC_RPA_CONTINUOUS_FEED.name(), false)
                );
            }
        }

        @Nested
        class DefendantResponseMultiparty {

            // 1v2 Different solicitor scenario-first response FullDefence received
            @Test
            void shouldGenerateDQ_1v2DiffSol_whenFirstResponseIsFullDefence() {
                CaseData caseData = CaseDataBuilderUnspec.builder()
                    .atStateRespondentFullDefence()
                    .multiPartyClaimTwoDefendantSolicitors()
                    .build();
                if (caseData.getRespondent2OrgRegistered() != null
                    && caseData.getRespondent2Represented() == null) {
                    caseData = caseData.toBuilder()
                        .respondent2Represented(YES)
                        .build();
                }
                StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

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
                verify(featureToggleService).isRpaContinuousFeedEnabled();
                assertThat(stateFlow.getFlags()).hasSize(5).contains(
                    entry("ONE_RESPONDENT_REPRESENTATIVE", false),
                    entry("RPA_CONTINUOUS_FEED", true),
                    entry("TWO_RESPONDENT_REPRESENTATIVES", true),
                    entry(FlowFlag.NOTICE_OF_CHANGE.name(), false),
                    entry(FlowFlag.SPEC_RPA_CONTINUOUS_FEED.name(), false)
                );
            }

            //1v2 Different solicitor scenario-first response FullDefence received
            @Test
            void shouldGenerateDQ_1v2DiffSol_whenFirstResponseIsNotFullDefence() {
                CaseData caseData = CaseDataBuilderUnspec.builder()
                    .atStateRespondentCounterClaim()
                    .multiPartyClaimTwoDefendantSolicitors()
                    .build();
                if (caseData.getRespondent2OrgRegistered() != null
                    && caseData.getRespondent2Represented() == null) {
                    caseData = caseData.toBuilder()
                        .respondent2Represented(YES)
                        .build();
                }
                StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

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
                verify(featureToggleService).isRpaContinuousFeedEnabled();
                assertThat(stateFlow.getFlags()).hasSize(5).contains(
                    entry("ONE_RESPONDENT_REPRESENTATIVE", false),
                    entry("RPA_CONTINUOUS_FEED", true),
                    entry("TWO_RESPONDENT_REPRESENTATIVES", true),
                    entry(FlowFlag.NOTICE_OF_CHANGE.name(), false),
                    entry(FlowFlag.SPEC_RPA_CONTINUOUS_FEED.name(), false)
                );
            }

            //1v2 Different solicitor scenario-first response FullDefence received
            @Test
            void shouldGenerateDQ_in1v2Scenario_whenFirstPartySubmitFullDefenceResponse() {
                CaseData caseData = CaseDataBuilderUnspec.builder()
                    .atStateRespondentFullDefence()
                    .multiPartyClaimTwoDefendantSolicitors()
                    .build();
                if (caseData.getRespondent2OrgRegistered() != null
                    && caseData.getRespondent2Represented() == null) {
                    caseData = caseData.toBuilder()
                        .respondent2Represented(YES)
                        .build();
                }
                StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

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
                verify(featureToggleService).isRpaContinuousFeedEnabled();
                assertThat(stateFlow.getFlags()).hasSize(5).contains(
                    entry("ONE_RESPONDENT_REPRESENTATIVE", false),
                    entry("RPA_CONTINUOUS_FEED", true),
                    entry("TWO_RESPONDENT_REPRESENTATIVES", true),
                    entry(FlowFlag.NOTICE_OF_CHANGE.name(), false),
                    entry(FlowFlag.SPEC_RPA_CONTINUOUS_FEED.name(), false)
                );
            }

            //1v2 Different solicitor scenario-first party acknowledges, not responds
            // second party submits response FullDefence
            @Test
            void shouldGenerateDQ_in1v2Scenario_whenSecondPartySubmitFullDefenceResponse() {
                CaseData caseData = CaseDataBuilderUnspec.builder()
                    .atStateRespondentFullDefenceRespondent2()
                    .multiPartyClaimTwoDefendantSolicitors()
                    .build();
                if (caseData.getRespondent2OrgRegistered() != null
                    && caseData.getRespondent2Represented() == null) {
                    caseData = caseData.toBuilder()
                        .respondent2Represented(YES)
                        .build();
                }
                StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

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
                verify(featureToggleService).isRpaContinuousFeedEnabled();
                assertThat(stateFlow.getFlags()).hasSize(5).contains(
                    entry("ONE_RESPONDENT_REPRESENTATIVE", false),
                    entry("RPA_CONTINUOUS_FEED", true),
                    entry(FlowFlag.SPEC_RPA_CONTINUOUS_FEED.name(), false),
                    entry(FlowFlag.NOTICE_OF_CHANGE.name(), false),
                    entry("TWO_RESPONDENT_REPRESENTATIVES", true)
                );
            }

            //Respondent 1 submits FULL DEFENCE, Respondent 2 submits FULL DEFENCE
            @Test
            void shouldReturnFullDefence_in1v2Scenario_whenBothPartiesSubmitFullDefenceResponses() {
                CaseData caseData = CaseDataBuilderUnspec.builder()
                    .atStateRespondentFullDefence_1v2_BothPartiesFullDefenceResponses()
                    .multiPartyClaimTwoDefendantSolicitors()
                    .build();
                if (caseData.getRespondent2OrgRegistered() != null
                    && caseData.getRespondent2Represented() == null) {
                    caseData = caseData.toBuilder()
                        .respondent2Represented(YES)
                        .build();
                }
                StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

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
                verify(featureToggleService).isRpaContinuousFeedEnabled();
                assertThat(stateFlow.getFlags()).hasSize(5).contains(
                    entry("ONE_RESPONDENT_REPRESENTATIVE", false),
                    entry("RPA_CONTINUOUS_FEED", true),
                    entry(FlowFlag.SPEC_RPA_CONTINUOUS_FEED.name(), false),
                    entry(FlowFlag.NOTICE_OF_CHANGE.name(), false),
                    entry("TWO_RESPONDENT_REPRESENTATIVES", true)
                );
            }

            //Respondent 1 and 2 acknowledges claim, then submits  FULL DEFENCE
            @Test
            void shouldReturnFullDefence_in1v2Scenario_whenBothPartiesAcknowledgedAndSubmitFullDefenceResponses() {
                CaseData caseData = CaseDataBuilderUnspec.builder()
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
                StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

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
                verify(featureToggleService).isRpaContinuousFeedEnabled();
                assertThat(stateFlow.getFlags()).hasSize(5).contains(
                    entry("ONE_RESPONDENT_REPRESENTATIVE", false),
                    entry("RPA_CONTINUOUS_FEED", true),
                    entry(FlowFlag.SPEC_RPA_CONTINUOUS_FEED.name(), false),
                    entry(FlowFlag.NOTICE_OF_CHANGE.name(), false),
                    entry("TWO_RESPONDENT_REPRESENTATIVES", true)
                );
            }

            //Respondent 1 acknowledges claim, then Respondent 1 & 2 submits  FULL DEFENCE
            @Test
            void shouldReturnFullDefence_in1v2Scenario_whenRep1AcknowledgedAndBothSubmitFullDefenceResponses() {
                CaseData caseData = CaseDataBuilderUnspec.builder()
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
                StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

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
                verify(featureToggleService).isRpaContinuousFeedEnabled();
                assertThat(stateFlow.getFlags()).hasSize(5).contains(
                    entry("ONE_RESPONDENT_REPRESENTATIVE", false),
                    entry("RPA_CONTINUOUS_FEED", true),
                    entry(FlowFlag.SPEC_RPA_CONTINUOUS_FEED.name(), false),
                    entry(FlowFlag.NOTICE_OF_CHANGE.name(), false),
                    entry("TWO_RESPONDENT_REPRESENTATIVES", true)
                );
            }

            // Respondent 2 acknowledges claim, Respondent 1 & 2 submits  FULL DEFENCE
            @Test
            void shouldReturnFullDefence_in1v2Scenario_whenRep2AcknowledgedAndBothSubmitFullDefenceResponses() {
                CaseData caseData = CaseDataBuilderUnspec.builder()
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

                StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

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
                verify(featureToggleService).isRpaContinuousFeedEnabled();
                assertThat(stateFlow.getFlags()).hasSize(5).contains(
                    entry("ONE_RESPONDENT_REPRESENTATIVE", false),
                    entry("RPA_CONTINUOUS_FEED", true),
                    entry(FlowFlag.SPEC_RPA_CONTINUOUS_FEED.name(), false),
                    entry(FlowFlag.NOTICE_OF_CHANGE.name(), false),
                    entry("TWO_RESPONDENT_REPRESENTATIVES", true)
                );
            }

            //Respondent 1 submits FULL DEFENCE, Respondent 2 submits COUNTER CLAIM
            @Test
            void shouldReturnDivergentResponseAndGoOffline_1v2Scenario_whenFirstRespondentSubmitsFullDefenceResponse() {
                CaseData caseData = CaseDataBuilderUnspec.builder()
                    .atStateRespondentFullDefence_1v2_Resp1FullDefenceAndResp2CounterClaim()
                    .multiPartyClaimTwoDefendantSolicitors()
                    .build();
                if (caseData.getRespondent2OrgRegistered() != null
                    && caseData.getRespondent2Represented() == null) {
                    caseData = caseData.toBuilder()
                        .respondent2Represented(YES)
                        .build();
                }
                StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

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
                verify(featureToggleService).isRpaContinuousFeedEnabled();
                assertThat(stateFlow.getFlags()).hasSize(5).contains(
                    entry("ONE_RESPONDENT_REPRESENTATIVE", false),
                    entry("RPA_CONTINUOUS_FEED", true),
                    entry(FlowFlag.SPEC_RPA_CONTINUOUS_FEED.name(), false),
                    entry(FlowFlag.NOTICE_OF_CHANGE.name(), false),
                    entry("TWO_RESPONDENT_REPRESENTATIVES", true)
                );
            }

            //Respondent 1 submits FULL DEFENCE, Respondent 2 submits COUNTER CLAIM
            @Test
            void shouldReturnDivergentResponse_in1v2SameSolicitorScenario_whenOneRespondentSubmitsFullDefence() {
                CaseData caseData = CaseDataBuilderUnspec.builder()
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
                StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

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
                verify(featureToggleService).isRpaContinuousFeedEnabled();
                assertThat(stateFlow.getFlags()).hasSize(4).contains(
                    entry("ONE_RESPONDENT_REPRESENTATIVE", true),
                    entry("RPA_CONTINUOUS_FEED", true),
                    entry(FlowFlag.NOTICE_OF_CHANGE.name(), false),
                    entry(FlowFlag.SPEC_RPA_CONTINUOUS_FEED.name(), false)
                );
            }

            //Respondent 1 submits ADMITS PART, Respondent 2 submits COUNTER CLAIM
            @Test
            void shouldReturnDivergentResponse_in1v2Scenario_whenNeitherRespondentSubmitsFullDefenceResponse() {
                CaseData caseData = CaseDataBuilderUnspec.builder()
                    .atStateDivergentResponse_1v2_Resp1FullAdmissionAndResp2CounterClaim()
                    .multiPartyClaimTwoDefendantSolicitors()
                    .build();
                if (caseData.getRespondent2OrgRegistered() != null
                    && caseData.getRespondent2Represented() == null) {
                    caseData = caseData.toBuilder()
                        .respondent2Represented(YES)
                        .build();
                }
                StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

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
                verify(featureToggleService).isRpaContinuousFeedEnabled();
                assertThat(stateFlow.getFlags()).hasSize(5).contains(
                    entry("ONE_RESPONDENT_REPRESENTATIVE", false),
                    entry("RPA_CONTINUOUS_FEED", true),
                    entry("TWO_RESPONDENT_REPRESENTATIVES", true),
                    entry(FlowFlag.NOTICE_OF_CHANGE.name(), false),
                    entry(FlowFlag.SPEC_RPA_CONTINUOUS_FEED.name(), false)
                );
            }

            //Respondent 1 submits ADMITS PART, Respondent 2 submits ADMITS PART
            @Test
            void shouldReturnAdmitsPartResponse_in1v2Scenario_whenBothRespondentsSubmitAdmitPartResponses() {
                CaseData caseData = CaseDataBuilderUnspec.builder()
                    .atStateFullAdmission_1v2_BothRespondentSolicitorsSubmitFullAdmissionResponse()
                    .multiPartyClaimTwoDefendantSolicitors()
                    .build();
                if (caseData.getRespondent2OrgRegistered() != null
                    && caseData.getRespondent2Represented() == null) {
                    caseData = caseData.toBuilder()
                        .respondent2Represented(YES)
                        .build();
                }
                StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

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
                verify(featureToggleService).isRpaContinuousFeedEnabled();
                assertThat(stateFlow.getFlags()).hasSize(5).contains(
                    entry("ONE_RESPONDENT_REPRESENTATIVE", false),
                    entry("RPA_CONTINUOUS_FEED", true),
                    entry(FlowFlag.SPEC_RPA_CONTINUOUS_FEED.name(), false),
                    entry(FlowFlag.NOTICE_OF_CHANGE.name(), false),
                    entry("TWO_RESPONDENT_REPRESENTATIVES", true)
                );
            }
        }

        @Test
        void shouldReturnClaimDismissed_whenCaseDataAtStateClaimDismissed() {
            CaseData caseData = CaseDataBuilderUnspec.builder().atStateClaimDismissed()
                .build();

            StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

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
            verify(featureToggleService).isRpaContinuousFeedEnabled();
            assertThat(stateFlow.getFlags()).hasSize(4).contains(
                entry("ONE_RESPONDENT_REPRESENTATIVE", true),
                entry("RPA_CONTINUOUS_FEED", true),
                entry(FlowFlag.NOTICE_OF_CHANGE.name(), false),
                entry(FlowFlag.SPEC_RPA_CONTINUOUS_FEED.name(), false)
            );
        }

        @ParameterizedTest
        @EnumSource(value = FlowState.Main.class,
            mode = EnumSource.Mode.INCLUDE,
            names = {"FULL_DEFENCE_PROCEED", "FULL_DEFENCE_NOT_PROCEED"}
        )
        void shouldReturnFullDefenceProceed_whenCaseDataAtStateApplicantRespondToDefence(FlowState.Main flowState) {
            CaseData caseData = CaseDataBuilderUnspec.builder().atState(flowState)
                .takenOfflineDate(LocalDateTime.now())
                .build();

            StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

            assertThat(stateFlow.getState())
                .extracting(State::getName)
                .isNotNull()
                .isEqualTo(flowState.fullName());
            assertThat(stateFlow.getStateHistory())
                .hasSize(11)
                .extracting(State::getName)
                .containsExactly(
                    DRAFT.fullName(), CLAIM_SUBMITTED.fullName(), CLAIM_ISSUED_PAYMENT_SUCCESSFUL.fullName(),
                    PENDING_CLAIM_ISSUED.fullName(), CLAIM_ISSUED.fullName(), CLAIM_NOTIFIED.fullName(),
                    CLAIM_DETAILS_NOTIFIED.fullName(), NOTIFICATION_ACKNOWLEDGED.fullName(),
                    ALL_RESPONSES_RECEIVED.fullName(), FULL_DEFENCE.fullName(), flowState.fullName()
                );
            verify(featureToggleService).isRpaContinuousFeedEnabled();
            assertThat(stateFlow.getFlags()).hasSize(4).contains(
                entry(FlowFlag.SPEC_RPA_CONTINUOUS_FEED.name(), false),
                entry(FlowFlag.NOTICE_OF_CHANGE.name(), false),
                entry("ONE_RESPONDENT_REPRESENTATIVE", true),
                entry("RPA_CONTINUOUS_FEED", true)
            );
        }

        //1v2 Different solicitor scenario-first response FullDefence received and with time extension
        @Test
        void shouldAwaitResponse_1v2DiffSol_whenFirstResponseIsFullDefenceAndTimeExtension() {
            CaseData caseData = CaseDataBuilderUnspec.builder()
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
            StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

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
            verify(featureToggleService).isRpaContinuousFeedEnabled();
            assertThat(stateFlow.getFlags()).hasSize(5).contains(
                entry("ONE_RESPONDENT_REPRESENTATIVE", false),
                entry("RPA_CONTINUOUS_FEED", true),
                entry("TWO_RESPONDENT_REPRESENTATIVES", true),
                entry(FlowFlag.NOTICE_OF_CHANGE.name(), false),
                entry(FlowFlag.SPEC_RPA_CONTINUOUS_FEED.name(), false)
            );
        }

        @Test
        //1v2 Different solicitor scenario-both responses FullDefence received and with time extension
        void shouldAwaitResponse_1v2DiffSol_whenBothRespondFullDefenceAndTimeExtension() {
            CaseData caseData = CaseDataBuilderUnspec.builder()
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
            StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

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
            verify(featureToggleService).isRpaContinuousFeedEnabled();
            assertThat(stateFlow.getFlags()).hasSize(5).contains(
                entry("ONE_RESPONDENT_REPRESENTATIVE", false),
                entry("RPA_CONTINUOUS_FEED", true),
                entry(FlowFlag.SPEC_RPA_CONTINUOUS_FEED.name(), false),
                entry("TWO_RESPONDENT_REPRESENTATIVES", true)
            );
        }

        //1v2 Different solicitor scenario-first response FullDefence received and with time extension
        @Test
        void shouldAwaitResponse_1v2DiffSol_whenFirstResponseIsFullDefenceAfterAcknowledgeClaimAndTimeExtension() {
            CaseData caseData = CaseDataBuilderUnspec.builder()
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
            StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

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
            verify(featureToggleService).isRpaContinuousFeedEnabled();
            assertThat(stateFlow.getFlags()).hasSize(5).contains(
                entry("ONE_RESPONDENT_REPRESENTATIVE", false),
                entry("RPA_CONTINUOUS_FEED", true),
                entry("TWO_RESPONDENT_REPRESENTATIVES", true),
                entry(FlowFlag.NOTICE_OF_CHANGE.name(), false),
                entry(FlowFlag.SPEC_RPA_CONTINUOUS_FEED.name(), false)
            );
        }

        @Test
        void shouldReturnProceedsWithOfflineJourney_whenCaseDataIsCaseProceedsInCaseman() {
            CaseData caseData = CaseDataBuilderUnspec.builder().atStateTakenOfflineByStaff().build();

            StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

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
            verify(featureToggleService).isRpaContinuousFeedEnabled();
            assertThat(stateFlow.getFlags()).hasSize(4).contains(
                entry(FlowFlag.SPEC_RPA_CONTINUOUS_FEED.name(), false),
                entry(FlowFlag.NOTICE_OF_CHANGE.name(), false),
                entry("ONE_RESPONDENT_REPRESENTATIVE", true),
                entry("RPA_CONTINUOUS_FEED", true)
            );
        }

        @Test
        void shouldReturnAwaitingCamundaState_whenDefendantHasRespondedAndApplicantIsOutOfTime() {
            CaseData caseData = CaseDataBuilderUnspec.builder().atStatePastApplicantResponseDeadline().build();

            StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

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
            CaseData caseData = CaseDataBuilderUnspec.builder().atStateTakenOfflinePastApplicantResponseDeadline().build();

            StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

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
            verify(featureToggleService).isRpaContinuousFeedEnabled();
            assertThat(stateFlow.getFlags()).hasSize(4).contains(
                entry(FlowFlag.SPEC_RPA_CONTINUOUS_FEED.name(), false),
                entry(FlowFlag.NOTICE_OF_CHANGE.name(), false),
                entry("ONE_RESPONDENT_REPRESENTATIVE", true),
                entry("RPA_CONTINUOUS_FEED", true)
            );
        }

        @Test
        void shouldReturnAwaitingCamundaState_whenPastClaimNotificationDeadline() {
            CaseData caseData = CaseDataBuilderUnspec.builder().atStateClaimPastClaimNotificationDeadline().build();
            if (caseData.getRespondent2OrgRegistered() != null
                && caseData.getRespondent2Represented() == null) {
                caseData = caseData.toBuilder()
                    .respondent2Represented(YES)
                    .build();
            }
            StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

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
            CaseData caseData = CaseDataBuilderUnspec.builder().atStateClaimDismissedPastClaimNotificationDeadline().build();

            StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

            assertThat(stateFlow.getState())
                .extracting(State::getName)
                .isNotNull()
                .isEqualTo(CLAIM_DISMISSED_PAST_CLAIM_NOTIFICATION_DEADLINE.fullName());
            assertThat(stateFlow.getStateHistory())
                .hasSize(7)
                .extracting(State::getName)
                .containsExactly(
                    DRAFT.fullName(), CLAIM_SUBMITTED.fullName(), CLAIM_ISSUED_PAYMENT_SUCCESSFUL.fullName(),
                    PENDING_CLAIM_ISSUED.fullName(), CLAIM_ISSUED.fullName(),
                    PAST_CLAIM_NOTIFICATION_DEADLINE_AWAITING_CAMUNDA.fullName(),
                    CLAIM_DISMISSED_PAST_CLAIM_NOTIFICATION_DEADLINE.fullName()
                );
            verify(featureToggleService).isRpaContinuousFeedEnabled();
            assertThat(stateFlow.getFlags()).hasSize(4).contains(
                entry(FlowFlag.SPEC_RPA_CONTINUOUS_FEED.name(), false),
                entry(FlowFlag.NOTICE_OF_CHANGE.name(), false),
                entry("ONE_RESPONDENT_REPRESENTATIVE", true),
                entry("RPA_CONTINUOUS_FEED", true)
            );
        }

        @Test
        void shouldReturnAwaitingCamundaState_whenCaseDataIsPastClaimDetailsNotification() {
            CaseData caseData = CaseDataBuilderUnspec.builder()
                .atStateClaimPastClaimDetailsNotificationDeadline()
                .build();
            if (caseData.getRespondent2OrgRegistered() != null
                && caseData.getRespondent2Represented() == null) {
                caseData = caseData.toBuilder()
                    .respondent2Represented(YES)
                    .build();
            }
            StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

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
            CaseData caseData = CaseDataBuilderUnspec.builder()
                .atStateClaimDismissedPastClaimDetailsNotificationDeadline()
                .build();

            StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

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
            verify(featureToggleService).isRpaContinuousFeedEnabled();
            assertThat(stateFlow.getFlags()).hasSize(4).contains(
                entry(FlowFlag.SPEC_RPA_CONTINUOUS_FEED.name(), false),
                entry(FlowFlag.NOTICE_OF_CHANGE.name(), false),
                entry("ONE_RESPONDENT_REPRESENTATIVE", true),
                entry("RPA_CONTINUOUS_FEED", true)
            );
        }
    }

    @Nested
    class TakenOfflineByStaff {

        @Test
        void shouldReturnProceedsWithOfflineJourney_whenCaseTakenOfflineAfterClaimIssue() {
            CaseData caseData = CaseDataBuilderUnspec.builder().atStateTakenOfflineByStaff()
                .build();
            StateFlow stateFlow = stateFlowEngine.evaluate(caseData);
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
            verify(featureToggleService).isRpaContinuousFeedEnabled();
            assertThat(stateFlow.getFlags()).hasSize(4).contains(
                entry("ONE_RESPONDENT_REPRESENTATIVE", true),
                entry("RPA_CONTINUOUS_FEED", true),
                entry(FlowFlag.NOTICE_OF_CHANGE.name(), false),
                entry(FlowFlag.SPEC_RPA_CONTINUOUS_FEED.name(), false)
            );
        }

        @Test
        void shouldReturnProceedsWithOfflineJourney_whenCaseTakenOfflineAfterClaimNotified() {
            CaseData caseData = CaseDataBuilderUnspec.builder().atStateTakenOfflineByStaffAfterClaimNotified().build();
            StateFlow stateFlow = stateFlowEngine.evaluate(caseData);
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
            verify(featureToggleService).isRpaContinuousFeedEnabled();
            assertThat(stateFlow.getFlags()).hasSize(4).contains(
                entry("ONE_RESPONDENT_REPRESENTATIVE", true),
                entry("RPA_CONTINUOUS_FEED", true),
                entry(FlowFlag.NOTICE_OF_CHANGE.name(), false),
                entry(FlowFlag.SPEC_RPA_CONTINUOUS_FEED.name(), false)
            );
        }

        @Test
        void shouldReturnProceedsWithOfflineJourney_whenCaseTakenOfflineAfterClaimDetailsNotified() {
            CaseData caseData = CaseDataBuilderUnspec.builder().atStateTakenOfflineByStaffAfterClaimDetailsNotified().build();
            StateFlow stateFlow = stateFlowEngine.evaluate(caseData);
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
            verify(featureToggleService).isRpaContinuousFeedEnabled();
            assertThat(stateFlow.getFlags()).hasSize(4).contains(
                entry("ONE_RESPONDENT_REPRESENTATIVE", true),
                entry("RPA_CONTINUOUS_FEED", true),
                entry(FlowFlag.NOTICE_OF_CHANGE.name(), false),
                entry(FlowFlag.SPEC_RPA_CONTINUOUS_FEED.name(), false)
            );
        }

        @Test
        void shouldReturnProceedsWithOfflineJourney_whenCaseTakenOfflineAfterClaimDetailsNotifiedExtension() {
            CaseData caseData = CaseDataBuilderUnspec.builder().atStateTakenOfflineByStaffAfterClaimDetailsNotifiedExtension()
                .build();
            StateFlow stateFlow = stateFlowEngine.evaluate(caseData);
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
            verify(featureToggleService).isRpaContinuousFeedEnabled();
            assertThat(stateFlow.getFlags()).hasSize(4).contains(
                entry("ONE_RESPONDENT_REPRESENTATIVE", true),
                entry("RPA_CONTINUOUS_FEED", true),
                entry(FlowFlag.NOTICE_OF_CHANGE.name(), false),
                entry(FlowFlag.SPEC_RPA_CONTINUOUS_FEED.name(), false)
            );
        }

        @Test
        void shouldReturnProceedsWithOfflineJourney_whenCaseTakenOfflineAfterNotificationAcknowledged() {
            CaseData caseData = CaseDataBuilderUnspec.builder().atStateTakenOfflineByStaffAfterNotificationAcknowledged()
                .build();
            StateFlow stateFlow = stateFlowEngine.evaluate(caseData);
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
            verify(featureToggleService).isRpaContinuousFeedEnabled();
            assertThat(stateFlow.getFlags()).hasSize(4).contains(
                entry("ONE_RESPONDENT_REPRESENTATIVE", true),
                entry("RPA_CONTINUOUS_FEED", true),
                entry(FlowFlag.NOTICE_OF_CHANGE.name(), false),
                entry(FlowFlag.SPEC_RPA_CONTINUOUS_FEED.name(), false)
            );
        }

        @Test
        void shouldReturnProceedsWithOfflineJourney_whenCaseTakenOfflineAfterNotificationAcknowledgeExtension() {
            CaseData caseData = CaseDataBuilderUnspec.builder()
                .atStateTakenOfflineByStaffAfterNotificationAcknowledgeExtension()
                .build();

            StateFlow stateFlow = stateFlowEngine.evaluate(caseData);
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
            verify(featureToggleService).isRpaContinuousFeedEnabled();
            assertThat(stateFlow.getFlags()).hasSize(4).contains(
                entry("ONE_RESPONDENT_REPRESENTATIVE", true),
                entry("RPA_CONTINUOUS_FEED", true),
                entry(FlowFlag.NOTICE_OF_CHANGE.name(), false),
                entry(FlowFlag.SPEC_RPA_CONTINUOUS_FEED.name(), false)
            );
        }

        @Test
        void shouldReturnProceedsWithOfflineJourney_whenCaseTakenOfflineAfterDefendantResponse() {
            CaseData caseData = CaseDataBuilderUnspec.builder().atStateTakenOfflineByStaffAfterDefendantResponse()
                .build();
            StateFlow stateFlow = stateFlowEngine.evaluate(caseData);
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
            verify(featureToggleService).isRpaContinuousFeedEnabled();
            assertThat(stateFlow.getFlags()).hasSize(4).contains(
                entry("ONE_RESPONDENT_REPRESENTATIVE", true),
                entry("RPA_CONTINUOUS_FEED", true),
                entry(FlowFlag.NOTICE_OF_CHANGE.name(), false),
                entry(FlowFlag.SPEC_RPA_CONTINUOUS_FEED.name(), false)
            );
        }

        @Test
        void shouldReturnProceedsWithOfflineJourney_whenCaseTakenOfflinePastClaimNotificationDeadline() {
            CaseData caseData = CaseDataBuilderUnspec.builder().atStateClaimDismissedPastClaimNotificationDeadline()
                .takenOfflineByStaffDate(LocalDateTime.now())
                .build();
            StateFlow stateFlow = stateFlowEngine.evaluate(caseData);
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
            verify(featureToggleService).isRpaContinuousFeedEnabled();
            assertThat(stateFlow.getFlags()).hasSize(4).contains(
                entry("ONE_RESPONDENT_REPRESENTATIVE", true),
                entry("RPA_CONTINUOUS_FEED", true),
                entry(FlowFlag.NOTICE_OF_CHANGE.name(), false),
                entry(FlowFlag.SPEC_RPA_CONTINUOUS_FEED.name(), false)
            );
        }

        @Test
        void shouldReturnProceedsWithOfflineJourney_whenCaseTakenOfflinePastClaimDetailsNotificationDeadline() {
            CaseData caseData = CaseDataBuilderUnspec.builder().atStateClaimDismissedPastClaimDetailsNotificationDeadline()
                .takenOfflineByStaffDate(LocalDateTime.now())
                .build();
            StateFlow stateFlow = stateFlowEngine.evaluate(caseData);
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
            verify(featureToggleService).isRpaContinuousFeedEnabled();
            assertThat(stateFlow.getFlags()).hasSize(4).contains(
                entry("ONE_RESPONDENT_REPRESENTATIVE", true),
                entry("RPA_CONTINUOUS_FEED", true),
                entry(FlowFlag.NOTICE_OF_CHANGE.name(), false),
                entry(FlowFlag.SPEC_RPA_CONTINUOUS_FEED.name(), false)
            );
        }
    }

    @Nested
    class ClaimDismissedPastClaimDismissedDeadline {

        @Test
        void shouldReturnAwaitingCamundaState_whenDeadlinePassedAfterStateClaimDetailsNotified() {
            CaseData caseData = CaseDataBuilderUnspec.builder().atStatePastClaimDismissedDeadline().build();
            if (caseData.getRespondent2OrgRegistered() != null
                && caseData.getRespondent2Represented() == null) {
                caseData = caseData.toBuilder()
                    .respondent2Represented(YES)
                    .build();
            }
            StateFlow stateFlow = stateFlowEngine.evaluate(caseData);
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
            CaseData caseData = CaseDataBuilderUnspec.builder().atStatePastClaimDismissedDeadline_1v2().build();
            if (caseData.getRespondent2OrgRegistered() != null
                && caseData.getRespondent2Represented() == null) {
                caseData = caseData.toBuilder()
                    .respondent2Represented(YES)
                    .build();
            }
            StateFlow stateFlow = stateFlowEngine.evaluate(caseData);
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
            CaseData caseData = CaseDataBuilderUnspec.builder().atStateClaimDismissed().build();
            StateFlow stateFlow = stateFlowEngine.evaluate(caseData);
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
            CaseData caseData = CaseDataBuilderUnspec.builder().atStateClaimDetailsNotifiedTimeExtension()
                .claimDismissedDeadline(LocalDateTime.now().minusDays(5))
                .build();
            if (caseData.getRespondent2OrgRegistered() != null
                && caseData.getRespondent2Represented() == null) {
                caseData = caseData.toBuilder()
                    .respondent2Represented(YES)
                    .build();
            }
            StateFlow stateFlow = stateFlowEngine.evaluate(caseData);
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
            verify(featureToggleService).isRpaContinuousFeedEnabled();
            assertThat(stateFlow.getFlags()).hasSize(4).contains(
                entry("ONE_RESPONDENT_REPRESENTATIVE", true),
                entry("RPA_CONTINUOUS_FEED", true),
                entry(FlowFlag.NOTICE_OF_CHANGE.name(), false),
                entry(FlowFlag.SPEC_RPA_CONTINUOUS_FEED.name(), false)
            );
        }

        @Test
        void shouldReturnDismissedState_whenDeadlinePassedAfterClaimDetailsNotifiedExtensionAndProcessedByCamunda() {
            CaseData caseData = CaseDataBuilderUnspec.builder().atStateClaimDetailsNotifiedTimeExtension()
                .claimDismissedDeadline(LocalDateTime.now().minusDays(5))
                .claimDismissedDate(LocalDateTime.now())
                .build();
            if (caseData.getRespondent2OrgRegistered() != null
                && caseData.getRespondent2Represented() == null) {
                caseData = caseData.toBuilder()
                    .respondent2Represented(YES)
                    .build();
            }
            StateFlow stateFlow = stateFlowEngine.evaluate(caseData);
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
            verify(featureToggleService).isRpaContinuousFeedEnabled();
            assertThat(stateFlow.getFlags())
                .hasSize(4)
                .contains(
                    entry("ONE_RESPONDENT_REPRESENTATIVE", true),
                    entry("RPA_CONTINUOUS_FEED", true),
                    entry(FlowFlag.NOTICE_OF_CHANGE.name(), false),
                    entry(FlowFlag.SPEC_RPA_CONTINUOUS_FEED.name(), false)
                );
        }

        @Test
        void shouldReturnClaimDismissedPastDeadline_whenDeadlinePassedAfterStateNotificationAcknowledged() {
            CaseData caseData = CaseDataBuilderUnspec.builder().atStateNotificationAcknowledged()
                .claimDismissedDeadline(LocalDateTime.now().minusDays(5))
                .build();
            if (caseData.getRespondent2OrgRegistered() != null
                && caseData.getRespondent2Represented() == null) {
                caseData = caseData.toBuilder()
                    .respondent2Represented(YES)
                    .build();
            }
            StateFlow stateFlow = stateFlowEngine.evaluate(caseData);
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
            verify(featureToggleService).isRpaContinuousFeedEnabled();
            assertThat(stateFlow.getFlags()).hasSize(4).contains(
                entry("ONE_RESPONDENT_REPRESENTATIVE", true),
                entry("RPA_CONTINUOUS_FEED", true),
                entry(FlowFlag.NOTICE_OF_CHANGE.name(), false),
                entry(FlowFlag.SPEC_RPA_CONTINUOUS_FEED.name(), false)
            );
        }

        @Test
        void shouldReturnDismissedState_whenDeadlinePassedAfterNotificationAcknowledgedAndProcessedByCamunda() {
            CaseData caseData = CaseDataBuilderUnspec.builder().atStateNotificationAcknowledged()
                .claimDismissedDeadline(LocalDateTime.now().minusDays(5))
                .claimDismissedDate(LocalDateTime.now())
                .build();
            StateFlow stateFlow = stateFlowEngine.evaluate(caseData);
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
            verify(featureToggleService).isRpaContinuousFeedEnabled();
            assertThat(stateFlow.getFlags()).hasSize(4).contains(
                entry("ONE_RESPONDENT_REPRESENTATIVE", true),
                entry("RPA_CONTINUOUS_FEED", true),
                entry(FlowFlag.NOTICE_OF_CHANGE.name(), false),
                entry(FlowFlag.SPEC_RPA_CONTINUOUS_FEED.name(), false)
            );
        }

        @Test
        void shouldReturnAwaitingCamundaState_whenDeadlinePassedAfterStateNotificationAcknowledgedTimeExtension() {
            CaseData caseData = CaseDataBuilderUnspec.builder().atStateNotificationAcknowledgedRespondent1TimeExtension()
                .claimDismissedDeadline(LocalDateTime.now().minusDays(5))
                .build();
            if (caseData.getRespondent2OrgRegistered() != null
                && caseData.getRespondent2Represented() == null) {
                caseData = caseData.toBuilder()
                    .respondent2Represented(YES)
                    .build();
            }
            StateFlow stateFlow = stateFlowEngine.evaluate(caseData);
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
            CaseData caseData = CaseDataBuilderUnspec.builder().atStateNotificationAcknowledgedRespondent1TimeExtension()
                .claimDismissedDeadline(LocalDateTime.now().minusDays(5))
                .claimDismissedDate(LocalDateTime.now())
                .build();
            if (caseData.getRespondent2OrgRegistered() != null
                && caseData.getRespondent2Represented() == null) {
                caseData = caseData.toBuilder()
                    .respondent2Represented(YES)
                    .build();
            }
            StateFlow stateFlow = stateFlowEngine.evaluate(caseData);
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
            CaseData caseData = CaseDataBuilderUnspec.builder().atStateClaimDismissed().build();

            StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

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
            verify(featureToggleService).isRpaContinuousFeedEnabled();
            assertThat(stateFlow.getFlags()).hasSize(4).contains(
                entry("ONE_RESPONDENT_REPRESENTATIVE", true),
                entry("RPA_CONTINUOUS_FEED", true),
                entry(FlowFlag.NOTICE_OF_CHANGE.name(), false),
                entry(FlowFlag.SPEC_RPA_CONTINUOUS_FEED.name(), false)
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
                CaseData caseData = CaseDataBuilderUnspec.builder()
                    .atStateRespondentFullDefence()
                    .multiPartyClaimTwoDefendantSolicitors()
                    .build();
                if (caseData.getRespondent2OrgRegistered() != null
                    && caseData.getRespondent2Represented() == null) {
                    caseData = caseData.toBuilder()
                        .respondent2Represented(YES)
                        .build();
                }
                StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

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
                verify(featureToggleService).isRpaContinuousFeedEnabled();
                assertThat(stateFlow.getFlags()).hasSize(5).contains(
                    entry("ONE_RESPONDENT_REPRESENTATIVE", false),
                    entry("RPA_CONTINUOUS_FEED", true),
                    entry("SPEC_RPA_CONTINUOUS_FEED", false),
                    entry("TWO_RESPONDENT_REPRESENTATIVES", true),
                    entry(FlowFlag.NOTICE_OF_CHANGE.name(), false)
                );
            }

            @Test
            //1v2 Different solicitor scenario-first response FullDefence received
            void shouldGenerateDQ_1v2DiffSol_whenFirstResponseIsNotFullDefence() {
                CaseData caseData = CaseDataBuilderUnspec.builder()
                    .atStateRespondentCounterClaim()
                    .multiPartyClaimTwoDefendantSolicitors()
                    .build();
                if (caseData.getRespondent2OrgRegistered() != null
                    && caseData.getRespondent2Represented() == null) {
                    caseData = caseData.toBuilder()
                        .respondent2Represented(YES)
                        .build();
                }
                StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

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
                verify(featureToggleService).isRpaContinuousFeedEnabled();
                assertThat(stateFlow.getFlags()).hasSize(5).contains(
                    entry("ONE_RESPONDENT_REPRESENTATIVE", false),
                    entry("RPA_CONTINUOUS_FEED", true),
                    entry("SPEC_RPA_CONTINUOUS_FEED", false),
                    entry("TWO_RESPONDENT_REPRESENTATIVES", true),
                    entry(FlowFlag.NOTICE_OF_CHANGE.name(), false)
                );
            }

            @Test
            //1v2 Different solicitor scenario-first response FullDefence received
            void shouldGenerateDQ_in1v2Scenario_whenFirstPartySubmitFullDefenceResponse() {
                CaseData caseData = CaseDataBuilderUnspec.builder()
                    .atStateRespondentFullDefence()
                    .multiPartyClaimTwoDefendantSolicitors()
                    .build();
                if (caseData.getRespondent2OrgRegistered() != null
                    && caseData.getRespondent2Represented() == null) {
                    caseData = caseData.toBuilder()
                        .respondent2Represented(YES)
                        .build();
                }

                StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

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
                verify(featureToggleService).isRpaContinuousFeedEnabled();
                assertThat(stateFlow.getFlags()).hasSize(5).contains(
                    entry("ONE_RESPONDENT_REPRESENTATIVE", false),
                    entry("RPA_CONTINUOUS_FEED", true),
                    entry("SPEC_RPA_CONTINUOUS_FEED", false),
                    entry("TWO_RESPONDENT_REPRESENTATIVES", true),
                    entry(FlowFlag.NOTICE_OF_CHANGE.name(), false)
                );
            }

            @Test
            //1v2 Different solicitor scenario-first party acknowledges, not responds
            // second party submits response FullDefence
            void shouldGenerateDQ_in1v2Scenario_whenSecondPartySubmitFullDefenceResponse() {
                CaseData caseData = CaseDataBuilderUnspec.builder()
                    .atStateRespondentFullDefenceRespondent2()
                    .multiPartyClaimTwoDefendantSolicitors()
                    .build();
                if (caseData.getRespondent2OrgRegistered() != null
                    && caseData.getRespondent2Represented() == null) {
                    caseData = caseData.toBuilder()
                        .respondent2Represented(YES)
                        .build();
                }
                StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

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
                verify(featureToggleService).isRpaContinuousFeedEnabled();
                assertThat(stateFlow.getFlags()).hasSize(5).contains(
                    entry("ONE_RESPONDENT_REPRESENTATIVE", false),
                    entry("RPA_CONTINUOUS_FEED", true),
                    entry("SPEC_RPA_CONTINUOUS_FEED", false),
                    entry("TWO_RESPONDENT_REPRESENTATIVES", true),
                    entry(FlowFlag.NOTICE_OF_CHANGE.name(), false)
                );
            }

            @Test
            //Respondent 1 submits FULL DEFENCE, Respondent 2 submits FULL DEFENCE
            void shouldReturnFullDefence_in1v2Scenario_whenBothPartiesSubmitFullDefenceResponses() {
                CaseData caseData = CaseDataBuilderUnspec.builder()
                    .atStateRespondentFullDefence_1v2_BothPartiesFullDefenceResponses()
                    .multiPartyClaimTwoDefendantSolicitors()
                    .build();
                if (caseData.getRespondent2OrgRegistered() != null
                    && caseData.getRespondent2Represented() == null) {
                    caseData = caseData.toBuilder()
                        .respondent2Represented(YES)
                        .build();
                }

                StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

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
                verify(featureToggleService).isRpaContinuousFeedEnabled();
                assertThat(stateFlow.getFlags()).hasSize(5).contains(
                    entry("ONE_RESPONDENT_REPRESENTATIVE", false),
                    entry("RPA_CONTINUOUS_FEED", true),
                    entry("SPEC_RPA_CONTINUOUS_FEED", false),
                    entry("TWO_RESPONDENT_REPRESENTATIVES", true),
                    entry(FlowFlag.NOTICE_OF_CHANGE.name(), false)
                );
            }

            @Test
            //Respondent 1 and 2 acknowledges claim, then submits  FULL DEFENCE
            void shouldReturnFullDefence_in1v2Scenario_whenBothPartiesAcknowledgedAndSubmitFullDefenceResponses() {
                CaseData caseData = CaseDataBuilderUnspec.builder()
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
                StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

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
                verify(featureToggleService).isRpaContinuousFeedEnabled();
                assertThat(stateFlow.getFlags()).hasSize(5).contains(
                    entry("ONE_RESPONDENT_REPRESENTATIVE", false),
                    entry("RPA_CONTINUOUS_FEED", true),
                    entry("SPEC_RPA_CONTINUOUS_FEED", false),
                    entry("TWO_RESPONDENT_REPRESENTATIVES", true),
                    entry(FlowFlag.NOTICE_OF_CHANGE.name(), false)
                );
            }

            @Test
            //Respondent 1 acknowledges claim, then Respondent 1 & 2 submits  FULL DEFENCE
            void shouldReturnFullDefence_in1v2Scenario_whenRep1AcknowledgedAndBothSubmitFullDefenceResponses() {
                CaseData caseData = CaseDataBuilderUnspec.builder()
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
                StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

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
                verify(featureToggleService).isRpaContinuousFeedEnabled();
                assertThat(stateFlow.getFlags()).hasSize(5).contains(
                    entry("ONE_RESPONDENT_REPRESENTATIVE", false),
                    entry("RPA_CONTINUOUS_FEED", true),
                    entry("SPEC_RPA_CONTINUOUS_FEED", false),
                    entry("TWO_RESPONDENT_REPRESENTATIVES", true),
                    entry(FlowFlag.NOTICE_OF_CHANGE.name(), false)
                );
            }

            @Test
            // Respondent 2 acknowledges claim, Respondent 1 & 2 submits  FULL DEFENCE
            void shouldReturnFullDefence_in1v2Scenario_whenRep2AcknowledgedAndBothSubmitFullDefenceResponses() {
                CaseData caseData = CaseDataBuilderUnspec.builder()
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

                StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

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
                verify(featureToggleService).isRpaContinuousFeedEnabled();
                assertThat(stateFlow.getFlags()).hasSize(5).contains(
                    entry("ONE_RESPONDENT_REPRESENTATIVE", false),
                    entry("RPA_CONTINUOUS_FEED", true),
                    entry("SPEC_RPA_CONTINUOUS_FEED", false),
                    entry("TWO_RESPONDENT_REPRESENTATIVES", true),
                    entry(FlowFlag.NOTICE_OF_CHANGE.name(), false)
                );
            }

            @Test
            //Respondent 1 submits FULL DEFENCE, Respondent 2 submits COUNTER CLAIM
            void shouldReturnDivergentResponseAndGoOffline_1v2Scenario_whenFirstRespondentSubmitsFullDefenceResponse() {
                CaseData caseData = CaseDataBuilderUnspec.builder()
                    .atStateRespondentFullDefence_1v2_Resp1FullDefenceAndResp2CounterClaim()
                    .multiPartyClaimTwoDefendantSolicitors()
                    .build();
                if (caseData.getRespondent2OrgRegistered() != null
                    && caseData.getRespondent2Represented() == null) {
                    caseData = caseData.toBuilder()
                        .respondent2Represented(YES)
                        .build();
                }
                StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

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
                verify(featureToggleService).isRpaContinuousFeedEnabled();
                assertThat(stateFlow.getFlags()).hasSize(5).contains(
                    entry("ONE_RESPONDENT_REPRESENTATIVE", false),
                    entry("RPA_CONTINUOUS_FEED", true),
                    entry("SPEC_RPA_CONTINUOUS_FEED", false),
                    entry("TWO_RESPONDENT_REPRESENTATIVES", true),
                    entry(FlowFlag.NOTICE_OF_CHANGE.name(), false)
                );
            }

            @Test
            //Respondent 1 submits FULL DEFENCE, Respondent 2 submits COUNTER CLAIM
            void shouldReturnDivergentResponse_in1v2SameSolicitorScenario_whenOneRespondentSubmitsFullDefence() {
                CaseData caseData = CaseDataBuilderUnspec.builder()
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
                StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

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
                verify(featureToggleService).isRpaContinuousFeedEnabled();
                assertThat(stateFlow.getFlags()).hasSize(4).contains(
                    entry("ONE_RESPONDENT_REPRESENTATIVE", true),
                    entry("SPEC_RPA_CONTINUOUS_FEED", false),
                    entry("RPA_CONTINUOUS_FEED", true),
                    entry(FlowFlag.NOTICE_OF_CHANGE.name(), false)
                );
            }

            //Respondent 1 submits ADMITS PART, Respondent 2 submits COUNTER CLAIM
            @Test
            void shouldReturnDivergentResponse_in1v2Scenario_whenNeitherRespondentSubmitsFullDefenceResponse() {
                CaseData caseData = CaseDataBuilderUnspec.builder()
                    .atStateDivergentResponse_1v2_Resp1FullAdmissionAndResp2CounterClaim()
                    .multiPartyClaimTwoDefendantSolicitors()
                    .build();
                if (caseData.getRespondent2OrgRegistered() != null
                    && caseData.getRespondent2Represented() == null) {
                    caseData = caseData.toBuilder()
                        .respondent2Represented(YES)
                        .build();
                }
                StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

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
                verify(featureToggleService).isRpaContinuousFeedEnabled();
                assertThat(stateFlow.getFlags()).hasSize(5).contains(
                    entry("ONE_RESPONDENT_REPRESENTATIVE", false),
                    entry("RPA_CONTINUOUS_FEED", true),
                    entry("SPEC_RPA_CONTINUOUS_FEED", false),
                    entry("TWO_RESPONDENT_REPRESENTATIVES", true),
                    entry(FlowFlag.NOTICE_OF_CHANGE.name(), false)
                );
            }

            //Respondent 1 submits ADMITS PART, Respondent 2 submits ADMITS PART
            @Test
            void shouldReturnAdmitsPartResponse_in1v2Scenario_whenBothRespondentsSubmitAdmitPartResponses() {
                CaseData caseData = CaseDataBuilderUnspec.builder()
                    .atStateFullAdmission_1v2_BothRespondentSolicitorsSubmitFullAdmissionResponse()
                    .multiPartyClaimTwoDefendantSolicitors()
                    .build();
                if (caseData.getRespondent2OrgRegistered() != null
                    && caseData.getRespondent2Represented() == null) {
                    caseData = caseData.toBuilder()
                        .respondent2Represented(YES)
                        .build();
                }

                StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

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
                verify(featureToggleService).isRpaContinuousFeedEnabled();
                assertThat(stateFlow.getFlags()).hasSize(5).contains(
                    entry("ONE_RESPONDENT_REPRESENTATIVE", false),
                    entry("RPA_CONTINUOUS_FEED", true),
                    entry("SPEC_RPA_CONTINUOUS_FEED", false),
                    entry("TWO_RESPONDENT_REPRESENTATIVES", true),
                    entry(FlowFlag.NOTICE_OF_CHANGE.name(), false)
                );
            }
        }
    }

    @Nested
    class AmbiguousErrors {

        @Test
        void claimIssue_fullAdmitAndDivergentRespondGoOffline() {
            CaseData caseData = CaseData.builder()
                .superClaimType(SuperClaimType.SPEC_CLAIM)
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
        public void claim1v1_reachFullAdmitProceed() {
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
        public void claim1v1_reachFullAdmitNoProceed() {
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
                .superClaimType(SuperClaimType.SPEC_CLAIM)
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
            CaseData caseData = CaseData.builder()
                // spec claim
                .superClaimType(SuperClaimType.SPEC_CLAIM)
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

            StateFlow fullState = stateFlowEngine.evaluate(caseData);
            Assertions.assertEquals(fullState.getState().getName(), FULL_DEFENCE.fullName());

            StateFlow newState = stateFlowEngine.evaluate(caseData.toBuilder()
                                                              .applicant1ProceedWithClaim(YES)
                                                              .build());

            Assertions.assertEquals(newState.getState().getName(), FULL_DEFENCE_PROCEED.fullName());
            Assertions.assertNull(newState.getFlags().get(FlowFlag.AGREED_TO_MEDIATION.name()));
        }

        @Test
        void fullDefencePartialMediationSpec() {
            CaseData caseData = CaseData.builder()
                // spec claim
                .superClaimType(SuperClaimType.SPEC_CLAIM)
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

            StateFlow fullState = stateFlowEngine.evaluate(caseData);
            Assertions.assertEquals(fullState.getState().getName(), FULL_DEFENCE.fullName());

            StateFlow newState = stateFlowEngine.evaluate(caseData.toBuilder()
                                                              .applicant1ProceedWithClaim(YES)
                                                              .applicant1ClaimMediationSpecRequired(
                                                                  SmallClaimMedicalLRspec.builder()
                                                                      .hasAgreedFreeMediation(NO)
                                                                      .build()
                                                              )
                                                              .build());

            Assertions.assertEquals(newState.getState().getName(), FULL_DEFENCE_PROCEED.fullName());
            Assertions.assertNull(newState.getFlags().get(FlowFlag.AGREED_TO_MEDIATION.name()));
        }

        @Test
        void fullDefenceAllMediationSpec() {
            CaseData caseData = CaseData.builder()
                // spec claim
                .superClaimType(SuperClaimType.SPEC_CLAIM)
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

            StateFlow fullState = stateFlowEngine.evaluate(caseData);
            Assertions.assertEquals(fullState.getState().getName(), FULL_DEFENCE.fullName());

            StateFlow newState = stateFlowEngine.evaluate(caseData.toBuilder()
                                                              .applicant1ProceedWithClaim(YES)
                                                              .applicant1ClaimMediationSpecRequired(
                                                                  SmallClaimMedicalLRspec.builder()
                                                                      .hasAgreedFreeMediation(YES)
                                                                      .build()
                                                              )
                                                              .build());

            Assertions.assertEquals(newState.getState().getName(), FULL_DEFENCE_PROCEED.fullName());
            Assertions.assertEquals(newState.getFlags().get(FlowFlag.AGREED_TO_MEDIATION.name()), Boolean.TRUE);
        }
    }
}
