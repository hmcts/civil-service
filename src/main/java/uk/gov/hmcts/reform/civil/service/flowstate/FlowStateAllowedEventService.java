package uk.gov.hmcts.reform.civil.service.flowstate;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.launchdarkly.FeatureToggleService;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.stateflow.StateFlow;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;
import static java.util.Map.entry;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.ACKNOWLEDGEMENT_OF_SERVICE;
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
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_CLAIM_SPEC;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_SDO;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.DEFAULT_JUDGEMENT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.DEFAULT_JUDGEMENT_SPEC;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.DEFENDANT_RESPONSE;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.DEFENDANT_RESPONSE_CUI;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.DEFENDANT_RESPONSE_SPEC;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.DISCONTINUE_CLAIM;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.DISMISS_CLAIM;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.ENTER_BREATHING_SPACE_SPEC;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.EVIDENCE_UPLOAD;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.EVIDENCE_UPLOAD_JUDGE;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.HEARING_SCHEDULED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.INFORM_AGREED_EXTENSION_DATE;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.INFORM_AGREED_EXTENSION_DATE_SPEC;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.INITIATE_GENERAL_APPLICATION;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.LIFT_BREATHING_SPACE_SPEC;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOC_REQUEST;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_DEFENDANT_OF_CLAIM;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_DEFENDANT_OF_CLAIM_DETAILS;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NotSuitable_SDO;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.RESUBMIT_CLAIM;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.STANDARD_DIRECTION_ORDER_DJ;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.TAKE_CASE_OFFLINE;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.WITHDRAW_CLAIM;
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
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.PENDING_CLAIM_ISSUED_UNREPRESENTED_DEFENDANT;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.SPEC_DRAFT;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.TAKEN_OFFLINE_AFTER_CLAIM_DETAILS_NOTIFIED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.TAKEN_OFFLINE_AFTER_CLAIM_NOTIFIED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.TAKEN_OFFLINE_BY_STAFF;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.TAKEN_OFFLINE_PAST_APPLICANT_RESPONSE_DEADLINE;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.TAKEN_OFFLINE_SDO_NOT_DRAWN;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.TAKEN_OFFLINE_UNREGISTERED_DEFENDANT;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.TAKEN_OFFLINE_UNREPRESENTED_DEFENDANT;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.TAKEN_OFFLINE_UNREPRESENTED_UNREGISTERED_DEFENDANT;
import static uk.gov.hmcts.reform.civil.utils.CaseCategoryUtils.isSpecCaseCategory;

@Service
@RequiredArgsConstructor
public class FlowStateAllowedEventService {

    private final StateFlowEngine stateFlowEngine;
    private final CaseDetailsConverter caseDetailsConverter;
    private final FeatureToggleService toggleService;

