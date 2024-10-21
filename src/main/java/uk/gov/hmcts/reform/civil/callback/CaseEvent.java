package uk.gov.hmcts.reform.civil.callback;

import static uk.gov.hmcts.reform.civil.callback.UserType.CAMUNDA;
import static uk.gov.hmcts.reform.civil.callback.UserType.TESTING_SUPPORT;
import static uk.gov.hmcts.reform.civil.callback.UserType.USER;

public enum CaseEvent {
    CREATE_CLAIM(USER),
    CREATE_CLAIM_SPEC(USER),
    CREATE_SERVICE_REQUEST_CLAIM(USER),
    CREATE_CLAIM_SPEC_AFTER_PAYMENT(USER),
    CREATE_CLAIM_AFTER_PAYMENT(USER),
    CREATE_LIP_CLAIM(USER),
    ENTER_BREATHING_SPACE_SPEC(USER),
    LIFT_BREATHING_SPACE_SPEC(USER),
    NO_REMISSION_HWF(USER),
    FULL_REMISSION_HWF(USER),
    NOTIFY_DEFENDANT_OF_CLAIM(USER),
    NOTIFY_DEFENDANT_OF_CLAIM_DETAILS(USER),
    ADD_OR_AMEND_CLAIM_DOCUMENTS(USER),
    ACKNOWLEDGE_CLAIM(USER),
    ACKNOWLEDGEMENT_OF_SERVICE(USER),
    ADD_DEFENDANT_LITIGATION_FRIEND(USER),
    INFORM_AGREED_EXTENSION_DATE(USER),
    INFORM_AGREED_EXTENSION_DATE_SPEC(USER),
    MEDIATION_SUCCESSFUL(USER),
    MEDIATION_UNSUCCESSFUL(USER),

    DEFENDANT_RESPONSE(USER),
    DEFENDANT_RESPONSE_SPEC(USER),
    DEFENDANT_RESPONSE_CUI(USER),
    CLAIMANT_RESPONSE_CUI(USER),
    UPLOAD_TRANSLATED_DOCUMENT(USER),
    MANAGE_DOCUMENTS(USER),
    CLAIMANT_RESPONSE(USER),
    CLAIMANT_RESPONSE_SPEC(USER),
    WITHDRAW_CLAIM(USER),
    DISCONTINUE_CLAIM(USER),
    SETTLE_CLAIM(USER),
    SETTLE_CLAIM_MARK_PAID_FULL(USER),
    DISCONTINUE_CLAIM_CLAIMANT(USER),
    VALIDATE_DISCONTINUE_CLAIM_CLAIMANT(USER),
    DISMISS_CLAIM(USER),
    CREATE_CASE_FLAGS(USER),
    MANAGE_CASE_FLAGS(USER),
    CITIZEN_CLAIM_ISSUE_PAYMENT(USER),
    CITIZEN_HEARING_FEE_PAYMENT(USER),

    HEARING_FEE_UNPAID(USER),
    HEARING_FEE_PAID(USER),
    NO_HEARING_FEE_DUE(USER),
    REQUEST_FOR_RECONSIDERATION_DEADLINE_CHECK(USER),
    MAIN_CASE_CLOSED(USER),
    APPLICATION_PROCEEDS_IN_HERITAGE(USER),
    CASE_PROCEEDS_IN_CASEMAN(USER),
    RESUBMIT_CLAIM(USER),
    AMEND_PARTY_DETAILS(USER),
    CHANGE_SOLICITOR_EMAIL(USER),
    TAKE_CASE_OFFLINE(USER),
    TRIAL_READY_CHECK(USER),
    TRIAL_READY_NOTIFICATION(USER),
    MOVE_TO_DECISION_OUTCOME(USER),
    ADD_CASE_NOTE(USER),
    DEFAULT_JUDGEMENT_SPEC(USER),
    DEFAULT_JUDGEMENT_NON_DIVERGENT_SPEC(CAMUNDA),
    DEFAULT_JUDGEMENT(USER),
    REQUEST_JUDGEMENT_ADMISSION_SPEC(USER),
    JUDGEMENT_BY_ADMISSION_NON_DIVERGENT_SPEC(CAMUNDA),
    INITIATE_GENERAL_APPLICATION(USER),
    CHECK_PAID_IN_FULL_SCHED_DEADLINE(CAMUNDA),
    CREATE_SDO(USER),
    REFER_TO_JUDGE(USER),
    STANDARD_DIRECTION_ORDER_DJ(USER),
    HEARING_SCHEDULED(USER),
    NotSuitable_SDO(USER),
    EVIDENCE_UPLOAD_JUDGE(USER),
    SERVICE_REQUEST_RECEIVED(USER),
    NOC_REQUEST(USER),
    APPLY_NOC_DECISION(USER),
    BUNDLE_CREATION_CHECK(USER),
    RESET_PIN(USER),
    GENERATE_DIRECTIONS_ORDER(USER),
    EVIDENCE_UPLOAD_APPLICANT(USER),
    EVIDENCE_UPLOAD_RESPONDENT(USER),
    EVIDENCE_UPLOAD_CHECK(USER),
    CREATE_BUNDLE(USER),
    asyncStitchingComplete(USER),
    EXTEND_RESPONSE_DEADLINE(USER),
    ADD_UNAVAILABLE_DATES(USER),
    COURT_OFFICER_ORDER(USER),

    REVIEW_HEARING_EXCEPTION(USER),
    UpdateNextHearingInfo(USER),

    UPDATE_NEXT_HEARING_DETAILS(USER),

    ASSIGN_LIP_DEFENDANT(USER),

    MANAGE_CONTACT_INFORMATION(USER),
    CONTACT_INFORMATION_UPDATED(CAMUNDA),
    CONTACT_INFORMATION_UPDATED_WA(CAMUNDA),

    UPLOAD_MEDIATION_DOCUMENTS(USER),
    CUI_UPLOAD_MEDIATION_DOCUMENTS(USER),

