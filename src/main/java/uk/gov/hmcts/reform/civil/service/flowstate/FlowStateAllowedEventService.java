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

import static java.util.Collections.emptyList;
import static java.util.Map.entry;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.DEFENDANT_SIGN_SETTLEMENT_AGREEMENT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.DISPATCH_BUSINESS_PROCESS;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.FEE_PAYMENT_OUTCOME;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NO_REMISSION_HWF;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.SEND_AND_REPLY;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.SETTLE_CLAIM;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.SETTLE_CLAIM_MARK_PAID_FULL;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.DISCONTINUE_CLAIM_CLAIMANT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.VALIDATE_DISCONTINUE_CLAIM_CLAIMANT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.UpdateNextHearingInfo;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.PARTIAL_REMISSION_HWF_GRANTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.UPDATE_NEXT_HEARING_DETAILS;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.asyncStitchingComplete;
import static uk.gov.hmcts.reform.civil.enums.CaseCategory.SPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.ACKNOWLEDGEMENT_OF_SERVICE;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.ACKNOWLEDGE_CLAIM;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.ADD_CASE_NOTE;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.ADD_DEFENDANT_LITIGATION_FRIEND;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.ADD_OR_AMEND_CLAIM_DOCUMENTS;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.ADD_UNAVAILABLE_DATES;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.AMEND_PARTY_DETAILS;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.AMEND_RESTITCH_BUNDLE;
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
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CONFIRM_ORDER_REVIEW;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.ORDER_REVIEW_OBLIGATION_CHECK;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.DECISION_ON_RECONSIDERATION_REQUEST;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.DEFAULT_JUDGEMENT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.DEFAULT_JUDGEMENT_SPEC;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.DEFENDANT_RESPONSE;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.DEFENDANT_RESPONSE_CUI;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.DEFENDANT_RESPONSE_SPEC;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.DISCONTINUE_CLAIM;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.DISMISS_CLAIM;
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
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.HEARING_SCHEDULED_RETRIGGER;
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
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NotSuitable_SDO;
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
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.STAY_CASE;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.DISMISS_CASE;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.MANAGE_STAY;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.TAKE_CASE_OFFLINE;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.TRANSFER_ONLINE_CASE;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.TRIAL_READINESS;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.TRIAL_READY_CHECK;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.TRIAL_READY_NOTIFICATION;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.UPLOAD_MEDIATION_DOCUMENTS;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CUI_UPLOAD_MEDIATION_DOCUMENTS;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.UPLOAD_TRANSLATED_DOCUMENT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.WITHDRAW_CLAIM;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.migrateCase;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.UPDATE_HELP_WITH_FEE_NUMBER;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CONFIRM_LISTING_COMPLETED;
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

    private final IStateFlowEngine stateFlowEngine;
    private final CaseDetailsConverter caseDetailsConverter;

    private static final List<CaseEvent> EVENT_WHITELIST = List.of(
        migrateCase,
        NOTIFY_HEARING_PARTIES,
        MANAGE_CONTACT_INFORMATION,
        CASE_PROCEEDS_IN_CASEMAN,
        UpdateNextHearingInfo,
        UPDATE_NEXT_HEARING_DETAILS,
        DISPATCH_BUSINESS_PROCESS,
        SEND_AND_REPLY
    );

    private static final Map<String, List<CaseEvent>> ALLOWED_EVENTS_ON_FLOW_STATE = Map.ofEntries(
        entry(
            DRAFT.fullName(),
            List.of(
                CREATE_CLAIM,
                migrateCase
            )
        ),

        entry(
            CLAIM_SUBMITTED.fullName(),
            List.of(
                ADD_CASE_NOTE,
                INITIATE_GENERAL_APPLICATION,
                CREATE_SDO,
                NotSuitable_SDO,
                REQUEST_FOR_RECONSIDERATION,
                migrateCase,
                TRANSFER_ONLINE_CASE,
                RECORD_JUDGMENT,
                EDIT_JUDGMENT,
                JUDGMENT_PAID_IN_FULL,
                SET_ASIDE_JUDGMENT,
                SETTLE_CLAIM,
                SETTLE_CLAIM_MARK_PAID_FULL,
                DISCONTINUE_CLAIM_CLAIMANT,
                VALIDATE_DISCONTINUE_CLAIM_CLAIMANT
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
                NotSuitable_SDO,
                REQUEST_FOR_RECONSIDERATION,
                migrateCase,
                TRANSFER_ONLINE_CASE,
                RECORD_JUDGMENT,
                EDIT_JUDGMENT,
                JUDGMENT_PAID_IN_FULL,
                SET_ASIDE_JUDGMENT,
                SETTLE_CLAIM,
                SETTLE_CLAIM_MARK_PAID_FULL,
                DISCONTINUE_CLAIM_CLAIMANT,
                VALIDATE_DISCONTINUE_CLAIM_CLAIMANT
            )
        ),

        entry(
            CLAIM_ISSUED_PAYMENT_SUCCESSFUL.fullName(),
            List.of(
                NOC_REQUEST,
                APPLY_NOC_DECISION,
                ADD_CASE_NOTE,
                INITIATE_GENERAL_APPLICATION,
                CREATE_SDO,
                NotSuitable_SDO,
                REQUEST_FOR_RECONSIDERATION,
                migrateCase,
                CREATE_CLAIM_SPEC_AFTER_PAYMENT,
                CREATE_CLAIM_AFTER_PAYMENT,
                TRANSFER_ONLINE_CASE,
                RECORD_JUDGMENT,
                EDIT_JUDGMENT,
                JUDGMENT_PAID_IN_FULL,
                SET_ASIDE_JUDGMENT,
                SETTLE_CLAIM,
                SETTLE_CLAIM_MARK_PAID_FULL,
                DISCONTINUE_CLAIM_CLAIMANT,
                VALIDATE_DISCONTINUE_CLAIM_CLAIMANT
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
                NotSuitable_SDO,
                REQUEST_FOR_RECONSIDERATION,
                EVIDENCE_UPLOAD_JUDGE,
                CREATE_CLAIM_AFTER_PAYMENT,
                CREATE_CLAIM_SPEC_AFTER_PAYMENT,
                EVIDENCE_UPLOAD_APPLICANT,
                migrateCase,
                EVIDENCE_UPLOAD_RESPONDENT,
                TRANSFER_ONLINE_CASE,
                INVALID_HWF_REFERENCE,
                SETTLE_CLAIM,
                SETTLE_CLAIM_MARK_PAID_FULL,
                DISCONTINUE_CLAIM_CLAIMANT,
                VALIDATE_DISCONTINUE_CLAIM_CLAIMANT,
                RECORD_JUDGMENT,
                EDIT_JUDGMENT,
                JUDGMENT_PAID_IN_FULL,
                SET_ASIDE_JUDGMENT,
                CONFIRM_ORDER_REVIEW,
                ORDER_REVIEW_OBLIGATION_CHECK
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
                REQUEST_FOR_RECONSIDERATION,
                EVIDENCE_UPLOAD_JUDGE,
                EVIDENCE_UPLOAD_APPLICANT,
                migrateCase,
                EVIDENCE_UPLOAD_RESPONDENT,
                TRANSFER_ONLINE_CASE,
                SETTLE_CLAIM,
                SETTLE_CLAIM_MARK_PAID_FULL,
                DISCONTINUE_CLAIM_CLAIMANT,
                VALIDATE_DISCONTINUE_CLAIM_CLAIMANT,
                CONFIRM_ORDER_REVIEW,
                ORDER_REVIEW_OBLIGATION_CHECK
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
                REQUEST_FOR_RECONSIDERATION,
                TAKE_CASE_OFFLINE,
                EVIDENCE_UPLOAD_JUDGE,
                HEARING_FEE_UNPAID,
                HEARING_FEE_PAID,
                HEARING_SCHEDULED,
                HEARING_SCHEDULED_RETRIGGER,
                CONFIRM_LISTING_COMPLETED,
                TRIAL_READY_CHECK,
                TRIAL_READY_NOTIFICATION,
                MOVE_TO_DECISION_OUTCOME,
                SERVICE_REQUEST_RECEIVED,
                EVIDENCE_UPLOAD_APPLICANT,
                migrateCase,
                EVIDENCE_UPLOAD_RESPONDENT,
                GENERATE_DIRECTIONS_ORDER,
                TRIAL_READINESS,
                BUNDLE_CREATION_NOTIFICATION,
                TRANSFER_ONLINE_CASE,
                asyncStitchingComplete,
                COURT_OFFICER_ORDER,
                SETTLE_CLAIM,
                SETTLE_CLAIM_MARK_PAID_FULL,
                DISCONTINUE_CLAIM_CLAIMANT,
                VALIDATE_DISCONTINUE_CLAIM_CLAIMANT,
                STAY_CASE,
                DISMISS_CASE,
                MANAGE_STAY,
                CONFIRM_ORDER_REVIEW,
                ORDER_REVIEW_OBLIGATION_CHECK
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
                NotSuitable_SDO,
                REQUEST_FOR_RECONSIDERATION,
                migrateCase,
                TAKE_CASE_OFFLINE,
                EVIDENCE_UPLOAD_JUDGE,
                HEARING_SCHEDULED,
                HEARING_SCHEDULED_RETRIGGER,
                CONFIRM_LISTING_COMPLETED,
                GENERATE_DIRECTIONS_ORDER,
                TRANSFER_ONLINE_CASE,
                COURT_OFFICER_ORDER,
                SETTLE_CLAIM,
                SETTLE_CLAIM_MARK_PAID_FULL,
                DISCONTINUE_CLAIM_CLAIMANT,
                VALIDATE_DISCONTINUE_CLAIM_CLAIMANT,
                STAY_CASE,
                DISMISS_CASE,
                MANAGE_STAY,
                CONFIRM_ORDER_REVIEW,
                ORDER_REVIEW_OBLIGATION_CHECK
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
                NotSuitable_SDO,
                REQUEST_FOR_RECONSIDERATION,
                DEFAULT_JUDGEMENT,
                STANDARD_DIRECTION_ORDER_DJ,
                migrateCase,
                TAKE_CASE_OFFLINE,
                EVIDENCE_UPLOAD_JUDGE,
                HEARING_SCHEDULED,
                HEARING_SCHEDULED_RETRIGGER,
                CONFIRM_LISTING_COMPLETED,
                GENERATE_DIRECTIONS_ORDER,
                TRANSFER_ONLINE_CASE,
                COURT_OFFICER_ORDER,
                SETTLE_CLAIM,
                SETTLE_CLAIM_MARK_PAID_FULL,
                DISCONTINUE_CLAIM_CLAIMANT,
                VALIDATE_DISCONTINUE_CLAIM_CLAIMANT,
                STAY_CASE,
                DISMISS_CASE,
                MANAGE_STAY,
                CONFIRM_ORDER_REVIEW,
                ORDER_REVIEW_OBLIGATION_CHECK
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
                NotSuitable_SDO,
                REQUEST_FOR_RECONSIDERATION,
                migrateCase,
                TAKE_CASE_OFFLINE,
                TRANSFER_ONLINE_CASE,
                GENERATE_DIRECTIONS_ORDER,
                EVIDENCE_UPLOAD_APPLICANT,
                EVIDENCE_UPLOAD_RESPONDENT,
                EVIDENCE_UPLOAD_JUDGE,
                TRIAL_READINESS,
                HEARING_SCHEDULED,
                HEARING_SCHEDULED_RETRIGGER,
                CONFIRM_LISTING_COMPLETED,
                TRIAL_READY_CHECK,
                TRIAL_READY_NOTIFICATION,
                MOVE_TO_DECISION_OUTCOME,
                HEARING_FEE_UNPAID,
                HEARING_FEE_PAID,
                BUNDLE_CREATION_NOTIFICATION,
                COURT_OFFICER_ORDER,
                SETTLE_CLAIM,
                SETTLE_CLAIM_MARK_PAID_FULL,
                DISCONTINUE_CLAIM_CLAIMANT,
                VALIDATE_DISCONTINUE_CLAIM_CLAIMANT,
                STAY_CASE,
                DISMISS_CASE,
                MANAGE_STAY,
                CONFIRM_ORDER_REVIEW,
                ORDER_REVIEW_OBLIGATION_CHECK
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
                NotSuitable_SDO,
                REQUEST_FOR_RECONSIDERATION,
                migrateCase,
                TRANSFER_ONLINE_CASE,
                SETTLE_CLAIM,
                SETTLE_CLAIM_MARK_PAID_FULL,
                DISCONTINUE_CLAIM_CLAIMANT,
                VALIDATE_DISCONTINUE_CLAIM_CLAIMANT,
                STAY_CASE,
                DISMISS_CASE,
                MANAGE_STAY,
                CONFIRM_ORDER_REVIEW,
                ORDER_REVIEW_OBLIGATION_CHECK
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
                NotSuitable_SDO,
                REQUEST_FOR_RECONSIDERATION,
                migrateCase,
                TRANSFER_ONLINE_CASE,
                SETTLE_CLAIM,
                SETTLE_CLAIM_MARK_PAID_FULL,
                DISCONTINUE_CLAIM_CLAIMANT,
                VALIDATE_DISCONTINUE_CLAIM_CLAIMANT,
                STAY_CASE,
                DISMISS_CASE,
                MANAGE_STAY,
                CONFIRM_ORDER_REVIEW,
                ORDER_REVIEW_OBLIGATION_CHECK
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
                NotSuitable_SDO,
                REQUEST_FOR_RECONSIDERATION,
                migrateCase,
                CLAIMANT_RESPONSE_CUI,
                TRANSFER_ONLINE_CASE,
                SETTLE_CLAIM,
                SETTLE_CLAIM_MARK_PAID_FULL,
                DISCONTINUE_CLAIM_CLAIMANT,
                VALIDATE_DISCONTINUE_CLAIM_CLAIMANT,
                STAY_CASE,
                DISMISS_CASE,
                MANAGE_STAY,
                CONFIRM_ORDER_REVIEW,
                ORDER_REVIEW_OBLIGATION_CHECK
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
                REQUEST_FOR_RECONSIDERATION,
                APPLICATION_OFFLINE_UPDATE_CLAIM,
                migrateCase,
                CLAIMANT_RESPONSE_CUI,
                DEFENDANT_SIGN_SETTLEMENT_AGREEMENT,
                TRANSFER_ONLINE_CASE,
                SETTLE_CLAIM,
                SETTLE_CLAIM_MARK_PAID_FULL,
                DISCONTINUE_CLAIM_CLAIMANT,
                VALIDATE_DISCONTINUE_CLAIM_CLAIMANT,
                STAY_CASE,
                DISMISS_CASE,
                MANAGE_STAY,
                CONFIRM_ORDER_REVIEW,
                ORDER_REVIEW_OBLIGATION_CHECK
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
                NotSuitable_SDO,
                REQUEST_FOR_RECONSIDERATION,
                CREATE_SDO,
                APPLICATION_OFFLINE_UPDATE_CLAIM,
                migrateCase,
                DEFENDANT_SIGN_SETTLEMENT_AGREEMENT,
                TRANSFER_ONLINE_CASE,
                SETTLE_CLAIM,
                SETTLE_CLAIM_MARK_PAID_FULL,
                DISCONTINUE_CLAIM_CLAIMANT,
                VALIDATE_DISCONTINUE_CLAIM_CLAIMANT,
                STAY_CASE,
                DISMISS_CASE,
                MANAGE_STAY,
                CONFIRM_ORDER_REVIEW,
                ORDER_REVIEW_OBLIGATION_CHECK
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
                REQUEST_FOR_RECONSIDERATION,
                APPLICATION_OFFLINE_UPDATE_CLAIM,
                migrateCase,
                TRANSFER_ONLINE_CASE,
                SETTLE_CLAIM,
                SETTLE_CLAIM_MARK_PAID_FULL,
                DISCONTINUE_CLAIM_CLAIMANT,
                VALIDATE_DISCONTINUE_CLAIM_CLAIMANT,
                CONFIRM_ORDER_REVIEW,
                ORDER_REVIEW_OBLIGATION_CHECK
            )
        ),

        entry(
            DIVERGENT_RESPOND_GO_OFFLINE.fullName(),
            List.of(
                APPLICATION_OFFLINE_UPDATE_CLAIM,
                migrateCase
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
                NotSuitable_SDO,
                REQUEST_FOR_RECONSIDERATION,
                HEARING_SCHEDULED,
                HEARING_SCHEDULED_RETRIGGER,
                CONFIRM_LISTING_COMPLETED,
                HEARING_FEE_PAID,
                HEARING_FEE_UNPAID,
                TRIAL_READY_CHECK,
                TRIAL_READY_NOTIFICATION,
                MOVE_TO_DECISION_OUTCOME,
                SERVICE_REQUEST_RECEIVED,
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
                ADD_UNAVAILABLE_DATES,
                SET_ASIDE_JUDGMENT,
                JUDGMENT_PAID_IN_FULL,
                REFER_JUDGE_DEFENCE_RECEIVED,
                RECORD_JUDGMENT,
                TRANSFER_ONLINE_CASE,
                CLAIMANT_RESPONSE_CUI,
                asyncStitchingComplete,
                DECISION_ON_RECONSIDERATION_REQUEST,
                EDIT_JUDGMENT,
                COURT_OFFICER_ORDER,
                SETTLE_CLAIM,
                SETTLE_CLAIM_MARK_PAID_FULL,
                DISCONTINUE_CLAIM_CLAIMANT,
                VALIDATE_DISCONTINUE_CLAIM_CLAIMANT,
                STAY_CASE,
                DISMISS_CASE,
                MANAGE_STAY,
                CONFIRM_ORDER_REVIEW,
                ORDER_REVIEW_OBLIGATION_CHECK
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
                NotSuitable_SDO,
                REQUEST_FOR_RECONSIDERATION,
                REFER_TO_JUDGE,
                migrateCase,
                TRANSFER_ONLINE_CASE,
                SETTLE_CLAIM,
                SETTLE_CLAIM_MARK_PAID_FULL,
                DISCONTINUE_CLAIM_CLAIMANT,
                VALIDATE_DISCONTINUE_CLAIM_CLAIMANT,
                STAY_CASE,
                DISMISS_CASE,
                MANAGE_STAY,
                CONFIRM_ORDER_REVIEW,
                ORDER_REVIEW_OBLIGATION_CHECK
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
                REQUEST_FOR_RECONSIDERATION,
                APPLICATION_CLOSED_UPDATE_CLAIM,
                REFER_TO_JUDGE,
                migrateCase,
                TRANSFER_ONLINE_CASE,
                SETTLE_CLAIM,
                SETTLE_CLAIM_MARK_PAID_FULL,
                DISCONTINUE_CLAIM_CLAIMANT,
                VALIDATE_DISCONTINUE_CLAIM_CLAIMANT
            )
        ),
        entry(
            CLAIM_DISMISSED_PAST_CLAIM_DETAILS_NOTIFICATION_DEADLINE.fullName(),
            List.of(
                CASE_PROCEEDS_IN_CASEMAN,
                ADD_CASE_NOTE,
                INITIATE_GENERAL_APPLICATION,
                CREATE_SDO,
                NotSuitable_SDO,
                REQUEST_FOR_RECONSIDERATION,
                REFER_TO_JUDGE,
                APPLICATION_CLOSED_UPDATE_CLAIM,
                migrateCase,
                TRANSFER_ONLINE_CASE,
                SETTLE_CLAIM,
                SETTLE_CLAIM_MARK_PAID_FULL,
                DISCONTINUE_CLAIM_CLAIMANT,
                VALIDATE_DISCONTINUE_CLAIM_CLAIMANT
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
                migrateCase
            )
        ),
        entry(
            PAST_APPLICANT_RESPONSE_DEADLINE_AWAITING_CAMUNDA.fullName(),
            List.of(
                TAKE_CASE_OFFLINE,
                APPLICATION_OFFLINE_UPDATE_CLAIM,
                migrateCase,
                NotSuitable_SDO,
                REQUEST_FOR_RECONSIDERATION,
                CREATE_SDO,
                TRANSFER_ONLINE_CASE,
                SETTLE_CLAIM,
                SETTLE_CLAIM_MARK_PAID_FULL,
                DISCONTINUE_CLAIM_CLAIMANT,
                VALIDATE_DISCONTINUE_CLAIM_CLAIMANT
            )
        ),
        entry(
            PAST_CLAIM_NOTIFICATION_DEADLINE_AWAITING_CAMUNDA.fullName(),
            List.of(
                DISMISS_CLAIM,
                ADD_CASE_NOTE,
                INITIATE_GENERAL_APPLICATION,
                CREATE_SDO,
                NotSuitable_SDO,
                REQUEST_FOR_RECONSIDERATION,
                REFER_TO_JUDGE,
                migrateCase,
                TRANSFER_ONLINE_CASE,
                SETTLE_CLAIM,
                SETTLE_CLAIM_MARK_PAID_FULL,
                DISCONTINUE_CLAIM_CLAIMANT,
                VALIDATE_DISCONTINUE_CLAIM_CLAIMANT
            )
        ),
        entry(
            PAST_CLAIM_DETAILS_NOTIFICATION_DEADLINE_AWAITING_CAMUNDA.fullName(),
            List.of(
                DISMISS_CLAIM,
                ADD_CASE_NOTE,
                INITIATE_GENERAL_APPLICATION,
                CREATE_SDO,
                NotSuitable_SDO,
                REQUEST_FOR_RECONSIDERATION,
                REFER_TO_JUDGE,
                migrateCase,
                TRANSFER_ONLINE_CASE,
                SETTLE_CLAIM,
                SETTLE_CLAIM_MARK_PAID_FULL,
                DISCONTINUE_CLAIM_CLAIMANT,
                VALIDATE_DISCONTINUE_CLAIM_CLAIMANT
            )
        ),
        entry(
            PAST_CLAIM_DISMISSED_DEADLINE_AWAITING_CAMUNDA.fullName(),
            List.of(
                DISMISS_CLAIM,
                migrateCase,
                CREATE_SDO,
                NotSuitable_SDO,
                REQUEST_FOR_RECONSIDERATION,
                TRANSFER_ONLINE_CASE,
                ADD_CASE_NOTE,
                SETTLE_CLAIM,
                SETTLE_CLAIM_MARK_PAID_FULL,
                DISCONTINUE_CLAIM_CLAIMANT,
                VALIDATE_DISCONTINUE_CLAIM_CLAIMANT,
                AMEND_RESTITCH_BUNDLE,
                asyncStitchingComplete
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
            CLAIM_DISMISSED_PAST_CLAIM_DISMISSED_DEADLINE.fullName(),
            List.of(
                APPLICATION_CLOSED_UPDATE_CLAIM,
                migrateCase
            )
        ),
        entry(
            IN_MEDIATION.fullName(),
            List.of(
                MEDIATION_SUCCESSFUL,
                MEDIATION_UNSUCCESSFUL,
                ADD_UNAVAILABLE_DATES,
                INITIATE_GENERAL_APPLICATION,
                TRANSFER_ONLINE_CASE,
                SETTLE_CLAIM,
                SETTLE_CLAIM_MARK_PAID_FULL,
                DISCONTINUE_CLAIM_CLAIMANT,
                VALIDATE_DISCONTINUE_CLAIM_CLAIMANT,
                STAY_CASE,
                DISMISS_CASE,
                MANAGE_STAY,
                CONFIRM_ORDER_REVIEW,
                ORDER_REVIEW_OBLIGATION_CHECK
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
                NotSuitable_SDO,
                TAKE_CASE_OFFLINE,
                EVIDENCE_UPLOAD_JUDGE,
                HEARING_FEE_UNPAID,
                HEARING_FEE_PAID,
                HEARING_SCHEDULED,
                HEARING_SCHEDULED_RETRIGGER,
                CONFIRM_LISTING_COMPLETED,
                TRIAL_READY_CHECK,
                TRIAL_READY_NOTIFICATION,
                MOVE_TO_DECISION_OUTCOME,
                SERVICE_REQUEST_RECEIVED,
                EVIDENCE_UPLOAD_APPLICANT,
                migrateCase,
                EVIDENCE_UPLOAD_RESPONDENT,
                GENERATE_DIRECTIONS_ORDER,
                TRIAL_READINESS,
                BUNDLE_CREATION_NOTIFICATION,
                ADD_UNAVAILABLE_DATES,
                asyncStitchingComplete,
                TRANSFER_ONLINE_CASE,
                INVALID_HWF_REFERENCE,
                COURT_OFFICER_ORDER,
                SETTLE_CLAIM,
                SETTLE_CLAIM_MARK_PAID_FULL,
                DISCONTINUE_CLAIM_CLAIMANT,
                VALIDATE_DISCONTINUE_CLAIM_CLAIMANT,
                AMEND_RESTITCH_BUNDLE,
                STAY_CASE,
                DISMISS_CASE,
                MANAGE_STAY,
                CONFIRM_ORDER_REVIEW,
                ORDER_REVIEW_OBLIGATION_CHECK
            )
        ),
        entry(
            CASE_STAYED.fullName(),
            List.of(
                INITIATE_GENERAL_APPLICATION,
                ADD_UNAVAILABLE_DATES,
                CHANGE_SOLICITOR_EMAIL,
                ORDER_REVIEW_OBLIGATION_CHECK
            )
        )
    );

    private static final Map<String, List<CaseEvent>> ALLOWED_EVENTS_ON_FLOW_STATE_SPEC = Map.ofEntries(
        entry(
            DRAFT.fullName(),
            List.of(
                CREATE_CLAIM,
                CREATE_LIP_CLAIM,
                migrateCase
            )
        ),

        entry(
            SPEC_DRAFT.fullName(),
            List.of(
                CREATE_CLAIM_SPEC,
                CREATE_LIP_CLAIM,
                migrateCase
            )
        ),

        entry(
            CLAIM_ISSUED_PAYMENT_SUCCESSFUL.fullName(),
            List.of(
                NOC_REQUEST,
                APPLY_NOC_DECISION,
                ADD_CASE_NOTE,
                INITIATE_GENERAL_APPLICATION,
                CREATE_SDO,
                NotSuitable_SDO,
                REQUEST_FOR_RECONSIDERATION,
                migrateCase,
                CREATE_CLAIM_SPEC_AFTER_PAYMENT,
                CREATE_CLAIM_AFTER_PAYMENT,
                TRANSFER_ONLINE_CASE,
                SETTLE_CLAIM,
                SETTLE_CLAIM_MARK_PAID_FULL,
                DISCONTINUE_CLAIM_CLAIMANT,
                VALIDATE_DISCONTINUE_CLAIM_CLAIMANT,
                RECORD_JUDGMENT,
                EDIT_JUDGMENT,
                JUDGMENT_PAID_IN_FULL,
                SET_ASIDE_JUDGMENT
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
                NotSuitable_SDO,
                REQUEST_FOR_RECONSIDERATION,
                migrateCase,
                TRANSFER_ONLINE_CASE,
                SETTLE_CLAIM,
                SETTLE_CLAIM_MARK_PAID_FULL,
                DISCONTINUE_CLAIM_CLAIMANT,
                VALIDATE_DISCONTINUE_CLAIM_CLAIMANT,
                RECORD_JUDGMENT,
                EDIT_JUDGMENT,
                JUDGMENT_PAID_IN_FULL,
                SET_ASIDE_JUDGMENT
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
                HEARING_SCHEDULED_RETRIGGER,
                CONFIRM_LISTING_COMPLETED,
                NotSuitable_SDO,
                REQUEST_FOR_RECONSIDERATION,
                EVIDENCE_UPLOAD_JUDGE,
                TRIAL_READY_CHECK,
                TRIAL_READY_NOTIFICATION,
                MOVE_TO_DECISION_OUTCOME,
                CREATE_CLAIM_SPEC_AFTER_PAYMENT,
                CREATE_CLAIM_AFTER_PAYMENT,
                EVIDENCE_UPLOAD_APPLICANT,
                migrateCase,
                EVIDENCE_UPLOAD_RESPONDENT,
                BUNDLE_CREATION_NOTIFICATION,
                CHANGE_SOLICITOR_EMAIL,
                LIP_CLAIM_SETTLED,
                asyncStitchingComplete,
                TRANSFER_ONLINE_CASE,
                INVALID_HWF_REFERENCE,
                RECORD_JUDGMENT,
                EDIT_JUDGMENT,
                JUDGMENT_PAID_IN_FULL,
                SET_ASIDE_JUDGMENT,
                SETTLE_CLAIM,
                SETTLE_CLAIM_MARK_PAID_FULL,
                DISCONTINUE_CLAIM_CLAIMANT,
                VALIDATE_DISCONTINUE_CLAIM_CLAIMANT,
                STAY_CASE,
                DISMISS_CASE,
                MANAGE_STAY,
                CONFIRM_ORDER_REVIEW,
                ORDER_REVIEW_OBLIGATION_CHECK
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
                NotSuitable_SDO,
                REQUEST_FOR_RECONSIDERATION,
                HEARING_SCHEDULED,
                HEARING_SCHEDULED_RETRIGGER,
                CONFIRM_LISTING_COMPLETED,
                TRIAL_READY_CHECK,
                TRIAL_READY_NOTIFICATION,
                MOVE_TO_DECISION_OUTCOME,
                EVIDENCE_UPLOAD_JUDGE,
                EVIDENCE_UPLOAD_APPLICANT,
                migrateCase,
                EVIDENCE_UPLOAD_RESPONDENT,
                BUNDLE_CREATION_NOTIFICATION,
                CHANGE_SOLICITOR_EMAIL,
                LIP_CLAIM_SETTLED,
                asyncStitchingComplete,
                TRANSFER_ONLINE_CASE,
                SETTLE_CLAIM,
                SETTLE_CLAIM_MARK_PAID_FULL,
                DISCONTINUE_CLAIM_CLAIMANT,
                VALIDATE_DISCONTINUE_CLAIM_CLAIMANT,
                STAY_CASE,
                DISMISS_CASE,
                MANAGE_STAY,
                CONFIRM_ORDER_REVIEW,
                ORDER_REVIEW_OBLIGATION_CHECK
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
                NotSuitable_SDO,
                REQUEST_FOR_RECONSIDERATION,
                migrateCase,
                CHANGE_SOLICITOR_EMAIL,
                TRANSFER_ONLINE_CASE,
                SETTLE_CLAIM,
                SETTLE_CLAIM_MARK_PAID_FULL,
                DISCONTINUE_CLAIM_CLAIMANT,
                VALIDATE_DISCONTINUE_CLAIM_CLAIMANT,
                STAY_CASE,
                DISMISS_CASE,
                MANAGE_STAY,
                CONFIRM_ORDER_REVIEW,
                ORDER_REVIEW_OBLIGATION_CHECK
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
                NotSuitable_SDO,
                REQUEST_FOR_RECONSIDERATION,
                migrateCase,
                CHANGE_SOLICITOR_EMAIL,
                LIP_CLAIM_SETTLED,
                TRANSFER_ONLINE_CASE,
                GENERATE_DIRECTIONS_ORDER,
                EVIDENCE_UPLOAD_APPLICANT,
                EVIDENCE_UPLOAD_RESPONDENT,
                EVIDENCE_UPLOAD_JUDGE,
                TRIAL_READINESS,
                HEARING_SCHEDULED,
                HEARING_SCHEDULED_RETRIGGER,
                CONFIRM_LISTING_COMPLETED,
                TRIAL_READY_CHECK,
                TRIAL_READY_NOTIFICATION,
                MOVE_TO_DECISION_OUTCOME,
                HEARING_FEE_UNPAID,
                HEARING_FEE_PAID,
                BUNDLE_CREATION_NOTIFICATION,
                COURT_OFFICER_ORDER,
                SETTLE_CLAIM,
                SETTLE_CLAIM_MARK_PAID_FULL,
                DISCONTINUE_CLAIM_CLAIMANT,
                VALIDATE_DISCONTINUE_CLAIM_CLAIMANT,
                STAY_CASE,
                DISMISS_CASE,
                MANAGE_STAY,
                CONFIRM_ORDER_REVIEW,
                ORDER_REVIEW_OBLIGATION_CHECK
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
                NotSuitable_SDO,
                REQUEST_FOR_RECONSIDERATION,
                migrateCase,
                DEFAULT_JUDGEMENT_SPEC,
                CHANGE_SOLICITOR_EMAIL,
                LIP_CLAIM_SETTLED,
                CLAIMANT_RESPONSE_CUI,
                TRANSFER_ONLINE_CASE,
                SETTLE_CLAIM,
                SETTLE_CLAIM_MARK_PAID_FULL,
                DISCONTINUE_CLAIM_CLAIMANT,
                VALIDATE_DISCONTINUE_CLAIM_CLAIMANT,
                STAY_CASE,
                DISMISS_CASE,
                MANAGE_STAY,
                ADD_CASE_NOTE,
                CONFIRM_ORDER_REVIEW,
                ORDER_REVIEW_OBLIGATION_CHECK
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
                REQUEST_FOR_RECONSIDERATION,
                APPLICATION_OFFLINE_UPDATE_CLAIM,
                migrateCase,
                DEFAULT_JUDGEMENT_SPEC,
                REQUEST_JUDGEMENT_ADMISSION_SPEC,
                CHANGE_SOLICITOR_EMAIL,
                LIP_CLAIM_SETTLED,
                CLAIMANT_RESPONSE_CUI,
                DEFENDANT_SIGN_SETTLEMENT_AGREEMENT,
                TRANSFER_ONLINE_CASE,
                SETTLE_CLAIM,
                SETTLE_CLAIM_MARK_PAID_FULL,
                DISCONTINUE_CLAIM_CLAIMANT,
                VALIDATE_DISCONTINUE_CLAIM_CLAIMANT,
                STAY_CASE,
                DISMISS_CASE,
                MANAGE_STAY,
                ADD_CASE_NOTE,
                CONFIRM_ORDER_REVIEW,
                NOC_REQUEST,
                APPLY_NOC_DECISION,
                ORDER_REVIEW_OBLIGATION_CHECK
            )
        ),
        entry(
            PART_ADMISSION.fullName(),
            List.of(
                DEFENDANT_RESPONSE_CUI,
                CLAIMANT_RESPONSE_SPEC,
                CLAIMANT_RESPONSE_CUI,
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
                REQUEST_FOR_RECONSIDERATION,
                APPLICATION_OFFLINE_UPDATE_CLAIM,
                migrateCase,
                DEFAULT_JUDGEMENT_SPEC,
                CHANGE_SOLICITOR_EMAIL,
                REQUEST_JUDGEMENT_ADMISSION_SPEC,
                LIP_CLAIM_SETTLED,
                DEFENDANT_SIGN_SETTLEMENT_AGREEMENT,
                TRANSFER_ONLINE_CASE,
                SETTLE_CLAIM,
                SETTLE_CLAIM_MARK_PAID_FULL,
                DISCONTINUE_CLAIM_CLAIMANT,
                VALIDATE_DISCONTINUE_CLAIM_CLAIMANT,
                STAY_CASE,
                DISMISS_CASE,
                MANAGE_STAY,
                MORE_INFORMATION_HWF,
                FEE_PAYMENT_OUTCOME,
                NO_REMISSION_HWF,
                PARTIAL_REMISSION_HWF_GRANTED,
                FULL_REMISSION_HWF,
                UPDATE_HELP_WITH_FEE_NUMBER,
                INVALID_HWF_REFERENCE,
                ADD_CASE_NOTE,
                CONFIRM_ORDER_REVIEW,
                NOC_REQUEST,
                APPLY_NOC_DECISION,
                ORDER_REVIEW_OBLIGATION_CHECK
            )
        ),

        entry(
            DIVERGENT_RESPOND_GO_OFFLINE.fullName(),
            List.of(
                APPLICATION_OFFLINE_UPDATE_CLAIM,
                migrateCase
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
                NotSuitable_SDO,
                REQUEST_FOR_RECONSIDERATION,
                migrateCase,
                CHANGE_SOLICITOR_EMAIL,
                LIP_CLAIM_SETTLED,
                TRANSFER_ONLINE_CASE,
                SETTLE_CLAIM,
                SETTLE_CLAIM_MARK_PAID_FULL,
                DISCONTINUE_CLAIM_CLAIMANT,
                VALIDATE_DISCONTINUE_CLAIM_CLAIMANT,
                CONFIRM_ORDER_REVIEW,
                NOC_REQUEST,
                APPLY_NOC_DECISION,
                ORDER_REVIEW_OBLIGATION_CHECK
            )
        ),
        entry(
            PART_ADMIT_REJECT_REPAYMENT.fullName(),
            List.of(
                DEFENDANT_SIGN_SETTLEMENT_AGREEMENT,
                REQUEST_JUDGEMENT_ADMISSION_SPEC,
                ADD_CASE_NOTE,
                NOC_REQUEST,
                APPLY_NOC_DECISION
            )
        ),
        entry(
            PART_ADMIT_PROCEED.fullName(),
            List.of(DEFENDANT_SIGN_SETTLEMENT_AGREEMENT,
                    ADD_CASE_NOTE,
                    NOC_REQUEST,
                    APPLY_NOC_DECISION)
        ),
        entry(
            PART_ADMIT_NOT_PROCEED.fullName(),
            List.of(DEFENDANT_SIGN_SETTLEMENT_AGREEMENT,
                    ADD_CASE_NOTE,
                    NOC_REQUEST,
                    APPLY_NOC_DECISION)
        ),
        entry(
            PART_ADMIT_PAY_IMMEDIATELY.fullName(),
            List.of(
                DEFENDANT_SIGN_SETTLEMENT_AGREEMENT,
                CLAIMANT_RESPONSE_CUI,
                LIP_CLAIM_SETTLED,
                STAY_CASE,
                DISMISS_CASE,
                MANAGE_STAY,
                ADD_CASE_NOTE,
                CONFIRM_ORDER_REVIEW,
                NOC_REQUEST,
                APPLY_NOC_DECISION,
                ORDER_REVIEW_OBLIGATION_CHECK,
                REQUEST_JUDGEMENT_ADMISSION_SPEC
            )
        ),
        entry(
            PART_ADMIT_AGREE_SETTLE.fullName(),
            List.of(DEFENDANT_SIGN_SETTLEMENT_AGREEMENT,
                    ADD_CASE_NOTE,
                    NOC_REQUEST,
                    APPLY_NOC_DECISION)
        ),
        entry(
            FULL_ADMIT_PAY_IMMEDIATELY.fullName(),
            List.of(DEFENDANT_SIGN_SETTLEMENT_AGREEMENT,
                    ADD_CASE_NOTE,
                    NOC_REQUEST,
                    APPLY_NOC_DECISION,
                    REQUEST_JUDGEMENT_ADMISSION_SPEC)
        ),
        entry(
            FULL_ADMIT_PROCEED.fullName(),
            List.of(DEFENDANT_SIGN_SETTLEMENT_AGREEMENT,
                    ADD_CASE_NOTE,
                    NOC_REQUEST,
                    APPLY_NOC_DECISION)
        ),
        entry(
            FULL_ADMIT_NOT_PROCEED.fullName(),
            List.of(DEFENDANT_SIGN_SETTLEMENT_AGREEMENT,
                    ADD_CASE_NOTE,
                    NOC_REQUEST,
                    APPLY_NOC_DECISION)
        ),
        entry(
            FULL_ADMIT_JUDGMENT_ADMISSION.fullName(),
            List.of(
                DEFENDANT_SIGN_SETTLEMENT_AGREEMENT,
                EDIT_JUDGMENT,
                JUDGMENT_PAID_IN_FULL,
                SET_ASIDE_JUDGMENT,
                ADD_CASE_NOTE,
                NOC_REQUEST,
                APPLY_NOC_DECISION
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
                NotSuitable_SDO,
                REQUEST_FOR_RECONSIDERATION,
                HEARING_SCHEDULED,
                HEARING_SCHEDULED_RETRIGGER,
                CONFIRM_LISTING_COMPLETED,
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
                SET_ASIDE_JUDGMENT,
                JUDGMENT_PAID_IN_FULL,
                REFER_JUDGE_DEFENCE_RECEIVED,
                RECORD_JUDGMENT,
                LIP_CLAIM_SETTLED,
                TRANSFER_ONLINE_CASE,
                asyncStitchingComplete,
                CLAIMANT_RESPONSE_CUI,
                UPLOAD_MEDIATION_DOCUMENTS,
                CUI_UPLOAD_MEDIATION_DOCUMENTS,
                REQUEST_FOR_RECONSIDERATION,
                DECISION_ON_RECONSIDERATION_REQUEST,
                EDIT_JUDGMENT,
                COURT_OFFICER_ORDER,
                SETTLE_CLAIM,
                SETTLE_CLAIM_MARK_PAID_FULL,
                DISCONTINUE_CLAIM_CLAIMANT,
                VALIDATE_DISCONTINUE_CLAIM_CLAIMANT,
                STAY_CASE,
                DISMISS_CASE,
                MANAGE_STAY,
                MORE_INFORMATION_HWF,
                FEE_PAYMENT_OUTCOME,
                NO_REMISSION_HWF,
                PARTIAL_REMISSION_HWF_GRANTED,
                FULL_REMISSION_HWF,
                UPDATE_HELP_WITH_FEE_NUMBER,
                INVALID_HWF_REFERENCE,
                ADD_CASE_NOTE,
                CONFIRM_ORDER_REVIEW,
                NOC_REQUEST,
                APPLY_NOC_DECISION,
                MEDIATION_SUCCESSFUL,
                MEDIATION_UNSUCCESSFUL,
                ORDER_REVIEW_OBLIGATION_CHECK
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
                NotSuitable_SDO,
                REQUEST_FOR_RECONSIDERATION,
                REFER_TO_JUDGE,
                migrateCase,
                CHANGE_SOLICITOR_EMAIL,
                LIP_CLAIM_SETTLED,
                TRANSFER_ONLINE_CASE,
                SETTLE_CLAIM,
                SETTLE_CLAIM_MARK_PAID_FULL,
                DISCONTINUE_CLAIM_CLAIMANT,
                VALIDATE_DISCONTINUE_CLAIM_CLAIMANT,
                ADD_CASE_NOTE,
                CONFIRM_ORDER_REVIEW,
                NOC_REQUEST,
                APPLY_NOC_DECISION,
                ORDER_REVIEW_OBLIGATION_CHECK
            )
        ),
        entry(
            CLAIM_DISMISSED_PAST_CLAIM_NOTIFICATION_DEADLINE.fullName(),
            List.of(
                CASE_PROCEEDS_IN_CASEMAN,
                migrateCase
            )
        ),
        entry(
            CLAIM_DISMISSED_PAST_CLAIM_DETAILS_NOTIFICATION_DEADLINE.fullName(),
            List.of(
                CASE_PROCEEDS_IN_CASEMAN,
                migrateCase
            )
        ),
        entry(
            PAST_APPLICANT_RESPONSE_DEADLINE_AWAITING_CAMUNDA.fullName(),
            List.of(
                TAKE_CASE_OFFLINE,
                APPLICATION_OFFLINE_UPDATE_CLAIM,
                migrateCase,
                TRANSFER_ONLINE_CASE,
                SETTLE_CLAIM,
                SETTLE_CLAIM_MARK_PAID_FULL,
                DISCONTINUE_CLAIM_CLAIMANT,
                VALIDATE_DISCONTINUE_CLAIM_CLAIMANT
            )
        ),
        entry(
            PAST_CLAIM_NOTIFICATION_DEADLINE_AWAITING_CAMUNDA.fullName(),
            List.of(
                DISMISS_CLAIM,
                migrateCase,
                TRANSFER_ONLINE_CASE,
                SETTLE_CLAIM,
                SETTLE_CLAIM_MARK_PAID_FULL,
                DISCONTINUE_CLAIM_CLAIMANT,
                VALIDATE_DISCONTINUE_CLAIM_CLAIMANT
            )
        ),
        entry(
            PAST_CLAIM_DETAILS_NOTIFICATION_DEADLINE_AWAITING_CAMUNDA.fullName(),
            List.of(
                DISMISS_CLAIM,
                migrateCase,
                TRANSFER_ONLINE_CASE,
                SETTLE_CLAIM,
                SETTLE_CLAIM_MARK_PAID_FULL,
                DISCONTINUE_CLAIM_CLAIMANT,
                VALIDATE_DISCONTINUE_CLAIM_CLAIMANT
            )
        ),
        entry(
            PAST_CLAIM_DISMISSED_DEADLINE_AWAITING_CAMUNDA.fullName(),
            List.of(
                DISMISS_CLAIM,
                migrateCase,
                ADD_CASE_NOTE,
                AMEND_RESTITCH_BUNDLE,
                asyncStitchingComplete
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
                NotSuitable_SDO,
                REQUEST_FOR_RECONSIDERATION,
                migrateCase,
                TRANSFER_ONLINE_CASE,
                SETTLE_CLAIM,
                SETTLE_CLAIM_MARK_PAID_FULL,
                DISCONTINUE_CLAIM_CLAIMANT,
                VALIDATE_DISCONTINUE_CLAIM_CLAIMANT,
                STAY_CASE,
                DISMISS_CASE,
                MANAGE_STAY,
                CONFIRM_ORDER_REVIEW,
                ORDER_REVIEW_OBLIGATION_CHECK
            )
        ),
        entry(
            AWAITING_RESPONSES_NOT_FULL_DEFENCE_RECEIVED.fullName(),
            List.of(
                DEFENDANT_RESPONSE_SPEC,
                DEFENDANT_RESPONSE_CUI,
                RESET_PIN,
                migrateCase,
                TRANSFER_ONLINE_CASE,
                SETTLE_CLAIM,
                SETTLE_CLAIM_MARK_PAID_FULL,
                DISCONTINUE_CLAIM_CLAIMANT,
                VALIDATE_DISCONTINUE_CLAIM_CLAIMANT,
                STAY_CASE,
                DISMISS_CASE,
                MANAGE_STAY,
                ADD_CASE_NOTE,
                CONFIRM_ORDER_REVIEW,
                ORDER_REVIEW_OBLIGATION_CHECK
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
                TRANSFER_ONLINE_CASE,
                SETTLE_CLAIM,
                SETTLE_CLAIM_MARK_PAID_FULL,
                DISCONTINUE_CLAIM_CLAIMANT,
                VALIDATE_DISCONTINUE_CLAIM_CLAIMANT,
                STAY_CASE,
                DISMISS_CASE,
                MANAGE_STAY,
                CONFIRM_ORDER_REVIEW,
                NOC_REQUEST,
                APPLY_NOC_DECISION,
                ORDER_REVIEW_OBLIGATION_CHECK
            )
        ),
        entry(
            IN_HEARING_READINESS.fullName(),
            List.of(
                ADD_DEFENDANT_LITIGATION_FRIEND,
                ENTER_BREATHING_SPACE_SPEC,
                LIFT_BREATHING_SPACE_SPEC,
                WITHDRAW_CLAIM,
                DISCONTINUE_CLAIM,
                CASE_PROCEEDS_IN_CASEMAN,
                AMEND_PARTY_DETAILS,
                ADD_CASE_NOTE,
                INITIATE_GENERAL_APPLICATION,
                CREATE_SDO,
                NotSuitable_SDO,
                REQUEST_FOR_RECONSIDERATION,
                HEARING_SCHEDULED,
                HEARING_SCHEDULED_RETRIGGER,
                CONFIRM_LISTING_COMPLETED,
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
                asyncStitchingComplete,
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
                COURT_OFFICER_ORDER,
                SETTLE_CLAIM,
                SETTLE_CLAIM_MARK_PAID_FULL,
                DISCONTINUE_CLAIM_CLAIMANT,
                VALIDATE_DISCONTINUE_CLAIM_CLAIMANT,
                AMEND_RESTITCH_BUNDLE,
                STAY_CASE,
                DISMISS_CASE,
                MANAGE_STAY,
                CONFIRM_ORDER_REVIEW,
                NOC_REQUEST,
                APPLY_NOC_DECISION,
                ORDER_REVIEW_OBLIGATION_CHECK
            )
        ),
        entry(
            PART_ADMIT_NOT_SETTLED_NO_MEDIATION.fullName(),
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
                NotSuitable_SDO,
                REQUEST_FOR_RECONSIDERATION,
                HEARING_SCHEDULED,
                HEARING_SCHEDULED_RETRIGGER,
                CONFIRM_LISTING_COMPLETED,
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
                asyncStitchingComplete,
                CLAIMANT_RESPONSE_CUI,
                DEFENDANT_SIGN_SETTLEMENT_AGREEMENT,
                TRANSFER_ONLINE_CASE,
                COURT_OFFICER_ORDER,
                SETTLE_CLAIM,
                SETTLE_CLAIM_MARK_PAID_FULL,
                DISCONTINUE_CLAIM_CLAIMANT,
                VALIDATE_DISCONTINUE_CLAIM_CLAIMANT,
                STAY_CASE,
                DISMISS_CASE,
                MANAGE_STAY,
                MORE_INFORMATION_HWF,
                FEE_PAYMENT_OUTCOME,
                NO_REMISSION_HWF,
                PARTIAL_REMISSION_HWF_GRANTED,
                FULL_REMISSION_HWF,
                UPDATE_HELP_WITH_FEE_NUMBER,
                INVALID_HWF_REFERENCE,
                AMEND_RESTITCH_BUNDLE,
                CONFIRM_ORDER_REVIEW,
                NOC_REQUEST,
                APPLY_NOC_DECISION,
                ORDER_REVIEW_OBLIGATION_CHECK
            )
        ),
        entry(
            RESPONDENT_RESPONSE_LANGUAGE_IS_BILINGUAL.fullName(),
            List.of(
                CHANGE_SOLICITOR_EMAIL,
                EXTEND_RESPONSE_DEADLINE,
                LIP_CLAIM_SETTLED,
                TRANSFER_ONLINE_CASE,
                SETTLE_CLAIM,
                SETTLE_CLAIM_MARK_PAID_FULL,
                DISCONTINUE_CLAIM_CLAIMANT,
                VALIDATE_DISCONTINUE_CLAIM_CLAIMANT,
                STAY_CASE,
                DISMISS_CASE,
                MANAGE_STAY,
                ADD_CASE_NOTE,
                INITIATE_GENERAL_APPLICATION,
                CONFIRM_ORDER_REVIEW,
                ORDER_REVIEW_OBLIGATION_CHECK
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
                NotSuitable_SDO,
                REQUEST_FOR_RECONSIDERATION,
                DECISION_ON_RECONSIDERATION_REQUEST,
                AMEND_RESTITCH_BUNDLE,
                HEARING_SCHEDULED,
                HEARING_SCHEDULED_RETRIGGER,
                CONFIRM_LISTING_COMPLETED,
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
                asyncStitchingComplete,
                UPLOAD_MEDIATION_DOCUMENTS,
                CUI_UPLOAD_MEDIATION_DOCUMENTS,
                TRANSFER_ONLINE_CASE,
                COURT_OFFICER_ORDER,
                ADD_CASE_NOTE,
                SETTLE_CLAIM,
                SETTLE_CLAIM_MARK_PAID_FULL,
                DISCONTINUE_CLAIM_CLAIMANT,
                VALIDATE_DISCONTINUE_CLAIM_CLAIMANT,
                LIP_CLAIM_SETTLED,
                STAY_CASE,
                DISMISS_CASE,
                MANAGE_STAY,
                CONFIRM_ORDER_REVIEW,
                ORDER_REVIEW_OBLIGATION_CHECK
            )
        ),
        entry(
            PREPARE_FOR_HEARING_CONDUCT_HEARING.fullName(),
            List.of(
                asyncStitchingComplete,
                UPLOAD_MEDIATION_DOCUMENTS,
                CUI_UPLOAD_MEDIATION_DOCUMENTS,
                TRANSFER_ONLINE_CASE,
                SETTLE_CLAIM,
                SETTLE_CLAIM_MARK_PAID_FULL,
                DISCONTINUE_CLAIM_CLAIMANT,
                VALIDATE_DISCONTINUE_CLAIM_CLAIMANT,
                LIP_CLAIM_SETTLED,
                AMEND_RESTITCH_BUNDLE,
                STAY_CASE,
                DISMISS_CASE,
                MANAGE_STAY,
                CONFIRM_ORDER_REVIEW,
                NOC_REQUEST,
                APPLY_NOC_DECISION,
                ORDER_REVIEW_OBLIGATION_CHECK
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
                TRANSFER_ONLINE_CASE,
                SETTLE_CLAIM,
                SETTLE_CLAIM_MARK_PAID_FULL,
                LIP_CLAIM_SETTLED,
                DISCONTINUE_CLAIM_CLAIMANT,
                VALIDATE_DISCONTINUE_CLAIM_CLAIMANT,
                ADD_CASE_NOTE,
                NOC_REQUEST,
                APPLY_NOC_DECISION
            )
        ),
        entry(
            FULL_ADMIT_REJECT_REPAYMENT.fullName(),
            List.of(
                DEFENDANT_SIGN_SETTLEMENT_AGREEMENT,
                REQUEST_JUDGEMENT_ADMISSION_SPEC,
                LIP_CLAIM_SETTLED,
                ADD_CASE_NOTE,
                NOC_REQUEST,
                APPLY_NOC_DECISION
            )
        ),
        entry(
            FULL_ADMIT_AGREE_REPAYMENT.fullName(),
            List.of(
                DEFENDANT_SIGN_SETTLEMENT_AGREEMENT,
                REQUEST_JUDGEMENT_ADMISSION_SPEC,
                TRANSFER_ONLINE_CASE,
                EDIT_JUDGMENT,
                JUDGMENT_PAID_IN_FULL,
                SET_ASIDE_JUDGMENT,
                SETTLE_CLAIM,
                LIP_CLAIM_SETTLED,
                SETTLE_CLAIM_MARK_PAID_FULL,
                DISCONTINUE_CLAIM_CLAIMANT,
                VALIDATE_DISCONTINUE_CLAIM_CLAIMANT,
                STAY_CASE,
                DISMISS_CASE,
                MANAGE_STAY,
                ADD_CASE_NOTE,
                CONFIRM_ORDER_REVIEW,
                NOC_REQUEST,
                APPLY_NOC_DECISION,
                ORDER_REVIEW_OBLIGATION_CHECK
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
                NO_REMISSION_HWF,
                LIP_CLAIM_SETTLED,
                ADD_CASE_NOTE
            )
        ),
        entry(
            CASE_STAYED.fullName(),
            List.of(
                INITIATE_GENERAL_APPLICATION,
                ADD_UNAVAILABLE_DATES,
                CHANGE_SOLICITOR_EMAIL,
                ADD_CASE_NOTE,
                NOC_REQUEST,
                APPLY_NOC_DECISION,
            ORDER_REVIEW_OBLIGATION_CHECK
            )
        ),
        entry(
            SIGN_SETTLEMENT_AGREEMENT.fullName(),
            List.of(
                REQUEST_JUDGEMENT_ADMISSION_SPEC,
                LIP_CLAIM_SETTLED,
                ADD_CASE_NOTE
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
                .toList();
        }
        return ALLOWED_EVENTS_ON_FLOW_STATE.entrySet().stream()
            .filter(entry -> entry.getValue().contains(caseEvent))
            .map(Map.Entry::getKey)
            .toList();
    }
}
