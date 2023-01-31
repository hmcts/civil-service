package uk.gov.hmcts.reform.civil.service.flowstate;

import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.launchdarkly.FeatureToggleService;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDetailsBuilder;

import java.util.Arrays;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.of;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.ACKNOWLEDGE_CLAIM;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.ADD_CASE_NOTE;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.ADD_DEFENDANT_LITIGATION_FRIEND;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.ADD_OR_AMEND_CLAIM_DOCUMENTS;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.AMEND_PARTY_DETAILS;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.APPLICATION_CLOSED_UPDATE_CLAIM;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.APPLICATION_OFFLINE_UPDATE_CLAIM;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.APPLY_NOC_DECISION;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CASE_PROCEEDS_IN_CASEMAN;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CHANGE_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CLAIMANT_RESPONSE;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CLAIMANT_RESPONSE_SPEC;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_CLAIM;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_CLAIM_AFTER_PAYMENT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_CLAIM_SPEC;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_CLAIM_SPEC_AFTER_PAYMENT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_SDO;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.DEFAULT_JUDGEMENT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.DEFENDANT_RESPONSE;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.DISCONTINUE_CLAIM;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.DISMISS_CLAIM;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.EVIDENCE_UPLOAD_APPLICANT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.EVIDENCE_UPLOAD_JUDGE;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.HEARING_FEE_PAID;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.EVIDENCE_UPLOAD_RESPONDENT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.HEARING_FEE_UNPAID;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.HEARING_SCHEDULED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.INFORM_AGREED_EXTENSION_DATE;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.INITIATE_GENERAL_APPLICATION;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.MOVE_TO_DECISION_OUTCOME;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOC_REQUEST;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_DEFENDANT_OF_CLAIM;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_DEFENDANT_OF_CLAIM_DETAILS;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NotSuitable_SDO;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.REFER_TO_JUDGE;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.RESUBMIT_CLAIM;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.SERVICE_REQUEST_RECEIVED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.STANDARD_DIRECTION_ORDER_DJ;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.TAKE_CASE_OFFLINE;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.TRIAL_READY_CHECK;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.TRIAL_READY_NOTIFICATION;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.GENERATE_DIRECTIONS_ORDER;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.WITHDRAW_CLAIM;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.migrateCase;
import static uk.gov.hmcts.reform.civil.enums.SuperClaimType.SPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.AWAITING_RESPONSES_FULL_DEFENCE_RECEIVED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.AWAITING_RESPONSES_NOT_FULL_DEFENCE_RECEIVED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.CLAIM_DETAILS_NOTIFIED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.CLAIM_DETAILS_NOTIFIED_TIME_EXTENSION;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.CLAIM_DISMISSED_HEARING_FEE_DUE_DEADLINE;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.CLAIM_DISMISSED_PAST_CLAIM_DETAILS_NOTIFICATION_DEADLINE;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.CLAIM_DISMISSED_PAST_CLAIM_NOTIFICATION_DEADLINE;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.CLAIM_ISSUED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.CLAIM_ISSUED_PAYMENT_FAILED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.CLAIM_ISSUED_PAYMENT_SUCCESSFUL;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.CLAIM_NOTIFIED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.CLAIM_SUBMITTED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.COUNTER_CLAIM;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.DIVERGENT_RESPOND_GO_OFFLINE;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.DRAFT;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.FULL_ADMISSION;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.FULL_DEFENCE;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.FULL_DEFENCE_NOT_PROCEED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.FULL_DEFENCE_PROCEED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.NOTIFICATION_ACKNOWLEDGED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.NOTIFICATION_ACKNOWLEDGED_TIME_EXTENSION;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.PART_ADMISSION;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.PAST_APPLICANT_RESPONSE_DEADLINE_AWAITING_CAMUNDA;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.PAST_CLAIM_DETAILS_NOTIFICATION_DEADLINE_AWAITING_CAMUNDA;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.PAST_CLAIM_DISMISSED_DEADLINE_AWAITING_CAMUNDA;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.PAST_CLAIM_NOTIFICATION_DEADLINE_AWAITING_CAMUNDA;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.PENDING_CLAIM_ISSUED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.PENDING_CLAIM_ISSUED_UNREPRESENTED_DEFENDANT;
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
    StateFlowEngine.class,
    FlowStateAllowedEventService.class
})
class FlowStateAllowedEventServiceTest {