    APPLICANT_TRIAL_READY_NOTIFY_OTHERS(USER),
    RESPONDENT1_TRIAL_READY_NOTIFY_OTHERS(USER),
    RESPONDENT2_TRIAL_READY_NOTIFY_OTHERS(USER),
    GENERATE_TRIAL_READY_DOCUMENT_APPLICANT(USER),
    GENERATE_TRIAL_READY_DOCUMENT_RESPONDENT1(USER),
    GENERATE_TRIAL_READY_DOCUMENT_RESPONDENT2(USER),
    LIP_CLAIM_SETTLED(USER),
    DEFENDANT_SIGN_SETTLEMENT_AGREEMENT(USER),
    TRIGGER_TASK_RECONFIG(USER),
    APPLY_HELP_WITH_HEARING_FEE(USER),
    MORE_INFORMATION_HWF(USER),
    NOTIFY_FORMER_SOLICITOR(CAMUNDA),
    NOTIFY_OTHER_SOLICITOR_1(CAMUNDA),
    NOTIFY_OTHER_SOLICITOR_2(CAMUNDA),
    TRIAL_READINESS(USER),
    BUNDLE_CREATION_NOTIFICATION(USER),
    RECORD_JUDGMENT(USER),
    EDIT_JUDGMENT(USER),
    SET_ASIDE_JUDGMENT(USER),
    JUDGMENT_PAID_IN_FULL(USER),
    REFER_JUDGE_DEFENCE_RECEIVED(USER),
    TRANSFER_ONLINE_CASE(USER),
    REQUEST_FOR_RECONSIDERATION(USER),
    REQUEST_FOR_RECONSIDERATION_NOTIFICATION_CUI_CLAIMANT(USER),
    REQUEST_FOR_RECONSIDERATION_NOTIFICATION_CUI_DEFENDANT(USER),
    UPDATE_HELP_WITH_FEE_NUMBER(USER),
    DECISION_ON_RECONSIDERATION_REQUEST(USER),
    EVIDENCE_UPLOADED(USER),
    UPLOAD_TRANSLATED_DOCUMENT_ORDER_NOTICE(USER),
    AMEND_RESTITCH_BUNDLE(USER),
    STAY_CASE(USER),
    MANAGE_STAY(USER),
    DISMISS_CASE(USER),
    STAY_LIFTED(USER),
    STAY_UPDATE_REQUESTED(USER),
    PARTIAL_REMISSION_HWF_GRANTED(USER),
    ASSIGN_CASE_TO_APPLICANT_SOLICITOR1(CAMUNDA),
    ASSIGN_CASE_TO_APPLICANT1(CAMUNDA),
    TRIGGER_APPLICATION_CLOSURE(CAMUNDA),
    APPLICATION_CLOSED_UPDATE_CLAIM(CAMUNDA),
    APPLY_NOC_DECISION_LIP(CAMUNDA),
    APPLY_NOC_DECISION_DEFENDANT_LIP(CAMUNDA),
    TRIGGER_APPLICATION_PROCEEDS_IN_HERITAGE(CAMUNDA),
    APPLICATION_OFFLINE_UPDATE_CLAIM(CAMUNDA),
    NOTIFY_APPLICANT_SOLICITOR1_BREATHING_SPACE_ENTER(CAMUNDA),
    NOTIFY_RESPONDENT_SOLICITOR1_BREATHING_SPACE_ENTER(CAMUNDA),
    NOTIFY_RESPONDENT_SOLICITOR2_BREATHING_SPACE_ENTER(CAMUNDA),
    NOTIFY_APPLICANT_SOLICITOR1_BREATHING_SPACE_LIFTED(CAMUNDA),
    NOTIFY_RESPONDENT_SOLICITOR1_BREATHING_SPACE_LIFTED(CAMUNDA),
    NOTIFY_LIP_RESPONDENT_FOR_SIGN_SETTLEMENT_AGREEMENT(CAMUNDA),
    NOTIFY_LIP_APPLICANT_FOR_SIGN_SETTLEMENT_AGREEMENT(CAMUNDA),
    NOTIFY_RESPONDENT_SOLICITOR1_FOR_CLAIM_ISSUE(CAMUNDA),
    NOTIFY_RESPONDENT_SOLICITOR2_FOR_CLAIM_ISSUE(CAMUNDA),
    NOTIFY_RESPONDENT_SOLICITOR1_FOR_CLAIM_ISSUE_CC(CAMUNDA),
    NOTIFY_RESPONDENT_SOLICITOR1_FOR_CLAIM_DETAILS(CAMUNDA),
    NOTIFY_RESPONDENT_SOLICITOR2_FOR_CLAIM_DETAILS(CAMUNDA),
    NOTIFY_RESPONDENT_SOLICITOR1_FOR_CLAIM_DETAILS_CC(CAMUNDA),
    NOTIFY_APPLICANT_SOLICITOR1_FOR_REQUEST_FOR_EXTENSION(CAMUNDA),
    NOTIFY_RESPONDENT_SOLICITOR1_FOR_EXTENSION_RESPONSE(CAMUNDA),
    NOTIFY_APPLICANT_SOLICITOR1_FOR_CLAIM_ACKNOWLEDGEMENT(CAMUNDA),
    NOTIFY_APPLICANT_SOLICITOR1_FOR_CLAIM_ACKNOWLEDGEMENT_CC(CAMUNDA),
    NOTIFY_APPLICANT_SOLICITOR1_FOR_SPEC_CLAIM_ACKNOWLEDGEMENT(CAMUNDA),
    NOTIFY_APPLICANT_SOLICITOR1_FOR_SPEC_CLAIM_ACKNOWLEDGEMENT_CC(CAMUNDA),
    NOTIFY_APPLICANT_SOLICITOR1_FOR_DEFENDANT_RESPONSE(CAMUNDA),
    NOTIFY_RESPONDENT_SOLICITOR1_FOR_DEFENDANT_RESPONSE_CC(CAMUNDA),
    NOTIFY_APPLICANT_SOLICITOR1_FOR_DEFENDANT_RESPONSE_CC(CAMUNDA),
    NOTIFY_RESPONDENT_SOLICITOR2_FOR_DEFENDANT_RESPONSE_CC(CAMUNDA),
    NOTIFY_RESPONDENT_SOLICITOR1_FOR_CASE_HANDED_OFFLINE(CAMUNDA),
    NOTIFY_RESPONDENT_SOLICITOR2_FOR_CASE_HANDED_OFFLINE(CAMUNDA),
    NOTIFY_APPLICANT_SOLICITOR1_FOR_CASE_HANDED_OFFLINE(CAMUNDA),
    NOTIFY_RESPONDENT_SOLICITOR1_FOR_TRIAL_READY(CAMUNDA),
    NOTIFY_RESPONDENT_SOLICITOR2_FOR_TRIAL_READY(CAMUNDA),
    NOTIFY_APPLICANT_SOLICITOR1_FOR_TRIAL_READY(CAMUNDA),
    NOTIFY_APPLICANT_MORE_INFORMATION_NEEDED(CAMUNDA),
    GENERATE_ORDER_NOTIFICATION(CAMUNDA),

