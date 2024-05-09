package uk.gov.hmcts.reform.civil.service.flowstate;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.stateflow.StateFlow;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;
import static java.util.Map.entry;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.DEFENDANT_SIGN_SETTLEMENT_AGREEMENT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.FEE_PAYMENT_OUTCOME;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.LIFT_BREATHING_SPACE_LIP;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NO_REMISSION_HWF;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.UPDATE_NEXT_HEARING_INFO;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.PARTIAL_REMISSION_HWF_GRANTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.UPDATE_NEXT_HEARING_DETAILS;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.ASYNC_STITCHING_COMPLETE;
import static uk.gov.hmcts.reform.civil.enums.CaseCategory.SPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.ACKNOWLEDGEMENT_OF_SERVICE;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.ACKNOWLEDGE_CLAIM;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.ADD_CASE_NOTE;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.ADD_DEFENDANT_LITIGATION_FRIEND;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.ADD_OR_AMEND_CLAIM_DOCUMENTS;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.ADD_UNAVAILABLE_DATES;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.AMEND_PARTY_DETAILS;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.APPLICATION_CLOSED_UPDATE_CLAIM;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.APPLICATION_OFFLINE_UPDATE_CLAIM;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.APPLY_NOC_DECISION;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.BUNDLE_CREATION_NOTIFICATION;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CASE_PROCEEDS_IN_CASEMAN;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CHANGE_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CLAIMANT_RESPONSE;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CLAIMANT_RESPONSE_CUI;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CLAIMANT_RESPONSE_SPEC;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_CLAIM;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_CLAIM_AFTER_PAYMENT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_CLAIM_SPEC;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_CLAIM_SPEC_AFTER_PAYMENT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_LIP_CLAIM;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_SDO;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.DECISION_ON_RECONSIDERATION_REQUEST;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.DEFAULT_JUDGEMENT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.DEFAULT_JUDGEMENT_SPEC;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.DEFENDANT_RESPONSE;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.DEFENDANT_RESPONSE_CUI;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.DEFENDANT_RESPONSE_SPEC;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.DISCONTINUE_CLAIM;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.DISMISS_CLAIM;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.ENTER_BREATHING_SPACE_LIP;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.ENTER_BREATHING_SPACE_SPEC;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.EVIDENCE_UPLOAD_APPLICANT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.EVIDENCE_UPLOAD_JUDGE;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.EVIDENCE_UPLOAD_RESPONDENT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.EXTEND_RESPONSE_DEADLINE;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.FULL_REMISSION_HWF;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.GENERATE_DIRECTIONS_ORDER;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.COURT_OFFICER_ORDER;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.HEARING_FEE_PAID;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.HEARING_FEE_UNPAID;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.HEARING_SCHEDULED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.INFORM_AGREED_EXTENSION_DATE;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.INFORM_AGREED_EXTENSION_DATE_SPEC;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.INITIATE_GENERAL_APPLICATION;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.INVALID_HWF_REFERENCE;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.JUDGMENT_PAID_IN_FULL;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.REFER_JUDGE_DEFENCE_RECEIVED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.LIFT_BREATHING_SPACE_SPEC;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.LIP_CLAIM_SETTLED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.MANAGE_CONTACT_INFORMATION;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.MEDIATION_SUCCESSFUL;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.MEDIATION_UNSUCCESSFUL;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.MORE_INFORMATION_HWF;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.MOVE_TO_DECISION_OUTCOME;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOC_REQUEST;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_CLAIMANT_CUI_FOR_DEADLINE_EXTENSION;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_DEFENDANT_CUI_FOR_DEADLINE_EXTENSION;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_DEFENDANT_OF_CLAIM;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_DEFENDANT_OF_CLAIM_DETAILS;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_HEARING_PARTIES;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOT_SUITABLE_SDO;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.RECORD_JUDGMENT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.EDIT_JUDGMENT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.REFER_TO_JUDGE;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.REQUEST_JUDGEMENT_ADMISSION_SPEC;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.REQUEST_FOR_RECONSIDERATION;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.RESET_PIN;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.RESUBMIT_CLAIM;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.SERVICE_REQUEST_RECEIVED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.SET_ASIDE_JUDGMENT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.STANDARD_DIRECTION_ORDER_DJ;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.TAKE_CASE_OFFLINE;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.TRANSFER_ONLINE_CASE;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.TRIAL_READINESS;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.TRIAL_READY_CHECK;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.TRIAL_READY_NOTIFICATION;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.UPLOAD_MEDIATION_DOCUMENTS;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CUI_UPLOAD_MEDIATION_DOCUMENTS;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.UPLOAD_TRANSLATED_DOCUMENT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.WITHDRAW_CLAIM;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.MIGRATE_CASE;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.UPDATE_HELP_WITH_FEE_NUMBER;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.AWAITING_RESPONSES_FULL_DEFENCE_RECEIVED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.AWAITING_RESPONSES_NOT_FULL_DEFENCE_RECEIVED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.CASE_STAYED;
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
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.COUNTER_CLAIM;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.DIVERGENT_RESPOND_GO_OFFLINE;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.DRAFT;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.FULL_ADMISSION;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.FULL_ADMIT_AGREE_REPAYMENT;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.FULL_ADMIT_PAY_IMMEDIATELY;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.FULL_ADMIT_PROCEED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.FULL_ADMIT_NOT_PROCEED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.FULL_ADMIT_REJECT_REPAYMENT;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.PENDING_CLAIM_ISSUED_UNREPRESENTED_DEFENDANT_ONE_V_ONE_SPEC;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.FULL_DEFENCE;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.FULL_DEFENCE_NOT_PROCEED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.FULL_DEFENCE_PROCEED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.IN_HEARING_READINESS;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.IN_MEDIATION;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.MEDIATION_UNSUCCESSFUL_PROCEED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.NOTIFICATION_ACKNOWLEDGED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.NOTIFICATION_ACKNOWLEDGED_TIME_EXTENSION;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.PART_ADMISSION;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.PART_ADMIT_AGREE_REPAYMENT;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.PART_ADMIT_AGREE_SETTLE;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.FULL_ADMIT_JUDGMENT_ADMISSION;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.PART_ADMIT_NOT_PROCEED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.PART_ADMIT_NOT_SETTLED_NO_MEDIATION;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.PART_ADMIT_PAY_IMMEDIATELY;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.PART_ADMIT_PROCEED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.PART_ADMIT_REJECT_REPAYMENT;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.PAST_APPLICANT_RESPONSE_DEADLINE_AWAITING_CAMUNDA;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.PAST_CLAIM_DETAILS_NOTIFICATION_DEADLINE_AWAITING_CAMUNDA;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.PAST_CLAIM_DISMISSED_DEADLINE_AWAITING_CAMUNDA;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.PAST_CLAIM_NOTIFICATION_DEADLINE_AWAITING_CAMUNDA;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.PENDING_CLAIM_ISSUED_UNREPRESENTED_DEFENDANT;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.PREPARE_FOR_HEARING_CONDUCT_HEARING;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.RESPONDENT_RESPONSE_LANGUAGE_IS_BILINGUAL;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.SIGN_SETTLEMENT_AGREEMENT;
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

