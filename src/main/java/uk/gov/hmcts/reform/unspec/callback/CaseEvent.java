package uk.gov.hmcts.reform.unspec.callback;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CaseEvent {
    CREATE_CLAIM("CREATE_CLAIM", "Create claim"),
    CONFIRM_SERVICE("CONFIRM_SERVICE", "Confirm service"),
    REQUEST_EXTENSION("REQUEST_EXTENSION", "Request extension"),
    RESPOND_EXTENSION("RESPOND_EXTENSION", "Respond to extension request"),
    MOVE_TO_STAYED("MOVE_TO_STAYED", "Move case to stayed"),
    ACKNOWLEDGE_SERVICE("ACKNOWLEDGE_SERVICE", "Acknowledge service"),
    DEFENDANT_RESPONSE("DEFENDANT_RESPONSE", "Respond to claim"),
    CLAIMANT_RESPONSE("CLAIMANT_RESPONSE", "View and respond to defence"),
    WITHDRAW_CLAIM("WITHDRAW_CLAIM", "Withdraw claim"),
    DISCONTINUE_CLAIM("DISCONTINUE_CLAIM", "Discontinue claim"),
    NOTIFY_DEFENDANT_SOLICITOR_FOR_CLAIM_ISSUE(
        "NOTIFY_DEFENDANT_SOLICITOR_FOR_CLAIM_ISSUE",
        "Notify defendant solicitor"
    );

    private final String value;
    private final String displayName;
}