    NOTIFY_APPLICANT_SOLICITOR1_FOR_GENERATE_ORDER(CAMUNDA),
    NOTIFY_RESPONDENT_SOLICITOR1_FOR_GENERATE_ORDER(CAMUNDA),
    NOTIFY_RESPONDENT_SOLICITOR2_FOR_GENERATE_ORDER(CAMUNDA),
    NOTIFY_APPLICANT_SOLICITOR_FOR_OTHER_TRIAL_READY(CAMUNDA),
    NOTIFY_RESPONDENT_SOLICITOR1_FOR_OTHER_TRIAL_READY(CAMUNDA),
    NOTIFY_RESPONDENT_SOLICITOR2_FOR_OTHER_TRIAL_READY(CAMUNDA),

    NOTIFY_APPLICANT_SOLICITOR1_FOR_FAILED_PAYMENT(CAMUNDA),
    NOTIFY_APPLICANT_SOLICITOR1_FOR_FAILED_PAYMENT_SPEC(CAMUNDA),
    NOTIFY_APPLICANT_SOLICITOR1_FOR_RESPONDENT_LITIGANT_IN_PERSON(CAMUNDA),
    NOTIFY_APPLICANT_SOLICITOR1_FOR_RESPONDENT_LITIGANT_IN_PERSON_SPEC(CAMUNDA),
    NOTIFY_APPLICANT_SOLICITOR1_FOR_AGREED_EXTENSION_DATE(CAMUNDA),
    NOTIFY_APPLICANT_SOLICITOR1_FOR_AGREED_EXTENSION_DATE_FOR_SPEC(CAMUNDA),
    NOTIFY_APPLICANT_SOLICITOR1_FOR_AGREED_EXTENSION_DATE_CC(CAMUNDA),
    NOTIFY_RESPONDENT_SOLICITOR2_FOR_AGREED_EXTENSION_DATE_CC(CAMUNDA),
    NOTIFY_APPLICANT_SOLICITOR1_FOR_AGREED_EXTENSION_DATE_FOR_SPEC_CC(CAMUNDA),
    NOTIFY_APPLICANT_RESPONDENT2_FOR_AGREED_EXTENSION_DATE_FOR_SPEC_CC(CAMUNDA),
    NOTIFY_APPLICANT_SOLICITOR1_FOR_CLAIM_CONTINUING_ONLINE(CAMUNDA),
    NOTIFY_APPLICANT_SOLICITOR1_FOR_CLAIM_CONTINUING_ONLINE_SPEC(CAMUNDA),
    NOTIFY_APPLICANT1_FOR_CLAIM_CONTINUING_ONLINE_SPEC(CAMUNDA),
    NOTIFY_LIP_DEFENDANT_RESPONSE_SUBMISSION(CAMUNDA),
    NOTIFY_LIP_DEFENDANT_PART_ADMIT_CLAIM_SETTLED(CAMUNDA),
    NOTIFY_RESPONDENT1_FOR_CLAIM_CONTINUING_ONLINE_SPEC(CAMUNDA),
    NOTIFY_RESPONDENT_SOLICITOR1_FOR_CLAIM_CONTINUING_ONLINE_SPEC(CAMUNDA),
    NOTIFY_RESPONDENT_SOLICITOR2_FOR_CLAIM_CONTINUING_ONLINE_SPEC(CAMUNDA),
    NOTIFY_RESPONDENT_SOLICITOR1_FOR_CLAIMANT_CONFIRMS_TO_PROCEED(CAMUNDA),
    NOTIFY_RESPONDENT_SOLICITOR2_FOR_CLAIMANT_CONFIRMS_TO_PROCEED(CAMUNDA),
    NOTIFY_RESPONDENT_SOLICITOR1_FOR_CLAIMANT_CONFIRMS_TO_PROCEED_CC(CAMUNDA),
    NOTIFY_RES_SOLICITOR1_FOR_CLAIMANT_CONFIRMS_TO_PROCEED_MULTITRACK(CAMUNDA),
    NOTIFY_RES_SOLICITOR2_FOR_CLAIMANT_CONFIRMS_TO_PROCEED_MULTITRACK(CAMUNDA),
    NOTIFY_APP_SOLICITOR1_FOR_CLAIMANT_CONFIRMS_TO_PROCEED_CC_MULTITRACK(CAMUNDA),
    NOTIFY_RESPONDENT_SOLICITOR1_FOR_CLAIMANT_CONFIRMS_NOT_TO_PROCEED(CAMUNDA),
    NOTIFY_RESPONDENT_SOLICITOR1_FOR_CLAIMANT_CONFIRMS_NOT_TO_PROCEED_LIP(CAMUNDA),
    NOTIFY_RESPONDENT_SOLICITOR2_FOR_CLAIMANT_CONFIRMS_NOT_TO_PROCEED(CAMUNDA),
    NOTIFY_RESPONDENT_SOLICITOR1_FOR_CLAIMANT_CONFIRMS_NOT_TO_PROCEED_CC(CAMUNDA),
    NOTIFY_RESPONDENT_SOLICITOR1_FOR_CASE_TAKEN_OFFLINE(CAMUNDA),
    NOTIFY_RESPONDENT_SOLICITOR2_FOR_CASE_TAKEN_OFFLINE(CAMUNDA),
    NOTIFY_APPLICANT_SOLICITOR1_FOR_CASE_TAKEN_OFFLINE(CAMUNDA),
    NOTIFY_APPLICANT_SOLICITOR1_FOR_CASE_TAKEN_OFFLINE_SPEC(CAMUNDA),
    NOTIFY_RESPONDENT_SOLICITOR1_FOR_LITIGATION_FRIEND_ADDED(CAMUNDA),
    NOTIFY_RESPONDENT_SOLICITOR2_FOR_LITIGATION_FRIEND_ADDED(CAMUNDA),
    NOTIFY_APPLICANT_SOLICITOR1_FOR_LITIGATION_FRIEND_ADDED(CAMUNDA),
    NOTIFY_RESPONDENT_SOLICITOR1_FOR_CASE_PROCEEDS_IN_CASEMAN(CAMUNDA),
    NOTIFY_RESPONDENT_SOLICITOR2_FOR_CASE_PROCEEDS_IN_CASEMAN(CAMUNDA),
    NOTIFY_APPLICANT_SOLICITOR1_FOR_CASE_PROCEEDS_IN_CASEMAN(CAMUNDA),
    NOTIFY_APPLICANT_SOLICITOR1_FOR_CLAIM_DISMISSED(CAMUNDA),
    NOTIFY_RESPONDENT_SOLICITOR1_FOR_CLAIM_DISMISSED(CAMUNDA),
    NOTIFY_RESPONDENT_SOLICITOR2_FOR_CLAIM_DISMISSED(CAMUNDA),
    NOTIFY_APPLICANTS_SOLICITOR_SDO_TRIGGERED(CAMUNDA),
    NOTIFY_RESPONDENT_SOLICITOR1_SDO_TRIGGERED(CAMUNDA),
    NOTIFY_RESPONDENT_SOLICITOR2_SDO_TRIGGERED(CAMUNDA),
    NOTIFY_RESPONDENT1_FOR_CLAIMANT_AGREED_REPAYMENT(CAMUNDA),
    NOTIFY_CLAIMANT_FOR_RESPONDENT1_REJECT_REPAYMENT(CAMUNDA),
    NOTIFY_CLAIMANT_FOR_SUCCESSFUL_PAYMENT(CAMUNDA),
    NOTIFY_RESPONDENT1_FOR_REQUEST_JUDGEMENT_BY_ADMISSION(CAMUNDA),
    NOTIFY_LIP_DEFENDANT_REJECT_REPAYMENT(CAMUNDA),
    NOTIFY_APPLICANT_MEDIATION_AGREEMENT(CAMUNDA),
    NOTIFY_RESPONDENT_MEDIATION_AGREEMENT(CAMUNDA),
    NOTIFY_RESPONDENT2_MEDIATION_AGREEMENT(CAMUNDA),
    NOTIFY_APPLICANT_MEDIATION_SUCCESSFUL(CAMUNDA),
    NOTIFY_RESPONDENT_MEDIATION_SUCCESSFUL(CAMUNDA),
    NOTIFY_INTERIM_JUDGMENT_CLAIMANT(CAMUNDA),
    NOTIFY_INTERIM_JUDGMENT_DEFENDANT(CAMUNDA),
    NOTIFY_CLAIMANT_CUI_FOR_DEADLINE_EXTENSION(CAMUNDA),
    NOTIFY_DEFENDANT_CUI_FOR_DEADLINE_EXTENSION(CAMUNDA),
    NOTIFY_DIRECTION_ORDER_DJ_CLAIMANT(CAMUNDA),
    NOTIFY_DIRECTION_ORDER_DJ_DEFENDANT(CAMUNDA),
    NOTIFY_DIRECTION_ORDER_DJ_DEFENDANT2(CAMUNDA),
    NOTIFY_CLAIM_RECONSIDERATION_UPHELD_CLAIMANT(CAMUNDA),
    NOTIFY_CLAIM_RECONSIDERATION_UPHELD_DEFENDANT(CAMUNDA),
    NOTIFY_DJ_NON_DIVERGENT_SPEC_CLAIMANT(CAMUNDA),
    NOTIFY_DJ_NON_DIVERGENT_SPEC_DEFENDANT1_LR(CAMUNDA),
    NOTIFY_DJ_NON_DIVERGENT_SPEC_DEFENDANT1_LIP(CAMUNDA),
    NOTIFY_DJ_NON_DIVERGENT_SPEC_DEFENDANT2_LR(CAMUNDA),
    NOTIFY_CLAIM_SET_ASIDE_JUDGMENT_CLAIMANT(CAMUNDA),
    NOTIFY_CLAIM_SET_ASIDE_JUDGMENT_DEFENDANT1(CAMUNDA),
    NOTIFY_CLAIM_SET_ASIDE_JUDGMENT_DEFENDANT1_LIP(CAMUNDA),
    NOTIFY_CLAIM_SET_ASIDE_JUDGMENT_DEFENDANT2(CAMUNDA),
    SEND_SET_ASIDE_JUDGEMENT_IN_ERROR_LETTER_TO_LIP_DEFENDANT1(CAMUNDA),
    POST_JO_DEFENDANT1_PIN_IN_LETTER(CAMUNDA),
    JUDGMENT_BY_ADMISSION_DEFENDANT1_PIN_IN_LETTER(CAMUNDA),
    NOTIFY_APPLICANT_SOLICITOR1_FOR_CONTACT_DETAILS_CHANGE(CAMUNDA),
    NOTIFY_APPLICANT_SOLICITOR1_FOR_DEFENDANT_RESPONSE_CUI(CAMUNDA),
    NOTIFY_RESPONDENT_SOLICITOR1_FOR_HEARING_FEE_UNPAID(CAMUNDA),
    NOTIFY_RESPONDENT_SOLICITOR2_FOR_HEARING_FEE_UNPAID(CAMUNDA),
    NOTIFY_APPLICANT_SOLICITOR1_FOR_HEARING_FEE_UNPAID(CAMUNDA),
    NOTIFY_APPLICANT_SOLICITOR_FOR_HEARING_FEE_AFTER_NOC(CAMUNDA),
    NOTIFY_CLAIMANT_JUDGMENT_VARIED_DETERMINATION_OF_MEANS(CAMUNDA),
    NOTIFY_SOLICITOR1_DEFENDANT_JUDGMENT_VARIED_DETERMINATION_OF_MEANS(CAMUNDA),
    NOTIFY_SOLICITOR2_DEFENDANT_JUDGMENT_VARIED_DETERMINATION_OF_MEANS(CAMUNDA),
    NOTIFY_DEFENDANT1_LIP_JUDGMENT_VARIED_DETERMINATION_OF_MEANS(CAMUNDA),
    SETTLE_CLAIM_MARKED_PAID_IN_FULL(CAMUNDA),
    NOTIFY_SOLICITOR1_DEFENDANT_SETTLE_CLAIM_MARKED_PAID_IN_FULL(CAMUNDA),
    NOTIFY_SOLICITOR2_DEFENDANT_SETTLE_CLAIM_MARKED_PAID_IN_FULL(CAMUNDA),
    SEND_SETTLE_CLAIM_PAID_IN_FULL_LETTER_TO_LIP_DEFENDANT1(CAMUNDA),