    private static final Map<String, List<CaseEvent>> ALLOWED_EVENTS_ON_FLOW_STATE = Map.ofEntries(
        entry(
            DRAFT.fullName(),
            List.of(
                CREATE_CLAIM
            )
        ),

        entry(
            CLAIM_SUBMITTED.fullName(),
            List.of(
                ADD_CASE_NOTE,
                INITIATE_GENERAL_APPLICATION,
                CREATE_SDO,
                NotSuitable_SDO
            )
        ),

        entry(
            CLAIM_ISSUED_PAYMENT_FAILED.fullName(),
            List.of(
                RESUBMIT_CLAIM,
                WITHDRAW_CLAIM,
                DISCONTINUE_CLAIM,
                AMEND_PARTY_DETAILS,
                ADD_CASE_NOTE,
                INITIATE_GENERAL_APPLICATION,
                CREATE_SDO,
                NotSuitable_SDO
            )
        ),

        entry(
            CLAIM_ISSUED_PAYMENT_SUCCESSFUL.fullName(),
            List.of(NOC_REQUEST,
                    APPLY_NOC_DECISION, ADD_CASE_NOTE, INITIATE_GENERAL_APPLICATION, CREATE_SDO,
                    NotSuitable_SDO)
        ),

        entry(
            CLAIM_ISSUED.fullName(),
            List.of(
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
                HEARING_SCHEDULED,
                NotSuitable_SDO
            )
        ),

        entry(
            CLAIM_NOTIFIED.fullName(),
            List.of(
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

                DEFAULT_JUDGEMENT,

                CHANGE_SOLICITOR_EMAIL,

                INITIATE_GENERAL_APPLICATION,
                CREATE_SDO,
                NotSuitable_SDO,
                EVIDENCE_UPLOAD_JUDGE,
                EVIDENCE_UPLOAD
            )
        ),

        entry(
            CLAIM_DETAILS_NOTIFIED.fullName(),
            List.of(
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
                DEFAULT_JUDGEMENT,
                CHANGE_SOLICITOR_EMAIL,
                INITIATE_GENERAL_APPLICATION,
                STANDARD_DIRECTION_ORDER_DJ,
                CREATE_SDO,
                NotSuitable_SDO,
                TAKE_CASE_OFFLINE,
                EVIDENCE_UPLOAD_JUDGE,
                HEARING_SCHEDULED,
                EVIDENCE_UPLOAD

            )
        ),

        entry(
            CLAIM_DETAILS_NOTIFIED_TIME_EXTENSION.fullName(),
            List.of(
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
                DEFAULT_JUDGEMENT,
                INFORM_AGREED_EXTENSION_DATE,
                CHANGE_SOLICITOR_EMAIL,
                INITIATE_GENERAL_APPLICATION,
                CREATE_SDO,
                NotSuitable_SDO
            )
        ),

        entry(
            NOTIFICATION_ACKNOWLEDGED.fullName(),
            List.of(
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
                DEFAULT_JUDGEMENT,
                CREATE_SDO,
                NotSuitable_SDO
            )
        ),

        entry(
            NOTIFICATION_ACKNOWLEDGED_TIME_EXTENSION.fullName(),
            List.of(
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
                DEFAULT_JUDGEMENT,
                CREATE_SDO,
                NotSuitable_SDO
            )
        ),

        entry(
            AWAITING_RESPONSES_FULL_DEFENCE_RECEIVED.fullName(),
            List.of(
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
                NotSuitable_SDO
            )
        ),

        entry(
            AWAITING_RESPONSES_NOT_FULL_DEFENCE_RECEIVED.fullName(),
            List.of(
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
                NotSuitable_SDO
            )
        ),

        entry(
            FULL_DEFENCE.fullName(),
            List.of(
                NOC_REQUEST,
                APPLY_NOC_DECISION,
                CLAIMANT_RESPONSE,
                WITHDRAW_CLAIM,
                ADD_DEFENDANT_LITIGATION_FRIEND,
                DISCONTINUE_CLAIM,
                CASE_PROCEEDS_IN_CASEMAN,
                AMEND_PARTY_DETAILS,
                TAKE_CASE_OFFLINE,
                APPLICATION_OFFLINE_UPDATE_CLAIM,
                ADD_CASE_NOTE,
                CHANGE_SOLICITOR_EMAIL,
                INITIATE_GENERAL_APPLICATION,
                CREATE_SDO,
                NotSuitable_SDO
            )
        ),

        entry(
            FULL_ADMISSION.fullName(),
            List.of(
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
                APPLICATION_OFFLINE_UPDATE_CLAIM
            )
        ),

        entry(
            PART_ADMISSION.fullName(),
            List.of(
                WITHDRAW_CLAIM,
                ADD_DEFENDANT_LITIGATION_FRIEND,
                DISCONTINUE_CLAIM,
                CASE_PROCEEDS_IN_CASEMAN,
                AMEND_PARTY_DETAILS,
                ADD_CASE_NOTE,
                CHANGE_SOLICITOR_EMAIL,
                INITIATE_GENERAL_APPLICATION,
                NotSuitable_SDO,
                CREATE_SDO,
                APPLICATION_OFFLINE_UPDATE_CLAIM
            )
        ),

        entry(
            COUNTER_CLAIM.fullName(),
            List.of(
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
                APPLICATION_OFFLINE_UPDATE_CLAIM
            )
        ),

        entry(
            DIVERGENT_RESPOND_GO_OFFLINE.fullName(),
            List.of(
                APPLICATION_OFFLINE_UPDATE_CLAIM
            )
        ),

        entry(
            FULL_DEFENCE_PROCEED.fullName(),
            List.of(
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
                NotSuitable_SDO
            )
        ),

        entry(
            FULL_DEFENCE_NOT_PROCEED.fullName(),
            List.of(
                ADD_DEFENDANT_LITIGATION_FRIEND,
                WITHDRAW_CLAIM,
                DISCONTINUE_CLAIM,
                CASE_PROCEEDS_IN_CASEMAN,
                AMEND_PARTY_DETAILS,
                ADD_CASE_NOTE,
                CHANGE_SOLICITOR_EMAIL,
                INITIATE_GENERAL_APPLICATION,
                CREATE_SDO,
                NotSuitable_SDO
            )
        ),
        entry(
            CLAIM_DISMISSED_PAST_CLAIM_NOTIFICATION_DEADLINE.fullName(),
            List.of(
                CASE_PROCEEDS_IN_CASEMAN,
                ADD_CASE_NOTE,
                INITIATE_GENERAL_APPLICATION,
                CREATE_SDO,
                NotSuitable_SDO,
                APPLICATION_CLOSED_UPDATE_CLAIM
            )
        ),
        entry(
            CLAIM_DISMISSED_PAST_CLAIM_DETAILS_NOTIFICATION_DEADLINE.fullName(),
            List.of(
                CASE_PROCEEDS_IN_CASEMAN,
                ADD_CASE_NOTE,
                INITIATE_GENERAL_APPLICATION,
                NotSuitable_SDO,
                CREATE_SDO,
                APPLICATION_CLOSED_UPDATE_CLAIM
            )
        ),
        entry(
            PENDING_CLAIM_ISSUED_UNREPRESENTED_DEFENDANT.fullName(),
            List.of(NOC_REQUEST,
                    APPLY_NOC_DECISION, TAKE_CASE_OFFLINE, APPLICATION_OFFLINE_UPDATE_CLAIM)
        ),
        entry(
            PAST_APPLICANT_RESPONSE_DEADLINE_AWAITING_CAMUNDA.fullName(),
            List.of(TAKE_CASE_OFFLINE, APPLICATION_OFFLINE_UPDATE_CLAIM)
        ),
        entry(
            PAST_CLAIM_NOTIFICATION_DEADLINE_AWAITING_CAMUNDA.fullName(),
            List.of(
                DISMISS_CLAIM,
                ADD_CASE_NOTE,
                INITIATE_GENERAL_APPLICATION,
                CREATE_SDO,
                NotSuitable_SDO
            )
        ),
        entry(
            PAST_CLAIM_DETAILS_NOTIFICATION_DEADLINE_AWAITING_CAMUNDA.fullName(),
            List.of(
                DISMISS_CLAIM,
                ADD_CASE_NOTE,
                INITIATE_GENERAL_APPLICATION,
                CREATE_SDO,
                NotSuitable_SDO
            )
        ),
        entry(
            PAST_CLAIM_DISMISSED_DEADLINE_AWAITING_CAMUNDA.fullName(),
            List.of(
                DISMISS_CLAIM
            )
        ),
        entry(
            TAKEN_OFFLINE_BY_STAFF.fullName(),
            List.of(ADD_CASE_NOTE, APPLICATION_OFFLINE_UPDATE_CLAIM)
        ),
        entry(
            TAKEN_OFFLINE_UNREGISTERED_DEFENDANT.fullName(),
            List.of(ADD_CASE_NOTE, APPLICATION_OFFLINE_UPDATE_CLAIM)
        ),
        entry(
            TAKEN_OFFLINE_UNREPRESENTED_DEFENDANT.fullName(),
            List.of(ADD_CASE_NOTE, APPLICATION_OFFLINE_UPDATE_CLAIM)
        ),
        entry(
            TAKEN_OFFLINE_UNREPRESENTED_UNREGISTERED_DEFENDANT.fullName(),
            List.of(ADD_CASE_NOTE, APPLICATION_OFFLINE_UPDATE_CLAIM)
        ),
        entry(
            TAKEN_OFFLINE_PAST_APPLICANT_RESPONSE_DEADLINE.fullName(),
            List.of(ADD_CASE_NOTE, APPLICATION_OFFLINE_UPDATE_CLAIM)
        ),
        entry(
            TAKEN_OFFLINE_AFTER_CLAIM_DETAILS_NOTIFIED.fullName(),
            List.of(ADD_CASE_NOTE, APPLICATION_OFFLINE_UPDATE_CLAIM)
        ),
        entry(
            TAKEN_OFFLINE_AFTER_CLAIM_NOTIFIED.fullName(),
            List.of(ADD_CASE_NOTE, APPLICATION_OFFLINE_UPDATE_CLAIM)
        ),
        entry(
            TAKEN_OFFLINE_SDO_NOT_DRAWN.fullName(),
            List.of(ADD_CASE_NOTE)
        ),
        entry(
            CLAIM_DISMISSED_PAST_CLAIM_DISMISSED_DEADLINE.fullName(),
            List.of(APPLICATION_CLOSED_UPDATE_CLAIM)
        )
    );

