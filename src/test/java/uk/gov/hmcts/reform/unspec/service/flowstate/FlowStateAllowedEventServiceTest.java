package uk.gov.hmcts.reform.unspec.service.flowstate;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.unspec.callback.CaseEvent;
import uk.gov.hmcts.reform.unspec.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.unspec.model.CaseData;
import uk.gov.hmcts.reform.unspec.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.unspec.sampledata.CaseDetailsBuilder;

import java.util.Arrays;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.of;
import static uk.gov.hmcts.reform.unspec.callback.CaseEvent.ACKNOWLEDGE_CLAIM;
import static uk.gov.hmcts.reform.unspec.callback.CaseEvent.ADD_DEFENDANT_LITIGATION_FRIEND;
import static uk.gov.hmcts.reform.unspec.callback.CaseEvent.ADD_OR_AMEND_CLAIM_DOCUMENTS;
import static uk.gov.hmcts.reform.unspec.callback.CaseEvent.AMEND_PARTY_DETAILS;
import static uk.gov.hmcts.reform.unspec.callback.CaseEvent.CASE_PROCEEDS_IN_CASEMAN;
import static uk.gov.hmcts.reform.unspec.callback.CaseEvent.CLAIMANT_RESPONSE;
import static uk.gov.hmcts.reform.unspec.callback.CaseEvent.CREATE_CLAIM;
import static uk.gov.hmcts.reform.unspec.callback.CaseEvent.DEFENDANT_RESPONSE;
import static uk.gov.hmcts.reform.unspec.callback.CaseEvent.DISCONTINUE_CLAIM;
import static uk.gov.hmcts.reform.unspec.callback.CaseEvent.DISMISS_CLAIM;
import static uk.gov.hmcts.reform.unspec.callback.CaseEvent.INFORM_AGREED_EXTENSION_DATE;
import static uk.gov.hmcts.reform.unspec.callback.CaseEvent.NOTIFY_DEFENDANT_OF_CLAIM;
import static uk.gov.hmcts.reform.unspec.callback.CaseEvent.NOTIFY_DEFENDANT_OF_CLAIM_DETAILS;
import static uk.gov.hmcts.reform.unspec.callback.CaseEvent.RESUBMIT_CLAIM;
import static uk.gov.hmcts.reform.unspec.callback.CaseEvent.TAKE_CASE_OFFLINE;
import static uk.gov.hmcts.reform.unspec.callback.CaseEvent.WITHDRAW_CLAIM;
import static uk.gov.hmcts.reform.unspec.service.flowstate.FlowState.Main.CLAIM_DETAILS_NOTIFIED;
import static uk.gov.hmcts.reform.unspec.service.flowstate.FlowState.Main.CLAIM_DETAILS_NOTIFIED_TIME_EXTENSION;
import static uk.gov.hmcts.reform.unspec.service.flowstate.FlowState.Main.CLAIM_ISSUED;
import static uk.gov.hmcts.reform.unspec.service.flowstate.FlowState.Main.CLAIM_ISSUED_PAYMENT_FAILED;
import static uk.gov.hmcts.reform.unspec.service.flowstate.FlowState.Main.CLAIM_NOTIFIED;
import static uk.gov.hmcts.reform.unspec.service.flowstate.FlowState.Main.COUNTER_CLAIM;
import static uk.gov.hmcts.reform.unspec.service.flowstate.FlowState.Main.DRAFT;
import static uk.gov.hmcts.reform.unspec.service.flowstate.FlowState.Main.FULL_ADMISSION;
import static uk.gov.hmcts.reform.unspec.service.flowstate.FlowState.Main.FULL_DEFENCE;
import static uk.gov.hmcts.reform.unspec.service.flowstate.FlowState.Main.FULL_DEFENCE_NOT_PROCEED;
import static uk.gov.hmcts.reform.unspec.service.flowstate.FlowState.Main.FULL_DEFENCE_PROCEED;
import static uk.gov.hmcts.reform.unspec.service.flowstate.FlowState.Main.NOTIFICATION_ACKNOWLEDGED;
import static uk.gov.hmcts.reform.unspec.service.flowstate.FlowState.Main.NOTIFICATION_ACKNOWLEDGED_TIME_EXTENSION;
import static uk.gov.hmcts.reform.unspec.service.flowstate.FlowState.Main.PART_ADMISSION;
import static uk.gov.hmcts.reform.unspec.service.flowstate.FlowState.Main.PENDING_CLAIM_ISSUED;