    DISPATCH_BUSINESS_PROCESS(CAMUNDA),
    START_BUSINESS_PROCESS(CAMUNDA),
    START_BUSINESS_PROCESS_GASPEC(CAMUNDA),
    END_BUSINESS_PROCESS(CAMUNDA),

    MAKE_PBA_PAYMENT(CAMUNDA),
    MAKE_PBA_PAYMENT_SPEC(CAMUNDA),
    MAKE_BULK_CLAIM_PAYMENT(CAMUNDA),
    VALIDATE_FEE(CAMUNDA),
    VALIDATE_FEE_SPEC(CAMUNDA),

    CREATE_SERVICE_REQUEST_API(CAMUNDA),
    MAKE_SERVICE_REQUEST_PBA_PAYMENT(CAMUNDA),

    GENERATE_CLAIM_FORM(CAMUNDA),
    GENERATE_CLAIM_FORM_SPEC(CAMUNDA),
    GENERATE_DRAFT_FORM(CAMUNDA),

    PROCESS_FULL_DEFENCE(CAMUNDA),
    PROCESS_FULL_DEFENCE_SPEC(CAMUNDA),
    PROCESS_PART_ADMISSION_DEFENCE_SPEC(CAMUNDA),
    PROCESS_CLAIM_ISSUE(CAMUNDA),
    PROCESS_CLAIM_ISSUE_SPEC(CAMUNDA),
    PROCESS_PAYMENT_FAILED(CAMUNDA),