    private static final Map<String, List<CaseEvent>> ALLOWED_EVENTS_ON_FLOW_STATE_SPEC = Map.ofEntries(
        entry(
            DRAFT.fullName(),
            List.of(
                CREATE_CLAIM
            )
        ),

        entry(
            SPEC_DRAFT.fullName(),
            List.of(
                CREATE_CLAIM_SPEC
            )
        ),

        entry(
            CLAIM_ISSUED_PAYMENT_FAILED.fullName(),
            List.of(
                ENTER_BREATHING_SPACE_SPEC,
                LIFT_BREATHING_SPACE_SPEC,
                RESUBMIT_CLAIM,
                WITHDRAW_CLAIM,
                DISCONTINUE_CLAIM,
                AMEND_PARTY_DETAILS,
                INITIATE_GENERAL_APPLICATION,
                CREATE_SDO,
                NotSuitable_SDO
            )
        ),

        entry(
            CLAIM_ISSUED.fullName(),
            List.of(
                ENTER_BREATHING_SPACE_SPEC,
                LIFT_BREATHING_SPACE_SPEC,
                NOTIFY_DEFENDANT_OF_CLAIM,
                ADD_DEFENDANT_LITIGATION_FRIEND,
                CASE_PROCEEDS_IN_CASEMAN,
                ADD_OR_AMEND_CLAIM_DOCUMENTS,
                AMEND_PARTY_DETAILS,
                ACKNOWLEDGEMENT_OF_SERVICE,
                INFORM_AGREED_EXTENSION_DATE,
                INFORM_AGREED_EXTENSION_DATE_SPEC,
                DEFENDANT_RESPONSE_SPEC,
                DEFENDANT_RESPONSE_CUI,
                DISMISS_CLAIM,
                DISCONTINUE_CLAIM,
                WITHDRAW_CLAIM,
                DEFAULT_JUDGEMENT_SPEC,
                INITIATE_GENERAL_APPLICATION,
                CREATE_SDO,
                HEARING_SCHEDULED,
                NotSuitable_SDO,
                EVIDENCE_UPLOAD_JUDGE
            )
        ),
        entry(
            CLAIM_NOTIFIED.fullName(),
            List.of(
                ACKNOWLEDGEMENT_OF_SERVICE,
                ENTER_BREATHING_SPACE_SPEC,
                LIFT_BREATHING_SPACE_SPEC,
                INFORM_AGREED_EXTENSION_DATE,
                INFORM_AGREED_EXTENSION_DATE_SPEC,
                DEFENDANT_RESPONSE_SPEC,
                DEFENDANT_RESPONSE_CUI,
                NOTIFY_DEFENDANT_OF_CLAIM_DETAILS,
                ADD_DEFENDANT_LITIGATION_FRIEND,
                CASE_PROCEEDS_IN_CASEMAN,
                ADD_OR_AMEND_CLAIM_DOCUMENTS,
                AMEND_PARTY_DETAILS,
                DISMISS_CLAIM,
                DISCONTINUE_CLAIM,
                WITHDRAW_CLAIM,
                DEFAULT_JUDGEMENT_SPEC,
                INITIATE_GENERAL_APPLICATION,
                CREATE_SDO,
                NotSuitable_SDO,
                EVIDENCE_UPLOAD
            )
        ),
        entry(
            NOTIFICATION_ACKNOWLEDGED.fullName(),
            List.of(
                DEFENDANT_RESPONSE,
                ENTER_BREATHING_SPACE_SPEC,
                LIFT_BREATHING_SPACE_SPEC,
                ADD_DEFENDANT_LITIGATION_FRIEND,
                WITHDRAW_CLAIM,
                DISCONTINUE_CLAIM,
                CASE_PROCEEDS_IN_CASEMAN,
                INFORM_AGREED_EXTENSION_DATE,
                AMEND_PARTY_DETAILS,
                DISMISS_CLAIM,
                DEFAULT_JUDGEMENT_SPEC,
                INITIATE_GENERAL_APPLICATION,
                CREATE_SDO,
                NotSuitable_SDO
            )
        ),

        entry(
            NOTIFICATION_ACKNOWLEDGED_TIME_EXTENSION.fullName(),
            List.of(
                DEFENDANT_RESPONSE,
                ENTER_BREATHING_SPACE_SPEC,
                LIFT_BREATHING_SPACE_SPEC,
                ADD_DEFENDANT_LITIGATION_FRIEND,
                WITHDRAW_CLAIM,
                DISCONTINUE_CLAIM,
                CASE_PROCEEDS_IN_CASEMAN,
                AMEND_PARTY_DETAILS,
                DISMISS_CLAIM,
                DEFAULT_JUDGEMENT_SPEC,
                INITIATE_GENERAL_APPLICATION,
                CREATE_SDO,
                NotSuitable_SDO
            )
        ),

        entry(
            FULL_DEFENCE.fullName(),
            List.of(
                CLAIMANT_RESPONSE,
                CLAIMANT_RESPONSE_SPEC,
                ENTER_BREATHING_SPACE_SPEC,
                LIFT_BREATHING_SPACE_SPEC,
                WITHDRAW_CLAIM,
                ADD_DEFENDANT_LITIGATION_FRIEND,
                DISCONTINUE_CLAIM,
                CASE_PROCEEDS_IN_CASEMAN,
                AMEND_PARTY_DETAILS,
                TAKE_CASE_OFFLINE,
                APPLICATION_OFFLINE_UPDATE_CLAIM,
                INITIATE_GENERAL_APPLICATION,
                CREATE_SDO,
                NotSuitable_SDO
            )
        ),

        entry(
            FULL_ADMISSION.fullName(),
            List.of(
                CLAIMANT_RESPONSE_SPEC,
                ENTER_BREATHING_SPACE_SPEC,
                LIFT_BREATHING_SPACE_SPEC,
                WITHDRAW_CLAIM,
                ADD_DEFENDANT_LITIGATION_FRIEND,
                DISCONTINUE_CLAIM,
                CASE_PROCEEDS_IN_CASEMAN,
                AMEND_PARTY_DETAILS,
                INITIATE_GENERAL_APPLICATION,
                CREATE_SDO,
                NotSuitable_SDO,
                APPLICATION_OFFLINE_UPDATE_CLAIM
            )
        ),

        entry(
            PART_ADMISSION.fullName(),
            List.of(
                CLAIMANT_RESPONSE_SPEC,
                ENTER_BREATHING_SPACE_SPEC,
                LIFT_BREATHING_SPACE_SPEC,
                WITHDRAW_CLAIM,
                ADD_DEFENDANT_LITIGATION_FRIEND,
                DISCONTINUE_CLAIM,
                CASE_PROCEEDS_IN_CASEMAN,
                AMEND_PARTY_DETAILS,
                INITIATE_GENERAL_APPLICATION,
                CREATE_SDO,
                NotSuitable_SDO,
                APPLICATION_OFFLINE_UPDATE_CLAIM
            )
        ),

        entry(
            DIVERGENT_RESPOND_GO_OFFLINE.fullName(),
            List.of(
                APPLICATION_OFFLINE_UPDATE_CLAIM
            )
        ),

        entry(
            COUNTER_CLAIM.fullName(),
            List.of(
                ENTER_BREATHING_SPACE_SPEC,
                LIFT_BREATHING_SPACE_SPEC,
                WITHDRAW_CLAIM,
                ADD_DEFENDANT_LITIGATION_FRIEND,
                DISCONTINUE_CLAIM,
                CASE_PROCEEDS_IN_CASEMAN,
                AMEND_PARTY_DETAILS,
                INITIATE_GENERAL_APPLICATION,
                CREATE_SDO,
                NotSuitable_SDO
            )
        ),

        entry(
            FULL_DEFENCE_PROCEED.fullName(),
            List.of(
                ADD_DEFENDANT_LITIGATION_FRIEND,
                ENTER_BREATHING_SPACE_SPEC,
                LIFT_BREATHING_SPACE_SPEC,
                WITHDRAW_CLAIM,
                DISCONTINUE_CLAIM,
                CASE_PROCEEDS_IN_CASEMAN,
                AMEND_PARTY_DETAILS,
                INITIATE_GENERAL_APPLICATION,
                CREATE_SDO,
                NotSuitable_SDO
            )
        ),

        entry(
            FULL_DEFENCE_NOT_PROCEED.fullName(),
            List.of(
                ADD_DEFENDANT_LITIGATION_FRIEND,
                ENTER_BREATHING_SPACE_SPEC,
                LIFT_BREATHING_SPACE_SPEC,
                WITHDRAW_CLAIM,
                DISCONTINUE_CLAIM,
                CASE_PROCEEDS_IN_CASEMAN,
                AMEND_PARTY_DETAILS,
                INITIATE_GENERAL_APPLICATION,
                CREATE_SDO,
                NotSuitable_SDO
            )
        ),
        entry(
            CLAIM_DISMISSED_PAST_CLAIM_NOTIFICATION_DEADLINE.fullName(),
            List.of(CASE_PROCEEDS_IN_CASEMAN)
        ),
        entry(
            CLAIM_DISMISSED_PAST_CLAIM_DETAILS_NOTIFICATION_DEADLINE.fullName(),
            List.of(CASE_PROCEEDS_IN_CASEMAN)
        ),
        entry(
            PAST_APPLICANT_RESPONSE_DEADLINE_AWAITING_CAMUNDA.fullName(),
            List.of(TAKE_CASE_OFFLINE, APPLICATION_OFFLINE_UPDATE_CLAIM)
        ),
        entry(
            PAST_CLAIM_NOTIFICATION_DEADLINE_AWAITING_CAMUNDA.fullName(),
            List.of(DISMISS_CLAIM)
        ),
        entry(
            PAST_CLAIM_DETAILS_NOTIFICATION_DEADLINE_AWAITING_CAMUNDA.fullName(),
            List.of(DISMISS_CLAIM)
        ),
        entry(
            PAST_CLAIM_DISMISSED_DEADLINE_AWAITING_CAMUNDA.fullName(),
            List.of(DISMISS_CLAIM)
        ),
        entry(
            AWAITING_RESPONSES_FULL_DEFENCE_RECEIVED.fullName(),
            List.of(
                DEFENDANT_RESPONSE_SPEC,
                DEFENDANT_RESPONSE_CUI,
                ACKNOWLEDGE_CLAIM,
                ENTER_BREATHING_SPACE_SPEC,
                LIFT_BREATHING_SPACE_SPEC,
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
                NotSuitable_SDO
            )
        ),
        entry(
            AWAITING_RESPONSES_NOT_FULL_DEFENCE_RECEIVED.fullName(),
            List.of(
                DEFENDANT_RESPONSE_SPEC,
                DEFENDANT_RESPONSE_CUI
            )
        )
    );