@Service
@RequiredArgsConstructor
public class FlowStateAllowedEventService {

    private final StateFlowEngine stateFlowEngine;
    private final CaseDetailsConverter caseDetailsConverter;

    private static final List<CaseEvent> EVENT_WHITELIST = List.of(
        MIGRATE_CASE,
        NOTIFY_HEARING_PARTIES,
        MANAGE_CONTACT_INFORMATION,
        CASE_PROCEEDS_IN_CASEMAN,
        UPDATE_NEXT_HEARING_INFO,
        UPDATE_NEXT_HEARING_DETAILS
    );

    private static final Map<String, List<CaseEvent>> ALLOWED_EVENTS_ON_FLOW_STATE = Map.ofEntries(
        entry(
            DRAFT.fullName(),
            List.of(
                CREATE_CLAIM,
                MIGRATE_CASE
            )
        ),

        entry(
            CLAIM_SUBMITTED.fullName(),
            List.of(
                ADD_CASE_NOTE,
                INITIATE_GENERAL_APPLICATION,
                CREATE_SDO,
                NOT_SUITABLE_SDO,
                MIGRATE_CASE,
                TRANSFER_ONLINE_CASE
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
                NOT_SUITABLE_SDO,
                MIGRATE_CASE,
                TRANSFER_ONLINE_CASE
            )
        ),

        entry(
            CLAIM_ISSUED_PAYMENT_SUCCESSFUL.fullName(),
            List.of(NOC_REQUEST,
                    APPLY_NOC_DECISION,
                    ADD_CASE_NOTE,
                    INITIATE_GENERAL_APPLICATION,
                    CREATE_SDO,
                    NOT_SUITABLE_SDO,
                    MIGRATE_CASE,
                    CREATE_CLAIM_SPEC_AFTER_PAYMENT,
                    CREATE_CLAIM_AFTER_PAYMENT,
                    TRANSFER_ONLINE_CASE
            )
        ),

        entry(
            CLAIM_ISSUED.fullName(),
            List.of(
                NOC_REQUEST,
                APPLY_NOC_DECISION,
                NOTIFY_DEFENDANT_OF_CLAIM,
                ADD_DEFENDANT_LITIGATION_FRIEND,
                CASE_PROCEEDS_IN_CASEMAN,
                UPLOAD_TRANSLATED_DOCUMENT,
                ADD_OR_AMEND_CLAIM_DOCUMENTS,
                AMEND_PARTY_DETAILS,
                DISMISS_CLAIM,
                DISCONTINUE_CLAIM,
                WITHDRAW_CLAIM,
                ADD_CASE_NOTE,
                CHANGE_SOLICITOR_EMAIL,
                INITIATE_GENERAL_APPLICATION,
                CREATE_SDO,
                NOT_SUITABLE_SDO,
                EVIDENCE_UPLOAD_JUDGE,
                CREATE_CLAIM_AFTER_PAYMENT,
                CREATE_CLAIM_SPEC_AFTER_PAYMENT,
                EVIDENCE_UPLOAD_APPLICANT,
                MIGRATE_CASE,
                EVIDENCE_UPLOAD_RESPONDENT,
                TRANSFER_ONLINE_CASE,
                INVALID_HWF_REFERENCE
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
                NOT_SUITABLE_SDO,
                EVIDENCE_UPLOAD_JUDGE,
                EVIDENCE_UPLOAD_APPLICANT,
                MIGRATE_CASE,
                EVIDENCE_UPLOAD_RESPONDENT,
                TRANSFER_ONLINE_CASE
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
                NOT_SUITABLE_SDO,
                TAKE_CASE_OFFLINE,
                EVIDENCE_UPLOAD_JUDGE,
                HEARING_FEE_UNPAID,
                HEARING_FEE_PAID,
                HEARING_SCHEDULED,
                TRIAL_READY_CHECK,
                TRIAL_READY_NOTIFICATION,
                MOVE_TO_DECISION_OUTCOME,
                SERVICE_REQUEST_RECEIVED,
                EVIDENCE_UPLOAD_APPLICANT,
                MIGRATE_CASE,
                EVIDENCE_UPLOAD_RESPONDENT,
                GENERATE_DIRECTIONS_ORDER,
                TRIAL_READINESS,
                BUNDLE_CREATION_NOTIFICATION,
                TRANSFER_ONLINE_CASE,
                ASYNC_STITCHING_COMPLETE,
                COURT_OFFICER_ORDER
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
                STANDARD_DIRECTION_ORDER_DJ,
                INFORM_AGREED_EXTENSION_DATE,
                CHANGE_SOLICITOR_EMAIL,
                INITIATE_GENERAL_APPLICATION,
                CREATE_SDO,
                NOT_SUITABLE_SDO,
                MIGRATE_CASE,
                TAKE_CASE_OFFLINE,
                EVIDENCE_UPLOAD_JUDGE,
                HEARING_SCHEDULED,
                GENERATE_DIRECTIONS_ORDER,
                TRANSFER_ONLINE_CASE,
                COURT_OFFICER_ORDER
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
                CREATE_SDO,
                NOT_SUITABLE_SDO,
                DEFAULT_JUDGEMENT,
                STANDARD_DIRECTION_ORDER_DJ,
                MIGRATE_CASE,
                TAKE_CASE_OFFLINE,
                EVIDENCE_UPLOAD_JUDGE,
                HEARING_SCHEDULED,
                GENERATE_DIRECTIONS_ORDER,
                TRANSFER_ONLINE_CASE,
                COURT_OFFICER_ORDER
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
                STANDARD_DIRECTION_ORDER_DJ,
                CREATE_SDO,
                NOT_SUITABLE_SDO,
                MIGRATE_CASE,
                TAKE_CASE_OFFLINE,
                TRANSFER_ONLINE_CASE,
                GENERATE_DIRECTIONS_ORDER,
                EVIDENCE_UPLOAD_APPLICANT,
                EVIDENCE_UPLOAD_RESPONDENT,
                EVIDENCE_UPLOAD_JUDGE,
                TRIAL_READINESS,
                HEARING_SCHEDULED,
                TRIAL_READY_CHECK,
                TRIAL_READY_NOTIFICATION,
                MOVE_TO_DECISION_OUTCOME,
                HEARING_FEE_UNPAID,
                HEARING_FEE_PAID,
                BUNDLE_CREATION_NOTIFICATION,
                COURT_OFFICER_ORDER
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
                NOT_SUITABLE_SDO,
                MIGRATE_CASE,
                TRANSFER_ONLINE_CASE
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
                NOT_SUITABLE_SDO,
                MIGRATE_CASE,
                TRANSFER_ONLINE_CASE
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
                NOT_SUITABLE_SDO,
                MIGRATE_CASE,
                CLAIMANT_RESPONSE_CUI,
                TRANSFER_ONLINE_CASE
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
                NOT_SUITABLE_SDO,
                APPLICATION_OFFLINE_UPDATE_CLAIM,
                MIGRATE_CASE,
                CLAIMANT_RESPONSE_CUI,
                DEFENDANT_SIGN_SETTLEMENT_AGREEMENT,
                TRANSFER_ONLINE_CASE
            )
        ),

        entry(
            PART_ADMISSION.fullName(),
            List.of(
                WITHDRAW_CLAIM,
                CLAIMANT_RESPONSE_CUI,
                ADD_DEFENDANT_LITIGATION_FRIEND,
                DISCONTINUE_CLAIM,
                CASE_PROCEEDS_IN_CASEMAN,
                AMEND_PARTY_DETAILS,
                ADD_CASE_NOTE,
                CHANGE_SOLICITOR_EMAIL,
                INITIATE_GENERAL_APPLICATION,
                NOT_SUITABLE_SDO,
                CREATE_SDO,
                APPLICATION_OFFLINE_UPDATE_CLAIM,
                MIGRATE_CASE,
                DEFENDANT_SIGN_SETTLEMENT_AGREEMENT,
                TRANSFER_ONLINE_CASE
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
                NOT_SUITABLE_SDO,
                APPLICATION_OFFLINE_UPDATE_CLAIM,
                MIGRATE_CASE,
                TRANSFER_ONLINE_CASE
            )
        ),

        entry(
            DIVERGENT_RESPOND_GO_OFFLINE.fullName(),
            List.of(
                APPLICATION_OFFLINE_UPDATE_CLAIM,
                MIGRATE_CASE
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
                NOT_SUITABLE_SDO,
                HEARING_SCHEDULED,
                HEARING_FEE_PAID,
                HEARING_FEE_UNPAID,
                TRIAL_READY_CHECK,
                TRIAL_READY_NOTIFICATION,
                MOVE_TO_DECISION_OUTCOME,
                SERVICE_REQUEST_RECEIVED,
                REFER_TO_JUDGE,
                MIGRATE_CASE,
                TAKE_CASE_OFFLINE,
                GENERATE_DIRECTIONS_ORDER,
                TRIAL_READINESS,
                EVIDENCE_UPLOAD_APPLICANT,
                EVIDENCE_UPLOAD_RESPONDENT,
                EVIDENCE_UPLOAD_JUDGE,
                TRIAL_READINESS,
                BUNDLE_CREATION_NOTIFICATION,
                ADD_UNAVAILABLE_DATES,
                SET_ASIDE_JUDGMENT,
                JUDGMENT_PAID_IN_FULL,
                REFER_JUDGE_DEFENCE_RECEIVED,
                RECORD_JUDGMENT,
                TRANSFER_ONLINE_CASE,
                CLAIMANT_RESPONSE_CUI,
                ASYNC_STITCHING_COMPLETE,
                REQUEST_FOR_RECONSIDERATION,
                DECISION_ON_RECONSIDERATION_REQUEST,
                EDIT_JUDGMENT,
                COURT_OFFICER_ORDER
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
                NOT_SUITABLE_SDO,
                REFER_TO_JUDGE,
                MIGRATE_CASE,
                TRANSFER_ONLINE_CASE
            )
        ),
        entry(
            CLAIM_DISMISSED_PAST_CLAIM_NOTIFICATION_DEADLINE.fullName(),
            List.of(
                CASE_PROCEEDS_IN_CASEMAN,
                ADD_CASE_NOTE,
                INITIATE_GENERAL_APPLICATION,
                CREATE_SDO,
                NOT_SUITABLE_SDO,
                APPLICATION_CLOSED_UPDATE_CLAIM,
                REFER_TO_JUDGE,
                MIGRATE_CASE,
                TRANSFER_ONLINE_CASE
            )
        ),
        entry(
            CLAIM_DISMISSED_PAST_CLAIM_DETAILS_NOTIFICATION_DEADLINE.fullName(),
            List.of(
                CASE_PROCEEDS_IN_CASEMAN,
                ADD_CASE_NOTE,
                INITIATE_GENERAL_APPLICATION,
                CREATE_SDO,
                NOT_SUITABLE_SDO,
                REFER_TO_JUDGE,
                APPLICATION_CLOSED_UPDATE_CLAIM,
                MIGRATE_CASE,
                TRANSFER_ONLINE_CASE
            )
        ),
        entry(
            CLAIM_DISMISSED_HEARING_FEE_DUE_DEADLINE.fullName(),
            List.of(
                CASE_PROCEEDS_IN_CASEMAN,
                ADD_CASE_NOTE
            )
        ),
        entry(
            PENDING_CLAIM_ISSUED_UNREPRESENTED_DEFENDANT.fullName(),
            List.of(
                NOC_REQUEST,
                APPLY_NOC_DECISION,
                TAKE_CASE_OFFLINE,
                NOTIFY_DEFENDANT_OF_CLAIM,
                APPLICATION_OFFLINE_UPDATE_CLAIM,
                MIGRATE_CASE
            )
        ),
        entry(
            PAST_APPLICANT_RESPONSE_DEADLINE_AWAITING_CAMUNDA.fullName(),
            List.of(
                TAKE_CASE_OFFLINE,
                APPLICATION_OFFLINE_UPDATE_CLAIM,
                MIGRATE_CASE,
                NOT_SUITABLE_SDO,
                CREATE_SDO,
                TRANSFER_ONLINE_CASE
            )
        ),
        entry(
            PAST_CLAIM_NOTIFICATION_DEADLINE_AWAITING_CAMUNDA.fullName(),
            List.of(
                DISMISS_CLAIM,
                ADD_CASE_NOTE,
                INITIATE_GENERAL_APPLICATION,
                CREATE_SDO,
                NOT_SUITABLE_SDO,
                REFER_TO_JUDGE,
                MIGRATE_CASE,
                TRANSFER_ONLINE_CASE
            )
        ),
        entry(
            PAST_CLAIM_DETAILS_NOTIFICATION_DEADLINE_AWAITING_CAMUNDA.fullName(),
            List.of(
                DISMISS_CLAIM,
                ADD_CASE_NOTE,
                INITIATE_GENERAL_APPLICATION,
                CREATE_SDO,
                NOT_SUITABLE_SDO,
                REFER_TO_JUDGE,
                MIGRATE_CASE,
                TRANSFER_ONLINE_CASE
            )
        ),
        entry(
            PAST_CLAIM_DISMISSED_DEADLINE_AWAITING_CAMUNDA.fullName(),
            List.of(
                DISMISS_CLAIM,
                MIGRATE_CASE,
                CREATE_SDO,
                NOT_SUITABLE_SDO,
                TRANSFER_ONLINE_CASE
            )
        ),
        entry(
            TAKEN_OFFLINE_BY_STAFF.fullName(),
            List.of(
                ADD_CASE_NOTE,
                APPLICATION_OFFLINE_UPDATE_CLAIM,
                MIGRATE_CASE
            )
        ),
        entry(
            TAKEN_OFFLINE_UNREGISTERED_DEFENDANT.fullName(),
            List.of(
                ADD_CASE_NOTE,
                APPLICATION_OFFLINE_UPDATE_CLAIM,
                MIGRATE_CASE
            )
        ),
        entry(
            TAKEN_OFFLINE_UNREPRESENTED_DEFENDANT.fullName(),
            List.of(
                ADD_CASE_NOTE,
                APPLICATION_OFFLINE_UPDATE_CLAIM,
                MIGRATE_CASE
            )
        ),
        entry(
            TAKEN_OFFLINE_UNREPRESENTED_UNREGISTERED_DEFENDANT.fullName(),
            List.of(
                ADD_CASE_NOTE,
                APPLICATION_OFFLINE_UPDATE_CLAIM,
                MIGRATE_CASE
            )
        ),
        entry(
            TAKEN_OFFLINE_PAST_APPLICANT_RESPONSE_DEADLINE.fullName(),
            List.of(
                ADD_CASE_NOTE,
                APPLICATION_OFFLINE_UPDATE_CLAIM,
                MIGRATE_CASE
            )
        ),
        entry(
            TAKEN_OFFLINE_AFTER_CLAIM_DETAILS_NOTIFIED.fullName(),
            List.of(
                ADD_CASE_NOTE,
                APPLICATION_OFFLINE_UPDATE_CLAIM,
                MIGRATE_CASE
            )
        ),
        entry(
            TAKEN_OFFLINE_AFTER_CLAIM_NOTIFIED.fullName(),
            List.of(
                ADD_CASE_NOTE,
                APPLICATION_OFFLINE_UPDATE_CLAIM,
                MIGRATE_CASE
            )
        ),
        entry(
            TAKEN_OFFLINE_SDO_NOT_DRAWN.fullName(),
            List.of(
                ADD_CASE_NOTE,
                MIGRATE_CASE
            )
        ),
        entry(
            TAKEN_OFFLINE_AFTER_SDO.fullName(),
            List.of(
                ADD_CASE_NOTE,
                AMEND_PARTY_DETAILS
            )
        ),
        entry(
            CLAIM_DISMISSED_PAST_CLAIM_DISMISSED_DEADLINE.fullName(),
            List.of(
                APPLICATION_CLOSED_UPDATE_CLAIM,
                MIGRATE_CASE
            )
        ),
        entry(
            IN_MEDIATION.fullName(),
            List.of(
                MEDIATION_SUCCESSFUL,
                MEDIATION_UNSUCCESSFUL,
                ADD_UNAVAILABLE_DATES,
                INITIATE_GENERAL_APPLICATION,
                TRANSFER_ONLINE_CASE
            )
        ),
        entry(
            IN_HEARING_READINESS.fullName(),
            List.of(
                NOC_REQUEST,
                APPLY_NOC_DECISION,
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
                NOT_SUITABLE_SDO,
                TAKE_CASE_OFFLINE,
                EVIDENCE_UPLOAD_JUDGE,
                HEARING_FEE_UNPAID,
                HEARING_FEE_PAID,
                HEARING_SCHEDULED,
                TRIAL_READY_CHECK,
                TRIAL_READY_NOTIFICATION,
                MOVE_TO_DECISION_OUTCOME,
                SERVICE_REQUEST_RECEIVED,
                EVIDENCE_UPLOAD_APPLICANT,
                MIGRATE_CASE,
                EVIDENCE_UPLOAD_RESPONDENT,
                GENERATE_DIRECTIONS_ORDER,
                TRIAL_READINESS,
                BUNDLE_CREATION_NOTIFICATION,
                ADD_UNAVAILABLE_DATES,
                ASYNC_STITCHING_COMPLETE,
                TRANSFER_ONLINE_CASE,
                INVALID_HWF_REFERENCE,
                COURT_OFFICER_ORDER
            )
        ),
        entry(
            CASE_STAYED.fullName(),
            List.of(
                INITIATE_GENERAL_APPLICATION,
                ADD_UNAVAILABLE_DATES,
                CHANGE_SOLICITOR_EMAIL
            )
        )
    );