    PROCEEDS_IN_HERITAGE_SYSTEM(CAMUNDA),
    PROCEEDS_IN_HERITAGE_SYSTEM_SPEC(CAMUNDA),
    GENERATE_ACKNOWLEDGEMENT_OF_CLAIM(CAMUNDA),
    GENERATE_ACKNOWLEDGEMENT_OF_CLAIM_SPEC(CAMUNDA),
    GENERATE_DIRECTIONS_QUESTIONNAIRE(CAMUNDA),
    GENERATE_RESPONSE_DQ_LIP_SEALED(CAMUNDA),
    GENERATE_RESPONSE_SEALED(CAMUNDA),
    GENERATE_RESPONSE_CUI_SEALED(CAMUNDA),
    NOTIFY_RPA_ON_CASE_HANDED_OFFLINE(CAMUNDA),
    NOTIFY_RPA_ON_CONTINUOUS_FEED(CAMUNDA),
    RETRY_NOTIFY_RPA_ON_CASE_HANDED_OFFLINE(CAMUNDA),
    RESET_RPA_NOTIFICATION_BUSINESS_PROCESS(CAMUNDA),

    UPDATE_CASE_DETAILS_AFTER_NOC(CAMUNDA),

    UPDATE_CASE_DATA(TESTING_SUPPORT),
    UPDATE_GA_CASE_DATA(TESTING_SUPPORT),

    GENERATE_DJ_FORM(CAMUNDA),
    GENERATE_DJ_FORM_SPEC(CAMUNDA),
    GEN_DJ_FORM_NON_DIVERGENT_SPEC_CLAIMANT(CAMUNDA),
    GEN_DJ_FORM_NON_DIVERGENT_SPEC_DEFENDANT(CAMUNDA),
    POST_DJ_NON_DIVERGENT_PIN_IN_LETTER_DEFENDANT1(CAMUNDA),
    GENERATE_HEARING_FORM(CAMUNDA),
    NOTIFY_APPLICANT_SOLICITOR_DJ_RECEIVED(CAMUNDA),
    NOTIFY_RESPONDENT_SOLICITOR_DJ_RECEIVED(CAMUNDA),
    NOTIFY_CASEWORKER_DJ_RECEIVED(CAMUNDA),
    ADD_PDF_TO_MAIN_CASE(CAMUNDA),
    JUDICIAL_REFERRAL(CAMUNDA),
    migrateCase(CAMUNDA),
    NOTIFY_CLAIMANT_HEARING(CAMUNDA),
    NOTIFY_DEFENDANT1_HEARING(CAMUNDA),
    NOTIFY_DEFENDANT2_HEARING(CAMUNDA),
    NOTIFY_RPA_DJ_UNSPEC(CAMUNDA),
    NOTIFY_RPA_DJ_SPEC(CAMUNDA),
    GENERATE_TRIAL_READY_FORM_APPLICANT(CAMUNDA),
    GENERATE_TRIAL_READY_FORM_RESPONDENT1(CAMUNDA),
    GENERATE_TRIAL_READY_FORM_RESPONDENT2(CAMUNDA),
    TRIGGER_LOCATION_UPDATE(USER),
    TRIGGER_UPDATE_GA_LOCATION(CAMUNDA),
    NOTIFY_RESPONDENT_SOLICITOR1_FOR_BUNDLE_CREATED(CAMUNDA),
    NOTIFY_RESPONDENT_SOLICITOR2_FOR_BUNDLE_CREATED(CAMUNDA),
    NOTIFY_APPLICANT_SOLICITOR1_FOR_BUNDLE_CREATED(CAMUNDA),
    NOTIFY_CLAIMANT_TRANSLATED_DOCUMENT_UPLOADED(CAMUNDA),
    NOTIFY_DEFENDANT_TRANSLATED_DOCUMENT_UPLOADED(CAMUNDA),
    NOTIFY_MEDIATION_UNSUCCESSFUL_CLAIMANT_LR(CAMUNDA),
    NOTIFY_MEDIATION_UNSUCCESSFUL_DEFENDANT_1_LR(CAMUNDA),
    NOTIFY_MEDIATION_UNSUCCESSFUL_DEFENDANT_2_LR(CAMUNDA),
    NOTIFY_MEDIATION_UNSUCCESSFUL_DEFENDANT_LIP(CAMUNDA),
    NOTIFY_JUDGMENT_VARIED_DETERMINATION_OF_MEANS(CAMUNDA),
    NOTIFY_MEDIATION_SUCCESSFUL_DEFENDANT_2_LR(CAMUNDA),
    NOTIFY_MEDIATION_SUCCESSFUL_DEFENDANT_LIP(CAMUNDA),

    SEND_SDO_ORDER_TO_LIP_DEFENDANT(CAMUNDA),
    SEND_SDO_ORDER_TO_LIP_CLAIMANT(CAMUNDA),
    UPDATE_MISSING_FIELDS(CAMUNDA),

