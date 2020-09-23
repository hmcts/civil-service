package uk.gov.hmcts.reform.unspec.service.flowstate;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.unspec.callback.CaseEvent;
import uk.gov.hmcts.reform.unspec.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.unspec.model.CaseData;
import uk.gov.hmcts.reform.unspec.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.unspec.sampledata.CaseDetailsBuilder;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.reform.unspec.callback.CaseEvent.ACKNOWLEDGE_SERVICE;
import static uk.gov.hmcts.reform.unspec.callback.CaseEvent.CLAIMANT_RESPONSE;
import static uk.gov.hmcts.reform.unspec.callback.CaseEvent.CONFIRM_SERVICE;
import static uk.gov.hmcts.reform.unspec.callback.CaseEvent.CREATE_CASE;
import static uk.gov.hmcts.reform.unspec.callback.CaseEvent.DEFENDANT_RESPONSE;
import static uk.gov.hmcts.reform.unspec.callback.CaseEvent.DISCONTINUE_CLAIM;
import static uk.gov.hmcts.reform.unspec.callback.CaseEvent.MOVE_TO_STAYED;
import static uk.gov.hmcts.reform.unspec.callback.CaseEvent.REQUEST_EXTENSION;
import static uk.gov.hmcts.reform.unspec.callback.CaseEvent.RESPOND_EXTENSION;
import static uk.gov.hmcts.reform.unspec.callback.CaseEvent.WITHDRAW_CLAIM;
import static uk.gov.hmcts.reform.unspec.service.flowstate.FlowState.Main.CLAIM_ISSUED;
import static uk.gov.hmcts.reform.unspec.service.flowstate.FlowState.Main.CLAIM_STAYED;
import static uk.gov.hmcts.reform.unspec.service.flowstate.FlowState.Main.DRAFT;
import static uk.gov.hmcts.reform.unspec.service.flowstate.FlowState.Main.EXTENSION_REQUESTED;
import static uk.gov.hmcts.reform.unspec.service.flowstate.FlowState.Main.EXTENSION_RESPONDED;
import static uk.gov.hmcts.reform.unspec.service.flowstate.FlowState.Main.FULL_DEFENCE;
import static uk.gov.hmcts.reform.unspec.service.flowstate.FlowState.Main.RESPONDED_TO_CLAIM;
import static uk.gov.hmcts.reform.unspec.service.flowstate.FlowState.Main.SERVICE_ACKNOWLEDGED;
import static uk.gov.hmcts.reform.unspec.service.flowstate.FlowState.Main.SERVICE_CONFIRMED;

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
                Arguments.of(CaseDataBuilder.builder().atStateClaimDraft().build(), DRAFT),
                Arguments.of(CaseDataBuilder.builder().atStateClaimCreated().build(), CLAIM_ISSUED),
                Arguments.of(CaseDataBuilder.builder().atStateClaimStayed().build(), CLAIM_STAYED),
                Arguments.of(CaseDataBuilder.builder().atStateServiceConfirmed().build(), SERVICE_CONFIRMED),
                Arguments.of(CaseDataBuilder.builder().atStateServiceAcknowledge().build(), SERVICE_ACKNOWLEDGED),
                Arguments.of(CaseDataBuilder.builder().atStateExtensionRequested().build(), EXTENSION_REQUESTED),
                Arguments.of(CaseDataBuilder.builder().atStateExtensionResponded().build(), EXTENSION_RESPONDED),
                Arguments.of(CaseDataBuilder.builder().atStateRespondedToClaim().build(), RESPONDED_TO_CLAIM),
                Arguments.of(CaseDataBuilder.builder().atStateFullDefence().build(), FULL_DEFENCE)
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

    @Nested
    class GetAllowedEventsForFlowState {

        @Test
        void shouldReturnValidEvents_whenFlowStateIsDraft() {
            assertThat(flowStateAllowedEventService.getAllowedEvents(DRAFT.fullName()))
                .containsExactlyInAnyOrder(CREATE_CASE, WITHDRAW_CLAIM, DISCONTINUE_CLAIM);
        }

        @Test
        void shouldReturnValidEvents_whenFlowStateIsClaimIssued() {
            assertThat(flowStateAllowedEventService.getAllowedEvents(CLAIM_ISSUED.fullName()))
                .containsExactlyInAnyOrder(MOVE_TO_STAYED, CONFIRM_SERVICE, WITHDRAW_CLAIM, DISCONTINUE_CLAIM);
        }

        @Test
        void shouldReturnValidEvents_whenFlowStateIsServiceConfirmed() {
            assertThat(flowStateAllowedEventService.getAllowedEvents(SERVICE_CONFIRMED.fullName()))
                .containsExactlyInAnyOrder(ACKNOWLEDGE_SERVICE, DEFENDANT_RESPONSE, WITHDRAW_CLAIM, DISCONTINUE_CLAIM);
        }

        @Test
        void shouldReturnValidEvents_whenFlowStateIsServiceAcknowledge() {
            assertThat(flowStateAllowedEventService.getAllowedEvents(SERVICE_ACKNOWLEDGED.fullName()))
                .containsExactlyInAnyOrder(REQUEST_EXTENSION, DEFENDANT_RESPONSE, WITHDRAW_CLAIM, DISCONTINUE_CLAIM);
        }

        @Test
        void shouldReturnValidEvents_whenFlowStateIsExtensionRequested() {
            assertThat(flowStateAllowedEventService.getAllowedEvents(EXTENSION_REQUESTED.fullName()))
                .containsExactlyInAnyOrder(DEFENDANT_RESPONSE, RESPOND_EXTENSION, WITHDRAW_CLAIM, DISCONTINUE_CLAIM);
        }

        @Test
        void shouldReturnValidEvents_whenFlowStateIsExtensionResponded() {
            assertThat(flowStateAllowedEventService.getAllowedEvents(EXTENSION_RESPONDED.fullName()))
                .containsExactlyInAnyOrder(DEFENDANT_RESPONSE, WITHDRAW_CLAIM, DISCONTINUE_CLAIM);
        }

        @Test
        void shouldReturnValidEvents_whenFlowStateIsRespondToClaim() {
            assertThat(flowStateAllowedEventService.getAllowedEvents(RESPONDED_TO_CLAIM.fullName()))
                .containsExactlyInAnyOrder(CLAIMANT_RESPONSE, WITHDRAW_CLAIM, DISCONTINUE_CLAIM);
        }

        @ParameterizedTest
        @EnumSource(value = FlowState.Main.class, names = {"CLAIM_STAYED", "FULL_DEFENCE"})
        void shouldReturnValidEvents_whenFlowStateIsClaimStayedOrFullDefence(FlowState.Main state) {
            assertThat(flowStateAllowedEventService.getAllowedEvents(state.fullName()))
                .containsExactlyInAnyOrder(WITHDRAW_CLAIM, DISCONTINUE_CLAIM);
        }
    }

    @Nested
    class IsEventAllowedOnFlowState {

        @ParameterizedTest
        @CsvSource({
            "DRAFT,CREATE_CASE",
            "CLAIM_ISSUED,MOVE_TO_STAYED",
            "CLAIM_ISSUED,CONFIRM_SERVICE",
            "SERVICE_CONFIRMED,ACKNOWLEDGE_SERVICE",
            "SERVICE_CONFIRMED,DEFENDANT_RESPONSE",
            "SERVICE_ACKNOWLEDGED,REQUEST_EXTENSION",
            "SERVICE_ACKNOWLEDGED,DEFENDANT_RESPONSE",
            "EXTENSION_REQUESTED,RESPOND_EXTENSION",
            "EXTENSION_REQUESTED,DEFENDANT_RESPONSE",
            "EXTENSION_RESPONDED,DEFENDANT_RESPONSE",
            "RESPONDED_TO_CLAIM,CLAIMANT_RESPONSE",
            "SERVICE_CONFIRMED,WITHDRAW_CLAIM",
            "RESPONDED_TO_CLAIM,DISCONTINUE_CLAIM"
        })
        void shouldReturnTrue_whenEventIsAllowedAtGivenState(
            FlowState.Main flowState,
            CaseEvent caseEvent
        ) {
            assertTrue(flowStateAllowedEventService.isAllowedOnState(flowState.fullName(), caseEvent));
        }

        @ParameterizedTest
        @CsvSource({
            "CLAIM_STAYED,CONFIRM_SERVICE",
            "FULL_DEFENCE,ACKNOWLEDGE_SERVICE"
        })
        void shouldReturnFalse_whenEventIsNotAllowedAtGivenState(
            FlowState.Main flowState,
            CaseEvent caseEvent
        ) {
            assertFalse(flowStateAllowedEventService.isAllowedOnState(flowState.fullName(), caseEvent));
        }
    }

    static class GetAllowedStatesForCaseEventArguments implements ArgumentsProvider {

        @Override
        @SneakyThrows
        public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
            return Stream.of(
                Arguments.of(CREATE_CASE, new String[]{DRAFT.fullName()}),
                Arguments.of(CONFIRM_SERVICE, new String[]{CLAIM_ISSUED.fullName()}),
                Arguments.of(REQUEST_EXTENSION, new String[]{SERVICE_ACKNOWLEDGED.fullName()}),
                Arguments.of(RESPOND_EXTENSION, new String[]{EXTENSION_REQUESTED.fullName()}),
                Arguments.of(MOVE_TO_STAYED, new String[]{CLAIM_ISSUED.fullName()}),
                Arguments.of(ACKNOWLEDGE_SERVICE, new String[]{SERVICE_CONFIRMED.fullName()}),
                Arguments.of(CLAIMANT_RESPONSE, new String[]{RESPONDED_TO_CLAIM.fullName()}),
                Arguments.of(
                    DEFENDANT_RESPONSE,
                    new String[]{
                        SERVICE_CONFIRMED.fullName(), SERVICE_ACKNOWLEDGED.fullName(),
                        EXTENSION_REQUESTED.fullName(), EXTENSION_RESPONDED.fullName()
                    }
                ),
                Arguments.of(
                    WITHDRAW_CLAIM,
                    new String[]{DRAFT.fullName(), CLAIM_ISSUED.fullName(), CLAIM_STAYED.fullName(),
                        SERVICE_CONFIRMED.fullName(), SERVICE_ACKNOWLEDGED.fullName(), EXTENSION_REQUESTED.fullName(),
                        EXTENSION_RESPONDED.fullName(), RESPONDED_TO_CLAIM.fullName(), FULL_DEFENCE.fullName()
                    }
                ),
                Arguments.of(
                    DISCONTINUE_CLAIM,
                    new String[]{DRAFT.fullName(), CLAIM_ISSUED.fullName(), CLAIM_STAYED.fullName(),
                        SERVICE_CONFIRMED.fullName(), SERVICE_ACKNOWLEDGED.fullName(), EXTENSION_REQUESTED.fullName(),
                        EXTENSION_RESPONDED.fullName(), RESPONDED_TO_CLAIM.fullName(), FULL_DEFENCE.fullName()
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
                Arguments.of(
                    true,
                    CaseDetailsBuilder.builder().atStateExtensionRequested().build(),
                    DEFENDANT_RESPONSE
                ),
                Arguments.of(true, CaseDetailsBuilder.builder().atStateExtensionRequested().build(), RESPOND_EXTENSION),
                Arguments.of(true, CaseDetailsBuilder.builder().atStateExtensionRequested().build(), WITHDRAW_CLAIM),
                Arguments.of(true, CaseDetailsBuilder.builder().atStateExtensionRequested().build(), DISCONTINUE_CLAIM),
                Arguments.of(false, CaseDetailsBuilder.builder().atStateExtensionRequested().build(), CREATE_CASE),
                Arguments.of(
                    false,
                    CaseDetailsBuilder.builder().atStateExtensionRequested().build(),
                    CLAIMANT_RESPONSE
                ),

                Arguments.of(true, CaseDetailsBuilder.builder().atStateServiceAcknowledge().build(), REQUEST_EXTENSION),
                Arguments.of(true, CaseDetailsBuilder.builder().atStateServiceAcknowledge().build(), WITHDRAW_CLAIM),
                Arguments.of(
                    true,
                    CaseDetailsBuilder.builder().atStateServiceAcknowledge().build(),
                    DEFENDANT_RESPONSE
                ),
                Arguments.of(true, CaseDetailsBuilder.builder().atStateServiceAcknowledge().build(), DISCONTINUE_CLAIM),
                Arguments.of(false, CaseDetailsBuilder.builder().atStateServiceAcknowledge().build(), CREATE_CASE),
                Arguments.of(false, CaseDetailsBuilder.builder().atStateServiceAcknowledge().build(), CONFIRM_SERVICE),
                Arguments.of(false, CaseDetailsBuilder.builder().atStateServiceAcknowledge().build(), CLAIMANT_RESPONSE)
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