    public FlowState getFlowState(CaseData caseData) {
        StateFlow stateFlow = stateFlowEngine.evaluate(caseData);
        return FlowState.fromFullName(stateFlow.getState().getName());
    }

    public List<CaseEvent> getAllowedEvents(String stateFullName) {
        return ALLOWED_EVENTS_ON_FLOW_STATE.getOrDefault(stateFullName, emptyList());
    }

    public boolean isAllowedOnState(String stateFullName, CaseEvent caseEvent) {
        return ALLOWED_EVENTS_ON_FLOW_STATE
            .getOrDefault(stateFullName, emptyList())
            .contains(caseEvent);
    }

    public boolean isAllowedOnStateForSpec(String stateFullName, CaseEvent caseEvent) {
        return ALLOWED_EVENTS_ON_FLOW_STATE_SPEC
            .getOrDefault(stateFullName, emptyList())
            .contains(caseEvent);
    }

    public boolean isAllowed(CaseDetails caseDetails, CaseEvent caseEvent) {
        CaseData caseData = caseDetailsConverter.toCaseData(caseDetails);

        if (isSpecCaseCategory(caseData, toggleService.isAccessProfilesEnabled())
            || CREATE_CLAIM_SPEC.equals(caseEvent)) {
            if (toggleService.isLrSpecEnabled()) {
                StateFlow stateFlow = stateFlowEngine.evaluateSpec(caseDetails);
                return isAllowedOnStateForSpec(stateFlow.getState().getName(), caseEvent);
            } else {
                return false;
            }
        } else {
            StateFlow stateFlow = stateFlowEngine.evaluate(caseDetails);
            return isAllowedOnState(stateFlow.getState().getName(), caseEvent);
        }
    }

    public List<String> getAllowedStates(CaseEvent caseEvent) {
        if (caseEvent.equals(CREATE_CLAIM_SPEC)) {
            if (toggleService.isLrSpecEnabled()) {
                return ALLOWED_EVENTS_ON_FLOW_STATE_SPEC.entrySet().stream()
                    .filter(entry -> entry.getValue().contains(caseEvent))
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList());
            } else {
                return Collections.emptyList();
            }
        }
        return ALLOWED_EVENTS_ON_FLOW_STATE.entrySet().stream()
            .filter(entry -> entry.getValue().contains(caseEvent))
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());
    }
}