    START_HEARING_NOTICE_BUSINESS_PROCESS(CAMUNDA),
    END_HEARING_NOTICE_BUSINESS_PROCESS(CAMUNDA),
    NOTIFY_HEARING_PARTIES(CAMUNDA),
    GENERATE_HEARING_NOTICE_HMC(CAMUNDA),
    NOTIFY_CLAIMANT_HEARING_HMC(CAMUNDA),
    NOTIFY_DEFENDANT1_HEARING_HMC(CAMUNDA),
    NOTIFY_DEFENDANT2_HEARING_HMC(CAMUNDA),
    CREATE_SERVICE_REQUEST_API_HMC(CAMUNDA),
    UPDATE_PARTIES_NOTIFIED_HMC(CAMUNDA),
    UPDATE_CASE_PROGRESS_HMC(CAMUNDA),
    UPDATE_CLAIMANT_INTENTION_CLAIM_STATE(CAMUNDA),
    NOTIFY_APPLICANT1_CLAIM_SUBMITTED(CAMUNDA),
    NOTIFY_DEFENDANT_CLAIMANT_SETTLE_THE_CLAIM(CAMUNDA),
    NOTIFY_DEFENDANT_LIP_CLAIMANT_REPRESENTED(CAMUNDA),
    NOTIFY_APPLICANT_LIP_SOLICITOR(CAMUNDA),
    NOTIFY_CLAIMANT_LIP_AFTER_NOC_APPROVAL(CAMUNDA),
    NOTIFY_DEFENDANT_AFTER_NOC_APPROVAL(CAMUNDA),
    NOTIFY_DEFENDANT_SOLICITOR_AFTER_NOC_APPROVAL(CAMUNDA),
    NOTIFY_CLAIMANT_DEFENDANT_REPRESENTED(CAMUNDA),
    SEND_CVP_JOIN_LINK(USER),
    SET_LIP_RESPONDENT_RESPONSE_DEADLINE(CAMUNDA),
    NOTIFY_LIP_RESPONDENT_CLAIMANT_CONFIRM_TO_PROCEED(CAMUNDA),
    NOTIFY_LIP_APPLICANT_CLAIMANT_CONFIRM_TO_PROCEED(CAMUNDA),
    CREATE_SERVICE_REQUEST_CUI_CLAIM_ISSUE(CAMUNDA),
    NOTIFY_APPLICANT1_FOR_REQUEST_JUDGEMENT_BY_ADMISSION_LIP_CLAIMANT(CAMUNDA),
    NOTIFY_RESPONDENT1_FOR_REQUEST_JUDGEMENT_BY_ADMISSION_LIP_CLAIMANT(CAMUNDA),
    TRIGGER_TASK_RECONFIG_GA(CAMUNDA),
    NOTIFY_APPLICANT1_GENERIC_TEMPLATE(CAMUNDA),
    SEND_HEARING_TO_LIP_DEFENDANT(CAMUNDA),
    SEND_HEARING_TO_LIP_CLAIMANT(CAMUNDA),
    SEND_FINAL_ORDER_TO_LIP_DEFENDANT(CAMUNDA),
    SEND_FINAL_ORDER_TO_LIP_CLAIMANT(CAMUNDA),
    SEND_TRANSLATED_ORDER_TO_LIP_DEFENDANT(CAMUNDA),
    SEND_TRANSLATED_ORDER_TO_LIP_CLAIMANT(CAMUNDA),
    GENERATE_LIP_CLAIMANT_MANUAL_DETERMINATION(CAMUNDA),
    GENERATE_LIP_SIGN_SETTLEMENT_AGREEMENT_FORM(CAMUNDA),
    UPDATE_CLAIM_STATE_AFTER_DOC_UPLOADED(CAMUNDA),
    NOTIFY_CLAIMANT_LIP_HELP_WITH_FEES(CAMUNDA),
    RETRIGGER_CASES(CAMUNDA),
    RETRIGGER_CLAIMANT_RESPONSE(CAMUNDA),
    RETRIGGER_CLAIMANT_RESPONSE_SPEC(CAMUNDA),
    RETRIGGER_NOTIFY_INTERIM_JUDGMENT_DEFENDANT(CAMUNDA),
    GENERATE_DEFAULT_JUDGMENT_BY_ADMISSION_RESPONSE_DOC(CAMUNDA),
    GENERATE_JUDGMENT_BY_ADMISSION_RESPONSE_DOC(CAMUNDA),
    GENERATE_JUDGMENT_BY_DETERMINATION_RESPONSE_DOC(CAMUNDA),
    GENERATE_INTERLOCUTORY_JUDGEMENT_DOCUMENT(CAMUNDA),
    UPLOAD_TRANSLATED_DOCUMENT_CLAIM_ISSUE(CAMUNDA),
    UPLOAD_TRANSLATED_DOCUMENT_CLAIMANT_INTENTION(CAMUNDA),
    NOTIFY_LIP_RESPONDENT_CLAIMANT_CONFIRM_TO_PROCEED_TRANSLATED_DOC(CAMUNDA),
    POST_DJ_NON_DIVERGENT_LETTER_DEFENDANT1(CAMUNDA),
    POST_DJ_NON_DIVERGENT_LETTER_DEFENDANT2(CAMUNDA),
    SET_SETTLEMENT_AGREEMENT_DEADLINE(CAMUNDA),
    GENERATE_LIP_CLAIMANT_CLAIM_FORM_SPEC(CAMUNDA),
    GENERATE_LIP_DEFENDANT_CLAIM_FORM_SPEC(CAMUNDA),
    NOTIFY_LIP_CLAIMANT_CLAIM_SUBMISSION(CAMUNDA),
    GENERATE_DASHBOARD_NOTIFICATION_CLAIM_FEE_REQUIRED_CLAIMANT1(CAMUNDA),
    CLAIMANT1_HWF_DASHBOARD_NOTIFICATION(CAMUNDA),
    INVALID_HWF_REFERENCE(USER),
    FEE_PAYMENT_OUTCOME(USER),
    NOTIFY_LIP_CLAIMANT_HWF_OUTCOME(CAMUNDA),
    GENERATE_DASHBOARD_NOTIFICATION_CUI(CAMUNDA),
    CREATE_DASHBOARD_NOTIFICATION_FOR_CLAIM_ISSUE_FOR_APPLICANT1(CAMUNDA),
    CREATE_DASHBOARD_NOTIFICATION_FOR_CLAIM_ISSUE_FOR_RESPONDENT1(CAMUNDA),
    CREATE_CLAIMANT_DASHBOARD_NOTIFICATION_FOR_CLAIMANT_RESPONSE(CAMUNDA),
    GENERATE_DASHBOARD_NOTIFICATION_CLAIM_ISSUE_HWF_CLAIMANT1(CAMUNDA),
    CREATE_DASHBOARD_NOTIFICATION_FOR_CCJ_REQUEST_FOR_APPLICANT1(CAMUNDA),
    CREATE_DASHBOARD_NOTIFICATION_FOR_CLAIM_SETTLED_FOR_DEFENDANT1(CAMUNDA),
    CREATE_CLAIMANT_DASHBOARD_NOTIFICATION_FOR_DEFENDANT_RESPONSE(CAMUNDA),
    CREATE_CLAIMANT_DASHBOARD_NOTIFICATION_FOR_DEFENDANT_RESPONSE_WELSH(CAMUNDA),
    CREATE_DEFENDANT_DASHBOARD_NOTIFICATION_FOR_DEFENDANT_RESPONSE(CAMUNDA),
    CREATE_DASHBOARD_NOTIFICATION_FOR_CLAIM_SETTLED_FOR_CLAIMANT1(CAMUNDA),
    CREATE_DEFENDANT_DASHBOARD_NOTIFICATION_FOR_CLAIMANT_RESPONSE(CAMUNDA),
    CREATE_CLAIMANT_CCJ_DASHBOARD_NOTIFICATION_FOR_CLAIMANT_RESPONSE(CAMUNDA),
    CREATE_DEFENDANT_CCJ_DASHBOARD_NOTIFICATION_FOR_CLAIMANT_RESPONSE(CAMUNDA),
    CREATE_DASHBOARD_NOTIFICATION_FOR_SETTLEMENT_DEFENDANT_RESPONSE(CAMUNDA),
    CREATE_DASHBOARD_NOTIFICATION_FOR_CCJ_REQUEST_FOR_RESPONDENT1(CAMUNDA),
    CREATE_CLAIMANT_DASHBOARD_NOTIFICATION_FOR_MEDIATION_UNSUCCESSFUL(CAMUNDA),
    CREATE_DASHBOARD_NOTIFICATION_MORE_TIME_REQUEST_FOR_RESPONDENT1(CAMUNDA),
    CREATE_CLAIMANT_DASHBOARD_NOTIFICATION_DEFENDANT_RESPONSE_DATE(CAMUNDA),
    CREATE_DASHBOARD_NOTIFICATION_FOR_MEDIATION_SUCCESSFUL_FOR_APPLICANT(CAMUNDA),
    CREATE_DASHBOARD_NOTIFICATION_FOR_MEDIATION_SUCCESSFUL_FOR_RESPONDENT(CAMUNDA),
    RECORD_JUDGMENT_NOTIFICATION(CAMUNDA),
    NOTIFY_RESPONDENT1_FOR_RECORD_JUDGMENT(CAMUNDA),
    NOTIFY_RESPONDENT2_FOR_RECORD_JUDGMENT(CAMUNDA),
    NOTIFY_APPLICANT_FOR_RECORD_JUDGMENT(CAMUNDA),
    CREATE_DASHBOARD_NOTIFICATION_FOR_DEFENDANT_RESPONSE_DEADLINE_CLAIMANT(CAMUNDA),
    DEFENDANT_RESPONSE_DEADLINE_CHECK(CAMUNDA),
    CREATE_DASHBOARD_NOTIFICATION_RESPONSE_DEADLINE_DEFENDANT(CAMUNDA),
    CREATE_DASHBOARD_NOTIFICATION_CP_TRIAL_ARRANGEMENTS_CLAIMANT(CAMUNDA),
    CREATE_DASHBOARD_NOTIFICATION_CP_TRIAL_ARRANGEMENTS_DEFENDANT(CAMUNDA),
    CREATE_DASHBOARD_NOTIFICATION_HEARING_SCHEDULED_CLAIMANT(CAMUNDA),

