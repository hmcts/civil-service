package uk.gov.hmcts.reform.unspec.callback;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import static uk.gov.hmcts.reform.unspec.callback.UserType.CAMUNDA;
import static uk.gov.hmcts.reform.unspec.callback.UserType.USER;

@Getter
@RequiredArgsConstructor
public enum CaseEvent {
    CREATE_CLAIM("Create claim", USER),
    CONFIRM_SERVICE("Confirm service", USER),
    REQUEST_EXTENSION("Request extension", USER),
    RESPOND_EXTENSION("Respond to extension request", USER),
    MOVE_TO_STAYED("Move case to stayed", USER),
    ACKNOWLEDGE_SERVICE("Acknowledge service", USER),
    DEFENDANT_RESPONSE("Respond to claim", USER),
    CLAIMANT_RESPONSE("View and respond to defence", USER),
    WITHDRAW_CLAIM("Withdraw claim", USER),
    DISCONTINUE_CLAIM("Discontinue claim", USER),
    NOTIFY_RESPONDENT_SOLICITOR1_FOR_CLAIM_ISSUE("Notify claim issue", CAMUNDA),
    NOTIFY_APPLICANT_SOLICITOR1_FOR_REQUEST_FOR_EXTENSION("Notify request for extension", CAMUNDA),
    NOTIFY_RESPONDENT_SOLICITOR1_FOR_EXTENSION_RESPONSE("Notify extension response", CAMUNDA),
    NOTIFY_APPLICANT_SOLICITOR1_FOR_SERVICE_ACKNOWLEDGEMENT("Notify service acknowledgement", CAMUNDA),
    NOTIFY_APPLICANT_SOLICITOR1_FOR_DEFENDANT_RESPONSE("Notify response", CAMUNDA),
    NOTIFY_RESPONDENT_SOLICITOR1_FOR_CASE_HANDED_OFFLINE("Notify handed offline", CAMUNDA),
    NOTIFY_APPLICANT_SOLICITOR1_FOR_CASE_HANDED_OFFLINE("Notify handed offline", CAMUNDA),
    NOTIFY_RESPONDENT_SOLICITOR1_FOR_CASE_TRANSFERRED_TO_LOCAL_COURT(
        "Notify transferred local court",
        CAMUNDA
    ),
    NOTIFY_APPLICANT_SOLICITOR1_FOR_CASE_TRANSFERRED_TO_LOCAL_COURT(
        "Notify transferred local court",
        CAMUNDA
    ),
    DISPATCH_BUSINESS_PROCESS("Dispatch business process", CAMUNDA),
    START_BUSINESS_PROCESS("Start business process", CAMUNDA),
    END_BUSINESS_PROCESS("End business process", CAMUNDA),
    TESTING_SUPPORT_RESET_BUSINESS_PROCESS("Reset business process", USER),
    MAKE_PBA_PAYMENT("Make PBA payment", CAMUNDA),
    GENERATE_CERTIFICATE_OF_SERVICE("Generate certificate of service", CAMUNDA);

    private final String displayName;
    private final UserType userType;

    public boolean isCamundaEvent() {
        return this.getUserType() == CAMUNDA;
    }
}
