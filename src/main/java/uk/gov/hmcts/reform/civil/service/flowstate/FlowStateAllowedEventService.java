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
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.DEFAULT_JUDGEMENT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.DEFAULT_JUDGEMENT_SPEC;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.DEFENDANT_RESPONSE;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.DEFENDANT_RESPONSE_CUI;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.DEFENDANT_RESPONSE_SPEC;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.DEFENDANT_SIGN_SETTLEMENT_AGREEMENT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.DISCONTINUE_CLAIM;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.DISMISS_CLAIM;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.ENTER_BREATHING_SPACE_LIP;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.ENTER_BREATHING_SPACE_SPEC;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.EVIDENCE_UPLOAD_APPLICANT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.EVIDENCE_UPLOAD_JUDGE;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.EVIDENCE_UPLOAD_RESPONDENT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.EXTEND_RESPONSE_DEADLINE;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.GENERATE_DIRECTIONS_ORDER;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.HEARING_FEE_PAID;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.HEARING_FEE_UNPAID;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.HEARING_SCHEDULED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.INFORM_AGREED_EXTENSION_DATE;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.INFORM_AGREED_EXTENSION_DATE_SPEC;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.INITIATE_GENERAL_APPLICATION;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.JUDGMENT_PAID_IN_FULL;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.LIFT_BREATHING_SPACE_LIP;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.LIFT_BREATHING_SPACE_SPEC;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.LIP_CLAIM_SETTLED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.MANAGE_CONTACT_INFORMATION;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.MEDIATION_SUCCESSFUL;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.MEDIATION_UNSUCCESSFUL;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.MOVE_TO_DECISION_OUTCOME;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOC_REQUEST;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_CLAIMANT_CUI_FOR_DEADLINE_EXTENSION;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_DEFENDANT_CUI_FOR_DEADLINE_EXTENSION;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_DEFENDANT_OF_CLAIM;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_DEFENDANT_OF_CLAIM_DETAILS;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_HEARING_PARTIES;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NotSuitable_SDO;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.RECORD_JUDGMENT;
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
import static uk.gov.hmcts.reform.civil.enums.CaseCategory.SPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.UPLOAD_TRANSLATED_DOCUMENT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.WITHDRAW_CLAIM;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.asyncStitchingComplete;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.migrateCase;
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
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.COUNTER_CLAIM;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.DIVERGENT_RESPOND_GO_OFFLINE;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.DRAFT;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.FULL_ADMISSION;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.FULL_ADMIT_AGREE_REPAYMENT;
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
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.PART_ADMIT_NOT_SETTLED_NO_MEDIATION;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.PAST_APPLICANT_RESPONSE_DEADLINE_AWAITING_CAMUNDA;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.PAST_CLAIM_DETAILS_NOTIFICATION_DEADLINE_AWAITING_CAMUNDA;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.PAST_CLAIM_DISMISSED_DEADLINE_AWAITING_CAMUNDA;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.PAST_CLAIM_NOTIFICATION_DEADLINE_AWAITING_CAMUNDA;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.PENDING_CLAIM_ISSUED_UNREPRESENTED_DEFENDANT;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.PREPARE_FOR_HEARING_CONDUCT_HEARING;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.RESPONDENT_RESPONSE_LANGUAGE_IS_BILINGUAL;
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
                migrateCase
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
                migrateCase
            )
        ),

        entry(
            CLAIM_ISSUED_PAYMENT_SUCCESSFUL.fullName(),
            List.of(NOC_REQUEST,
                    APPLY_NOC_DECISION,
                    ADD_CASE_NOTE,
                    INITIATE_GENERAL_APPLICATION,
                    CREATE_SDO,
                    NotSuitable_SDO,
                    migrateCase,
                    CREATE_CLAIM_SPEC_AFTER_PAYMENT,
                    CREATE_CLAIM_AFTER_PAYMENT
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
                EVIDENCE_UPLOAD_JUDGE,
                CREATE_CLAIM_SPEC_AFTER_PAYMENT,
                CREATE_CLAIM_AFTER_PAYMENT,
                EVIDENCE_UPLOAD_APPLICANT,
                migrateCase,
                EVIDENCE_UPLOAD_RESPONDENT
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
                EVIDENCE_UPLOAD_APPLICANT,
                migrateCase,
                EVIDENCE_UPLOAD_RESPONDENT
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
                HEARING_FEE_UNPAID,
                HEARING_FEE_PAID,
                HEARING_SCHEDULED,
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
                asyncStitchingComplete
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
                migrateCase,
                TAKE_CASE_OFFLINE,
                EVIDENCE_UPLOAD_JUDGE,
                HEARING_SCHEDULED,
                GENERATE_DIRECTIONS_ORDER
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
                STANDARD_DIRECTION_ORDER_DJ,
                CREATE_SDO,
                NotSuitable_SDO,
                migrateCase,
                TAKE_CASE_OFFLINE,
                EVIDENCE_UPLOAD_JUDGE,
                HEARING_SCHEDULED,
                GENERATE_DIRECTIONS_ORDER
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
                migrateCase,
                TAKE_CASE_OFFLINE,
                EVIDENCE_UPLOAD_JUDGE,
                HEARING_SCHEDULED,
                GENERATE_DIRECTIONS_ORDER
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
                migrateCase
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
                migrateCase
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
                migrateCase,
                CLAIMANT_RESPONSE_CUI
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
                APPLICATION_OFFLINE_UPDATE_CLAIM,
                migrateCase,
                CLAIMANT_RESPONSE_CUI
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
                CREATE_SDO,
                APPLICATION_OFFLINE_UPDATE_CLAIM,
                migrateCase
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
                APPLICATION_OFFLINE_UPDATE_CLAIM,
                migrateCase
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
                HEARING_SCHEDULED,
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
                RECORD_JUDGMENT,
                TRANSFER_ONLINE_CASE,
                CLAIMANT_RESPONSE_CUI,
                asyncStitchingComplete,
                REQUEST_FOR_RECONSIDERATION
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
                REFER_TO_JUDGE,
                migrateCase
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
                APPLICATION_CLOSED_UPDATE_CLAIM,
                REFER_TO_JUDGE,
                migrateCase
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
                REFER_TO_JUDGE,
                APPLICATION_CLOSED_UPDATE_CLAIM,
                migrateCase
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
                CREATE_SDO
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
                REFER_TO_JUDGE,
                migrateCase
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
                REFER_TO_JUDGE,
                migrateCase
            )
        ),
        entry(
            PAST_CLAIM_DISMISSED_DEADLINE_AWAITING_CAMUNDA.fullName(),
            List.of(
                DISMISS_CLAIM,
                migrateCase,
                CREATE_SDO,
                NotSuitable_SDO
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
                ADD_UNAVAILABLE_DATES
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
                asyncStitchingComplete
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
            List.of(NOC_REQUEST,
                    APPLY_NOC_DECISION,
                    ADD_CASE_NOTE,
                    INITIATE_GENERAL_APPLICATION,
                    CREATE_SDO,
                    NotSuitable_SDO,
                    migrateCase,
                    CREATE_CLAIM_SPEC_AFTER_PAYMENT,
                    CREATE_CLAIM_AFTER_PAYMENT
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
                NotSuitable_SDO,
                migrateCase
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
                NotSuitable_SDO,
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
                asyncStitchingComplete
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
                NotSuitable_SDO,
                HEARING_SCHEDULED,
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
                asyncStitchingComplete
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
                NotSuitable_SDO,
                migrateCase,
                CHANGE_SOLICITOR_EMAIL
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
                NotSuitable_SDO,
                migrateCase,
                CHANGE_SOLICITOR_EMAIL,
                LIP_CLAIM_SETTLED
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
                NotSuitable_SDO,
                migrateCase,
                DEFAULT_JUDGEMENT_SPEC,
                CHANGE_SOLICITOR_EMAIL,
                LIP_CLAIM_SETTLED,
                CLAIMANT_RESPONSE_CUI
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
                NotSuitable_SDO,
                APPLICATION_OFFLINE_UPDATE_CLAIM,
                migrateCase,
                DEFAULT_JUDGEMENT_SPEC,
                REQUEST_JUDGEMENT_ADMISSION_SPEC,
                CHANGE_SOLICITOR_EMAIL,
                LIP_CLAIM_SETTLED,
                CLAIMANT_RESPONSE_CUI
            )
        ),

        entry(
            PART_ADMISSION.fullName(),
            List.of(
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
                NotSuitable_SDO,
                APPLICATION_OFFLINE_UPDATE_CLAIM,
                migrateCase,
                DEFAULT_JUDGEMENT_SPEC,
                CHANGE_SOLICITOR_EMAIL,
                REQUEST_JUDGEMENT_ADMISSION_SPEC,
                LIP_CLAIM_SETTLED
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
                NotSuitable_SDO,
                migrateCase,
                CHANGE_SOLICITOR_EMAIL,
                LIP_CLAIM_SETTLED
            )
        ),

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
                NotSuitable_SDO,
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
                SET_ASIDE_JUDGMENT,
                JUDGMENT_PAID_IN_FULL,
                RECORD_JUDGMENT,
                LIP_CLAIM_SETTLED,
                TRANSFER_ONLINE_CASE,
                asyncStitchingComplete,
                CLAIMANT_RESPONSE_CUI,
                UPLOAD_MEDIATION_DOCUMENTS,
                REQUEST_FOR_RECONSIDERATION
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
                NotSuitable_SDO,
                REFER_TO_JUDGE,
                migrateCase,
                CHANGE_SOLICITOR_EMAIL,
                LIP_CLAIM_SETTLED
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
                migrateCase
            )
        ),
        entry(
            PAST_CLAIM_NOTIFICATION_DEADLINE_AWAITING_CAMUNDA.fullName(),
            List.of(
                DISMISS_CLAIM,
                migrateCase
            )
        ),
        entry(
            PAST_CLAIM_DETAILS_NOTIFICATION_DEADLINE_AWAITING_CAMUNDA.fullName(),
            List.of(
                DISMISS_CLAIM,
                migrateCase
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
                NotSuitable_SDO,
                migrateCase
            )
        ),
        entry(
            AWAITING_RESPONSES_NOT_FULL_DEFENCE_RECEIVED.fullName(),
            List.of(
                DEFENDANT_RESPONSE_SPEC,
                DEFENDANT_RESPONSE_CUI,
                RESET_PIN,
                migrateCase
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
                LIP_CLAIM_SETTLED
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
                NotSuitable_SDO,
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
                asyncStitchingComplete,
                UPLOAD_MEDIATION_DOCUMENTS
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
                NotSuitable_SDO,
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
                asyncStitchingComplete,
                CLAIMANT_RESPONSE_CUI
            )
        ),
        entry(
            RESPONDENT_RESPONSE_LANGUAGE_IS_BILINGUAL.fullName(),
            List.of(
                CHANGE_SOLICITOR_EMAIL,
                EXTEND_RESPONSE_DEADLINE,
                LIP_CLAIM_SETTLED

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
                asyncStitchingComplete
            )
        ),
        entry(
            PREPARE_FOR_HEARING_CONDUCT_HEARING.fullName(),
            List.of(
                asyncStitchingComplete,
                UPLOAD_MEDIATION_DOCUMENTS
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
                REQUEST_JUDGEMENT_ADMISSION_SPEC
            )
        ),
        entry(
            FULL_ADMIT_AGREE_REPAYMENT.fullName(),
            List.of(
                DEFENDANT_SIGN_SETTLEMENT_AGREEMENT,
                REQUEST_JUDGEMENT_ADMISSION_SPEC
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
        if (caseEvent.equals(migrateCase)) {
            return true;
        }

        if (caseEvent.equals(NOTIFY_HEARING_PARTIES)) {
            return true;
        }

        if (caseEvent.equals(MANAGE_CONTACT_INFORMATION)) {
            return true;
        }

        if (caseEvent.equals(CASE_PROCEEDS_IN_CASEMAN)) {
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