    CREATE_DASHBOARD_NOTIFICATION_HEARING_SCHEDULED_CLAIMANT_HMC(CAMUNDA),
    CREATE_DASHBOARD_NOTIFICATION_HEARING_SCHEDULED_DEFENDANT(CAMUNDA),
    CREATE_DASHBOARD_NOTIFICATION_FOR_HEARING_FEE_UNPAID_FOR_CLAIMANT1(CAMUNDA),
    CREATE_DASHBOARD_NOTIFICATION_FOR_HEARING_FEE_UNPAID_FOR_DEFENDANT1(CAMUNDA),
    CREATE_DASHBOARD_NOTIFICATION_FINAL_ORDER_CLAIMANT(CAMUNDA),
    CREATE_DASHBOARD_NOTIFICATION_FINAL_ORDER_DEFENDANT(CAMUNDA),
    CREATE_DASHBOARD_NOTIFICATION_DJ_SDO_CLAIMANT(CAMUNDA),
    CREATE_DASHBOARD_NOTIFICATION_DJ_SDO_DEFENDANT(CAMUNDA),
    CREATE_DASHBOARD_NOTIFICATION_DJ_NON_DIVERGENT_DEFENDANT(CAMUNDA),
    CREATE_DASHBOARD_NOTIFICATION_DJ_NON_DIVERGENT_CLAIMANT(CAMUNDA),
    CREATE_DASHBOARD_NOTIFICATION_JUDGEMENT_BY_ADMISSION_DEFENDANT(CAMUNDA),
    CREATE_DASHBOARD_NOTIFICATION_JUDGEMENT_BY_ADMISSION_CLAIMANT(CAMUNDA),
    CREATE_DASHBOARD_NOTIFICATION_SDO_CLAIMANT(CAMUNDA),
    CREATE_DASHBOARD_NOTIFICATION_SDO_DEFENDANT(CAMUNDA),
    CREATE_DASHBOARD_NOTIFICATION_TRIAL_ARRANGEMENTS_NOTIFY_CLAIMANT(CAMUNDA),
    CREATE_DASHBOARD_NOTIFICATION_TRIAL_ARRANGEMENTS_NOTIFY_DEFENDANT(CAMUNDA),
    CREATE_DEFENDANT_DASHBOARD_NOTIFICATION_FOR_MEDIATION_UNSUCCESSFUL(CAMUNDA),
    CREATE_DASHBOARD_NOTIFICATION_FOR_BUNDLE_CREATED_FOR_CLAIMANT1(CAMUNDA),

