package uk.gov.hmcts.reform.unspec.service.flowstate;

import static org.springframework.util.StringUtils.hasLength;

public interface FlowState {

    String fullName();

    static FlowState fromFullName(String fullName) {
        if (!hasLength(fullName)) {
            throw new IllegalArgumentException("Invalid full name:" + fullName);
        }
        int lastIndexOfDot = fullName.lastIndexOf('.');
        String flowStateName = fullName.substring(lastIndexOfDot + 1);
        String flowName = fullName.substring(0, lastIndexOfDot);
        if (flowName.equals("MAIN")) {
            return Main.valueOf(flowStateName);
        } else {
            throw new IllegalArgumentException("Invalid flow name:" + flowName);
        }
    }

    enum Main implements FlowState {
        DRAFT,
        PENDING_CASE_ISSUED,
        PAYMENT_SUCCESSFUL,
        PAYMENT_FAILED,
        AWAITING_CASE_NOTIFICATION,
        AWAITING_CASE_DETAILS_NOTIFICATION,
        EXTENSION_REQUESTED,
        CLAIM_ISSUED,
        CLAIM_ACKNOWLEDGED,
        RESPONDENT_FULL_DEFENCE,
        RESPONDENT_FULL_ADMISSION,
        RESPONDENT_PART_ADMISSION,
        RESPONDENT_COUNTER_CLAIM,
        FULL_DEFENCE_PROCEED,
        FULL_DEFENCE_NOT_PROCEED,
        CLAIM_WITHDRAWN,
        CLAIM_DISCONTINUED,
        CASE_PROCEEDS_IN_CASEMAN,
        TAKEN_OFFLINE_PAST_APPLICANT_RESPONSE_DEADLINE,
        PROCEEDS_OFFLINE_UNREPRESENTED_DEFENDANT,
        PENDING_CLAIM_ISSUED_UNREGISTERED_DEFENDANT,
        PROCEEDS_OFFLINE_ADMIT_OR_COUNTER_CLAIM,
        CLAIM_DISMISSED_DEFENDANT_OUT_OF_TIME,
        CLAIM_DISMISSED_PAST_CLAIM_NOTIFICATION_DEADLINE,
        CLAIM_DISMISSED_PAST_CLAIM_DETAILS_NOTIFICATION_DEADLINE;

        public static final String FLOW_NAME = "MAIN";

        @Override
        public String fullName() {
            return FLOW_NAME + "." + name();
        }
    }
}