@SpringBootTest(classes = {
    JacksonAutoConfiguration.class,
    CaseDetailsConverter.class,
    StateFlowEngine.class,
    FlowStateAllowedEventService.class
})
class FlowStateAllowedEventServiceTest {

    @Autowired
    FlowStateAllowedEventService flowStateAllowedEventService;

    static class GetFlowStateArguments implements ArgumentsProvider {

        @Override
        @SneakyThrows
        public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
            return Stream.of(
                of(CaseDataBuilder.builder().atStateClaimDraft().build(), DRAFT),
                of(CaseDataBuilder.builder().atStatePaymentFailed().build(), CLAIM_ISSUED_PAYMENT_FAILED),
                of(CaseDataBuilder.builder().atStatePendingClaimIssued().build(), PENDING_CLAIM_ISSUED),
                of(
                    CaseDataBuilder.builder().atStateClaimNotified().build(),
                    CLAIM_NOTIFIED
                ),
                of(CaseDataBuilder.builder().atStateClaimDetailsNotified().build(), CLAIM_DETAILS_NOTIFIED),
                of(CaseDataBuilder.builder().atStateNotificationAcknowledged().build(), NOTIFICATION_ACKNOWLEDGED),
                of(CaseDataBuilder.builder().atStateRespondentFullDefence().build(), FULL_DEFENCE),
                of(CaseDataBuilder.builder().atStateRespondentFullAdmission().build(), FULL_ADMISSION),
                of(CaseDataBuilder.builder().atStateRespondentPartAdmission().build(), PART_ADMISSION),
                of(CaseDataBuilder.builder().atStateRespondentCounterClaim().build(), COUNTER_CLAIM),
                of(
                    CaseDataBuilder.builder().atStateClaimDetailsNotifiedTimeExtension().build(),
                    CLAIM_DETAILS_NOTIFIED_TIME_EXTENSION
                )
            );
        }
    }

    @Nested
    class GetFlowState {

        @ParameterizedTest(name = "{index} => should return flow state {1} when case data {0}")
        @ArgumentsSource(GetFlowStateArguments.class)
        void shouldReturnValidState_whenCaseDataProvided(CaseData caseData, FlowState.Main flowState) {
            assertThat(flowStateAllowedEventService.getFlowState(caseData))
                .isEqualTo(flowState);
        }
    }

    static class GetAllowedCaseEventForFlowStateArguments implements ArgumentsProvider {

        @Override
        @SneakyThrows
        public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
            return Stream.of(
                of(
                    DRAFT,
                    new CaseEvent[] {
                        CREATE_CLAIM
                    }
                ),
                of(
                    CLAIM_ISSUED_PAYMENT_FAILED,
                    new CaseEvent[] {
                        RESUBMIT_CLAIM,
                        WITHDRAW_CLAIM,
                        DISCONTINUE_CLAIM,
                        AMEND_PARTY_DETAILS
                    }
                ),
                of(
                    CLAIM_ISSUED,
                    new CaseEvent[] {
                        NOTIFY_DEFENDANT_OF_CLAIM,
                        ADD_DEFENDANT_LITIGATION_FRIEND,
                        CASE_PROCEEDS_IN_CASEMAN,
                        ADD_OR_AMEND_CLAIM_DOCUMENTS,
                        AMEND_PARTY_DETAILS,
                        DISMISS_CLAIM,
                        DISCONTINUE_CLAIM,
                        WITHDRAW_CLAIM
                    }
                ),
                of(
                    CLAIM_NOTIFIED,
                    new CaseEvent[] {
                        NOTIFY_DEFENDANT_OF_CLAIM_DETAILS,
                        ADD_DEFENDANT_LITIGATION_FRIEND,
                        CASE_PROCEEDS_IN_CASEMAN,
                        ADD_OR_AMEND_CLAIM_DOCUMENTS,
                        AMEND_PARTY_DETAILS,
                        DISMISS_CLAIM,
                        DISCONTINUE_CLAIM,
                        WITHDRAW_CLAIM
                    }
                ),
                of(
                    CLAIM_DETAILS_NOTIFIED,
                    new CaseEvent[] {
                        ACKNOWLEDGE_CLAIM,
                        DEFENDANT_RESPONSE,
                        INFORM_AGREED_EXTENSION_DATE,
                        ADD_DEFENDANT_LITIGATION_FRIEND,
                        WITHDRAW_CLAIM,
                        DISCONTINUE_CLAIM,
                        AMEND_PARTY_DETAILS,
                        CASE_PROCEEDS_IN_CASEMAN,
                        DISMISS_CLAIM
                    }
                ),
                of(
                    CLAIM_DETAILS_NOTIFIED_TIME_EXTENSION,
                    new CaseEvent[] {
                        ACKNOWLEDGE_CLAIM,
                        DEFENDANT_RESPONSE,
                        ADD_DEFENDANT_LITIGATION_FRIEND,
                        WITHDRAW_CLAIM,
                        DISCONTINUE_CLAIM,
                        CASE_PROCEEDS_IN_CASEMAN,
                        AMEND_PARTY_DETAILS,
                        DISMISS_CLAIM
                    }
                ),
                of(
                    NOTIFICATION_ACKNOWLEDGED,
                    new CaseEvent[] {
                        DEFENDANT_RESPONSE,
                        ADD_DEFENDANT_LITIGATION_FRIEND,
                        WITHDRAW_CLAIM,
                        DISCONTINUE_CLAIM,
                        CASE_PROCEEDS_IN_CASEMAN,
                        INFORM_AGREED_EXTENSION_DATE,
                        AMEND_PARTY_DETAILS,
                        DISMISS_CLAIM
                    }
                ),
                of(
                    NOTIFICATION_ACKNOWLEDGED_TIME_EXTENSION,
                    new CaseEvent[] {
                        DEFENDANT_RESPONSE,
                        ADD_DEFENDANT_LITIGATION_FRIEND,
                        WITHDRAW_CLAIM,
                        DISCONTINUE_CLAIM,
                        CASE_PROCEEDS_IN_CASEMAN,
                        AMEND_PARTY_DETAILS,
                        DISMISS_CLAIM
                    }
                ),
                of(
                    FULL_DEFENCE,
                    new CaseEvent[] {
                        CLAIMANT_RESPONSE,
                        WITHDRAW_CLAIM,
                        ADD_DEFENDANT_LITIGATION_FRIEND,
                        DISCONTINUE_CLAIM,
                        CASE_PROCEEDS_IN_CASEMAN,
                        AMEND_PARTY_DETAILS,
                        TAKE_CASE_OFFLINE
                    }
                ),
                of(
                    FULL_ADMISSION,
                    new CaseEvent[] {
                        WITHDRAW_CLAIM,
                        ADD_DEFENDANT_LITIGATION_FRIEND,
                        DISCONTINUE_CLAIM,
                        CASE_PROCEEDS_IN_CASEMAN,
                        AMEND_PARTY_DETAILS
                    }
                ),
                of(
                    PART_ADMISSION,
                    new CaseEvent[] {
                        WITHDRAW_CLAIM,
                        ADD_DEFENDANT_LITIGATION_FRIEND,
                        DISCONTINUE_CLAIM,
                        CASE_PROCEEDS_IN_CASEMAN,
                        AMEND_PARTY_DETAILS
                    }
                ),
                of(
                    COUNTER_CLAIM,
                    new CaseEvent[] {
                        WITHDRAW_CLAIM,
                        ADD_DEFENDANT_LITIGATION_FRIEND,
                        DISCONTINUE_CLAIM,
                        CASE_PROCEEDS_IN_CASEMAN,
                        AMEND_PARTY_DETAILS
                    }
                ),
                of(
                    FULL_DEFENCE_PROCEED,
                    new CaseEvent[] {
                        ADD_DEFENDANT_LITIGATION_FRIEND,
                        WITHDRAW_CLAIM,
                        DISCONTINUE_CLAIM,
                        CASE_PROCEEDS_IN_CASEMAN,
                        AMEND_PARTY_DETAILS
                    }
                ),
                of(
                    FULL_DEFENCE_NOT_PROCEED,
                    new CaseEvent[] {
                        ADD_DEFENDANT_LITIGATION_FRIEND,
                        WITHDRAW_CLAIM,
                        DISCONTINUE_CLAIM,
                        CASE_PROCEEDS_IN_CASEMAN,
                        AMEND_PARTY_DETAILS
                    }
                )
            );
        }
    }

    @Nested
    class GetAllowedEventsForFlowState {

        @ParameterizedTest
        @ArgumentsSource(GetAllowedCaseEventForFlowStateArguments.class)
        void shouldReturnValidEvents_whenFlowStateIsProvided(FlowState.Main flowState, CaseEvent... caseEvents) {
            assertThat(flowStateAllowedEventService.getAllowedEvents(flowState.fullName()))
                .containsExactlyInAnyOrder(caseEvents);
        }
    }

    @Nested
    class IsEventAllowedOnFlowState {

        @ParameterizedTest
        @ArgumentsSource(GetAllowedCaseEventForFlowStateArguments.class)
        void shouldReturnTrue_whenEventIsAllowedAtGivenState(FlowState.Main flowState, CaseEvent... caseEvents) {
            Arrays.stream(caseEvents).forEach(caseEvent ->
                                                  assertTrue(flowStateAllowedEventService.isAllowedOnState(
                                                      flowState.fullName(),
                                                      caseEvent
                                                  ))
            );
        }

        @ParameterizedTest
        @CsvSource({
            "DRAFT, CASE_PROCEEDS_IN_CASEMAN",
            "CLAIM_ISSUED, NOTIFY_DEFENDANT_OF_CLAIM_DETAILS",
            "CLAIM_NOTIFIED, ACKNOWLEDGE_CLAIM",
            "FULL_DEFENCE_PROCEED, ACKNOWLEDGE_CLAIM",
            "CLAIM_NOTIFIED, INFORM_AGREED_EXTENSION_DATE"
        })
        void shouldReturnFalse_whenEventIsNotAllowedAtGivenState(FlowState.Main flowState, CaseEvent caseEvent) {
            assertFalse(flowStateAllowedEventService.isAllowedOnState(flowState.fullName(), caseEvent));
        }
    }

    static class GetAllowedStatesForCaseEventArguments implements ArgumentsProvider {

        @Override
        @SneakyThrows
        public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
            return Stream.of(
                of(CREATE_CLAIM, new String[] {DRAFT.fullName()}),
                of(RESUBMIT_CLAIM, new String[] {CLAIM_ISSUED_PAYMENT_FAILED.fullName()}),
                of(ACKNOWLEDGE_CLAIM, new String[] {CLAIM_DETAILS_NOTIFIED.fullName(),
                    CLAIM_DETAILS_NOTIFIED_TIME_EXTENSION.fullName()}),
                of(NOTIFY_DEFENDANT_OF_CLAIM, new String[] {CLAIM_ISSUED.fullName()}),
                of(CLAIMANT_RESPONSE, new String[] {FULL_DEFENCE.fullName()}),
                of(
                    DEFENDANT_RESPONSE,
                    new String[] {NOTIFICATION_ACKNOWLEDGED.fullName(),
                        CLAIM_DETAILS_NOTIFIED.fullName(),
                        CLAIM_DETAILS_NOTIFIED_TIME_EXTENSION.fullName(),
                        NOTIFICATION_ACKNOWLEDGED_TIME_EXTENSION.fullName()
                    }
                ),
                of(
                    WITHDRAW_CLAIM,
                    new String[] {CLAIM_ISSUED.fullName(), CLAIM_NOTIFIED.fullName(), CLAIM_DETAILS_NOTIFIED.fullName(),
                        NOTIFICATION_ACKNOWLEDGED.fullName(), CLAIM_ISSUED_PAYMENT_FAILED.fullName(),
                        FULL_DEFENCE.fullName(), FULL_ADMISSION.fullName(),
                        PART_ADMISSION.fullName(), COUNTER_CLAIM.fullName(),
                        FULL_DEFENCE_PROCEED.fullName(), FULL_DEFENCE_NOT_PROCEED.fullName(),
                        CLAIM_DETAILS_NOTIFIED_TIME_EXTENSION.fullName(),
                        NOTIFICATION_ACKNOWLEDGED_TIME_EXTENSION.fullName()
                    }
                ),
                of(
                    DISCONTINUE_CLAIM,
                    new String[] {CLAIM_ISSUED.fullName(), CLAIM_NOTIFIED.fullName(), CLAIM_DETAILS_NOTIFIED.fullName(),
                        NOTIFICATION_ACKNOWLEDGED.fullName(), CLAIM_ISSUED_PAYMENT_FAILED.fullName(),
                        FULL_DEFENCE.fullName(), FULL_ADMISSION.fullName(),
                        PART_ADMISSION.fullName(), COUNTER_CLAIM.fullName(),
                        FULL_DEFENCE_PROCEED.fullName(), FULL_DEFENCE_NOT_PROCEED.fullName(),
                        CLAIM_DETAILS_NOTIFIED_TIME_EXTENSION.fullName(),
                        NOTIFICATION_ACKNOWLEDGED_TIME_EXTENSION.fullName()
                    }
                ),
                of(
                    CASE_PROCEEDS_IN_CASEMAN,
                    new String[] {CLAIM_ISSUED.fullName(), CLAIM_NOTIFIED.fullName(),
                        CLAIM_DETAILS_NOTIFIED.fullName(), NOTIFICATION_ACKNOWLEDGED.fullName(),
                        FULL_DEFENCE.fullName(), FULL_ADMISSION.fullName(),
                        PART_ADMISSION.fullName(), COUNTER_CLAIM.fullName(),
                        FULL_DEFENCE_PROCEED.fullName(), FULL_DEFENCE_NOT_PROCEED.fullName(),
                        CLAIM_DETAILS_NOTIFIED_TIME_EXTENSION.fullName(),
                        NOTIFICATION_ACKNOWLEDGED_TIME_EXTENSION.fullName()
                    }
                ),
                of(
                    ADD_DEFENDANT_LITIGATION_FRIEND,
                    new String[] {CLAIM_ISSUED.fullName(), CLAIM_NOTIFIED.fullName(),
                        CLAIM_DETAILS_NOTIFIED.fullName(), NOTIFICATION_ACKNOWLEDGED.fullName(),
                        FULL_DEFENCE.fullName(), FULL_ADMISSION.fullName(),
                        PART_ADMISSION.fullName(), COUNTER_CLAIM.fullName(),
                        FULL_DEFENCE_PROCEED.fullName(), FULL_DEFENCE_NOT_PROCEED.fullName(),
                        CLAIM_DETAILS_NOTIFIED_TIME_EXTENSION.fullName(),
                        NOTIFICATION_ACKNOWLEDGED_TIME_EXTENSION.fullName()
                    }
                ),
                of(
                    ADD_OR_AMEND_CLAIM_DOCUMENTS,
                    new String[] {CLAIM_ISSUED.fullName(), CLAIM_NOTIFIED.fullName()}
                ),
                of(NOTIFY_DEFENDANT_OF_CLAIM_DETAILS, new String[] {CLAIM_NOTIFIED.fullName()}),
                of(INFORM_AGREED_EXTENSION_DATE, new String[] {CLAIM_DETAILS_NOTIFIED.fullName(),
                    NOTIFICATION_ACKNOWLEDGED.fullName()}),
                of(
                    AMEND_PARTY_DETAILS,
                    new String[] {CLAIM_ISSUED.fullName(), CLAIM_ISSUED_PAYMENT_FAILED.fullName(),
                        CLAIM_NOTIFIED.fullName(), CLAIM_DETAILS_NOTIFIED.fullName(),
                        CLAIM_DETAILS_NOTIFIED_TIME_EXTENSION.fullName(), NOTIFICATION_ACKNOWLEDGED.fullName(),
                        FULL_DEFENCE.fullName(), FULL_ADMISSION.fullName(),
                        PART_ADMISSION.fullName(), COUNTER_CLAIM.fullName(),
                        FULL_DEFENCE_PROCEED.fullName(), FULL_DEFENCE_NOT_PROCEED.fullName(),
                        NOTIFICATION_ACKNOWLEDGED_TIME_EXTENSION.fullName()
                    }
                )
            );
        }
    }

    @Nested
    class GetAllowedStatesForCaseEvent {

        @ParameterizedTest
        @ArgumentsSource(GetAllowedStatesForCaseEventArguments.class)
        void shouldReturnValidStates_whenCaseEventIsGiven(CaseEvent caseEvent, String... flowStates) {
            assertThat(flowStateAllowedEventService.getAllowedStates(caseEvent))
                .containsExactlyInAnyOrder(flowStates);
        }
    }

    static class GetAllowedStatesForCaseDetailsArguments implements ArgumentsProvider {

        @Override
        @SneakyThrows
        public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
            return Stream.of(
                of(
                    true,
                    CaseDetailsBuilder.builder().atStateClaimAcknowledge().build(),
                    INFORM_AGREED_EXTENSION_DATE
                ),
                of(true, CaseDetailsBuilder.builder().atStateClaimAcknowledge().build(), WITHDRAW_CLAIM),
                of(true, CaseDetailsBuilder.builder().atStateClaimAcknowledge().build(), DEFENDANT_RESPONSE),
                of(true, CaseDetailsBuilder.builder().atStateClaimAcknowledge().build(), DISCONTINUE_CLAIM),
                of(true, CaseDetailsBuilder.builder().atStateClaimAcknowledge().build(), CASE_PROCEEDS_IN_CASEMAN),
                of(false, CaseDetailsBuilder.builder().atStateClaimAcknowledge().build(), CREATE_CLAIM),
                of(false, CaseDetailsBuilder.builder().atStateClaimAcknowledge().build(), CLAIMANT_RESPONSE),
                of(
                    true,
                    CaseDetailsBuilder.builder().atStateCaseIssued().build(),
                    NOTIFY_DEFENDANT_OF_CLAIM
                ),
                of(
                    true,
                    CaseDetailsBuilder.builder().atStateCaseIssued().build(),
                    ADD_DEFENDANT_LITIGATION_FRIEND
                ),
                of(
                    true,
                    CaseDetailsBuilder.builder().atStateCaseIssued().build(),
                    CASE_PROCEEDS_IN_CASEMAN
                ),
                of(
                    true,
                    CaseDetailsBuilder.builder().atStateCaseIssued().build(),
                    ADD_OR_AMEND_CLAIM_DOCUMENTS
                ),
                of(
                    false,
                    CaseDetailsBuilder.builder().atStateCaseIssued().build(),
                    NOTIFY_DEFENDANT_OF_CLAIM_DETAILS
                ),
                of(
                    true,
                    CaseDetailsBuilder.builder().atStateAwaitingCaseDetailsNotification().build(),
                    NOTIFY_DEFENDANT_OF_CLAIM_DETAILS
                ),
                of(
                    true,
                    CaseDetailsBuilder.builder().atStateAwaitingCaseDetailsNotification().build(),
                    ADD_DEFENDANT_LITIGATION_FRIEND
                ),
                of(
                    true,
                    CaseDetailsBuilder.builder().atStateAwaitingCaseDetailsNotification().build(),
                    CASE_PROCEEDS_IN_CASEMAN
                ),
                of(
                    true,
                    CaseDetailsBuilder.builder().atStateAwaitingCaseDetailsNotification().build(),
                    ADD_OR_AMEND_CLAIM_DOCUMENTS
                ),
                of(
                    false,
                    CaseDetailsBuilder.builder().atStateAwaitingCaseDetailsNotification().build(),
                    ACKNOWLEDGE_CLAIM
                ),
                of(false, CaseDetailsBuilder.builder().atStateProceedsOffline().build(), AMEND_PARTY_DETAILS),
                of(true, CaseDetailsBuilder.builder().atStateAwaitingRespondentAcknowledgement().build(),
                   AMEND_PARTY_DETAILS)
            );
        }
    }

    @Nested
    class IsEventAllowedOnCaseDetails {

        @ParameterizedTest
        @ArgumentsSource(GetAllowedStatesForCaseDetailsArguments.class)
        void shouldReturnValidStates_whenCaseEventIsGiven(
            boolean expected,
            CaseDetails caseDetails,
            CaseEvent caseEvent
        ) {
            assertThat(flowStateAllowedEventService.isAllowed(caseDetails, caseEvent))
                .isEqualTo(expected);
        }
    }
}