    CREATE_DASHBOARD_NOTIFICATION_FOR_BUNDLE_CREATED_FOR_DEFENDANT1(CAMUNDA),
    CREATE_DASHBOARD_NOTIFICATION_HEARING_FEE_PAID_CLAIMANT(CAMUNDA),
    CREATE_DASHBOARD_NOTIFICATION_HELP_FEE_IN_REVIEW_CLAIMANT(CAMUNDA),
    CREATE_DASHBOARD_NOTIFICATION_UPLOAD_HEARING_DOCUMENTS_CLAIMANT(CAMUNDA),
    CREATE_DASHBOARD_NOTIFICATION_UPLOAD_HEARING_DOCUMENTS_DEFENDANT(CAMUNDA),
    CREATE_DASHBOARD_NOTIFICATION_EVIDENCE_UPLOADED_CLAIMANT(CAMUNDA),
    CREATE_DASHBOARD_NOTIFICATION_EVIDENCE_UPLOADED_DEFENDANT(CAMUNDA),
    CREATE_CLAIMANT_DASHBOARD_NOTIFICATION_FOR_CASE_PROCEED_OFFLINE(CAMUNDA),
    CREATE_DEFENDANT_DASHBOARD_NOTIFICATION_FOR_CASE_PROCEED_OFFLINE(CAMUNDA),
    CREATE_DASHBOARD_NOTIFICATION_SET_ASIDE_JUDGEMENT_CLAIMANT(CAMUNDA),
    GEN_JUDGMENT_BY_ADMISSION_DOC_CLAIMANT(CAMUNDA),
    GEN_JUDGMENT_BY_ADMISSION_DOC_DEFENDANT(CAMUNDA),
    CREATE_DASHBOARD_NOTIFICATION_SET_ASIDE_JUDGMENT_DEFENDANT(CAMUNDA),
    CREATE_DASHBOARD_NOTIFICATION_RECORD_JUDGMENT_DEFENDANT(CAMUNDA),
    GEN_JUDGMENT_BY_DETERMINATION_DOC_DEFENDANT(CAMUNDA),
    GEN_JUDGMENT_BY_DETERMINATION_DOC_CLAIMANT(CAMUNDA),
    CREATE_NOTIFICATION_REQUEST_FOR_RECONSIDERATION_CLAIMANT(CAMUNDA),
    CREATE_NOTIFICATION_REQUEST_FOR_RECONSIDERATION_DEFENDANT(CAMUNDA),
    CREATE_CLAIMANT_DASHBOARD_NOTIFICATION_FOR_DEFENDANT_NOC(CAMUNDA),
    NOTIFY_DEFENDANT_JUDGMENT_BY_ADMISSION(CAMUNDA),
    NOTIFY_CLAIMANT_JUDGMENT_BY_ADMISSION(CAMUNDA),
    UPDATE_DASHBOARD_TASK_LIST_DEFENDANT_DECISION_OUTCOME(CAMUNDA),
    UPDATE_DASHBOARD_TASK_LIST_CLAIMANT_DECISION_OUTCOME(CAMUNDA),
    CREATE_DASHBOARD_NOTIFICATION_DECISION_RECONSIDERATION_CLAIMANT1(CAMUNDA),
    CREATE_DASHBOARD_NOTIFICATION_DECISION_RECONSIDERATION_DEFENDANT1(CAMUNDA),
    NOTIFY_CLAIMANT_UPLOADED_DOCUMENT_ORDER_NOTICE(CAMUNDA),
    NOTIFY_DEFENDANT_UPLOADED_DOCUMENT_ORDER_NOTICE(CAMUNDA),
    NOTIFY_VALIDATION_DICONTINUANCE_FAILURE_CLAIMANT(CAMUNDA),
    UPDATE_VISIBILITY_NOTICE_OF_DISCONTINUANCE(CAMUNDA),
    GEN_NOTICE_OF_DISCONTINUANCE(CAMUNDA),
    NOTIFY_DISCONTINUANCE_DEFENDANT1(CAMUNDA),
    NOTIFY_DISCONTINUANCE_CLAIMANT1(CAMUNDA),
    SEND_DISCONTINUANCE_LETTER_LIP_DEFENDANT1(CAMUNDA),
    NOTIFY_DISCONTINUANCE_DEFENDANT2(CAMUNDA),
    NOTIFY_CLAIMANT_AMEND_RESTITCH_BUNDLE(CAMUNDA),
    NOTIFY_DEFENDANT_AMEND_RESTITCH_BUNDLE(CAMUNDA),
    SEND_JUDGMENT_DETAILS_CJES(CAMUNDA),
    SEND_JUDGMENT_DETAILS_CJES_SA(CAMUNDA),
    CREATE_DASHBOARD_NOTIFICATION_AMEND_RESTITCH_BUNDLE_CLAIMANT(CAMUNDA),
    CREATE_DASHBOARD_NOTIFICATION_AMEND_RESTITCH_BUNDLE_DEFENDANT(CAMUNDA),
    NOTIFY_CLAIMANT_STAY_CASE(CAMUNDA),
    NOTIFY_DEFENDANT_STAY_CASE(CAMUNDA),
    NOTIFY_CLAIMANT_DISMISS_CASE(CAMUNDA),
    NOTIFY_DEFENDANT_DISMISS_CASE(CAMUNDA),
    CREATE_DASHBOARD_NOTIFICATION_DISMISS_CASE_CLAIMANT(CAMUNDA),
    CREATE_DASHBOARD_NOTIFICATION_DISMISS_CASE_DEFENDANT(CAMUNDA),
    CREATE_DASHBOARD_NOTIFICATION_STAY_CASE_CLAIMANT(CAMUNDA),
    CREATE_DASHBOARD_NOTIFICATION_STAY_CASE_DEFENDANT(CAMUNDA),
    CREATE_DASHBOARD_NOTIFICATION_APPLICATION_PROCEED_OFFLINE_CLAIMANT(CAMUNDA),
    CREATE_DASHBOARD_NOTIFICATION_APPLICATION_PROCEED_OFFLINE_DEFENDANT(CAMUNDA),
    PROCESS_COSC_APPLICATION(CAMUNDA),
    NOTIFY_CLAIMANT_STAY_LIFTED(CAMUNDA),
    NOTIFY_DEFENDANT_STAY_LIFTED(CAMUNDA),
    NOTIFY_DEFENDANT2_STAY_LIFTED(CAMUNDA),
    CHECK_AND_MARK_PAID_IN_FULL(CAMUNDA),
    NOTIFY_CLAIMANT_STAY_UPDATE_REQUESTED(CAMUNDA),
    NOTIFY_DEFENDANT_STAY_UPDATE_REQUESTED(CAMUNDA),
    NOTIFY_DEFENDANT2_STAY_UPDATE_REQUESTED(CAMUNDA);

    private final UserType userType;

    CaseEvent(UserType userType) {
        this.userType = userType;
    }

    public UserType getUserType() {
        return userType;
    }

    public boolean isCamundaEvent() {
        return this.getUserType() == CAMUNDA;
    }
}