    @MockBean
    private FeatureToggleService featureToggleService;

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
                    CaseDataBuilder.builder().atStateClaimNotified_1v1().build(),
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
                    new CaseEvent[]{
                        CREATE_CLAIM,
                        migrateCase
                    }
                ),

                of(
                    CLAIM_SUBMITTED,
                    new CaseEvent[]{
                        ADD_CASE_NOTE,
                        INITIATE_GENERAL_APPLICATION,
                        CREATE_SDO,
                        NotSuitable_SDO,
                        migrateCase
                    }
                ),
                of(
                    CLAIM_ISSUED_PAYMENT_FAILED,
                    new CaseEvent[]{
                        RESUBMIT_CLAIM,
                        WITHDRAW_CLAIM,
                        DISCONTINUE_CLAIM,
                        AMEND_PARTY_DETAILS,
                        ADD_CASE_NOTE,
                        INITIATE_GENERAL_APPLICATION,
                        CREATE_SDO,
                        NotSuitable_SDO,
                        migrateCase
                    }
                ),
                of(
                    CLAIM_ISSUED_PAYMENT_SUCCESSFUL,
                    new CaseEvent[]{
                        NOC_REQUEST,
                        APPLY_NOC_DECISION,
                        ADD_CASE_NOTE,
                        INITIATE_GENERAL_APPLICATION,
                        CREATE_SDO,
                        NotSuitable_SDO,
                        migrateCase,
                        CREATE_CLAIM_AFTER_PAYMENT,
                        CREATE_CLAIM_SPEC_AFTER_PAYMENT
                    }
                ),
                of(
                    CLAIM_ISSUED,
                    new CaseEvent[]{
                        NOC_REQUEST,
                        APPLY_NOC_DECISION,
                        NOTIFY_DEFENDANT_OF_CLAIM,
                        ADD_DEFENDANT_LITIGATION_FRIEND,
                        CASE_PROCEEDS_IN_CASEMAN,
                        ADD_OR_AMEND_CLAIM_DOCUMENTS,
                        AMEND_PARTY_DETAILS,
                        DISMISS_CLAIM,
                        DISCONTINUE_CLAIM,
                        WITHDRAW_CLAIM,
                        ADD_CASE_NOTE,
                        CHANGE_SOLICITOR_EMAIL,
                        INITIATE_GENERAL_APPLICATION,
                        CREATE_SDO,
                        NotSuitable_SDO,
                        EVIDENCE_UPLOAD_JUDGE,
                        CREATE_CLAIM_AFTER_PAYMENT,
                        CREATE_CLAIM_SPEC_AFTER_PAYMENT,
                        EVIDENCE_UPLOAD_APPLICANT,
                        migrateCase,
                        EVIDENCE_UPLOAD_RESPONDENT

                    }
                ),
                of(
                    CLAIM_NOTIFIED,
                    new CaseEvent[]{
                        NOC_REQUEST,
                        APPLY_NOC_DECISION,
                        NOTIFY_DEFENDANT_OF_CLAIM_DETAILS,
                        ADD_DEFENDANT_LITIGATION_FRIEND,
                        CASE_PROCEEDS_IN_CASEMAN,
                        ADD_OR_AMEND_CLAIM_DOCUMENTS,
                        AMEND_PARTY_DETAILS,
                        DISMISS_CLAIM,
                        DISCONTINUE_CLAIM,
                        WITHDRAW_CLAIM,
                        ADD_CASE_NOTE,
                        INITIATE_GENERAL_APPLICATION,
                        CREATE_SDO,
                        NotSuitable_SDO,
                        DEFAULT_JUDGEMENT,
                        CHANGE_SOLICITOR_EMAIL,
                        EVIDENCE_UPLOAD_JUDGE,
                        EVIDENCE_UPLOAD_APPLICANT,
                        migrateCase,
                        EVIDENCE_UPLOAD_RESPONDENT
                    }
                ),
                of(
                    CLAIM_DETAILS_NOTIFIED,
                    new CaseEvent[]{
                        NOC_REQUEST,
                        APPLY_NOC_DECISION,
                        ACKNOWLEDGE_CLAIM,
                        DEFENDANT_RESPONSE,
                        INFORM_AGREED_EXTENSION_DATE,
                        ADD_DEFENDANT_LITIGATION_FRIEND,
                        WITHDRAW_CLAIM,
                        DISCONTINUE_CLAIM,
                        AMEND_PARTY_DETAILS,
                        CASE_PROCEEDS_IN_CASEMAN,
                        DISMISS_CLAIM,
                        ADD_CASE_NOTE,
                        INITIATE_GENERAL_APPLICATION,
                        CREATE_SDO,
                        NotSuitable_SDO,
                        DEFAULT_JUDGEMENT,
                        CHANGE_SOLICITOR_EMAIL,
                        STANDARD_DIRECTION_ORDER_DJ,
                        TAKE_CASE_OFFLINE,
                        EVIDENCE_UPLOAD_JUDGE,
                        HEARING_FEE_UNPAID,
                        HEARING_FEE_PAID,
                        TRIAL_READY_NOTIFICATION,
                        TRIAL_READY_CHECK,
                        MOVE_TO_DECISION_OUTCOME,
                        SERVICE_REQUEST_RECEIVED,
                        HEARING_SCHEDULED,
                        EVIDENCE_UPLOAD_APPLICANT,
                        migrateCase,
                        EVIDENCE_UPLOAD_RESPONDENT,
                        GENERATE_DIRECTIONS_ORDER
                    }
                ),
                of(
                    CLAIM_DETAILS_NOTIFIED_TIME_EXTENSION,
                    new CaseEvent[]{
                        NOC_REQUEST,
                        APPLY_NOC_DECISION,
                        ACKNOWLEDGE_CLAIM,
                        DEFENDANT_RESPONSE,
                        ADD_DEFENDANT_LITIGATION_FRIEND,
                        WITHDRAW_CLAIM,
                        DISCONTINUE_CLAIM,
                        CASE_PROCEEDS_IN_CASEMAN,
                        AMEND_PARTY_DETAILS,
                        DISMISS_CLAIM,
                        ADD_CASE_NOTE,
                        INFORM_AGREED_EXTENSION_DATE,
                        INITIATE_GENERAL_APPLICATION,
                        CREATE_SDO,
                        NotSuitable_SDO,
                        DEFAULT_JUDGEMENT,
                        CHANGE_SOLICITOR_EMAIL,
                        migrateCase

                    }
                ),
                of(
                    NOTIFICATION_ACKNOWLEDGED,
                    new CaseEvent[]{
                        NOC_REQUEST,
                        APPLY_NOC_DECISION,
                        ACKNOWLEDGE_CLAIM,
                        DEFENDANT_RESPONSE,
                        ADD_DEFENDANT_LITIGATION_FRIEND,
                        WITHDRAW_CLAIM,
                        DISCONTINUE_CLAIM,
                        CASE_PROCEEDS_IN_CASEMAN,
                        INFORM_AGREED_EXTENSION_DATE,
                        AMEND_PARTY_DETAILS,
                        DISMISS_CLAIM,
                        ADD_CASE_NOTE,
                        CHANGE_SOLICITOR_EMAIL,
                        INITIATE_GENERAL_APPLICATION,
                        CREATE_SDO,
                        NotSuitable_SDO,
                        DEFAULT_JUDGEMENT,
                        migrateCase
                    }
                ),
                of(
                    NOTIFICATION_ACKNOWLEDGED_TIME_EXTENSION,
                    new CaseEvent[]{
                        NOC_REQUEST,
                        APPLY_NOC_DECISION,
                        ACKNOWLEDGE_CLAIM,
                        DEFENDANT_RESPONSE,
                        ADD_DEFENDANT_LITIGATION_FRIEND,
                        WITHDRAW_CLAIM,
                        DISCONTINUE_CLAIM,
                        CASE_PROCEEDS_IN_CASEMAN,
                        AMEND_PARTY_DETAILS,
                        DISMISS_CLAIM,
                        ADD_CASE_NOTE,
                        INFORM_AGREED_EXTENSION_DATE,
                        CHANGE_SOLICITOR_EMAIL,
                        INITIATE_GENERAL_APPLICATION,
                        CREATE_SDO,
                        NotSuitable_SDO,
                        DEFAULT_JUDGEMENT,
                        migrateCase
                    }
                ),
                of(
                    AWAITING_RESPONSES_FULL_DEFENCE_RECEIVED,
                    new CaseEvent[]{
                        NOC_REQUEST,
                        APPLY_NOC_DECISION,
                        DEFENDANT_RESPONSE,
                        ACKNOWLEDGE_CLAIM,
                        INFORM_AGREED_EXTENSION_DATE,
                        ADD_DEFENDANT_LITIGATION_FRIEND,
                        WITHDRAW_CLAIM,
                        DISCONTINUE_CLAIM,
                        AMEND_PARTY_DETAILS,
                        CASE_PROCEEDS_IN_CASEMAN,
                        DISMISS_CLAIM,
                        ADD_CASE_NOTE,
                        CHANGE_SOLICITOR_EMAIL,
                        INITIATE_GENERAL_APPLICATION,
                        CREATE_SDO,
                        NotSuitable_SDO,
                        migrateCase
                    }
                ),
                of(
                    AWAITING_RESPONSES_NOT_FULL_DEFENCE_RECEIVED,
                    new CaseEvent[]{
                        NOC_REQUEST,
                        APPLY_NOC_DECISION,
                        DEFENDANT_RESPONSE,
                        ACKNOWLEDGE_CLAIM,
                        INFORM_AGREED_EXTENSION_DATE,
                        ADD_DEFENDANT_LITIGATION_FRIEND,
                        WITHDRAW_CLAIM,
                        DISCONTINUE_CLAIM,
                        AMEND_PARTY_DETAILS,
                        CASE_PROCEEDS_IN_CASEMAN,
                        DISMISS_CLAIM,
                        ADD_CASE_NOTE,
                        CHANGE_SOLICITOR_EMAIL,
                        INITIATE_GENERAL_APPLICATION,
                        CREATE_SDO,
                        NotSuitable_SDO,
                        migrateCase
                    }
                ),
                of(
                    FULL_DEFENCE,
                    new CaseEvent[]{
                        NOC_REQUEST,
                        APPLY_NOC_DECISION,
                        CLAIMANT_RESPONSE,
                        WITHDRAW_CLAIM,
                        ADD_DEFENDANT_LITIGATION_FRIEND,
                        DISCONTINUE_CLAIM,
                        CASE_PROCEEDS_IN_CASEMAN,
                        AMEND_PARTY_DETAILS,
                        TAKE_CASE_OFFLINE,
                        ADD_CASE_NOTE,
                        CHANGE_SOLICITOR_EMAIL,
                        INITIATE_GENERAL_APPLICATION,
                        CREATE_SDO,
                        NotSuitable_SDO,
                        APPLICATION_OFFLINE_UPDATE_CLAIM,
                        migrateCase
                    }
                ),
                of(
                    FULL_ADMISSION,
                    new CaseEvent[]{
                        WITHDRAW_CLAIM,
                        ADD_DEFENDANT_LITIGATION_FRIEND,
                        DISCONTINUE_CLAIM,
                        CASE_PROCEEDS_IN_CASEMAN,
                        AMEND_PARTY_DETAILS,
                        ADD_CASE_NOTE,
                        CHANGE_SOLICITOR_EMAIL,
                        INITIATE_GENERAL_APPLICATION,
                        CREATE_SDO,
                        NotSuitable_SDO,
                        APPLICATION_OFFLINE_UPDATE_CLAIM,
                        migrateCase
                    }
                ),
                of(
                    PART_ADMISSION,
                    new CaseEvent[]{
                        WITHDRAW_CLAIM,
                        ADD_DEFENDANT_LITIGATION_FRIEND,
                        DISCONTINUE_CLAIM,
                        CASE_PROCEEDS_IN_CASEMAN,
                        AMEND_PARTY_DETAILS,
                        ADD_CASE_NOTE,
                        CHANGE_SOLICITOR_EMAIL,
                        INITIATE_GENERAL_APPLICATION,
                        CREATE_SDO,
                        NotSuitable_SDO,
                        APPLICATION_OFFLINE_UPDATE_CLAIM,
                        migrateCase
                    }
                ),
                of(
                    COUNTER_CLAIM,
                    new CaseEvent[]{
                        WITHDRAW_CLAIM,
                        ADD_DEFENDANT_LITIGATION_FRIEND,
                        DISCONTINUE_CLAIM,
                        CASE_PROCEEDS_IN_CASEMAN,
                        AMEND_PARTY_DETAILS,
                        ADD_CASE_NOTE,
                        CHANGE_SOLICITOR_EMAIL,
                        INITIATE_GENERAL_APPLICATION,
                        CREATE_SDO,
                        NotSuitable_SDO,
                        APPLICATION_OFFLINE_UPDATE_CLAIM,
                        migrateCase
                    }
                ),
                of(
                    DIVERGENT_RESPOND_GO_OFFLINE,
                    new CaseEvent[]{
                        APPLICATION_OFFLINE_UPDATE_CLAIM,
                        migrateCase
                    }
                ),
                of(
                    FULL_DEFENCE_PROCEED,
                    new CaseEvent[]{
                        NOC_REQUEST,
                        APPLY_NOC_DECISION,
                        ADD_DEFENDANT_LITIGATION_FRIEND,
                        WITHDRAW_CLAIM,
                        DISCONTINUE_CLAIM,
                        CASE_PROCEEDS_IN_CASEMAN,
                        AMEND_PARTY_DETAILS,
                        ADD_CASE_NOTE,
                        CHANGE_SOLICITOR_EMAIL,
                        INITIATE_GENERAL_APPLICATION,
                        CREATE_SDO,
                        NotSuitable_SDO,
                        HEARING_SCHEDULED,
                        HEARING_FEE_UNPAID,
                        HEARING_FEE_PAID,
                        TRIAL_READY_CHECK,
                        TRIAL_READY_NOTIFICATION,
                        MOVE_TO_DECISION_OUTCOME,
                        SERVICE_REQUEST_RECEIVED,
                        REFER_TO_JUDGE,
                        migrateCase,
                        TAKE_CASE_OFFLINE,
                        GENERATE_DIRECTIONS_ORDER
                    }
                ),
                of(
                    FULL_DEFENCE_NOT_PROCEED,
                    new CaseEvent[]{
                        ADD_DEFENDANT_LITIGATION_FRIEND,
                        WITHDRAW_CLAIM,
                        DISCONTINUE_CLAIM,
                        CASE_PROCEEDS_IN_CASEMAN,
                        AMEND_PARTY_DETAILS,
                        ADD_CASE_NOTE,
                        CHANGE_SOLICITOR_EMAIL,
                        INITIATE_GENERAL_APPLICATION,
                        CREATE_SDO,
                        NotSuitable_SDO,
                        REFER_TO_JUDGE,
                        migrateCase
                    }
                ),
                of(
                    CLAIM_DISMISSED_PAST_CLAIM_NOTIFICATION_DEADLINE,
                    new CaseEvent[]{
                        CASE_PROCEEDS_IN_CASEMAN,
                        ADD_CASE_NOTE,
                        INITIATE_GENERAL_APPLICATION,
                        CREATE_SDO,
                        NotSuitable_SDO,
                        APPLICATION_CLOSED_UPDATE_CLAIM,
                        REFER_TO_JUDGE,
                        migrateCase
                    }
                ),
                of(
                    CLAIM_DISMISSED_PAST_CLAIM_DETAILS_NOTIFICATION_DEADLINE,
                    new CaseEvent[]{
                        CASE_PROCEEDS_IN_CASEMAN,
                        ADD_CASE_NOTE,
                        INITIATE_GENERAL_APPLICATION,
                        NotSuitable_SDO,
                        CREATE_SDO,
                        REFER_TO_JUDGE,
                        APPLICATION_CLOSED_UPDATE_CLAIM,
                        migrateCase
                    }
                ),
                of(
                    CLAIM_DISMISSED_HEARING_FEE_DUE_DEADLINE,
                    new CaseEvent[]{
                        CASE_PROCEEDS_IN_CASEMAN
                    }
                ),
                of(
                    CLAIM_DISMISSED_HEARING_FEE_DUE_DEADLINE,
                    new CaseEvent[]{
                        CASE_PROCEEDS_IN_CASEMAN
                    }
                ),
                of(
                    PAST_APPLICANT_RESPONSE_DEADLINE_AWAITING_CAMUNDA,
                    new CaseEvent[]{
                        TAKE_CASE_OFFLINE, APPLICATION_OFFLINE_UPDATE_CLAIM,
                        migrateCase
                    }
                ),
                of(
                    PAST_CLAIM_NOTIFICATION_DEADLINE_AWAITING_CAMUNDA,
                    new CaseEvent[]{
                        DISMISS_CLAIM,
                        ADD_CASE_NOTE,
                        INITIATE_GENERAL_APPLICATION,
                        CREATE_SDO,
                        NotSuitable_SDO,
                        REFER_TO_JUDGE,
                        migrateCase
                    }
                ),
                of(
                    PAST_CLAIM_DETAILS_NOTIFICATION_DEADLINE_AWAITING_CAMUNDA,
                    new CaseEvent[]{
                        DISMISS_CLAIM,
                        ADD_CASE_NOTE,
                        INITIATE_GENERAL_APPLICATION,
                        CREATE_SDO,
                        NotSuitable_SDO,
                        REFER_TO_JUDGE,
                        migrateCase
                    }
                ),
                of(
                    PAST_CLAIM_DISMISSED_DEADLINE_AWAITING_CAMUNDA,
                    new CaseEvent[]{
                        DISMISS_CLAIM,
                        migrateCase
                    }
                ),
                of(
                    TAKEN_OFFLINE_BY_STAFF,
                    new CaseEvent[] {
                        ADD_CASE_NOTE, APPLICATION_OFFLINE_UPDATE_CLAIM,
                        migrateCase
                    }
                ),
                of(
                    TAKEN_OFFLINE_UNREGISTERED_DEFENDANT,
                    new CaseEvent[] {
                        ADD_CASE_NOTE, APPLICATION_OFFLINE_UPDATE_CLAIM,
                        migrateCase
                    }
                ),
                of(
                    TAKEN_OFFLINE_UNREPRESENTED_DEFENDANT,
                    new CaseEvent[] {
                        ADD_CASE_NOTE, APPLICATION_OFFLINE_UPDATE_CLAIM,
                        migrateCase
                    }
                ),
                of(
                    TAKEN_OFFLINE_UNREPRESENTED_UNREGISTERED_DEFENDANT,
                    new CaseEvent[] {
                        ADD_CASE_NOTE, APPLICATION_OFFLINE_UPDATE_CLAIM,
                        migrateCase
                    }
                ),
                of(
                    TAKEN_OFFLINE_PAST_APPLICANT_RESPONSE_DEADLINE,
                    new CaseEvent[] {
                        ADD_CASE_NOTE, APPLICATION_OFFLINE_UPDATE_CLAIM,
                        migrateCase
                    }
                ),
                of(
                    TAKEN_OFFLINE_AFTER_CLAIM_DETAILS_NOTIFIED,
                    new CaseEvent[] {
                        ADD_CASE_NOTE, APPLICATION_OFFLINE_UPDATE_CLAIM,
                        migrateCase
                    }
                ),
                of(
                    TAKEN_OFFLINE_AFTER_CLAIM_NOTIFIED,
                    new CaseEvent[] {
                        ADD_CASE_NOTE, APPLICATION_OFFLINE_UPDATE_CLAIM,
                        migrateCase
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
                of(CREATE_CLAIM, new String[]{DRAFT.fullName()}),
                of(RESUBMIT_CLAIM, new String[]{CLAIM_ISSUED_PAYMENT_FAILED.fullName()}),
                of(ACKNOWLEDGE_CLAIM, new String[]{CLAIM_DETAILS_NOTIFIED.fullName(),
                    NOTIFICATION_ACKNOWLEDGED.fullName(),
                    NOTIFICATION_ACKNOWLEDGED_TIME_EXTENSION.fullName(),
                    CLAIM_DETAILS_NOTIFIED_TIME_EXTENSION.fullName(),
                    AWAITING_RESPONSES_FULL_DEFENCE_RECEIVED.fullName(),
                    AWAITING_RESPONSES_NOT_FULL_DEFENCE_RECEIVED.fullName()}),
                of(NOTIFY_DEFENDANT_OF_CLAIM, new String[]{CLAIM_ISSUED.fullName(),
                    PENDING_CLAIM_ISSUED_UNREPRESENTED_DEFENDANT.fullName()}),
                of(CLAIMANT_RESPONSE, new String[]{FULL_DEFENCE.fullName()}),
                of(
                    DEFENDANT_RESPONSE,
                    new String[]{NOTIFICATION_ACKNOWLEDGED.fullName(),
                        CLAIM_DETAILS_NOTIFIED.fullName(),
                        CLAIM_DETAILS_NOTIFIED_TIME_EXTENSION.fullName(),
                        NOTIFICATION_ACKNOWLEDGED_TIME_EXTENSION.fullName(),
                        AWAITING_RESPONSES_FULL_DEFENCE_RECEIVED.fullName(),
                        AWAITING_RESPONSES_NOT_FULL_DEFENCE_RECEIVED.fullName()
                    }
                ),
                of(
                    WITHDRAW_CLAIM,
                    new String[]{CLAIM_ISSUED.fullName(), CLAIM_NOTIFIED.fullName(), CLAIM_DETAILS_NOTIFIED.fullName(),
                        NOTIFICATION_ACKNOWLEDGED.fullName(), CLAIM_ISSUED_PAYMENT_FAILED.fullName(),
                        FULL_DEFENCE.fullName(), FULL_ADMISSION.fullName(),
                        PART_ADMISSION.fullName(), COUNTER_CLAIM.fullName(),
                        FULL_DEFENCE_PROCEED.fullName(), FULL_DEFENCE_NOT_PROCEED.fullName(),
                        CLAIM_DETAILS_NOTIFIED_TIME_EXTENSION.fullName(),
                        NOTIFICATION_ACKNOWLEDGED_TIME_EXTENSION.fullName(),
                        AWAITING_RESPONSES_FULL_DEFENCE_RECEIVED.fullName(),
                        AWAITING_RESPONSES_NOT_FULL_DEFENCE_RECEIVED.fullName()
                    }
                ),
                of(
                    DISCONTINUE_CLAIM,
                    new String[]{CLAIM_ISSUED.fullName(), CLAIM_NOTIFIED.fullName(), CLAIM_DETAILS_NOTIFIED.fullName(),
                        NOTIFICATION_ACKNOWLEDGED.fullName(), CLAIM_ISSUED_PAYMENT_FAILED.fullName(),
                        FULL_DEFENCE.fullName(), FULL_ADMISSION.fullName(),
                        PART_ADMISSION.fullName(), COUNTER_CLAIM.fullName(),
                        FULL_DEFENCE_PROCEED.fullName(), FULL_DEFENCE_NOT_PROCEED.fullName(),
                        CLAIM_DETAILS_NOTIFIED_TIME_EXTENSION.fullName(),
                        NOTIFICATION_ACKNOWLEDGED_TIME_EXTENSION.fullName(),
                        AWAITING_RESPONSES_FULL_DEFENCE_RECEIVED.fullName(),
                        AWAITING_RESPONSES_NOT_FULL_DEFENCE_RECEIVED.fullName()
                    }
                ),
                of(
                    CASE_PROCEEDS_IN_CASEMAN,
                    new String[]{CLAIM_ISSUED.fullName(), CLAIM_NOTIFIED.fullName(),
                        CLAIM_DETAILS_NOTIFIED.fullName(), NOTIFICATION_ACKNOWLEDGED.fullName(),
                        FULL_DEFENCE.fullName(), FULL_ADMISSION.fullName(),
                        PART_ADMISSION.fullName(), COUNTER_CLAIM.fullName(),
                        FULL_DEFENCE_PROCEED.fullName(), FULL_DEFENCE_NOT_PROCEED.fullName(),
                        CLAIM_DETAILS_NOTIFIED_TIME_EXTENSION.fullName(),
                        NOTIFICATION_ACKNOWLEDGED_TIME_EXTENSION.fullName(),
                        CLAIM_DISMISSED_PAST_CLAIM_NOTIFICATION_DEADLINE.fullName(),
                        CLAIM_DISMISSED_PAST_CLAIM_DETAILS_NOTIFICATION_DEADLINE.fullName(),
                        CLAIM_DISMISSED_HEARING_FEE_DUE_DEADLINE.fullName(),
                        AWAITING_RESPONSES_FULL_DEFENCE_RECEIVED.fullName(),
                        AWAITING_RESPONSES_NOT_FULL_DEFENCE_RECEIVED.fullName()
                    }
                ),
                of(
                    ADD_DEFENDANT_LITIGATION_FRIEND,
                    new String[]{CLAIM_ISSUED.fullName(), CLAIM_NOTIFIED.fullName(),
                        CLAIM_DETAILS_NOTIFIED.fullName(), NOTIFICATION_ACKNOWLEDGED.fullName(),
                        FULL_DEFENCE.fullName(), FULL_ADMISSION.fullName(),
                        PART_ADMISSION.fullName(), COUNTER_CLAIM.fullName(),
                        FULL_DEFENCE_PROCEED.fullName(), FULL_DEFENCE_NOT_PROCEED.fullName(),
                        CLAIM_DETAILS_NOTIFIED_TIME_EXTENSION.fullName(),
                        NOTIFICATION_ACKNOWLEDGED_TIME_EXTENSION.fullName(),
                        AWAITING_RESPONSES_FULL_DEFENCE_RECEIVED.fullName(),
                        AWAITING_RESPONSES_NOT_FULL_DEFENCE_RECEIVED.fullName()
                    }
                ),
                of(
                    ADD_OR_AMEND_CLAIM_DOCUMENTS,
                    new String[]{CLAIM_ISSUED.fullName(), CLAIM_NOTIFIED.fullName()}
                ),
                of(NOTIFY_DEFENDANT_OF_CLAIM_DETAILS, new String[]{CLAIM_NOTIFIED.fullName()}),
                of(INFORM_AGREED_EXTENSION_DATE, new String[]{CLAIM_DETAILS_NOTIFIED.fullName(),
                    NOTIFICATION_ACKNOWLEDGED.fullName(),
                    CLAIM_DETAILS_NOTIFIED_TIME_EXTENSION.fullName(),
                    NOTIFICATION_ACKNOWLEDGED_TIME_EXTENSION.fullName(),
                    AWAITING_RESPONSES_FULL_DEFENCE_RECEIVED.fullName(),
                    AWAITING_RESPONSES_NOT_FULL_DEFENCE_RECEIVED.fullName()}),
                of(
                    AMEND_PARTY_DETAILS,
                    new String[]{CLAIM_ISSUED.fullName(), CLAIM_ISSUED_PAYMENT_FAILED.fullName(),
                        CLAIM_NOTIFIED.fullName(), CLAIM_DETAILS_NOTIFIED.fullName(),
                        CLAIM_DETAILS_NOTIFIED_TIME_EXTENSION.fullName(), NOTIFICATION_ACKNOWLEDGED.fullName(),
                        FULL_DEFENCE.fullName(), FULL_ADMISSION.fullName(),
                        PART_ADMISSION.fullName(), COUNTER_CLAIM.fullName(),
                        FULL_DEFENCE_PROCEED.fullName(), FULL_DEFENCE_NOT_PROCEED.fullName(),
                        NOTIFICATION_ACKNOWLEDGED_TIME_EXTENSION.fullName(),
                        AWAITING_RESPONSES_FULL_DEFENCE_RECEIVED.fullName(),
                        AWAITING_RESPONSES_NOT_FULL_DEFENCE_RECEIVED.fullName()
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

        @ParameterizedTest
        @ArgumentsSource(GetAllowedStatesForCaseEventArguments.class)
        void shouldReturnValidStatesLRspec_whenCaseEventIsGiven(CaseEvent caseEvent, String... flowStates) {
            Mockito.when(featureToggleService.isLrSpecEnabled()).thenReturn(false, true);
            assertThat(flowStateAllowedEventService.getAllowedStates(CREATE_CLAIM_SPEC))
                .isEmpty();
            assertThat(flowStateAllowedEventService.getAllowedStates(CREATE_CLAIM_SPEC))
                .isNotEmpty();
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
                of(false, CaseDetailsBuilder.builder().atStateClaimAcknowledge().build(), CREATE_CLAIM_SPEC),
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
                of(false, CaseDetailsBuilder.builder().atStateProceedsOffline1v1().build(), AMEND_PARTY_DETAILS),
                of(true, CaseDetailsBuilder.builder().atStateAwaitingRespondentAcknowledgement1v1().build(),
                   AMEND_PARTY_DETAILS
                ),
                of(
                    true,
                    CaseDetailsBuilder.builder().atStateFullDefenceSpec().build(),
                    CLAIMANT_RESPONSE_SPEC
                ),
                of(
                    true,
                    CaseDetailsBuilder.builder().atStatePartAdmitSpec().build(),
                    CLAIMANT_RESPONSE_SPEC
                ),
                of(
                    true,
                    CaseDetailsBuilder.builder().atStateFullAdmitSpec().build(),
                    CLAIMANT_RESPONSE_SPEC
                )
            );
        }
    }

    @Nested
    class IsEventAllowedOnCaseDetails {

        @BeforeEach
        void enableSpec() {
            Mockito.when(featureToggleService.isLrSpecEnabled()).thenReturn(true);
        }

        @ParameterizedTest
        @ArgumentsSource(GetAllowedStatesForCaseDetailsArguments.class)
        void shouldReturnValidStates_whenCaseEventIsGiven(
            boolean expected,
            CaseDetails caseDetails,
            CaseEvent caseEvent
        ) {
            //work around starts: to force SPEC CLAIM tests to pass to not impact Damages.
            if ((caseDetails.getData().get("superClaimType") != null
                && caseDetails.getData().get("superClaimType").equals(SPEC_CLAIM))
                || caseEvent.toString().equals("CREATE_CLAIM_SPEC")) {
                expected = false;
            }
            //work around ends.

            assertThat(flowStateAllowedEventService.isAllowed(caseDetails, caseEvent))
                .isEqualTo(expected);
        }

        @ParameterizedTest
        @ArgumentsSource(GetAllowedStatesForCaseDetailsArguments.class)
        void shouldReturnValidStates_whenCaseEventIsGiven_spec(
            boolean expected,
            CaseDetails caseDetails,
            CaseEvent caseEvent
        ) {
            Mockito.when(featureToggleService.isLrSpecEnabled()).thenReturn(true);

            assertThat(flowStateAllowedEventService.isAllowed(caseDetails, caseEvent))
                .isEqualTo(expected);
        }
    }

    @Test
    void shouldReturnTrue_whenCaseEventIsMigrateCase() {
        CaseDetails caseDetails =
            CaseDetailsBuilder.builder()
            .atStateAwaitingCaseDetailsNotification().build();
        assertThat(flowStateAllowedEventService.isAllowed(caseDetails, migrateCase))
            .isEqualTo(true);
    }
}
