package uk.gov.hmcts.reform.unspec.callback;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CaseEvent {
    CREATE_CLAIM("Create claim"),
    CONFIRM_SERVICE("Confirm service"),
    REQUEST_EXTENSION("Request extension"),
    RESPOND_EXTENSION("Respond to extension request"),
    MOVE_TO_STAYED("Move case to stayed"),
    ACKNOWLEDGE_SERVICE("Acknowledge service"),
    DEFENDANT_RESPONSE("Respond to claim"),
    CLAIMANT_RESPONSE("View and respond to defence"),
    WITHDRAW_CLAIM("Withdraw claim"),
    DISCONTINUE_CLAIM("Discontinue claim"),
    NOTIFY_RESPONDENT_SOLICITOR1_FOR_CLAIM_ISSUE("Notify claim issue"),
    NOTIFY_APPLICANT_SOLICITOR1_FOR_REQUEST_FOR_EXTENSION("Notify request for extension"),
    NOTIFY_RESPONDENT_SOLICITOR1_FOR_EXTENSION_RESPONSE("Notify extension response"),
    NOTIFY_APPLICANT_SOLICITOR1_FOR_SERVICE_ACKNOWLEDGEMENT("Notify service acknowledgement"),
    NOTIFY_APPLICANT_SOLICITOR1_FOR_DEFENDANT_RESPONSE("Notify response"),
    NOTIFY_RESPONDENT_SOLICITOR1_FOR_CASE_HANDED_OFFLINE("Notify handed offline"),
    NOTIFY_APPLICANT_SOLICITOR1_FOR_CASE_HANDED_OFFLINE("Notify handed offline"),
    NOTIFY_RESPONDENT_SOLICITOR1_FOR_CASE_TRANSFERRED_TO_LOCAL_COURT("Notify transferred local court"),
    NOTIFY_APPLICANT_SOLICITOR1_FOR_CASE_TRANSFERRED_TO_LOCAL_COURT("Notify transferred local court");

    private final String displayName;
}