    private static final Map<String, List<CaseEvent>> ALLOWED_EVENTS_ON_FLOW_STATE_SPEC = Map.ofEntries(
        entry(
            DRAFT.fullName(),
            List.of(
                CREATE_CLAIM,
                CREATE_LIP_CLAIM,
                MIGRATE_CASE
            )
        ),

        entry(
            SPEC_DRAFT.fullName(),
            List.of(
                CREATE_CLAIM_SPEC,
                CREATE_LIP_CLAIM,
                MIGRATE_CASE
            )
        ),

        entry(
            CLAIM_ISSUED_PAYMENT_SUCCESSFUL.fullName(),
            List.of(NOC_REQUEST,
                    APPLY_NOC_DECISION,
                    ADD_CASE_NOTE,
                    INITIATE_GENERAL_APPLICATION,
                    CREATE_SDO,
                    NOT_SUITABLE_SDO,
                    MIGRATE_CASE,
                    CREATE_CLAIM_SPEC_AFTER_PAYMENT,
                    CREATE_CLAIM_AFTER_PAYMENT,
                    TRANSFER_ONLINE_CASE
            )
        ),

        entry(
            CLAIM_ISSUED_PAYMENT_FAILED.fullName(),
            List.of(
                ENTER_BREATHING_SPACE_SPEC,
                ENTER_BREATHING_SPACE_LIP,
                LIFT_BREATHING_SPACE_SPEC,
                LIFT_BREATHING_SPACE_LIP,
                RESUBMIT_CLAIM,
                WITHDRAW_CLAIM,
                DISCONTINUE_CLAIM,
                AMEND_PARTY_DETAILS,
                INITIATE_GENERAL_APPLICATION,
                CREATE_SDO,
                NOT_SUITABLE_SDO,
                MIGRATE_CASE,
                TRANSFER_ONLINE_CASE
            )
        ),

        entry(
            CLAIM_ISSUED.fullName(),
            List.of(
                ENTER_BREATHING_SPACE_SPEC,
                ENTER_BREATHING_SPACE_LIP,
                LIFT_BREATHING_SPACE_SPEC,
                LIFT_BREATHING_SPACE_LIP,
                NOTIFY_DEFENDANT_OF_CLAIM,
                ADD_DEFENDANT_LITIGATION_FRIEND,
                CASE_PROCEEDS_IN_CASEMAN,
                ADD_OR_AMEND_CLAIM_DOCUMENTS,
                ADD_CASE_NOTE,
                AMEND_PARTY_DETAILS,
                ACKNOWLEDGEMENT_OF_SERVICE,
                INFORM_AGREED_EXTENSION_DATE,
                INFORM_AGREED_EXTENSION_DATE_SPEC,
                NOTIFY_DEFENDANT_CUI_FOR_DEADLINE_EXTENSION,
                NOTIFY_CLAIMANT_CUI_FOR_DEADLINE_EXTENSION,
                EXTEND_RESPONSE_DEADLINE,
                DEFENDANT_RESPONSE_SPEC,
                DEFENDANT_RESPONSE_CUI,
                RESET_PIN,
                DISMISS_CLAIM,
                DISCONTINUE_CLAIM,
                WITHDRAW_CLAIM,
                DEFAULT_JUDGEMENT_SPEC,
                INITIATE_GENERAL_APPLICATION,
                CREATE_SDO,
                HEARING_SCHEDULED,
                NOT_SUITABLE_SDO,
                EVIDENCE_UPLOAD_JUDGE,
                TRIAL_READY_CHECK,
                TRIAL_READY_NOTIFICATION,
                MOVE_TO_DECISION_OUTCOME,
                CREATE_CLAIM_SPEC_AFTER_PAYMENT,
                CREATE_CLAIM_AFTER_PAYMENT,
                EVIDENCE_UPLOAD_APPLICANT,
                MIGRATE_CASE,
                EVIDENCE_UPLOAD_RESPONDENT,
                BUNDLE_CREATION_NOTIFICATION,
                CHANGE_SOLICITOR_EMAIL,
                LIP_CLAIM_SETTLED,
                ASYNC_STITCHING_COMPLETE,
                TRANSFER_ONLINE_CASE,
                INVALID_HWF_REFERENCE
            )
        ),
        entry(
            CLAIM_NOTIFIED.fullName(),
            List.of(
                ACKNOWLEDGEMENT_OF_SERVICE,
                ENTER_BREATHING_SPACE_SPEC,
                ENTER_BREATHING_SPACE_LIP,
                LIFT_BREATHING_SPACE_LIP,
                LIFT_BREATHING_SPACE_SPEC,
                INFORM_AGREED_EXTENSION_DATE,
                INFORM_AGREED_EXTENSION_DATE_SPEC,
                EXTEND_RESPONSE_DEADLINE,
                DEFENDANT_RESPONSE_SPEC,
                DEFENDANT_RESPONSE_CUI,
                RESET_PIN,
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
                NOT_SUITABLE_SDO,
                HEARING_SCHEDULED,
                TRIAL_READY_CHECK,
                TRIAL_READY_NOTIFICATION,
                MOVE_TO_DECISION_OUTCOME,
                EVIDENCE_UPLOAD_JUDGE,
                EVIDENCE_UPLOAD_APPLICANT,
                MIGRATE_CASE,
                EVIDENCE_UPLOAD_RESPONDENT,
                BUNDLE_CREATION_NOTIFICATION,
                CHANGE_SOLICITOR_EMAIL,
                LIP_CLAIM_SETTLED,
                ASYNC_STITCHING_COMPLETE,
                TRANSFER_ONLINE_CASE
            )
        ),
        entry(
            NOTIFICATION_ACKNOWLEDGED.fullName(),
            List.of(
                DEFENDANT_RESPONSE,
                ENTER_BREATHING_SPACE_SPEC,
                ENTER_BREATHING_SPACE_LIP,
                LIFT_BREATHING_SPACE_LIP,
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
                NOT_SUITABLE_SDO,
                MIGRATE_CASE,
                CHANGE_SOLICITOR_EMAIL,
                TRANSFER_ONLINE_CASE
            )
        ),

        entry(
            NOTIFICATION_ACKNOWLEDGED_TIME_EXTENSION.fullName(),
            List.of(
                DEFENDANT_RESPONSE,
                ENTER_BREATHING_SPACE_SPEC,
                ENTER_BREATHING_SPACE_LIP,
                LIFT_BREATHING_SPACE_LIP,
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
                NOT_SUITABLE_SDO,
                MIGRATE_CASE,
                CHANGE_SOLICITOR_EMAIL,
                LIP_CLAIM_SETTLED,
                TRANSFER_ONLINE_CASE,
                GENERATE_DIRECTIONS_ORDER,
                EVIDENCE_UPLOAD_APPLICANT,
                EVIDENCE_UPLOAD_RESPONDENT,
                EVIDENCE_UPLOAD_JUDGE,
                TRIAL_READINESS,
                HEARING_SCHEDULED,
                TRIAL_READY_CHECK,
                TRIAL_READY_NOTIFICATION,
                MOVE_TO_DECISION_OUTCOME,
                HEARING_FEE_UNPAID,
                HEARING_FEE_PAID,
                BUNDLE_CREATION_NOTIFICATION,
                COURT_OFFICER_ORDER
            )
        ),

        entry(
            FULL_DEFENCE.fullName(),
            List.of(
                CLAIMANT_RESPONSE,
                CLAIMANT_RESPONSE_SPEC,
                ENTER_BREATHING_SPACE_SPEC,
                ENTER_BREATHING_SPACE_LIP,
                LIFT_BREATHING_SPACE_LIP,
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
                NOT_SUITABLE_SDO,
                MIGRATE_CASE,
                DEFAULT_JUDGEMENT_SPEC,
                CHANGE_SOLICITOR_EMAIL,
                LIP_CLAIM_SETTLED,
                CLAIMANT_RESPONSE_CUI,
                TRANSFER_ONLINE_CASE
            )
        ),

        entry(
            FULL_ADMISSION.fullName(),
            List.of(
                CLAIMANT_RESPONSE_SPEC,
                ENTER_BREATHING_SPACE_SPEC,
                ENTER_BREATHING_SPACE_LIP,
                LIFT_BREATHING_SPACE_LIP,
                LIFT_BREATHING_SPACE_SPEC,
                WITHDRAW_CLAIM,
                ADD_DEFENDANT_LITIGATION_FRIEND,
                DISCONTINUE_CLAIM,
                CASE_PROCEEDS_IN_CASEMAN,
                AMEND_PARTY_DETAILS,
                INITIATE_GENERAL_APPLICATION,
                CREATE_SDO,
                NOT_SUITABLE_SDO,
                APPLICATION_OFFLINE_UPDATE_CLAIM,
                MIGRATE_CASE,
                DEFAULT_JUDGEMENT_SPEC,
                REQUEST_JUDGEMENT_ADMISSION_SPEC,
                CHANGE_SOLICITOR_EMAIL,
                LIP_CLAIM_SETTLED,
                CLAIMANT_RESPONSE_CUI,
                DEFENDANT_SIGN_SETTLEMENT_AGREEMENT,
                TRANSFER_ONLINE_CASE
            )
        ),

        entry(
            PART_ADMISSION.fullName(),
            List.of(
                DEFENDANT_RESPONSE_CUI,
                CLAIMANT_RESPONSE_SPEC,
                CLAIMANT_RESPONSE_CUI,
                ENTER_BREATHING_SPACE_SPEC,
                ENTER_BREATHING_SPACE_LIP,
                LIFT_BREATHING_SPACE_LIP,
                LIFT_BREATHING_SPACE_SPEC,
                WITHDRAW_CLAIM,
                ADD_DEFENDANT_LITIGATION_FRIEND,
                DISCONTINUE_CLAIM,
                CASE_PROCEEDS_IN_CASEMAN,
                AMEND_PARTY_DETAILS,
                INITIATE_GENERAL_APPLICATION,
                CREATE_SDO,
                NOT_SUITABLE_SDO,
                APPLICATION_OFFLINE_UPDATE_CLAIM,
                MIGRATE_CASE,
                DEFAULT_JUDGEMENT_SPEC,
                CHANGE_SOLICITOR_EMAIL,
                REQUEST_JUDGEMENT_ADMISSION_SPEC,
                LIP_CLAIM_SETTLED,
                DEFENDANT_SIGN_SETTLEMENT_AGREEMENT,
                TRANSFER_ONLINE_CASE
            )
        ),

        entry(
            DIVERGENT_RESPOND_GO_OFFLINE.fullName(),
            List.of(
                APPLICATION_OFFLINE_UPDATE_CLAIM,
                MIGRATE_CASE
            )
        ),

        entry(
            COUNTER_CLAIM.fullName(),
            List.of(
                ENTER_BREATHING_SPACE_SPEC,
                ENTER_BREATHING_SPACE_LIP,
                LIFT_BREATHING_SPACE_LIP,
                LIFT_BREATHING_SPACE_SPEC,
                WITHDRAW_CLAIM,
                ADD_DEFENDANT_LITIGATION_FRIEND,
                DISCONTINUE_CLAIM,
                CASE_PROCEEDS_IN_CASEMAN,
                AMEND_PARTY_DETAILS,
                INITIATE_GENERAL_APPLICATION,
                CREATE_SDO,
                NOT_SUITABLE_SDO,
                MIGRATE_CASE,
                CHANGE_SOLICITOR_EMAIL,
                LIP_CLAIM_SETTLED,
                TRANSFER_ONLINE_CASE
            )
        ),
        entry(PART_ADMIT_REJECT_REPAYMENT.fullName(),
              List.of(
                  DEFENDANT_SIGN_SETTLEMENT_AGREEMENT,
                  REQUEST_JUDGEMENT_ADMISSION_SPEC
              )
        ),
        entry(PART_ADMIT_PROCEED.fullName(),
              List.of(DEFENDANT_SIGN_SETTLEMENT_AGREEMENT)),
        entry(PART_ADMIT_NOT_PROCEED.fullName(),
              List.of(DEFENDANT_SIGN_SETTLEMENT_AGREEMENT)),
        entry(PART_ADMIT_PAY_IMMEDIATELY.fullName(),
              List.of(
                  DEFENDANT_SIGN_SETTLEMENT_AGREEMENT,
                  CLAIMANT_RESPONSE_CUI
              )
        ),
        entry(PART_ADMIT_AGREE_SETTLE.fullName(),
              List.of(DEFENDANT_SIGN_SETTLEMENT_AGREEMENT)),
        entry(FULL_ADMIT_PAY_IMMEDIATELY.fullName(),
              List.of(DEFENDANT_SIGN_SETTLEMENT_AGREEMENT)),
        entry(FULL_ADMIT_PROCEED.fullName(),
              List.of(DEFENDANT_SIGN_SETTLEMENT_AGREEMENT)),
        entry(FULL_ADMIT_NOT_PROCEED.fullName(),
              List.of(DEFENDANT_SIGN_SETTLEMENT_AGREEMENT)),
        entry(FULL_ADMIT_JUDGMENT_ADMISSION.fullName(),
              List.of(DEFENDANT_SIGN_SETTLEMENT_AGREEMENT)),
        entry(
            FULL_DEFENCE_PROCEED.fullName(),
            List.of(
                ADD_DEFENDANT_LITIGATION_FRIEND,
                ENTER_BREATHING_SPACE_SPEC,
                ENTER_BREATHING_SPACE_LIP,
                LIFT_BREATHING_SPACE_LIP,
                LIFT_BREATHING_SPACE_SPEC,
                WITHDRAW_CLAIM,
                DISCONTINUE_CLAIM,
                CASE_PROCEEDS_IN_CASEMAN,
                AMEND_PARTY_DETAILS,
                INITIATE_GENERAL_APPLICATION,
                CREATE_SDO,
                NOT_SUITABLE_SDO,
                HEARING_SCHEDULED,
                TRIAL_READY_CHECK,
                TRIAL_READY_NOTIFICATION,
                MOVE_TO_DECISION_OUTCOME,
                HEARING_FEE_UNPAID,
                HEARING_FEE_PAID,
                REFER_TO_JUDGE,
                MIGRATE_CASE,
                TAKE_CASE_OFFLINE,
                GENERATE_DIRECTIONS_ORDER,
                TRIAL_READINESS,
                EVIDENCE_UPLOAD_APPLICANT,
                EVIDENCE_UPLOAD_RESPONDENT,
                EVIDENCE_UPLOAD_JUDGE,
                TRIAL_READINESS,
                BUNDLE_CREATION_NOTIFICATION,
                ADD_CASE_NOTE,
                CHANGE_SOLICITOR_EMAIL,
                ADD_UNAVAILABLE_DATES,
                SET_ASIDE_JUDGMENT,
                JUDGMENT_PAID_IN_FULL,
                REFER_JUDGE_DEFENCE_RECEIVED,
                RECORD_JUDGMENT,
                LIP_CLAIM_SETTLED,
                TRANSFER_ONLINE_CASE,
                ASYNC_STITCHING_COMPLETE,
                CLAIMANT_RESPONSE_CUI,
                UPLOAD_MEDIATION_DOCUMENTS,
                CUI_UPLOAD_MEDIATION_DOCUMENTS,
                REQUEST_FOR_RECONSIDERATION,
                DECISION_ON_RECONSIDERATION_REQUEST,
                EDIT_JUDGMENT,
                COURT_OFFICER_ORDER
            )
        ),

        entry(
            FULL_DEFENCE_NOT_PROCEED.fullName(),
            List.of(
                ADD_DEFENDANT_LITIGATION_FRIEND,
                ENTER_BREATHING_SPACE_SPEC,
                ENTER_BREATHING_SPACE_LIP,
                LIFT_BREATHING_SPACE_LIP,
                LIFT_BREATHING_SPACE_SPEC,
                WITHDRAW_CLAIM,
                DISCONTINUE_CLAIM,
                CASE_PROCEEDS_IN_CASEMAN,
                AMEND_PARTY_DETAILS,
                INITIATE_GENERAL_APPLICATION,
                CREATE_SDO,
                NOT_SUITABLE_SDO,
                REFER_TO_JUDGE,
                MIGRATE_CASE,
                CHANGE_SOLICITOR_EMAIL,
                LIP_CLAIM_SETTLED,
                TRANSFER_ONLINE_CASE
            )
        ),
        entry(
            CLAIM_DISMISSED_PAST_CLAIM_NOTIFICATION_DEADLINE.fullName(),
            List.of(
                CASE_PROCEEDS_IN_CASEMAN,
                MIGRATE_CASE
            )
        ),
        entry(
            CLAIM_DISMISSED_PAST_CLAIM_DETAILS_NOTIFICATION_DEADLINE.fullName(),
            List.of(
                CASE_PROCEEDS_IN_CASEMAN,
                MIGRATE_CASE
            )
        ),
        entry(
            PAST_APPLICANT_RESPONSE_DEADLINE_AWAITING_CAMUNDA.fullName(),
            List.of(
                TAKE_CASE_OFFLINE,
                APPLICATION_OFFLINE_UPDATE_CLAIM,
                MIGRATE_CASE,
                TRANSFER_ONLINE_CASE
            )
        ),
        entry(
            PAST_CLAIM_NOTIFICATION_DEADLINE_AWAITING_CAMUNDA.fullName(),
            List.of(
                DISMISS_CLAIM,
                migrateCase,
                TRANSFER_ONLINE_CASE
            )
        ),
        entry(
            PAST_CLAIM_DETAILS_NOTIFICATION_DEADLINE_AWAITING_CAMUNDA.fullName(),
            List.of(
                DISMISS_CLAIM,
                migrateCase,
                TRANSFER_ONLINE_CASE
            )
        ),
        entry(
            PAST_CLAIM_DISMISSED_DEADLINE_AWAITING_CAMUNDA.fullName(),
            List.of(
                DISMISS_CLAIM,
                migrateCase
            )
        ),
        entry(
            AWAITING_RESPONSES_FULL_DEFENCE_RECEIVED.fullName(),
            List.of(
                DEFENDANT_RESPONSE_SPEC,
                DEFENDANT_RESPONSE_CUI,
                RESET_PIN,
                ACKNOWLEDGE_CLAIM,
                ENTER_BREATHING_SPACE_SPEC,
                ENTER_BREATHING_SPACE_LIP,
                LIFT_BREATHING_SPACE_LIP,
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
                NOT_SUITABLE_SDO,
                migrateCase,
                TRANSFER_ONLINE_CASE
            )
        ),
        entry(
            AWAITING_RESPONSES_NOT_FULL_DEFENCE_RECEIVED.fullName(),
            List.of(
                DEFENDANT_RESPONSE_SPEC,
                DEFENDANT_RESPONSE_CUI,
                RESET_PIN,
                migrateCase,
                TRANSFER_ONLINE_CASE
            )
        ),
        entry(
            IN_MEDIATION.fullName(),
            List.of(
                MEDIATION_SUCCESSFUL,
                MEDIATION_UNSUCCESSFUL,
                CREATE_SDO,
                CHANGE_SOLICITOR_EMAIL,
                ADD_UNAVAILABLE_DATES,
                LIP_CLAIM_SETTLED,
                INITIATE_GENERAL_APPLICATION,
                TRANSFER_ONLINE_CASE
            )
        ),
        entry(
            IN_HEARING_READINESS.fullName(),
            List.of(
                ADD_DEFENDANT_LITIGATION_FRIEND,
                ENTER_BREATHING_SPACE_SPEC,
                ENTER_BREATHING_SPACE_LIP,
                LIFT_BREATHING_SPACE_LIP,
                LIFT_BREATHING_SPACE_SPEC,
                WITHDRAW_CLAIM,
                DISCONTINUE_CLAIM,
                CASE_PROCEEDS_IN_CASEMAN,
                AMEND_PARTY_DETAILS,
                INITIATE_GENERAL_APPLICATION,
                CREATE_SDO,
                NOT_SUITABLE_SDO,
                HEARING_SCHEDULED,
                TRIAL_READY_CHECK,
                TRIAL_READY_NOTIFICATION,
                MOVE_TO_DECISION_OUTCOME,
                HEARING_FEE_UNPAID,
                HEARING_FEE_PAID,
                REFER_TO_JUDGE,
                migrateCase,
                TAKE_CASE_OFFLINE,
                GENERATE_DIRECTIONS_ORDER,
                TRIAL_READINESS,
                EVIDENCE_UPLOAD_APPLICANT,
                EVIDENCE_UPLOAD_RESPONDENT,
                EVIDENCE_UPLOAD_JUDGE,
                TRIAL_READINESS,
                BUNDLE_CREATION_NOTIFICATION,
                CHANGE_SOLICITOR_EMAIL,
                ADD_UNAVAILABLE_DATES,
                LIP_CLAIM_SETTLED,
                ASYNC_STITCHING_COMPLETE,
                UPLOAD_MEDIATION_DOCUMENTS,
                MORE_INFORMATION_HWF,
                FEE_PAYMENT_OUTCOME,
                NO_REMISSION_HWF,
                CUI_UPLOAD_MEDIATION_DOCUMENTS,
                TRANSFER_ONLINE_CASE,
                PARTIAL_REMISSION_HWF_GRANTED,
                FULL_REMISSION_HWF,
                UPDATE_HELP_WITH_FEE_NUMBER,
                INVALID_HWF_REFERENCE,
                COURT_OFFICER_ORDER
            )
        ),
        entry(
            PART_ADMIT_NOT_SETTLED_NO_MEDIATION.fullName(),
            List.of(
                ADD_DEFENDANT_LITIGATION_FRIEND,
                ENTER_BREATHING_SPACE_SPEC,
                ENTER_BREATHING_SPACE_LIP,
                LIFT_BREATHING_SPACE_LIP,
                LIFT_BREATHING_SPACE_SPEC,
                WITHDRAW_CLAIM,
                DISCONTINUE_CLAIM,
                CASE_PROCEEDS_IN_CASEMAN,
                AMEND_PARTY_DETAILS,
                INITIATE_GENERAL_APPLICATION,
                CREATE_SDO,
                NOT_SUITABLE_SDO,
                HEARING_SCHEDULED,
                TRIAL_READY_CHECK,
                TRIAL_READY_NOTIFICATION,
                MOVE_TO_DECISION_OUTCOME,
                HEARING_FEE_UNPAID,
                HEARING_FEE_PAID,
                REFER_TO_JUDGE,
                migrateCase,
                TAKE_CASE_OFFLINE,
                GENERATE_DIRECTIONS_ORDER,
                TRIAL_READINESS,
                EVIDENCE_UPLOAD_APPLICANT,
                EVIDENCE_UPLOAD_RESPONDENT,
                EVIDENCE_UPLOAD_JUDGE,
                TRIAL_READINESS,
                BUNDLE_CREATION_NOTIFICATION,
                ADD_CASE_NOTE,
                CHANGE_SOLICITOR_EMAIL,
                ADD_UNAVAILABLE_DATES,
                LIP_CLAIM_SETTLED,
                ASYNC_STITCHING_COMPLETE,
                CLAIMANT_RESPONSE_CUI,
                DEFENDANT_SIGN_SETTLEMENT_AGREEMENT,
                TRANSFER_ONLINE_CASE,
                COURT_OFFICER_ORDER
            )
        ),
        entry(
            RESPONDENT_RESPONSE_LANGUAGE_IS_BILINGUAL.fullName(),
            List.of(
                CHANGE_SOLICITOR_EMAIL,
                EXTEND_RESPONSE_DEADLINE,
                LIP_CLAIM_SETTLED,
                TRANSFER_ONLINE_CASE

            )
        ),
        entry(
            MEDIATION_UNSUCCESSFUL_PROCEED.fullName(),
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
                NOT_SUITABLE_SDO,
                HEARING_SCHEDULED,
                TRIAL_READY_CHECK,
                TRIAL_READY_NOTIFICATION,
                MOVE_TO_DECISION_OUTCOME,
                HEARING_FEE_UNPAID,
                HEARING_FEE_PAID,
                REFER_TO_JUDGE,
                migrateCase,
                TAKE_CASE_OFFLINE,
                GENERATE_DIRECTIONS_ORDER,
                TRIAL_READINESS,
                EVIDENCE_UPLOAD_APPLICANT,
                EVIDENCE_UPLOAD_RESPONDENT,
                EVIDENCE_UPLOAD_JUDGE,
                TRIAL_READINESS,
                BUNDLE_CREATION_NOTIFICATION,
                CHANGE_SOLICITOR_EMAIL,
                ADD_UNAVAILABLE_DATES,
                ASYNC_STITCHING_COMPLETE,
                UPLOAD_MEDIATION_DOCUMENTS,
                CUI_UPLOAD_MEDIATION_DOCUMENTS,
                TRANSFER_ONLINE_CASE,
                COURT_OFFICER_ORDER
            )
        ),
        entry(
            PREPARE_FOR_HEARING_CONDUCT_HEARING.fullName(),
            List.of(
                ASYNC_STITCHING_COMPLETE,
                UPLOAD_MEDIATION_DOCUMENTS,
                CUI_UPLOAD_MEDIATION_DOCUMENTS,
                TRANSFER_ONLINE_CASE
            )
        ),
        entry(
            TAKEN_OFFLINE_BY_STAFF.fullName(),
            List.of(
                ADD_CASE_NOTE,
                APPLICATION_OFFLINE_UPDATE_CLAIM,
                migrateCase
            )
        ),
        entry(
            TAKEN_OFFLINE_UNREGISTERED_DEFENDANT.fullName(),
            List.of(
                ADD_CASE_NOTE,
                APPLICATION_OFFLINE_UPDATE_CLAIM,
                migrateCase
            )
        ),
        entry(
            TAKEN_OFFLINE_UNREPRESENTED_DEFENDANT.fullName(),
            List.of(
                ADD_CASE_NOTE,
                APPLICATION_OFFLINE_UPDATE_CLAIM,
                migrateCase
            )
        ),
        entry(
            TAKEN_OFFLINE_UNREPRESENTED_UNREGISTERED_DEFENDANT.fullName(),
            List.of(
                ADD_CASE_NOTE,
                APPLICATION_OFFLINE_UPDATE_CLAIM,
                migrateCase
            )
        ),
        entry(
            TAKEN_OFFLINE_PAST_APPLICANT_RESPONSE_DEADLINE.fullName(),
            List.of(
                ADD_CASE_NOTE,
                APPLICATION_OFFLINE_UPDATE_CLAIM,
                migrateCase
            )
        ),
        entry(
            TAKEN_OFFLINE_AFTER_CLAIM_DETAILS_NOTIFIED.fullName(),
            List.of(
                ADD_CASE_NOTE,
                APPLICATION_OFFLINE_UPDATE_CLAIM,
                migrateCase
            )
        ),
        entry(
            TAKEN_OFFLINE_AFTER_CLAIM_NOTIFIED.fullName(),
            List.of(
                ADD_CASE_NOTE,
                APPLICATION_OFFLINE_UPDATE_CLAIM,
                migrateCase
            )
        ),
        entry(
            TAKEN_OFFLINE_SDO_NOT_DRAWN.fullName(),
            List.of(
                ADD_CASE_NOTE,
                migrateCase
            )
        ),
        entry(
            TAKEN_OFFLINE_AFTER_SDO.fullName(),
            List.of(
                ADD_CASE_NOTE,
                AMEND_PARTY_DETAILS
            )
        ),
        entry(
            PART_ADMIT_AGREE_REPAYMENT.fullName(),
            List.of(
                DEFENDANT_SIGN_SETTLEMENT_AGREEMENT,
                REQUEST_JUDGEMENT_ADMISSION_SPEC,
                TRANSFER_ONLINE_CASE
            )
        ),
        entry(
            FULL_ADMIT_REJECT_REPAYMENT.fullName(),
            List.of(
                DEFENDANT_SIGN_SETTLEMENT_AGREEMENT,
                REQUEST_JUDGEMENT_ADMISSION_SPEC
            )
        ),
        entry(
            FULL_ADMIT_AGREE_REPAYMENT.fullName(),
            List.of(
                DEFENDANT_SIGN_SETTLEMENT_AGREEMENT,
                REQUEST_JUDGEMENT_ADMISSION_SPEC,
                TRANSFER_ONLINE_CASE
            )
        ),
        entry(
            PENDING_CLAIM_ISSUED_UNREPRESENTED_DEFENDANT_ONE_V_ONE_SPEC.fullName(),
            List.of(
                UPLOAD_TRANSLATED_DOCUMENT,
                MORE_INFORMATION_HWF,
                PARTIAL_REMISSION_HWF_GRANTED,
                FEE_PAYMENT_OUTCOME,
                FULL_REMISSION_HWF,
                UPDATE_HELP_WITH_FEE_NUMBER,
                INVALID_HWF_REFERENCE,
                NO_REMISSION_HWF
            )
        ),
        entry(
            CASE_STAYED.fullName(),
            List.of(
                INITIATE_GENERAL_APPLICATION,
                ADD_UNAVAILABLE_DATES,
                CHANGE_SOLICITOR_EMAIL
            )
        ),
        entry(
            SIGN_SETTLEMENT_AGREEMENT.fullName(),
            List.of(
                REQUEST_JUDGEMENT_ADMISSION_SPEC
            )
        ),
        entry(
            CLAIM_DISMISSED_HEARING_FEE_DUE_DEADLINE.fullName(),
            List.of(
                CASE_PROCEEDS_IN_CASEMAN,
                ADD_CASE_NOTE
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
        if (EVENT_WHITELIST.contains(caseEvent)) {
            return true;
        }

        CaseData caseData = caseDetailsConverter.toCaseData(caseDetails);
        if (SPEC_CLAIM.equals(caseData.getCaseAccessCategory())
            || CREATE_CLAIM_SPEC.equals(caseEvent) || CREATE_LIP_CLAIM.equals(caseEvent)) {
            StateFlow stateFlow = stateFlowEngine.evaluateSpec(caseDetails);
            return isAllowedOnStateForSpec(stateFlow.getState().getName(), caseEvent);
        } else {
            StateFlow stateFlow = stateFlowEngine.evaluate(caseDetails);
            return isAllowedOnState(stateFlow.getState().getName(), caseEvent);
        }
    }

    public List<String> getAllowedStates(CaseEvent caseEvent) {
        if (caseEvent.equals(CREATE_CLAIM_SPEC)) {
            return ALLOWED_EVENTS_ON_FLOW_STATE_SPEC.entrySet().stream()
                .filter(entry -> entry.getValue().contains(caseEvent))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
        }
        return ALLOWED_EVENTS_ON_FLOW_STATE.entrySet().stream()
            .filter(entry -> entry.getValue().contains(caseEvent))
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());
    }
}
